"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
const utils_http = require("./utils/http.js");
if (!Math) {
  "./pages/home/home.js";
  "./pages/flight/flight.js";
  "./pages/category/category.js";
  "./pages/profile/profile.js";
  "./pages/detail/detail.js";
  "./pages/hotelDetail/hotelDetail.js";
  "./pages/booking/booking.js";
  "./pages/my-orders/my-orders.js";
  "./pages/collection/collection.js";
  "./pages/login-register/login-register.js";
  "./pages/route-detail/route-detail.js";
  "./pages/cart/cart.js";
}
const _sfc_main = {
  created() {
    const token = common_vendor.index.getStorageSync("token");
    if (!token) {
      common_vendor.index.redirectTo({
        url: "/pages/login-register/login-register"
      });
    }
  }
};
if (!Array) {
  const _component_router_view = common_vendor.resolveComponent("router-view");
  _component_router_view();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {};
}
const App = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render]]);
function createApp() {
  const app = common_vendor.createSSRApp(App);
  app.config.globalProperties.$http = utils_http.http;
  return {
    app
  };
}
createApp().app.mount("#app");
exports.createApp = createApp;
//# sourceMappingURL=../.sourcemap/mp-weixin/app.js.map
