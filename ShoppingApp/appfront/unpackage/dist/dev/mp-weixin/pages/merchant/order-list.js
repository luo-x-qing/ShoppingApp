"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      activeTab: "all",
      orderList: [],
      merchantId: null,
      // 拒绝弹窗相关
      showRejectDialogFlag: false,
      rejectOrderId: null,
      rejectReason: "",
      // 入住时间校验相关
      showDateWarning: false,
      warningMessage: "",
      // 商家名称
      merchantName: ""
    };
  },
  computed: {
    tabList() {
      return [
        { name: "全部", value: "all" },
        { name: "待支付", value: "pending" },
        { name: "待确认", value: "waitConfirm" },
        { name: "已支付", value: "paid" },
        { name: "已确认", value: "confirmed" },
        { name: "已入住", value: "checkin" },
        { name: "已完成", value: "completed" },
        { name: "取消申请", value: "cancelling" },
        { name: "已取消", value: "cancelled" }
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
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        if (userInfo && userInfo.id) {
          this.merchantId = userInfo.id;
          this.merchantName = userInfo.shopName || userInfo.name || "商家";
          this.loadOrdersByMerchant();
        } else {
          common_vendor.index.showToast({ title: "请先登录", icon: "none" });
          setTimeout(() => {
            common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
          }, 1500);
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/merchant/order-list.vue:212", "读取商家信息失败", e);
      }
    },
    // 直接通过商家ID加载订单
    loadOrdersByMerchant() {
      if (!this.merchantId)
        return;
      common_vendor.index.showLoading({ title: "加载中..." });
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotel-orders/merchant/orders`,
        method: "GET",
        data: {
          merchantId: this.merchantId
        },
        success: (res) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("log", "at pages/merchant/order-list.vue:230", "订单列表返回：", res.data);
          if (res.data && res.data.code === 200) {
            let orders = res.data.data || [];
            this.checkAndCancelExpiredOrders(orders);
            this.processOrders(orders);
          } else {
            this.loadAllOrders();
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/merchant/order-list.vue:241", "获取订单失败", err);
          this.loadAllOrders();
        }
      });
    },
    // 降级方案：获取所有订单
    loadAllOrders() {
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotel-orders`,
        method: "GET",
        success: (res) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("log", "at pages/merchant/order-list.vue:254", "获取所有订单：", res.data);
          if (res.data && res.data.code === 200) {
            let orders = res.data.data || [];
            this.checkAndCancelExpiredOrders(orders);
            this.processOrders(orders);
          } else {
            this.orderList = [];
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/merchant/order-list.vue:266", "获取订单失败", err);
          common_vendor.index.showToast({ title: "获取订单失败", icon: "none" });
          this.orderList = [];
        }
      });
    },
    // 检查并自动取消过期订单
    checkAndCancelExpiredOrders(orders) {
      if (!orders || orders.length === 0)
        return;
      const today = /* @__PURE__ */ new Date();
      today.setHours(0, 0, 0, 0);
      const autoCancelStatuses = ["待确认", "已支付"];
      const expiredOrders = orders.filter((order) => {
        if (!autoCancelStatuses.includes(order.status))
          return false;
        if (!order.checkIn)
          return false;
        const checkIn = new Date(order.checkIn);
        checkIn.setHours(0, 0, 0, 0);
        return checkIn <= today;
      });
      if (expiredOrders.length === 0)
        return;
      common_vendor.index.__f__("log", "at pages/merchant/order-list.vue:294", `发现 ${expiredOrders.length} 个订单已过入住日期未确认，自动取消`);
      expiredOrders.forEach((order) => {
        this.cancelExpiredOrder(order);
      });
    },
    cancelExpiredOrder(order) {
      common_vendor.index.__f__("log", "at pages/merchant/order-list.vue:302", `自动取消订单 ${order.id}，入住日期：${order.checkIn}，状态：${order.status}`);
      const cancelReason = `商家未在入住日期(${order.checkIn})前确认订单，系统自动取消`;
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotel-orders/${order.id}/auto-cancel`,
        method: "POST",
        data: {
          reason: cancelReason
        },
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/order-list.vue:313", `订单 ${order.id} 自动取消结果`, res.data);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/merchant/order-list.vue:316", `订单 ${order.id} 自动取消失败`, err);
          this.updateOrderStatus(order.id, "已取消", cancelReason);
        }
      });
    },
    updateOrderStatus(orderId, status, reason) {
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotel-orders/${orderId}`,
        method: "PUT",
        data: {
          status,
          cancelReason: reason || "订单过期自动取消"
        },
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/order-list.vue:331", `订单 ${orderId} 状态已更新为 ${status}`, res.data);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/merchant/order-list.vue:334", `更新订单 ${orderId} 状态失败`, err);
        }
      });
    },
    // 处理订单数据
    processOrders(orders) {
      common_vendor.index.__f__("log", "at pages/merchant/order-list.vue:341", "原始订单数量：", orders.length);
      orders.forEach((order) => {
        common_vendor.index.__f__("log", "at pages/merchant/order-list.vue:344", `订单ID: ${order.id}, 酒店ID: ${order.hotelId}, 状态: "${order.status}"`);
      });
      if (this.activeTab !== "all") {
        orders = orders.filter((order) => this.filterByStatus(order.status));
        common_vendor.index.__f__("log", "at pages/merchant/order-list.vue:349", "筛选后订单数量：", orders.length);
      }
      orders.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
      this.orderList = orders;
    },
    // 状态筛选映射
    filterByStatus(status) {
      const statusMap = {
        "pending": "待支付",
        "waitConfirm": "待确认",
        "paid": "已支付",
        "confirmed": "已确认",
        "checkin": "已入住",
        "completed": "已完成",
        "cancelling": "取消申请中",
        "cancelled": "已取消"
      };
      return statusMap[this.activeTab] === status;
    },
    // 切换标签
    switchTab(status) {
      this.activeTab = status;
      this.loadOrdersByMerchant();
    },
    // ========== 入住时间校验 ==========
    canCheckIn(checkInDate) {
      if (!checkInDate)
        return false;
      const today = /* @__PURE__ */ new Date();
      today.setHours(0, 0, 0, 0);
      const checkIn = new Date(checkInDate);
      checkIn.setHours(0, 0, 0, 0);
      return checkIn <= today;
    },
    getCheckInFailReason(checkInDate) {
      if (!checkInDate)
        return "订单缺少入住日期信息";
      const today = /* @__PURE__ */ new Date();
      today.setHours(0, 0, 0, 0);
      const checkIn = new Date(checkInDate);
      checkIn.setHours(0, 0, 0, 0);
      const diffDays = Math.ceil((checkIn - today) / (1e3 * 60 * 60 * 24));
      if (checkIn > today) {
        return `订单入住时间为 ${checkInDate}，距离入住还有 ${diffDays} 天，请于 ${checkInDate} 当天或之后办理入住`;
      }
      return "未知原因";
    },
    handleCheckIn(order) {
      if (!this.canCheckIn(order.checkIn)) {
        this.warningMessage = this.getCheckInFailReason(order.checkIn);
        this.showDateWarning = true;
        return;
      }
      common_vendor.index.showModal({
        title: "确认入住",
        content: `确认订单 ${order.id} 的客人已入住吗？`,
        success: (res) => {
          if (res.confirm) {
            this.confirmCheckIn(order);
          }
        }
      });
    },
    // 确认订单
    confirmOrder(order) {
      common_vendor.index.showModal({
        title: "确认订单",
        content: `确认订单 ${order.id} 吗？确认后订单状态将变为"已确认"，并自动发送欢迎消息给客户。`,
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.showLoading({ title: "处理中..." });
            common_vendor.index.request({
              url: `http://localhost:8080/api/hotel-orders/${order.id}/confirm`,
              method: "POST",
              success: (res2) => {
                common_vendor.index.hideLoading();
                if (res2.data && res2.data.code === 200) {
                  common_vendor.index.showToast({ title: "确认成功", icon: "success" });
                  this.sendConfirmedMessage(order);
                  this.loadOrdersByMerchant();
                } else {
                  common_vendor.index.showToast({ title: res2.data.message || "确认失败", icon: "none" });
                }
              },
              fail: (err) => {
                common_vendor.index.hideLoading();
                common_vendor.index.__f__("error", "at pages/merchant/order-list.vue:452", "确认订单失败", err);
                common_vendor.index.showToast({ title: "网络错误", icon: "none" });
              }
            });
          }
        }
      });
    },
    sendConfirmedMessage(order) {
      const hotelId = order.hotelId || order.id;
      const hotelName = order.name || order.hotelName || `酒店${hotelId}`;
      const merchantId = this.merchantId;
      const customerUsername = order.username;
      const orderId = order.id;
      if (!customerUsername) {
        common_vendor.index.__f__("error", "at pages/merchant/order-list.vue:469", "缺少客户用户名，无法发送确认消息");
        return;
      }
      const confirmedMessage = `🎉 欢迎预订${hotelName}！

📅 入住日期：${order.checkIn || "待确认"}
📅 退房日期：${order.checkOut || "待确认"}
💰 订单金额：¥${order.price || 0}

如有任何问题，请随时联系我们，欢迎您的入住~`;
      common_vendor.index.__f__("log", "at pages/merchant/order-list.vue:475", `发送确认消息，订单ID: ${orderId}, 用户: ${customerUsername}`);
      this.saveConfirmedMessageToLocal(order, hotelId, hotelName, confirmedMessage);
      common_vendor.index.request({
        url: "http://localhost:8080/api/messages/send",
        method: "POST",
        data: {
          orderId,
          hotelId,
          merchantId: merchantId.toString(),
          username: customerUsername,
          content: confirmedMessage,
          senderRole: "merchant",
          isRead: 0
        },
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/order-list.vue:492", "确认消息发送成功", res.data);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/merchant/order-list.vue:495", "确认消息发送失败：", err);
        }
      });
    },
    saveConfirmedMessageToLocal(order, hotelId, hotelName, message) {
      const userId = order.username;
      if (!userId)
        return;
      const key = `chat_${this.merchantId}_${userId}`;
      const saved = common_vendor.index.getStorageSync(key) || {};
      const messages = saved.messages || [];
      const hasSameMessage = messages.some((msg) => msg.content === message);
      if (!hasSameMessage) {
        messages.push({ role: "merchant", content: message });
      }
      const data = {
        messages,
        lastMessage: message,
        lastSender: "merchant",
        lastTime: this.formatTime(/* @__PURE__ */ new Date()),
        unreadCount: (saved.unreadCount || 0) + 1,
        name: userId,
        hotelName
      };
      common_vendor.index.setStorageSync(key, data);
      const merchantMessages = common_vendor.index.getStorageSync("merchant_messages") || {};
      merchantMessages[userId] = {
        name: userId,
        hotelName,
        messages,
        lastMessage: message,
        lastTime: this.formatTime(/* @__PURE__ */ new Date()),
        unreadCount: (saved.unreadCount || 0) + 1
      };
      common_vendor.index.setStorageSync("merchant_messages", merchantMessages);
    },
    // 确认入住
    confirmCheckIn(order) {
      common_vendor.index.showLoading({ title: "处理中..." });
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotel-orders/${order.id}/checkin`,
        method: "POST",
        success: (res) => {
          common_vendor.index.hideLoading();
          if (res.data && res.data.code === 200) {
            common_vendor.index.showToast({ title: "确认入住成功", icon: "success" });
            this.loadOrdersByMerchant();
          } else {
            common_vendor.index.showToast({ title: res.data.message || "操作失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/merchant/order-list.vue:555", "确认入住失败", err);
          common_vendor.index.showToast({ title: "网络错误", icon: "none" });
        }
      });
    },
    // 确认退房
    confirmCheckOut(order) {
      common_vendor.index.showModal({
        title: "确认退房",
        content: `确认订单 ${order.id} 的客人已退房吗？确认后订单将标记为"已完成"。`,
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.showLoading({ title: "处理中..." });
            common_vendor.index.request({
              url: `http://localhost:8080/api/hotel-orders/${order.id}/checkout`,
              method: "POST",
              success: (res2) => {
                common_vendor.index.hideLoading();
                if (res2.data && res2.data.code === 200) {
                  common_vendor.index.showToast({ title: "退房成功，订单已完成", icon: "success" });
                  this.loadOrdersByMerchant();
                } else {
                  common_vendor.index.showToast({ title: res2.data.message || "操作失败", icon: "none" });
                }
              },
              fail: (err) => {
                common_vendor.index.hideLoading();
                common_vendor.index.__f__("error", "at pages/merchant/order-list.vue:584", "确认退房失败", err);
                common_vendor.index.showToast({ title: "网络错误", icon: "none" });
              }
            });
          }
        }
      });
    },
    // 同意取消
    approveCancel(order) {
      common_vendor.index.showModal({
        title: "同意取消",
        content: `确定同意订单 ${order.id} 的取消申请吗？取消后订单将失效。`,
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.showLoading({ title: "处理中..." });
            common_vendor.index.request({
              url: `http://localhost:8080/api/hotel-orders/${order.id}/cancel-approve`,
              method: "POST",
              success: (res2) => {
                common_vendor.index.hideLoading();
                if (res2.data && res2.data.code === 200) {
                  common_vendor.index.showToast({ title: "已同意取消", icon: "success" });
                  this.loadOrdersByMerchant();
                } else {
                  common_vendor.index.showToast({ title: res2.data.message || "操作失败", icon: "none" });
                }
              },
              fail: (err) => {
                common_vendor.index.hideLoading();
                common_vendor.index.__f__("error", "at pages/merchant/order-list.vue:616", "同意取消失败", err);
                common_vendor.index.showToast({ title: "网络错误", icon: "none" });
              }
            });
          }
        }
      });
    },
    // 显示拒绝弹窗
    showRejectDialog(order) {
      this.rejectOrderId = order.id;
      this.rejectReason = "";
      this.showRejectDialogFlag = true;
    },
    // 关闭拒绝弹窗
    closeRejectDialog() {
      this.showRejectDialogFlag = false;
      this.rejectOrderId = null;
      this.rejectReason = "";
    },
    // 拒绝取消
    rejectCancel() {
      if (!this.rejectReason.trim()) {
        common_vendor.index.showToast({ title: "请输入拒绝原因", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "处理中..." });
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotel-orders/${this.rejectOrderId}/cancel-reject`,
        method: "POST",
        data: {
          reason: this.rejectReason
        },
        success: (res) => {
          common_vendor.index.hideLoading();
          if (res.data && res.data.code === 200) {
            common_vendor.index.showToast({ title: "已拒绝取消", icon: "success" });
            this.closeRejectDialog();
            this.loadOrdersByMerchant();
          } else {
            common_vendor.index.showToast({ title: res.data.message || "操作失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/merchant/order-list.vue:666", "拒绝取消失败", err);
          common_vendor.index.showToast({ title: "网络错误", icon: "none" });
        }
      });
    },
    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        "待支付": "待支付",
        "待确认": "待确认",
        "已支付": "已支付",
        "已确认": "已确认",
        "已入住": "已入住",
        "已完成": "已完成",
        "已取消": "已取消",
        "取消申请中": "申请取消中",
        "取消拒绝": "取消申请被拒绝"
      };
      return statusMap[status] || status;
    },
    // 获取状态样式
    getStatusClass(status) {
      const classMap = {
        "待支付": "status-pending",
        "待确认": "status-wait-confirm",
        "已支付": "status-paid",
        "已确认": "status-confirmed",
        "已入住": "status-checkin",
        "已完成": "status-completed",
        "已取消": "status-cancelled",
        "取消申请中": "status-cancelling",
        "取消拒绝": "status-rejected"
      };
      return classMap[status] || "status-default";
    },
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr)
        return "--";
      try {
        const date = new Date(dateStr);
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        const hour = String(date.getHours()).padStart(2, "0");
        const minute = String(date.getMinutes()).padStart(2, "0");
        return `${month}/${day} ${hour}:${minute}`;
      } catch (e) {
        return dateStr;
      }
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
    a: common_vendor.f($options.tabList, (status, k0, i0) => {
      return {
        a: common_vendor.t(status.name),
        b: common_vendor.n($data.activeTab === status.value ? "active" : ""),
        c: status.value,
        d: common_vendor.o(($event) => $options.switchTab(status.value), status.value)
      };
    }),
    b: $data.orderList.length > 0
  }, $data.orderList.length > 0 ? {
    c: common_vendor.f($data.orderList, (order, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(order.id),
        b: common_vendor.t($options.formatDate(order.createTime)),
        c: common_vendor.t($options.getStatusText(order.status)),
        d: common_vendor.n($options.getStatusClass(order.status)),
        e: common_vendor.t(order.hotelName),
        f: common_vendor.t(order.roomTypeName),
        g: common_vendor.t(order.checkIn),
        h: common_vendor.t(order.checkOut),
        i: common_vendor.t(order.contactName || order.username),
        j: common_vendor.t(order.contactPhone || "--"),
        k: common_vendor.t(order.price),
        l: order.status === "取消申请中"
      }, order.status === "取消申请中" ? {
        m: common_vendor.t(order.cancelReason || "用户申请取消")
      } : {}, {
        n: order.status === "取消拒绝"
      }, order.status === "取消拒绝" ? {
        o: common_vendor.t(order.rejectReason || "商家拒绝了取消申请")
      } : {}, {
        p: order.status !== "已取消" && order.status !== "取消拒绝" && order.status !== "已完成"
      }, order.status !== "已取消" && order.status !== "取消拒绝" && order.status !== "已完成" ? common_vendor.e({
        q: order.status === "待支付"
      }, order.status === "待支付" ? {} : {}, {
        r: order.status === "待确认"
      }, order.status === "待确认" ? {
        s: common_vendor.o(($event) => $options.confirmOrder(order), order.id)
      } : {}, {
        t: order.status === "已支付"
      }, order.status === "已支付" ? {
        v: common_vendor.o(($event) => $options.confirmOrder(order), order.id),
        w: common_vendor.o(($event) => $options.handleCheckIn(order), order.id)
      } : {}, {
        x: order.status === "已确认"
      }, order.status === "已确认" ? {
        y: common_vendor.o(($event) => $options.handleCheckIn(order), order.id)
      } : {}, {
        z: order.status === "已入住"
      }, order.status === "已入住" ? {
        A: common_vendor.o(($event) => $options.confirmCheckOut(order), order.id)
      } : {}, {
        B: order.status === "取消申请中"
      }, order.status === "取消申请中" ? {
        C: common_vendor.o(($event) => $options.showRejectDialog(order), order.id),
        D: common_vendor.o(($event) => $options.approveCancel(order), order.id)
      } : {}) : {}, {
        E: order.status === "已取消"
      }, order.status === "已取消" ? {} : {}, {
        F: order.status === "取消拒绝"
      }, order.status === "取消拒绝" ? {} : {}, {
        G: order.status === "已完成"
      }, order.status === "已完成" ? {} : {}, {
        H: order.id
      });
    })
  } : {}, {
    d: $data.showRejectDialogFlag
  }, $data.showRejectDialogFlag ? {
    e: common_vendor.o((...args) => $options.closeRejectDialog && $options.closeRejectDialog(...args)),
    f: $data.rejectReason,
    g: common_vendor.o(($event) => $data.rejectReason = $event.detail.value),
    h: common_vendor.o((...args) => $options.rejectCancel && $options.rejectCancel(...args)),
    i: common_vendor.o(() => {
    }),
    j: common_vendor.o((...args) => $options.closeRejectDialog && $options.closeRejectDialog(...args))
  } : {}, {
    k: $data.showDateWarning
  }, $data.showDateWarning ? {
    l: common_vendor.t($data.warningMessage),
    m: common_vendor.o(($event) => $data.showDateWarning = false),
    n: common_vendor.o(() => {
    }),
    o: common_vendor.o(($event) => $data.showDateWarning = false)
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-3b6d764b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/merchant/order-list.js.map
