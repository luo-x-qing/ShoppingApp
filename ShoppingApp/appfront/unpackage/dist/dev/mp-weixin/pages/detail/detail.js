"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      id: null,
      detail: {},
      images: [],
      comments: [],
      userScore: 5,
      commentContent: "",
      routePlanCount: 0
    };
  },
  onLoad(options) {
    this.id = options.id;
    this.getDetail();
    this.getComments();
    this.getRoutePlanCount();
  },
  onShow() {
    this.getRoutePlanCount();
  },
  methods: {
    getRoutePlanKey() {
      return "routePlan_" + (common_vendor.index.getStorageSync("loginUsername") || "default");
    },
    getRoutePlanCount() {
      const spots = common_vendor.index.getStorageSync(this.getRoutePlanKey());
      this.routePlanCount = Array.isArray(spots) ? spots.length : 0;
    },
    getDetail() {
      common_vendor.index.request({
        url: `http://localhost:8080/api/attractions/${this.id}`,
        success: (res) => {
          this.detail = res.data;
          if (res.data.photo) {
            this.images = [res.data.photo.startsWith("http") ? res.data.photo : "http://localhost:8080" + res.data.photo];
          }
          if (this.images.length === 0) {
            this.images = ["/static/home/6.jpg"];
          }
        }
      });
    },
    getComments() {
      common_vendor.index.request({
        url: `http://localhost:8080/api/comments/attraction/${this.id}`,
        success: (res) => {
          this.comments = res.data;
          this.calcAvgScore();
        }
      });
    },
    calcAvgScore() {
      if (this.comments.length === 0) {
        this.detail.score = 0;
        return;
      }
      const total = this.comments.reduce((sum, c) => sum + c.score, 0);
      const avg = total / this.comments.length;
      this.detail.score = avg;
      common_vendor.index.request({
        url: `http://localhost:8080/api/attractions/${this.id}/score`,
        method: "PUT",
        data: { score: avg }
      });
    },
    selectStar(num) {
      this.userScore = num;
    },
    addToRoutePlan() {
      const username = common_vendor.index.getStorageSync("loginUsername");
      if (!username) {
        common_vendor.index.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      const key = this.getRoutePlanKey();
      let spots = common_vendor.index.getStorageSync(key) || [];
      if (!Array.isArray(spots))
        spots = [];
      if (spots.some((s) => s.id == this.id)) {
        common_vendor.index.showToast({ title: "已在规划列表中", icon: "none" });
        return;
      }
      spots.push({
        id: this.id,
        name: this.detail.name,
        province: this.detail.province,
        city: this.detail.city,
        photo: this.detail.photo
      });
      common_vendor.index.setStorageSync(key, spots);
      this.routePlanCount = spots.length;
      common_vendor.index.showToast({ title: "已加入旅游线路规划" });
    },
    submitComment() {
      if (!this.commentContent.trim()) {
        common_vendor.index.showToast({ title: "请输入评价内容", icon: "none" });
        return;
      }
      common_vendor.index.request({
        url: "http://localhost:8080/api/comments",
        method: "POST",
        data: {
          attractionId: this.id,
          content: this.commentContent,
          score: this.userScore,
          userName: "匿名游客"
        },
        success: () => {
          common_vendor.index.showToast({ title: "评价提交成功" });
          this.commentContent = "";
          this.userScore = 5;
          this.getComments();
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
    a: common_vendor.f($data.images, (img, i, i0) => {
      return {
        a: img,
        b: i
      };
    }),
    b: common_vendor.t($data.detail.name),
    c: common_vendor.f(5, (n, k0, i0) => {
      return {
        a: common_vendor.t(n <= Math.round($data.detail.score || 0) ? "⭐" : "☆"),
        b: n
      };
    }),
    d: common_vendor.t(($data.detail.score || 0).toFixed(1)),
    e: common_vendor.t($data.detail.province),
    f: $data.detail.city
  }, $data.detail.city ? {
    g: common_vendor.t($data.detail.city)
  } : {}, {
    h: common_vendor.t($data.detail.type || "未分类"),
    i: common_vendor.t($data.detail.level || "未评级"),
    j: common_vendor.t($data.detail.openTime || "待确认"),
    k: common_vendor.t($data.detail.ticketPrice != null ? $data.detail.ticketPrice : "待确认"),
    l: common_vendor.t($data.detail.description),
    m: common_vendor.o((...args) => $options.addToRoutePlan && $options.addToRoutePlan(...args)),
    n: common_vendor.f(5, (n, k0, i0) => {
      return {
        a: common_vendor.t(n <= $data.userScore ? "⭐" : "☆"),
        b: n,
        c: common_vendor.o(($event) => $options.selectStar(n), n)
      };
    }),
    o: $data.commentContent,
    p: common_vendor.o(($event) => $data.commentContent = $event.detail.value),
    q: common_vendor.o((...args) => $options.submitComment && $options.submitComment(...args)),
    r: $data.comments.length === 0
  }, $data.comments.length === 0 ? {} : {}, {
    s: common_vendor.f($data.comments, (item, k0, i0) => {
      return {
        a: common_vendor.t(item.userName || "匿名游客"),
        b: common_vendor.f(5, (n, k1, i1) => {
          return {
            a: common_vendor.t(n <= item.score ? "⭐" : "☆"),
            b: n
          };
        }),
        c: common_vendor.t(item.content),
        d: common_vendor.t(item.createTime),
        e: item.id
      };
    })
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-eca06f3c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/detail/detail.js.map
