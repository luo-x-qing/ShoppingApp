"use strict";
const common_vendor = require("./common/vendor.js");
const utils_http = require("./utils/http.js");
if (!Math) {
  "./pages/home/home.js";
  "./pages/flight/flight.js";
  "./pages/flight/search-flight.js";
  "./pages/flight/order.js";
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
  "./pages/ai-plan/ai-plan.js";
  "./pages/ai-chat/ai-chat.js";
  "./pages/merchant/home.js";
  "./pages/merchant/hotel-list.js";
  "./pages/merchant/order-list.js";
  "./pages/merchant/comment-list.js";
  "./pages/merchant/messages.js";
  "./pages/merchant/notifications.js";
  "./pages/merchant/setting.js";
  "./pages/message/message.js";
  "./pages/profile/setting.js";
  "./pages/profile/notifications.js";
}
const _sfc_main$1 = {
  data() {
    return {
      spots: [],
      showPanel: false
    };
  },
  mounted() {
    this.loadSpots();
    this.timer = setInterval(() => this.loadSpots(), 2e3);
  },
  beforeUnmount() {
    if (this.timer)
      clearInterval(this.timer);
  },
  methods: {
    getKey() {
      return "routePlan_" + (common_vendor.index.getStorageSync("loginUsername") || "default");
    },
    loadSpots() {
      const key = this.getKey();
      const data = common_vendor.index.getStorageSync(key);
      this.spots = Array.isArray(data) ? data : [];
    },
    togglePanel() {
      this.showPanel = !this.showPanel;
      if (this.showPanel)
        this.loadSpots();
    },
    confirmRemove(i) {
      const spot = this.spots[i];
      common_vendor.index.showModal({
        title: "提示",
        content: `从路线中移除「${spot.name}」吗？`,
        success: (res) => {
          if (res.confirm) {
            this.spots.splice(i, 1);
            common_vendor.index.setStorageSync(this.getKey(), this.spots);
          }
        }
      });
    },
    clearAll() {
      this.spots = [];
      common_vendor.index.setStorageSync(this.getKey(), []);
    },
    goToFullPlan() {
      this.showPanel = false;
      common_vendor.index.navigateTo({ url: "/pages/ai-plan/ai-plan" });
    }
  }
};
function _sfc_render$1(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.spots.length > 0
  }, $data.spots.length > 0 ? {
    b: common_vendor.t($data.spots.length)
  } : {}, {
    c: common_vendor.o((...args) => $options.togglePanel && $options.togglePanel(...args)),
    d: $data.showPanel
  }, $data.showPanel ? {
    e: common_vendor.o((...args) => $options.togglePanel && $options.togglePanel(...args))
  } : {}, {
    f: $data.showPanel
  }, $data.showPanel ? common_vendor.e({
    g: common_vendor.t($data.spots.length),
    h: common_vendor.o((...args) => $options.clearAll && $options.clearAll(...args)),
    i: $data.spots.length === 0
  }, $data.spots.length === 0 ? {} : {}, {
    j: common_vendor.f($data.spots, (s, i, i0) => {
      return {
        a: common_vendor.t(i + 1),
        b: common_vendor.t(s.name),
        c: common_vendor.t(s.province),
        d: common_vendor.t(s.city),
        e: s.id,
        f: common_vendor.o(($event) => $options.confirmRemove(i), s.id)
      };
    }),
    k: common_vendor.o((...args) => $options.goToFullPlan && $options.goToFullPlan(...args))
  }) : {});
}
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main$1, [["render", _sfc_render$1], ["__scopeId", "data-v-6b8d0a73"]]);
const _sfc_main = {
  components: { RouteFloat: Component },
  onLaunch() {
    this.checkLogin();
  },
  onShow() {
    this.checkLogin();
  },
  onHide() {
  },
  methods: {
    checkLogin() {
      const token = common_vendor.index.getStorageSync("token");
      if (!token) {
        const pages = getCurrentPages();
        if (pages.length > 0) {
          const route = pages[pages.length - 1].route;
          if (route === "pages/login-register/login-register") {
            return;
          }
        }
        common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
      }
    }
  }
};
if (!Array) {
  const _component_router_view = common_vendor.resolveComponent("router-view");
  const _component_RouteFloat = common_vendor.resolveComponent("RouteFloat");
  (_component_router_view + _component_RouteFloat)();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {};
}
const App = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render]]);
const RouteFloat = () => "./components/RouteFloat.js";
function createApp() {
  const app = common_vendor.createSSRApp(App);
  app.config.globalProperties.$http = utils_http.http;
  app.component("RouteFloat", RouteFloat);
  return {
    app
  };
}
createApp().app.mount("#app");
exports.Component = Component;
exports.createApp = createApp;
//# sourceMappingURL=../.sourcemap/mp-weixin/main.js.map
