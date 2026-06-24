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

      <!-- 服务通知（分类显示：酒店 / 机票） -->
      <view class="section-divider">
        <text class="divider-text">服务通知</text>
      </view>

      <!-- 酒店预订成功分类 -->
      <view class="chat-item notice-chat" @click="viewCategoryNotices('hotel')">
        <view class="chat-avatar notice-avatar hotel-avatar">
          <text class="avatar-emoji">🏨</text>
        </view>
        <view class="chat-content">
          <view class="chat-header">
            <text class="chat-name">酒店预订成功</text>
            <text class="chat-time">{{ hotelLastTime }}</text>
          </view>
          <view class="chat-preview">
            <text class="preview-text">{{ hotelLastMessage }}</text>
          </view>
        </view>
        <view class="chat-badge" v-if="hotelUnreadCount > 0">
          <text>{{ hotelUnreadCount > 99 ? '99+' : hotelUnreadCount }}</text>
        </view>
      </view>

      <!-- 机票订购成功分类 -->
      <view class="chat-item notice-chat" @click="viewCategoryNotices('flight')">
        <view class="chat-avatar notice-avatar flight-avatar">
          <text class="avatar-emoji">✈️</text>
        </view>
        <view class="chat-content">
          <view class="chat-header">
            <text class="chat-name">机票订购成功</text>
            <text class="chat-time">{{ flightLastTime }}</text>
          </view>
          <view class="chat-preview">
            <text class="preview-text">{{ flightLastMessage }}</text>
          </view>
        </view>
        <view class="chat-badge" v-if="flightUnreadCount > 0">
          <text>{{ flightUnreadCount > 99 ? '99+' : flightUnreadCount }}</text>
        </view>
      </view>

      <view v-if="hotelOrdersList.length === 0 && flightOrdersList.length === 0" class="empty-notice">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无服务通知</text>
      </view>

      <!-- 商家消息 -->
      <view class="section-divider" v-if="merchantMessages.length > 0">
        <text class="divider-text">商家消息</text>
      </view>

      <view class="chat-item merchant-chat" v-for="merchant in merchantMessages" :key="merchant.id" @click="goToMerchantChat(merchant)">
        <view class="chat-avatar">
          <view v-if="merchant.avatarType === 'text'" class="avatar-text" :style="{backgroundColor: getAvatarColor(merchant.name)}">
            <text class="avatar-text-char">{{ merchant.avatar }}</text>
          </view>
          <image v-else class="avatar-img" :src="merchant.avatar" mode="aspectFill"></image>
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

      <view v-if="merchantMessages.length === 0 && hotelOrdersList.length === 0 && flightOrdersList.length === 0" class="empty-state">
        <text class="empty-icon">💬</text>
        <text class="empty-text">暂无消息，去预订酒店或机票吧~</text>
      </view>
    </scroll-view>

    <!-- 分类通知详情弹窗 -->
    <view class="category-modal" v-if="showCategoryModal" @click="closeCategoryModal">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ categoryTitle }}</text>
          <text class="modal-close" @click="closeCategoryModal">✕</text>
        </view>

        <scroll-view class="notice-list" scroll-y="true">
          <view v-if="currentCategoryNotices.length === 0" class="empty-notice-detail">
            <text class="empty-icon">📭</text>
            <text class="empty-text">暂无通知</text>
          </view>

          <view
            class="notice-item"
            v-for="notice in currentCategoryNotices"
            :key="notice.id"
            @click="viewOrderDetail(notice)"
          >
            <view class="notice-header">
              <text class="notice-title">{{ notice.title }}</text>
              <text class="notice-time">{{ notice.time }}</text>
            </view>
            <view class="notice-content">
              <text class="notice-preview">{{ notice.preview }}</text>
            </view>
            <view class="notice-badge" v-if="!notice.read">
              <text>未读</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 商家聊天弹窗 -->
    <view class="merchant-chat-modal" v-if="showMerchantChat" @click="closeMerchantChat">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <view class="modal-header-left">
            <view v-if="currentMerchant.avatarType === 'text'" class="modal-avatar-text" :style="{backgroundColor: getAvatarColor(currentMerchant.name)}">
              <text>{{ currentMerchant.avatar }}</text>
            </view>
            <image v-else class="modal-avatar-img" :src="currentMerchant.avatar" mode="aspectFill"></image>
            <text class="modal-title">{{ currentMerchant.name }}</text>
          </view>
          <text class="modal-close" @click="closeMerchantChat">✕</text>
        </view>

        <scroll-view class="chat-messages" scroll-y="true" scroll-into-view="msg-bottom2" :scroll-with-animation="true">
          <view class="message-list-container">
            <view class="message-item" v-for="(msg, idx) in currentMessages" :key="idx">
              <!-- 商家消息（左边） -->
              <view class="message-left" v-if="msg.role === 'merchant'">
                <view class="message-avatar">
                  <view v-if="currentMerchant.avatarType === 'text'" class="small-avatar-text" :style="{backgroundColor: getAvatarColor(currentMerchant.name)}">
                    <text>{{ currentMerchant.avatar }}</text>
                  </view>
                  <image v-else class="small-avatar" :src="currentMerchant.avatar" mode="aspectFill"></image>
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

      // AI
      aiUnreadCount: 0,
      aiLastMessage: '我是AI旅游规划助手，点击开始规划你的行程 ✈️',
      aiLastMessageTime: this.formatTime(new Date()),

      // 分类通知数据
      hotelOrdersList: [],
      flightOrdersList: [],
      hotelUnreadCount: 0,
      flightUnreadCount: 0,
      hotelLastMessage: '',
      hotelLastTime: '',
      flightLastMessage: '',
      flightLastTime: '',

      // 分类弹窗
      showCategoryModal: false,
      categoryTitle: '',
      currentCategoryNotices: [],
      currentCategoryType: '',

      // 商家消息
      merchantMessages: [],
      showMerchantChat: false,
      currentMerchant: null,
      currentMessages: [],
      merchantInput: '',
      quickReplies: ['好的，谢谢', '请问有优惠吗？', '可以改期吗？', '退订政策是什么？', '酒店地址在哪？', '有停车场吗？'],
      hotelOrders: [],
      flightOrders: [],

      // 轮询定时器
      unreadTimer: null,
      isLoading: false
    };
  },

  onLoad() {
    this.username = uni.getStorageSync("loginUsername") || "";
    this.loadOrderNotices();
    this.loadHotelOrders();
    this.loadFlightOrders();
  },

  onShow() {
    this.username = uni.getStorageSync("loginUsername") || "";
    this.loadOrderNotices();
    this.loadHotelOrders();
    this.loadFlightOrders();
    this.countTotalUnread();
    this.startUnreadPolling();
  },

  onHide() {
    this.stopUnreadPolling();
  },

  onUnload() {
    this.stopUnreadPolling();
  },

  methods: {
    getAvatarColor(name) {
      const colors = [
        '#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1',
        '#13c2c2', '#eb2f96', '#fa8c16', '#a0d911', '#2f54eb'
      ];
      let hash = 0;
      for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash);
      }
      const index = Math.abs(hash) % colors.length;
      return colors[index];
    },

    refreshUnreadCount() {
      if (!this.username) return;
      this.loadOrderNotices(() => {
        this.refreshMerchantUnreadCount();
      });
    },

    refreshMerchantUnreadCount() {
      if (!this.username || this.merchantMessages.length === 0) {
        this.countTotalUnread();
        return;
      }
      const merchantIds = this.merchantMessages.map(m => m.merchantId || m.id);
      let completed = 0;
      merchantIds.forEach((merchantId, index) => {
        uni.request({
          url: `http://localhost:8080/api/messages/unread?role=user&identifier=${this.username}&otherParty=${merchantId}`,
          method: 'GET',
          success: (res) => {
            if (res.data && res.data.code === 200) {
              const count = res.data.data?.unreadCount || 0;
              this.merchantMessages[index].unreadCount = count;
              const savedMessages = uni.getStorageSync('merchant_messages') || {};
              const id = this.merchantMessages[index].id;
              if (savedMessages[id]) {
                savedMessages[id].unreadCount = count;
                uni.setStorageSync('merchant_messages', savedMessages);
              }
            }
            completed++;
            if (completed === merchantIds.length) {
              this.countTotalUnread();
            }
          },
          fail: () => {
            completed++;
            if (completed === merchantIds.length) {
              this.countTotalUnread();
            }
          }
        });
      });
    },

    // ========== 加载订单通知（分类处理） ==========
    loadOrderNotices(callback) {
      if (!this.username) {
        this.hotelOrdersList = [];
        this.flightOrdersList = [];
        if (callback) callback();
        return;
      }

      uni.request({
        url: `http://localhost:8080/api/notices/user/${this.username}`,
        method: "GET",
        success: (res) => {
          let notices = [];
          if (res.data && res.data.code === 200) {
            notices = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            notices = res.data;
          }

          this.hotelOrdersList = [];
          this.flightOrdersList = [];

          notices.forEach(notice => {
            if (notice.type === 'HOTEL_ORDER' ||
                (notice.title && notice.title.includes('酒店')) ||
                (notice.content && notice.content.includes('酒店预订'))) {
              this.hotelOrdersList.push({
                id: notice.id,
                type: 'hotel',
                orderId: notice.orderId,
                title: notice.title || '酒店预订成功',
                preview: notice.content,
                time: this.formatTime(notice.createTime),
                read: notice.isRead || false,
                rawContent: notice.content,
                createTime: notice.createTime
              });
            }
            else if (notice.type === 'FLIGHT_ORDER' ||
                     (notice.title && notice.title.includes('机票')) ||
                     (notice.content && notice.content.includes('机票订购'))) {
              this.flightOrdersList.push({
                id: notice.id,
                type: 'flight',
                orderId: notice.orderId,
                title: notice.title || '机票订购成功',
                preview: notice.content,
                time: this.formatTime(notice.createTime),
                read: notice.isRead || false,
                rawContent: notice.content,
                createTime: notice.createTime
              });
            }
          });

          this.hotelOrdersList.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
          this.flightOrdersList.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));

          this.hotelUnreadCount = this.hotelOrdersList.filter(n => !n.read).length;
          this.flightUnreadCount = this.flightOrdersList.filter(n => !n.read).length;

          if (this.hotelOrdersList.length > 0) {
            const lastHotel = this.hotelOrdersList[0];
            this.hotelLastMessage = lastHotel.preview.substring(0, 30) + (lastHotel.preview.length > 30 ? '...' : '');
            this.hotelLastTime = lastHotel.time;
          } else {
            this.hotelLastMessage = '暂无酒店订单通知';
            this.hotelLastTime = '';
          }

          if (this.flightOrdersList.length > 0) {
            const lastFlight = this.flightOrdersList[0];
            this.flightLastMessage = lastFlight.preview.substring(0, 30) + (lastFlight.preview.length > 30 ? '...' : '');
            this.flightLastTime = lastFlight.time;
          } else {
            this.flightLastMessage = '暂无机票订单通知';
            this.flightLastTime = '';
          }

          if (callback) callback();
        },
        fail: (err) => {
          this.hotelOrdersList = [];
          this.flightOrdersList = [];
          if (callback) callback();
        }
      });
    },

    viewCategoryNotices(type) {
      if (type === 'hotel') {
        this.categoryTitle = '酒店预订成功';
        this.currentCategoryNotices = [...this.hotelOrdersList];
        this.currentCategoryType = 'hotel';
      } else {
        this.categoryTitle = '机票订购成功';
        this.currentCategoryNotices = [...this.flightOrdersList];
        this.currentCategoryType = 'flight';
      }

      this.showCategoryModal = true;

      const unreadNotices = this.currentCategoryNotices.filter(n => !n.read);
      unreadNotices.forEach(notice => {
        notice.read = true;
        this.markNoticeRead(notice.id);
      });

      if (type === 'hotel') {
        this.hotelUnreadCount = 0;
      } else {
        this.flightUnreadCount = 0;
      }
      this.countTotalUnread();
    },

    closeCategoryModal() {
      this.showCategoryModal = false;
      this.currentCategoryNotices = [];
    },

    viewOrderDetail(notice) {
      if (notice.orderId) {
        if (this.currentCategoryType === 'hotel') {
          uni.navigateTo({
            url: `/pages/order/hotel-order-detail?id=${notice.orderId}`
          });
        } else {
          uni.navigateTo({
            url: `/pages/order/flight-order-detail?id=${notice.orderId}`
          });
        }
      } else {
        uni.showModal({
          title: notice.title,
          content: notice.preview,
          showCancel: false,
          confirmText: '知道了'
        });
      }
    },

    markNoticeRead(noticeId) {
      uni.request({
        url: `http://localhost:8080/api/notices/${noticeId}/read`,
        method: "PUT",
        success: (res) => {
          console.log('标记已读成功, noticeId:', noticeId);
        },
        fail: (err) => {
          console.error('标记已读失败：', err);
        }
      });
    },

    clearAllUnread() {
      if (!this.username) {
        uni.showToast({ title: '请先登录', icon: 'none' });
        return;
      }

      uni.request({
        url: `http://localhost:8080/api/notices/user/${this.username}/read-all`,
        method: "PUT",
        success: (res) => {
          console.log('批量标记已读成功');
        },
        fail: (err) => {
          console.error('批量标记已读失败：', err);
        }
      });

      this.hotelOrdersList.forEach(notice => { notice.read = true; });
      this.flightOrdersList.forEach(notice => { notice.read = true; });
      this.hotelUnreadCount = 0;
      this.flightUnreadCount = 0;
      this.aiUnreadCount = 0;

      this.merchantMessages.forEach(merchant => {
        merchant.unreadCount = 0;
        const savedMessages = uni.getStorageSync('merchant_messages') || {};
        const merchantId = merchant.id;
        if (savedMessages[merchantId]) {
          savedMessages[merchantId].unreadCount = 0;
          uni.setStorageSync('merchant_messages', savedMessages);
        }
        uni.request({
          url: `http://localhost:8080/api/messages/read?role=user&identifier=${this.username}&otherParty=${merchant.merchantId || merchant.id}`,
          method: 'POST',
          fail: (err) => {
            console.error('标记商家消息已读失败：', err);
          }
        });
      });

      this.countTotalUnread();
      uni.showToast({ title: '已全部标为已读', icon: 'success' });
    },

    loadHotelOrders(callback) {
      if (!this.username) {
        this.hotelOrders = [];
        this.generateMerchantMessages();
        if (callback) callback();
        return;
      }

      uni.request({
        url: "http://localhost:8080/api/hotel-orders/user?username=" + this.username,
        method: "GET",
        success: (res) => {
          let orders = [];
          if (res.data && res.data.code === 200) {
            orders = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            orders = res.data;
          }
          this.hotelOrders = orders;
          this.generateMerchantMessages();
          if (callback) callback();
        },
        fail: (err) => {
          this.hotelOrders = [];
          this.generateMerchantMessages();
          if (callback) callback();
        }
      });
    },

    loadFlightOrders(callback) {
      if (!this.username) {
        if (callback) callback();
        return;
      }

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
          if (callback) callback();
        },
        fail: () => {
          if (callback) callback();
        }
      });
    },

    generateMerchantMessages() {
      if (!this.hotelOrders || this.hotelOrders.length === 0) {
        this.merchantMessages = [];
        this.countTotalUnread();
        return;
      }

      const savedMessages = uni.getStorageSync('merchant_messages') || {};
      const merchants = {};

      this.hotelOrders.forEach(order => {
        const validStatuses = ['待支付', '待确认', '已确认', '已入住', '已完成', '已支付'];
        if (!validStatuses.includes(order.status)) {
          return;
        }

        const hotelId = order.hotelId || order.id;
        const hotelName = order.name || order.hotelName || `酒店${hotelId}`;

        if (!merchants[hotelId]) {
          let unreadCount = 0;
          let messages = [];
          let lastMessage = '';
          let lastTime = this.formatTime(new Date(order.createTime || Date.now()));

          if (savedMessages[hotelId]) {
            unreadCount = savedMessages[hotelId].unreadCount || 0;
            messages = savedMessages[hotelId].messages || [];
            if (savedMessages[hotelId].lastMessage) {
              lastMessage = savedMessages[hotelId].lastMessage;
            }
            if (savedMessages[hotelId].lastTime) {
              lastTime = savedMessages[hotelId].lastTime;
            }
          }

          if (messages.length === 0 && !lastMessage) {
            if (order.status === '已确认' || order.status === '已支付') {
              lastMessage = `订单已确认，欢迎入住${hotelName}`;
            } else if (order.status === '待确认') {
              lastMessage = `订单已支付，等待商家确认中`;
            } else if (order.status === '已入住') {
              lastMessage = `已入住${hotelName}`;
            } else if (order.status === '已完成') {
              lastMessage = `订单已完成，感谢入住${hotelName}`;
            } else if (order.status === '待支付') {
              lastMessage = `订单待支付，请尽快完成支付`;
            } else {
              lastMessage = `订单状态：${order.status}`;
            }
            lastTime = this.formatTime(new Date(order.createTime || Date.now()));
          }

          if (!lastMessage) {
            lastMessage = `订单 ${order.id} 状态：${order.status}`;
          }

          merchants[hotelId] = {
            id: hotelId,
            userId: order.username,
            merchantId: order.merchantId || hotelId.toString(),
            hotelId: hotelId,
            orderId: order.id,
            name: hotelName,
            avatar: hotelName.charAt(0),
            avatarType: 'text',
            lastMessage: lastMessage,
            lastTime: lastTime,
            unreadCount: unreadCount,
            messages: messages
          };
        }
      });

      this.merchantMessages = Object.values(merchants);
      this.refreshMerchantUnreadCount();
    },

    saveMerchantMessages(merchantId, messages, unreadCount, lastMessage, lastTime) {
      const savedMessages = uni.getStorageSync('merchant_messages') || {};
      const merchant = this.merchantMessages.find(m => m.id === merchantId);

      let shortLastMsg = lastMessage;
      if (lastMessage && lastMessage.includes('\n')) {
        shortLastMsg = lastMessage.split('\n')[0] || lastMessage.substring(0, 50);
      }
      if (shortLastMsg && shortLastMsg.length > 50) {
        shortLastMsg = shortLastMsg.substring(0, 50) + '...';
      }

      savedMessages[merchantId] = {
        id: merchantId,
        messages,
        unreadCount,
        lastMessage: shortLastMsg,
        lastTime,
        name: merchant?.name || '',
        avatar: merchant?.avatar || '/static/hotel-avatar.png'
      };
      uni.setStorageSync('merchant_messages', savedMessages);
    },

    countTotalUnread() {
      let totalUnread = this.aiUnreadCount + this.hotelUnreadCount + this.flightUnreadCount;
      this.merchantMessages.forEach(merchant => {
        totalUnread += (merchant.unreadCount || 0);
      });

      if (totalUnread > 0) {
        uni.setTabBarBadge({
          index: 2,
          text: totalUnread > 99 ? '99+' : totalUnread.toString()
        });
      } else {
        uni.removeTabBarBadge({ index: 2 });
      }
    },

    goToAIChat() {
      uni.navigateTo({
        url: '/pages/ai-chat/ai-chat'
      });
      this.aiUnreadCount = 0;
      this.countTotalUnread();
    },

    goToMerchantChat(merchant) {
      this.currentMerchant = merchant;

      this.loadChatHistoryFromServer(merchant, () => {
        this.currentMessages = [...this.currentMerchant.messages];
        this.currentMerchant.unreadCount = 0;

        const savedMessages = uni.getStorageSync('merchant_messages') || {};
        const merchantId = merchant.id;
        if (savedMessages[merchantId]) {
          savedMessages[merchantId].unreadCount = 0;
          uni.setStorageSync('merchant_messages', savedMessages);
        }

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
              const lastMsg = messages[messages.length - 1];
              if (lastMsg) {
                merchant.lastTime = this.formatTime(lastMsg.createTime);
              }
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
        const shortUserMsg = userMsg.length > 50 ? userMsg.substring(0, 50) + '...' : userMsg;
        this.currentMerchant.lastMessage = shortUserMsg;
        this.currentMerchant.lastTime = this.formatTime(new Date());
        this.currentMerchant.messages = this.currentMessages;
        this.saveMerchantMessages(this.currentMerchant.id, this.currentMessages, 0, shortUserMsg, this.formatTime(new Date()));
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
    },

    startUnreadPolling() {
      this.stopUnreadPolling();
      this.unreadTimer = setInterval(() => {
        this.refreshUnreadCount();
      }, 30000);
    },

    stopUnreadPolling() {
      if (this.unreadTimer) {
        clearInterval(this.unreadTimer);
        this.unreadTimer = null;
      }
    }
  }
};
</script>

