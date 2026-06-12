"use strict";
const common_vendor = require("../common/vendor.js");
const _sfc_main = {
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
      common_vendor.index.switchTab({ url: "/pages/ai-plan/ai-plan" });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
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
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-6b8d0a73"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../.sourcemap/mp-weixin/components/RouteFloat.js.map
