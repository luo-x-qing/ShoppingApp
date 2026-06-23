<template>
  <view class="container">
    <!-- 顶部标题 -->
    <view class="header">
      <view class="header-bg"></view>
      <text class="title">✈️ 机票查询</text>
      <text class="subtitle">特价机票，说走就走</text>
    </view>

    <!-- 交换城市按钮 -->
    <view class="swap-city" @click="swapCities">
      <text class="swap-icon">⇅</text>
    </view>

    <!-- 搜索表单 -->
    <view class="search-form">
      <view class="form-item" @click="showCityPicker('from')">
        <view class="icon-box">
          <text class="icon">🛫</text>
        </view>
        <view class="form-content">
          <text class="label">出发城市</text>
          <view class="input-box">
            <text :class="searchParams.fromCity ? 'selected' : 'placeholder'">
              {{ searchParams.fromCity ? searchParams.fromCityName + ' (' + searchParams.fromCity + ')' : '请选择出发城市' }}
            </text>
            <text class="arrow-right">›</text>
          </view>
        </view>
      </view>

      <view class="form-item" @click="showCityPicker('to')">
        <view class="icon-box">
          <text class="icon">🛬</text>
        </view>
        <view class="form-content">
          <text class="label">到达城市</text>
          <view class="input-box">
            <text :class="searchParams.toCity ? 'selected' : 'placeholder'">
              {{ searchParams.toCity ? searchParams.toCityName + ' (' + searchParams.toCity + ')' : '请选择到达城市' }}
            </text>
            <text class="arrow-right">›</text>
          </view>
        </view>
      </view>

      <view class="form-item">
        <view class="icon-box">
          <text class="icon">📅</text>
        </view>
        <view class="form-content">
          <text class="label">出发日期</text>
          <picker mode="date" :value="searchParams.fromDate" @change="onDateChange">
            <view class="input-box">
              <text :class="searchParams.fromDate ? 'selected' : 'placeholder'">{{ searchParams.fromDate || '请选择日期' }}</text>
              <text class="arrow-right">›</text>
            </view>
          </picker>
        </view>
      </view>

      <button class="search-btn" @click="searchFlights" :disabled="isLoading">
        {{ isLoading ? '搜索中...' : '🔍 搜索机票' }}
      </button>
    </view>

    <!-- 城市选择弹窗 -->
    <view class="city-picker-mask" v-if="showCityPickerFlag" @click="closeCityPicker">
      <view class="city-picker-container" @click.stop>
        <view class="city-picker-header">
          <text class="picker-title">{{ cityPickerType === 'from' ? '选择出发城市' : '选择到达城市' }}</text>
          <text class="close-btn" @click="closeCityPicker">✕</text>
        </view>
        
        <view class="city-search">
          <input 
            class="search-input" 
            v-model="citySearchKeyword" 
            placeholder="🔍 搜索城市名称或拼音" 
            @input="filterCities"
          />
        </view>
        
        <view class="hot-cities" v-if="!citySearchKeyword">
          <view class="section-title">🔥 热门城市</view>
          <view class="city-grid">
            <view 
              class="city-item" 
              v-for="city in hotCities" 
              :key="city.code"
              @click="selectCity(city)"
            >
              <text class="city-name">{{ city.name }}</text>
              <text class="city-code">{{ city.code }}</text>
            </view>
          </view>
        </view>
        
        <scroll-view class="city-list" scroll-y="true">
          <view v-for="(group, letter) in filteredCityList" :key="letter" class="city-group">
            <view class="group-title">{{ letter }}</view>
            <view 
              class="city-item-row" 
              v-for="city in group" 
              :key="city.code"
              @click="selectCity(city)"
            >
              <text class="city-name-text">{{ city.name }}</text>
              <text class="city-code-text">{{ city.code }}</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 搜索结果弹窗 -->
    <view class="result-mask" v-if="showResultFlag" @click="closeResult">
      <view class="result-container" @click.stop>
        <view class="result-header">
          <text class="result-title">✈️ 航班列表</text>
          <text class="close-btn" @click="closeResult">✕</text>
        </view>
        
        <view class="result-info">
          <view class="route">
            <text class="from">{{ searchParams.fromCityName }}</text>
            <text class="arrow">→</text>
            <text class="to">{{ searchParams.toCityName }}</text>
          </view>
          <text class="date">📅 {{ searchParams.fromDate }}</text>
        </view>
        
        <scroll-view class="result-list" scroll-y="true" v-if="flightList.length > 0">
          <view 
            class="flight-card" 
            v-for="(flight, index) in flightList" 
            :key="index"
            @click="selectFlight(flight)"
          >
            <view class="flight-header">
              <view class="flight-left">
                <text class="flight-number">{{ flight.flightNumber || '--' }}</text>
                <text class="airline">{{ flight.airline || '航空公司' }}</text>
              </view>
              <text class="price-tag">¥{{ flight.price || '--' }}</text>
            </view>
            
            <view class="flight-info">
              <view class="time-info">
                <view class="departure">
                  <text class="time">{{ flight.departureTimeDisplay || '--' }}</text>
                  <text class="airport">{{ flight.departureAirport || '--' }}</text>
                </view>
                <view class="flight-line">
                  <text class="duration">{{ flight.duration || '--' }}</text>
                  <view class="line"></view>
                  <text class="arrow-icon">✈️</text>
                </view>
                <view class="arrival">
                  <text class="time">{{ flight.arrivalTimeDisplay || '--' }}</text>
                  <text class="airport">{{ flight.arrivalAirport || '--' }}</text>
                </view>
              </view>
            </view>
            
            <view class="flight-footer">
              <text class="book-hint">立即预订 ›</text>
            </view>
          </view>
        </scroll-view>
        
        <view v-else class="empty-result">
          <text class="empty-icon">🕊️</text>
          <text>暂无航班信息</text>
          <text class="try-again">请尝试其他日期或城市</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      searchParams: {
        fromCity: 'PEK',
        fromCityName: '北京',
        toCity: 'SHA',
        toCityName: '上海',
        fromDate: ''
      },
      isLoading: false,
      flightList: [],
      showCityPickerFlag: false,
      showResultFlag: false,
      cityPickerType: 'from',
      citySearchKeyword: '',
      hotCities: [
        { name: '北京', code: 'PEK', pinyin: 'beijing' },
        { name: '上海', code: 'SHA', pinyin: 'shanghai' },
        { name: '广州', code: 'CAN', pinyin: 'guangzhou' },
        { name: '深圳', code: 'SZX', pinyin: 'shenzhen' },
        { name: '成都', code: 'CTU', pinyin: 'chengdu' },
        { name: '杭州', code: 'HGH', pinyin: 'hangzhou' },
        { name: '西安', code: 'XIY', pinyin: 'xian' },
        { name: '重庆', code: 'CKG', pinyin: 'chongqing' },
        { name: '武汉', code: 'WUH', pinyin: 'wuhan' },
        { name: '厦门', code: 'XMN', pinyin: 'xiamen' },
        { name: '南京', code: 'NKG', pinyin: 'nanjing' },
        { name: '三亚', code: 'SYX', pinyin: 'sanya' }
      ],
      cityList: [
        { name: '澳门', code: 'MFM', pinyin: 'aomen', firstLetter: 'A' },
        { name: '北京', code: 'PEK', pinyin: 'beijing', firstLetter: 'B' },
        { name: '北京大兴', code: 'PKX', pinyin: 'beijingdaxing', firstLetter: 'B' },
        { name: '包头', code: 'BAV', pinyin: 'baotou', firstLetter: 'B' },
        { name: '北海', code: 'BHY', pinyin: 'beihai', firstLetter: 'B' },
        { name: '重庆', code: 'CKG', pinyin: 'chongqing', firstLetter: 'C' },
        { name: '成都', code: 'CTU', pinyin: 'chengdu', firstLetter: 'C' },
        { name: '成都天府', code: 'TFU', pinyin: 'chengdutianfu', firstLetter: 'C' },
        { name: '长沙', code: 'CSX', pinyin: 'changsha', firstLetter: 'C' },
        { name: '长春', code: 'CGQ', pinyin: 'changchun', firstLetter: 'C' },
        { name: '常州', code: 'CZX', pinyin: 'changzhou', firstLetter: 'C' },
        { name: '大连', code: 'DLC', pinyin: 'dalian', firstLetter: 'D' },
        { name: '敦煌', code: 'DNH', pinyin: 'dunhuang', firstLetter: 'D' },
        { name: '鄂尔多斯', code: 'DSN', pinyin: 'eerduosi', firstLetter: 'E' },
        { name: '福州', code: 'FOC', pinyin: 'fuzhou', firstLetter: 'F' },
        { name: '广州', code: 'CAN', pinyin: 'guangzhou', firstLetter: 'G' },
        { name: '桂林', code: 'KWL', pinyin: 'guilin', firstLetter: 'G' },
        { name: '贵阳', code: 'KWE', pinyin: 'guiyang', firstLetter: 'G' },
        { name: '赣州', code: 'KOW', pinyin: 'ganzhou', firstLetter: 'G' },
        { name: '杭州', code: 'HGH', pinyin: 'hangzhou', firstLetter: 'H' },
        { name: '哈尔滨', code: 'HRB', pinyin: 'haerbin', firstLetter: 'H' },
        { name: '海口', code: 'HAK', pinyin: 'haikou', firstLetter: 'H' },
        { name: '合肥', code: 'HFE', pinyin: 'hefei', firstLetter: 'H' },
        { name: '呼和浩特', code: 'HET', pinyin: 'huhehaote', firstLetter: 'H' },
        { name: '黄山', code: 'TXN', pinyin: 'huangshan', firstLetter: 'H' },
        { name: '香港', code: 'HKG', pinyin: 'xianggang', firstLetter: 'X' },
        { name: '济南', code: 'TNA', pinyin: 'jinan', firstLetter: 'J' },
        { name: '济宁', code: 'JNG', pinyin: 'jining', firstLetter: 'J' },
        { name: '昆明', code: 'KMG', pinyin: 'kunming', firstLetter: 'K' },
        { name: '喀什', code: 'KHG', pinyin: 'kashi', firstLetter: 'K' },
        { name: '兰州', code: 'LHW', pinyin: 'lanzhou', firstLetter: 'L' },
        { name: '丽江', code: 'LJG', pinyin: 'lijiang', firstLetter: 'L' },
        { name: '拉萨', code: 'LXA', pinyin: 'lasa', firstLetter: 'L' },
        { name: '临沂', code: 'LYI', pinyin: 'linyi', firstLetter: 'L' },
        { name: '洛阳', code: 'LYA', pinyin: 'luoyang', firstLetter: 'L' },
        { name: '绵阳', code: 'MIG', pinyin: 'mianyang', firstLetter: 'M' },
        { name: '南京', code: 'NKG', pinyin: 'nanjing', firstLetter: 'N' },
        { name: '南宁', code: 'NNG', pinyin: 'nanning', firstLetter: 'N' },
        { name: '宁波', code: 'NGB', pinyin: 'ningbo', firstLetter: 'N' },
        { name: '南昌', code: 'KHN', pinyin: 'nanchang', firstLetter: 'N' },
        { name: '南通', code: 'NTG', pinyin: 'nantong', firstLetter: 'N' },
        { name: '普洱', code: 'SYM', pinyin: 'puer', firstLetter: 'P' },
        { name: '青岛', code: 'TAO', pinyin: 'qingdao', firstLetter: 'Q' },
        { name: '秦皇岛', code: 'BPE', pinyin: 'qinhuangdao', firstLetter: 'Q' },
        { name: '上海', code: 'SHA', pinyin: 'shanghai', firstLetter: 'S' },
        { name: '上海浦东', code: 'PVG', pinyin: 'shanghaipudong', firstLetter: 'S' },
        { name: '深圳', code: 'SZX', pinyin: 'shenzhen', firstLetter: 'S' },
        { name: '沈阳', code: 'SHE', pinyin: 'shenyang', firstLetter: 'S' },
        { name: '三亚', code: 'SYX', pinyin: 'sanya', firstLetter: 'S' },
        { name: '苏州', code: 'SZV', pinyin: 'suzhou', firstLetter: 'S' },
        { name: '石家庄', code: 'SJW', pinyin: 'shijiazhuang', firstLetter: 'S' },
        { name: '汕头', code: 'SWA', pinyin: 'shantou', firstLetter: 'S' },
        { name: '天津', code: 'TSN', pinyin: 'tianjin', firstLetter: 'T' },
        { name: '太原', code: 'TYN', pinyin: 'taiyuan', firstLetter: 'T' },
        { name: '台北', code: 'TPE', pinyin: 'taibei', firstLetter: 'T' },
        { name: '台州', code: 'HYN', pinyin: 'taizhou', firstLetter: 'T' },
        { name: '武汉', code: 'WUH', pinyin: 'wuhan', firstLetter: 'W' },
        { name: '乌鲁木齐', code: 'URC', pinyin: 'wulumuqi', firstLetter: 'W' },
        { name: '无锡', code: 'WUX', pinyin: 'wuxi', firstLetter: 'W' },
        { name: '温州', code: 'WNZ', pinyin: 'wenzhou', firstLetter: 'W' },
        { name: '威海', code: 'WEH', pinyin: 'weihai', firstLetter: 'W' },
        { name: '潍坊', code: 'WEF', pinyin: 'weifang', firstLetter: 'W' },
        { name: '西安', code: 'XIY', pinyin: 'xian', firstLetter: 'X' },
        { name: '西宁', code: 'XNN', pinyin: 'xining', firstLetter: 'X' },
        { name: '徐州', code: 'XUZ', pinyin: 'xuzhou', firstLetter: 'X' },
        { name: '厦门', code: 'XMN', pinyin: 'xiamen', firstLetter: 'X' },
        { name: '银川', code: 'INC', pinyin: 'yinchuan', firstLetter: 'Y' },
        { name: '烟台', code: 'YNT', pinyin: 'yantai', firstLetter: 'Y' },
        { name: '宜昌', code: 'YIH', pinyin: 'yichang', firstLetter: 'Y' },
        { name: '义乌', code: 'YIW', pinyin: 'yiwu', firstLetter: 'Y' },
        { name: '盐城', code: 'YNZ', pinyin: 'yancheng', firstLetter: 'Y' },
        { name: '郑州', code: 'CGO', pinyin: 'zhengzhou', firstLetter: 'Z' },
        { name: '珠海', code: 'ZUH', pinyin: 'zhuhai', firstLetter: 'Z' },
        { name: '张家界', code: 'DYG', pinyin: 'zhangjiajie', firstLetter: 'Z' },
        { name: '湛江', code: 'ZHA', pinyin: 'zhanjiang', firstLetter: 'Z' },
        { name: '遵义', code: 'ZYI', pinyin: 'zunyi', firstLetter: 'Z' },
        { name: '九寨沟', code: 'JZH', pinyin: 'jiuzhaigou', firstLetter: 'J' },
        { name: '西双版纳', code: 'JHG', pinyin: 'xishuangbanna', firstLetter: 'X' },
        { name: '腾冲', code: 'TCZ', pinyin: 'tengchong', firstLetter: 'T' },
        { name: '林芝', code: 'LZY', pinyin: 'linzhi', firstLetter: 'L' },
        { name: '香格里拉', code: 'DIG', pinyin: 'xianggelila', firstLetter: 'X' }
      ]
    };
  },
  
  computed: {
    filteredCityList() {
      let filtered = [...this.cityList];
      if (this.citySearchKeyword) {
        const keyword = this.citySearchKeyword.toLowerCase();
        filtered = filtered.filter(city => 
          city.name.includes(keyword) || 
          city.pinyin.includes(keyword) || 
          city.code.toLowerCase().includes(keyword)
        );
      }
      const grouped = {};
      filtered.forEach(city => {
        const letter = city.firstLetter;
        if (!grouped[letter]) grouped[letter] = [];
        grouped[letter].push(city);
      });
      const sorted = {};
      Object.keys(grouped).sort().forEach(key => { sorted[key] = grouped[key]; });
      return sorted;
    }
  },
 onLoad() {
   // 1. 设置默认日期（7天后）
   const date = new Date();
   date.setDate(date.getDate() + 7);
   const year = date.getFullYear();
   const month = String(date.getMonth() + 1).padStart(2, '0');
   const day = String(date.getDate()).padStart(2, '0');
   this.searchParams.fromDate = `${year}-${month}-${day}`;
 
   // 2. 从存储中读取外部传入的搜索参数
   const externalParams = uni.getStorageSync('flight_search_params');
   if (externalParams) {
     if (externalParams.fromCity) {
       const matched = this.cityList.find(c => 
         c.name === externalParams.fromCity || c.code === externalParams.fromCity
       );
       if (matched) {
         this.searchParams.fromCity = matched.code;
         this.searchParams.fromCityName = matched.name;
       }
     }
     if (externalParams.toCity) {
       const matched = this.cityList.find(c => 
         c.name === externalParams.toCity || c.code === externalParams.toCity
       );
       if (matched) {
         this.searchParams.toCity = matched.code;
         this.searchParams.toCityName = matched.name;
       }
     }
     if (externalParams.fromDate) {
       this.searchParams.fromDate = externalParams.fromDate;
     }
     // 使用后清除，避免重复使用
     uni.removeStorageSync('flight_search_params');
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
      this.citySearchKeyword = '';
      this.showCityPickerFlag = true;
    },
    
    closeCityPicker() {
      this.showCityPickerFlag = false;
      this.citySearchKeyword = '';
    },
    
    filterCities() {},
    
    selectCity(city) {
      if (this.cityPickerType === 'from') {
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
      if (!dateTimeStr) return '--';
      try {
        if (dateTimeStr.includes(' ')) {
          const timePart = dateTimeStr.split(' ')[1];
          return timePart.substring(0, 5);
        }
        if (dateTimeStr.includes('T')) {
          const parts = dateTimeStr.split('T');
          const timePart = parts[1];
          return timePart.substring(0, 5);
        }
        return dateTimeStr;
      } catch(e) {
        return dateTimeStr;
      }
    },
    
    calculateDurationFromMinutes(minutes) {
      if (!minutes) return '--';
      const mins = parseInt(minutes);
      if (isNaN(mins)) return '--';
      const hours = Math.floor(mins / 60);
      const minsLeft = mins % 60;
      if (hours > 0) {
        return `${hours}小时${minsLeft}分`;
      }
      return `${minsLeft}分`;
    },
    
    searchFlights() {
      if (!this.searchParams.fromCity) { 
        uni.showToast({ title: '请选择出发城市', icon: 'none' }); 
        return; 
      }
      if (!this.searchParams.toCity) { 
        uni.showToast({ title: '请选择到达城市', icon: 'none' }); 
        return; 
      }
      if (this.searchParams.fromCity === this.searchParams.toCity) { 
        uni.showToast({ title: '出发城市和到达城市不能相同', icon: 'none' }); 
        return; 
      }
      if (!this.searchParams.fromDate) { 
        uni.showToast({ title: '请选择出发日期', icon: 'none' }); 
        return; 
      }
      
      this.isLoading = true;
      uni.request({
        url: 'http://localhost:8080/api/flights/search',
        method: 'GET',
        data: { 
          dep: this.searchParams.fromCity, 
          arr: this.searchParams.toCity, 
          date: this.searchParams.fromDate 
        },
        success: (res) => {
          console.log('航班查询返回：', res.data);
          
          let flights = [];
          
          if (res.data && res.data.success && typeof res.data.data === 'string') {
            console.log('原始数据前500字符：', res.data.data.substring(0, 500));
            flights = this.parseFlightsFromString(res.data.data);
          } else if (res.data && res.data.code === 200 && res.data.data && Array.isArray(res.data.data)) {
            flights = res.data.data;
          } else if (res.data && res.data.success && res.data.data && Array.isArray(res.data.data)) {
            flights = res.data.data;
          }
          
          if (flights.length > 0) {
            this.flightList = flights.slice(0, 20);
            console.log('解析出的航班数量：', this.flightList.length);
          } else {
            this.flightList = this.getMockFlights();
            uni.showToast({ title: '使用演示数据', icon: 'none' });
          }
          this.showResultFlag = true;
        },
        fail: (err) => {
          console.error('请求失败：', err);
          this.flightList = this.getMockFlights();
          this.showResultFlag = true;
          uni.showToast({ title: '查询失败，使用演示数据', icon: 'none' });
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
        '北京-上海': [850, 720, 680, 1280, 980],
        '上海-北京': [850, 720, 680, 1280, 980],
        '北京-广州': [1280, 1180, 1080, 1580, 1380],
        '广州-北京': [1280, 1180, 1080, 1580, 1380],
        '上海-深圳': [890, 820, 750, 1180, 950],
        '深圳-上海': [890, 820, 750, 1180, 950],
        '北京-深圳': [1350, 1250, 1150, 1680, 1450],
        '深圳-北京': [1350, 1250, 1150, 1680, 1450],
        '上海-广州': [1120, 1020, 920, 1380, 1180],
        '广州-上海': [1120, 1020, 920, 1380, 1180]
      };
      const routeKey = `${fromCity}-${toCity}`;
      const prices = priceMap[routeKey] || [680, 580, 520, 880, 750];
      
      return [
        { 
          flightNumber: 'CA1234', 
          airline: '中国国航', 
          departureAirport: fromCity + '首都机场', 
          arrivalAirport: toCity + '虹桥机场', 
          departureTime: selectedDate + ' 08:00', 
          arrivalTime: selectedDate + ' 10:30', 
          departureTimeDisplay: '08:00', 
          arrivalTimeDisplay: '10:30', 
          duration: '2小时30分', 
          price: prices[0] 
        },
        { 
          flightNumber: 'MU5678', 
          airline: '东方航空', 
          departureAirport: fromCity + '大兴机场', 
          arrivalAirport: toCity + '浦东机场', 
          departureTime: selectedDate + ' 14:00', 
          arrivalTime: selectedDate + ' 16:20', 
          departureTimeDisplay: '14:00', 
          arrivalTimeDisplay: '16:20', 
          duration: '2小时20分', 
          price: prices[1] 
        },
        { 
          flightNumber: 'CZ9012', 
          airline: '南方航空', 
          departureAirport: fromCity + '首都机场', 
          arrivalAirport: toCity + '虹桥机场', 
          departureTime: selectedDate + ' 18:30', 
          arrivalTime: selectedDate + ' 21:00', 
          departureTimeDisplay: '18:30', 
          arrivalTimeDisplay: '21:00', 
          duration: '2小时30分', 
          price: prices[2] 
        },
        { 
          flightNumber: 'HU7890', 
          airline: '海南航空', 
          departureAirport: fromCity + '首都机场', 
          arrivalAirport: toCity + '浦东机场', 
          departureTime: selectedDate + ' 09:15', 
          arrivalTime: selectedDate + ' 11:45', 
          departureTimeDisplay: '09:15', 
          arrivalTimeDisplay: '11:45', 
          duration: '2小时30分', 
          price: prices[3] 
        },
        { 
          flightNumber: '3U4567', 
          airline: '四川航空', 
          departureAirport: fromCity + '大兴机场', 
          arrivalAirport: toCity + '虹桥机场', 
          departureTime: selectedDate + ' 16:30', 
          arrivalTime: selectedDate + ' 18:50', 
          departureTimeDisplay: '16:30', 
          arrivalTimeDisplay: '18:50', 
          duration: '2小时20分', 
          price: prices[4] 
        }
      ];
    },
    
    parseFlightsFromString(dataStr) {
      const flights = [];
      try {
        const allFlights = dataStr.match(/\{\s*'fcategory':[^}]+\}/g);
        if (allFlights && allFlights.length > 0) {
          console.log('直接找到航班对象数量：', allFlights.length);
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
            console.log('从data数组找到航班数量：', flightMatches.length);
            return this.extractFlightsFromMatches(flightMatches);
          }
        }
        
        console.log('未找到任何航班数据');
        return [];
        
      } catch (e) {
        console.error('解析航班数据失败：', e);
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
          
          const flightState = stateMatch ? stateMatch[1] : '';
          
          if (flightState === '计划' && flightNoMatch && depTimeMatch && arrTimeMatch) {
            const depTime = depTimeMatch[1].replace('T', ' ');
            const arrTime = arrTimeMatch[1].replace('T', ' ');
            const durationMin = durationMatch ? durationMatch[1] : '';
            
            flights.push({
              flightNumber: flightNoMatch[1],
              airline: airlineMatch ? airlineMatch[1] : '航空公司',
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
        } catch(e) {
          console.error('提取航班失败：', e);
        }
      }
      
      return flights;
    },
    
    closeResult() { 
      this.showResultFlag = false; 
    },
    
    selectFlight(flight) {
      uni.setStorageSync('selectedFlight', flight);
      this.showResultFlag = false;
      uni.navigateTo({ url: '/pages/flight/order' });
    }
  }
};
</script>

<style scoped>
page { background: #f5f7fa; }

.container {
  width: 100%;
  min-height: 100vh;
  box-sizing: border-box;
  padding-bottom: 20rpx;
}

.header {
  position: relative;
  padding: 60rpx 0 50rpx;
  text-align: center;
  overflow: hidden;
}

.header-bg {
  position: absolute;
  top: -50%;
  left: -20%;
  width: 140%;
  height: 140%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  border-radius: 50%;
  z-index: -1;
}

.header .title {
  display: block;
  color: #fff;
  font-size: 48rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.header .subtitle {
  display: block;
  color: rgba(255,255,255,0.8);
  font-size: 26rpx;
}

.swap-city {
  position: relative;
  margin: -30rpx auto 0;
  width: 80rpx;
  height: 80rpx;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.swap-icon { font-size: 36rpx; color: #667eea; }

.search-form {
  background: #fff;
  margin: 30rpx 30rpx;
  border-radius: 32rpx;
  padding: 10rpx 0;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.form-item {
  display: flex;
  align-items: center;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.form-item:last-child { border-bottom: none; }

.icon-box {
  width: 60rpx;
  height: 60rpx;
  background: #f0f0f5;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.icon { font-size: 32rpx; }

.form-content { flex: 1; }

.label {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
  display: block;
}

.input-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 28rpx;
}

.input-box .selected { color: #333; font-weight: 500; }
.input-box .placeholder { color: #ccc; }
.arrow-right { color: #ccc; font-size: 32rpx; }

.search-btn {
  width: 90%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 44rpx;
  margin: 30rpx auto 30rpx;
  font-size: 32rpx;
  font-weight: bold;
  display: block;
  box-shadow: 0 8rpx 20rpx rgba(102, 126, 234, 0.3);
}

.search-btn[disabled] { opacity: 0.6; }

/* 城市选择弹窗样式 */
.city-picker-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.city-picker-container {
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  height: 80vh;
  display: flex;
  flex-direction: column;
}

.city-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.picker-title { font-size: 34rpx; font-weight: bold; color: #333; }
.close-btn { font-size: 44rpx; color: #999; padding: 10rpx; }

.city-search { padding: 20rpx 30rpx; border-bottom: 1rpx solid #eee; }
.search-input {
  background: #f5f5f5;
  border-radius: 40rpx;
  padding: 20rpx 30rpx;
  font-size: 28rpx;
}

.hot-cities { padding: 20rpx 30rpx; border-bottom: 10rpx solid #f5f5f5; }
.section-title { font-size: 28rpx; color: #999; margin-bottom: 20rpx; }

.city-grid { display: flex; flex-wrap: wrap; gap: 20rpx; }
.city-item {
  flex: 1;
  min-width: 28%;
  padding: 20rpx 0;
  text-align: center;
  background: #f5f5f5;
  border-radius: 16rpx;
}
.city-name { font-size: 28rpx; color: #333; display: block; }
.city-code { font-size: 22rpx; color: #999; display: block; }

.city-list { flex: 1; overflow-y: auto; padding: 0 30rpx; }
.city-group { margin-bottom: 20rpx; }
.group-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #667eea;
  padding: 20rpx 0;
  background: #fff;
  position: sticky;
  top: 0;
}
.city-item-row {
  display: flex;
  justify-content: space-between;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.city-name-text { font-size: 30rpx; color: #333; }
.city-code-text { font-size: 24rpx; color: #999; }

/* 搜索结果弹窗 */
.result-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.result-container {
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  height: 75vh;
  display: flex;
  flex-direction: column;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.result-title { font-size: 34rpx; font-weight: bold; color: #333; }

.result-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: linear-gradient(135deg, #f5f0ff, #fff);
}

.route { display: flex; align-items: center; gap: 16rpx; }
.route .from, .route .to { font-size: 32rpx; font-weight: bold; color: #333; }
.route .arrow { font-size: 28rpx; color: #667eea; }
.result-info .date { font-size: 26rpx; color: #999; }

.result-list { flex: 1; overflow-y: auto; padding: 20rpx 30rpx; }

.flight-card {
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 20rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
  border: 1rpx solid #f0f0f0;
}

.flight-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.flight-left { display: flex; align-items: baseline; gap: 16rpx; }
.flight-number { font-size: 34rpx; font-weight: bold; color: #667eea; }
.airline { font-size: 24rpx; color: #999; background: #f5f5f5; padding: 4rpx 12rpx; border-radius: 20rpx; }
.price-tag { font-size: 40rpx; font-weight: bold; color: #ff4d4f; }

.flight-info { margin-bottom: 20rpx; }
.time-info { display: flex; justify-content: space-between; align-items: center; }

.departure, .arrival { text-align: center; }
.departure .time, .arrival .time { font-size: 32rpx; font-weight: bold; color: #333; display: block; }
.departure .airport, .arrival .airport { font-size: 22rpx; color: #999; margin-top: 6rpx; display: block; }

.flight-line {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 30rpx;
}
.flight-line .duration { font-size: 22rpx; color: #999; margin-bottom: 8rpx; }
.flight-line .line { width: 100%; height: 2rpx; background: #e0e0e0; margin: 8rpx 0; }
.flight-line .arrow-icon { font-size: 28rpx; color: #667eea; }

.flight-footer { text-align: right; }
.book-hint {
  font-size: 26rpx;
  color: #fff;
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 12rpx 28rpx;
  border-radius: 40rpx;
  display: inline-block;
}

.empty-result {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}

.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; opacity: 0.5; }
.empty-result .try-again { margin-top: 20rpx; font-size: 26rpx; color: #ccc; }
</style>