"use strict";
const common_vendor = require("../../common/vendor.js");
const BASE_URL = "http://localhost:8080";
const AMAP_KEY = "dde14364ce2a51865aa1cdc5af1598cf";
const PROVINCE_CENTERS = {
  "北京市": [39.9042, 116.4074],
  "上海市": [31.2304, 121.4737],
  "天津市": [39.1252, 117.1908],
  "重庆市": [29.533, 106.5048],
  "四川省": [30.5728, 104.0668],
  "广东省": [23.1292, 113.2644],
  "浙江省": [30.2741, 120.1551],
  "福建省": [26.0745, 119.2965],
  "江苏省": [32.0617, 118.7969],
  "湖北省": [30.5465, 114.3423],
  "湖南省": [28.2569, 112.941],
  "山东省": [36.6516, 117.0204],
  "河南省": [34.7656, 113.7532],
  "河北省": [38.0455, 114.502],
  "陕西省": [34.2655, 108.9542]
};
const _sfc_main = {
  data() {
    return {
      id: null,
      detail: {},
      images: [],
      comments: [],
      userScore: 5,
      commentContent: "",
      centerLat: null,
      centerLng: null,
      currentLat: null,
      currentLng: null,
      markers: [],
      nearbyList: [],
      nearbyMarkers: [],
      sheetOffset: 0,
      maxOffset: 0,
      dragStartY: 0,
      dragBaseOffset: 0,
      isDragging: false
    };
  },
  onLoad(options) {
    this.id = options.id;
    this.calcSheetHeight();
    this.loadData();
  },
  onReady() {
    this.calcSheetHeight();
  },
  methods: {
    calcSheetHeight() {
      const sys = common_vendor.index.getSystemInfoSync();
      const sheetH = sys.windowHeight * 0.75;
      const peekH = 140 * sys.windowWidth / 750;
      this.maxOffset = Math.max(sheetH - peekH, 200);
      this.sheetOffset = this.maxOffset;
    },
    toggleSheet() {
      this.sheetOffset = this.sheetOffset >= this.maxOffset - 10 ? 0 : this.maxOffset;
    },
    onTouchStart(e) {
      this.dragStartY = e.touches[0].clientY;
      this.dragBaseOffset = this.sheetOffset;
      this.isDragging = true;
    },
    onTouchMove(e) {
      if (!this.isDragging)
        return;
      const delta = this.dragStartY - e.touches[0].clientY;
      this.sheetOffset = Math.max(0, Math.min(this.maxOffset, this.dragBaseOffset - delta));
    },
    onTouchEnd() {
      this.isDragging = false;
      const mid = this.maxOffset / 2;
      this.sheetOffset = this.sheetOffset > mid ? this.maxOffset : 0;
    },
    loadData() {
      this.getDetail();
      this.getComments();
    },
    getDetail() {
      common_vendor.index.request({
        url: `${BASE_URL}/api/attractions/${this.id}`,
        success: (res) => {
          this.detail = res.data;
          this.setupMap();
          this.getNearby();
          if (res.data.photo) {
            this.images = [res.data.photo.startsWith("http") ? res.data.photo : BASE_URL + res.data.photo];
          }
          if (this.images.length === 0) {
            this.images = ["/static/home/6.jpg"];
          }
        }
      });
    },
    setupMap() {
      const d = this.detail;
      if (d.latitude && d.longitude) {
        this.centerLat = d.latitude;
        this.centerLng = d.longitude;
        this.setCurrentCoords(d.latitude, d.longitude);
      } else {
        this.geocodeAndSetup(d);
      }
    },
    geocodeAndSetup(d) {
      const address = d.city ? d.city + d.name : d.province ? d.province + d.name : d.name;
      common_vendor.index.__f__("log", "at pages/detail/detail.vue:225", "[高德] 开始地理编码:", address);
      common_vendor.index.request({
        url: "https://restapi.amap.com/v3/geocode/geo",
        data: {
          key: AMAP_KEY,
          address,
          city: d.city || "",
          extensions: "all",
          s: "rsx",
          platform: "WXJS",
          appname: AMAP_KEY,
          sdkversion: "1.2.0",
          logversion: "2.0"
        },
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/detail/detail.vue:240", "[高德] 返回:", JSON.stringify(res.data));
          const data = res.data;
          if (data && data.status === "1" && data.geocodes && data.geocodes.length > 0) {
            const loc = data.geocodes[0].location.split(",");
            const lng = parseFloat(loc[0]);
            const lat = parseFloat(loc[1]);
            common_vendor.index.__f__("log", "at pages/detail/detail.vue:246", "[高德] 成功获取坐标:", lat, lng);
            this.centerLat = lat;
            this.centerLng = lng;
            this.setCurrentCoords(lat, lng);
            common_vendor.index.request({
              url: `${BASE_URL}/api/attractions/${this.id}`,
              method: "PUT",
              data: { ...this.detail, latitude: lat, longitude: lng }
            });
          } else {
            common_vendor.index.__f__("warn", "at pages/detail/detail.vue:256", "[高德] 地理编码无结果:", data);
            this.useProvinceFallback(d);
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/detail/detail.vue:261", "[高德] 请求失败:", err);
          this.useProvinceFallback(d);
        }
      });
    },
    useProvinceFallback(d) {
      if (d.province && PROVINCE_CENTERS[d.province]) {
        const c = PROVINCE_CENTERS[d.province];
        this.centerLat = c[0];
        this.centerLng = c[1];
        this.setCurrentCoords(c[0], c[1]);
      }
    },
    setCurrentCoords(lat, lng) {
      this.currentLat = lat;
      this.currentLng = lng;
      this.renderMarkers();
    },
    renderMarkers() {
      const currentMarker = this.currentLat && this.currentLng ? {
        id: 0,
        latitude: this.currentLat,
        longitude: this.currentLng,
        title: this.detail.name,
        label: {
          content: this.detail.name,
          color: "#fff",
          fontSize: 13,
          borderRadius: 6,
          bgColor: "#1677ff",
          padding: 6,
          textAlign: "center"
        },
        width: 36,
        height: 36
      } : null;
      this.markers = currentMarker ? [currentMarker, ...this.nearbyMarkers] : this.nearbyMarkers;
    },
    getNearby() {
      common_vendor.index.request({
        url: `${BASE_URL}/api/attractions/${this.id}/nearby?radius=50`,
        success: (res) => {
          const data = Array.isArray(res.data) ? res.data : [];
          this.nearbyList = data;
          this.nearbyMarkers = this.nearbyList.map((item, i) => ({
            id: i + 1,
            latitude: item.latitude,
            longitude: item.longitude,
            title: item.name,
            label: {
              content: `${item.name} (${item.distance}km)`,
              color: "#333",
              fontSize: 11,
              borderRadius: 4,
              bgColor: "rgba(255,255,255,0.9)",
              padding: 4,
              textAlign: "center"
            },
            width: 24,
            height: 24
          }));
          this.renderMarkers();
        },
        fail: () => {
          this.nearbyList = [];
          this.nearbyMarkers = [];
          this.renderMarkers();
        }
      });
    },
    onMarkerTap(e) {
      const markerId = e.detail.markerId;
      if (markerId === 0)
        return;
      const item = this.nearbyList[markerId - 1];
      if (item)
        this.goNearby(item);
    },
    goNearby(item) {
      common_vendor.index.navigateTo({ url: `/pages/detail/detail?id=${item.id}` });
    },
    getComments() {
      common_vendor.index.request({
        url: `${BASE_URL}/api/comments/attraction/${this.id}`,
        success: (res) => {
          this.comments = res.data || [];
          this.calcAvgScore();
        }
      });
    },
    calcAvgScore() {
      if (this.comments.length === 0) {
        this.detail.score = 0;
        return;
      }
      const total = this.comments.reduce((s, c) => s + c.score, 0);
      const avg = total / this.comments.length;
      this.detail.score = avg;
      common_vendor.index.request({
        url: `${BASE_URL}/api/attractions/${this.id}/score`,
        method: "PUT",
        data: { score: avg }
      });
    },
    selectStar(n) {
      this.userScore = n;
    },
    addToRoutePlan() {
      const username = common_vendor.index.getStorageSync("loginUsername");
      if (!username) {
        common_vendor.index.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      const key = "routePlan_" + username;
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
      common_vendor.index.showToast({ title: "已加入旅游线路规划" });
    },
    submitComment() {
      if (!this.commentContent.trim()) {
        common_vendor.index.showToast({ title: "请输入评价内容", icon: "none" });
        return;
      }
      common_vendor.index.request({
        url: `${BASE_URL}/api/comments`,
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
    a: $data.centerLat,
    b: $data.centerLng,
    c: $data.markers,
    d: common_vendor.o((...args) => $options.onMarkerTap && $options.onMarkerTap(...args)),
    e: common_vendor.o((...args) => $options.toggleSheet && $options.toggleSheet(...args)),
    f: common_vendor.t($data.detail.name || "景点详情"),
    g: common_vendor.t(($data.detail.score || 0).toFixed(1)),
    h: common_vendor.o((...args) => $options.addToRoutePlan && $options.addToRoutePlan(...args)),
    i: common_vendor.f($data.images, (img, i, i0) => {
      return {
        a: img,
        b: i
      };
    }),
    j: common_vendor.t($data.detail.province),
    k: common_vendor.t($data.detail.city),
    l: common_vendor.t($data.detail.type || "未分类"),
    m: common_vendor.t($data.detail.level || "未评级"),
    n: common_vendor.t($data.detail.openTime || "待确认"),
    o: common_vendor.t($data.detail.ticketPrice != null ? $data.detail.ticketPrice : "待确认"),
    p: common_vendor.t($data.detail.description || "暂无介绍"),
    q: common_vendor.o((...args) => $options.addToRoutePlan && $options.addToRoutePlan(...args)),
    r: common_vendor.t($data.nearbyList.length),
    s: $data.nearbyList.length === 0
  }, $data.nearbyList.length === 0 ? {} : {}, {
    t: common_vendor.f($data.nearbyList, (item, k0, i0) => {
      return {
        a: common_vendor.t(item.name),
        b: common_vendor.t(item.distance),
        c: common_vendor.t(item.score || "暂无"),
        d: item.id,
        e: common_vendor.o(($event) => $options.goNearby(item), item.id)
      };
    }),
    v: common_vendor.f(5, (n, k0, i0) => {
      return {
        a: common_vendor.t(n <= $data.userScore ? "⭐" : "☆"),
        b: n,
        c: common_vendor.o(($event) => $options.selectStar(n), n)
      };
    }),
    w: $data.commentContent,
    x: common_vendor.o(($event) => $data.commentContent = $event.detail.value),
    y: common_vendor.o((...args) => $options.submitComment && $options.submitComment(...args)),
    z: $data.comments.length === 0
  }, $data.comments.length === 0 ? {} : {}, {
    A: common_vendor.f($data.comments, (item, k0, i0) => {
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
    }),
    B: $data.isDragging ? 1 : "",
    C: common_vendor.s(`transform: translateY(${$data.sheetOffset}px)`),
    D: common_vendor.o((...args) => $options.onTouchStart && $options.onTouchStart(...args)),
    E: common_vendor.o((...args) => $options.onTouchMove && $options.onTouchMove(...args)),
    F: common_vendor.o((...args) => $options.onTouchEnd && $options.onTouchEnd(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-eca06f3c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/detail/detail.js.map
