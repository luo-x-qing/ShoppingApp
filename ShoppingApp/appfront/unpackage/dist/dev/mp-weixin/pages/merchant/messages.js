"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      merchantId: null,
      merchantName: "",
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
      inputMessage: "",
      // 快捷回复
      quickReplies: [
        "好的，收到！",
        "请问有什么可以帮您？",
        "感谢您的预订！",
        "稍后为您处理",
        "请提供订单号",
        "已为您安排"
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
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        if (userInfo && userInfo.id) {
          this.merchantId = userInfo.id;
          this.merchantName = userInfo.shopName || userInfo.name || "商家";
          common_vendor.index.__f__("log", "at pages/merchant/messages.vue:179", "商家ID:", this.merchantId);
        } else {
          common_vendor.index.showToast({ title: "请先登录", icon: "none" });
          setTimeout(() => {
            common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
          }, 1500);
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/merchant/messages.vue:187", "读取商家信息失败", e);
      }
    },
    // 加载会话列表
    loadConversations() {
      if (!this.merchantId)
        return;
      common_vendor.index.__f__("log", "at pages/merchant/messages.vue:195", "开始加载商家会话列表, 商家ID:", this.merchantId);
      this.loadConversationsFromServer();
    },
    // 从服务器加载会话列表
    loadConversationsFromServer() {
      common_vendor.index.request({
        url: `http://localhost:8080/api/messages/merchant/conversations?merchantId=${this.merchantId}`,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/messages.vue:205", "会话列表查询结果:", res.data);
          if (res.data && res.data.code === 200) {
            const conversations = res.data.data || [];
            common_vendor.index.__f__("log", "at pages/merchant/messages.vue:208", "从服务器获取到会话数量:", conversations.length);
            if (conversations.length > 0) {
              this.conversations = conversations.map((conv) => {
                const rawMessage = conv.lastMessage || "暂无消息";
                const displayMessage = rawMessage.replace(/\n/g, " ").replace(/\s+/g, " ");
                const key = `chat_${this.merchantId}_${conv.userId}`;
                const saved = common_vendor.index.getStorageSync(key);
                return {
                  userId: conv.userId,
                  userName: conv.userName || conv.userId,
                  hotelName: conv.hotelName || "酒店",
                  hotelId: conv.hotelId,
                  orderId: conv.orderId,
                  merchantId: this.merchantId,
                  lastMessage: displayMessage,
                  lastSender: conv.lastSender || "merchant",
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
          common_vendor.index.__f__("error", "at pages/merchant/messages.vue:243", "获取会话列表失败:", err);
          this.loadOrdersByMerchantDirect();
        }
      });
    },
    // 直接根据商家ID查询订单
    loadOrdersByMerchantDirect() {
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotel-orders/merchant/orders?merchantId=${this.merchantId}`,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/messages.vue:255", "订单查询结果:", res.data);
          if (res.data && res.data.code === 200) {
            const orders = res.data.data || [];
            common_vendor.index.__f__("log", "at pages/merchant/messages.vue:258", "直接获取到订单数量:", orders.length);
            this.generateConversationsFromOrders(orders);
          } else {
            common_vendor.index.__f__("log", "at pages/merchant/messages.vue:261", "未获取到订单，尝试从本地存储加载");
            this.loadFromLocalStorage();
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/merchant/messages.vue:266", "获取订单失败:", err);
          this.loadFromLocalStorage();
        }
      });
    },
    // 根据订单生成会话列表
    generateConversationsFromOrders(orders) {
      common_vendor.index.__f__("log", "at pages/merchant/messages.vue:274", "=== 开始生成会话 ===");
      common_vendor.index.__f__("log", "at pages/merchant/messages.vue:275", "传入订单数量:", orders.length);
      const conversationsMap = {};
      orders.forEach((order) => {
        const userId = order.username || `order_${order.id}`;
        const hotelName = order.name || order.hotelName || `酒店${order.hotelId}`;
        const userName = order.contactName || userId;
        if (!conversationsMap[userId]) {
          const key = `chat_${this.merchantId}_${userId}`;
          const saved = common_vendor.index.getStorageSync(key);
          const rawMessage = saved ? saved.lastMessage : `欢迎预订${hotelName}！`;
          const displayMessage = rawMessage ? rawMessage.replace(/\n/g, " ").replace(/\s+/g, " ") : "";
          conversationsMap[userId] = {
            userId,
            userName,
            hotelName,
            hotelId: order.hotelId,
            orderId: order.id,
            merchantId: this.merchantId,
            lastMessage: displayMessage,
            lastSender: saved ? saved.lastSender : "merchant",
            lastTime: saved ? saved.lastTime : this.formatTime(order.createTime),
            unreadCount: 0,
            showDelete: false,
            messages: saved ? saved.messages : [
              {
                role: "merchant",
                content: `🎉 欢迎预订${hotelName}！

📅 入住日期：${order.checkIn || "待确认"}
📅 退房日期：${order.checkOut || "待确认"}
💰 订单金额：¥${order.price || 0}

如有任何问题，请随时联系我们！祝您入住愉快！`
              }
            ]
          };
        }
      });
      this.conversations = Object.values(conversationsMap);
      common_vendor.index.__f__("log", "at pages/merchant/messages.vue:314", "最终生成的会话数量:", this.conversations.length);
      this.fetchUnreadCountsFromServer();
    },
    // 从服务器获取未读数
    fetchUnreadCountsFromServer() {
      this.conversations.forEach((conv) => {
        common_vendor.index.request({
          url: `http://localhost:8080/api/messages/unread?role=merchant&identifier=${this.merchantId}&otherParty=${conv.userId}`,
          method: "GET",
          success: (res) => {
            var _a;
            if (res.data && res.data.code === 200) {
              const serverUnread = ((_a = res.data.data) == null ? void 0 : _a.unreadCount) || 0;
              conv.unreadCount = serverUnread;
              const key = `chat_${this.merchantId}_${conv.userId}`;
              const saved = common_vendor.index.getStorageSync(key);
              if (saved) {
                saved.unreadCount = serverUnread;
                common_vendor.index.setStorageSync(key, saved);
              }
              this.calcTotalUnread();
            }
          },
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/merchant/messages.vue:341", "获取未读数失败", err);
          }
        });
      });
    },
    // 从本地存储加载会话
    loadFromLocalStorage() {
      const savedMessages = common_vendor.index.getStorageSync("merchant_messages") || {};
      const conversations = [];
      Object.keys(savedMessages).forEach((key) => {
        const data = savedMessages[key];
        if (data && data.name) {
          const rawMessage = data.lastMessage || "您好";
          const displayMessage = rawMessage.replace(/\n/g, " ").replace(/\s+/g, " ");
          conversations.push({
            userId: key,
            userName: data.name,
            hotelName: data.hotelName || "酒店",
            lastMessage: displayMessage,
            lastTime: data.lastTime || this.formatTime(/* @__PURE__ */ new Date()),
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
      common_vendor.index.__f__("log", "at pages/merchant/messages.vue:375", "从本地存储加载的会话数量:", this.conversations.length);
      this.calcTotalUnread();
    },
    // 计算总未读数
    calcTotalUnread() {
      this.totalUnread = this.conversations.reduce((sum, conv) => sum + (conv.unreadCount || 0), 0);
    },
    // 标记消息已读
    markMessagesAsRead(username) {
      if (!username) {
        common_vendor.index.__f__("error", "at pages/merchant/messages.vue:387", "用户名不能为空");
        return;
      }
      common_vendor.index.request({
        url: `http://localhost:8080/api/messages/read?role=merchant&identifier=${this.merchantId}&otherParty=${username}`,
        method: "POST",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/messages.vue:394", "标记已读成功", res.data);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/merchant/messages.vue:397", "标记已读失败", err);
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
      if (!this.currentConv)
        return;
      const moveX = e.touches[0].clientX;
      const moveY = e.touches[0].clientY;
      const deltaX = moveX - this.startX;
      const deltaY = moveY - this.startY;
      if (Math.abs(deltaX) > Math.abs(deltaY) && deltaX < -30) {
        this.conversations.forEach((item) => {
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
      common_vendor.index.showModal({
        title: "提示",
        content: `确定要删除与 ${conv.userName} 的聊天记录吗？`,
        success: (res) => {
          if (res.confirm) {
            const index = this.conversations.findIndex((c) => c.userId === conv.userId);
            if (index !== -1) {
              this.conversations.splice(index, 1);
            }
            const key = `chat_${this.merchantId}_${conv.userId}`;
            common_vendor.index.removeStorageSync(key);
            const savedMessages = common_vendor.index.getStorageSync("merchant_messages") || {};
            delete savedMessages[conv.userId];
            common_vendor.index.setStorageSync("merchant_messages", savedMessages);
            this.calcTotalUnread();
            common_vendor.index.showToast({ title: "删除成功", icon: "success" });
          } else {
            conv.showDelete = false;
          }
        }
      });
    },
    // 一键已读
    clearAllUnread() {
      this.conversations.forEach((conv) => {
        conv.unreadCount = 0;
        const key = `chat_${this.merchantId}_${conv.userId}`;
        const saved = common_vendor.index.getStorageSync(key);
        if (saved) {
          saved.unreadCount = 0;
          common_vendor.index.setStorageSync(key, saved);
        }
      });
      this.calcTotalUnread();
      common_vendor.index.request({
        url: `http://localhost:8080/api/messages/merchant/read-all?merchantId=${this.merchantId}`,
        method: "POST",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/messages.vue:479", "全部标记已读成功", res.data);
          common_vendor.index.showToast({ title: "已全部标为已读", icon: "success" });
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/merchant/messages.vue:483", "标记全部已读失败", err);
          common_vendor.index.showToast({ title: "操作失败", icon: "none" });
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
      const saved = common_vendor.index.getStorageSync(key);
      if (saved) {
        saved.unreadCount = 0;
        common_vendor.index.setStorageSync(key, saved);
      }
      this.markMessagesAsRead(conv.userId);
      this.loadChatHistory();
      this.showChat = true;
    },
    // 加载聊天记录
    loadChatHistory() {
      if (!this.currentUser)
        return;
      common_vendor.index.request({
        url: `http://localhost:8080/api/messages/chat?username=${this.currentUser.userId}&merchantId=${this.merchantId}`,
        method: "GET",
        success: (res) => {
          if (res.data && res.data.code === 200) {
            const messages = res.data.data || [];
            if (messages.length > 0) {
              this.currentMessages = messages.map((msg) => ({
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
      const saved = common_vendor.index.getStorageSync(key);
      if (saved && saved.messages) {
        this.currentMessages = saved.messages;
      } else {
        this.currentMessages = this.currentUser.messages || [
          { role: "merchant", content: `您好！欢迎入住${this.currentUser.hotelName}！请问有什么可以帮您？` }
        ];
        this.saveMessagesToLocal();
      }
      this.scrollToBottom();
    },
    // 保存消息到本地
    saveMessagesToLocal() {
      const key = `chat_${this.merchantId}_${this.currentUser.userId}`;
      const lastMsg = this.currentMessages[this.currentMessages.length - 1];
      const rawLastMessage = (lastMsg == null ? void 0 : lastMsg.content) || "";
      const displayLastMessage = rawLastMessage ? rawLastMessage.replace(/\n/g, " ").replace(/\s+/g, " ") : "";
      common_vendor.index.setStorageSync(key, {
        messages: this.currentMessages,
        lastMessage: displayLastMessage,
        lastSender: lastMsg == null ? void 0 : lastMsg.role,
        lastTime: this.formatTime(/* @__PURE__ */ new Date()),
        unreadCount: 0
      });
      const index = this.conversations.findIndex((c) => c.userId === this.currentUser.userId);
      if (index !== -1) {
        this.conversations[index].lastMessage = displayLastMessage;
        this.conversations[index].lastSender = lastMsg == null ? void 0 : lastMsg.role;
        this.conversations[index].lastTime = this.formatTime(/* @__PURE__ */ new Date());
        this.conversations[index].unreadCount = 0;
      }
    },
    // 滚动到底部
    scrollToBottom() {
      this.$nextTick(() => {
        setTimeout(() => {
          common_vendor.index.createSelectorQuery().select("#msg-bottom").boundingClientRect().exec();
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
      if (!this.inputMessage.trim())
        return;
      const msgContent = this.inputMessage.trim();
      this.currentMessages.push({ role: "merchant", content: msgContent });
      this.inputMessage = "";
      this.scrollToBottom();
      this.saveMessagesToLocal();
      if (this.currentUser) {
        const displayMsg = msgContent.replace(/\n/g, " ").replace(/\s+/g, " ");
        this.currentUser.lastMessage = displayMsg;
        this.currentUser.lastSender = "merchant";
        this.currentUser.lastTime = this.formatTime(/* @__PURE__ */ new Date());
        this.currentUser.unreadCount = 0;
        const key = `chat_${this.merchantId}_${this.currentUser.userId}`;
        const saved = common_vendor.index.getStorageSync(key);
        if (saved) {
          saved.unreadCount = 0;
          saved.lastMessage = displayMsg;
          saved.lastSender = "merchant";
          saved.lastTime = this.formatTime(/* @__PURE__ */ new Date());
          common_vendor.index.setStorageSync(key, saved);
        }
      }
      common_vendor.index.request({
        url: "http://localhost:8080/api/messages/send",
        method: "POST",
        data: {
          merchantId: this.merchantId.toString(),
          username: this.currentUser.userId,
          orderId: this.currentUser.orderId,
          hotelId: this.currentUser.hotelId,
          content: msgContent,
          senderRole: "merchant",
          isRead: 0
        },
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/messages.vue:642", "消息发送成功", res.data);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/merchant/messages.vue:645", "消息发送失败", err);
        }
      });
    },
    // 关闭聊天
    closeChat() {
      this.showChat = false;
      this.currentUser = null;
      this.currentMessages = [];
      this.inputMessage = "";
    },
    // 格式化时间
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
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.t($data.totalUnread),
    b: common_vendor.t($data.conversations.length),
    c: common_vendor.o((...args) => $options.clearAllUnread && $options.clearAllUnread(...args)),
    d: common_vendor.f($data.conversations, (conv, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.o(($event) => $options.deleteConversation(conv), conv.userId),
        b: common_vendor.t(conv.userName.charAt(0)),
        c: conv.unreadCount > 0
      }, conv.unreadCount > 0 ? {} : {}, {
        d: common_vendor.t(conv.userName),
        e: common_vendor.t(conv.lastTime),
        f: common_vendor.t(conv.hotelName),
        g: conv.lastSender === "user"
      }, conv.lastSender === "user" ? {
        h: common_vendor.t(conv.lastMessage)
      } : {
        i: common_vendor.t(conv.lastMessage)
      }, {
        j: conv.unreadCount > 0
      }, conv.unreadCount > 0 ? {
        k: common_vendor.t(conv.unreadCount > 99 ? "99+" : conv.unreadCount)
      } : {}, {
        l: conv.showDelete ? 1 : "",
        m: common_vendor.o(($event) => $options.handleTouchStart($event, conv), conv.userId),
        n: common_vendor.o(($event) => $options.handleTouchMove($event, conv), conv.userId),
        o: common_vendor.o(($event) => $options.handleTouchEnd($event, conv), conv.userId),
        p: common_vendor.o(($event) => $options.openChat(conv), conv.userId),
        q: conv.userId
      });
    }),
    e: $data.conversations.length === 0
  }, $data.conversations.length === 0 ? {} : {}, {
    f: $data.showChat
  }, $data.showChat ? common_vendor.e({
    g: common_vendor.t($data.currentUser.userName),
    h: common_vendor.t($data.currentUser.hotelName),
    i: common_vendor.o((...args) => $options.closeChat && $options.closeChat(...args)),
    j: common_vendor.f($data.currentMessages, (msg, idx, i0) => {
      var _a;
      return common_vendor.e({
        a: msg.role === "user"
      }, msg.role === "user" ? {
        b: common_vendor.t(((_a = $data.currentUser.userName) == null ? void 0 : _a.charAt(0)) || "用"),
        c: common_vendor.t(msg.content)
      } : {
        d: common_vendor.t(msg.content)
      }, {
        e: idx
      });
    }),
    k: $data.quickReplies.length > 0
  }, $data.quickReplies.length > 0 ? {
    l: common_vendor.f($data.quickReplies, (reply, idx, i0) => {
      return {
        a: common_vendor.t(reply),
        b: idx,
        c: common_vendor.o(($event) => $options.sendQuickReply(reply), idx)
      };
    })
  } : {}, {
    m: common_vendor.o((...args) => $options.sendMessage && $options.sendMessage(...args)),
    n: $data.inputMessage,
    o: common_vendor.o(($event) => $data.inputMessage = $event.detail.value),
    p: common_vendor.o((...args) => $options.sendMessage && $options.sendMessage(...args)),
    q: common_vendor.o(() => {
    }),
    r: common_vendor.o((...args) => $options.closeChat && $options.closeChat(...args))
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-d4a6fad9"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/merchant/messages.js.map
