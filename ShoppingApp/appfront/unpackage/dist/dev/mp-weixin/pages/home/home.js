"use strict";
const common_vendor = require("../../common/vendor.js");
const provinceCityMap = {
  "北京市": ["北京市"],
  "天津市": ["天津市"],
  "河北省": ["石家庄市", "唐山市", "秦皇岛市", "邯郸市", "保定市", "张家口市", "承德市"],
  "山西省": ["太原市", "大同市", "长治市", "晋城市", "晋中市", "运城市", "临汾市"],
  "内蒙古自治区": ["呼和浩特市", "包头市", "赤峰市", "鄂尔多斯市", "呼伦贝尔市"],
  "辽宁省": ["沈阳市", "大连市", "鞍山市", "抚顺市", "本溪市", "锦州市"],
  "吉林省": ["长春市", "吉林市", "四平市", "延边朝鲜族自治州"],
  "黑龙江省": ["哈尔滨市", "齐齐哈尔市", "牡丹江市", "佳木斯市", "大庆市"],
  "上海市": ["上海市"],
  "江苏省": ["南京市", "无锡市", "徐州市", "苏州市", "南通市", "扬州市", "镇江市", "常州市"],
  "浙江省": ["杭州市", "宁波市", "温州市", "嘉兴市", "湖州市", "绍兴市", "金华市", "台州市"],
  "安徽省": ["合肥市", "芜湖市", "蚌埠市", "黄山市", "六安市", "池州市"],
  "福建省": ["福州市", "厦门市", "莆田市", "泉州市", "漳州市", "南平市", "龙岩市"],
  "江西省": ["南昌市", "景德镇市", "九江市", "赣州市", "吉安市", "宜春市", "上饶市"],
  "山东省": ["济南市", "青岛市", "淄博市", "烟台市", "潍坊市", "济宁市", "泰安市", "威海市"],
  "河南省": ["郑州市", "开封市", "洛阳市", "安阳市", "焦作市", "南阳市", "信阳市"],
  "湖北省": ["武汉市", "黄石市", "宜昌市", "襄阳市", "荆州市", "十堰市", "恩施土家族苗族自治州"],
  "湖南省": ["长沙市", "株洲市", "湘潭市", "衡阳市", "张家界市", "岳阳市", "湘西土家族苗族自治州"],
  "广东省": ["广州市", "深圳市", "珠海市", "汕头市", "佛山市", "韶关市", "东莞市", "中山市", "惠州市"],
  "广西壮族自治区": ["南宁市", "柳州市", "桂林市", "北海市", "防城港市", "百色市"],
  "海南省": ["海口市", "三亚市", "三沙市"],
  "重庆市": ["重庆市"],
  "四川省": ["成都市", "自贡市", "攀枝花市", "泸州市", "德阳市", "绵阳市", "乐山市", "南充市", "宜宾市", "阿坝藏族羌族自治州", "甘孜藏族自治州"],
  "贵州省": ["贵阳市", "遵义市", "安顺市", "黔东南苗族侗族自治州", "黔南布依族苗族自治州"],
  "云南省": ["昆明市", "曲靖市", "保山市", "丽江市", "大理白族自治州", "迪庆藏族自治州"],
  "西藏自治区": ["拉萨市", "日喀则市", "林芝市"],
  "陕西省": ["西安市", "宝鸡市", "咸阳市", "渭南市", "延安市", "汉中市"],
  "甘肃省": ["兰州市", "嘉峪关市", "天水市", "张掖市", "酒泉市", "甘南藏族自治州"],
  "青海省": ["西宁市", "海东市"],
  "宁夏回族自治区": ["银川市", "石嘴山市", "吴忠市"],
  "新疆维吾尔自治区": ["乌鲁木齐市", "克拉玛依市", "吐鲁番市", "伊犁哈萨克自治州"],
  "香港特别行政区": ["香港"],
  "澳门特别行政区": ["澳门"],
  "台湾省": ["台北市", "高雄市", "台中市", "台南市"]
};
const _sfc_main = {
  data() {
    return {
      selectedProvince: "四川省",
      selectedProvinceIndex: 22,
      selectedCityIndex: 0,
      keyword: "",
      provinceNames: Object.keys(provinceCityMap),
      provinceCityMap,
      banners: [
        { url: "/static/home/6.jpg" },
        { url: "/static/home/7.jpg" },
        { url: "/static/home/8.jpg" },
        { url: "/static/home/9.jpg" },
        { url: "/static/home/10.jpg" }
      ],
      spots: [],
      collectedSpots: [],
      recommendedRoutes: []
    };
  },
  computed: {
    selectedCity() {
      return this.cityList[this.selectedCityIndex] || "全部城市";
    },
    cityList() {
      const cities = this.provinceCityMap[this.selectedProvince] || [];
      return ["全部城市", ...cities];
    }
  },
  onLoad() {
    const username = common_vendor.index.getStorageSync("loginUsername") || "";
    const key = "myCollection_" + username;
    let saved = common_vendor.index.getStorageSync(key);
    if (saved) {
      this.collectedSpots = saved;
    }
    this.getSpots();
    this.getRecommendedRoutes();
  },
  methods: {
    changeProvince(e) {
      const idx = e.detail.value;
      this.selectedProvince = this.provinceNames[idx];
      this.selectedProvinceIndex = idx;
      this.selectedCityIndex = 0;
      this.keyword = "";
      this.getSpots();
      this.getRecommendedRoutes();
    },
    changeCity(e) {
      this.selectedCityIndex = e.detail.value;
      this.keyword = "";
      this.getSpots();
    },
    getSpots() {
      const province = this.selectedProvince;
      const city = this.selectedCity;
      let url;
      if (city && city !== "全部城市") {
        url = "http://localhost:8080/api/attractions/province/" + encodeURIComponent(province) + "/city/" + encodeURIComponent(city);
      } else {
        url = "http://localhost:8080/api/attractions/province/" + encodeURIComponent(province);
      }
      common_vendor.index.request({
        url,
        method: "GET",
        success: (res) => {
          if (res.statusCode !== 200 || !Array.isArray(res.data)) {
            this.spots = [];
            return;
          }
          this.spots = res.data.map((item) => ({
            id: item.id,
            name: item.name,
            image: "http://localhost:8080" + item.photo,
            desc: item.description,
            city: item.city,
            isCollected: this.collectedSpots.some((i) => i.id === item.id)
          }));
        },
        fail: () => {
          this.spots = [];
        }
      });
    },
    searchSpot() {
      const kw = this.keyword.trim();
      if (!kw) {
        this.getSpots();
        return;
      }
      const province = this.selectedProvince;
      const city = this.selectedCity;
      let url;
      if (city && city !== "全部城市") {
        url = `http://localhost:8080/api/attractions/search?province=${encodeURIComponent(province)}&city=${encodeURIComponent(city)}&name=${encodeURIComponent(kw)}`;
      } else {
        url = `http://localhost:8080/api/attractions/search?province=${encodeURIComponent(province)}&name=${encodeURIComponent(kw)}`;
      }
      common_vendor.index.request({
        url,
        success: (res) => {
          if (res.statusCode !== 200 || !Array.isArray(res.data)) {
            this.spots = [];
            return;
          }
          this.spots = res.data.map((item) => ({
            id: item.id,
            name: item.name,
            image: "http://localhost:8080" + item.photo,
            desc: item.description,
            city: item.city,
            isCollected: this.collectedSpots.some((i) => i.id === item.id)
          }));
        },
        fail: () => {
          this.spots = [];
        }
      });
    },
    toggleCollect(spot) {
      const username = common_vendor.index.getStorageSync("loginUsername") || "";
      const key = "myCollection_" + username;
      let arr = JSON.parse(JSON.stringify(this.collectedSpots));
      const index = arr.findIndex((i) => i.id === spot.id);
      if (index > -1) {
        arr.splice(index, 1);
        common_vendor.index.showToast({ title: "取消收藏" });
      } else {
        arr.push(spot);
        common_vendor.index.showToast({ title: "收藏成功" });
      }
      this.collectedSpots = arr;
      common_vendor.index.setStorageSync(key, arr);
      this.getSpots();
    },
    goToDetail(id) {
      common_vendor.index.navigateTo({
        url: "/pages/detail/detail?id=" + id
      });
    },
    getRecommendedRoutes() {
      const province = this.selectedProvince;
      const city = this.selectedCity;
      let url = "http://localhost:8080/api/recommend/routes?province=" + encodeURIComponent(province);
      if (city && city !== "全部城市") {
        url += "&city=" + encodeURIComponent(city);
      }
      common_vendor.index.request({
        url,
        method: "GET",
        success: (res) => {
          if (res.statusCode === 200 && Array.isArray(res.data)) {
            this.recommendedRoutes = res.data;
          } else {
            this.recommendedRoutes = [];
          }
        },
        fail: () => {
          this.recommendedRoutes = [];
        }
      });
    },
    goToRouteDetail(id) {
      common_vendor.index.navigateTo({
        url: "/pages/route-detail/route-detail?id=" + id
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.searchSpot && $options.searchSpot(...args)),
    b: $data.keyword,
    c: common_vendor.o(($event) => $data.keyword = $event.detail.value),
    d: common_vendor.o((...args) => $options.searchSpot && $options.searchSpot(...args)),
    e: common_vendor.f($data.banners, (item, index, i0) => {
      return {
        a: item.url,
        b: index
      };
    }),
    f: common_vendor.t($data.selectedProvince),
    g: $data.selectedProvinceIndex,
    h: $data.provinceNames,
    i: common_vendor.o((...args) => $options.changeProvince && $options.changeProvince(...args)),
    j: common_vendor.t($options.selectedCity),
    k: $data.selectedCityIndex,
    l: $options.cityList,
    m: common_vendor.o((...args) => $options.changeCity && $options.changeCity(...args)),
    n: common_vendor.f($data.spots, (item, index, i0) => {
      return {
        a: item.image,
        b: common_vendor.t(item.name),
        c: common_vendor.t(item.isCollected ? "★ 已收藏" : "☆ 收藏"),
        d: common_vendor.o(($event) => $options.toggleCollect(item), index),
        e: index,
        f: common_vendor.o(($event) => $options.goToDetail(item.id), index)
      };
    }),
    o: $data.recommendedRoutes.length > 0
  }, $data.recommendedRoutes.length > 0 ? {
    p: common_vendor.t($options.selectedCity !== "全部城市" ? $options.selectedCity : $data.selectedProvince),
    q: common_vendor.f($data.recommendedRoutes, (route, index, i0) => {
      return common_vendor.e({
        a: common_vendor.t(route.name),
        b: common_vendor.t(route.price),
        c: common_vendor.t(route.intro),
        d: common_vendor.t(route.days),
        e: route.distance
      }, route.distance ? {
        f: common_vendor.t(route.distance)
      } : {}, {
        g: route.duration
      }, route.duration ? {
        h: common_vendor.t(route.duration)
      } : {}, {
        i: common_vendor.o(($event) => $options.goToRouteDetail(route.id), index),
        j: index
      });
    })
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-07e72d3c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/home.js.map
