"use strict";
const common_vendor = require("../../common/vendor.js");
const BASE_URL = "http://localhost:8080";
const _sfc_main = {
  data() {
    return {
      routeId: null,
      route: {},
      loading: true
    };
  },
  computed: {
    dayGroups() {
      const scenics = this.route.routeScenics || [];
      const map = {};
      scenics.forEach((rs) => {
        const day = rs.dayNumber || 1;
        if (!map[day])
          map[day] = [];
        map[day].push(rs);
      });
      return Object.keys(map).sort().map((k) => ({
        dayNumber: k,
        scenics: map[k]
      }));
    },
    totalScenicCount() {
      return (this.route.routeScenics || []).length;
    },
    totalTicketPrice() {
      const scenics = this.route.routeScenics || [];
      return scenics.reduce((sum, rs) => sum + (rs.scenic && rs.scenic.ticketPrice || 0), 0);
    }
  },
  onLoad(options) {
    this.routeId = options.id;
    this.getRouteDetail();
  },
  methods: {
    getRouteDetail() {
      common_vendor.index.request({
        url: BASE_URL + "/tour-route/" + this.routeId + "/detail",
        method: "GET",
        success: (res) => {
          this.route = res.data;
          this.loading = false;
        },
        fail: () => {
          common_vendor.index.showToast({ title: "加载失败", icon: "none" });
          this.loading = false;
        }
      });
    },
    goToScenic(scenicId) {
      if (scenicId) {
        common_vendor.index.navigateTo({ url: "/pages/detail/detail?id=" + scenicId });
      }
    },
    applyRoute() {
      const username = common_vendor.index.getStorageSync("loginUsername");
      if (!username) {
        common_vendor.index.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "添加中..." });
      common_vendor.index.request({
        url: BASE_URL + "/api/cart/add-route",
        method: "POST",
        data: { username, routeId: this.routeId },
        success: (res) => {
          common_vendor.index.hideLoading();
          common_vendor.index.showModal({
            title: "添加成功",
            content: "该路线所有景区门票已加入购物车",
            success: (confirmRes) => {
              if (confirmRes.confirm) {
                common_vendor.index.navigateTo({ url: "/pages/cart/cart" });
              }
            }
          });
        },
        fail: () => {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({ title: "添加失败", icon: "none" });
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
  return common_vendor.e({
    a: $data.loading
  }, $data.loading ? {} : common_vendor.e({
    b: common_vendor.t($data.route.name),
    c: common_vendor.t($data.route.price),
    d: common_vendor.t($data.route.days),
    e: $data.route.distance
  }, $data.route.distance ? {
    f: common_vendor.t($data.route.distance)
  } : {}, {
    g: $data.route.duration
  }, $data.route.duration ? {
    h: common_vendor.t($data.route.duration)
  } : {}, {
    i: $data.route.intro
  }, $data.route.intro ? {
    j: common_vendor.t($data.route.intro)
  } : {}, {
    k: !$data.route.routeScenics || $data.route.routeScenics.length === 0
  }, !$data.route.routeScenics || $data.route.routeScenics.length === 0 ? {} : {}, {
    l: common_vendor.f($options.dayGroups, (day, k0, i0) => {
      return {
        a: common_vendor.t(day.dayNumber),
        b: common_vendor.f(day.scenics, (rs, k1, i1) => {
          return {
            a: rs.scenic.photo ? rs.scenic.photo.startsWith("http") ? rs.scenic.photo : "http://localhost:8080" + rs.scenic.photo : "/static/home/6.jpg",
            b: common_vendor.t(rs.scenic.name),
            c: common_vendor.t(rs.scenic.description),
            d: common_vendor.t(rs.scenic.ticketPrice || 0),
            e: rs.id,
            f: common_vendor.o(($event) => $options.goToScenic(rs.scenic.id), rs.id)
          };
        }),
        c: day.dayNumber
      };
    }),
    m: $data.route.serviceInclude
  }, $data.route.serviceInclude ? {
    n: common_vendor.t($data.route.serviceInclude)
  } : {}, {
    o: $data.route.serviceExclude
  }, $data.route.serviceExclude ? {
    p: common_vendor.t($data.route.serviceExclude)
  } : {}, {
    q: $data.route.notice
  }, $data.route.notice ? {
    r: common_vendor.t($data.route.notice)
  } : {}, {
    s: common_vendor.t($options.totalScenicCount),
    t: common_vendor.t($options.totalTicketPrice),
    v: common_vendor.o((...args) => $options.applyRoute && $options.applyRoute(...args))
  }));
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-50e89ba1"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/route-detail/route-detail.js.map
