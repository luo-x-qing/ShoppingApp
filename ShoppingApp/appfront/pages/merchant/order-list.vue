<template>
  <view class="container">
    <!-- 状态筛选标签 -->
    <view class="tabs">
      <view 
        class="tab" 
        :class="activeTab === status.value ? 'active' : ''" 
        v-for="status in tabList" 
        :key="status.value"
        @click="switchTab(status.value)"
      >
        {{ status.name }}
      </view>
    </view>

    <!-- 订单列表 -->
    <view class="order-list" v-if="orderList.length > 0">
      <view class="order-card" v-for="order in orderList" :key="order.id">
        <!-- 订单头部 -->
        <view class="order-header">
          <view class="order-info">
            <text class="order-id">订单号：{{ order.id }}</text>
            <text class="order-time">{{ formatDate(order.createTime) }}</text>
          </view>
          <view class="order-status" :class="getStatusClass(order.status)">
            {{ getStatusText(order.status) }}
          </view>
        </view>

        <!-- 订单内容 -->
        <view class="order-content">
          <view class="hotel-info">
            <text class="hotel-name">{{ order.hotelName }}</text>
            <text class="room-type">{{ order.roomTypeName }}</text>
          </view>
          <view class="date-info">
            <text>入住：{{ order.checkIn }}</text>
            <text>退房：{{ order.checkOut }}</text>
          </view>
          <view class="customer-info">
            <text>联系人：{{ order.contactName || order.username }}</text>
            <text>电话：{{ order.contactPhone || '--' }}</text>
          </view>
          <view class="price-info">
            <text>订单金额：¥{{ order.price }}</text>
          </view>
          
          <!-- 取消申请信息 -->
          <view v-if="order.status === '取消申请中'" class="cancel-request-info">
            <text class="cancel-label">取消申请：</text>
            <text class="cancel-reason">{{ order.cancelReason || '用户申请取消' }}</text>
          </view>
          
          <!-- 取消拒绝原因 -->
          <view v-if="order.status === '取消拒绝'" class="reject-info">
            <text class="reject-label">拒绝原因：</text>
            <text class="reject-reason">{{ order.rejectReason || '商家拒绝了取消申请' }}</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="order-actions" v-if="order.status !== '已取消' && order.status !== '取消拒绝' && order.status !== '已完成'">
          <!-- 待支付订单：无需操作，等待用户支付 -->
          <view v-if="order.status === '待支付'" class="waiting-tip">
            <text>等待用户支付</text>
          </view>
          
          <!-- 待确认订单：可确认订单 -->
          <view v-if="order.status === '待确认'" class="action-buttons">
            <button class="btn btn-confirm" @click="confirmOrder(order)">确认订单</button>
          </view>
          
          <!-- 已支付订单：可确认订单、确认入住 -->
          <view v-if="order.status === '已支付'" class="action-buttons">
            <button class="btn btn-confirm" @click="confirmOrder(order)">确认订单</button>
            <button class="btn btn-checkin" @click="handleCheckIn(order)">确认入住</button>
          </view>
          
          <!-- 已确认订单：可确认入住 -->
          <view v-if="order.status === '已确认'" class="action-buttons">
            <button class="btn btn-checkin" @click="handleCheckIn(order)">确认入住</button>
          </view>
          
          <!-- 已入住订单：可确认退房（完成订单） -->
          <view v-if="order.status === '已入住'" class="action-buttons">
            <button class="btn btn-checkout" @click="confirmCheckOut(order)">确认退房</button>
          </view>
          
          <!-- 取消申请中订单：同意或拒绝取消 -->
          <view v-if="order.status === '取消申请中'" class="action-buttons">
            <button class="btn btn-reject" @click="showRejectDialog(order)">拒绝取消</button>
            <button class="btn btn-approve" @click="approveCancel(order)">同意取消</button>
          </view>
        </view>
        
        <!-- 已取消订单提示 -->
        <view v-if="order.status === '已取消'" class="cancelled-tip">
          <text>该订单已取消</text>
        </view>
        
        <!-- 取消拒绝订单提示 -->
        <view v-if="order.status === '取消拒绝'" class="rejected-tip">
          <text>已拒绝取消申请</text>
        </view>
        
        <!-- 已完成订单提示 -->
        <view v-if="order.status === '已完成'" class="completed-tip">
          <text>✅ 订单已完成</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-box" v-else>
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无订单</text>
    </view>

    <!-- 拒绝取消弹窗 -->
    <view class="dialog-mask" v-if="showRejectDialogFlag" @click="closeRejectDialog">
      <view class="dialog-container" @click.stop>
        <view class="dialog-header">
          <text class="dialog-title">拒绝取消申请</text>
          <text class="close-btn" @click="closeRejectDialog">✕</text>
        </view>
        <view class="dialog-content">
          <view class="input-area">
            <text class="input-label">拒绝原因</text>
            <textarea 
              class="reason-input" 
              v-model="rejectReason"
              placeholder="请输入拒绝原因（将展示给用户）"
              maxlength="200"
              auto-height
            />
          </view>
          <button class="btn-submit" @click="rejectCancel">确认拒绝</button>
        </view>
      </view>
    </view>
    
    <!-- 入住时间未到提示弹窗 -->
    <view class="dialog-mask" v-if="showDateWarning" @click="showDateWarning = false">
      <view class="warning-dialog" @click.stop>
        <view class="warning-icon">⚠️</view>
        <view class="warning-title">无法确认入住</view>
        <view class="warning-message">{{ warningMessage }}</view>
        <button class="warning-btn" @click="showDateWarning = false">我知道了</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      activeTab: 'all',
      orderList: [],
      merchantId: null,
      
      // 拒绝弹窗相关
      showRejectDialogFlag: false,
      rejectOrderId: null,
      rejectReason: '',
      
      // 入住时间校验相关
      showDateWarning: false,
      warningMessage: '',
      
      // 商家名称
      merchantName: ''
    };
  },
  
  computed: {
    tabList() {
      return [
        { name: '全部', value: 'all' },
        { name: '待支付', value: 'pending' },
        { name: '待确认', value: 'waitConfirm' },
        { name: '已支付', value: 'paid' },
        { name: '已确认', value: 'confirmed' },
        { name: '已入住', value: 'checkin' },
        { name: '已完成', value: 'completed' },
        { name: '取消申请', value: 'cancelling' },
        { name: '已取消', value: 'cancelled' }
      ];
    }
  },
  
  onShow() {
    this.loadMerchantInfo();
  },
  
  methods: {
    // 加载商家信息
    loadMerchantInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo');
        if (userInfo && userInfo.id) {
          this.merchantId = userInfo.id;
          this.merchantName = userInfo.shopName || userInfo.name || '商家';
          this.loadOrdersByMerchant();
        } else {
          uni.showToast({ title: '请先登录', icon: 'none' });
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login-register/login-register' });
          }, 1500);
        }
      } catch (e) {
        console.error('读取商家信息失败', e);
      }
    },
    
    // 直接通过商家ID加载订单
    loadOrdersByMerchant() {
      if (!this.merchantId) return;
      
      uni.showLoading({ title: '加载中...' });
      
      uni.request({
        url: `http://localhost:8080/api/hotel-orders/merchant/orders`,
        method: 'GET',
        data: {
          merchantId: this.merchantId
        },
        success: (res) => {
          uni.hideLoading();
          console.log('订单列表返回：', res.data);
          
          if (res.data && res.data.code === 200) {
            let orders = res.data.data || [];
            // 🔥 在加载订单后自动检查过期订单
            this.checkAndCancelExpiredOrders(orders);
            this.processOrders(orders);
          } else {
            this.loadAllOrders();
          }
        },
        fail: (err) => {
          console.error('获取订单失败', err);
          this.loadAllOrders();
        }
      });
    },
    
    // 降级方案：获取所有订单
    loadAllOrders() {
      uni.request({
        url: `http://localhost:8080/api/hotel-orders`,
        method: 'GET',
        success: (res) => {
          uni.hideLoading();
          console.log('获取所有订单：', res.data);
          
          if (res.data && res.data.code === 200) {
            let orders = res.data.data || [];
            this.checkAndCancelExpiredOrders(orders);
            this.processOrders(orders);
          } else {
            this.orderList = [];
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('获取订单失败', err);
          uni.showToast({ title: '获取订单失败', icon: 'none' });
          this.orderList = [];
        }
      });
    },
    
    // ========== 🔥 核心方法：检查并自动取消过期订单 ==========
    /**
     * 检查所有待确认和已支付的订单，如果入住日期已过且商家未确认，自动取消
     */
    checkAndCancelExpiredOrders(orders) {
      if (!orders || orders.length === 0) return;
      
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      // 需要自动取消的状态：待确认、已支付
      const autoCancelStatuses = ['待确认', '已支付'];
      
      // 找出需要取消的订单
      const expiredOrders = orders.filter(order => {
        // 只处理待确认和已支付的订单
        if (!autoCancelStatuses.includes(order.status)) return false;
        
        // 检查入住日期
        if (!order.checkIn) return false;
        
        const checkIn = new Date(order.checkIn);
        checkIn.setHours(0, 0, 0, 0);
        
        // 如果入住日期 <= 今天，说明已经过了入住时间
        return checkIn <= today;
      });
      
      if (expiredOrders.length === 0) return;
      
      console.log(`发现 ${expiredOrders.length} 个订单已过入住日期未确认，自动取消`);
      
      // 逐个取消过期订单
      expiredOrders.forEach(order => {
        this.cancelExpiredOrder(order);
      });
    },
    
    /**
     * 取消过期的订单（调用后端接口）
     */
    cancelExpiredOrder(order) {
      console.log(`自动取消订单 ${order.id}，入住日期：${order.checkIn}，状态：${order.status}`);
      
      // 构建取消原因
      const cancelReason = `商家未在入住日期(${order.checkIn})前确认订单，系统自动取消`;
      
      // 调用取消接口（这里需要根据您的后端API调整）
      uni.request({
        url: `http://localhost:8080/api/hotel-orders/${order.id}/auto-cancel`,
        method: 'POST',
        data: {
          reason: cancelReason
        },
        success: (res) => {
          console.log(`订单 ${order.id} 自动取消失败`, res.data);
          
          // 如果专用接口不存在，尝试使用取消申请接口（需要后端支持商家直接取消）
          // 或者使用更新状态接口
          this.updateOrderStatus(order.id, '已取消', cancelReason);
        },
        fail: (err) => {
          console.error(`订单 ${order.id} 自动取消失败`, err);
          // 尝试备用方案
          this.updateOrderStatus(order.id, '已取消', cancelReason);
        }
      });
    },
    
    /**
     * 备用方案：直接更新订单状态
     * 如果后端没有专用接口，直接调用更新接口
     */
    updateOrderStatus(orderId, status, reason) {
      uni.request({
        url: `http://localhost:8080/api/hotel-orders/${orderId}`,
        method: 'PUT',
        data: {
          status: status,
          cancelReason: reason || '订单过期自动取消'
        },
        success: (res) => {
          console.log(`订单 ${orderId} 状态已更新为 ${status}`, res.data);
        },
        fail: (err) => {
          console.error(`更新订单 ${orderId} 状态失败`, err);
        }
      });
    },
    
    // 处理订单数据
    processOrders(orders) {
      console.log('原始订单数量：', orders.length);
      
      orders.forEach(order => {
        console.log(`订单ID: ${order.id}, 酒店ID: ${order.hotelId}, 状态: "${order.status}"`);
      });
      
      if (this.activeTab !== 'all') {
        orders = orders.filter(order => this.filterByStatus(order.status));
        console.log('筛选后订单数量：', orders.length);
      }
      
      orders.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
      this.orderList = orders;
    },
    
    // 状态筛选映射
    filterByStatus(status) {
      const statusMap = {
        'pending': '待支付',
        'waitConfirm': '待确认',
        'paid': '已支付',
        'confirmed': '已确认',
        'checkin': '已入住',
        'completed': '已完成',
        'cancelling': '取消申请中',
        'cancelled': '已取消'
      };
      return statusMap[this.activeTab] === status;
    },
    
    // 切换标签
    switchTab(status) {
      this.activeTab = status;
      this.loadOrdersByMerchant();
    },
    
    // ========== 入住时间校验 ==========
    /**
     * 检查是否可以确认入住
     * @param {string} checkInDate - 入住日期 (格式: YYYY-MM-DD)
     * @returns {boolean} - 是否可以入住
     */
    canCheckIn(checkInDate) {
      if (!checkInDate) return false;
      
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const checkIn = new Date(checkInDate);
      checkIn.setHours(0, 0, 0, 0);
      
      // 入住日期 <= 今天 才能办理入住
      return checkIn <= today;
    },
    
    /**
     * 获取入住校验失败的原因
     * @param {string} checkInDate - 入住日期
     * @returns {string} - 失败原因
     */
    getCheckInFailReason(checkInDate) {
      if (!checkInDate) return '订单缺少入住日期信息';
      
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const checkIn = new Date(checkInDate);
      checkIn.setHours(0, 0, 0, 0);
      
      const diffDays = Math.ceil((checkIn - today) / (1000 * 60 * 60 * 24));
      
      if (checkIn > today) {
        return `订单入住时间为 ${checkInDate}，距离入住还有 ${diffDays} 天，请于 ${checkInDate} 当天或之后办理入住`;
      }
      
      return '未知原因';
    },
    
    // 处理确认入住（带校验）
    handleCheckIn(order) {
      // 校验入住时间
      if (!this.canCheckIn(order.checkIn)) {
        this.warningMessage = this.getCheckInFailReason(order.checkIn);
        this.showDateWarning = true;
        return;
      }
      
      // 校验通过，弹出确认框
      uni.showModal({
        title: '确认入住',
        content: `确认订单 ${order.id} 的客人已入住吗？`,
        success: (res) => {
          if (res.confirm) {
            this.confirmCheckIn(order);
          }
        }
      });
    },
    
    // ========== 确认订单 ==========
    confirmOrder(order) {
      uni.showModal({
        title: '确认订单',
        content: `确认订单 ${order.id} 吗？确认后订单状态将变为"已确认"，并自动发送欢迎消息给客户。`,
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' });
            
            uni.request({
              url: `http://localhost:8080/api/hotel-orders/${order.id}/confirm`,
              method: 'POST',
              success: (res) => {
                uni.hideLoading();
                if (res.data && res.data.code === 200) {
                  uni.showToast({ title: '确认成功', icon: 'success' });
                  
                  // 确认订单成功后，发送确认消息给客户
                  this.sendConfirmedMessage(order);
                  
                  // 刷新订单列表
                  this.loadOrdersByMerchant();
                } else {
                  uni.showToast({ title: res.data.message || '确认失败', icon: 'none' });
                }
              },
              fail: (err) => {
                uni.hideLoading();
                console.error('确认订单失败', err);
                uni.showToast({ title: '网络错误', icon: 'none' });
              }
            });
          }
        }
      });
    },
    
    /**
     * 发送确认消息给客户
     */
    sendConfirmedMessage(order) {
      const hotelId = order.hotelId || order.id;
      const hotelName = order.name || order.hotelName || `酒店${hotelId}`;
      const merchantId = this.merchantId;
      const customerUsername = order.username;
      const orderId = order.id;
      
      if (!customerUsername) {
        console.error('缺少客户用户名，无法发送确认消息');
        return;
      }
      
      const confirmedMessage = `🎉 欢迎预订${hotelName}！\n\n📅 入住日期：${order.checkIn || '待确认'}\n📅 退房日期：${order.checkOut || '待确认'}\n💰 订单金额：¥${order.price || 0}\n\n如有任何问题，请随时联系我们，欢迎您的入住~`;
      
      console.log(`发送确认消息，订单ID: ${orderId}, 用户: ${customerUsername}`);
      
      // 1. 保存到本地存储
      this.saveConfirmedMessageToLocal(order, hotelId, hotelName, confirmedMessage);
      
      // 2. 发送到服务器
      uni.request({
        url: 'http://localhost:8080/api/messages/send',
        method: 'POST',
        data: {
          orderId: orderId,
          hotelId: hotelId,
          merchantId: merchantId.toString(),
          username: customerUsername,
          content: confirmedMessage,
          senderRole: 'merchant',
          isRead: 0
        },
        success: (res) => {
          console.log('确认消息发送成功', res.data);
        },
        fail: (err) => {
          console.error('确认消息发送失败：', err);
        }
      });
    },
    
    /**
     * 保存确认消息到本地存储
     */
    saveConfirmedMessageToLocal(order, hotelId, hotelName, message) {
      const userId = order.username;
      if (!userId) return;
      
      const key = `chat_${this.merchantId}_${userId}`;
      const saved = uni.getStorageSync(key) || {};
      
      const messages = saved.messages || [];
      const hasSameMessage = messages.some(msg => msg.content === message);
      if (!hasSameMessage) {
        messages.push({ role: 'merchant', content: message });
      }
      
      const data = {
        messages: messages,
        lastMessage: message,
        lastSender: 'merchant',
        lastTime: this.formatTime(new Date()),
        unreadCount: (saved.unreadCount || 0) + 1,
        name: userId,
        hotelName: hotelName
      };
      
      uni.setStorageSync(key, data);
      
      const merchantMessages = uni.getStorageSync('merchant_messages') || {};
      merchantMessages[userId] = {
        name: userId,
        hotelName: hotelName,
        messages: messages,
        lastMessage: message,
        lastTime: this.formatTime(new Date()),
        unreadCount: (saved.unreadCount || 0) + 1
      };
      uni.setStorageSync('merchant_messages', merchantMessages);
    },
    
    // 确认入住
    confirmCheckIn(order) {
      uni.showLoading({ title: '处理中...' });
      
      uni.request({
        url: `http://localhost:8080/api/hotel-orders/${order.id}/checkin`,
        method: 'POST',
        success: (res) => {
          uni.hideLoading();
          if (res.data && res.data.code === 200) {
            uni.showToast({ title: '确认入住成功', icon: 'success' });
            this.loadOrdersByMerchant();
          } else {
            uni.showToast({ title: res.data.message || '操作失败', icon: 'none' });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('确认入住失败', err);
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    },
    
    // 确认退房
    confirmCheckOut(order) {
      uni.showModal({
        title: '确认退房',
        content: `确认订单 ${order.id} 的客人已退房吗？确认后订单将标记为"已完成"。`,
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' });
            
            uni.request({
              url: `http://localhost:8080/api/hotel-orders/${order.id}/checkout`,
              method: 'POST',
              success: (res) => {
                uni.hideLoading();
                if (res.data && res.data.code === 200) {
                  uni.showToast({ title: '退房成功，订单已完成', icon: 'success' });
                  this.loadOrdersByMerchant();
                } else {
                  uni.showToast({ title: res.data.message || '操作失败', icon: 'none' });
                }
              },
              fail: (err) => {
                uni.hideLoading();
                console.error('确认退房失败', err);
                uni.showToast({ title: '网络错误', icon: 'none' });
              }
            });
          }
        }
      });
    },
    
    // 同意取消
    approveCancel(order) {
      uni.showModal({
        title: '同意取消',
        content: `确定同意订单 ${order.id} 的取消申请吗？取消后订单将失效。`,
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' });
            
            uni.request({
              url: `http://localhost:8080/api/hotel-orders/${order.id}/cancel-approve`,
              method: 'POST',
              success: (res) => {
                uni.hideLoading();
                if (res.data && res.data.code === 200) {
                  uni.showToast({ title: '已同意取消', icon: 'success' });
                  this.loadOrdersByMerchant();
                } else {
                  uni.showToast({ title: res.data.message || '操作失败', icon: 'none' });
                }
              },
              fail: (err) => {
                uni.hideLoading();
                console.error('同意取消失败', err);
                uni.showToast({ title: '网络错误', icon: 'none' });
              }
            });
          }
        }
      });
    },
    
    // 显示拒绝弹窗
    showRejectDialog(order) {
      this.rejectOrderId = order.id;
      this.rejectReason = '';
      this.showRejectDialogFlag = true;
    },
    
    // 关闭拒绝弹窗
    closeRejectDialog() {
      this.showRejectDialogFlag = false;
      this.rejectOrderId = null;
      this.rejectReason = '';
    },
    
    // 拒绝取消
    rejectCancel() {
      if (!this.rejectReason.trim()) {
        uni.showToast({ title: '请输入拒绝原因', icon: 'none' });
        return;
      }
      
      uni.showLoading({ title: '处理中...' });
      
      uni.request({
        url: `http://localhost:8080/api/hotel-orders/${this.rejectOrderId}/cancel-reject`,
        method: 'POST',
        data: {
          reason: this.rejectReason
        },
        success: (res) => {
          uni.hideLoading();
          if (res.data && res.data.code === 200) {
            uni.showToast({ title: '已拒绝取消', icon: 'success' });
            this.closeRejectDialog();
            this.loadOrdersByMerchant();
          } else {
            uni.showToast({ title: res.data.message || '操作失败', icon: 'none' });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('拒绝取消失败', err);
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    },
    
    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        '待支付': '待支付',
        '待确认': '待确认',
        '已支付': '已支付',
        '已确认': '已确认',
        '已入住': '已入住',
        '已完成': '已完成',
        '已取消': '已取消',
        '取消申请中': '申请取消中',
        '取消拒绝': '取消申请被拒绝'
      };
      return statusMap[status] || status;
    },
    
    // 获取状态样式
    getStatusClass(status) {
      const classMap = {
        '待支付': 'status-pending',
        '待确认': 'status-wait-confirm',
        '已支付': 'status-paid',
        '已确认': 'status-confirmed',
        '已入住': 'status-checkin',
        '已完成': 'status-completed',
        '已取消': 'status-cancelled',
        '取消申请中': 'status-cancelling',
        '取消拒绝': 'status-rejected'
      };
      return classMap[status] || 'status-default';
    },
    
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return '--';
      try {
        const date = new Date(dateStr);
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hour = String(date.getHours()).padStart(2, '0');
        const minute = String(date.getMinutes()).padStart(2, '0');
        return `${month}/${day} ${hour}:${minute}`;
      } catch (e) {
        return dateStr;
      }
    },
    
    // 格式化时间
    formatTime(date) {
      if (!date) return '刚刚';
      const d = new Date(date);
      const now = new Date();
      const diff = now - d;
      
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;
      
      return `${d.getMonth() + 1}月${d.getDate()}日`;
    }
  }
};
</script>

