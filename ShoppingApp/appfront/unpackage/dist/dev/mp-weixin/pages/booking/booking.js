"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      id: "",
      hotelName: "",
      price: 0,
      checkInDate: "",
      checkOutDate: "",
      days: 1,
      totalPrice: 0,
      username: ""
    };
  },
  onLoad(options) {
    const today = /* @__PURE__ */ new Date();
    const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1e3);
    const formatDate = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
    this.id = options.id;
    this.hotelName = options.name;
    this.price = Number(options.price);
    this.checkInDate = formatDate(today);
    this.checkOutDate = formatDate(tomorrow);
    this.days = 1;
    this.totalPrice = Number(options.price);
    this.username = options.username || common_vendor.index.getStorageSync("loginUsername");
  },
  methods: {
    bindCheckInChange(e) {
      this.checkInDate = e.detail.value;
      this.calcDays();
    },
    bindCheckOutChange(e) {
      this.checkOutDate = e.detail.value;
      this.calcDays();
    },
    calcDays() {
      const inDate = new Date(this.checkInDate);
      const outDate = new Date(this.checkOutDate);
      const diff = (outDate - inDate) / (1e3 * 60 * 60 * 24);
      const days = diff > 0 ? diff : 1;
      this.days = days;
      this.totalPrice = days * this.price;
    },
    // ✅ 强制把 username 传给后端
    submitOrder() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotel-orders",
        method: "POST",
        data: {
          hotelId: this.id,
          name: this.hotelName,
          price: this.totalPrice,
          checkIn: this.checkInDate,
          checkOut: this.checkOutDate,
          username: this.username
          // 🔥 这里必须存在！
        },
        success: (res) => {
          common_vendor.index.showToast({ title: "预订成功", icon: "success" });
          setTimeout(() => {
            common_vendor.index.navigateBack({ delta: 2 });
          }, 1500);
        }
      });
    }
  }
};
if (!Array) {
  const _component_RouteFloat = common_vendor.resolveComponent("RouteFloat");
  _component_RouteFloat();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.t($data.hotelName),
    b: common_vendor.t($data.price),
    c: common_vendor.t($data.checkInDate),
    d: $data.checkInDate,
    e: common_vendor.o((...args) => $options.bindCheckInChange && $options.bindCheckInChange(...args)),
    f: common_vendor.t($data.checkOutDate),
    g: $data.checkOutDate,
    h: common_vendor.o((...args) => $options.bindCheckOutChange && $options.bindCheckOutChange(...args)),
    i: common_vendor.t($data.days),
    j: common_vendor.t($data.totalPrice),
    k: common_vendor.o((...args) => $options.submitOrder && $options.submitOrder(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-d331dabb"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/booking/booking.js.map