<style scoped>
/* 全局样式 - 蓝紫渐变 */
page {
  background: linear-gradient(180deg, #f0efff 0%, #e8e6ff 100%);
}

.container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f0efff 0%, #e8e6ff 100%);
}

/* 头部样式 - 蓝紫渐变 */
.header {
  background: linear-gradient(135deg, #5b5ce6, #8b5cf6);
  padding: 32rpx 30rpx;
  padding-top: calc(32rpx + constant(safe-area-inset-top));
  padding-top: calc(32rpx + env(safe-area-inset-top));
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(91, 92, 230, 0.25);
}

.header .title {
  color: #fff;
  font-size: 36rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
}

.clear-btn {
  color: rgba(255,255,255,0.95);
  font-size: 26rpx;
  padding: 10rpx 24rpx;
  background: rgba(255,255,255,0.2);
  border-radius: 40rpx;
  transition: all 0.2s ease;
}

.clear-btn:active {
  background: rgba(255,255,255,0.3);
}

/* 消息列表 */
.message-list {
  flex: 1;
  padding: 16rpx 0;
}

/* 聊天项通用样式 */
.chat-item {
  display: flex;
  align-items: center;
  padding: 28rpx 30rpx;
  background-color: #fff;
  margin-bottom: 2rpx;
  transition: background-color 0.2s ease;
}

.chat-item:active {
  background-color: #f0efff;
}

.ai-chat {
  background-color: #fff;
}

.notice-chat {
  background-color: #fff;
}

/* 头像通用样式 */
.chat-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  position: relative;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #e8e6ff;
}

