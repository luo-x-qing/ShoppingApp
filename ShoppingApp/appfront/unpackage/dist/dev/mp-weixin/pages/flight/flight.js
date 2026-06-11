"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      flightIndex: 0,
      flightList: ["CA1234 北京→广州", "MU5101 上海→北京", "CZ3521 深圳→上海"],
      flightData: [
        { flightNumber: "CA1234", departCity: "北京", arriveCity: "广州", departTime: "2025-05-20 08:00", arriveTime: "2025-05-20 10:30", price: 880 },
        { flightNumber: "MU5101", departCity: "上海", arriveCity: "北京", departTime: "2025-05-21 09:00", arriveTime: "2025-05-21 11:20", price: 920 },
        { flightNumber: "CZ3521", departCity: "深圳", arriveCity: "上海", departTime: "2025-05-22 14:00", arriveTime: "2025-05-22 16:10", price: 760 }
      ],
      form: {
        flightNumber: "",
        departCity: "",
        arriveCity: "",
        departTime: "",
        arriveTime: "",
        userName: "",
        userPhone: "",
        price: 0,
        loginUsername: ""
      }
    };
  },
  onLoad() {
    this.form.loginUsername = common_vendor.index.getStorageSync("loginUsername") || "";
    this.onFlightChange({ detail: { value: 0 } });
  },
  methods: {
    onFlightChange(e) {
      const idx = e.detail.value;
      this.flightIndex = idx;
      const data = this.flightData[idx];
      this.form = { ...this.form, ...data };
    },
    submitOrder() {
      if (!this.form.userName) {
        common_vendor.index.showToast({ title: "请输入姓名", icon: "none" });
        return;
      }
      if (!/^1\d{10}$/.test(this.form.userPhone)) {
        common_vendor.index.showToast({ title: "手机号格式错误", icon: "none" });
        return;
      }
      this.form.loginUsername = common_vendor.index.getStorageSync("loginUsername") || "";
      common_vendor.index.request({
        url: "http://localhost:8080/api/flight-orders",
        method: "POST",
        header: { "Content-Type": "application/json" },
        data: this.form,
        success: () => {
          common_vendor.index.showToast({ title: "预订成功", icon: "success" });
          setTimeout(() => {
            common_vendor.index.navigateTo({ url: "/pages/my-orders/my-orders" });
          }, 1200);
        },
        fail: () => {
          common_vendor.index.showToast({ title: "提交失败", icon: "none" });
        }
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.t($data.form.flightNumber || "请选择航班"),
    b: $data.flightIndex,
    c: $data.flightList,
    d: common_vendor.o((...args) => $options.onFlightChange && $options.onFlightChange(...args)),
    e: common_vendor.t($data.form.departCity),
    f: common_vendor.t($data.form.arriveCity),
    g: common_vendor.t($data.form.departTime),
    h: common_vendor.t($data.form.arriveTime),
    i: $data.form.userName,
    j: common_vendor.o(($event) => $data.form.userName = $event.detail.value),
    k: $data.form.userPhone,
    l: common_vendor.o(($event) => $data.form.userPhone = $event.detail.value),
    m: common_vendor.t($data.form.price),
    n: common_vendor.o((...args) => $options.submitOrder && $options.submitOrder(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-aa7f39c0"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/flight/flight.js.map
