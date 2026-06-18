"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const _sfc_main = {
  data() {
    return {
      username: "",
      showAIChat: false,
      aiUnreadCount: 0,
      aiLastMessage: "AI智能助手即将上线，敬请期待！",
      aiLastMessageTime: this.formatTime(/* @__PURE__ */ new Date()),
      topNotices: [],
      merchantMessages: [],
      showMerchantChat: false,
      currentMerchant: null,
      currentMessages: [],
      merchantInput: "",
      quickReplies: ["好的，谢谢", "请问有优惠吗？", "可以改期吗？", "退订政策是什么？", "酒店地址在哪？", "有停车场吗？"],
      hotelOrders: [],
      flightOrders: [],
      aiMessages: [],
      // 对话历史 [{role, content}]
      aiInput: "",
      // 输入框绑定的内容
      aiLoading: false
      // 是否等待 AI 回复
    };
  },
  onLoad() {
    this.username = common_vendor.index.getStorageSync("loginUsername") || "";
    common_vendor.index.__f__("log", "at pages/message/message.vue:229", "=== onLoad === 用户名:", this.username);
    this.loadNotices();
    this.loadHotelOrders();
    this.loadFlightOrders();
  },
  onShow() {
    this.username = common_vendor.index.getStorageSync("loginUsername") || "";
    common_vendor.index.__f__("log", "at pages/message/message.vue:237", "=== onShow === 用户名:", this.username);
    this.loadNotices();
    this.loadHotelOrders();
    this.loadFlightOrders();
    this.countTotalUnread();
  },
  methods: {
    loadNotices() {
      const readNotices = common_vendor.index.getStorageSync("read_notices") || {};
      this.topNotices = [
        {
          id: 1,
          title: "🏨 酒店预订成功",
          emoji: "🏨",
          preview: "您预订的酒店已确认，请按时入住！如有问题可联系商家。",
          time: this.formatTime(/* @__PURE__ */ new Date()),
          read: readNotices[1] || false
        },
        {
          id: 2,
          title: "✈️ 机票订购成功",
          emoji: "✈️",
          preview: "您的机票已出票成功，请提前2小时到达机场！",
          time: this.formatTime(/* @__PURE__ */ new Date()),
          read: readNotices[2] || false
        }
      ];
    },
    markNoticeRead(noticeId) {
      const readNotices = common_vendor.index.getStorageSync("read_notices") || {};
      readNotices[noticeId] = true;
      common_vendor.index.setStorageSync("read_notices", readNotices);
    },
    loadHotelOrders() {
      if (!this.username) {
        common_vendor.index.__f__("log", "at pages/message/message.vue:275", "用户名不存在，无法加载订单");
        this.generateMerchantMessages();
        return;
      }
      common_vendor.index.__f__("log", "at pages/message/message.vue:280", "开始加载酒店订单，用户名：", this.username);
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotel-orders/user?username=" + this.username,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/message/message.vue:286", "酒店订单API返回数据：", res.data);
          let orders = [];
          if (res.data && res.data.code === 200) {
            orders = res.data.data || [];
            common_vendor.index.__f__("log", "at pages/message/message.vue:291", "从data.data获取订单，数量：", orders.length);
          } else if (Array.isArray(res.data)) {
            orders = res.data;
            common_vendor.index.__f__("log", "at pages/message/message.vue:294", "从数组获取订单，数量：", orders.length);
          } else {
            common_vendor.index.__f__("log", "at pages/message/message.vue:296", "无法解析订单数据");
            orders = [];
          }
          common_vendor.index.__f__("log", "at pages/message/message.vue:300", "订单详情：");
          orders.forEach((order, idx) => {
            common_vendor.index.__f__("log", "at pages/message/message.vue:302", `订单${idx + 1}: ID=${order.id}, 状态="${order.status}", 酒店=${order.name || order.hotelName}, 客户=${order.username}`);
          });
          this.hotelOrders = orders;
          this.generateMerchantMessages();
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/message/message.vue:309", "获取酒店订单失败：", err);
          this.hotelOrders = [];
          this.generateMerchantMessages();
        }
      });
    },
    loadFlightOrders() {
      if (!this.username)
        return;
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
          this.generateMerchantMessages();
        },
        fail: () => {
        }
      });
    },
    generateMerchantMessages() {
      common_vendor.index.__f__("log", "at pages/message/message.vue:337", "=== generateMerchantMessages 被调用 ===");
      common_vendor.index.__f__("log", "at pages/message/message.vue:338", "hotelOrders数量：", this.hotelOrders.length);
      if (!this.hotelOrders || this.hotelOrders.length === 0) {
        common_vendor.index.__f__("log", "at pages/message/message.vue:341", "没有订单数据，商家消息列表为空");
        this.merchantMessages = [];
        return;
      }
      common_vendor.index.getStorageSync("merchant_messages") || {};
      const merchants = {};
      this.hotelOrders.forEach((order) => {
        common_vendor.index.__f__("log", "at pages/message/message.vue:350", `处理订单: ID=${order.id}, 状态="${order.status}"`);
        if (order.status === "待支付" || order.status === "已取消") {
          common_vendor.index.__f__("log", "at pages/message/message.vue:354", `  -> 跳过订单 ${order.id}，状态为 ${order.status}`);
          return;
        }
        common_vendor.index.__f__("log", "at pages/message/message.vue:358", `  -> 为订单 ${order.id} 生成商家会话`);
        const hotelId = order.hotelId || order.id;
        const hotelName = order.name || order.hotelName || `酒店${hotelId}`;
        common_vendor.index.__f__("log", "at pages/message/message.vue:362", `  -> 酒店名称: ${hotelName}, 客户用户名: ${order.username}`);
        if (!merchants[hotelId]) {
          merchants[hotelId] = {
            id: hotelId,
            userId: order.username,
            merchantId: order.merchantId || hotelId.toString(),
            hotelId,
            orderId: order.id,
            name: hotelName,
            avatar: "/static/hotel-avatar.png",
            lastMessage: `欢迎预订${hotelName}！入住日期：${order.checkIn || "待确认"}`,
            lastTime: this.formatTime(new Date(order.createTime || Date.now())),
            unreadCount: 1,
            messages: [
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
          common_vendor.index.__f__("log", "at pages/message/message.vue:383", `  -> 成功添加商家: ${hotelName}`);
        }
      });
      this.merchantMessages = Object.values(merchants);
      common_vendor.index.__f__("log", "at pages/message/message.vue:388", "最终生成的商家会话数量：", this.merchantMessages.length);
      if (this.merchantMessages.length > 0) {
        common_vendor.index.__f__("log", "at pages/message/message.vue:391", "商家列表：", this.merchantMessages.map((m) => m.name));
      }
    },
    saveMerchantMessages(merchantId, messages, unreadCount, lastMessage, lastTime) {
      const savedMessages = common_vendor.index.getStorageSync("merchant_messages") || {};
      const merchant = this.merchantMessages.find((m) => m.id === merchantId);
      savedMessages[merchantId] = {
        id: merchantId,
        messages,
        unreadCount,
        lastMessage,
        lastTime,
        name: (merchant == null ? void 0 : merchant.name) || "",
        avatar: (merchant == null ? void 0 : merchant.avatar) || "/static/hotel-avatar.png"
      };
      common_vendor.index.setStorageSync("merchant_messages", savedMessages);
    },
    countTotalUnread() {
      let totalUnread = this.aiUnreadCount;
      this.topNotices.forEach((notice) => {
        if (!notice.read)
          totalUnread++;
      });
      this.merchantMessages.forEach((merchant) => {
        totalUnread += merchant.unreadCount || 0;
      });
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const isTabBarPage = currentPage.route === "pages/message/message";
      if (isTabBarPage) {
        if (totalUnread > 0) {
          common_vendor.index.setTabBarBadge({ index: 2, text: totalUnread > 99 ? "99+" : totalUnread.toString() });
        } else {
          common_vendor.index.removeTabBarBadge({ index: 2 });
        }
      }
    },
    clearAllUnread() {
      this.aiUnreadCount = 0;
      this.topNotices.forEach((notice) => {
        notice.read = true;
        this.markNoticeRead(notice.id);
      });
      this.merchantMessages.forEach((merchant) => {
        merchant.unreadCount = 0;
        this.saveMerchantMessages(merchant.id, merchant.messages, 0, merchant.lastMessage, merchant.lastTime);
      });
      this.countTotalUnread();
      common_vendor.index.showToast({ title: "已全部标为已读", icon: "success" });
    },
    viewNotice(notice) {
      notice.read = true;
      this.markNoticeRead(notice.id);
      this.countTotalUnread();
      common_vendor.index.showModal({ title: notice.title, content: notice.preview, showCancel: false, confirmText: "知道了" });
    },
    goToAIChat() {
      common_vendor.index.navigateTo({
        url: "/pages/ai-chat/ai-chat"
      });
      this.aiUnreadCount = 0;
      this.countTotalUnread();
    },
    closeAIChat() {
      this.showAIChat = false;
      common_vendor.index.setStorageSync("ai_messages", this.aiMessages);
    },
    // ========== 修改后的 goToMerchantChat - 从服务器加载历史消息 ==========
    goToMerchantChat(merchant) {
      this.currentMerchant = merchant;
      this.loadChatHistoryFromServer(merchant, () => {
        this.currentMessages = [...this.currentMerchant.messages];
        this.currentMerchant.unreadCount = 0;
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
    // ========== 新增：从服务器加载聊天记录 ==========
    loadChatHistoryFromServer(merchant, callback) {
      common_vendor.index.request({
        url: `http://localhost:8080/api/messages/chat?username=${this.username}&merchantId=${merchant.merchantId || merchant.id}&hotelId=${merchant.hotelId || ""}`,
        method: "GET",
        success: (res) => {
          var _a, _b;
          if (res.data && res.data.code === 200) {
            const messages = res.data.data || [];
            if (messages.length > 0) {
              merchant.messages = messages.map((msg) => ({
                role: msg.senderRole,
                content: msg.content
              }));
              merchant.lastMessage = ((_a = messages[messages.length - 1]) == null ? void 0 : _a.content) || merchant.lastMessage;
              merchant.lastTime = this.formatTime((_b = messages[messages.length - 1]) == null ? void 0 : _b.createTime);
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
      common_vendor.index.__f__("log", "at pages/message/message.vue:534", "发送消息 - 订单中的客户用户名:", customerUsername);
      common_vendor.index.__f__("log", "at pages/message/message.vue:535", "当前登录用户名:", this.username);
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
        this.currentMerchant.lastMessage = userMsg;
        this.currentMerchant.lastTime = this.formatTime(/* @__PURE__ */ new Date());
        this.currentMerchant.messages = this.currentMessages;
        this.saveMerchantMessages(this.currentMerchant.id, this.currentMessages, 0, userMsg, this.formatTime(/* @__PURE__ */ new Date()));
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
          common_vendor.index.__f__("log", "at pages/message/message.vue:578", "消息发送成功", res.data);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/message/message.vue:581", "消息发送失败", err);
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
    onAIInput(e) {
      this.aiInput = e.detail.value;
    },
    sendAIMessage() {
      if (!this.aiInput.trim() || this.aiLoading)
        return;
      const userMsg = this.aiInput.trim();
      this.aiMessages.push({ role: "user", content: userMsg });
      this.aiInput = "";
      this.aiLoading = true;
      this.$nextTick(() => {
        common_vendor.index.createSelectorQuery().select("#ai-msg-bottom").boundingClientRect().exec();
      });
      common_vendor.index.request({
        url: "http://localhost:8080/api/ai/chat",
        // 请替换为你的实际后端地址
        method: "POST",
        timeout: 9e4,
        // 前端超时设置为 90 秒，避免过早断开
        data: {
          messages: this.aiMessages,
          // 发送完整对话历史（含系统提示词）
          username: this.username
          // 可选，用于日志
        },
        success: (res) => {
          if (res.data && res.data.reply) {
            this.aiMessages.push({ role: "assistant", content: res.data.reply });
          } else {
            this.aiMessages.push({ role: "assistant", content: "抱歉，AI 服务返回异常，请稍后再试。" });
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/message/message.vue:692", "AI请求失败", err);
          this.aiMessages.push({ role: "assistant", content: "网络开小差了，请稍后再试。" });
        },
        complete: () => {
          this.aiLoading = false;
          this.$nextTick(() => {
            common_vendor.index.createSelectorQuery().select("#ai-msg-bottom").boundingClientRect().exec();
          });
        }
      });
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
    h: common_vendor.f($data.topNotices, (notice, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(notice.emoji),
        b: common_vendor.t(notice.title),
        c: common_vendor.t(notice.time),
        d: common_vendor.t(notice.preview),
        e: !notice.read
      }, !notice.read ? {} : {}, {
        f: notice.id,
        g: common_vendor.o(($event) => $options.viewNotice(notice), notice.id)
      });
    }),
    i: common_vendor.f($data.merchantMessages, (merchant, k0, i0) => {
      return common_vendor.e({
        a: merchant.avatar,
        b: common_vendor.t(merchant.name),
        c: common_vendor.t(merchant.lastTime),
        d: common_vendor.t(merchant.lastMessage),
        e: merchant.unreadCount > 0
      }, merchant.unreadCount > 0 ? {
        f: common_vendor.t(merchant.unreadCount > 99 ? "99+" : merchant.unreadCount)
      } : {}, {
        g: merchant.id,
        h: common_vendor.o(($event) => $options.goToMerchantChat(merchant), merchant.id)
      });
    }),
    j: $data.merchantMessages.length === 0 && $data.topNotices.length === 0
  }, $data.merchantMessages.length === 0 && $data.topNotices.length === 0 ? {} : {}, {
    k: $data.showAIChat
  }, $data.showAIChat ? common_vendor.e({
    l: common_vendor.o((...args) => $options.closeAIChat && $options.closeAIChat(...args)),
    m: common_vendor.f($data.aiMessages, (msg, idx, i0) => {
      return common_vendor.e({
        a: msg.role === "assistant"
      }, msg.role === "assistant" ? {
        b: common_assets._imports_0$1,
        c: common_vendor.t(msg.content)
      } : {
        d: common_vendor.t(msg.content)
      }, {
        e: idx
      });
    }),
    n: $data.aiLoading
  }, $data.aiLoading ? {
    o: common_assets._imports_0$1
  } : {}, {
    p: common_vendor.o((...args) => $options.sendAIMessage && $options.sendAIMessage(...args)),
    q: common_vendor.o([($event) => $data.aiInput = $event.detail.value, (...args) => $options.onAIInput && $options.onAIInput(...args)]),
    r: $data.aiInput,
    s: common_vendor.o((...args) => $options.sendAIMessage && $options.sendAIMessage(...args)),
    t: $data.aiLoading,
    v: common_vendor.o(() => {
    }),
    w: common_vendor.o((...args) => $options.closeAIChat && $options.closeAIChat(...args))
  }) : {}, {
    x: $data.showMerchantChat
  }, $data.showMerchantChat ? common_vendor.e({
    y: common_vendor.t($data.currentMerchant.name),
    z: common_vendor.o((...args) => $options.closeMerchantChat && $options.closeMerchantChat(...args)),
    A: common_vendor.f($data.currentMessages, (msg, idx, i0) => {
      return common_vendor.e({
        a: msg.role === "merchant"
      }, msg.role === "merchant" ? {
        b: $data.currentMerchant.avatar,
        c: common_vendor.t(msg.content)
      } : {
        d: common_vendor.t(msg.content)
      }, {
        e: idx
      });
    }),
    B: $data.quickReplies.length > 0
  }, $data.quickReplies.length > 0 ? {
    C: common_vendor.f($data.quickReplies, (reply, idx, i0) => {
      return {
        a: common_vendor.t(reply),
        b: idx,
        c: common_vendor.o(($event) => $options.sendQuickReply(reply), idx)
      };
    })
  } : {}, {
    D: common_vendor.o((...args) => $options.sendMerchantMessage && $options.sendMerchantMessage(...args)),
    E: $data.merchantInput,
    F: common_vendor.o(($event) => $data.merchantInput = $event.detail.value),
    G: common_vendor.o((...args) => $options.sendMerchantMessage && $options.sendMerchantMessage(...args)),
    H: common_vendor.o(() => {
    }),
    I: common_vendor.o((...args) => $options.closeMerchantChat && $options.closeMerchantChat(...args))
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-4c1b26cf"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/message/message.js.map