/* AI头像样式 - 蓝紫渐变 */
.ai-chat .chat-avatar {
  background: linear-gradient(135deg, #5b5ce6, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 在线状态 */
.online-dot {
  position: absolute;
  bottom: 4rpx;
  right: 4rpx;
  width: 20rpx;
  height: 20rpx;
  background-color: #10b981;
  border-radius: 50%;
  border: 2rpx solid #fff;
}

/* 通知头像 - 蓝紫渐变 */
.notice-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-emoji {
  font-size: 48rpx;
}

.hotel-avatar {
  background: linear-gradient(135deg, #5b5ce6, #8b5cf6);
}

.flight-avatar {
  background: linear-gradient(135deg, #6366f1, #a855f7);
}

/* 内容区域 */
.chat-content {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 12rpx;
}

.chat-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 240rpx;
}

.chat-time {
  font-size: 22rpx;
  color: #9ca3af;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.chat-preview {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
}

.preview-text {
  font-size: 26rpx;
  color: #6b7280;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 未读徽章 */
.chat-badge {
  min-width: 36rpx;
  height: 36rpx;
  background: #ef4444;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 10rpx;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.chat-badge text {
  color: #fff;
  font-size: 20rpx;
  font-weight: 600;
}

/* 分隔线 */
.section-divider {
  padding: 24rpx 30rpx 16rpx 30rpx;
  background: linear-gradient(180deg, #f0efff 0%, #e8e6ff 100%);
}

.divider-text {
  font-size: 24rpx;
  color: #9ca3af;
  font-weight: 500;
  letter-spacing: 1rpx;
}

/* 空状态 */
.empty-notice {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
}

.empty-notice .empty-icon {
  font-size: 88rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
  color: #c7d2fe;
}

.empty-notice .empty-text {
  font-size: 26rpx;
  color: #9ca3af;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-state .empty-icon {
  font-size: 88rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
  color: #c7d2fe;
}

.empty-state .empty-text {
  font-size: 28rpx;
  color: #9ca3af;
}

/* ========== 分类通知弹窗 ========== */
.category-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-modal .modal-container {
  width: 92%;
  height: 80vh;
  background: #fff;
  border-radius: 32rpx;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 20rpx 40rpx rgba(0, 0, 0, 0.15);
}

.category-modal .modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  background: linear-gradient(135deg, #5b5ce6, #8b5cf6);
}

.category-modal .modal-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
}

.category-modal .modal-close {
  font-size: 48rpx;
  color: rgba(255,255,255,0.85);
  padding: 8rpx;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 30rpx;
}

.category-modal .modal-close:active {
  background: rgba(255,255,255,0.15);
}

.notice-list {
  flex: 1;
  padding: 24rpx;
  background: #f5f3ff;
}

.notice-item {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  border: 1rpx solid #e9e8ff;
  position: relative;
}

.notice-item:active {
  background-color: #f0efff;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 16rpx;
}

.notice-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
}

.notice-time {
  font-size: 22rpx;
  color: #9ca3af;
}

.notice-content {
  margin-top: 12rpx;
}

.notice-preview {
  font-size: 26rpx;
  color: #6b7280;
  line-height: 1.5;
}

.notice-badge {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  background: #ef4444;
  border-radius: 20rpx;
  padding: 6rpx 14rpx;
}

.notice-badge text {
  color: #fff;
  font-size: 20rpx;
  font-weight: 500;
}

.empty-notice-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-notice-detail .empty-icon {
  font-size: 88rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
  color: #c7d2fe;
}

.empty-notice-detail .empty-text {
  font-size: 28rpx;
  color: #9ca3af;
}

/* ========== 商家聊天弹窗 ========== */
.merchant-chat-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.merchant-chat-modal .modal-container {
  width: 94%;
  height: 88vh;
  background: #f5f3ff;
  border-radius: 32rpx;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 20rpx 40rpx rgba(0, 0, 0, 0.15);
}

.merchant-chat-modal .modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 30rpx;
  background: linear-gradient(135deg, #5b5ce6, #8b5cf6);
}

.modal-header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.merchant-chat-modal .modal-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
}

.merchant-chat-modal .modal-close {
  font-size: 48rpx;
  color: rgba(255,255,255,0.85);
  padding: 8rpx;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 30rpx;
}

.merchant-chat-modal .modal-close:active {
  background: rgba(255,255,255,0.15);
}

/* 聊天消息区域 */
.chat-messages {
  flex: 1;
  padding: 30rpx 24rpx;
  background: #f5f3ff;
  overflow-y: auto;
}

.message-list-container {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.message-item {
  width: 100%;
}

/* 商家消息（左侧） */
.message-left {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 12rpx;
}

.message-left .message-avatar {
  flex-shrink: 0;
}

/* 用户消息（右侧） */
.message-right {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 12rpx;
}

.message-avatar {
  width: 70rpx;
  height: 70rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.small-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background-color: #e8e6ff;
}

.user-avatar {
  width: 64rpx;
  height: 64rpx;
  background: linear-gradient(135deg, #5b5ce6, #8b5cf6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(91, 92, 230, 0.3);
}

/* 消息气泡 */
.message-bubble {
  max-width: 68%;
  padding: 20rpx 28rpx;
  border-radius: 28rpx;
  font-size: 28rpx;
  line-height: 1.5;
  word-wrap: break-word;
  white-space: normal;
}

.user-bubble {
  background: linear-gradient(135deg, #5b5ce6, #8b5cf6);
  color: #fff;
  border-radius: 28rpx 28rpx 8rpx 28rpx;
  box-shadow: 0 2rpx 8rpx rgba(91, 92, 230, 0.2);
}

.merchant-bubble {
  background: #ffffff;
  color: #1f2937;
  border-radius: 28rpx 28rpx 28rpx 8rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

/* 快捷回复 */
.quick-replies {
  padding: 16rpx 24rpx;
  background: #fff;
  border-top: 1rpx solid #e9e8ff;
  border-bottom: 1rpx solid #e9e8ff;
}

.quick-scroll {
  white-space: nowrap;
  display: flex;
  gap: 16rpx;
}

.quick-reply-item {
  display: inline-block;
  padding: 14rpx 28rpx;
  background: #eeedff;
  border-radius: 48rpx;
  font-size: 26rpx;
  color: #5b5ce6;
  transition: all 0.2s ease;
}

.quick-reply-item:active {
  background: #e2e1ff;
  transform: scale(0.96);
}

/* 输入区域 */
.chat-input-area {
  display: flex;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  align-items: center;
  gap: 20rpx;
  border-top: 1rpx solid #e9e8ff;
}

.chat-input {
  flex: 1;
  height: 72rpx;
  background: #eeedff;
  border-radius: 40rpx;
  padding: 0 28rpx;
  font-size: 28rpx;
  color: #1f2937;
}

.chat-input::placeholder {
  color: #a5b4fc;
}

.send-btn {
  width: 100rpx;
  height: 68rpx;
  background: linear-gradient(135deg, #5b5ce6, #8b5cf6);
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  line-height: 1;
  padding: 0;
  box-shadow: 0 2rpx 8rpx rgba(91, 92, 230, 0.3);
  transition: transform 0.1s ease;
}

.send-btn:active {
  transform: scale(0.96);
}

.send-btn::after {
  border: none;
}

/* ========== 文字头像样式 ========== */
.avatar-text {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 44rpx;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.avatar-text-char {
  font-size: 48rpx;
  font-weight: 600;
}

/* 小尺寸文字头像 */
.small-avatar-text {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

/* 弹窗头部文字头像 */
.modal-avatar-text {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
}

.modal-avatar-img {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  margin-right: 0;
}
</style>
