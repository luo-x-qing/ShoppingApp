<template>
  <view class="container">
    <!-- 顶部统计 -->
    <view class="stats-bar">
      <view class="stat-item">
        <text class="stat-number">{{ totalUnread }}</text>
        <text class="stat-label">未读消息</text>
      </view>
      <view class="stat-item">
        <text class="stat-number">{{ conversations.length }}</text>
        <text class="stat-label">对话数量</text>
      </view>
      <view class="stat-item">
        <text class="clear-all" @click="clearAllUnread">一键已读</text>
      </view>
    </view>

    <!-- 会话列表 -->
    <scroll-view class="conversation-list" scroll-y="true">
      <view 
        class="conversation-item-wrapper"
        v-for="conv in conversations" 
        :key="conv.userId"
      >
        <!-- 左滑删除按钮 -->
        <view class="delete-btn" @click="deleteConversation(conv)">
          <text>删除</text>
        </view>
        
        <!-- 会话内容 -->
        <view 
          class="conversation-item"
          :class="{ 'slide-left': conv.showDelete }"
          @touchstart="handleTouchStart($event, conv)"
          @touchmove="handleTouchMove($event, conv)"
          @touchend="handleTouchEnd($event, conv)"
          @click="openChat(conv)"
        >
          <view class="conv-avatar">
            <view class="avatar-placeholder">{{ conv.userName.charAt(0) }}</view>
            <view class="unread-dot" v-if="conv.unreadCount > 0"></view>
          </view>
          <view class="conv-content">
            <view class="conv-header">
              <text class="conv-name">{{ conv.userName }}</text>
              <text class="conv-time">{{ conv.lastTime }}</text>
            </view>
            <view class="conv-hotel">
              <text class="hotel-name">🏨 {{ conv.hotelName }}</text>
            </view>
            <view class="conv-preview">
              <text class="preview-text" v-if="conv.lastSender === 'user'">用户：{{ conv.lastMessage }}</text>
              <text class="preview-text merchant-preview" v-else>商家：{{ conv.lastMessage }}</text>
            </view>
          </view>
          <view class="conv-badge" v-if="conv.unreadCount > 0">
            <text>{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</text>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="conversations.length === 0" class="empty-state">
        <text class="empty-icon">💬</text>
        <text class="empty-text">暂无客户消息</text>
        <text class="empty-tip">当有客户预订酒店后会自动显示在这里</text>
      </view>
    </scroll-view>

    <!-- 聊天弹窗 -->
    <view class="chat-modal" v-if="showChat" @click="closeChat">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <view class="header-info">
            <text class="modal-title">{{ currentUser.userName }}</text>
            <text class="modal-hotel">🏨 {{ currentUser.hotelName }}</text>
          </view>
          <text class="modal-close" @click="closeChat">✕</text>
        </view>
        
        <scroll-view class="chat-messages" scroll-y="true" scroll-into-view="msg-bottom" :scroll-with-animation="true">
          <view class="message-item" v-for="(msg, idx) in currentMessages" :key="idx">
            <!-- 用户消息（左边） -->
            <view class="message-left" v-if="msg.role === 'user'">
              <view class="message-avatar">
                <view class="user-avatar">{{ currentUser.userName?.charAt(0) || '用' }}</view>
              </view>
              <view class="message-bubble user-bubble">
                <text>{{ msg.content }}</text>
              </view>
            </view>
            
            <!-- 商家消息（右边） -->
            <view class="message-right" v-else>
              <view class="message-bubble merchant-bubble">
                <text>{{ msg.content }}</text>
              </view>
              <view class="message-avatar">
                <view class="merchant-avatar">商</view>
              </view>
            </view>
          </view>
          <view id="msg-bottom"></view>
        </scroll-view>
        
        <!-- 快捷回复 -->
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
            v-model="inputMessage" 
            placeholder="输入消息..." 
            confirm-type="send"
            @confirm="sendMessage"
          />
          <button class="send-btn" @click="sendMessage">发送</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      merchantId: null,
      merchantName: '',
      conversations: [],
      totalUnread: 0,
      
      // 触摸滑动相关
      startX: 0,
      startY: 0,
      currentConv: null,
      
      // 聊天相关
      showChat: false,
      currentUser: null,
      currentMessages: [],
      inputMessage: '',
      
      // 快捷回复
      quickReplies: [
        '好的，收到！', 
        '请问有什么可以帮您？', 
        '感谢您的预订！',
        '稍后为您处理',
        '请提供订单号',
        '已为您安排'
      ]
    };
  },
  
  onLoad() {
    this.loadMerchantInfo();
  },
  
  onShow() {
    this.loadMerchantInfo();
    this.loadConversations();
  },
  
  methods: {
    // 加载商家信息
    loadMerchantInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo');
        if (userInfo && userInfo.id) {
          this.merchantId = userInfo.id;
          this.merchantName = userInfo.shopName || userInfo.name || '商家';
          console.log('商家ID:', this.merchantId);
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
    
    // 加载会话列表
    loadConversations() {
      if (!this.merchantId) return;
      
      console.log('开始加载商家会话列表, 商家ID:', this.merchantId);
      this.loadConversationsFromServer();
    },
    
    // 从服务器加载会话列表
    loadConversationsFromServer() {
      uni.request({
        url: `http://localhost:8080/api/messages/merchant/conversations?merchantId=${this.merchantId}`,
        method: 'GET',
        success: (res) => {
          console.log('会话列表查询结果:', res.data);
          if (res.data && res.data.code === 200) {
            const conversations = res.data.data || [];
            console.log('从服务器获取到会话数量:', conversations.length);
            
            if (conversations.length > 0) {
              this.conversations = conversations.map(conv => {
                const rawMessage = conv.lastMessage || '暂无消息';
                const displayMessage = rawMessage.replace(/\n/g, ' ').replace(/\s+/g, ' ');
                
                const key = `chat_${this.merchantId}_${conv.userId}`;
                const saved = uni.getStorageSync(key);
                
                return {
                  userId: conv.userId,
                  userName: conv.userName || conv.userId,
                  hotelName: conv.hotelName || '酒店',
                  hotelId: conv.hotelId,
                  orderId: conv.orderId,
                  merchantId: this.merchantId,
                  lastMessage: displayMessage,
                  lastSender: conv.lastSender || 'merchant',
                  lastTime: this.formatTime(conv.lastTime),
                  unreadCount: conv.unreadCount || 0,
                  showDelete: false,
                  messages: saved ? saved.messages : []
                };
              });
              
              this.calcTotalUnread();
            } else {
              this.loadOrdersByMerchantDirect();
            }
          } else {
            this.loadOrdersByMerchantDirect();
          }
        },
        fail: (err) => {
          console.error('获取会话列表失败:', err);
          this.loadOrdersByMerchantDirect();
        }
      });
    },
    
    // 直接根据商家ID查询订单
    loadOrdersByMerchantDirect() {
      uni.request({
        url: `http://localhost:8080/api/hotel-orders/merchant/orders?merchantId=${this.merchantId}`,
        method: 'GET',
        success: (res) => {
          console.log('订单查询结果:', res.data);
          if (res.data && res.data.code === 200) {
            const orders = res.data.data || [];
            console.log('直接获取到订单数量:', orders.length);
            this.generateConversationsFromOrders(orders);
          } else {
            console.log('未获取到订单，尝试从本地存储加载');
            this.loadFromLocalStorage();
          }
        },
        fail: (err) => {
          console.error('获取订单失败:', err);
          this.loadFromLocalStorage();
        }
      });
    },
    
    // 根据订单生成会话列表
    generateConversationsFromOrders(orders) {
      console.log('=== 开始生成会话 ===');
      console.log('传入订单数量:', orders.length);
      
      const conversationsMap = {};
      
      orders.forEach(order => {
        const userId = order.username || `order_${order.id}`;
        const hotelName = order.name || order.hotelName || `酒店${order.hotelId}`;
        const userName = order.contactName || userId;
        
        if (!conversationsMap[userId]) {
          const key = `chat_${this.merchantId}_${userId}`;
          const saved = uni.getStorageSync(key);
          
          const rawMessage = saved ? saved.lastMessage : `欢迎预订${hotelName}！`;
          const displayMessage = rawMessage ? rawMessage.replace(/\n/g, ' ').replace(/\s+/g, ' ') : '';
          
          conversationsMap[userId] = {
            userId: userId,
            userName: userName,
            hotelName: hotelName,
            hotelId: order.hotelId,
            orderId: order.id,
            merchantId: this.merchantId,
            lastMessage: displayMessage,
            lastSender: saved ? saved.lastSender : 'merchant',
            lastTime: saved ? saved.lastTime : this.formatTime(order.createTime),
            unreadCount: 0,
            showDelete: false,
            messages: saved ? saved.messages : [
              { 
                role: 'merchant', 
                content: `🎉 欢迎预订${hotelName}！\n\n📅 入住日期：${order.checkIn || '待确认'}\n📅 退房日期：${order.checkOut || '待确认'}\n💰 订单金额：¥${order.price || 0}\n\n如有任何问题，请随时联系我们！祝您入住愉快！` 
              }
            ]
          };
        }
      });
      
      this.conversations = Object.values(conversationsMap);
      console.log('最终生成的会话数量:', this.conversations.length);
      
      this.fetchUnreadCountsFromServer();
    },
    
    // 从服务器获取未读数
    fetchUnreadCountsFromServer() {
      this.conversations.forEach(conv => {
        uni.request({
          url: `http://localhost:8080/api/messages/unread?role=merchant&identifier=${this.merchantId}&otherParty=${conv.userId}`,
          method: 'GET',
          success: (res) => {
            if (res.data && res.data.code === 200) {
              const serverUnread = res.data.data?.unreadCount || 0;
              conv.unreadCount = serverUnread;
              
              const key = `chat_${this.merchantId}_${conv.userId}`;
              const saved = uni.getStorageSync(key);
              if (saved) {
                saved.unreadCount = serverUnread;
                uni.setStorageSync(key, saved);
              }
              
              this.calcTotalUnread();
            }
          },
          fail: (err) => {
            console.error('获取未读数失败', err);
          }
        });
      });
    },
    
    // 从本地存储加载会话
    loadFromLocalStorage() {
      const savedMessages = uni.getStorageSync('merchant_messages') || {};
      const conversations = [];
      
      Object.keys(savedMessages).forEach(key => {
        const data = savedMessages[key];
        if (data && data.name) {
          const rawMessage = data.lastMessage || '您好';
          const displayMessage = rawMessage.replace(/\n/g, ' ').replace(/\s+/g, ' ');
          
          conversations.push({
            userId: key,
            userName: data.name,
            hotelName: data.hotelName || '酒店',
            lastMessage: displayMessage,
            lastTime: data.lastTime || this.formatTime(new Date()),
            unreadCount: data.unreadCount || 0,
            orderId: data.orderId,
            hotelId: data.hotelId,
            merchantId: this.merchantId,
            showDelete: false,
            messages: data.messages || []
          });
        }
      });
      
      this.conversations = conversations;
      console.log('从本地存储加载的会话数量:', this.conversations.length);
      this.calcTotalUnread();
    },
    
    // 计算总未读数
    calcTotalUnread() {
      this.totalUnread = this.conversations.reduce((sum, conv) => sum + (conv.unreadCount || 0), 0);
    },
    
    // 标记消息已读
    markMessagesAsRead(username) {
      if (!username) {
        console.error('用户名不能为空');
        return;
      }
      uni.request({
        url: `http://localhost:8080/api/messages/read?role=merchant&identifier=${this.merchantId}&otherParty=${username}`,
        method: 'POST',
        success: (res) => {
          console.log('标记已读成功', res.data);
        },
        fail: (err) => {
          console.error('标记已读失败', err);
        }
      });
    },
    
    // ========== 左滑删除功能 ==========
    handleTouchStart(e, conv) {
      this.startX = e.touches[0].clientX;
      this.startY = e.touches[0].clientY;
      this.currentConv = conv;
    },
    
    handleTouchMove(e, conv) {
      if (!this.currentConv) return;
      
      const moveX = e.touches[0].clientX;
      const moveY = e.touches[0].clientY;
      const deltaX = moveX - this.startX;
      const deltaY = moveY - this.startY;
      
      if (Math.abs(deltaX) > Math.abs(deltaY) && deltaX < -30) {
        this.conversations.forEach(item => {
          if (item.userId !== conv.userId && item.showDelete) {
            item.showDelete = false;
          }
        });
        conv.showDelete = true;
      } else if (Math.abs(deltaX) > Math.abs(deltaY) && deltaX > 30) {
        conv.showDelete = false;
      }
    },
    
    handleTouchEnd(e, conv) {
      this.currentConv = null;
    },
    
    // 删除会话
    deleteConversation(conv) {
      uni.showModal({
        title: '提示',
        content: `确定要删除与 ${conv.userName} 的聊天记录吗？`,
        success: (res) => {
          if (res.confirm) {
            const index = this.conversations.findIndex(c => c.userId === conv.userId);
            if (index !== -1) {
              this.conversations.splice(index, 1);
            }
            
            const key = `chat_${this.merchantId}_${conv.userId}`;
            uni.removeStorageSync(key);
            
            const savedMessages = uni.getStorageSync('merchant_messages') || {};
            delete savedMessages[conv.userId];
            uni.setStorageSync('merchant_messages', savedMessages);
            
            this.calcTotalUnread();
            
            uni.showToast({ title: '删除成功', icon: 'success' });
          } else {
            conv.showDelete = false;
          }
        }
      });
    },
    
    // 一键已读
    clearAllUnread() {
      this.conversations.forEach(conv => {
        conv.unreadCount = 0;
        const key = `chat_${this.merchantId}_${conv.userId}`;
        const saved = uni.getStorageSync(key);
        if (saved) {
          saved.unreadCount = 0;
          uni.setStorageSync(key, saved);
        }
      });
      this.calcTotalUnread();
      
      uni.request({
        url: `http://localhost:8080/api/messages/merchant/read-all?merchantId=${this.merchantId}`,
        method: 'POST',
        success: (res) => {
          console.log('全部标记已读成功', res.data);
          uni.showToast({ title: '已全部标为已读', icon: 'success' });
        },
        fail: (err) => {
          console.error('标记全部已读失败', err);
          uni.showToast({ title: '操作失败', icon: 'none' });
        }
      });
    },
    
    // 打开聊天
    openChat(conv) {
      conv.showDelete = false;
      
      this.currentUser = conv;
      
      conv.unreadCount = 0;
      this.calcTotalUnread();
      
      const key = `chat_${this.merchantId}_${conv.userId}`;
      const saved = uni.getStorageSync(key);
      if (saved) {
        saved.unreadCount = 0;
        uni.setStorageSync(key, saved);
      }
      
      this.markMessagesAsRead(conv.userId);
      
      this.loadChatHistory();
      this.showChat = true;
    },
    
    // 加载聊天记录
    loadChatHistory() {
      if (!this.currentUser) return;
      
      uni.request({
        url: `http://localhost:8080/api/messages/chat?username=${this.currentUser.userId}&merchantId=${this.merchantId}`,
        method: 'GET',
        success: (res) => {
          if (res.data && res.data.code === 200) {
            const messages = res.data.data || [];
            if (messages.length > 0) {
              this.currentMessages = messages.map(msg => ({
                role: msg.senderRole,
                content: msg.content
              }));
              this.saveMessagesToLocal();
              this.scrollToBottom();
              
              this.markMessagesAsRead(this.currentUser.userId);
              return;
            }
          }
          this.loadLocalMessages();
        },
        fail: () => {
          this.loadLocalMessages();
        }
      });
    },
    
    // 从本地加载消息
    loadLocalMessages() {
      const key = `chat_${this.merchantId}_${this.currentUser.userId}`;
      const saved = uni.getStorageSync(key);
      
      if (saved && saved.messages) {
        this.currentMessages = saved.messages;
      } else {
        this.currentMessages = this.currentUser.messages || [
          { role: 'merchant', content: `您好！欢迎入住${this.currentUser.hotelName}！请问有什么可以帮您？` }
        ];
        this.saveMessagesToLocal();
      }
      
      this.scrollToBottom();
    },
    
    // 保存消息到本地
    saveMessagesToLocal() {
      const key = `chat_${this.merchantId}_${this.currentUser.userId}`;
      const lastMsg = this.currentMessages[this.currentMessages.length - 1];
      
      const rawLastMessage = lastMsg?.content || '';
      const displayLastMessage = rawLastMessage ? rawLastMessage.replace(/\n/g, ' ').replace(/\s+/g, ' ') : '';
      
      uni.setStorageSync(key, {
        messages: this.currentMessages,
        lastMessage: displayLastMessage,
        lastSender: lastMsg?.role,
        lastTime: this.formatTime(new Date()),
        unreadCount: 0
      });
      
      const index = this.conversations.findIndex(c => c.userId === this.currentUser.userId);
      if (index !== -1) {
        this.conversations[index].lastMessage = displayLastMessage;
        this.conversations[index].lastSender = lastMsg?.role;
        this.conversations[index].lastTime = this.formatTime(new Date());
        this.conversations[index].unreadCount = 0;
      }
    },
    
    // 滚动到底部
    scrollToBottom() {
      this.$nextTick(() => {
        setTimeout(() => {
          uni.createSelectorQuery().select('#msg-bottom').boundingClientRect().exec();
        }, 100);
      });
    },
    
    // 发送快捷回复
    sendQuickReply(reply) {
      this.inputMessage = reply;
      this.sendMessage();
    },
    
    // 发送消息
    sendMessage() {
      if (!this.inputMessage.trim()) return;
      
      const msgContent = this.inputMessage.trim();
      
      this.currentMessages.push({ role: 'merchant', content: msgContent });
      this.inputMessage = '';
      
      this.scrollToBottom();
      
      this.saveMessagesToLocal();
      
      if (this.currentUser) {
        const displayMsg = msgContent.replace(/\n/g, ' ').replace(/\s+/g, ' ');
        this.currentUser.lastMessage = displayMsg;
        this.currentUser.lastSender = 'merchant';
        this.currentUser.lastTime = this.formatTime(new Date());
        this.currentUser.unreadCount = 0;
        
        const key = `chat_${this.merchantId}_${this.currentUser.userId}`;
        const saved = uni.getStorageSync(key);
        if (saved) {
          saved.unreadCount = 0;
          saved.lastMessage = displayMsg;
          saved.lastSender = 'merchant';
          saved.lastTime = this.formatTime(new Date());
          uni.setStorageSync(key, saved);
        }
      }
      
      uni.request({
        url: 'http://localhost:8080/api/messages/send',
        method: 'POST',
        data: {
          merchantId: this.merchantId.toString(),
          username: this.currentUser.userId,
          orderId: this.currentUser.orderId,
          hotelId: this.currentUser.hotelId,
          content: msgContent,
          senderRole: 'merchant',
          isRead: 0
        },
        success: (res) => {
          console.log('消息发送成功', res.data);
        },
        fail: (err) => {
          console.error('消息发送失败', err);
        }
      });
    },
    
    // 关闭聊天
    closeChat() {
      this.showChat = false;
      this.currentUser = null;
      this.currentMessages = [];
      this.inputMessage = '';
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

.stats-bar {
  display: flex;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-number {
  font-size: 40rpx;
  font-weight: bold;
  color: #1677ff;
  display: block;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.clear-all {
  font-size: 26rpx;
  color: #1677ff;
  padding: 10rpx 20rpx;
  background: #e8f4ff;
  border-radius: 30rpx;
  display: inline-block;
}

/* 会话列表 - 支持左滑删除 */
.conversation-list {
  height: calc(100vh - 180rpx);
}

.conversation-item-wrapper {
  position: relative;
  overflow: hidden;
  margin-bottom: 0;
}

.delete-btn {
  position: absolute;
  right: 0;
  top: 0;
  width: 150rpx;
  height: 100%;
  background-color: #ff4d4f;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28rpx;
  z-index: 1;
  border-radius: 0;
}

.conversation-item {
  position: relative;
  display: flex;
  align-items: center;
  padding: 28rpx 30rpx;
  background-color: #fff;
  border-bottom: 1rpx solid #f0f0f0;
  transition: transform 0.3s ease;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

.conversation-item.slide-left {
  transform: translateX(-150rpx);
}

.conversation-item:active {
  background-color: #f9f9f9;
}

.conv-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  position: relative;
  flex-shrink: 0;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #1677ff, #0050b3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
}

.unread-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 20rpx;
  height: 20rpx;
  background-color: #52c41a;
  border-radius: 50%;
  border: 2rpx solid #fff;
}

.conv-content {
  flex: 1;
  min-width: 0;
}

.conv-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 8rpx;
}

.conv-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.conv-time {
  font-size: 22rpx;
  color: #999;
}

.conv-hotel {
  margin-bottom: 8rpx;
}

.hotel-name {
  font-size: 24rpx;
  color: #1677ff;
}

.conv-preview {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-text {
  font-size: 26rpx;
  color: #999;
}

.preview-text.merchant-preview {
  color: #52c41a;
}

.conv-badge {
  min-width: 36rpx;
  height: 36rpx;
  background-color: #ff4d4f;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
  margin-left: 16rpx;
}

.conv-badge text {
  color: #fff;
  font-size: 20rpx;
  font-weight: bold;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 20rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.empty-tip {
  font-size: 24rpx;
  color: #ccc;
  margin-top: 10rpx;
}

/* 聊天弹窗 */
.chat-modal {
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

.modal-container {
  width: 90%;
  height: 80vh;
  background: #fff;
  border-radius: 30rpx;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background: linear-gradient(135deg, #1677ff, #0050b3);
}

.header-info {
  display: flex;
  flex-direction: column;
}

.modal-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #fff;
}

.modal-hotel {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4rpx;
}

.modal-close {
  font-size: 40rpx;
  color: #fff;
  padding: 10rpx;
}

.chat-messages {
  flex: 1;
  padding: 30rpx;
  background: #f5f5f5;
  overflow-y: auto;
}

.message-item {
  margin-bottom: 30rpx;
  width: 100%;
}

.message-left {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
}

.message-left .message-bubble {
  margin-left: 16rpx;
}

.message-right {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  transform: translateX(-40rpx);
}

.message-right .message-bubble {
  margin-right: 16rpx;
}

.message-avatar {
  width: 70rpx;
  height: 70rpx;
  flex-shrink: 0;
}

.merchant-avatar {
  width: 60rpx;
  height: 60rpx;
  background: linear-gradient(135deg, #52c41a, #389e0d);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: bold;
  color: #fff;
}

.user-avatar {
  width: 60rpx;
  height: 60rpx;
  background: linear-gradient(135deg, #1677ff, #0050b3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: bold;
  color: #fff;
}

.message-bubble {
  max-width: 60%;
  padding: 20rpx 28rpx;
  border-radius: 30rpx;
  font-size: 28rpx;
  line-height: 1.5;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.user-bubble {
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
  border-radius: 30rpx 30rpx 30rpx 8rpx;
}

.merchant-bubble {
  background: #fff;
  color: #333;
  border-radius: 30rpx 30rpx 8rpx 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.quick-replies {
  padding: 15rpx 20rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
  border-bottom: 1rpx solid #eee;
}

.quick-scroll {
  white-space: nowrap;
  display: flex;
}

.quick-reply-item {
  display: inline-block;
  padding: 12rpx 24rpx;
  background: #f5f5f5;
  border-radius: 40rpx;
  font-size: 24rpx;
  color: #1677ff;
  border: 1rpx solid #e0e0e0;
  margin-right: 20rpx;
}

.quick-reply-item:active {
  background: #e8f4ff;
}

.chat-input-area {
  display: flex;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #eee;
  background: #fff;
  align-items: center;
  gap: 16rpx;
}

.chat-input {
  flex: 1;
  height: 70rpx;
  background: #f5f5f5;
  border-radius: 35rpx;
  padding: 0 25rpx;
  font-size: 28rpx;
}

.send-btn {
  width: 100rpx;
  height: 60rpx;
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
  border-radius: 30rpx;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  line-height: 1;
  padding: 0;
}

.send-btn::after {
  border: none;
}
</style>
