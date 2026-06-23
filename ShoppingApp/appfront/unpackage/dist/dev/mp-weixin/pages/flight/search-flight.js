"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      searchParams: {
        fromCity: "PEK",
        fromCityName: "北京",
        toCity: "SHA",
        toCityName: "上海",
        fromDate: ""
      },
      isLoading: false,
      flightList: [],
      showCityPickerFlag: false,
      showResultFlag: false,
      cityPickerType: "from",
      citySearchKeyword: "",
      hotCities: [
        { name: "北京", code: "PEK", pinyin: "beijing" },
        { name: "上海", code: "SHA", pinyin: "shanghai" },
        { name: "广州", code: "CAN", pinyin: "guangzhou" },
        { name: "深圳", code: "SZX", pinyin: "shenzhen" },
        { name: "成都", code: "CTU", pinyin: "chengdu" },
        { name: "杭州", code: "HGH", pinyin: "hangzhou" },
        { name: "西安", code: "XIY", pinyin: "xian" },
        { name: "重庆", code: "CKG", pinyin: "chongqing" },
        { name: "武汉", code: "WUH", pinyin: "wuhan" },
        { name: "厦门", code: "XMN", pinyin: "xiamen" },
        { name: "南京", code: "NKG", pinyin: "nanjing" },
        { name: "三亚", code: "SYX", pinyin: "sanya" }
      ],
      cityList: [
        { name: "澳门", code: "MFM", pinyin: "aomen", firstLetter: "A" },
        { name: "北京", code: "PEK", pinyin: "beijing", firstLetter: "B" },
        { name: "北京大兴", code: "PKX", pinyin: "beijingdaxing", firstLetter: "B" },
        { name: "包头", code: "BAV", pinyin: "baotou", firstLetter: "B" },
        { name: "北海", code: "BHY", pinyin: "beihai", firstLetter: "B" },
        { name: "重庆", code: "CKG", pinyin: "chongqing", firstLetter: "C" },
        { name: "成都", code: "CTU", pinyin: "chengdu", firstLetter: "C" },
        { name: "成都天府", code: "TFU", pinyin: "chengdutianfu", firstLetter: "C" },
        { name: "长沙", code: "CSX", pinyin: "changsha", firstLetter: "C" },
        { name: "长春", code: "CGQ", pinyin: "changchun", firstLetter: "C" },
        { name: "常州", code: "CZX", pinyin: "changzhou", firstLetter: "C" },
        { name: "大连", code: "DLC", pinyin: "dalian", firstLetter: "D" },
        { name: "敦煌", code: "DNH", pinyin: "dunhuang", firstLetter: "D" },
        { name: "鄂尔多斯", code: "DSN", pinyin: "eerduosi", firstLetter: "E" },
        { name: "福州", code: "FOC", pinyin: "fuzhou", firstLetter: "F" },
        { name: "广州", code: "CAN", pinyin: "guangzhou", firstLetter: "G" },
        { name: "桂林", code: "KWL", pinyin: "guilin", firstLetter: "G" },
        { name: "贵阳", code: "KWE", pinyin: "guiyang", firstLetter: "G" },
        { name: "赣州", code: "KOW", pinyin: "ganzhou", firstLetter: "G" },
        { name: "杭州", code: "HGH", pinyin: "hangzhou", firstLetter: "H" },
        { name: "哈尔滨", code: "HRB", pinyin: "haerbin", firstLetter: "H" },
        { name: "海口", code: "HAK", pinyin: "haikou", firstLetter: "H" },
        { name: "合肥", code: "HFE", pinyin: "hefei", firstLetter: "H" },
        { name: "呼和浩特", code: "HET", pinyin: "huhehaote", firstLetter: "H" },
        { name: "黄山", code: "TXN", pinyin: "huangshan", firstLetter: "H" },
        { name: "香港", code: "HKG", pinyin: "xianggang", firstLetter: "X" },
        { name: "济南", code: "TNA", pinyin: "jinan", firstLetter: "J" },
        { name: "济宁", code: "JNG", pinyin: "jining", firstLetter: "J" },
        { name: "昆明", code: "KMG", pinyin: "kunming", firstLetter: "K" },
        { name: "喀什", code: "KHG", pinyin: "kashi", firstLetter: "K" },
        { name: "兰州", code: "LHW", pinyin: "lanzhou", firstLetter: "L" },
        { name: "丽江", code: "LJG", pinyin: "lijiang", firstLetter: "L" },
        { name: "拉萨", code: "LXA", pinyin: "lasa", firstLetter: "L" },
        { name: "临沂", code: "LYI", pinyin: "linyi", firstLetter: "L" },
        { name: "洛阳", code: "LYA", pinyin: "luoyang", firstLetter: "L" },
        { name: "绵阳", code: "MIG", pinyin: "mianyang", firstLetter: "M" },
        { name: "南京", code: "NKG", pinyin: "nanjing", firstLetter: "N" },
        { name: "南宁", code: "NNG", pinyin: "nanning", firstLetter: "N" },
        { name: "宁波", code: "NGB", pinyin: "ningbo", firstLetter: "N" },
        { name: "南昌", code: "KHN", pinyin: "nanchang", firstLetter: "N" },
        { name: "南通", code: "NTG", pinyin: "nantong", firstLetter: "N" },
        { name: "普洱", code: "SYM", pinyin: "puer", firstLetter: "P" },
        { name: "青岛", code: "TAO", pinyin: "qingdao", firstLetter: "Q" },
        { name: "秦皇岛", code: "BPE", pinyin: "qinhuangdao", firstLetter: "Q" },
        { name: "上海", code: "SHA", pinyin: "shanghai", firstLetter: "S" },
        { name: "上海浦东", code: "PVG", pinyin: "shanghaipudong", firstLetter: "S" },
        { name: "深圳", code: "SZX", pinyin: "shenzhen", firstLetter: "S" },
        { name: "沈阳", code: "SHE", pinyin: "shenyang", firstLetter: "S" },
        { name: "三亚", code: "SYX", pinyin: "sanya", firstLetter: "S" },
        { name: "苏州", code: "SZV", pinyin: "suzhou", firstLetter: "S" },
        { name: "石家庄", code: "SJW", pinyin: "shijiazhuang", firstLetter: "S" },
        { name: "汕头", code: "SWA", pinyin: "shantou", firstLetter: "S" },
        { name: "天津", code: "TSN", pinyin: "tianjin", firstLetter: "T" },
        { name: "太原", code: "TYN", pinyin: "taiyuan", firstLetter: "T" },
        { name: "台北", code: "TPE", pinyin: "taibei", firstLetter: "T" },
        { name: "台州", code: "HYN", pinyin: "taizhou", firstLetter: "T" },
        { name: "武汉", code: "WUH", pinyin: "wuhan", firstLetter: "W" },
        { name: "乌鲁木齐", code: "URC", pinyin: "wulumuqi", firstLetter: "W" },
        { name: "无锡", code: "WUX", pinyin: "wuxi", firstLetter: "W" },
        { name: "温州", code: "WNZ", pinyin: "wenzhou", firstLetter: "W" },
        { name: "威海", code: "WEH", pinyin: "weihai", firstLetter: "W" },
        { name: "潍坊", code: "WEF", pinyin: "weifang", firstLetter: "W" },
        { name: "西安", code: "XIY", pinyin: "xian", firstLetter: "X" },
        { name: "西宁", code: "XNN", pinyin: "xining", firstLetter: "X" },
        { name: "徐州", code: "XUZ", pinyin: "xuzhou", firstLetter: "X" },
        { name: "厦门", code: "XMN", pinyin: "xiamen", firstLetter: "X" },
        { name: "银川", code: "INC", pinyin: "yinchuan", firstLetter: "Y" },
        { name: "烟台", code: "YNT", pinyin: "yantai", firstLetter: "Y" },
        { name: "宜昌", code: "YIH", pinyin: "yichang", firstLetter: "Y" },
        { name: "义乌", code: "YIW", pinyin: "yiwu", firstLetter: "Y" },
        { name: "盐城", code: "YNZ", pinyin: "yancheng", firstLetter: "Y" },
        { name: "郑州", code: "CGO", pinyin: "zhengzhou", firstLetter: "Z" },
        { name: "珠海", code: "ZUH", pinyin: "zhuhai", firstLetter: "Z" },
        { name: "张家界", code: "DYG", pinyin: "zhangjiajie", firstLetter: "Z" },
        { name: "湛江", code: "ZHA", pinyin: "zhanjiang", firstLetter: "Z" },
        { name: "遵义", code: "ZYI", pinyin: "zunyi", firstLetter: "Z" },
        { name: "九寨沟", code: "JZH", pinyin: "jiuzhaigou", firstLetter: "J" },
        { name: "西双版纳", code: "JHG", pinyin: "xishuangbanna", firstLetter: "X" },
        { name: "腾冲", code: "TCZ", pinyin: "tengchong", firstLetter: "T" },
        { name: "林芝", code: "LZY", pinyin: "linzhi", firstLetter: "L" },
        { name: "香格里拉", code: "DIG", pinyin: "xianggelila", firstLetter: "X" }
      ]
    };
  },
  computed: {
    filteredCityList() {
      let filtered = [...this.cityList];
      if (this.citySearchKeyword) {
        const keyword = this.citySearchKeyword.toLowerCase();
        filtered = filtered.filter(
          (city) => city.name.includes(keyword) || city.pinyin.includes(keyword) || city.code.toLowerCase().includes(keyword)
        );
      }
      const grouped = {};
      filtered.forEach((city) => {
        const letter = city.firstLetter;
        if (!grouped[letter])
          grouped[letter] = [];
        grouped[letter].push(city);
      });
      const sorted = {};
      Object.keys(grouped).sort().forEach((key) => {
        sorted[key] = grouped[key];
      });
      return sorted;
    }
  },
  onLoad() {
    const date = /* @__PURE__ */ new Date();
    date.setDate(date.getDate() + 7);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    this.searchParams.fromDate = `${year}-${month}-${day}`;
    const externalParams = common_vendor.index.getStorageSync("flight_search_params");
    if (externalParams) {
      if (externalParams.fromCity) {
        const matched = this.cityList.find(
          (c) => c.name === externalParams.fromCity || c.code === externalParams.fromCity
        );
        if (matched) {
          this.searchParams.fromCity = matched.code;
          this.searchParams.fromCityName = matched.name;
        }
      }
      if (externalParams.toCity) {
        const matched = this.cityList.find(
          (c) => c.name === externalParams.toCity || c.code === externalParams.toCity
        );
        if (matched) {
          this.searchParams.toCity = matched.code;
          this.searchParams.toCityName = matched.name;
        }
      }
      if (externalParams.fromDate) {
        this.searchParams.fromDate = externalParams.fromDate;
      }
      common_vendor.index.removeStorageSync("flight_search_params");
    }
  },
  methods: {
    swapCities() {
      const tempCode = this.searchParams.fromCity;
      const tempName = this.searchParams.fromCityName;
      this.searchParams.fromCity = this.searchParams.toCity;
      this.searchParams.fromCityName = this.searchParams.toCityName;
      this.searchParams.toCity = tempCode;
      this.searchParams.toCityName = tempName;
    },
    showCityPicker(type) {
      this.cityPickerType = type;
      this.citySearchKeyword = "";
      this.showCityPickerFlag = true;
    },
    closeCityPicker() {
      this.showCityPickerFlag = false;
      this.citySearchKeyword = "";
    },
    filterCities() {
    },
    selectCity(city) {
      if (this.cityPickerType === "from") {
        this.searchParams.fromCity = city.code;
        this.searchParams.fromCityName = city.name;
      } else {
        this.searchParams.toCity = city.code;
        this.searchParams.toCityName = city.name;
      }
      this.closeCityPicker();
    },
    onDateChange(e) {
      this.searchParams.fromDate = e.detail.value;
    },
    formatTimeDisplay(dateTimeStr) {
      if (!dateTimeStr)
        return "--";
      try {
        if (dateTimeStr.includes(" ")) {
          const timePart = dateTimeStr.split(" ")[1];
          return timePart.substring(0, 5);
        }
        if (dateTimeStr.includes("T")) {
          const parts = dateTimeStr.split("T");
          const timePart = parts[1];
          return timePart.substring(0, 5);
        }
        return dateTimeStr;
      } catch (e) {
        return dateTimeStr;
      }
    },
    calculateDurationFromMinutes(minutes) {
      if (!minutes)
        return "--";
      const mins = parseInt(minutes);
      if (isNaN(mins))
        return "--";
      const hours = Math.floor(mins / 60);
      const minsLeft = mins % 60;
      if (hours > 0) {
        return `${hours}小时${minsLeft}分`;
      }
      return `${minsLeft}分`;
    },
    searchFlights() {
      if (!this.searchParams.fromCity) {
        common_vendor.index.showToast({ title: "请选择出发城市", icon: "none" });
        return;
      }
      if (!this.searchParams.toCity) {
        common_vendor.index.showToast({ title: "请选择到达城市", icon: "none" });
        return;
      }
      if (this.searchParams.fromCity === this.searchParams.toCity) {
        common_vendor.index.showToast({ title: "出发城市和到达城市不能相同", icon: "none" });
        return;
      }
      if (!this.searchParams.fromDate) {
        common_vendor.index.showToast({ title: "请选择出发日期", icon: "none" });
        return;
      }
      this.isLoading = true;
      common_vendor.index.request({
        url: "http://localhost:8080/api/flights/search",
        method: "GET",
        data: {
          dep: this.searchParams.fromCity,
          arr: this.searchParams.toCity,
          date: this.searchParams.fromDate
        },
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/flight/search-flight.vue:455", "航班查询返回：", res.data);
          let flights = [];
          if (res.data && res.data.success && typeof res.data.data === "string") {
            common_vendor.index.__f__("log", "at pages/flight/search-flight.vue:460", "原始数据前500字符：", res.data.data.substring(0, 500));
            flights = this.parseFlightsFromString(res.data.data);
          } else if (res.data && res.data.code === 200 && res.data.data && Array.isArray(res.data.data)) {
            flights = res.data.data;
          } else if (res.data && res.data.success && res.data.data && Array.isArray(res.data.data)) {
            flights = res.data.data;
          }
          if (flights.length > 0) {
            this.flightList = flights.slice(0, 20);
            common_vendor.index.__f__("log", "at pages/flight/search-flight.vue:470", "解析出的航班数量：", this.flightList.length);
          } else {
            this.flightList = this.getMockFlights();
            common_vendor.index.showToast({ title: "使用演示数据", icon: "none" });
          }
          this.showResultFlag = true;
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/flight/search-flight.vue:478", "请求失败：", err);
          this.flightList = this.getMockFlights();
          this.showResultFlag = true;
          common_vendor.index.showToast({ title: "查询失败，使用演示数据", icon: "none" });
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    },
    getMockFlights() {
      const fromCity = this.searchParams.fromCityName;
      const toCity = this.searchParams.toCityName;
      const selectedDate = this.searchParams.fromDate;
      const priceMap = {
        "北京-上海": [850, 720, 680, 1280, 980],
        "上海-北京": [850, 720, 680, 1280, 980],
        "北京-广州": [1280, 1180, 1080, 1580, 1380],
        "广州-北京": [1280, 1180, 1080, 1580, 1380],
        "上海-深圳": [890, 820, 750, 1180, 950],
        "深圳-上海": [890, 820, 750, 1180, 950],
        "北京-深圳": [1350, 1250, 1150, 1680, 1450],
        "深圳-北京": [1350, 1250, 1150, 1680, 1450],
        "上海-广州": [1120, 1020, 920, 1380, 1180],
        "广州-上海": [1120, 1020, 920, 1380, 1180]
      };
      const routeKey = `${fromCity}-${toCity}`;
      const prices = priceMap[routeKey] || [680, 580, 520, 880, 750];
      return [
        {
          flightNumber: "CA1234",
          airline: "中国国航",
          departureAirport: fromCity + "首都机场",
          arrivalAirport: toCity + "虹桥机场",
          departureTime: selectedDate + " 08:00",
          arrivalTime: selectedDate + " 10:30",
          departureTimeDisplay: "08:00",
          arrivalTimeDisplay: "10:30",
          duration: "2小时30分",
          price: prices[0]
        },
        {
          flightNumber: "MU5678",
          airline: "东方航空",
          departureAirport: fromCity + "大兴机场",
          arrivalAirport: toCity + "浦东机场",
          departureTime: selectedDate + " 14:00",
          arrivalTime: selectedDate + " 16:20",
          departureTimeDisplay: "14:00",
          arrivalTimeDisplay: "16:20",
          duration: "2小时20分",
          price: prices[1]
        },
        {
          flightNumber: "CZ9012",
          airline: "南方航空",
          departureAirport: fromCity + "首都机场",
          arrivalAirport: toCity + "虹桥机场",
          departureTime: selectedDate + " 18:30",
          arrivalTime: selectedDate + " 21:00",
          departureTimeDisplay: "18:30",
          arrivalTimeDisplay: "21:00",
          duration: "2小时30分",
          price: prices[2]
        },
        {
          flightNumber: "HU7890",
          airline: "海南航空",
          departureAirport: fromCity + "首都机场",
          arrivalAirport: toCity + "浦东机场",
          departureTime: selectedDate + " 09:15",
          arrivalTime: selectedDate + " 11:45",
          departureTimeDisplay: "09:15",
          arrivalTimeDisplay: "11:45",
          duration: "2小时30分",
          price: prices[3]
        },
        {
          flightNumber: "3U4567",
          airline: "四川航空",
          departureAirport: fromCity + "大兴机场",
          arrivalAirport: toCity + "虹桥机场",
          departureTime: selectedDate + " 16:30",
          arrivalTime: selectedDate + " 18:50",
          departureTimeDisplay: "16:30",
          arrivalTimeDisplay: "18:50",
          duration: "2小时20分",
          price: prices[4]
        }
      ];
    },
    parseFlightsFromString(dataStr) {
      try {
        const allFlights = dataStr.match(/\{\s*'fcategory':[^}]+\}/g);
        if (allFlights && allFlights.length > 0) {
          common_vendor.index.__f__("log", "at pages/flight/search-flight.vue:578", "直接找到航班对象数量：", allFlights.length);
          return this.extractFlightsFromMatches(allFlights);
        }
        let dataArrayMatch = dataStr.match(/data['"]?\s*:\s*\[(.*?)\]/s);
        if (!dataArrayMatch) {
          dataArrayMatch = dataStr.match(/'data':\s*\[(.*?)\]/s);
        }
        if (dataArrayMatch) {
          const flightsStr = dataArrayMatch[1];
          const flightMatches = flightsStr.match(/\{\s*'fcategory':[^}]+\}/g);
          if (flightMatches && flightMatches.length > 0) {
            common_vendor.index.__f__("log", "at pages/flight/search-flight.vue:591", "从data数组找到航班数量：", flightMatches.length);
            return this.extractFlightsFromMatches(flightMatches);
          }
        }
        common_vendor.index.__f__("log", "at pages/flight/search-flight.vue:596", "未找到任何航班数据");
        return [];
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/flight/search-flight.vue:600", "解析航班数据失败：", e);
        return [];
      }
    },
    extractFlightsFromMatches(flightMatches) {
      const flights = [];
      for (const flightStr of flightMatches) {
        try {
          const flightNoMatch = flightStr.match(/FlightNo['"]?\s*:\s*['"]([^'"]+)['"]/);
          const airlineMatch = flightStr.match(/FlightCompany['"]?\s*:\s*['"]([^'"]+)['"]/);
          const depTimeMatch = flightStr.match(/FlightDeptimePlanDate['"]?\s*:\s*['"]([^'"]+)['"]/);
          const arrTimeMatch = flightStr.match(/FlightArrtimePlanDate['"]?\s*:\s*['"]([^'"]+)['"]/);
          const depAirportMatch = flightStr.match(/FlightDepAirport['"]?\s*:\s*['"]([^'"]+)['"]/);
          const arrAirportMatch = flightStr.match(/FlightArrAirport['"]?\s*:\s*['"]([^'"]+)['"]/);
          const stateMatch = flightStr.match(/FlightState['"]?\s*:\s*['"]([^'"]+)['"]/);
          const durationMatch = flightStr.match(/FlightDuration['"]?\s*:\s*['"]?([0-9]+)['"]?/);
          const flightState = stateMatch ? stateMatch[1] : "";
          if (flightState === "计划" && flightNoMatch && depTimeMatch && arrTimeMatch) {
            const depTime = depTimeMatch[1].replace("T", " ");
            const arrTime = arrTimeMatch[1].replace("T", " ");
            const durationMin = durationMatch ? durationMatch[1] : "";
            flights.push({
              flightNumber: flightNoMatch[1],
              airline: airlineMatch ? airlineMatch[1] : "航空公司",
              departureTime: depTime,
              arrivalTime: arrTime,
              departureTimeDisplay: this.formatTimeDisplay(depTime),
              arrivalTimeDisplay: this.formatTimeDisplay(arrTime),
              departureAirport: depAirportMatch ? depAirportMatch[1] : this.searchParams.fromCityName,
              arrivalAirport: arrAirportMatch ? arrAirportMatch[1] : this.searchParams.toCityName,
              duration: this.calculateDurationFromMinutes(durationMin),
              price: Math.floor(Math.random() * 600) + 500
            });
          }
        } catch (e) {
          common_vendor.index.__f__("error", "at pages/flight/search-flight.vue:640", "提取航班失败：", e);
        }
      }
      return flights;
    },
    closeResult() {
      this.showResultFlag = false;
    },
    selectFlight(flight) {
      common_vendor.index.setStorageSync("selectedFlight", flight);
      this.showResultFlag = false;
      common_vendor.index.navigateTo({ url: "/pages/flight/order" });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.swapCities && $options.swapCities(...args)),
    b: common_vendor.t($data.searchParams.fromCity ? $data.searchParams.fromCityName + " (" + $data.searchParams.fromCity + ")" : "请选择出发城市"),
    c: common_vendor.n($data.searchParams.fromCity ? "selected" : "placeholder"),
    d: common_vendor.o(($event) => $options.showCityPicker("from")),
    e: common_vendor.t($data.searchParams.toCity ? $data.searchParams.toCityName + " (" + $data.searchParams.toCity + ")" : "请选择到达城市"),
    f: common_vendor.n($data.searchParams.toCity ? "selected" : "placeholder"),
    g: common_vendor.o(($event) => $options.showCityPicker("to")),
    h: common_vendor.t($data.searchParams.fromDate || "请选择日期"),
    i: common_vendor.n($data.searchParams.fromDate ? "selected" : "placeholder"),
    j: $data.searchParams.fromDate,
    k: common_vendor.o((...args) => $options.onDateChange && $options.onDateChange(...args)),
    l: common_vendor.t($data.isLoading ? "搜索中..." : "🔍 搜索机票"),
    m: common_vendor.o((...args) => $options.searchFlights && $options.searchFlights(...args)),
    n: $data.isLoading,
    o: $data.showCityPickerFlag
  }, $data.showCityPickerFlag ? common_vendor.e({
    p: common_vendor.t($data.cityPickerType === "from" ? "选择出发城市" : "选择到达城市"),
    q: common_vendor.o((...args) => $options.closeCityPicker && $options.closeCityPicker(...args)),
    r: common_vendor.o([($event) => $data.citySearchKeyword = $event.detail.value, (...args) => $options.filterCities && $options.filterCities(...args)]),
    s: $data.citySearchKeyword,
    t: !$data.citySearchKeyword
  }, !$data.citySearchKeyword ? {
    v: common_vendor.f($data.hotCities, (city, k0, i0) => {
      return {
        a: common_vendor.t(city.name),
        b: common_vendor.t(city.code),
        c: city.code,
        d: common_vendor.o(($event) => $options.selectCity(city), city.code)
      };
    })
  } : {}, {
    w: common_vendor.f($options.filteredCityList, (group, letter, i0) => {
      return {
        a: common_vendor.t(letter),
        b: common_vendor.f(group, (city, k1, i1) => {
          return {
            a: common_vendor.t(city.name),
            b: common_vendor.t(city.code),
            c: city.code,
            d: common_vendor.o(($event) => $options.selectCity(city), city.code)
          };
        }),
        c: letter
      };
    }),
    x: common_vendor.o(() => {
    }),
    y: common_vendor.o((...args) => $options.closeCityPicker && $options.closeCityPicker(...args))
  }) : {}, {
    z: $data.showResultFlag
  }, $data.showResultFlag ? common_vendor.e({
    A: common_vendor.o((...args) => $options.closeResult && $options.closeResult(...args)),
    B: common_vendor.t($data.searchParams.fromCityName),
    C: common_vendor.t($data.searchParams.toCityName),
    D: common_vendor.t($data.searchParams.fromDate),
    E: $data.flightList.length > 0
  }, $data.flightList.length > 0 ? {
    F: common_vendor.f($data.flightList, (flight, index, i0) => {
      return {
        a: common_vendor.t(flight.flightNumber || "--"),
        b: common_vendor.t(flight.airline || "航空公司"),
        c: common_vendor.t(flight.price || "--"),
        d: common_vendor.t(flight.departureTimeDisplay || "--"),
        e: common_vendor.t(flight.departureAirport || "--"),
        f: common_vendor.t(flight.duration || "--"),
        g: common_vendor.t(flight.arrivalTimeDisplay || "--"),
        h: common_vendor.t(flight.arrivalAirport || "--"),
        i: index,
        j: common_vendor.o(($event) => $options.selectFlight(flight), index)
      };
    })
  } : {}, {
    G: common_vendor.o(() => {
    }),
    H: common_vendor.o((...args) => $options.closeResult && $options.closeResult(...args))
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-e3d3529c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/flight/search-flight.js.map
