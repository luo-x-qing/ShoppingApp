"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      spots: [],
      days: 3,
      generating: false,
      planResult: null
    };
  },
  computed: {
    routePlanKey() {
      return "routePlan_" + (common_vendor.index.getStorageSync("loginUsername") || "default");
    },
    targetCity() {
      const cities = [...new Set(this.spots.map((s) => s.city).filter(Boolean))];
      return cities.length > 0 ? cities.join("、") : "";
    }
  },
  onShow() {
    this.loadSpots();
  },
  methods: {
    loadSpots() {
      const data = common_vendor.index.getStorageSync(this.routePlanKey);
      this.spots = Array.isArray(data) ? data : [];
    },
    removeSpot(index) {
      this.spots.splice(index, 1);
      common_vendor.index.setStorageSync(this.routePlanKey, this.spots);
      this.planResult = null;
    },
    clearAll() {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定清空所有已选景点？",
        success: (res) => {
          if (res.confirm) {
            this.spots = [];
            common_vendor.index.setStorageSync(this.routePlanKey, []);
            this.planResult = null;
          }
        }
      });
    },
    generatePlan() {
      if (!this.targetCity) {
        common_vendor.index.showToast({ title: "景点缺少城市信息", icon: "none" });
        return;
      }
      this.generating = true;
      const city = this.targetCity.split("、")[0];
      common_vendor.index.request({
        url: `http://localhost:8080/travel/plan?city=${encodeURIComponent(city)}&days=${this.days}`,
        method: "GET",
        success: (res) => {
          this.planResult = res.data;
          common_vendor.index.showToast({ title: "路线生成成功", icon: "success" });
        },
        fail: () => {
          common_vendor.index.showToast({ title: "AI规划失败，请重试", icon: "none" });
        },
        complete: () => {
          this.generating = false;
        }
      });
    },
    addRouteToCart() {
      if (!this.planResult || !this.planResult.spots)
        return;
      const username = common_vendor.index.getStorageSync("loginUsername");
      if (!username) {
        common_vendor.index.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      let count = 0;
      const promises = this.planResult.spots.map((spotName) => {
        return new Promise((resolve) => {
          common_vendor.index.request({
            url: "http://localhost:8080/api/cart/add-scenic",
            method: "POST",
            data: { username, scenicName: spotName, quantity: 1 },
            success: () => {
              count++;
            },
            fail: () => {
            },
            complete: () => resolve()
          });
        });
      });
      Promise.all(promises).then(() => {
        common_vendor.index.showToast({ title: `已添加 ${count} 个景点到购物车` });
      });
    }
  }
};
if (!Array) {
  const _component_RouteFloat = common_vendor.resolveComponent("RouteFloat");
  _component_RouteFloat();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.t($data.spots.length),
    b: common_vendor.o((...args) => $options.clearAll && $options.clearAll(...args)),
    c: $data.spots.length === 0
  }, $data.spots.length === 0 ? {} : {}, {
    d: common_vendor.f($data.spots, (s, i, i0) => {
      return {
        a: common_vendor.t(i + 1),
        b: common_vendor.t(s.name),
        c: common_vendor.t(s.province),
        d: common_vendor.t(s.city),
        e: common_vendor.o(($event) => $options.removeSpot(i), s.id),
        f: s.id
      };
    }),
    e: $data.spots.length > 0
  }, $data.spots.length > 0 ? {
    f: common_vendor.t($options.targetCity || "自动识别"),
    g: common_vendor.f([1, 2, 3, 4, 5], (d, k0, i0) => {
      return {
        a: common_vendor.t(d),
        b: $data.days === d ? 1 : "",
        c: d,
        d: common_vendor.o(($event) => $data.days = d, d)
      };
    }),
    h: common_vendor.t($data.generating ? "AI生成中..." : "🤖 AI 生成路线"),
    i: common_vendor.o((...args) => $options.generatePlan && $options.generatePlan(...args)),
    j: $data.generating
  } : {}, {
    k: $data.planResult
  }, $data.planResult ? common_vendor.e({
    l: common_vendor.t($data.planResult.city),
    m: common_vendor.t($data.planResult.days),
    n: $data.planResult.route && $data.planResult.route.distance
  }, $data.planResult.route && $data.planResult.route.distance ? {
    o: common_vendor.t(($data.planResult.route.distance / 1e3).toFixed(1)),
    p: common_vendor.t(Math.round($data.planResult.route.duration / 60))
  } : {}, {
    q: common_vendor.f($data.planResult.spots, (spot, i, i0) => {
      return common_vendor.e({
        a: common_vendor.t(i + 1),
        b: common_vendor.t(spot),
        c: $data.planResult.locations && $data.planResult.locations[i]
      }, $data.planResult.locations && $data.planResult.locations[i] ? {
        d: common_vendor.t($data.planResult.locations[i].location)
      } : {}, {
        e: i
      });
    }),
    r: common_vendor.o((...args) => $options.addRouteToCart && $options.addRouteToCart(...args)),
    s: common_vendor.o(($event) => $data.planResult = null)
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-f6c85b3a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/ai-plan/ai-plan.js.map