<style scoped>
.container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

/* 标签栏 */
.tabs {
  display: flex;
  flex-wrap: wrap;
  background-color: #fff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  padding: 10rpx 0;
}

.tab {
  flex: 1;
  min-width: 25%;
  text-align: center;
  padding: 20rpx 0;
  font-size: 26rpx;
  color: #666;
}

.tab.active {
  background-color: #1677ff;
  color: #fff;
  border-radius: 8rpx;
  margin: 0 5rpx;
}

/* 订单卡片 */
.order-card {
  background-color: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx;
  background-color: #fafafa;
  border-bottom: 1rpx solid #eee;
}

.order-id {
  font-size: 26rpx;
  color: #333;
  font-weight: bold;
}

.order-time {
  font-size: 24rpx;
  color: #999;
  margin-left: 20rpx;
}

.order-status {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.status-pending {
  background-color: #fff7e6;
  color: #fa8c16;
}

.status-wait-confirm {
  background-color: #e6f7ff;
  color: #1677ff;
}

.status-paid {
  background-color: #e6f7ff;
  color: #1677ff;
}

.status-confirmed {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-checkin {
  background-color: #e6fffb;
  color: #13c2c2;
}

.status-completed {
  background-color: #f0f0f0;
  color: #666;
}

.status-cancelled {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.status-cancelling {
  background-color: #fff7e6;
  color: #fa8c16;
}

.status-rejected {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.status-default {
  background-color: #f0f0f0;
  color: #666;
}

/* 订单内容 */
.order-content {
  padding: 25rpx;
}

.hotel-info {
  margin-bottom: 15rpx;
}

.hotel-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.room-type {
  font-size: 26rpx;
  color: #666;
}

.date-info {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 15rpx;
  display: flex;
  justify-content: space-between;
}

.customer-info {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 15rpx;
  display: flex;
  justify-content: space-between;
}

.price-info {
  font-size: 28rpx;
  font-weight: bold;
  color: #ff4d4f;
  margin-top: 10rpx;
}

/* 取消申请信息 */
.cancel-request-info {
  margin-top: 15rpx;
  padding: 15rpx;
  background-color: #fff7e6;
  border-radius: 8rpx;
}

.cancel-label {
  font-size: 26rpx;
  color: #fa8c16;
}

.cancel-reason {
  font-size: 26rpx;
  color: #666;
  margin-left: 10rpx;
}

.reject-info {
  margin-top: 15rpx;
  padding: 15rpx;
  background-color: #fff1f0;
  border-radius: 8rpx;
}

.reject-label {
  font-size: 26rpx;
  color: #ff4d4f;
}

.reject-reason {
  font-size: 26rpx;
  color: #666;
  margin-left: 10rpx;
}

/* 操作按钮 */
.order-actions {
  padding: 20rpx 25rpx;
  border-top: 1rpx solid #eee;
}

.waiting-tip {
  text-align: center;
  padding: 15rpx;
  background-color: #f0f0f0;
  border-radius: 8rpx;
  font-size: 26rpx;
  color: #999;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
}

.btn {
  flex: 1;
  height: 70rpx;
  line-height: 70rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: none;
}

.btn-confirm {
  background-color: #1677ff;
  color: #fff;
}

.btn-checkin {
  background-color: #52c41a;
  color: #fff;
}

.btn-checkout {
  background-color: #1677ff;
  color: #fff;
}

.btn-approve {
  background-color: #52c41a;
  color: #fff;
}

.btn-reject {
  background-color: #ff4d4f;
  color: #fff;
}

.cancelled-tip, .rejected-tip, .completed-tip {
  padding: 20rpx 25rpx;
  text-align: center;
  background-color: #f5f5f5;
  font-size: 26rpx;
  color: #999;
}

.completed-tip {
  color: #52c41a;
}

/* 空状态 */
.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 30rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 拒绝弹窗样式 */
.dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-container {
  width: 600rpx;
  background: #fff;
  border-radius: 30rpx;
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.dialog-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  font-size: 40rpx;
  color: #999;
  padding: 10rpx;
}

.dialog-content {
  padding: 30rpx;
}

.input-area {
  margin-bottom: 30rpx;
}

.input-label {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 15rpx;
}

.reason-input {
  width: 100%;
  min-height: 150rpx;
  border: 1rpx solid #ddd;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.btn-submit {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
}

/* 入住时间未到提示弹窗 */
.warning-dialog {
  width: 560rpx;
  background: #fff;
  border-radius: 30rpx;
  padding: 50rpx 40rpx 40rpx;
  text-align: center;
  box-sizing: border-box;
}

.warning-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.warning-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.warning-message {
  font-size: 28rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 40rpx;
}

.warning-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
}
</style>