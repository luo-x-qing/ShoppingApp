"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      activeTab: 1,
      orderList: [],
      flightOrders: [],
      username: "",
      // 状态筛选
      hotelStatusFilter: "all",
      flightStatusFilter: "all",
      // 支付弹窗相关
      showPayDialogFlag: false,
      payingOrderId: null,
      payingAmount: 0,
      payPassword: "",
      payingType: "",
      // 取消申请弹窗相关
      showCancelRequestFlag: false,
      cancelOrderItem: null,
      cancelReason: ""
    };
  },
  computed: {
    isPasswordValid() {
      return this.payPassword && this.payPassword.length === 6 && /^\d{6}$/.test(this.payPassword);
    },
    // 筛选后的酒店订单
    filteredOrderList() {
      if (this.hotelStatusFilter === "all") {
        return this.orderList;
      }
      return this.orderList.filter((item) => item.status === this.hotelStatusFilter);
    },
    // 筛选后的机票订单
    filteredFlightOrders() {
      if (this.flightStatusFilter === "all") {
        return this.flightOrders;
      }
      return this.flightOrders.filter((item) => item.status === this.flightStatusFilter);
    }
  },
  onShow() {
    this.username = common_vendor.index.getStorageSync("loginUsername") || "";
    const userInfo = common_vendor.index.getStorageSync("userInfo");
    if (userInfo && userInfo.username) {
      this.username = userInfo.username;
    }
    common_vendor.index.__f__("log", "at pages/my-orders/my-orders.vue:405", "当前登录用户：", this.username);
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
      const cannotCancelStatuses = ["已入住", "已完成", "已取消", "取消申请中", "取消拒绝"];
      if (cannotCancelStatuses.includes(item.status)) {
        return false;
      }
      if (item.status === "待支付") {
        return false;
      }
      return true;
    },
    switchTab(type) {
      this.activeTab = type;
      if (type === 1) {
        this.hotelStatusFilter = "all";
      } else {
        this.flightStatusFilter = "all";
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
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotel-orders/user?username=" + this.username,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/my-orders/my-orders.vue:457", "酒店订单返回数据：", res.data);
          let orders = [];
          if (res.data && res.data.code === 200) {
            orders = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            orders = res.data;
          } else {
            orders = [];
          }
          this.orderList = orders.map((item) => {
            item.content = "";
            item.score = 5;
            item.commented = false;
            if (!item.status)
              item.status = "待支付";
            return item;
          });
          this.checkAllCommentedOrders();
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/my-orders/my-orders.vue:478", "获取酒店订单失败：", err);
          this.orderList = [];
        }
      });
    },
    getHotelStatusText(status) {
      const statusMap = {
        "待支付": "待支付",
        "待确认": "待确认",
        "已支付": "已支付",
        "已确认": "已确认",
        "已入住": "已入住",
        "已完成": "已完成",
        "已取消": "已取消",
        "取消申请中": "取消申请中",
        "取消拒绝": "取消申请被拒绝"
      };
      return statusMap[status] || status || "待支付";
    },
    getHotelStatusClass(status) {
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
    cancelHotelOrder(item) {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要取消这个订单吗？取消后无法恢复。",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.showLoading({ title: "取消中..." });
            common_vendor.index.request({
              url: `http://localhost:8080/api/hotel-orders/${item.id}/cancel`,
              method: "POST",
              success: (res2) => {
                common_vendor.index.hideLoading();
                common_vendor.index.__f__("log", "at pages/my-orders/my-orders.vue:527", "取消酒店订单结果：", res2.data);
                if (res2.data && res2.data.code === 200) {
                  common_vendor.index.showToast({ title: "订单已取消", icon: "success" });
                  this.getHotelOrders();
                } else {
                  common_vendor.index.showToast({ title: res2.data.message || "取消失败", icon: "none" });
                }
              },
              fail: (err) => {
                common_vendor.index.hideLoading();
                common_vendor.index.__f__("error", "at pages/my-orders/my-orders.vue:538", "取消订单失败：", err);
                common_vendor.index.showToast({ title: "网络错误", icon: "none" });
              }
            });
          }
        }
      });
    },
    showHotelPayDialog(item) {
      if (item.status !== "待支付") {
        common_vendor.index.showToast({ title: "订单状态不正确，无法支付", icon: "none" });
        return;
      }
      this.payingType = "hotel";
      this.payingOrderId = item.id;
      this.payingAmount = item.price;
      this.payPassword = "";
      this.showPayDialogFlag = true;
    },
    showCancelRequestDialog(item) {
      this.cancelOrderItem = item;
      this.cancelReason = "";
      this.showCancelRequestFlag = true;
    },
    closeCancelRequestDialog() {
      this.showCancelRequestFlag = false;
      this.cancelOrderItem = null;
      this.cancelReason = "";
    },
    submitCancelRequest() {
      if (!this.cancelOrderItem)
        return;
      if (!this.username) {
        common_vendor.index.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "提交中..." });
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotel-orders/${this.cancelOrderItem.id}/cancel-request`,
        method: "POST",
        header: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        data: {
          username: this.username,
          reason: this.cancelReason || "用户申请取消"
        },
        success: (res) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("log", "at pages/my-orders/my-orders.vue:594", "取消申请结果：", res.data);
          if (res.data && res.data.code === 200) {
            common_vendor.index.showToast({ title: "取消申请已提交，等待商家审核", icon: "success" });
            this.closeCancelRequestDialog();
            this.getHotelOrders();
          } else {
            common_vendor.index.showToast({ title: res.data.message || "提交失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/my-orders/my-orders.vue:606", "提交取消申请失败：", err);
          common_vendor.index.showToast({ title: "网络错误，请稍后重试", icon: "none" });
        }
      });
    },
    closeRejectTip(item) {
      this.getHotelOrders();
    },
    checkAllCommentedOrders() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotel-comments/all",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/my-orders/my-orders.vue:620", "评价数据：", res.data);
          let comments = [];
          if (res.data && res.data.code === 200) {
            comments = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            comments = res.data;
          } else {
            comments = [];
          }
          const commentedOrderIds = comments.map((c) => c.orderId);
          this.orderList.forEach((item) => {
            if (commentedOrderIds.includes(item.id)) {
              item.commented = true;
            }
          });
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/my-orders/my-orders.vue:639", "获取评价失败：", err);
        }
      });
    },
    submitComment(item) {
      if (!item.content) {
        common_vendor.index.showToast({ title: "请输入评价内容", icon: "none" });
        return;
      }
      if (item.commented) {
        common_vendor.index.showToast({ title: "已评价", icon: "none" });
        return;
      }
      if (!this.username) {
        common_vendor.index.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "提交中..." });
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotel-comments",
        method: "POST",
        header: {
          "Content-Type": "application/json"
        },
        data: {
          hotelId: item.hotelId,
          orderId: item.id,
          content: item.content,
          score: item.score,
          username: this.username
        },
        success: (res) => {
          var _a;
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("log", "at pages/my-orders/my-orders.vue:676", "提交评价结果：", res.data);
          if (res.data && res.data.code === 200) {
            common_vendor.index.showToast({ title: "评价成功", icon: "success" });
            item.commented = true;
          } else if (res.data === "已评价" || res.data && res.data.code === 500 && res.data.message === "已评价") {
            common_vendor.index.showToast({ title: "该订单已评价", icon: "none" });
            item.commented = true;
          } else {
            common_vendor.index.showToast({ title: ((_a = res.data) == null ? void 0 : _a.message) || "评价失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/my-orders/my-orders.vue:690", "提交评价失败：", err);
          common_vendor.index.showToast({ title: "评价失败，请重试", icon: "none" });
        }
      });
    },
    // ========== 机票订单相关 ==========
    getFlightOrders() {
      if (!this.username || this.username === "") {
        this.flightOrders = [];
        return;
      }
      common_vendor.index.request({
        url: "http://localhost:8080/api/flight-orders?username=" + this.username,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/my-orders/my-orders.vue:707", "后端返回的机票订单：", res.data);
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
          common_vendor.index.__f__("error", "at pages/my-orders/my-orders.vue:721", "获取机票订单失败：", err);
          this.flightOrders = [];
        }
      });
    },
    cancelFlightOrder(orderId) {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要取消这个订单吗？",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.showLoading({ title: "取消中..." });
            common_vendor.index.request({
              url: `http://localhost:8080/api/flight-orders/${orderId}/cancel`,
              method: "PUT",
              success: (res2) => {
                var _a;
                common_vendor.index.hideLoading();
                if (res2.data && res2.data.code === 200) {
                  common_vendor.index.showToast({ title: "订单已取消", icon: "success" });
                  this.getFlightOrders();
                } else {
                  common_vendor.index.showToast({ title: ((_a = res2.data) == null ? void 0 : _a.message) || "取消失败", icon: "none" });
                }
              },
              fail: (err) => {
                common_vendor.index.hideLoading();
                common_vendor.index.__f__("error", "at pages/my-orders/my-orders.vue:750", "取消订单失败：", err);
                common_vendor.index.showToast({ title: "网络错误", icon: "none" });
              }
            });
          }
        }
      });
    },
    showFlightPayDialog(orderId) {
      const order = this.flightOrders.find((item) => item.id === orderId);
      if (order) {
        if (order.status !== "待支付") {
          common_vendor.index.showToast({ title: "订单状态不正确，无法支付", icon: "none" });
          return;
        }
        this.payingType = "flight";
        this.payingOrderId = orderId;
        this.payingAmount = order.price;
        this.payPassword = "";
        this.showPayDialogFlag = true;
      } else {
        common_vendor.index.showToast({ title: "订单不存在", icon: "none" });
      }
    },
    closePayDialog() {
      this.showPayDialogFlag = false;
      this.payPassword = "";
      this.payingOrderId = null;
      this.payingType = "";
    },
    onPasswordInput(e) {
      let value = e.detail.value.replace(/\D/g, "");
      if (value.length > 6) {
        value = value.slice(0, 6);
      }
      this.payPassword = value;
    },
    confirmPay() {
      if (!this.isPasswordValid) {
        common_vendor.index.showToast({ title: "请输入6位数字支付密码", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "支付中..." });
      let url = "";
      let method = "";
      if (this.payingType === "hotel") {
        url = `http://localhost:8080/api/hotel-orders/${this.payingOrderId}/pay`;
        method = "POST";
      } else {
        url = `http://localhost:8080/api/flight-orders/${this.payingOrderId}/pay`;
        method = "PUT";
      }
      common_vendor.index.request({
        url,
        method,
        success: (res) => {
          var _a;
          common_vendor.index.hideLoading();
          if (res.data && res.data.code === 200) {
            common_vendor.index.showToast({ title: "支付成功！", icon: "success" });
            this.closePayDialog();
            if (this.payingType === "hotel") {
              this.getHotelOrders();
            } else {
              this.getFlightOrders();
            }
          } else {
            common_vendor.index.showToast({ title: ((_a = res.data) == null ? void 0 : _a.message) || "支付失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/my-orders/my-orders.vue:830", "支付请求失败:", err);
          if (err.statusCode === 404) {
            common_vendor.index.showToast({ title: "支付接口不存在", icon: "none" });
          } else if (err.statusCode === 500) {
            common_vendor.index.showToast({ title: "服务器错误", icon: "none" });
          } else {
            common_vendor.index.showToast({ title: "网络错误，支付失败", icon: "none" });
          }
        }
      });
    },
    formatCity(city) {
      if (!city)
        return "--";
      return city.split("机场")[0] || city;
    },
    formatFullDateTime(dateTimeStr) {
      if (!dateTimeStr)
        return "--";
      try {
        const date = new Date(dateTimeStr);
        if (!isNaN(date.getTime())) {
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, "0");
          const day = String(date.getDate()).padStart(2, "0");
          const hour = String(date.getHours()).padStart(2, "0");
          const minute = String(date.getMinutes()).padStart(2, "0");
          return `${year}-${month}-${day} ${hour}:${minute}`;
        }
        return dateTimeStr;
      } catch (e) {
        return dateTimeStr;
      }
    },
    getStatusText(status) {
      const statusMap = {
        "待支付": "待支付",
        "已支付": "已支付",
        "已出票": "已出票",
        "已取消": "已取消",
        "已完成": "已完成"
      };
      return statusMap[status] || status || "待支付";
    },
    getStatusClass(status) {
      const classMap = {
        "待支付": "status-pending",
        "已支付": "status-paid",
        "已出票": "status-confirmed",
        "已取消": "status-cancelled",
        "已完成": "status-completed"
      };
      return classMap[status] || "status-default";
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.n($data.activeTab === 1 ? "active" : ""),
    b: common_vendor.o(($event) => $options.switchTab(1)),
    c: common_vendor.n($data.activeTab === 2 ? "active" : ""),
    d: common_vendor.o(($event) => $options.switchTab(2)),
    e: $data.activeTab === 1 && $data.orderList.length > 0
  }, $data.activeTab === 1 && $data.orderList.length > 0 ? {
    f: $data.hotelStatusFilter === "all" ? 1 : "",
    g: common_vendor.o(($event) => $options.setHotelStatusFilter("all")),
    h: $data.hotelStatusFilter === "待支付" ? 1 : "",
    i: common_vendor.o(($event) => $options.setHotelStatusFilter("待支付")),
    j: $data.hotelStatusFilter === "待确认" ? 1 : "",
    k: common_vendor.o(($event) => $options.setHotelStatusFilter("待确认")),
    l: $data.hotelStatusFilter === "已支付" ? 1 : "",
    m: common_vendor.o(($event) => $options.setHotelStatusFilter("已支付")),
    n: $data.hotelStatusFilter === "已确认" ? 1 : "",
    o: common_vendor.o(($event) => $options.setHotelStatusFilter("已确认")),
    p: $data.hotelStatusFilter === "已入住" ? 1 : "",
    q: common_vendor.o(($event) => $options.setHotelStatusFilter("已入住")),
    r: $data.hotelStatusFilter === "已完成" ? 1 : "",
    s: common_vendor.o(($event) => $options.setHotelStatusFilter("已完成")),
    t: $data.hotelStatusFilter === "已取消" ? 1 : "",
    v: common_vendor.o(($event) => $options.setHotelStatusFilter("已取消")),
    w: $data.hotelStatusFilter === "取消申请中" ? 1 : "",
    x: common_vendor.o(($event) => $options.setHotelStatusFilter("取消申请中")),
    y: $data.hotelStatusFilter === "取消拒绝" ? 1 : "",
    z: common_vendor.o(($event) => $options.setHotelStatusFilter("取消拒绝"))
  } : {}, {
    A: $data.activeTab === 1 && $options.filteredOrderList.length > 0
  }, $data.activeTab === 1 && $options.filteredOrderList.length > 0 ? {
    B: common_vendor.t($options.filteredOrderList.length),
    C: common_vendor.t($data.orderList.length),
    D: common_vendor.t($data.orderList.filter((o) => o.status === "已完成").length),
    E: common_vendor.t($data.orderList.filter((o) => o.status === "待支付").length)
  } : {}, {
    F: $data.activeTab === 1
  }, $data.activeTab === 1 ? common_vendor.e({
    G: $options.filteredOrderList.length === 0
  }, $options.filteredOrderList.length === 0 ? {
    H: common_vendor.t($data.hotelStatusFilter === "all" ? "" : $data.hotelStatusFilter)
  } : {}, {
    I: common_vendor.f($options.filteredOrderList, (item, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(item.name || item.hotelName),
        b: common_vendor.t(item.id),
        c: common_vendor.t($options.getHotelStatusText(item.status)),
        d: common_vendor.n($options.getHotelStatusClass(item.status)),
        e: common_vendor.t(item.roomTypeName || "标准间"),
        f: common_vendor.t(item.checkIn),
        g: common_vendor.t(item.checkOut),
        h: common_vendor.t(item.contactName || item.username),
        i: common_vendor.t(item.contactPhone || "--"),
        j: common_vendor.t(item.price),
        k: item.status === "取消申请中"
      }, item.status === "取消申请中" ? {} : {}, {
        l: item.status === "取消拒绝"
      }, item.status === "取消拒绝" ? {
        m: common_vendor.t(item.rejectReason || "商家拒绝了取消申请")
      } : {}, {
        n: item.status === "待支付"
      }, item.status === "待支付" ? {
        o: common_vendor.o(($event) => $options.cancelHotelOrder(item), item.id),
        p: common_vendor.o(($event) => $options.showHotelPayDialog(item), item.id)
      } : $options.canApplyCancel(item) ? {
        r: common_vendor.o(($event) => $options.showCancelRequestDialog(item), item.id)
      } : item.status === "取消申请中" ? {} : item.status === "取消拒绝" ? {
        v: common_vendor.o(($event) => $options.closeRejectTip(item), item.id)
      } : {}, {
        q: $options.canApplyCancel(item),
        s: item.status === "取消申请中",
        t: item.status === "取消拒绝",
        w: !item.commented && item.status === "已完成"
      }, !item.commented && item.status === "已完成" ? {
        x: common_vendor.f(5, (n, k1, i1) => {
          return {
            a: n,
            b: n <= item.score ? 1 : "",
            c: common_vendor.o(($event) => item.score = n, n)
          };
        }),
        y: item.content,
        z: common_vendor.o(($event) => item.content = $event.detail.value, item.id),
        A: common_vendor.o(($event) => $options.submitComment(item), item.id)
      } : item.commented ? {} : {}, {
        B: item.commented,
        C: item.id
      });
    })
  }) : {}, {
    J: $data.activeTab === 2
  }, $data.activeTab === 2 ? common_vendor.e({
    K: $data.flightOrders.length > 0
  }, $data.flightOrders.length > 0 ? {
    L: $data.flightStatusFilter === "all" ? 1 : "",
    M: common_vendor.o(($event) => $options.setFlightStatusFilter("all")),
    N: $data.flightStatusFilter === "待支付" ? 1 : "",
    O: common_vendor.o(($event) => $options.setFlightStatusFilter("待支付")),
    P: $data.flightStatusFilter === "已支付" ? 1 : "",
    Q: common_vendor.o(($event) => $options.setFlightStatusFilter("已支付")),
    R: $data.flightStatusFilter === "已出票" ? 1 : "",
    S: common_vendor.o(($event) => $options.setFlightStatusFilter("已出票")),
    T: $data.flightStatusFilter === "已完成" ? 1 : "",
    U: common_vendor.o(($event) => $options.setFlightStatusFilter("已完成")),
    V: $data.flightStatusFilter === "已取消" ? 1 : "",
    W: common_vendor.o(($event) => $options.setFlightStatusFilter("已取消"))
  } : {}, {
    X: $options.filteredFlightOrders.length === 0
  }, $options.filteredFlightOrders.length === 0 ? {
    Y: common_vendor.t($data.flightStatusFilter === "all" ? "" : $data.flightStatusFilter)
  } : {}, {
    Z: common_vendor.f($options.filteredFlightOrders, (item, index, i0) => {
      return common_vendor.e({
        a: common_vendor.t(item.flightNumber || "--"),
        b: common_vendor.t(item.airline || "航空公司"),
        c: common_vendor.t($options.getStatusText(item.status)),
        d: common_vendor.n($options.getStatusClass(item.status)),
        e: common_vendor.t($options.formatCity(item.departCity)),
        f: common_vendor.t(item.departAirport || ""),
        g: common_vendor.t(item.duration || ""),
        h: common_vendor.t($options.formatCity(item.arriveCity)),
        i: common_vendor.t(item.arriveAirport || ""),
        j: common_vendor.t($options.formatFullDateTime(item.departTime)),
        k: common_vendor.t($options.formatFullDateTime(item.arriveTime)),
        l: common_vendor.t(item.passengerName || "--"),
        m: common_vendor.t(item.price),
        n: item.status === "待支付"
      }, item.status === "待支付" ? {
        o: common_vendor.o(($event) => $options.cancelFlightOrder(item.id), index),
        p: common_vendor.o(($event) => $options.showFlightPayDialog(item.id), index)
      } : {}, {
        q: index
      });
    })
  }) : {}, {
    aa: $data.showPayDialogFlag
  }, $data.showPayDialogFlag ? {
    ab: common_vendor.o((...args) => $options.closePayDialog && $options.closePayDialog(...args)),
    ac: common_vendor.t($data.payingAmount),
    ad: common_vendor.o([($event) => $data.payPassword = $event.detail.value, (...args) => $options.onPasswordInput && $options.onPasswordInput(...args)]),
    ae: $data.payPassword,
    af: common_vendor.o((...args) => $options.closePayDialog && $options.closePayDialog(...args)),
    ag: common_vendor.o((...args) => $options.confirmPay && $options.confirmPay(...args)),
    ah: !$options.isPasswordValid,
    ai: common_vendor.o(() => {
    }),
    aj: common_vendor.o((...args) => $options.closePayDialog && $options.closePayDialog(...args))
  } : {}, {
    ak: $data.showCancelRequestFlag
  }, $data.showCancelRequestFlag ? {
    al: common_vendor.o((...args) => $options.closeCancelRequestDialog && $options.closeCancelRequestDialog(...args)),
    am: $data.cancelReason,
    an: common_vendor.o(($event) => $data.cancelReason = $event.detail.value),
    ao: common_vendor.o((...args) => $options.closeCancelRequestDialog && $options.closeCancelRequestDialog(...args)),
    ap: common_vendor.o((...args) => $options.submitCancelRequest && $options.submitCancelRequest(...args)),
    aq: common_vendor.o(() => {
    }),
    ar: common_vendor.o((...args) => $options.closeCancelRequestDialog && $options.closeCancelRequestDialog(...args))
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-5fe9fe45"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/my-orders/my-orders.js.map
