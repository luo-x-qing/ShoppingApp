<template>
  <view class="container">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="title">💬 消息中心</text>
      <view class="header-actions">
        <text class="clear-btn" @click="clearAllUnread">一键已读</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view class="message-list" scroll-y="true">
      <!-- AI 智能助手 -->
      <view class="chat-item ai-chat" @click="goToAIChat">
        <view class="chat-avatar">
          <image class="avatar-img" src="/static/ai-avatar.png" mode="aspectFill"></image>
          <view class="online-dot"></view>
        </view>
        <view class="chat-content">
          <view class="chat-header">
            <text class="chat-name">AI 智能助手</text>
            <text class="chat-time">{{ aiLastMessageTime }}</text>
          </view>
          <view class="chat-preview">
            <text class="preview-text">{{ aiLastMessage }}</text>
          </view>
        </view>
        <view class="chat-badge" v-if="aiUnreadCount > 0">
          <text>{{ aiUnreadCount > 99 ? '99+' : aiUnreadCount }}</text>
        </view>
      </view>

      <!-- 置顶通知 -->
      <view class="section-divider">
        <text class="divider-text">置顶通知</text>
      </view>

      <view class="chat-item notice-chat" v-for="notice in topNotices" :key="notice.id" @click="viewNotice(notice)">
        <view class="chat-avatar notice-avatar">
          <text class="avatar-emoji">{{ notice.emoji }}</text>
        </view>
        <view class="chat-content">
          <view class="chat-header">
            <text class="chat-name">{{ notice.title }}</text>
            <text class="chat-time">{{ notice.time }}</text>
          </view>
          <view class="chat-preview">
            <text class="preview-text">{{ notice.preview }}</text>
          </view>
        </view>
        <view class="chat-badge" v-if="!notice.read">
          <text>新</text>
        </view>
      </view>

      <!-- 商家消息 -->
      <view class="section-divider">
        <text class="divider-text">商家消息</text>
      </view>

      <view class="chat-item merchant-chat" v-for="merchant in merchantMessages" :key="merchant.id" @click="goToMerchantChat(merchant)">
        <view class="chat-avatar">
          <image class="avatar-img" :src="merchant.avatar" mode="aspectFill"></image>
        </view>
        <view class="chat-content">
          <view class="chat-header">
            <text class="chat-name">{{ merchant.name }}</text>
            <text class="chat-time">{{ merchant.lastTime }}</text>
          </view>
          <view class="chat-preview">
            <text class="preview-text">{{ merchant.lastMessage }}</text>
          </view>
        </view>
        <view class="chat-badge" v-if="merchant.unreadCount > 0">
          <text>{{ merchant.unreadCount > 99 ? '99+' : merchant.unreadCount }}</text>
        </view>
      </view>

      <view v-if="merchantMessages.length === 0 && topNotices.length === 0" class="empty-state">
        <text class="empty-icon">💬</text>
        <text class="empty-text">暂无消息，去预订酒店或机票吧~</text>
      </view>
    </scroll-view>

    <!-- AI 聊天弹窗 -->
    <view class="ai-chat-modal" v-if="showAIChat" @click="closeAIChat">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">AI 智能助手</text>
          <text class="modal-close" @click="closeAIChat">✕</text>
        </view>
        <view class="coming-soon">
          <text class="coming-icon">🤖</text>
          <text class="coming-text">AI 智能助手功能即将上线</text>
          <text class="coming-tip">敬请期待...</text>
        </view>
      </view>
    </view>

    <!-- 商家聊天弹窗 -->
    <view class="merchant-chat-modal" v-if="showMerchantChat" @click="closeMerchantChat">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ currentMerchant.name }}</text>
          <text class="modal-close" @click="closeMerchantChat">✕</text>
        </view>
        
        <scroll-view class="chat-messages" scroll-y="true" scroll-into-view="msg-bottom2" :scroll-with-animation="true">
          <view class="message-list-container">
            <view class="message-item" v-for="(msg, idx) in currentMessages" :key="idx">
              <!-- 商家消息（左边） -->
              <view class="message-left" v-if="msg.role === 'merchant'">
                <view class="message-avatar">
                  <image class="small-avatar" :src="currentMerchant.avatar" mode="aspectFill"></image>
                </view>
                <view class="message-bubble merchant-bubble">
                  <text>{{ msg.content }}</text>
                </view>
              </view>
              
              <!-- 用户消息（右边） -->
              <view class="message-right" v-else>
                <view class="message-bubble user-bubble">
                  <text>{{ msg.content }}</text>
                </view>
                <view class="message-avatar">
                  <view class="user-avatar">👤</view>
                </view>
              </view>
            </view>
          </view>
          <view id="msg-bottom2"></view>
        </scroll-view>
        
        <!-- 快捷回复选项 -->
        <view class="quick-replies" v-if="quickReplies.length > 0">
          <scroll-view scroll-x="true" class="quick-scroll">
            <view class="quick-reply-item" v-for="(reply, idx) in quickReplies" :key="idx" @click="sendQuickReply(reply)">
              <text>{{ reply }}</text>
            </view>
          </scroll-view>
        </view>
        
        <view class="chat-input-area">
          <input 
            class="chat-input" 
            v-model="merchantInput" 
            placeholder="输入消息..." 
            confirm-type="send"
            @confirm="sendMerchantMessage"
          />
          <button class="send-btn" @click="sendMerchantMessage">发送</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      showAIChat: false,
      aiUnreadCount: 0,
      aiLastMessage: 'AI智能助手即将上线，敬请期待！',
      aiLastMessageTime: this.formatTime(new Date()),
      topNotices: [],
      merchantMessages: [],
      showMerchantChat: false,
      currentMerchant: null,
      currentMessages: [],
      merchantInput: '',
      quickReplies: ['好的，谢谢', '请问有优惠吗？', '可以改期吗？', '退订政策是什么？', '酒店地址在哪？', '有停车场吗？'],
      hotelOrders: [],
      flightOrders: []
    };
  },
  
  onLoad() {
    this.username = uni.getStorageSync("loginUsername") || "";
    console.log('=== onLoad === 用户名:', this.username);
    this.loadNotices();
    this.loadHotelOrders();
    this.loadFlightOrders();
  },
  
  onShow() {
    this.username = uni.getStorageSync("loginUsername") || "";
    console.log('=== onShow === 用户名:', this.username);
    this.loadNotices();
    this.loadHotelOrders();
    this.loadFlightOrders();
    this.countTotalUnread();
  },
  
  methods: {
    loadNotices() {
      const readNotices = uni.getStorageSync('read_notices') || {};
      this.topNotices = [
        {
          id: 1,
          title: '🏨 酒店预订成功',
          emoji: '🏨',
          preview: '您预订的酒店已确认，请按时入住！如有问题可联系商家。',
          time: this.formatTime(new Date()),
          read: readNotices[1] || false
        },
        {
          id: 2,
          title: '✈️ 机票订购成功',
          emoji: '✈️',
          preview: '您的机票已出票成功，请提前2小时到达机场！',
          time: this.formatTime(new Date()),
          read: readNotices[2] || false
        }
      ];
    },
    
    markNoticeRead(noticeId) {
      const readNotices = uni.getStorageSync('read_notices') || {};
      readNotices[noticeId] = true;
      uni.setStorageSync('read_notices', readNotices);
    },
    
    loadHotelOrders() {
      if (!this.username) {
        console.log('用户名不存在，无法加载订单');
        this.generateMerchantMessages();
        return;
      }
      
      console.log('开始加载酒店订单，用户名：', this.username);
      
      uni.request({
        url: "http://localhost:8080/api/hotel-orders/user?username=" + this.username,
        method: "GET",
        success: (res) => {
          console.log('酒店订单API返回数据：', res.data);
          
          let orders = [];
          if (res.data && res.data.code === 200) {
            orders = res.data.data || [];
            console.log('从data.data获取订单，数量：', orders.length);
          } else if (Array.isArray(res.data)) {
            orders = res.data;
            console.log('从数组获取订单，数量：', orders.length);
          } else {
            console.log('无法解析订单数据');
            orders = [];
          }
          
          console.log('订单详情：');
          orders.forEach((order, idx) => {
            console.log(`订单${idx+1}: ID=${order.id}, 状态="${order.status}", 酒店=${order.name || order.hotelName}, 客户=${order.username}`);
          });
          
          this.hotelOrders = orders;
          this.generateMerchantMessages();
        },
        fail: (err) => {
          console.error('获取酒店订单失败：', err);
          this.hotelOrders = [];
          this.generateMerchantMessages();
        }
      });
    },
    
    loadFlightOrders() {
      if (!this.username) return;
      
      uni.request({
        url: "http://localhost:8080/api/flight-orders?username=" + this.username,
        method: "GET",
        success: (res) => {
          let orders = [];
          if (res.data && res.data.code === 200) {
            orders = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            orders = res.data;
          }
          this.flightOrders = orders;
          this.generateMerchantMessages();
        },
        fail: () => {}
      });
    },
    
    generateMerchantMessages() {
      console.log('=== generateMerchantMessages 被调用 ===');
      console.log('hotelOrders数量：', this.hotelOrders.length);
      
      if (!this.hotelOrders || this.hotelOrders.length === 0) {
        console.log('没有订单数据，商家消息列表为空');
        this.merchantMessages = [];
        return;
      }
      
      const savedMessages = uni.getStorageSync('merchant_messages') || {};
      const merchants = {};
      
      this.hotelOrders.forEach(order => {
        console.log(`处理订单: ID=${order.id}, 状态="${order.status}"`);
        
        // 只过滤掉待支付和已取消的订单
        if (order.status === '待支付' || order.status === '已取消') {
          console.log(`  -> 跳过订单 ${order.id}，状态为 ${order.status}`);
          return;
        }
        
        console.log(`  -> 为订单 ${order.id} 生成商家会话`);
        
        const hotelId = order.hotelId || order.id;
        const hotelName = order.name || order.hotelName || `酒店${hotelId}`;
        console.log(`  -> 酒店名称: ${hotelName}, 客户用户名: ${order.username}`);
        
        if (!merchants[hotelId]) {
          merchants[hotelId] = {
            id: hotelId,
            userId: order.username,
            merchantId: order.merchantId || hotelId.toString(),
            hotelId: hotelId,
            orderId: order.id,
            name: hotelName,
            avatar: '/static/hotel-avatar.png',
            lastMessage: `欢迎预订${hotelName}！入住日期：${order.checkIn || '待确认'}`,
            lastTime: this.formatTime(new Date(order.createTime || Date.now())),
            unreadCount: 1,
            messages: [
              { 
                role: 'merchant', 
                content: `🎉 欢迎预订${hotelName}！\n\n📅 入住日期：${order.checkIn || '待确认'}\n📅 退房日期：${order.checkOut || '待确认'}\n💰 订单金额：¥${order.price || 0}\n\n如有任何问题，请随时联系我们！祝您入住愉快！` 
              }
            ]
          };
          console.log(`  -> 成功添加商家: ${hotelName}`);
        }
      });
      
      this.merchantMessages = Object.values(merchants);
      console.log('最终生成的商家会话数量：', this.merchantMessages.length);
      
      if (this.merchantMessages.length > 0) {
        console.log('商家列表：', this.merchantMessages.map(m => m.name));
      }
    },
    
    saveMerchantMessages(merchantId, messages, unreadCount, lastMessage, lastTime) {
      const savedMessages = uni.getStorageSync('merchant_messages') || {};
      const merchant = this.merchantMessages.find(m => m.id === merchantId);
      savedMessages[merchantId] = { 
        id: merchantId,
        messages, 
        unreadCount, 
        lastMessage, 
        lastTime,
        name: merchant?.name || '',
        avatar: merchant?.avatar || '/static/hotel-avatar.png'
      };
      uni.setStorageSync('merchant_messages', savedMessages);
    },
    
    countTotalUnread() {
      let totalUnread = this.aiUnreadCount;
      this.topNotices.forEach(notice => { if (!notice.read) totalUnread++; });
      this.merchantMessages.forEach(merchant => { totalUnread += merchant.unreadCount || 0; });
      
      if (totalUnread > 0) {
        uni.setTabBarBadge({ index: 2, text: totalUnread > 99 ? '99+' : totalUnread.toString() });
      } else {
        uni.removeTabBarBadge({ index: 2 });
      }
    },
    
    clearAllUnread() {
      this.aiUnreadCount = 0;
      this.topNotices.forEach(notice => {
        notice.read = true;
        this.markNoticeRead(notice.id);
      });
      this.merchantMessages.forEach(merchant => {
        merchant.unreadCount = 0;
        this.saveMerchantMessages(merchant.id, merchant.messages, 0, merchant.lastMessage, merchant.lastTime);
      });
      this.countTotalUnread();
      uni.showToast({ title: '已全部标为已读', icon: 'success' });
    },
    
    viewNotice(notice) {
      notice.read = true;
      this.markNoticeRead(notice.id);
      this.countTotalUnread();
      uni.showModal({ title: notice.title, content: notice.preview, showCancel: false, confirmText: '知道了' });
    },
    
    goToAIChat() {
      this.aiUnreadCount = 0;
      this.countTotalUnread();
      this.showAIChat = true;
    },
    
    closeAIChat() { this.showAIChat = false; },
    
    // ========== 修改后的 goToMerchantChat - 从服务器加载历史消息 ==========
    goToMerchantChat(merchant) {
      this.currentMerchant = merchant;
      
      // 先从服务器加载最新聊天记录
      this.loadChatHistoryFromServer(merchant, () => {
        this.currentMessages = [...this.currentMerchant.messages];
        this.currentMerchant.unreadCount = 0;
        
        // 标记为已读
        uni.request({
          url: `http://localhost:8080/api/messages/read?role=user&identifier=${this.username}&otherParty=${merchant.merchantId || merchant.id}`,
          method: 'POST'
        });
        
        this.saveMerchantMessages(merchant.id, this.currentMessages, 0, merchant.lastMessage, merchant.lastTime);
        const index = this.merchantMessages.findIndex(m => m.id === merchant.id);
        if (index !== -1) this.merchantMessages[index].unreadCount = 0;
        this.countTotalUnread();
        this.showMerchantChat = true;
        
        setTimeout(() => {
          uni.createSelectorQuery().select('#msg-bottom2').boundingClientRect().exec();
        }, 300);
      });
    },
    
    // ========== 新增：从服务器加载聊天记录 ==========
    loadChatHistoryFromServer(merchant, callback) {
      uni.request({
        url: `http://localhost:8080/api/messages/chat?username=${this.username}&merchantId=${merchant.merchantId || merchant.id}&hotelId=${merchant.hotelId || ''}`,
        method: 'GET',
        success: (res) => {
          if (res.data && res.data.code === 200) {
            const messages = res.data.data || [];
            if (messages.length > 0) {
              merchant.messages = messages.map(msg => ({
                role: msg.senderRole,
                content: msg.content
              }));
              merchant.lastMessage = messages[messages.length - 1]?.content || merchant.lastMessage;
              merchant.lastTime = this.formatTime(messages[messages.length - 1]?.createTime);
            }
          }
          if (callback) callback();
        },
        fail: () => {
          if (callback) callback();
        }
      });
    },
    
    closeMerchantChat() {
      this.showMerchantChat = false;
      this.currentMerchant = null;
      this.currentMessages = [];
      this.merchantInput = '';
    },
    
    sendQuickReply(reply) {
      this.merchantInput = reply;
      this.sendMerchantMessage();
    },
    
    sendMerchantMessage() {
      if (!this.merchantInput.trim()) return;
      
      const customerUsername = this.currentMerchant?.userId;
      
      console.log('发送消息 - 订单中的客户用户名:', customerUsername);
      console.log('当前登录用户名:', this.username);
      
      if (!customerUsername) {
        uni.showToast({ title: '无法获取客户信息', icon: 'none' });
        return;
      }
      
      const userMsg = this.merchantInput.trim();
      const merchantId = this.currentMerchant.merchantId || this.currentMerchant.id;
      const hotelId = this.currentMerchant.hotelId;
      const orderId = this.currentMerchant.orderId;
      const merchantName = this.currentMerchant?.name || '商家';
      
      // 添加到本地显示
      this.currentMessages.push({ role: 'user', content: userMsg });
      this.merchantInput = '';
      
      setTimeout(() => {
        uni.createSelectorQuery().select('#msg-bottom2').boundingClientRect().exec();
      }, 100);
      
      // 更新本地存储
      if (this.currentMerchant) {
        this.currentMerchant.lastMessage = userMsg;
        this.currentMerchant.lastTime = this.formatTime(new Date());
        this.currentMerchant.messages = this.currentMessages;
        this.saveMerchantMessages(this.currentMerchant.id, this.currentMessages, 0, userMsg, this.formatTime(new Date()));
      }
      
      // 发送消息到服务器
      uni.request({
        url: 'http://localhost:8080/api/messages/send',
        method: 'POST',
        data: {
          orderId: orderId,
          hotelId: hotelId,
          merchantId: merchantId.toString(),
          username: customerUsername,
          content: userMsg,
          senderRole: 'user',
          isRead: 0
        },
        success: (res) => {
          console.log('消息发送成功', res.data);
        },
        fail: (err) => {
          console.error('消息发送失败', err);
          uni.showToast({ title: '发送失败', icon: 'none' });
        }
      });
      
      // 自动回复
      setTimeout(() => {
        let autoReply = '';
        const lowerMsg = userMsg.toLowerCase();
        
        if (lowerMsg.includes('优惠') || lowerMsg.includes('折扣')) {
          autoReply = `感谢您对${merchantName}的关注！我们目前有暑期特惠活动，部分房型低至8折。`;
        } else if (lowerMsg.includes('改期') || lowerMsg.includes('修改')) {
          autoReply = `${merchantName}支持改期，请至少提前3天联系我们。请问您想改到哪一天？`;
        } else if (lowerMsg.includes('取消') || lowerMsg.includes('退订')) {
          autoReply = `${merchantName}的取消政策：入住前3天免费取消，入住前1-2天收取50%房费。`;
        } else if (lowerMsg.includes('停车')) {
          autoReply = `${merchantName}提供免费停车场，入住期间可免费停车。`;
        } else if (lowerMsg.includes('早餐')) {
          autoReply = `${merchantName}的早餐时间为7:00-10:00，位于酒店2楼餐厅。`;
        } else if (lowerMsg.includes('地址')) {
          autoReply = `${merchantName}的地址请在订单详情中查看，您也可以打开地图导航。`;
        } else if (lowerMsg.includes('谢谢') || lowerMsg.includes('感谢')) {
          autoReply = `不客气！${merchantName}祝您旅途愉快，有任何问题随时联系我们！😊`;
        } else if (lowerMsg.includes('你好') || lowerMsg.includes('您好')) {
          autoReply = `您好！${merchantName}很高兴为您服务！请问有什么可以帮您的？`;
        } else {
          autoReply = `感谢您的留言！${merchantName}会尽快处理您的问题。祝您生活愉快！`;
        }
        
        this.currentMessages.push({ role: 'merchant', content: autoReply });
        
        setTimeout(() => {
          uni.createSelectorQuery().select('#msg-bottom2').boundingClientRect().exec();
        }, 100);
        
        if (this.currentMerchant) {
          this.currentMerchant.lastMessage = autoReply;
          this.currentMerchant.lastTime = this.formatTime(new Date());
          this.currentMerchant.messages = this.currentMessages;
          this.saveMerchantMessages(this.currentMerchant.id, this.currentMessages, 0, autoReply, this.formatTime(new Date()));
        }
        
        uni.request({
          url: 'http://localhost:8080/api/messages/send',
          method: 'POST',
          data: {
            orderId: orderId,
            hotelId: hotelId,
            merchantId: merchantId.toString(),
            username: customerUsername,
            content: autoReply,
            senderRole: 'merchant',
            isRead: 0
          }
        });
      }, 800);
    },
    
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
page { background-color: #f5f5f5; }
.container { width: 100%; min-height: 100vh; display: flex; flex-direction: column; background-color: #f5f5f5; }

/* 头部 */
.header {
  background: linear-gradient(135deg, #1677ff, #0050b3);
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header .title { color: #fff; font-size: 36rpx; font-weight: bold; }
.clear-btn { color: rgba(255,255,255,0.8); font-size: 26rpx; padding: 8rpx 20rpx; background: rgba(255,255,255,0.2); border-radius: 30rpx; }

/* 消息列表 */
.message-list { flex: 1; padding: 0; }
.chat-item { display: flex; align-items: center; padding: 28rpx 30rpx; background-color: #fff; border-bottom: 1rpx solid #f0f0f0; }
.chat-item:active { background-color: #f9f9f9; }
.ai-chat { background: linear-gradient(135deg, #f0f7ff, #fff); }
.notice-chat { background-color: #fffef5; }

.chat-avatar { width: 100rpx; height: 100rpx; border-radius: 50%; margin-right: 24rpx; position: relative; flex-shrink: 0; }
.avatar-img { width: 100%; height: 100%; border-radius: 50%; background-color: #e8f4ff; }
.notice-avatar { background: linear-gradient(135deg, #ff9a3c, #ff6b6b); display: flex; align-items: center; justify-content: center; }
.avatar-emoji { font-size: 48rpx; }
.online-dot { position: absolute; bottom: 0; right: 4rpx; width: 20rpx; height: 20rpx; background-color: #52c41a; border-radius: 50%; border: 2rpx solid #fff; }

.chat-content { flex: 1; min-width: 0; }
.chat-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 12rpx; }
.chat-name { font-size: 32rpx; font-weight: 600; color: #333; }
.chat-time { font-size: 22rpx; color: #999; }
.chat-preview { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.preview-text { font-size: 26rpx; color: #999; }

.chat-badge { min-width: 36rpx; height: 36rpx; background-color: #ff4d4f; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; padding: 0 8rpx; margin-left: 16rpx; }
.chat-badge text { color: #fff; font-size: 20rpx; font-weight: bold; }

.section-divider { padding: 20rpx 30rpx; background-color: #f5f5f5; }
.divider-text { font-size: 24rpx; color: #999; }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 120rpx 0; }
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #999; }

/* AI 弹窗 */
.ai-chat-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; align-items: center; justify-content: center; }
.ai-chat-modal .modal-container { width: 90%; height: 60vh; background: #fff; border-radius: 30rpx; display: flex; flex-direction: column; overflow: hidden; }
.ai-chat-modal .modal-header { display: flex; justify-content: space-between; align-items: center; padding: 30rpx; background: linear-gradient(135deg, #1677ff, #0050b3); }
.ai-chat-modal .modal-title { font-size: 34rpx; font-weight: bold; color: #fff; }
.ai-chat-modal .modal-close { font-size: 40rpx; color: #fff; padding: 10rpx; }
.coming-soon { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 20rpx; }
.coming-icon { font-size: 80rpx; }
.coming-text { font-size: 32rpx; color: #333; font-weight: bold; }
.coming-tip { font-size: 26rpx; color: #999; }

/* 商家聊天弹窗 */
.merchant-chat-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; align-items: center; justify-content: center; }
.merchant-chat-modal .modal-container { width: 90%; height: 80vh; background: #fff; border-radius: 30rpx; display: flex; flex-direction: column; overflow: hidden; }
.merchant-chat-modal .modal-header { display: flex; justify-content: space-between; align-items: center; padding: 30rpx; background: linear-gradient(135deg, #1677ff, #0050b3); }
.merchant-chat-modal .modal-title { font-size: 34rpx; font-weight: bold; color: #fff; }
.merchant-chat-modal .modal-close { font-size: 40rpx; color: #fff; padding: 10rpx; }

/* 聊天消息区域 */
.chat-messages { 
  flex: 1; 
  padding: 30rpx 20rpx 30rpx 30rpx;
  background: #f5f5f5; 
  overflow-y: auto; 
}
.message-list-container { display: flex; flex-direction: column; }

.message-item { margin-bottom: 30rpx; width: 100%; }

/* 商家消息 - 左边 */
.message-left { display: flex; justify-content: flex-start; align-items: flex-start; }
.message-left .message-avatar { margin-right: 16rpx; }
.message-left .message-bubble { margin-left: 0; }

/* 用户消息 - 右边 */
.message-right { 
  display: flex; 
  justify-content: flex-end; 
  align-items: flex-start; 
   transform: translateX(-40rpx);
}
.message-right .message-bubble { 
  margin-right: 8rpx;
}
.message-right .message-avatar { 
  margin-left: 0px; 
}

.message-avatar { width: 70rpx; height: 70rpx; flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
.small-avatar { width: 60rpx; height: 60rpx; border-radius: 50%; background-color: #e8f4ff; }
.user-avatar { width: 60rpx; height: 60rpx; background: linear-gradient(135deg, #1677ff, #0050b3); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 32rpx; color: #fff; }

.message-bubble { max-width: 60%; padding: 20rpx 28rpx; border-radius: 30rpx; font-size: 28rpx; line-height: 1.5; word-wrap: break-word; white-space: pre-wrap; }

/* 用户消息气泡（蓝色）- 右边 */
.user-bubble { background: linear-gradient(135deg, #1677ff, #0050b3); color: #fff; border-radius: 30rpx 30rpx 8rpx 30rpx; }

/* 商家消息气泡（白色）- 左边 */
.merchant-bubble { background: #fff; color: #333; border-radius: 30rpx 30rpx 30rpx 8rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05); }

/* 快捷回复 */
.quick-replies { padding: 15rpx 20rpx; background: #fff; border-top: 1rpx solid #eee; border-bottom: 1rpx solid #eee; }
.quick-scroll { white-space: nowrap; display: flex; }
.quick-reply-item { display: inline-block; padding: 12rpx 24rpx; background: #f5f5f5; border-radius: 40rpx; font-size: 24rpx; color: #1677ff; border: 1rpx solid #e0e0e0; margin-right: 20rpx; }
.quick-reply-item:active { background: #e8f4ff; }

/* 底部输入区域 */
.chat-input-area { display: flex; padding: 20rpx 30rpx; padding-bottom: calc(20rpx + constant(safe-area-inset-bottom)); padding-bottom: calc(20rpx + env(safe-area-inset-bottom)); border-top: 1rpx solid #eee; background: #fff; align-items: center; gap: 16rpx; }
.chat-input { flex: 1; height: 70rpx; background: #f5f5f5; border-radius: 35rpx; padding: 0 25rpx; font-size: 28rpx; }
.send-btn { width: 100rpx; height: 60rpx; background: linear-gradient(135deg, #1677ff, #0050b3); color: #fff; border-radius: 30rpx; font-size: 26rpx; display: flex; align-items: center; justify-content: center; border: none; line-height: 1; padding: 0; }
.send-btn::after { border: none; }
</style>