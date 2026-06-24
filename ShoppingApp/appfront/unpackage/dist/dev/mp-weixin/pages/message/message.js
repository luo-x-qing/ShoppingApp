"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const _sfc_main = {
  data() {
    return {
      username: "",
      // AI
      aiUnreadCount: 0,
      aiLastMessage: "我是AI旅游规划助手，点击开始规划你的行程 ✈️",
      aiLastMessageTime: this.formatTime(/* @__PURE__ */ new Date()),
      // 分类通知数据
      hotelOrdersList: [],
      flightOrdersList: [],
      hotelUnreadCount: 0,
      flightUnreadCount: 0,
      hotelLastMessage: "",
      hotelLastTime: "",
      flightLastMessage: "",
      flightLastTime: "",
      // 分类弹窗
      showCategoryModal: false,
      categoryTitle: "",
      currentCategoryNotices: [],
      currentCategoryType: "",
      // 商家消息
      merchantMessages: [],
      showMerchantChat: false,
      currentMerchant: null,
      currentMessages: [],
      merchantInput: "",
      quickReplies: ["好的，谢谢", "请问有优惠吗？", "可以改期吗？", "退订政策是什么？", "酒店地址在哪？", "有停车场吗？"],
      hotelOrders: [],
      flightOrders: [],
      // 轮询定时器
      unreadTimer: null,
      isLoading: false
    };
  },
  onLoad() {
    this.username = common_vendor.index.getStorageSync("loginUsername") || "";
    this.loadOrderNotices();
    this.loadHotelOrders();
    this.loadFlightOrders();
  },
  onShow() {
    this.username = common_vendor.index.getStorageSync("loginUsername") || "";
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
        "#1890ff",
        "#52c41a",
        "#faad14",
        "#f5222d",
        "#722ed1",
        "#13c2c2",
        "#eb2f96",
        "#fa8c16",
        "#a0d911",
        "#2f54eb"
      ];
      let hash = 0;
      for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash);
      }
      const index = Math.abs(hash) % colors.length;
      return colors[index];
    },
    refreshUnreadCount() {
      if (!this.username)
        return;
      this.loadOrderNotices(() => {
        this.refreshMerchantUnreadCount();
      });
    },
    refreshMerchantUnreadCount() {
      if (!this.username || this.merchantMessages.length === 0) {
        this.countTotalUnread();
        return;
      }
      const merchantIds = this.merchantMessages.map((m) => m.merchantId || m.id);
      let completed = 0;
      merchantIds.forEach((merchantId, index) => {
        common_vendor.index.request({
          url: `http://localhost:8080/api/messages/unread?role=user&identifier=${this.username}&otherParty=${merchantId}`,
          method: "GET",
          success: (res) => {
            var _a;
            if (res.data && res.data.code === 200) {
              const count = ((_a = res.data.data) == null ? void 0 : _a.unreadCount) || 0;
              this.merchantMessages[index].unreadCount = count;
              const savedMessages = common_vendor.index.getStorageSync("merchant_messages") || {};
              const id = this.merchantMessages[index].id;
              if (savedMessages[id]) {
                savedMessages[id].unreadCount = count;
                common_vendor.index.setStorageSync("merchant_messages", savedMessages);
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
        if (callback)
          callback();
        return;
      }
      common_vendor.index.request({
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
          notices.forEach((notice) => {
            if (notice.type === "HOTEL_ORDER" || notice.title && notice.title.includes("酒店") || notice.content && notice.content.includes("酒店预订")) {
              this.hotelOrdersList.push({
                id: notice.id,
                type: "hotel",
                orderId: notice.orderId,
                title: notice.title || "酒店预订成功",
                preview: notice.content,
                time: this.formatTime(notice.createTime),
                read: notice.isRead || false,
                rawContent: notice.content,
                createTime: notice.createTime
              });
            } else if (notice.type === "FLIGHT_ORDER" || notice.title && notice.title.includes("机票") || notice.content && notice.content.includes("机票订购")) {
              this.flightOrdersList.push({
                id: notice.id,
                type: "flight",
                orderId: notice.orderId,
                title: notice.title || "机票订购成功",
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
          this.hotelUnreadCount = this.hotelOrdersList.filter((n) => !n.read).length;
          this.flightUnreadCount = this.flightOrdersList.filter((n) => !n.read).length;
          if (this.hotelOrdersList.length > 0) {
            const lastHotel = this.hotelOrdersList[0];
            this.hotelLastMessage = lastHotel.preview.substring(0, 30) + (lastHotel.preview.length > 30 ? "..." : "");
            this.hotelLastTime = lastHotel.time;
          } else {
            this.hotelLastMessage = "暂无酒店订单通知";
            this.hotelLastTime = "";
          }
          if (this.flightOrdersList.length > 0) {
            const lastFlight = this.flightOrdersList[0];
            this.flightLastMessage = lastFlight.preview.substring(0, 30) + (lastFlight.preview.length > 30 ? "..." : "");
            this.flightLastTime = lastFlight.time;
          } else {
            this.flightLastMessage = "暂无机票订单通知";
            this.flightLastTime = "";
          }
          if (callback)
            callback();
        },
        fail: (err) => {
          this.hotelOrdersList = [];
          this.flightOrdersList = [];
          if (callback)
            callback();
        }
      });
    },
    viewCategoryNotices(type) {
      if (type === "hotel") {
        this.categoryTitle = "酒店预订成功";
        this.currentCategoryNotices = [...this.hotelOrdersList];
        this.currentCategoryType = "hotel";
      } else {
        this.categoryTitle = "机票订购成功";
        this.currentCategoryNotices = [...this.flightOrdersList];
        this.currentCategoryType = "flight";
      }
      this.showCategoryModal = true;
      const unreadNotices = this.currentCategoryNotices.filter((n) => !n.read);
      unreadNotices.forEach((notice) => {
        notice.read = true;
        this.markNoticeRead(notice.id);
      });
      if (type === "hotel") {
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
        if (this.currentCategoryType === "hotel") {
          common_vendor.index.navigateTo({
            url: `/pages/order/hotel-order-detail?id=${notice.orderId}`
          });
        } else {
          common_vendor.index.navigateTo({
            url: `/pages/order/flight-order-detail?id=${notice.orderId}`
          });
        }
      } else {
        common_vendor.index.showModal({
          title: notice.title,
          content: notice.preview,
          showCancel: false,
          confirmText: "知道了"
        });
      }
    },
    markNoticeRead(noticeId) {
      common_vendor.index.request({
        url: `http://localhost:8080/api/notices/${noticeId}/read`,
        method: "PUT",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/message/message.vue:489", "标记已读成功, noticeId:", noticeId);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/message/message.vue:492", "标记已读失败：", err);
        }
      });
    },
    clearAllUnread() {
      if (!this.username) {
        common_vendor.index.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      common_vendor.index.request({
        url: `http://localhost:8080/api/notices/user/${this.username}/read-all`,
        method: "PUT",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/message/message.vue:507", "批量标记已读成功");
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/message/message.vue:510", "批量标记已读失败：", err);
        }
      });
      this.hotelOrdersList.forEach((notice) => {
        notice.read = true;
      });
      this.flightOrdersList.forEach((notice) => {
        notice.read = true;
      });
      this.hotelUnreadCount = 0;
      this.flightUnreadCount = 0;
      this.aiUnreadCount = 0;
      this.merchantMessages.forEach((merchant) => {
        merchant.unreadCount = 0;
        const savedMessages = common_vendor.index.getStorageSync("merchant_messages") || {};
        const merchantId = merchant.id;
        if (savedMessages[merchantId]) {
          savedMessages[merchantId].unreadCount = 0;
          common_vendor.index.setStorageSync("merchant_messages", savedMessages);
        }
        common_vendor.index.request({
          url: `http://localhost:8080/api/messages/read?role=user&identifier=${this.username}&otherParty=${merchant.merchantId || merchant.id}`,
          method: "POST",
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/message/message.vue:532", "标记商家消息已读失败：", err);
          }
        });
      });
      this.countTotalUnread();
      common_vendor.index.showToast({ title: "已全部标为已读", icon: "success" });
    },
    loadHotelOrders(callback) {
      if (!this.username) {
        this.hotelOrders = [];
        this.generateMerchantMessages();
        if (callback)
          callback();
        return;
      }
      common_vendor.index.request({
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
          if (callback)
            callback();
        },
        fail: (err) => {
          this.hotelOrders = [];
          this.generateMerchantMessages();
          if (callback)
            callback();
        }
      });
    },
    loadFlightOrders(callback) {
      if (!this.username) {
        if (callback)
          callback();
        return;
      }
      common_vendor.index.request({
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
          if (callback)
            callback();
        },
        fail: () => {
          if (callback)
            callback();
        }
      });
    },
    generateMerchantMessages() {
      if (!this.hotelOrders || this.hotelOrders.length === 0) {
        this.merchantMessages = [];
        this.countTotalUnread();
        return;
      }
      const savedMessages = common_vendor.index.getStorageSync("merchant_messages") || {};
      const merchants = {};
      this.hotelOrders.forEach((order) => {
        const validStatuses = ["待支付", "待确认", "已确认", "已入住", "已完成", "已支付"];
        if (!validStatuses.includes(order.status)) {
          return;
        }
        const hotelId = order.hotelId || order.id;
        const hotelName = order.name || order.hotelName || `酒店${hotelId}`;
        if (!merchants[hotelId]) {
          let unreadCount = 0;
          let messages = [];
          let lastMessage = "";
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
            if (order.status === "已确认" || order.status === "已支付") {
              lastMessage = `订单已确认，欢迎入住${hotelName}`;
            } else if (order.status === "待确认") {
              lastMessage = `订单已支付，等待商家确认中`;
            } else if (order.status === "已入住") {
              lastMessage = `已入住${hotelName}`;
            } else if (order.status === "已完成") {
              lastMessage = `订单已完成，感谢入住${hotelName}`;
            } else if (order.status === "待支付") {
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
            hotelId,
            orderId: order.id,
            name: hotelName,
            avatar: hotelName.charAt(0),
            avatarType: "text",
            lastMessage,
            lastTime,
            unreadCount,
            messages
          };
        }
      });
      this.merchantMessages = Object.values(merchants);
      this.refreshMerchantUnreadCount();
    },
    saveMerchantMessages(merchantId, messages, unreadCount, lastMessage, lastTime) {
      const savedMessages = common_vendor.index.getStorageSync("merchant_messages") || {};
      const merchant = this.merchantMessages.find((m) => m.id === merchantId);
      let shortLastMsg = lastMessage;
      if (lastMessage && lastMessage.includes("\n")) {
        shortLastMsg = lastMessage.split("\n")[0] || lastMessage.substring(0, 50);
      }
      if (shortLastMsg && shortLastMsg.length > 50) {
        shortLastMsg = shortLastMsg.substring(0, 50) + "...";
      }
      savedMessages[merchantId] = {
        id: merchantId,
        messages,
        unreadCount,
        lastMessage: shortLastMsg,
        lastTime,
        name: (merchant == null ? void 0 : merchant.name) || "",
        avatar: (merchant == null ? void 0 : merchant.avatar) || "/static/hotel-avatar.png"
      };
      common_vendor.index.setStorageSync("merchant_messages", savedMessages);
    },
    countTotalUnread() {
      let totalUnread = this.aiUnreadCount + this.hotelUnreadCount + this.flightUnreadCount;
      this.merchantMessages.forEach((merchant) => {
        totalUnread += merchant.unreadCount || 0;
      });
      if (totalUnread > 0) {
        common_vendor.index.setTabBarBadge({
          index: 2,
          text: totalUnread > 99 ? "99+" : totalUnread.toString()
        });
      } else {
        common_vendor.index.removeTabBarBadge({ index: 2 });
      }
    },
    goToAIChat() {
      common_vendor.index.navigateTo({
        url: "/pages/ai-chat/ai-chat"
      });
      this.aiUnreadCount = 0;
      this.countTotalUnread();
    },
    goToMerchantChat(merchant) {
      this.currentMerchant = merchant;
      this.loadChatHistoryFromServer(merchant, () => {
        this.currentMessages = [...this.currentMerchant.messages];
        this.currentMerchant.unreadCount = 0;
        const savedMessages = common_vendor.index.getStorageSync("merchant_messages") || {};
        const merchantId = merchant.id;
        if (savedMessages[merchantId]) {
          savedMessages[merchantId].unreadCount = 0;
          common_vendor.index.setStorageSync("merchant_messages", savedMessages);
        }
        common_vendor.index.request({
          url: `http://localhost:8080/api/messages/read?role=user&identifier=${this.username}&otherParty=${merchant.merchantId || merchant.id}`,
          method: "POST"
        });
        this.saveMerchantMessages(merchant.id, this.currentMessages, 0, merchant.lastMessage, merchant.lastTime);
        const index = this.merchantMessages.findIndex((m) => m.id === merchant.id);
        if (index !== -1)
          this.merchantMessages[index].unreadCount = 0;
        this.countTotalUnread();
        this.showMerchantChat = true;
        setTimeout(() => {
          common_vendor.index.createSelectorQuery().select("#msg-bottom2").boundingClientRect().exec();
        }, 300);
      });
    },
    loadChatHistoryFromServer(merchant, callback) {
      common_vendor.index.request({
        url: `http://localhost:8080/api/messages/chat?username=${this.username}&merchantId=${merchant.merchantId || merchant.id}&hotelId=${merchant.hotelId || ""}`,
        method: "GET",
        success: (res) => {
          if (res.data && res.data.code === 200) {
            const messages = res.data.data || [];
            if (messages.length > 0) {
              merchant.messages = messages.map((msg) => ({
                role: msg.senderRole,
                content: msg.content
              }));
              const lastMsg = messages[messages.length - 1];
              if (lastMsg) {
                merchant.lastTime = this.formatTime(lastMsg.createTime);
              }
            }
          }
          if (callback)
            callback();
        },
        fail: () => {
          if (callback)
            callback();
        }
      });
    },
    closeMerchantChat() {
      this.showMerchantChat = false;
      this.currentMerchant = null;
      this.currentMessages = [];
      this.merchantInput = "";
    },
    sendQuickReply(reply) {
      this.merchantInput = reply;
      this.sendMerchantMessage();
    },
    sendMerchantMessage() {
      var _a, _b;
      if (!this.merchantInput.trim())
        return;
      const customerUsername = (_a = this.currentMerchant) == null ? void 0 : _a.userId;
      if (!customerUsername) {
        common_vendor.index.showToast({ title: "无法获取客户信息", icon: "none" });
        return;
      }
      const userMsg = this.merchantInput.trim();
      const merchantId = this.currentMerchant.merchantId || this.currentMerchant.id;
      const hotelId = this.currentMerchant.hotelId;
      const orderId = this.currentMerchant.orderId;
      const merchantName = ((_b = this.currentMerchant) == null ? void 0 : _b.name) || "商家";
      this.currentMessages.push({ role: "user", content: userMsg });
      this.merchantInput = "";
      setTimeout(() => {
        common_vendor.index.createSelectorQuery().select("#msg-bottom2").boundingClientRect().exec();
      }, 100);
      if (this.currentMerchant) {
        const shortUserMsg = userMsg.length > 50 ? userMsg.substring(0, 50) + "..." : userMsg;
        this.currentMerchant.lastMessage = shortUserMsg;
        this.currentMerchant.lastTime = this.formatTime(/* @__PURE__ */ new Date());
        this.currentMerchant.messages = this.currentMessages;
        this.saveMerchantMessages(this.currentMerchant.id, this.currentMessages, 0, shortUserMsg, this.formatTime(/* @__PURE__ */ new Date()));
      }
      common_vendor.index.request({
        url: "http://localhost:8080/api/messages/send",
        method: "POST",
        data: {
          orderId,
          hotelId,
          merchantId: merchantId.toString(),
          username: customerUsername,
          content: userMsg,
          senderRole: "user",
          isRead: 0
        },
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/message/message.vue:838", "消息发送成功", res.data);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/message/message.vue:841", "消息发送失败", err);
          common_vendor.index.showToast({ title: "发送失败", icon: "none" });
        }
      });
      setTimeout(() => {
        let autoReply = "";
        const lowerMsg = userMsg.toLowerCase();
        if (lowerMsg.includes("优惠") || lowerMsg.includes("折扣")) {
          autoReply = `感谢您对${merchantName}的关注！我们目前有暑期特惠活动，部分房型低至8折。`;
        } else if (lowerMsg.includes("改期") || lowerMsg.includes("修改")) {
          autoReply = `${merchantName}支持改期，请至少提前3天联系我们。请问您想改到哪一天？`;
        } else if (lowerMsg.includes("取消") || lowerMsg.includes("退订")) {
          autoReply = `${merchantName}的取消政策：入住前3天免费取消，入住前1-2天收取50%房费。`;
        } else if (lowerMsg.includes("停车")) {
          autoReply = `${merchantName}提供免费停车场，入住期间可免费停车。`;
        } else if (lowerMsg.includes("早餐")) {
          autoReply = `${merchantName}的早餐时间为7:00-10:00，位于酒店2楼餐厅。`;
        } else if (lowerMsg.includes("地址")) {
          autoReply = `${merchantName}的地址请在订单详情中查看，您也可以打开地图导航。`;
        } else if (lowerMsg.includes("谢谢") || lowerMsg.includes("感谢")) {
          autoReply = `不客气！${merchantName}祝您旅途愉快，有任何问题随时联系我们！😊`;
        } else if (lowerMsg.includes("你好") || lowerMsg.includes("您好")) {
          autoReply = `您好！${merchantName}很高兴为您服务！请问有什么可以帮您的？`;
        } else {
          autoReply = `感谢您的留言！${merchantName}会尽快处理您的问题。祝您生活愉快！`;
        }
        this.currentMessages.push({ role: "merchant", content: autoReply });
        setTimeout(() => {
          common_vendor.index.createSelectorQuery().select("#msg-bottom2").boundingClientRect().exec();
        }, 100);
        if (this.currentMerchant) {
          this.currentMerchant.lastMessage = autoReply;
          this.currentMerchant.lastTime = this.formatTime(/* @__PURE__ */ new Date());
          this.currentMerchant.messages = this.currentMessages;
          this.saveMerchantMessages(this.currentMerchant.id, this.currentMessages, 0, autoReply, this.formatTime(/* @__PURE__ */ new Date()));
        }
        common_vendor.index.request({
          url: "http://localhost:8080/api/messages/send",
          method: "POST",
          data: {
            orderId,
            hotelId,
            merchantId: merchantId.toString(),
            username: customerUsername,
            content: autoReply,
            senderRole: "merchant",
            isRead: 0
          }
        });
      }, 800);
    },
    formatTime(date) {
      if (!date)
        return "刚刚";
      const d = new Date(date);
      const now = /* @__PURE__ */ new Date();
      const diff = now - d;
      if (diff < 6e4)
        return "刚刚";
      if (diff < 36e5)
        return `${Math.floor(diff / 6e4)}分钟前`;
      if (diff < 864e5)
        return `${Math.floor(diff / 36e5)}小时前`;
      if (diff < 6048e5)
        return `${Math.floor(diff / 864e5)}天前`;
      return `${d.getMonth() + 1}月${d.getDate()}日`;
    },
    startUnreadPolling() {
      this.stopUnreadPolling();
      this.unreadTimer = setInterval(() => {
        this.refreshUnreadCount();
      }, 3e4);
    },
    stopUnreadPolling() {
      if (this.unreadTimer) {
        clearInterval(this.unreadTimer);
        this.unreadTimer = null;
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.clearAllUnread && $options.clearAllUnread(...args)),
    b: common_assets._imports_0$1,
    c: common_vendor.t($data.aiLastMessageTime),
    d: common_vendor.t($data.aiLastMessage),
    e: $data.aiUnreadCount > 0
  }, $data.aiUnreadCount > 0 ? {
    f: common_vendor.t($data.aiUnreadCount > 99 ? "99+" : $data.aiUnreadCount)
  } : {}, {
    g: common_vendor.o((...args) => $options.goToAIChat && $options.goToAIChat(...args)),
    h: common_vendor.t($data.hotelLastTime),
    i: common_vendor.t($data.hotelLastMessage),
    j: $data.hotelUnreadCount > 0
  }, $data.hotelUnreadCount > 0 ? {
    k: common_vendor.t($data.hotelUnreadCount > 99 ? "99+" : $data.hotelUnreadCount)
  } : {}, {
    l: common_vendor.o(($event) => $options.viewCategoryNotices("hotel")),
    m: common_vendor.t($data.flightLastTime),
    n: common_vendor.t($data.flightLastMessage),
    o: $data.flightUnreadCount > 0
  }, $data.flightUnreadCount > 0 ? {
    p: common_vendor.t($data.flightUnreadCount > 99 ? "99+" : $data.flightUnreadCount)
  } : {}, {
    q: common_vendor.o(($event) => $options.viewCategoryNotices("flight")),
    r: $data.hotelOrdersList.length === 0 && $data.flightOrdersList.length === 0
  }, $data.hotelOrdersList.length === 0 && $data.flightOrdersList.length === 0 ? {} : {}, {
    s: $data.merchantMessages.length > 0
  }, $data.merchantMessages.length > 0 ? {} : {}, {
    t: common_vendor.f($data.merchantMessages, (merchant, k0, i0) => {
      return common_vendor.e({
        a: merchant.avatarType === "text"
      }, merchant.avatarType === "text" ? {
        b: common_vendor.t(merchant.avatar),
        c: $options.getAvatarColor(merchant.name)
      } : {
        d: merchant.avatar
      }, {
        e: common_vendor.t(merchant.name),
        f: common_vendor.t(merchant.lastTime),
        g: common_vendor.t(merchant.lastMessage),
        h: merchant.unreadCount > 0
      }, merchant.unreadCount > 0 ? {
        i: common_vendor.t(merchant.unreadCount > 99 ? "99+" : merchant.unreadCount)
      } : {}, {
        j: merchant.id,
        k: common_vendor.o(($event) => $options.goToMerchantChat(merchant), merchant.id)
      });
    }),
    v: $data.merchantMessages.length === 0 && $data.hotelOrdersList.length === 0 && $data.flightOrdersList.length === 0
  }, $data.merchantMessages.length === 0 && $data.hotelOrdersList.length === 0 && $data.flightOrdersList.length === 0 ? {} : {}, {
    w: $data.showCategoryModal
  }, $data.showCategoryModal ? common_vendor.e({
    x: common_vendor.t($data.categoryTitle),
    y: common_vendor.o((...args) => $options.closeCategoryModal && $options.closeCategoryModal(...args)),
    z: $data.currentCategoryNotices.length === 0
  }, $data.currentCategoryNotices.length === 0 ? {} : {}, {
    A: common_vendor.f($data.currentCategoryNotices, (notice, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(notice.title),
        b: common_vendor.t(notice.time),
        c: common_vendor.t(notice.preview),
        d: !notice.read
      }, !notice.read ? {} : {}, {
        e: notice.id,
        f: common_vendor.o(($event) => $options.viewOrderDetail(notice), notice.id)
      });
    }),
    B: common_vendor.o(() => {
    }),
    C: common_vendor.o((...args) => $options.closeCategoryModal && $options.closeCategoryModal(...args))
  }) : {}, {
    D: $data.showMerchantChat
  }, $data.showMerchantChat ? common_vendor.e({
    E: $data.currentMerchant.avatarType === "text"
  }, $data.currentMerchant.avatarType === "text" ? {
    F: common_vendor.t($data.currentMerchant.avatar),
    G: $options.getAvatarColor($data.currentMerchant.name)
  } : {
    H: $data.currentMerchant.avatar
  }, {
    I: common_vendor.t($data.currentMerchant.name),
    J: common_vendor.o((...args) => $options.closeMerchantChat && $options.closeMerchantChat(...args)),
    K: common_vendor.f($data.currentMessages, (msg, idx, i0) => {
      return common_vendor.e({
        a: msg.role === "merchant"
      }, msg.role === "merchant" ? common_vendor.e({
        b: $data.currentMerchant.avatarType === "text"
      }, $data.currentMerchant.avatarType === "text" ? {
        c: common_vendor.t($data.currentMerchant.avatar),
        d: $options.getAvatarColor($data.currentMerchant.name)
      } : {
        e: $data.currentMerchant.avatar
      }, {
        f: common_vendor.t(msg.content)
      }) : {
        g: common_vendor.t(msg.content)
      }, {
        h: idx
      });
    }),
    L: $data.quickReplies.length > 0
  }, $data.quickReplies.length > 0 ? {
    M: common_vendor.f($data.quickReplies, (reply, idx, i0) => {
      return {
        a: common_vendor.t(reply),
        b: idx,
        c: common_vendor.o(($event) => $options.sendQuickReply(reply), idx)
      };
    })
  } : {}, {
    N: common_vendor.o((...args) => $options.sendMerchantMessage && $options.sendMerchantMessage(...args)),
    O: $data.merchantInput,
    P: common_vendor.o(($event) => $data.merchantInput = $event.detail.value),
    Q: common_vendor.o((...args) => $options.sendMerchantMessage && $options.sendMerchantMessage(...args)),
    R: common_vendor.o(() => {
    }),
    S: common_vendor.o((...args) => $options.closeMerchantChat && $options.closeMerchantChat(...args))
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-4c1b26cf"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/message/message.js.map
