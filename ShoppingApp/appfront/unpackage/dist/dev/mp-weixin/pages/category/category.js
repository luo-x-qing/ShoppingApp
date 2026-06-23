"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      categoryList: ["全部酒店", "度假型酒店", "商务型酒店", "公寓型酒店", "连锁酒店"],
      activeTab: 0,
      hotelList: [],
      keyword: "",
      loading: false,
      // 搜索建议相关
      showSuggest: false,
      suggestList: [],
      // 位置相关
      showLocationPicker: false,
      searchKeyword: "",
      mapSuggestList: [],
      mapCenter: {
        latitude: 26.0822,
        longitude: 119.2955
      },
      markers: [],
      selectedLocation: null,
      nearbyPois: [],
      currentLocation: {
        name: "",
        lat: null,
        lng: null
      },
      // 排序相关
      currentSort: "",
      sortOrder: "asc",
      originalHotelList: [],
      // 滚动区域高度
      scrollHeight: 0
    };
  },
  onLoad() {
    this.getUserLocation();
    this.loadData();
    this.calcScrollHeight();
  },
  onReady() {
    this.calcScrollHeight();
  },
  // 点击其他地方关闭建议
  onPageTap() {
    this.showSuggest = false;
  },
  methods: {
    // 计算滚动区域高度
    calcScrollHeight() {
      const systemInfo = common_vendor.index.getSystemInfoSync();
      const windowHeight = systemInfo.windowHeight;
      const windowWidth = systemInfo.windowWidth;
      const rpxToPx = (rpx) => {
        return rpx * windowWidth / 750;
      };
      const totalHeaderHeight = rpxToPx(280);
      this.scrollHeight = windowHeight - totalHeaderHeight - 20;
      common_vendor.index.__f__("log", "at pages/category/category.vue:276", "滚动区域高度:", this.scrollHeight);
    },
    getUserLocation() {
      common_vendor.index.getLocation({
        type: "gcj02",
        success: (res) => {
          this.currentLocation = {
            name: "我的位置",
            lat: res.latitude,
            lng: res.longitude
          };
          this.loadData();
        },
        fail: () => {
          common_vendor.index.__f__("log", "at pages/category/category.vue:291", "获取位置失败");
        }
      });
    },
    // 加载全部酒店列表（过滤掉已停业的酒店）
    loadData() {
      this.loading = true;
      let cat = this.categoryList[this.activeTab];
      let url = "http://localhost:8080/api/hotels";
      if (cat !== "全部酒店")
        url += "/category/" + cat;
      common_vendor.index.__f__("log", "at pages/category/category.vue:303", "请求URL:", url);
      common_vendor.index.request({
        url,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/category/category.vue:309", "接口返回数据:", res.data);
          let hotelData = [];
          if (res.data && res.data.code === 200) {
            hotelData = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            hotelData = res.data;
          } else {
            hotelData = [];
          }
          const availableHotels = hotelData.filter((item) => item.status !== "已停业");
          this.originalHotelList = availableHotels.map((item) => {
            const distanceValue = this.calculateDistanceValue(item.latitude, item.longitude);
            return {
              id: item.id,
              name: item.name,
              starLevel: item.starLevel,
              price: item.price,
              category: item.category,
              rating: item.avgRating || 0,
              reviewCount: item.reviewCount || 0,
              latitude: item.latitude,
              longitude: item.longitude,
              image: this.getImageUrl(item.coverImage),
              distanceText: this.formatDistance(distanceValue),
              distanceValue
            };
          });
          this.applySort();
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/category/category.vue:344", "请求失败:", err);
          common_vendor.index.showToast({ title: "加载失败", icon: "none" });
        },
        complete: () => {
          this.loading = false;
        }
      });
    },
    // 实时搜索建议（输入时触发）- 只显示营业中的酒店
    onSearchInput(e) {
      const keyword = e.detail.value;
      this.keyword = keyword;
      if (!keyword.trim()) {
        this.suggestList = [];
        this.showSuggest = false;
        return;
      }
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotels/search",
        method: "GET",
        data: { keyword: keyword.trim() },
        success: (res) => {
          let hotelData = [];
          if (res.data && res.data.code === 200) {
            hotelData = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            hotelData = res.data;
          }
          const availableHotels = hotelData.filter((item) => item.status !== "已停业");
          this.suggestList = availableHotels.slice(0, 5).map((item) => ({
            id: item.id,
            name: item.name,
            category: item.category,
            price: item.price,
            starLevel: item.starLevel,
            image: this.getImageUrl(item.coverImage)
          }));
          this.showSuggest = this.suggestList.length > 0;
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/category/category.vue:392", "获取搜索建议失败:", err);
        }
      });
    },
    // 点击搜索按钮或回车
    onSearchConfirm() {
      this.showSuggest = false;
      if (this.keyword.trim()) {
        if (this.suggestList.length > 0) {
          this.goHotelDetail(this.suggestList[0].id, this.suggestList[0].name);
        } else {
          common_vendor.index.showToast({ title: "未找到相关酒店", icon: "none" });
        }
      }
    },
    // 跳转酒店详情页
    goHotelDetail(id, name) {
      this.showSuggest = false;
      common_vendor.index.navigateTo({
        url: "/pages/hotelDetail/hotelDetail?id=" + id
      });
    },
    calculateDistanceValue(lat, lng) {
      if (!this.currentLocation.lat || !this.currentLocation.lng || !lat || !lng) {
        return null;
      }
      const R = 6371;
      const dLat = this.toRad(lat - this.currentLocation.lat);
      const dLng = this.toRad(lng - this.currentLocation.lng);
      const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(this.toRad(this.currentLocation.lat)) * Math.cos(this.toRad(lat)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      const distance = R * c;
      return distance;
    },
    formatDistance(distanceKm) {
      if (distanceKm === null)
        return null;
      if (distanceKm < 1) {
        return `${Math.round(distanceKm * 1e3)}m`;
      }
      return `${distanceKm.toFixed(1)}km`;
    },
    toRad(deg) {
      return deg * Math.PI / 180;
    },
    getImageUrl(path) {
      if (!path)
        return "/static/default-hotel.png";
      if (path.startsWith("http"))
        return path;
      if (path.startsWith("/file"))
        return "http://localhost:8080" + path;
      if (path.startsWith("/uploads"))
        return "http://localhost:8080" + path;
      return "http://localhost:8080" + path;
    },
    applySort() {
      let sortedList = [...this.originalHotelList];
      if (this.currentSort === "price") {
        sortedList.sort((a, b) => {
          const priceA = a.price || 0;
          const priceB = b.price || 0;
          return this.sortOrder === "asc" ? priceA - priceB : priceB - priceA;
        });
      } else if (this.currentSort === "distance") {
        sortedList.sort((a, b) => {
          const distA = a.distanceValue !== null ? a.distanceValue : Infinity;
          const distB = b.distanceValue !== null ? b.distanceValue : Infinity;
          return this.sortOrder === "asc" ? distA - distB : distB - distA;
        });
      } else if (this.currentSort === "rating") {
        sortedList.sort((a, b) => {
          const ratingA = a.rating || 0;
          const ratingB = b.rating || 0;
          return this.sortOrder === "asc" ? ratingA - ratingB : ratingB - ratingA;
        });
      }
      this.hotelList = sortedList;
    },
    changeTab(index) {
      this.activeTab = index;
      this.loadData();
    },
    changeSort(type) {
      if (this.currentSort === type) {
        this.sortOrder = this.sortOrder === "asc" ? "desc" : "asc";
      } else {
        this.currentSort = type;
        this.sortOrder = "asc";
      }
      if (type === "distance" && (!this.currentLocation.lat || !this.currentLocation.lng)) {
        common_vendor.index.showModal({
          title: "提示",
          content: "请先选择您的位置",
          confirmText: "去选择",
          success: (res) => {
            if (res.confirm) {
              this.openLocationPicker();
            }
          }
        });
        return;
      }
      this.applySort();
    },
    openLocationPicker() {
      this.showLocationPicker = true;
      this.searchKeyword = "";
      this.mapSuggestList = [];
      this.nearbyPois = [];
      if (this.currentLocation.lat && this.currentLocation.lng) {
        this.mapCenter = {
          latitude: this.currentLocation.lat,
          longitude: this.currentLocation.lng
        };
        this.markers = [{
          id: Date.now(),
          latitude: this.currentLocation.lat,
          longitude: this.currentLocation.lng,
          title: "我的位置",
          width: 30,
          height: 30
        }];
        this.selectedLocation = {
          address: this.currentLocation.name,
          lat: this.currentLocation.lat,
          lng: this.currentLocation.lng
        };
      }
    },
    closeLocationPicker() {
      this.showLocationPicker = false;
    },
    onMapSearchInput(e) {
      const keyword = e.detail.value;
      this.searchKeyword = keyword;
      if (!keyword.trim()) {
        this.mapSuggestList = [];
        return;
      }
      common_vendor.index.request({
        url: "http://localhost:8080/api/map/search",
        method: "GET",
        data: { keyword },
        success: (res) => {
          if (res.data && res.data.success) {
            this.mapSuggestList = res.data.data || [];
          } else {
            this.mapSuggestList = [];
          }
        }
      });
    },
    searchLocation() {
      if (!this.searchKeyword.trim()) {
        common_vendor.index.showToast({ title: "请输入搜索关键词", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "搜索中..." });
      common_vendor.index.request({
        url: "http://localhost:8080/api/map/geocode",
        method: "GET",
        data: { address: this.searchKeyword },
        success: (res) => {
          common_vendor.index.hideLoading();
          if (res.data && res.data.success) {
            const { latitude, longitude, address } = res.data;
            this.mapCenter = { latitude, longitude };
            this.markers = [{
              id: Date.now(),
              latitude,
              longitude,
              title: address,
              width: 30,
              height: 30
            }];
            this.selectedLocation = { address, lat: latitude, lng: longitude };
            this.getNearbyAddresses(latitude, longitude);
          } else {
            common_vendor.index.showToast({ title: "未找到该地点", icon: "none" });
          }
        }
      });
    },
    selectSuggest(item) {
      if (item.latitude && item.longitude) {
        this.mapCenter = {
          latitude: item.latitude,
          longitude: item.longitude
        };
        this.markers = [{
          id: Date.now(),
          latitude: item.latitude,
          longitude: item.longitude,
          title: item.name,
          width: 30,
          height: 30
        }];
        this.selectedLocation = {
          address: item.address || item.name,
          lat: item.latitude,
          lng: item.longitude
        };
        this.mapSuggestList = [];
        this.getNearbyAddresses(item.latitude, item.longitude);
      } else {
        this.searchKeyword = item.name;
        this.mapSuggestList = [];
        this.searchLocation();
      }
    },
    onMapTap(e) {
      const { latitude, longitude } = e.detail;
      if (!latitude || !longitude)
        return;
      this.markers = [{
        id: Date.now(),
        latitude,
        longitude,
        title: "选中的位置",
        width: 30,
        height: 30
      }];
      this.getNearbyAddresses(latitude, longitude);
    },
    getNearbyAddresses(lat, lng) {
      common_vendor.index.request({
        url: "http://localhost:8080/api/map/reverse-geocode",
        method: "GET",
        data: { lat, lng },
        success: (res) => {
          if (res.data && res.data.success) {
            this.nearbyPois = res.data.nearbyPois || [];
            if (res.data.address) {
              this.selectedLocation = {
                address: res.data.address,
                lat,
                lng
              };
            }
          }
        }
      });
    },
    selectPoi(poi) {
      this.selectedLocation = {
        address: poi.address || poi.name,
        lat: this.mapCenter.latitude,
        lng: this.mapCenter.longitude
      };
      this.nearbyPois = [];
      common_vendor.index.showToast({ title: "已选择：" + poi.name, icon: "success" });
    },
    confirmLocation() {
      if (this.selectedLocation && this.selectedLocation.address) {
        this.currentLocation = {
          name: this.selectedLocation.address,
          lat: this.selectedLocation.lat,
          lng: this.selectedLocation.lng
        };
        common_vendor.index.showToast({ title: "位置已选择", icon: "success" });
        this.closeLocationPicker();
        this.originalHotelList = this.originalHotelList.map((item) => {
          const distanceValue = this.calculateDistanceValue(item.latitude, item.longitude);
          return {
            ...item,
            distanceText: this.formatDistance(distanceValue),
            distanceValue
          };
        });
        if (this.currentSort === "distance") {
          this.applySort();
        }
      } else {
        common_vendor.index.showToast({ title: "请先在地图上选择位置", icon: "none" });
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.t($data.currentLocation.name || "选择位置"),
    b: common_vendor.o((...args) => $options.openLocationPicker && $options.openLocationPicker(...args)),
    c: $data.currentSort === "price" && $data.sortOrder === "asc" ? 1 : "",
    d: $data.currentSort === "price" && $data.sortOrder === "desc" ? 1 : "",
    e: $data.currentSort === "price" ? 1 : "",
    f: common_vendor.o(($event) => $options.changeSort("price")),
    g: $data.currentSort === "distance" && $data.sortOrder === "asc" ? 1 : "",
    h: $data.currentSort === "distance" && $data.sortOrder === "desc" ? 1 : "",
    i: $data.currentSort === "distance" ? 1 : "",
    j: common_vendor.o(($event) => $options.changeSort("distance")),
    k: $data.currentSort === "rating" && $data.sortOrder === "asc" ? 1 : "",
    l: $data.currentSort === "rating" && $data.sortOrder === "desc" ? 1 : "",
    m: $data.currentSort === "rating" ? 1 : "",
    n: common_vendor.o(($event) => $options.changeSort("rating")),
    o: common_vendor.o([($event) => $data.keyword = $event.detail.value, (...args) => $options.onSearchInput && $options.onSearchInput(...args)]),
    p: common_vendor.o((...args) => $options.onSearchConfirm && $options.onSearchConfirm(...args)),
    q: common_vendor.o(($event) => $data.showSuggest = true),
    r: $data.keyword,
    s: common_vendor.o((...args) => $options.onSearchConfirm && $options.onSearchConfirm(...args)),
    t: $data.showSuggest && $data.suggestList.length > 0
  }, $data.showSuggest && $data.suggestList.length > 0 ? {
    v: common_vendor.f($data.suggestList, (item, k0, i0) => {
      return {
        a: common_vendor.t(item.name),
        b: common_vendor.t(item.category),
        c: common_vendor.t(item.price),
        d: item.id,
        e: common_vendor.o(($event) => $options.goHotelDetail(item.id, item.name), item.id)
      };
    })
  } : {}, {
    w: common_vendor.f($data.categoryList, (item, index, i0) => {
      return {
        a: common_vendor.t(item),
        b: index,
        c: $data.activeTab === index ? 1 : "",
        d: common_vendor.o(($event) => $options.changeTab(index), index)
      };
    }),
    x: common_vendor.f($data.hotelList, (item, k0, i0) => {
      return common_vendor.e({
        a: item.image,
        b: common_vendor.t(item.name),
        c: common_vendor.t(item.starLevel),
        d: common_vendor.t(item.category),
        e: item.distanceText
      }, item.distanceText ? {
        f: common_vendor.t(item.distanceText)
      } : {}, {
        g: item.rating > 0
      }, item.rating > 0 ? {
        h: common_vendor.t(item.rating)
      } : {}, {
        i: common_vendor.t(item.price),
        j: item.id,
        k: common_vendor.o(($event) => $options.goHotelDetail(item.id, item.name), item.id)
      });
    }),
    y: $data.hotelList.length === 0 && !$data.loading
  }, $data.hotelList.length === 0 && !$data.loading ? {} : {}, {
    z: $data.loading
  }, $data.loading ? {} : {}, {
    A: $data.scrollHeight + "px",
    B: $data.showLocationPicker
  }, $data.showLocationPicker ? common_vendor.e({
    C: common_vendor.o((...args) => $options.closeLocationPicker && $options.closeLocationPicker(...args)),
    D: common_vendor.o([($event) => $data.searchKeyword = $event.detail.value, (...args) => $options.onMapSearchInput && $options.onMapSearchInput(...args)]),
    E: common_vendor.o((...args) => $options.searchLocation && $options.searchLocation(...args)),
    F: $data.searchKeyword,
    G: common_vendor.o((...args) => $options.searchLocation && $options.searchLocation(...args)),
    H: $data.mapSuggestList.length > 0
  }, $data.mapSuggestList.length > 0 ? {
    I: common_vendor.f($data.mapSuggestList, (item, index, i0) => {
      return {
        a: common_vendor.t(item.name),
        b: common_vendor.t(item.address),
        c: index,
        d: common_vendor.o(($event) => $options.selectSuggest(item), index)
      };
    })
  } : {}, {
    J: $data.mapCenter.latitude,
    K: $data.mapCenter.longitude,
    L: $data.markers,
    M: common_vendor.o((...args) => $options.onMapTap && $options.onMapTap(...args)),
    N: $data.nearbyPois.length > 0
  }, $data.nearbyPois.length > 0 ? {
    O: common_vendor.o(($event) => $data.nearbyPois = []),
    P: common_vendor.f($data.nearbyPois, (poi, index, i0) => {
      return {
        a: common_vendor.t(poi.name),
        b: common_vendor.t(poi.address),
        c: index,
        d: common_vendor.o(($event) => $options.selectPoi(poi), index)
      };
    })
  } : {}, {
    Q: $data.selectedLocation
  }, $data.selectedLocation ? {
    R: common_vendor.t($data.selectedLocation.address)
  } : {}, {
    S: common_vendor.o((...args) => $options.closeLocationPicker && $options.closeLocationPicker(...args)),
    T: common_vendor.o((...args) => $options.confirmLocation && $options.confirmLocation(...args)),
    U: common_vendor.o(() => {
    }),
    V: common_vendor.o((...args) => $options.closeLocationPicker && $options.closeLocationPicker(...args))
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-8145b772"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/category/category.js.map
