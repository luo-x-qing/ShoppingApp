<template>
  <view class="container">
    <!-- 顶部切换标签 -->
    <view class="tabs">
      <view class="tab" :class="activeTab === 1 ? 'active' : ''" @click="switchTab(1)">
        <text class="tab-icon">🏨</text>
        <text>酒店订单</text>
      </view>
      <view class="tab" :class="activeTab === 2 ? 'active' : ''" @click="switchTab(2)">
        <text class="tab-icon">✈️</text>
        <text>机票订单</text>
      </view>
    </view>

    <!-- 酒店订单 - 状态筛选栏 -->
    <view class="status-filter" v-if="activeTab === 1 && orderList.length > 0">
      <scroll-view scroll-x class="filter-scroll">
        <view class="filter-items">
          <view 
            class="filter-item" 
            :class="{ active: hotelStatusFilter === 'all' }"
            @click="setHotelStatusFilter('all')"
          >
            全部
          </view>
          <view 
            class="filter-item" 
            :class="{ active: hotelStatusFilter === '待支付' }"
            @click="setHotelStatusFilter('待支付')"
          >
            待支付
          </view>
          <view 
            class="filter-item" 
            :class="{ active: hotelStatusFilter === '待确认' }"
            @click="setHotelStatusFilter('待确认')"
          >
            待确认
          </view>
          <view 
            class="filter-item" 
            :class="{ active: hotelStatusFilter === '已支付' }"
            @click="setHotelStatusFilter('已支付')"
          >
            已支付
          </view>
          <view 
            class="filter-item" 
            :class="{ active: hotelStatusFilter === '已确认' }"
            @click="setHotelStatusFilter('已确认')"
          >
            已确认
          </view>
          <view 
            class="filter-item" 
            :class="{ active: hotelStatusFilter === '已入住' }"
            @click="setHotelStatusFilter('已入住')"
          >
            已入住
          </view>
          <view 
            class="filter-item" 
            :class="{ active: hotelStatusFilter === '已完成' }"
            @click="setHotelStatusFilter('已完成')"
          >
            已完成
          </view>
          <view 
            class="filter-item" 
            :class="{ active: hotelStatusFilter === '已取消' }"
            @click="setHotelStatusFilter('已取消')"
          >
            已取消
          </view>
          <view 
            class="filter-item" 
            :class="{ active: hotelStatusFilter === '取消申请中' }"
            @click="setHotelStatusFilter('取消申请中')"
          >
            取消申请中
          </view>
          <view 
            class="filter-item" 
            :class="{ active: hotelStatusFilter === '取消拒绝' }"
            @click="setHotelStatusFilter('取消拒绝')"
          >
            取消拒绝
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-card" v-if="activeTab === 1 && filteredOrderList.length > 0">
      <view class="stat-item">
        <text class="stat-value">{{ filteredOrderList.length }}</text>
        <text class="stat-label">当前筛选</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ orderList.length }}</text>
        <text class="stat-label">总订单</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ orderList.filter(o => o.status === '已完成').length }}</text>
        <text class="stat-label">已完成</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ orderList.filter(o => o.status === '待支付').length }}</text>
        <text class="stat-label">待支付</text>
      </view>
    </view>

    <!-- 酒店订单区域 -->
    <view v-if="activeTab === 1">
      <view class="empty-box" v-if="filteredOrderList.length === 0">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无{{ hotelStatusFilter === 'all' ? '' : hotelStatusFilter }}订单</text>
        <text class="empty-hint">去首页预订心仪的酒店吧~</text>
      </view>
      
      <view class="order-card" v-for="item in filteredOrderList" :key="item.id">
        <!-- 订单头部 -->
        <view class="order-card-header">
          <view class="hotel-info">
            <text class="hotel-name">{{ item.name || item.hotelName }}</text>
            <text class="order-id">订单号：{{ item.id }}</text>
          </view>
          <view class="order-status" :class="getHotelStatusClass(item.status)">
            {{ getHotelStatusText(item.status) }}
          </view>
        </view>

        <!-- 订单内容 -->
        <view class="order-card-content">
          <view class="info-row">
            <text class="info-label">🏠 房型</text>
            <text class="info-value">{{ item.roomTypeName || '标准间' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">📅 入住</text>
            <text class="info-value">{{ item.checkIn }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">📅 退房</text>
            <text class="info-value">{{ item.checkOut }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">👤 联系人</text>
            <text class="info-value">{{ item.contactName || item.username }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">📞 电话</text>
            <text class="info-value">{{ item.contactPhone || '--' }}</text>
          </view>
          <view class="price-row">
            <text class="price-label">订单金额</text>
            <text class="price-value">¥{{ item.price }}</text>
          </view>
        </view>

        <!-- 取消申请信息 -->
        <view v-if="item.status === '取消申请中'" class="cancel-info">
          <text class="cancel-icon">⏳</text>
          <text>取消申请已提交，等待商家审核</text>
        </view>
        <view v-if="item.status === '取消拒绝'" class="reject-info">
          <text class="reject-icon">❌</text>
          <text>拒绝原因：{{ item.rejectReason || '商家拒绝了取消申请' }}</text>
        </view>

        <!-- 操作按钮 -->
        <view class="order-card-footer">
          <view class="action-buttons" v-if="item.status === '待支付'">
            <button class="btn-outline" @click="cancelHotelOrder(item)">取消订单</button>
            <button class="btn-primary" @click="showHotelPayDialog(item)">去支付</button>
          </view>
          
          <view class="action-buttons" v-else-if="canApplyCancel(item)">
            <button class="btn-warning" @click="showCancelRequestDialog(item)">申请取消</button>
          </view>
          
          <view class="waiting-tip" v-else-if="item.status === '取消申请中'">
            <text>⏳ 等待商家审核...</text>
          </view>
          
          <view class="action-buttons" v-else-if="item.status === '取消拒绝'">
            <button class="btn-info" @click="closeRejectTip(item)">我知道了</button>
          </view>
        </view>

        <!-- 评价区域 -->
        <view class="comment-section" v-if="!item.commented && item.status === '已完成'">
          <view class="rating-row">
            <text class="rating-label">评分：</text>
            <view class="stars">
              <text 
                v-for="n in 5" 
                :key="n"
                class="star"
                :class="{ active: n <= item.score }"
                @click="item.score = n"
              >★</text>
            </view>
          </view>
          <view class="comment-input-wrapper">
            <input class="comment-input" v-model="item.content" placeholder="写下你的评价..." />
            <button class="comment-submit" @click="submitComment(item)">提交</button>
          </view>
        </view>
        <view v-else-if="item.commented" class="commented-tip">
          <text>✅ 已评价</text>
        </view>
      </view>
    </view>

    <!-- 机票订单区域（保留原有功能） -->
    <view v-if="activeTab === 2">
      <!-- 机票订单状态筛选 -->
      <view class="status-filter" v-if="flightOrders.length > 0">
        <scroll-view scroll-x class="filter-scroll">
          <view class="filter-items">
            <view class="filter-item" :class="{ active: flightStatusFilter === 'all' }" @click="setFlightStatusFilter('all')">全部</view>
            <view class="filter-item" :class="{ active: flightStatusFilter === '待支付' }" @click="setFlightStatusFilter('待支付')">待支付</view>
            <view class="filter-item" :class="{ active: flightStatusFilter === '已支付' }" @click="setFlightStatusFilter('已支付')">已支付</view>
            <view class="filter-item" :class="{ active: flightStatusFilter === '已出票' }" @click="setFlightStatusFilter('已出票')">已出票</view>
            <view class="filter-item" :class="{ active: flightStatusFilter === '已完成' }" @click="setFlightStatusFilter('已完成')">已完成</view>
            <view class="filter-item" :class="{ active: flightStatusFilter === '已取消' }" @click="setFlightStatusFilter('已取消')">已取消</view>
          </view>
        </scroll-view>
      </view>

      <view class="empty-box" v-if="filteredFlightOrders.length === 0">
        <text class="empty-icon">✈️</text>
        <text class="empty-text">暂无{{ flightStatusFilter === 'all' ? '' : flightStatusFilter }}订单</text>
        <text class="empty-hint">去购票页面预订机票吧~</text>
      </view>
      
      <view class="order-card" v-for="(item, index) in filteredFlightOrders" :key="index">
        <view class="order-card-header">
          <view class="flight-info">
            <text class="flight-number">{{ item.flightNumber || '--' }}</text>
            <text class="airline">{{ item.airline || '航空公司' }}</text>
          </view>
          <view class="order-status" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </view>
        </view>

        <view class="order-card-content">
          <view class="route-row">
            <view class="city">
              <text class="city-name">{{ formatCity(item.departCity) }}</text>
              <text class="airport-name">{{ item.departAirport || '' }}</text>
            </view>
            <view class="route-line">
              <text class="arrow-icon">✈️</text>
              <text class="duration">{{ item.duration || '' }}</text>
            </view>
            <view class="city">
              <text class="city-name">{{ formatCity(item.arriveCity) }}</text>
              <text class="airport-name">{{ item.arriveAirport || '' }}</text>
            </view>
          </view>
          <view class="info-row">
            <text class="info-label">🕐 出发</text>
            <text class="info-value">{{ formatFullDateTime(item.departTime) }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">🕐 到达</text>
            <text class="info-value">{{ formatFullDateTime(item.arriveTime) }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">👤 乘客</text>
            <text class="info-value">{{ item.passengerName || '--' }}</text>
          </view>
          <view class="price-row">
            <text class="price-label">订单金额</text>
            <text class="price-value">¥{{ item.price }}</text>
          </view>
        </view>

        <view class="order-card-footer">
          <view class="action-buttons" v-if="item.status === '待支付'">
            <button class="btn-outline" @click="cancelFlightOrder(item.id)">取消订单</button>
            <button class="btn-primary" @click="showFlightPayDialog(item.id)">去支付</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 支付弹窗 -->
    <view class="modal-mask" v-if="showPayDialogFlag" @click="closePayDialog">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">支付验证</text>
          <text class="modal-close" @click="closePayDialog">×</text>
        </view>
        <view class="modal-body">
          <view class="pay-amount">
            <text class="amount-label">支付金额</text>
            <text class="amount-value">¥{{ payingAmount }}</text>
          </view>
          <view class="pay-input">
            <text class="input-label">支付密码</text>
            <input 
              class="password-input" 
              type="number" 
              maxlength="6" 
              v-model="payPassword"
              placeholder="请输入6位数字密码"
              @input="onPasswordInput"
            />
          </view>
        </view>
        <view class="modal-footer">
          <button class="cancel-btn" @click="closePayDialog">取消</button>
          <button class="confirm-btn" @click="confirmPay" :disabled="!isPasswordValid">确认支付</button>
        </view>
      </view>
    </view>
    
    <!-- 取消申请弹窗 -->
    <view class="modal-mask" v-if="showCancelRequestFlag" @click="closeCancelRequestDialog">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">申请取消订单</text>
          <text class="modal-close" @click="closeCancelRequestDialog">×</text>
        </view>
        <view class="modal-body">
          <textarea 
            class="reason-input" 
            v-model="cancelReason"
            placeholder="请输入取消原因（选填）"
            maxlength="200"
            auto-height
          />
        </view>
        <view class="modal-footer">
          <button class="cancel-btn" @click="closeCancelRequestDialog">取消</button>
          <button class="confirm-btn" @click="submitCancelRequest">提交申请</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      activeTab: 1,
      orderList: [],
      flightOrders: [],
      username: "",
      
      // 状态筛选
      hotelStatusFilter: 'all',
      flightStatusFilter: 'all',
      
      // 支付弹窗相关
      showPayDialogFlag: false,
      payingOrderId: null,
      payingAmount: 0,
      payPassword: '',
      payingType: '',
      
      // 取消申请弹窗相关
      showCancelRequestFlag: false,
      cancelOrderItem: null,
      cancelReason: ''
    };
  },
  
  computed: {
    isPasswordValid() {
      return this.payPassword && this.payPassword.length === 6 && /^\d{6}$/.test(this.payPassword);
    },
    
    // 筛选后的酒店订单
    filteredOrderList() {
      if (this.hotelStatusFilter === 'all') {
        return this.orderList;
      }
      return this.orderList.filter(item => item.status === this.hotelStatusFilter);
    },
    
    // 筛选后的机票订单
    filteredFlightOrders() {
      if (this.flightStatusFilter === 'all') {
        return this.flightOrders;
      }
      return this.flightOrders.filter(item => item.status === this.flightStatusFilter);
    }
  },

  onShow() {
    this.username = uni.getStorageSync("loginUsername") || "";
    const userInfo = uni.getStorageSync('userInfo');
    if (userInfo && userInfo.username) {
      this.username = userInfo.username;
    }
    console.log("当前登录用户：", this.username);
    this.loadOrderData();
  },

  methods: {
    // 设置酒店订单筛选
    setHotelStatusFilter(status) {
      this.hotelStatusFilter = status;
    },
    
    // 设置机票订单筛选
    setFlightStatusFilter(status) {
      this.flightStatusFilter = status;
    },
    
    // 判断是否可以申请取消
    canApplyCancel(item) {
      const cannotCancelStatuses = ['已入住', '已完成', '已取消', '取消申请中', '取消拒绝'];
      if (cannotCancelStatuses.includes(item.status)) {
        return false;
      }
      if (item.status === '待支付') {
        return false;
      }
      return true;
    },
    
    switchTab(type) {
      this.activeTab = type;
      // 切换时重置筛选
      if (type === 1) {
        this.hotelStatusFilter = 'all';
      } else {
        this.flightStatusFilter = 'all';
      }
      this.loadOrderData();
    },

    loadOrderData() {
      if (this.activeTab === 1) {
        this.getHotelOrders();
      } else {
        this.getFlightOrders();
      }
    },

    // ========== 酒店订单相关 ==========
    getHotelOrders() {
      uni.request({
        url: "http://localhost:8080/api/hotel-orders/user?username=" + this.username,
        method: "GET",
        success: (res) => {
          console.log("酒店订单返回数据：", res.data);
          
          let orders = [];
          if (res.data && res.data.code === 200) {
            orders = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            orders = res.data;
          } else {
            orders = [];
          }
          
          this.orderList = orders.map(item => {
            item.content = "";
            item.score = 5;
            item.commented = false;
            if (!item.status) item.status = '待支付';
            return item;
          });
          this.checkAllCommentedOrders();
        },
        fail: (err) => {
          console.error("获取酒店订单失败：", err);
          this.orderList = [];
        }
      });
    },
    
    getHotelStatusText(status) {
      const statusMap = {
        '待支付': '待支付',
        '待确认': '待确认',
        '已支付': '已支付',
        '已确认': '已确认',
        '已入住': '已入住',
        '已完成': '已完成',
        '已取消': '已取消',
        '取消申请中': '取消申请中',
        '取消拒绝': '取消申请被拒绝'
      };
      return statusMap[status] || status || '待支付';
    },
    
    getHotelStatusClass(status) {
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
    
    cancelHotelOrder(item) {
      uni.showModal({
        title: '提示',
        content: '确定要取消这个订单吗？取消后无法恢复。',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '取消中...' });
            
            uni.request({
              url: `http://localhost:8080/api/hotel-orders/${item.id}/cancel`,
              method: "POST",
              success: (res) => {
                uni.hideLoading();
                console.log("取消酒店订单结果：", res.data);
                
                if (res.data && res.data.code === 200) {
                  uni.showToast({ title: "订单已取消", icon: "success" });
                  this.getHotelOrders();
                } else {
                  uni.showToast({ title: res.data.message || "取消失败", icon: "none" });
                }
              },
              fail: (err) => {
                uni.hideLoading();
                console.error("取消订单失败：", err);
                uni.showToast({ title: "网络错误", icon: "none" });
              }
            });
          }
        }
      });
    },
    
    showHotelPayDialog(item) {
      if (item.status !== '待支付') {
        uni.showToast({ title: "订单状态不正确，无法支付", icon: "none" });
        return;
      }
      
      this.payingType = 'hotel';
      this.payingOrderId = item.id;
      this.payingAmount = item.price;
      this.payPassword = '';
      this.showPayDialogFlag = true;
    },
    
    showCancelRequestDialog(item) {
      this.cancelOrderItem = item;
      this.cancelReason = '';
      this.showCancelRequestFlag = true;
    },
    
    closeCancelRequestDialog() {
      this.showCancelRequestFlag = false;
      this.cancelOrderItem = null;
      this.cancelReason = '';
    },
    
    submitCancelRequest() {
      if (!this.cancelOrderItem) return;
      
      if (!this.username) {
        uni.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      
      uni.showLoading({ title: '提交中...' });
      
      uni.request({
        url: `http://localhost:8080/api/hotel-orders/${this.cancelOrderItem.id}/cancel-request`,
        method: "POST",
        header: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        data: {
          username: this.username,
          reason: this.cancelReason || '用户申请取消'
        },
        success: (res) => {
          uni.hideLoading();
          console.log("取消申请结果：", res.data);
          
          if (res.data && res.data.code === 200) {
            uni.showToast({ title: "取消申请已提交，等待商家审核", icon: "success" });
            this.closeCancelRequestDialog();
            this.getHotelOrders();
          } else {
            uni.showToast({ title: res.data.message || "提交失败", icon: "none" });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error("提交取消申请失败：", err);
          uni.showToast({ title: "网络错误，请稍后重试", icon: "none" });
        }
      });
    },
    
    closeRejectTip(item) {
      this.getHotelOrders();
    },

    checkAllCommentedOrders() {
      uni.request({
        url: "http://localhost:8080/api/hotel-comments/all",
        success: (res) => {
          console.log("评价数据：", res.data);
          
          let comments = [];
          if (res.data && res.data.code === 200) {
            comments = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            comments = res.data;
          } else {
            comments = [];
          }
          
          const commentedOrderIds = comments.map(c => c.orderId);
          this.orderList.forEach(item => {
            if (commentedOrderIds.includes(item.id)) {
              item.commented = true;
            }
          });
        },
        fail: (err) => {
          console.error("获取评价失败：", err);
        }
      });
    },

    submitComment(item) {
      if (!item.content) {
        uni.showToast({ title: "请输入评价内容", icon: "none" });
        return;
      }
      if (item.commented) {
        uni.showToast({ title: "已评价", icon: "none" });
        return;
      }
      
      if (!this.username) {
        uni.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      
      uni.showLoading({ title: '提交中...' });
      
      uni.request({
        url: "http://localhost:8080/api/hotel-comments",
        method: "POST",
        header: {
          'Content-Type': 'application/json'
        },
        data: {
          hotelId: item.hotelId,
          orderId: item.id,
          content: item.content,
          score: item.score,
          username: this.username
        },
        success: (res) => {
          uni.hideLoading();
          console.log("提交评价结果：", res.data);
          
          if (res.data && res.data.code === 200) {
            uni.showToast({ title: "评价成功", icon: "success" });
            item.commented = true;
          } else if (res.data === "已评价" || (res.data && res.data.code === 500 && res.data.message === "已评价")) {
            uni.showToast({ title: "该订单已评价", icon: "none" });
            item.commented = true;
          } else {
            uni.showToast({ title: res.data?.message || "评价失败", icon: "none" });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error("提交评价失败：", err);
          uni.showToast({ title: "评价失败，请重试", icon: "none" });
        }
      });
    },

    // ========== 机票订单相关 ==========
    getFlightOrders() {
      if (!this.username || this.username === '') {
        this.flightOrders = [];
        return;
      }
      
      uni.request({
        url: "http://localhost:8080/api/flight-orders?username=" + this.username,
        method: "GET",
        success: (res) => {
          console.log("后端返回的机票订单：", res.data);
          
          let orders = [];
          if (res.data && res.data.code === 200) {
            orders = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            orders = res.data;
          } else {
            orders = [];
          }
          
          this.flightOrders = orders;
        },
        fail: (err) => {
          console.error("获取机票订单失败：", err);
          this.flightOrders = [];
        }
      });
    },
    
    cancelFlightOrder(orderId) {
      uni.showModal({
        title: '提示',
        content: '确定要取消这个订单吗？',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '取消中...' });
            
            uni.request({
              url: `http://localhost:8080/api/flight-orders/${orderId}/cancel`,
              method: "PUT",
              success: (res) => {
                uni.hideLoading();
                
                if (res.data && res.data.code === 200) {
                  uni.showToast({ title: "订单已取消", icon: "success" });
                  this.getFlightOrders();
                } else {
                  uni.showToast({ title: res.data?.message || "取消失败", icon: "none" });
                }
              },
              fail: (err) => {
                uni.hideLoading();
                console.error("取消订单失败：", err);
                uni.showToast({ title: "网络错误", icon: "none" });
              }
            });
          }
        }
      });
    },
    
    showFlightPayDialog(orderId) {
      const order = this.flightOrders.find(item => item.id === orderId);
      if (order) {
        if (order.status !== '待支付') {
          uni.showToast({ title: "订单状态不正确，无法支付", icon: "none" });
          return;
        }
        
        this.payingType = 'flight';
        this.payingOrderId = orderId;
        this.payingAmount = order.price;
        this.payPassword = '';
        this.showPayDialogFlag = true;
      } else {
        uni.showToast({ title: "订单不存在", icon: "none" });
      }
    },
    
    closePayDialog() {
      this.showPayDialogFlag = false;
      this.payPassword = '';
      this.payingOrderId = null;
      this.payingType = '';
    },
    
    onPasswordInput(e) {
      let value = e.detail.value.replace(/\D/g, '');
      if (value.length > 6) {
        value = value.slice(0, 6);
      }
      this.payPassword = value;
    },
    
    confirmPay() {
      if (!this.isPasswordValid) {
        uni.showToast({ title: "请输入6位数字支付密码", icon: "none" });
        return;
      }
      
      uni.showLoading({ title: '支付中...' });
      
      let url = '';
      let method = '';
      if (this.payingType === 'hotel') {
        url = `http://localhost:8080/api/hotel-orders/${this.payingOrderId}/pay`;
        method = "POST";
      } else {
        url = `http://localhost:8080/api/flight-orders/${this.payingOrderId}/pay`;
        method = "PUT";
      }
      
      uni.request({
        url: url,
        method: method,
        success: (res) => {
          uni.hideLoading();
          
          if (res.data && res.data.code === 200) {
            uni.showToast({ title: "支付成功！", icon: "success" });
            this.closePayDialog();
            if (this.payingType === 'hotel') {
              this.getHotelOrders();
            } else {
              this.getFlightOrders();
            }
          } else {
            uni.showToast({ title: res.data?.message || "支付失败", icon: "none" });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error("支付请求失败:", err);
          
          if (err.statusCode === 404) {
            uni.showToast({ title: "支付接口不存在", icon: "none" });
          } else if (err.statusCode === 500) {
            uni.showToast({ title: "服务器错误", icon: "none" });
          } else {
            uni.showToast({ title: "网络错误，支付失败", icon: "none" });
          }
        }
      });
    },
    
    formatCity(city) {
      if (!city) return '--';
      return city.split('机场')[0] || city;
    },
    
    formatFullDateTime(dateTimeStr) {
      if (!dateTimeStr) return '--';
      try {
        const date = new Date(dateTimeStr);
        if (!isNaN(date.getTime())) {
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          const hour = String(date.getHours()).padStart(2, '0');
          const minute = String(date.getMinutes()).padStart(2, '0');
          return `${year}-${month}-${day} ${hour}:${minute}`;
        }
        return dateTimeStr;
      } catch (e) {
        return dateTimeStr;
      }
    },
    
    getStatusText(status) {
      const statusMap = {
        '待支付': '待支付',
        '已支付': '已支付',
        '已出票': '已出票',
        '已取消': '已取消',
        '已完成': '已完成'
      };
      return statusMap[status] || status || '待支付';
    },
    
    getStatusClass(status) {
      const classMap = {
        '待支付': 'status-pending',
        '已支付': 'status-paid',
        '已出票': 'status-confirmed',
        '已取消': 'status-cancelled',
        '已完成': 'status-completed'
      };
      return classMap[status] || 'status-default';
    }
  }
};
</script>

<style scoped>
page {
  background-color: #f5f7fa;
}

.container {
  padding: 24rpx;
  min-height: 100vh;
  box-sizing: border-box;
  padding-bottom: 40rpx;
}

/* 顶部切换标签 */
.tabs {
  display: flex;
  background: #fff;
  border-radius: 60rpx;
  margin-bottom: 24rpx;
  padding: 8rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #666;
  border-radius: 52rpx;
  transition: all 0.3s;
}

.tab-icon {
  font-size: 32rpx;
}

.tab.active {
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(22, 119, 255, 0.3);
}

/* 状态筛选栏 */
.status-filter {
  background: #fff;
  border-radius: 24rpx;
  padding: 16rpx 0;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.filter-scroll {
  white-space: nowrap;
}

.filter-items {
  display: inline-flex;
  padding: 0 20rpx;
  gap: 16rpx;
}

.filter-item {
  display: inline-block;
  padding: 12rpx 28rpx;
  font-size: 26rpx;
  color: #666;
  background-color: #f5f7fa;
  border-radius: 40rpx;
  transition: all 0.3s;
}

.filter-item.active {
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
  box-shadow: 0 2rpx 8rpx rgba(22, 119, 255, 0.3);
}

/* 统计卡片 */
.stats-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 44rpx;
  font-weight: bold;
  color: #1677ff;
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.stat-divider {
  width: 1rpx;
  height: 60rpx;
  background-color: #eee;
}

/* 订单卡片 */
.order-card {
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 28rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.hotel-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
}

.order-id {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

.flight-info {
  display: flex;
  flex-direction: column;
}

.flight-number {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.airline {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

.order-status {
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  font-size: 24rpx;
  font-weight: 500;
}

/* 订单内容 */
.order-card-content {
  padding: 20rpx 28rpx;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.info-label {
  width: 130rpx;
  font-size: 26rpx;
  color: #999;
}

.info-value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
}

/* 航线展示 */
.route-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
  padding: 16rpx 0;
}

.city {
  text-align: center;
}

.city-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
}

.airport-name {
  font-size: 22rpx;
  color: #999;
  margin-top: 6rpx;
  display: block;
}

.route-line {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 20rpx;
}

.arrow-icon {
  font-size: 28rpx;
  color: #1677ff;
}

.duration {
  font-size: 22rpx;
  color: #999;
  margin-top: 6rpx;
}

/* 价格行 */
.price-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx dashed #eee;
}

.price-label {
  font-size: 26rpx;
  color: #999;
}

.price-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff4d4f;
}

/* 取消/拒绝信息 */
.cancel-info, .reject-info {
  margin: 0 28rpx 16rpx;
  padding: 16rpx;
  background-color: #fff7e6;
  border-radius: 16rpx;
  font-size: 24rpx;
  color: #fa8c16;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.reject-info {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.cancel-icon, .reject-icon {
  font-size: 28rpx;
}

/* 操作按钮区域 */
.order-card-footer {
  padding: 16rpx 28rpx 28rpx;
  border-top: 1rpx solid #f0f0f0;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
}

.action-buttons button {
  flex: 1;
  height: 76rpx;
  line-height: 76rpx;
  border-radius: 44rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
  margin: 0;
}

.btn-primary {
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(22, 119, 255, 0.3);
}

.btn-outline {
  background: #fff;
  color: #ff4d4f;
  border: 1rpx solid #ff4d4f;
}

.btn-warning {
  background: #fff;
  color: #fa8c16;
  border: 1rpx solid #fa8c16;
}

.btn-info {
  background: #fff;
  color: #1677ff;
  border: 1rpx solid #1677ff;
}

.waiting-tip {
  text-align: center;
  padding: 20rpx;
  background-color: #f8f9fa;
  border-radius: 44rpx;
  font-size: 26rpx;
  color: #999;
}

/* 评价区域 */
.comment-section {
  margin-top: 16rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.rating-row {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.rating-label {
  font-size: 26rpx;
  color: #666;
  margin-right: 16rpx;
}

.stars {
  display: flex;
  gap: 8rpx;
}

.star {
  font-size: 36rpx;
  color: #e0e0e0;
}

.star.active {
  color: #ffbc36;
}

.comment-input-wrapper {
  display: flex;
  gap: 16rpx;
}

.comment-input {
  flex: 1;
  height: 72rpx;
  background-color: #f5f7fa;
  border-radius: 36rpx;
  padding: 0 24rpx;
  font-size: 26rpx;
}

.comment-submit {
  width: 120rpx;
  height: 72rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 36rpx;
  font-size: 26rpx;
  border: none;
}

.commented-tip {
  margin-top: 16rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
  text-align: center;
  font-size: 24rpx;
  color: #52c41a;
}

/* 状态颜色 */
.status-pending { background: #fff7e6; color: #fa8c16; }
.status-wait-confirm { background: #e6f7ff; color: #1677ff; }
.status-paid { background: #e6f7ff; color: #1677ff; }
.status-confirmed { background: #f6ffed; color: #52c41a; }
.status-checkin { background: #e6fffb; color: #13c2c2; }
.status-completed { background: #f0f0f0; color: #666; }
.status-cancelled { background: #fff1f0; color: #ff4d4f; }
.status-cancelling { background: #fff7e6; color: #fa8c16; }
.status-rejected { background: #fff1f0; color: #ff4d4f; }
.status-default { background: #f0f0f0; color: #666; }

/* 空状态 */
.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 40rpx;
  background: #fff;
  border-radius: 24rpx;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 24rpx;
  color: #ccc;
}

/* 弹窗样式 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-container {
  width: 85%;
  max-width: 600rpx;
  background-color: #fff;
  border-radius: 32rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 32rpx 20rpx;
  border-bottom: 1rpx solid #eee;
}

.modal-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.modal-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}

.modal-body {
  padding: 32rpx;
}

.modal-footer {
  display: flex;
  padding: 20rpx 32rpx 40rpx;
  gap: 20rpx;
}

.cancel-btn, .confirm-btn {
  flex: 1;
  border-radius: 50rpx;
  padding: 24rpx;
  font-size: 28rpx;
  border: none;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.confirm-btn {
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
}

.confirm-btn[disabled] {
  opacity: 0.6;
}

.pay-amount {
  text-align: center;
  margin-bottom: 40rpx;
}

.amount-label {
  font-size: 26rpx;
  color: #999;
  display: block;
  margin-bottom: 12rpx;
}

.amount-value {
  font-size: 56rpx;
  font-weight: bold;
  color: #ff4d4f;
}

.pay-input {
  margin-top: 20rpx;
}

.input-label {
  font-size: 26rpx;
  color: #666;
  display: block;
  margin-bottom: 16rpx;
}

.password-input {
  width: 100%;
  height: 88rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 16rpx;
  text-align: center;
  font-size: 40rpx;
  letter-spacing: 20rpx;
}

.reason-input {
  width: 100%;
  min-height: 160rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
</style>