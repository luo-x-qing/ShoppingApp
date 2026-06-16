"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      flightInfo: {},
      orderData: {
        userName: "",
        idCard: "",
        userPhone: "",
        passengerCount: 1,
        loginUsername: ""
      },
      passengerCounts: [1, 2, 3, 4, 5],
      isSubmitting: false
    };
  },
  computed: {
    totalPrice() {
      const price = parseFloat(this.flightInfo.price) || 0;
      return price * this.orderData.passengerCount;
    },
    departureCity() {
      const airport = this.flightInfo.departureAirport || "";
      return airport.split("机场")[0] || airport || "--";
    },
    arrivalCity() {
      const airport = this.flightInfo.arrivalAirport || "";
      return airport.split("机场")[0] || airport || "--";
    }
  },
  onLoad() {
    common_vendor.index.__f__("log", "at pages/flight/order.vue:145", "订单页面加载");
    const flight = common_vendor.index.getStorageSync("selectedFlight");
    common_vendor.index.__f__("log", "at pages/flight/order.vue:149", "获取到的航班信息：", flight);
    if (flight && Object.keys(flight).length > 0) {
      this.flightInfo = flight;
    } else {
      common_vendor.index.showToast({
        title: "请先选择航班",
        icon: "none",
        duration: 2e3
      });
      setTimeout(() => {
        common_vendor.index.navigateBack();
      }, 2e3);
    }
    const loginUsername = common_vendor.index.getStorageSync("loginUsername");
    this.orderData.loginUsername = loginUsername || "";
    if (!this.orderData.loginUsername) {
      common_vendor.index.showToast({
        title: "请先登录",
        icon: "none",
        duration: 2e3
      });
      setTimeout(() => {
        common_vendor.index.navigateTo({
          url: "/pages/login-register/login-register"
        });
      }, 2e3);
    }
    common_vendor.index.__f__("log", "at pages/flight/order.vue:182", "订单数据初始化完成", this.flightInfo);
  },
  methods: {
    // 乘机人数变化
    onPassengerCountChange(e) {
      this.orderData.passengerCount = this.passengerCounts[e.detail.value];
    },
    // 格式化显示日期时间（用于页面展示）
    formatDisplayDateTime(dateTimeStr) {
      if (!dateTimeStr)
        return "--";
      if (dateTimeStr.includes(" ")) {
        return dateTimeStr;
      }
      return dateTimeStr;
    },
    // 格式化时间用于提交（转为 LocalDateTime 格式）
    formatDateTimeForSubmit(dateTimeStr) {
      if (!dateTimeStr)
        return null;
      try {
        if (dateTimeStr.includes(" ") && !dateTimeStr.includes("T")) {
          const [date, time] = dateTimeStr.split(" ");
          const timeWithSeconds = time.split(":").length === 2 ? time + ":00" : time;
          return `${date}T${timeWithSeconds}`;
        }
        if (dateTimeStr.includes("T")) {
          return dateTimeStr;
        }
        return dateTimeStr;
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/flight/order.vue:217", "日期解析错误：", e);
        return null;
      }
    },
    // 提交订单
    submitOrder() {
      common_vendor.index.__f__("log", "at pages/flight/order.vue:224", "提交订单", this.orderData);
      if (!this.orderData.loginUsername) {
        common_vendor.index.showToast({ title: "请先登录", icon: "none" });
        setTimeout(() => {
          common_vendor.index.navigateTo({
            url: "/pages/login-register/login-register"
          });
        }, 1500);
        return;
      }
      if (!this.orderData.userName || this.orderData.userName.trim() === "") {
        common_vendor.index.showToast({ title: "请输入乘客姓名", icon: "none" });
        return;
      }
      if (this.orderData.userName.length < 2) {
        common_vendor.index.showToast({ title: "请输入真实姓名", icon: "none" });
        return;
      }
      if (!this.orderData.idCard || this.orderData.idCard.trim() === "") {
        common_vendor.index.showToast({ title: "请输入身份证号", icon: "none" });
        return;
      }
      const idCardRegex = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/;
      if (!idCardRegex.test(this.orderData.idCard)) {
        common_vendor.index.showToast({ title: "身份证号格式不正确", icon: "none" });
        return;
      }
      if (!this.orderData.userPhone) {
        common_vendor.index.showToast({ title: "请输入联系电话", icon: "none" });
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(this.orderData.userPhone)) {
        common_vendor.index.showToast({ title: "手机号格式不正确", icon: "none" });
        return;
      }
      const submitData = {
        flightNumber: this.flightInfo.flightNumber,
        departCity: this.departureCity,
        arriveCity: this.arrivalCity,
        departTime: this.formatDateTimeForSubmit(this.flightInfo.departureTime),
        arriveTime: this.formatDateTimeForSubmit(this.flightInfo.arrivalTime),
        price: this.totalPrice,
        username: this.orderData.loginUsername,
        passengerName: this.orderData.userName,
        passengerIdCard: this.orderData.idCard,
        contactPhone: this.orderData.userPhone,
        status: "待支付"
      };
      common_vendor.index.__f__("log", "at pages/flight/order.vue:283", "提交的订单数据：", submitData);
      this.isSubmitting = true;
      common_vendor.index.showLoading({ title: "提交中..." });
      common_vendor.index.request({
        url: "http://localhost:8080/api/flight-orders",
        method: "POST",
        header: {
          "Content-Type": "application/json"
        },
        data: submitData,
        success: (res) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("log", "at pages/flight/order.vue:298", "提交成功", res);
          if (res.statusCode === 200 && res.data.code === 200) {
            common_vendor.index.showToast({ title: "预订成功！", icon: "success" });
            common_vendor.index.removeStorageSync("selectedFlight");
            setTimeout(() => {
              common_vendor.index.switchTab({
                url: "/pages/profile/profile"
              });
            }, 1500);
          } else {
            common_vendor.index.showToast({
              title: res.data.message || "提交失败，请重试",
              icon: "none"
            });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/flight/order.vue:321", "提交订单失败：", err);
          common_vendor.index.showToast({
            title: "网络错误，请检查网络连接",
            icon: "none"
          });
        },
        complete: () => {
          this.isSubmitting = false;
        }
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.t($data.flightInfo.flightNumber || "--"),
    b: common_vendor.t($data.flightInfo.airline || "--"),
    c: common_vendor.t($options.departureCity || "--"),
    d: common_vendor.t($options.arrivalCity || "--"),
    e: common_vendor.t($options.formatDisplayDateTime($data.flightInfo.departureTime)),
    f: common_vendor.t($options.formatDisplayDateTime($data.flightInfo.arrivalTime)),
    g: common_vendor.t($data.flightInfo.duration || "--"),
    h: common_vendor.t($data.flightInfo.price || "--"),
    i: $data.orderData.userName,
    j: common_vendor.o(($event) => $data.orderData.userName = $event.detail.value),
    k: $data.orderData.idCard,
    l: common_vendor.o(($event) => $data.orderData.idCard = $event.detail.value),
    m: $data.orderData.userPhone,
    n: common_vendor.o(($event) => $data.orderData.userPhone = $event.detail.value),
    o: common_vendor.t($data.orderData.passengerCount),
    p: $data.passengerCounts,
    q: common_vendor.o((...args) => $options.onPassengerCountChange && $options.onPassengerCountChange(...args)),
    r: common_vendor.t($options.totalPrice),
    s: common_vendor.t($data.isSubmitting ? "提交中..." : "提交订单"),
    t: common_vendor.o((...args) => $options.submitOrder && $options.submitOrder(...args)),
    v: $data.isSubmitting
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-197ccafb"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/flight/order.js.map
