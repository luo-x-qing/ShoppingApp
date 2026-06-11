<template>
  <view class="container">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="title">🏞️ 旅游景点</text>
    </view>

    <!-- 搜索框 -->
    <view class="search-box">
      <input 
        class="search-input" 
        placeholder="搜索当前城市景点" 
        v-model="keyword" 
        @confirm="searchSpot"
      />
      <button class="search-btn" @click="searchSpot">搜索</button>
    </view>

    <!-- 轮播图 -->
    <swiper class="banner" autoplay circular interval="3000">
      <swiper-item v-for="(item, index) in banners" :key="index">
        <image :src="item.url" class="banner-img" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <!-- 省份 + 城市 选择器 -->
    <view class="select-row">
      <view class="select-wrapper" style="flex: 1;">
        <picker 
          mode="selector" 
          :value="selectedProvinceIndex" 
          :range="provinceNames" 
          @change="changeProvince"
        >
          <view class="select-box">
            <text class="province-text">{{ selectedProvince }}</text>
            <text class="arrow">∨</text>
          </view>
        </picker>
      </view>
      <view class="select-wrapper" style="flex: 1; margin-left: 10rpx;">
        <picker 
          mode="selector" 
          :value="selectedCityIndex" 
          :range="cityList" 
          @change="changeCity"
        >
          <view class="select-box">
            <text class="province-text">{{ selectedCity }}</text>
            <text class="arrow">∨</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 景点列表 -->
    <view class="list">
      <view 
        v-for="(item, index) in spots" 
        :key="index" 
        class="card" 
        @click="goToDetail(item.id)"
      >
        <image :src="item.image" class="card-img" mode="aspectFill" />
        <view class="card-info">
          <text class="card-name">{{ item.name }}</text>
          <view @click.stop="toggleCollect(item)">
            <text class="collect-text">
              {{ item.isCollected ? '★ 已收藏' : '☆ 收藏' }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- AI推荐路线 -->
    <view class="recommend-section" v-if="recommendedRoutes.length > 0">
      <view class="recommend-header">
        <text class="recommend-title">🤖 AI推荐路线</text>
        <text class="recommend-subtitle">基于"{{ selectedCity !== '全部城市' ? selectedCity : selectedProvince }}"热门景点智能推荐</text>
      </view>
      <view
        v-for="(route, index) in recommendedRoutes"
        :key="index"
        class="route-card"
      >
        <view class="route-top">
          <text class="route-name">{{ route.name }}</text>
          <text class="route-price">¥{{ route.price }}</text>
        </view>
        <text class="route-intro">{{ route.intro }}</text>
        <view class="route-meta">
          <text>📅 {{ route.days }}天</text>
          <text v-if="route.distance">📏 {{ route.distance }}km</text>
          <text v-if="route.duration">⏱ {{ route.duration }}</text>
        </view>
        <view class="route-actions">
          <button class="detail-btn" @click="goToRouteDetail(route.id)">查看详情</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
// 省份对应城市映射
const provinceCityMap = {
  "北京市": ["北京市"],
  "天津市": ["天津市"],
  "河北省": ["石家庄市","唐山市","秦皇岛市","邯郸市","保定市","张家口市","承德市"],
  "山西省": ["太原市","大同市","长治市","晋城市","晋中市","运城市","临汾市"],
  "内蒙古自治区": ["呼和浩特市","包头市","赤峰市","鄂尔多斯市","呼伦贝尔市"],
  "辽宁省": ["沈阳市","大连市","鞍山市","抚顺市","本溪市","锦州市"],
  "吉林省": ["长春市","吉林市","四平市","延边朝鲜族自治州"],
  "黑龙江省": ["哈尔滨市","齐齐哈尔市","牡丹江市","佳木斯市","大庆市"],
  "上海市": ["上海市"],
  "江苏省": ["南京市","无锡市","徐州市","苏州市","南通市","扬州市","镇江市","常州市"],
  "浙江省": ["杭州市","宁波市","温州市","嘉兴市","湖州市","绍兴市","金华市","台州市"],
  "安徽省": ["合肥市","芜湖市","蚌埠市","黄山市","六安市","池州市"],
  "福建省": ["福州市","厦门市","莆田市","泉州市","漳州市","南平市","龙岩市"],
  "江西省": ["南昌市","景德镇市","九江市","赣州市","吉安市","宜春市","上饶市"],
  "山东省": ["济南市","青岛市","淄博市","烟台市","潍坊市","济宁市","泰安市","威海市"],
  "河南省": ["郑州市","开封市","洛阳市","安阳市","焦作市","南阳市","信阳市"],
  "湖北省": ["武汉市","黄石市","宜昌市","襄阳市","荆州市","十堰市","恩施土家族苗族自治州"],
  "湖南省": ["长沙市","株洲市","湘潭市","衡阳市","张家界市","岳阳市","湘西土家族苗族自治州"],
  "广东省": ["广州市","深圳市","珠海市","汕头市","佛山市","韶关市","东莞市","中山市","惠州市"],
  "广西壮族自治区": ["南宁市","柳州市","桂林市","北海市","防城港市","百色市"],
  "海南省": ["海口市","三亚市","三沙市"],
  "重庆市": ["重庆市"],
  "四川省": ["成都市","自贡市","攀枝花市","泸州市","德阳市","绵阳市","乐山市","南充市","宜宾市","阿坝藏族羌族自治州","甘孜藏族自治州"],
  "贵州省": ["贵阳市","遵义市","安顺市","黔东南苗族侗族自治州","黔南布依族苗族自治州"],
  "云南省": ["昆明市","曲靖市","保山市","丽江市","大理白族自治州","迪庆藏族自治州"],
  "西藏自治区": ["拉萨市","日喀则市","林芝市"],
  "陕西省": ["西安市","宝鸡市","咸阳市","渭南市","延安市","汉中市"],
  "甘肃省": ["兰州市","嘉峪关市","天水市","张掖市","酒泉市","甘南藏族自治州"],
  "青海省": ["西宁市","海东市"],
  "宁夏回族自治区": ["银川市","石嘴山市","吴忠市"],
  "新疆维吾尔自治区": ["乌鲁木齐市","克拉玛依市","吐鲁番市","伊犁哈萨克自治州"],
  "香港特别行政区": ["香港"],
  "澳门特别行政区": ["澳门"],
  "台湾省": ["台北市","高雄市","台中市","台南市"]
};

export default {
  data() {
    return {
      selectedProvince: "四川省",
      selectedProvinceIndex: 22,
      selectedCityIndex: 0,
      keyword: "",
      provinceNames: Object.keys(provinceCityMap),
      provinceCityMap: provinceCityMap,
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
    const username = uni.getStorageSync("loginUsername") || "";
    const key = "myCollection_" + username;
    let saved = uni.getStorageSync(key);
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
      uni.request({
        url: url,
        method: "GET",
        success: (res) => {
          if (res.statusCode !== 200 || !Array.isArray(res.data)) {
            this.spots = [];
            return;
          }
          this.spots = res.data.map(item => ({
            id: item.id,
            name: item.name,
            image: "http://localhost:8080" + item.photo,
            desc: item.description,
            city: item.city,
            isCollected: this.collectedSpots.some(i => i.id === item.id)
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
      uni.request({
        url: url,
        success: (res) => {
          if (res.statusCode !== 200 || !Array.isArray(res.data)) {
            this.spots = [];
            return;
          }
          this.spots = res.data.map(item => ({
            id: item.id,
            name: item.name,
            image: "http://localhost:8080" + item.photo,
            desc: item.description,
            city: item.city,
            isCollected: this.collectedSpots.some(i => i.id === item.id)
          }));
        },
        fail: () => {
          this.spots = [];
        }
      });
    },

    toggleCollect(spot) {
      const username = uni.getStorageSync("loginUsername") || "";
      const key = "myCollection_" + username;
      let arr = JSON.parse(JSON.stringify(this.collectedSpots));
      const index = arr.findIndex(i => i.id === spot.id);

      if (index > -1) {
        arr.splice(index, 1);
        uni.showToast({ title: "取消收藏" });
      } else {
        arr.push(spot);
        uni.showToast({ title: "收藏成功" });
      }

      this.collectedSpots = arr;
      uni.setStorageSync(key, arr);
      this.getSpots();
    },

    goToDetail(id) {
      uni.navigateTo({
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
      uni.request({
        url: url,
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
      uni.navigateTo({
        url: "/pages/route-detail/route-detail?id=" + id
      });
    }
  }
};
</script>

<style scoped>
page {
  background: #f5f5f5;
  margin: 0;
  padding: 0;
}

.container {
  width: 100%;
  box-sizing: border-box;
}

.header {
  background: #fff;
  padding: 25rpx 0;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
}

.search-box {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #fff;
  margin-bottom: 10rpx;
}
.search-input {
  flex: 1;
  height: 70rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx 0 0 8rpx;
  padding-left: 20rpx;
  font-size: 28rpx;
}
.search-btn {
  height: 70rpx;
  background: #1677ff;
  color: #fff;
  border-radius: 0 8rpx 8rpx 0;
  padding: 0 25rpx;
  font-size: 28rpx;
  border: none;
}

.banner {
  width: 100%;
  height: 350rpx;
}
.banner-img {
  width: 100%;
  height: 100%;
}

/* 省份+城市选择器行 */
.select-row {
  display: flex;
  padding: 20rpx;
  background: #fff;
  box-sizing: border-box;
}
.select-wrapper {
  box-sizing: border-box;
  background: #fff;
}
.select-box {
  height: 70rpx;
  line-height: 70rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
  display: flex;
  justify-content: flex-start;
  align-items: center;
}
.province-text {
  color: #1677ff;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.arrow {
  margin-left: 6rpx;
  color: #1677ff;
}

.list {
  padding: 20rpx;
}
.card {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  box-shadow: 0 1rpx 5rpx rgba(0,0,0,0.1);
}
.card-img {
  width: 100%;
  height: 300rpx;
}
.card-info {
  display: flex;
  justify-content: space-between;
  padding: 20rpx;
  align-items: center;
}
.card-name {
  font-size: 30rpx;
  font-weight: 500;
}
.collect-text {
  font-size: 26rpx;
  color: #ff9800;
}

.recommend-section {
  padding: 20rpx;
}
.recommend-header {
  margin-bottom: 20rpx;
}
.recommend-title {
  font-size: 34rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 6rpx;
}
.recommend-subtitle {
  font-size: 24rpx;
  color: #999;
}
.route-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  color: #fff;
}
.route-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}
.route-name {
  font-size: 34rpx;
  font-weight: bold;
}
.route-price {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffd700;
}
.route-intro {
  font-size: 26rpx;
  opacity: 0.9;
  display: block;
  margin-bottom: 12rpx;
  line-height: 1.5;
}
.route-meta {
  display: flex;
  gap: 20rpx;
  font-size: 24rpx;
  opacity: 0.85;
  margin-bottom: 16rpx;
}
.route-actions {
  display: flex;
  justify-content: flex-end;
}
.detail-btn {
  background: rgba(255,255,255,0.25);
  color: #fff;
  border: 1rpx solid rgba(255,255,255,0.5);
  border-radius: 8rpx;
  padding: 10rpx 30rpx;
  font-size: 26rpx;
}
</style>