"use strict";
const common_vendor = require("../../common/vendor.js");
const BASE_URL = "http://localhost:8080";
const _sfc_main = {
  data() {
    return {
      spots: [],
      days: 3,
      budget: "",
      travelers: 1,
      generating: false,
      sending: false,
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
    },
    allSpots() {
      const r = this.planResult;
      if (!r || !r.scenicSpots)
        return [];
      return r.scenicSpots.filter((s) => s.lat && s.lng);
    },
    mapMarkers() {
      return this.allSpots.map((s, i) => ({
        id: i,
        latitude: s.lat,
        longitude: s.lng,
        title: s.name,
        label: {
          content: `${s.order}`,
          color: "#fff",
          fontSize: 13,
          borderRadius: 8,
          bgColor: "#ff6b35",
          padding: 6,
          textAlign: "center"
        },
        width: 30,
        height: 30
      }));
    },
    mapCenter() {
      const arr = this.allSpots;
      if (arr.length === 0)
        return { lat: 0, lng: 0 };
      const sum = arr.reduce((s, p) => ({ lat: s.lat + p.lat, lng: s.lng + p.lng }), { lat: 0, lng: 0 });
      return { lat: sum.lat / arr.length, lng: sum.lng / arr.length };
    },
    mapPolyline() {
      if (this.allSpots.length < 2)
        return [];
      const sorted = [...this.allSpots].sort((a, b) => a.order - b.order);
      const points = sorted.map((s) => ({ latitude: s.lat, longitude: s.lng }));
      return [{ points, color: "#ff6b35", width: 4 }];
    },
    mapIncludePoints() {
      return this.allSpots.map((s) => ({ latitude: s.lat, longitude: s.lng }));
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
      const spotsParam = this.spots.map((s) => s.name).join("，");
      const budgetVal = parseInt(this.budget) || 0;
      common_vendor.index.request({
        url: `${BASE_URL}/travel/plan`,
        method: "GET",
        data: {
          city,
          days: this.days,
          budget: budgetVal,
          travelers: this.travelers,
          spots: spotsParam
        },
        success: (res) => {
          if (res.data.error) {
            common_vendor.index.showToast({ title: res.data.error, icon: "none" });
            return;
          }
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
    resetPlan() {
      this.planResult = null;
    },
    typeLabel(type) {
      const map = {
        breakfast: "🍳 早餐",
        lunch: "🍜 午餐",
        dinner: "🍽 晚餐",
        scenic: "🏞 景点",
        transport: "🚗 交通",
        hotel: "🏨 住宿",
        free: "🆓 自由活动"
      };
      return map[type] || type;
    },
    sendToAIChat() {
      if (!this.planResult || this.sending)
        return;
      this.sending = true;
      const r = this.planResult;
      let text = `这是我规划的${r.city}${r.days}天行程（预算¥${r.budget || "不限"}，${r.travelers}人）：

`;
      (r.itinerary || []).forEach((day) => {
        text += `【第${day.day}天】
`;
        (day.schedule || []).forEach((item) => {
          text += `  ${item.time} ${this.typeLabel(item.type)} ${item.content}`;
          if (item.location)
            text += `（${item.location}）`;
          text += "\n";
        });
        text += "\n";
      });
      if (r.tips)
        text += `💡 ${r.tips}

`;
      text += "请帮我优化这个行程，给出改进建议！";
      common_vendor.index.setStorageSync("ai_route_context", text);
      common_vendor.index.setStorageSync("ai_pending_route", true);
      this.sending = false;
      common_vendor.index.navigateTo({ url: "/pages/ai-chat/ai-chat" });
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
    h: $data.budget,
    i: common_vendor.o(($event) => $data.budget = $event.detail.value),
    j: common_vendor.f([1, 2, 3, 4, 5, 6], (n, k0, i0) => {
      return {
        a: common_vendor.t(n),
        b: $data.travelers === n ? 1 : "",
        c: n,
        d: common_vendor.o(($event) => $data.travelers = n, n)
      };
    }),
    k: common_vendor.t($data.generating ? "AI生成中..." : "🤖 AI 生成路线"),
    l: common_vendor.o((...args) => $options.generatePlan && $options.generatePlan(...args)),
    m: $data.generating
  } : {}, {
    n: $data.planResult
  }, $data.planResult ? common_vendor.e({
    o: common_vendor.t($data.planResult.city),
    p: common_vendor.t($data.planResult.days),
    q: common_vendor.t($data.planResult.travelers),
    r: common_vendor.t($data.planResult.budget || "不限"),
    s: common_vendor.t($data.planResult.totalBudget),
    t: common_vendor.f($data.planResult.itinerary, (day, di, i0) => {
      return {
        a: common_vendor.t(day.day),
        b: common_vendor.t(day.schedule.length),
        c: common_vendor.f(day.schedule, (item, si, i1) => {
          return common_vendor.e({
            a: common_vendor.t(item.time),
            b: common_vendor.t($options.typeLabel(item.type)),
            c: item.type === "scenic" && item.spotName
          }, item.type === "scenic" && item.spotName ? {
            d: common_vendor.t(item.spotName)
          } : {}, {
            e: common_vendor.t(item.content),
            f: item.location
          }, item.location ? {
            g: common_vendor.t(item.location)
          } : {}, {
            h: si,
            i: common_vendor.n("type-" + item.type)
          });
        }),
        d: di
      };
    }),
    v: $options.allSpots.length > 0
  }, $options.allSpots.length > 0 ? {
    w: $options.mapCenter.lat,
    x: $options.mapCenter.lng,
    y: $options.mapMarkers,
    z: $options.mapPolyline,
    A: $options.mapIncludePoints
  } : {}, {
    B: $data.planResult.tips
  }, $data.planResult.tips ? {
    C: common_vendor.t($data.planResult.tips)
  } : {}, {
    D: common_vendor.o((...args) => $options.sendToAIChat && $options.sendToAIChat(...args)),
    E: $data.sending,
    F: common_vendor.o((...args) => $options.resetPlan && $options.resetPlan(...args))
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-f6c85b3a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/ai-plan/ai-plan.js.map
