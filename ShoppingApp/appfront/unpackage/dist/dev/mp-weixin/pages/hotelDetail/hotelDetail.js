"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      id: null,
      hotel: {},
      images: [],
      commentList: [],
      isCollected: false,
      username: "",
      avgScore: 4.8
    };
  },
  onLoad(options) {
    this.id = options.id;
    this.username = common_vendor.index.getStorageSync("loginUsername");
    this.getHotel();
    this.getComments();
    this.checkCollect();
  },
  methods: {
    // 计算酒店平均分
    calcAvgScore() {
      if (this.commentList.length === 0) {
        this.avgScore = 4.8;
        return;
      }
      let total = 0;
      this.commentList.forEach((item) => {
        total += item.score || 5;
      });
      let avg = total / this.commentList.length;
      this.avgScore = avg.toFixed(1);
    },
    getHotel() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotels/" + this.id,
        success: (res) => {
          this.hotel = res.data;
          this.images = res.data.detailImages ? res.data.detailImages.map((i) => "http://localhost:8080" + i.imageUrl) : [];
        }
      });
    },
    getComments() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotel-comments/hotel/" + this.id,
        success: (res) => {
          this.commentList = res.data;
          this.calcAvgScore();
        }
      });
    },
    goToBook() {
      common_vendor.index.navigateTo({
        url: "/pages/booking/booking?id=" + this.id + "&name=" + this.hotel.name + "&price=" + this.hotel.price + "&username=" + this.username
      });
    },
    checkCollect() {
      const key = "myCollection_" + this.username;
      let data = common_vendor.index.getStorageSync(key) || [];
      this.isCollected = data.some((item) => item.id === this.hotel.id);
    },
    toggleCollect() {
      const key = "myCollection_" + this.username;
      let data = common_vendor.index.getStorageSync(key) || [];
      const index = data.findIndex((item) => item.id === this.hotel.id);
      if (index > -1) {
        data.splice(index, 1);
        this.isCollected = false;
        common_vendor.index.showToast({ title: "取消收藏" });
      } else {
        data.push(this.hotel);
        this.isCollected = true;
        common_vendor.index.showToast({ title: "收藏成功" });
      }
      common_vendor.index.setStorageSync(key, data);
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.f($data.images, (img, k0, i0) => {
      return {
        a: img,
        b: img
      };
    }),
    b: common_vendor.t($data.isCollected ? "★" : "☆"),
    c: common_vendor.o((...args) => $options.toggleCollect && $options.toggleCollect(...args)),
    d: common_vendor.t($data.hotel.name),
    e: common_vendor.t($data.hotel.star || 5),
    f: common_vendor.t($data.avgScore),
    g: common_vendor.t($data.hotel.price),
    h: common_vendor.t($data.hotel.address || "暂无地址"),
    i: common_vendor.o((...args) => $options.goToBook && $options.goToBook(...args)),
    j: common_vendor.f($data.commentList, (item, k0, i0) => {
      return {
        a: common_vendor.t(item.score),
        b: common_vendor.t(item.content),
        c: common_vendor.t(item.createTime),
        d: item.id
      };
    }),
    k: $data.commentList.length === 0
  }, $data.commentList.length === 0 ? {} : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-d2073f73"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/hotelDetail/hotelDetail.js.map
