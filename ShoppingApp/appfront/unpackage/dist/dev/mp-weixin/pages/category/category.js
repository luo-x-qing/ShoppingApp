"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      categoryList: ["全部酒店", "度假型酒店", "商务型酒店", "公寓型酒店", "连锁酒店"],
      activeTab: 0,
      hotelList: [],
      keyword: ""
    };
  },
  onLoad() {
    this.loadData();
  },
  methods: {
    loadData() {
      let cat = this.categoryList[this.activeTab];
      let url = "http://localhost:8080/api/hotels";
      if (cat !== "全部酒店")
        url += "/category/" + cat;
      common_vendor.index.request({
        url,
        success: (res) => {
          this.hotelList = res.data.map((i) => ({
            id: i.id,
            name: i.name,
            starLevel: i.starLevel,
            price: i.price,
            category: i.category,
            image: "http://localhost:8080" + i.coverImage
          }));
        }
      });
    },
    changeTab(index) {
      this.activeTab = index;
      this.loadData();
    },
    searchHotel() {
      if (!this.keyword)
        return this.loadData();
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotels",
        success: (res) => {
          this.hotelList = res.data.filter((i) => i.name.includes(this.keyword)).map((i) => ({
            id: i.id,
            name: i.name,
            starLevel: i.starLevel,
            price: i.price,
            category: i.category,
            image: "http://localhost:8080" + i.coverImage
          }));
        }
      });
    },
    goHotelDetail(id) {
      common_vendor.index.navigateTo({
        url: "/pages/hotelDetail/hotelDetail?id=" + id
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o((...args) => $options.searchHotel && $options.searchHotel(...args)),
    b: $data.keyword,
    c: common_vendor.o(($event) => $data.keyword = $event.detail.value),
    d: common_vendor.o((...args) => $options.searchHotel && $options.searchHotel(...args)),
    e: common_vendor.f($data.categoryList, (item, index, i0) => {
      return {
        a: common_vendor.t(item),
        b: index,
        c: $data.activeTab === index ? 1 : "",
        d: common_vendor.o(($event) => $options.changeTab(index), index)
      };
    }),
    f: common_vendor.f($data.hotelList, (item, k0, i0) => {
      return {
        a: item.image,
        b: common_vendor.t(item.name),
        c: common_vendor.t(item.starLevel),
        d: common_vendor.t(item.category),
        e: common_vendor.t(item.price),
        f: item.id,
        g: common_vendor.o(($event) => $options.goHotelDetail(item.id), item.id)
      };
    })
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-8145b772"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/category/category.js.map
