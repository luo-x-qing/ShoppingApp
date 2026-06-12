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
const cityPinyinMap = {
  "北京市": "beijing",
  "天津市": "tianjin",
  "上海市": "shanghai",
  "重庆市": "chongqing",
  "石家庄市": "shijiazhuang",
  "唐山市": "tangshan",
  "秦皇岛市": "qinhuangdao",
  "邯郸市": "handan",
  "保定市": "baoding",
  "张家口市": "zhangjiakou",
  "承德市": "chengde",
  "太原市": "taiyuan",
  "大同市": "datong",
  "长治市": "changzhi",
  "晋城市": "jincheng",
  "晋中市": "jinzhong",
  "运城市": "yuncheng",
  "临汾市": "linfen",
  "呼和浩特市": "huhehaote",
  "包头市": "baotou",
  "赤峰市": "chifeng",
  "鄂尔多斯市": "eerduosi",
  "呼伦贝尔市": "hulunbeier",
  "沈阳市": "shenyang",
  "大连市": "dalian",
  "鞍山市": "anshan",
  "抚顺市": "fushun",
  "本溪市": "benxi",
  "锦州市": "jinzhou",
  "长春市": "changchun",
  "吉林市": "jilin",
  "四平市": "siping",
  "延边朝鲜族自治州": "yanbian",
  "哈尔滨市": "haerbin",
  "齐齐哈尔市": "qiqihaer",
  "牡丹江市": "mudanjiang",
  "佳木斯市": "jiamusi",
  "大庆市": "daqing",
  "南京市": "nanjing",
  "无锡市": "wuxi",
  "徐州市": "xuzhou",
  "苏州市": "suzhou",
  "南通市": "nantong",
  "扬州市": "yangzhou",
  "镇江市": "zhenjiang",
  "常州市": "changzhou",
  "杭州市": "hangzhou",
  "宁波市": "ningbo",
  "温州市": "wenzhou",
  "嘉兴市": "jiaxing",
  "湖州市": "huzhou",
  "绍兴市": "shaoxing",
  "金华市": "jinhua",
  "台州市": "taizhou",
  "合肥市": "hefei",
  "芜湖市": "wuhu",
  "蚌埠市": "bengbu",
  "黄山市": "huangshan",
  "六安市": "luan",
  "池州市": "chizhou",
  "福州市": "fuzhou",
  "厦门市": "xiamen",
  "莆田市": "putian",
  "泉州市": "quanzhou",
  "漳州市": "zhangzhou",
  "南平市": "nanping",
  "龙岩市": "longyan",
  "南昌市": "nanchang",
  "景德镇市": "jingdezhen",
  "九江市": "jiujiang",
  "赣州市": "ganzhou",
  "吉安市": "jian",
  "宜春市": "yichun",
  "上饶市": "shangrao",
  "济南市": "jinan",
  "青岛市": "qingdao",
  "淄博市": "zibo",
  "烟台市": "yantai",
  "潍坊市": "weifang",
  "济宁市": "jining",
  "泰安市": "taian",
  "威海市": "weihai",
  "郑州市": "zhengzhou",
  "开封市": "kaifeng",
  "洛阳市": "luoyang",
  "安阳市": "anyang",
  "焦作市": "jiaozuo",
  "南阳市": "nanyang",
  "信阳市": "xinyang",
  "武汉市": "wuhan",
  "黄石市": "huangshi",
  "宜昌市": "yichang",
  "襄阳市": "xiangyang",
  "荆州市": "jingzhou",
  "十堰市": "shiyan",
  "恩施土家族苗族自治州": "enshi",
  "长沙市": "changsha",
  "株洲市": "zhuzhou",
  "湘潭市": "xiangtan",
  "衡阳市": "hengyang",
  "张家界市": "zhangjiajie",
  "岳阳市": "yueyang",
  "湘西土家族苗族自治州": "xiangxi",
  "广州市": "guangzhou",
  "深圳市": "shenzhen",
  "珠海市": "zhuhai",
  "汕头市": "shantou",
  "佛山市": "foshan",
  "韶关市": "shaoguan",
  "东莞市": "dongguan",
  "中山市": "zhongshan",
  "惠州市": "huizhou",
  "南宁市": "nanning",
  "柳州市": "liuzhou",
  "桂林市": "guilin",
  "北海市": "beihai",
  "防城港市": "fangchenggang",
  "百色市": "baise",
  "海口市": "haikou",
  "三亚市": "sanya",
  "三沙市": "sansha",
  "成都市": "chengdu",
  "自贡市": "zigong",
  "攀枝花市": "panzhihua",
  "泸州市": "luzhou",
  "德阳市": "deyang",
  "绵阳市": "mianyang",
  "乐山市": "leshan",
  "南充市": "nanchong",
  "宜宾市": "yibin",
  "阿坝藏族羌族自治州": "aba",
  "甘孜藏族自治州": "ganzi",
  "贵阳市": "guiyang",
  "遵义市": "zunyi",
  "安顺市": "anshun",
  "黔东南苗族侗族自治州": "qiandongnan",
  "黔南布依族苗族自治州": "qiannan",
  "昆明市": "kunming",
  "曲靖市": "qujing",
  "保山市": "baoshan",
  "丽江市": "lijiang",
  "大理白族自治州": "dali",
  "迪庆藏族自治州": "diqing",
  "拉萨市": "lasa",
  "日喀则市": "rikaze",
  "林芝市": "linzhi",
  "西安市": "xian",
  "宝鸡市": "baoji",
  "咸阳市": "xianyang",
  "渭南市": "weinan",
  "延安市": "yanan",
  "汉中市": "hanzhong",
  "兰州市": "lanzhou",
  "嘉峪关市": "jiayuguan",
  "天水市": "tianshui",
  "张掖市": "zhangye",
  "酒泉市": "jiuquan",
  "甘南藏族自治州": "gannan",
  "西宁市": "xining",
  "海东市": "haidong",
  "银川市": "yinchuan",
  "石嘴山市": "shizuishan",
  "吴忠市": "wuzhong",
  "乌鲁木齐市": "wulumuqi",
  "克拉玛依市": "kelamayi",
  "吐鲁番市": "tulufan",
  "伊犁哈萨克自治州": "yili",
  "香港": "hongkong",
  "澳门": "aomen",
  "台北市": "taibei",
  "高雄市": "gaoxiong",
  "台中市": "taizhong",
  "台南市": "tainan"
};
const citySearchList = [];
for (const [province, cities] of Object.entries(provinceCityMap)) {
  for (const city of cities) {
    citySearchList.push({
      province,
      city,
      pinyin: cityPinyinMap[city] || ""
    });
  }
}
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
      recommendedRoutes: [],
      suggestions: []
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
    onInput() {
      const kw = this.keyword.trim().toLowerCase();
      if (!kw) {
        this.suggestions = [];
        return;
      }
      this.suggestions = citySearchList.filter(
        (s) => s.city.includes(kw) || s.city.replace("市", "").includes(kw) || s.pinyin.includes(kw)
      ).slice(0, 10);
    },
    selectCity(s) {
      this.suggestions = [];
      this.keyword = "";
      const provIdx = this.provinceNames.indexOf(s.province);
      if (provIdx > -1) {
        this.selectedProvinceIndex = provIdx;
        this.selectedProvince = s.province;
        const cities = this.provinceCityMap[s.province] || [];
        const cityIdx = cities.indexOf(s.city);
        this.selectedCityIndex = cityIdx + 1;
      }
      this.getSpots();
      this.getRecommendedRoutes();
    },
    changeProvince(e) {
      const idx = e.detail.value;
      this.selectedProvince = this.provinceNames[idx];
      this.selectedProvinceIndex = idx;
      this.selectedCityIndex = 0;
      this.keyword = "";
      this.suggestions = [];
      this.getSpots();
      this.getRecommendedRoutes();
    },
    changeCity(e) {
      this.selectedCityIndex = e.detail.value;
      this.keyword = "";
      this.suggestions = [];
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
      this.suggestions = [];
      const kw = this.keyword.trim().toLowerCase();
      if (!kw) {
        this.getSpots();
        return;
      }
      const matched = citySearchList.find(
        (s) => s.city.includes(kw) || s.pinyin === kw || s.pinyin.includes(kw)
      );
      if (matched) {
        this.selectCity(matched);
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
    a: common_vendor.o([($event) => $data.keyword = $event.detail.value, (...args) => $options.onInput && $options.onInput(...args)]),
    b: common_vendor.o((...args) => $options.searchSpot && $options.searchSpot(...args)),
    c: $data.keyword,
    d: common_vendor.o((...args) => $options.searchSpot && $options.searchSpot(...args)),
    e: $data.suggestions.length > 0
  }, $data.suggestions.length > 0 ? {
    f: common_vendor.f($data.suggestions, (s, i, i0) => {
      return common_vendor.e({
        a: common_vendor.t(s.province),
        b: common_vendor.t(s.city),
        c: s.pinyin
      }, s.pinyin ? {
        d: common_vendor.t(s.pinyin)
      } : {}, {
        e: i,
        f: common_vendor.o(($event) => $options.selectCity(s), i)
      });
    })
  } : {}, {
    g: common_vendor.f($data.banners, (item, index, i0) => {
      return {
        a: item.url,
        b: index
      };
    }),
    h: common_vendor.t($data.selectedProvince),
    i: $data.selectedProvinceIndex,
    j: $data.provinceNames,
    k: common_vendor.o((...args) => $options.changeProvince && $options.changeProvince(...args)),
    l: common_vendor.t($options.selectedCity),
    m: $data.selectedCityIndex,
    n: $options.cityList,
    o: common_vendor.o((...args) => $options.changeCity && $options.changeCity(...args)),
    p: common_vendor.f($data.spots, (item, index, i0) => {
      return {
        a: item.image,
        b: common_vendor.t(item.name),
        c: common_vendor.t(item.isCollected ? "★ 已收藏" : "☆ 收藏"),
        d: common_vendor.o(($event) => $options.toggleCollect(item), index),
        e: index,
        f: common_vendor.o(($event) => $options.goToDetail(item.id), index)
      };
    }),
    q: $data.recommendedRoutes.length > 0
  }, $data.recommendedRoutes.length > 0 ? {
    r: common_vendor.t($options.selectedCity !== "全部城市" ? $options.selectedCity : $data.selectedProvince),
    s: common_vendor.f($data.recommendedRoutes, (route, index, i0) => {
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
