"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      tab: 0,
      hotelList: [],
      spotList: [],
      username: "",
      refreshing: false,
      baseUrl: "http://localhost:8080",
      scrollHeight: "calc(100vh - 200rpx)",
      defaultHotelImage: "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 200 150'%3E%3Crect width='200' height='150' fill='%23e0e0e0'/%3E%3Ctext x='100' y='75' text-anchor='middle' fill='%23999' font-size='14' dy='.3em'%3E🏨 暂无图片%3C/text%3E%3C/svg%3E",
      defaultSpotImage: "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 200 150'%3E%3Crect width='200' height='150' fill='%23e0e0e0'/%3E%3Ctext x='100' y='75' text-anchor='middle' fill='%23999' font-size='14' dy='.3em'%3E🏛️ 暂无图片%3C/text%3E%3C/svg%3E"
    };
  },
  onLoad() {
    this.getScreenHeight();
  },
  onShow() {
    this.username = common_vendor.index.getStorageSync("loginUsername") || "";
    this.loadCollection();
  },
  methods: {
    getScreenHeight() {
      try {
        const windowInfo = common_vendor.index.getWindowInfo();
        const screenHeight = windowInfo.windowHeight;
        this.scrollHeight = screenHeight - 200 + "px";
      } catch (e) {
        this.scrollHeight = "calc(100vh - 200rpx)";
      }
    },
    getHotelImage(item) {
      const url = (item == null ? void 0 : item.coverImage) || (item == null ? void 0 : item.image);
      if (!url)
        return this.defaultHotelImage;
      if (url.startsWith("data:"))
        return url;
      if (url.startsWith("http"))
        return url;
      if (url.startsWith("/"))
        return this.baseUrl + url;
      return this.baseUrl + "/" + url;
    },
    getSpotImage(item) {
      const url = (item == null ? void 0 : item.coverImage) || (item == null ? void 0 : item.image) || (item == null ? void 0 : item.picUrl);
      if (!url)
        return this.defaultSpotImage;
      if (url.startsWith("data:"))
        return url;
      if (url.startsWith("http"))
        return url;
      if (url.startsWith("/"))
        return this.baseUrl + url;
      return this.baseUrl + "/" + url;
    },
    switchTab(index) {
      this.tab = index;
    },
    loadCollection() {
      const key = "myCollection_" + this.username;
      let data = common_vendor.index.getStorageSync(key) || [];
      this.hotelList = data.filter((i) => i && i.price !== void 0 && i.price !== null);
      this.spotList = data.filter((i) => i && (i.price === void 0 || i.price === null));
      common_vendor.index.__f__("log", "at pages/collection/collection.vue:192", "加载收藏完成 - 酒店:", this.hotelList.length, "景点:", this.spotList.length);
    },
    deleteHotel(idx) {
      if (idx < 0 || idx >= this.hotelList.length) {
        common_vendor.index.__f__("error", "at pages/collection/collection.vue:197", "无效的酒店索引:", idx);
        common_vendor.index.showToast({ title: "删除失败", icon: "none" });
        return;
      }
      const target = this.hotelList[idx];
      if (!target || !target.id) {
        common_vendor.index.__f__("error", "at pages/collection/collection.vue:204", "酒店数据无效:", target);
        common_vendor.index.showToast({ title: "数据错误", icon: "none" });
        return;
      }
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要删除这个酒店收藏吗？",
        success: (res) => {
          if (res.confirm) {
            const key = "myCollection_" + this.username;
            let all = common_vendor.index.getStorageSync(key) || [];
            let result = all.filter((i) => i && i.id !== target.id);
            common_vendor.index.setStorageSync(key, result);
            this.loadCollection();
            common_vendor.index.showToast({ title: "已删除", icon: "success" });
          }
        }
      });
    },
    deleteSpot(idx) {
      if (idx < 0 || idx >= this.spotList.length) {
        common_vendor.index.__f__("error", "at pages/collection/collection.vue:227", "无效的景点索引:", idx);
        common_vendor.index.showToast({ title: "删除失败", icon: "none" });
        return;
      }
      const target = this.spotList[idx];
      if (!target || !target.id) {
        common_vendor.index.__f__("error", "at pages/collection/collection.vue:234", "景点数据无效:", target);
        common_vendor.index.showToast({ title: "数据错误", icon: "none" });
        return;
      }
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要删除这个景点收藏吗？",
        success: (res) => {
          if (res.confirm) {
            const key = "myCollection_" + this.username;
            let all = common_vendor.index.getStorageSync(key) || [];
            let result = all.filter((i) => i && i.id !== target.id);
            common_vendor.index.setStorageSync(key, result);
            this.loadCollection();
            common_vendor.index.showToast({ title: "已删除", icon: "success" });
          }
        }
      });
    },
    viewHotelDetail(item) {
      common_vendor.index.__f__("log", "at pages/collection/collection.vue:256", "跳转酒店详情，ID:", item == null ? void 0 : item.id, "名称:", item == null ? void 0 : item.name);
      if (!item || !item.id) {
        common_vendor.index.showToast({ title: "酒店信息错误", icon: "none" });
        return;
      }
      common_vendor.index.navigateTo({
        url: `/pages/hotelDetail/hotelDetail?id=${item.id}`,
        success: () => {
          common_vendor.index.__f__("log", "at pages/collection/collection.vue:266", "跳转酒店详情成功");
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/collection/collection.vue:269", "跳转酒店详情失败:", err);
          common_vendor.index.showToast({
            title: "页面跳转失败，请检查路径",
            icon: "none",
            duration: 2e3
          });
        }
      });
    },
    viewSpotDetail(item) {
      common_vendor.index.__f__("log", "at pages/collection/collection.vue:280", "跳转景点详情，ID:", item == null ? void 0 : item.id, "名称:", item == null ? void 0 : item.name);
      if (!item || !item.id) {
        common_vendor.index.showToast({ title: "景点信息错误", icon: "none" });
        return;
      }
      common_vendor.index.navigateTo({
        url: `/pages/detail/detail?id=${item.id}`,
        success: () => {
          common_vendor.index.__f__("log", "at pages/collection/collection.vue:290", "跳转景点详情成功");
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/collection/collection.vue:293", "跳转景点详情失败:", err);
          common_vendor.index.showToast({
            title: "页面跳转失败，请检查路径",
            icon: "none",
            duration: 2e3
          });
        }
      });
    },
    goExploreHotel() {
      common_vendor.index.switchTab({
        url: "/pages/category/category"
      });
    },
    goExploreSpot() {
      common_vendor.index.switchTab({
        url: "/pages/home/home"
      });
    },
    onRefresh() {
      this.refreshing = true;
      this.loadCollection();
      setTimeout(() => {
        this.refreshing = false;
        common_vendor.index.showToast({ title: "刷新成功", icon: "success" });
      }, 1e3);
    }
  }
};
if (!Array) {
  const _component_RouteFloat = common_vendor.resolveComponent("RouteFloat");
  _component_RouteFloat();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.hotelList.length
  }, $data.hotelList.length ? {
    b: common_vendor.t($data.hotelList.length)
  } : {}, {
    c: common_vendor.n($data.tab === 0 ? "active" : ""),
    d: common_vendor.o(($event) => $options.switchTab(0)),
    e: $data.spotList.length
  }, $data.spotList.length ? {
    f: common_vendor.t($data.spotList.length)
  } : {}, {
    g: common_vendor.n($data.tab === 1 ? "active" : ""),
    h: common_vendor.o(($event) => $options.switchTab(1)),
    i: $data.tab === 0
  }, $data.tab === 0 ? common_vendor.e({
    j: $data.hotelList.length === 0
  }, $data.hotelList.length === 0 ? {
    k: common_vendor.o((...args) => $options.goExploreHotel && $options.goExploreHotel(...args))
  } : {}, {
    l: common_vendor.f($data.hotelList, (item, index, i0) => {
      return common_vendor.e({
        a: $options.getHotelImage(item),
        b: item.starLevel
      }, item.starLevel ? {
        c: common_vendor.t(item.starLevel)
      } : {}, {
        d: common_vendor.t(item.name),
        e: common_vendor.o(($event) => $options.deleteHotel(index), item.id || index),
        f: item.address
      }, item.address ? {
        g: common_vendor.t(item.address)
      } : {}, {
        h: common_vendor.t(item.price),
        i: item.avgRating
      }, item.avgRating ? {
        j: common_vendor.t(item.avgRating)
      } : {}, {
        k: item.id || index,
        l: common_vendor.o(($event) => $options.viewHotelDetail(item), item.id || index)
      });
    })
  }) : {}, {
    m: $data.tab === 1
  }, $data.tab === 1 ? common_vendor.e({
    n: $data.spotList.length === 0
  }, $data.spotList.length === 0 ? {
    o: common_vendor.o((...args) => $options.goExploreSpot && $options.goExploreSpot(...args))
  } : {}, {
    p: common_vendor.f($data.spotList, (item, index, i0) => {
      return common_vendor.e({
        a: $options.getSpotImage(item),
        b: common_vendor.t(item.name),
        c: common_vendor.o(($event) => $options.deleteSpot(index), item.id || index),
        d: item.address
      }, item.address ? {
        e: common_vendor.t(item.address)
      } : {}, {
        f: item.category
      }, item.category ? {
        g: common_vendor.t(item.category)
      } : {}, {
        h: item.id || index,
        i: common_vendor.o(($event) => $options.viewSpotDetail(item), item.id || index)
      });
    })
  }) : {}, {
    q: $data.scrollHeight,
    r: common_vendor.o((...args) => $options.onRefresh && $options.onRefresh(...args)),
    s: $data.refreshing
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-cd17183b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/collection/collection.js.map
