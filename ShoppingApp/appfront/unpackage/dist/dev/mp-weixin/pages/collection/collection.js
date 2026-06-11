"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      tab: 0,
      hotelList: [],
      spotList: [],
      username: ""
    };
  },
  onShow() {
    this.username = common_vendor.index.getStorageSync("loginUsername") || "";
    this.loadCollection();
  },
  methods: {
    loadCollection() {
      const key = "myCollection_" + this.username;
      let data = common_vendor.index.getStorageSync(key) || [];
      this.hotelList = data.filter((i) => i.price !== void 0);
      this.spotList = data.filter((i) => i.price === void 0);
    },
    deleteHotel(idx) {
      const key = "myCollection_" + this.username;
      let all = common_vendor.index.getStorageSync(key) || [];
      let target = this.hotelList[idx];
      let result = all.filter((i) => i.id !== target.id);
      common_vendor.index.setStorageSync(key, result);
      this.loadCollection();
      common_vendor.index.showToast({ title: "已删除" });
    },
    deleteSpot(idx) {
      const key = "myCollection_" + this.username;
      let all = common_vendor.index.getStorageSync(key) || [];
      let target = this.spotList[idx];
      let result = all.filter((i) => i.id !== target.id);
      common_vendor.index.setStorageSync(key, result);
      this.loadCollection();
      common_vendor.index.showToast({ title: "已删除" });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.n($data.tab === 0 ? "active" : ""),
    b: common_vendor.o(($event) => $data.tab = 0),
    c: common_vendor.n($data.tab === 1 ? "active" : ""),
    d: common_vendor.o(($event) => $data.tab = 1),
    e: $data.tab === 0
  }, $data.tab === 0 ? common_vendor.e({
    f: $data.hotelList.length === 0
  }, $data.hotelList.length === 0 ? {} : {}, {
    g: common_vendor.f($data.hotelList, (item, index, i0) => {
      return {
        a: item.image,
        b: common_vendor.t(item.name),
        c: common_vendor.t(item.price),
        d: common_vendor.o(($event) => $options.deleteHotel(index), index),
        e: index
      };
    })
  }) : {}, {
    h: $data.tab === 1
  }, $data.tab === 1 ? common_vendor.e({
    i: $data.spotList.length === 0
  }, $data.spotList.length === 0 ? {} : {}, {
    j: common_vendor.f($data.spotList, (item, index, i0) => {
      return {
        a: item.image,
        b: common_vendor.t(item.name),
        c: common_vendor.o(($event) => $options.deleteSpot(index), index),
        d: index
      };
    })
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-cd17183b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/collection/collection.js.map
