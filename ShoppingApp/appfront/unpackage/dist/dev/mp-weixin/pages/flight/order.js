"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      flightInfo: {},
      orderData: {
        passengerCount: 1,
        loginUsername: ""
      },
      passengerList: [
        {
          userName: "",
          idCard: "",
          userPhone: ""
        }
      ],
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
    common_vendor.index.__f__("log", "at pages/flight/order.vue:154", "订单页面加载");
    const flight = common_vendor.index.getStorageSync("selectedFlight");
    common_vendor.index.__f__("log", "at pages/flight/order.vue:158", "获取到的航班信息：", flight);
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
    common_vendor.index.__f__("log", "at pages/flight/order.vue:191", "订单数据初始化完成", this.flightInfo);
  },
  methods: {
    // 乘机人数变化
    onPassengerCountChange(e) {
      const newCount = this.passengerCounts[e.detail.value];
      this.orderData.passengerCount = newCount;
      const currentCount = this.passengerList.length;
      if (newCount > currentCount) {
        for (let i = currentCount; i < newCount; i++) {
          this.passengerList.push({
            userName: "",
            idCard: "",
            userPhone: ""
          });
        }
      } else if (newCount < currentCount) {
        this.passengerList = this.passengerList.slice(0, newCount);
      }
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
        common_vendor.index.__f__("error", "at pages/flight/order.vue:238", "日期解析错误：", e);
        return null;
      }
    },
    // 提交订单 - 为每个乘客单独创建订单
    async submitOrder() {
      common_vendor.index.__f__("log", "at pages/flight/order.vue:245", "提交订单", this.orderData);
      common_vendor.index.__f__("log", "at pages/flight/order.vue:246", "乘机人列表", this.passengerList);
      if (!this.orderData.loginUsername) {
        common_vendor.index.showToast({ title: "请先登录", icon: "none" });
        setTimeout(() => {
          common_vendor.index.navigateTo({
            url: "/pages/login-register/login-register"
          });
        }, 1500);
        return;
      }
      for (let i = 0; i < this.passengerList.length; i++) {
        const passenger = this.passengerList[i];
        const passengerNum = i + 1;
        if (!passenger.userName || passenger.userName.trim() === "") {
          common_vendor.index.showToast({ title: `请输入乘机人${passengerNum}的姓名`, icon: "none" });
          return;
        }
        if (passenger.userName.length < 2) {
          common_vendor.index.showToast({ title: `请输入乘机人${passengerNum}的真实姓名`, icon: "none" });
          return;
        }
        if (!passenger.idCard || passenger.idCard.trim() === "") {
          common_vendor.index.showToast({ title: `请输入乘机人${passengerNum}的身份证号`, icon: "none" });
          return;
        }
        const idCardRegex = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/;
        if (!idCardRegex.test(passenger.idCard)) {
          common_vendor.index.showToast({ title: `乘机人${passengerNum}身份证号格式不正确`, icon: "none" });
          return;
        }
        if (!passenger.userPhone) {
          common_vendor.index.showToast({ title: `请输入乘机人${passengerNum}的联系电话`, icon: "none" });
          return;
        }
        if (!/^1[3-9]\d{9}$/.test(passenger.userPhone)) {
          common_vendor.index.showToast({ title: `乘机人${passengerNum}手机号格式不正确`, icon: "none" });
          return;
        }
      }
      this.isSubmitting = true;
      common_vendor.index.showLoading({ title: "正在提交订单...", mask: true });
      const price = parseFloat(this.flightInfo.price) || 0;
      let successCount = 0;
      let failCount = 0;
      for (let i = 0; i < this.passengerList.length; i++) {
        const passenger = this.passengerList[i];
        const passengerNum = i + 1;
        const submitData = {
          flightNumber: this.flightInfo.flightNumber,
          departCity: this.departureCity,
          arriveCity: this.arrivalCity,
          departTime: this.formatDateTimeForSubmit(this.flightInfo.departureTime),
          arriveTime: this.formatDateTimeForSubmit(this.flightInfo.arrivalTime),
          price,
          username: this.orderData.loginUsername,
          passengerName: passenger.userName,
          passengerIdCard: passenger.idCard,
          contactPhone: passenger.userPhone,
          status: "待支付"
        };
        common_vendor.index.__f__("log", "at pages/flight/order.vue:318", `提交第${passengerNum}个订单：`, submitData);
        try {
          const result = await this.createOrder(submitData);
          if (result.success) {
            successCount++;
          } else {
            failCount++;
          }
        } catch (error) {
          common_vendor.index.__f__("error", "at pages/flight/order.vue:328", `第${passengerNum}个订单提交失败：`, error);
          failCount++;
        }
      }
      common_vendor.index.hideLoading();
      if (successCount === this.passengerList.length) {
        common_vendor.index.showToast({
          title: `成功预订${successCount}个订单！`,
          icon: "success"
        });
        common_vendor.index.removeStorageSync("selectedFlight");
        setTimeout(() => {
          common_vendor.index.switchTab({
            url: "/pages/profile/profile"
          });
        }, 1500);
      } else if (successCount > 0) {
        common_vendor.index.showModal({
          title: "提交结果",
          content: `成功提交${successCount}个订单，失败${failCount}个，请检查后重试`,
          showCancel: false,
          success: () => {
            if (successCount > 0) {
              common_vendor.index.removeStorageSync("selectedFlight");
              setTimeout(() => {
                common_vendor.index.switchTab({
                  url: "/pages/profile/profile"
                });
              }, 500);
            }
          }
        });
      } else {
        common_vendor.index.showToast({
          title: "订单提交失败，请重试",
          icon: "none"
        });
      }
      this.isSubmitting = false;
    },
    createOrder(submitData) {
      return new Promise((resolve, reject) => {
        common_vendor.index.request({
          url: "http://localhost:8080/api/flight-orders",
          method: "POST",
          header: {
            "Content-Type": "application/json"
          },
          data: submitData,
          success: (res) => {
            common_vendor.index.__f__("log", "at pages/flight/order.vue:384", "单个订单提交结果：", res);
            if (res.statusCode === 200 && res.data.code === 200) {
              resolve({ success: true, data: res.data });
            } else {
              resolve({ success: false, message: res.data.message || "提交失败" });
            }
          },
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/flight/order.vue:392", "订单提交失败：", err);
            reject(err);
          }
        });
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
    i: common_vendor.t($data.orderData.passengerCount),
    j: $data.passengerCounts,
    k: common_vendor.o((...args) => $options.onPassengerCountChange && $options.onPassengerCountChange(...args)),
    l: common_vendor.f($data.passengerList, (passenger, index, i0) => {
      return {
        a: common_vendor.t(index + 1),
        b: passenger.userName,
        c: common_vendor.o(($event) => passenger.userName = $event.detail.value, index),
        d: passenger.idCard,
        e: common_vendor.o(($event) => passenger.idCard = $event.detail.value, index),
        f: passenger.userPhone,
        g: common_vendor.o(($event) => passenger.userPhone = $event.detail.value, index),
        h: index
      };
    }),
    m: common_vendor.t($options.totalPrice),
    n: common_vendor.t($data.isSubmitting ? "提交中..." : "提交订单"),
    o: common_vendor.o((...args) => $options.submitOrder && $options.submitOrder(...args)),
    p: $data.isSubmitting
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-197ccafb"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/flight/order.js.map
