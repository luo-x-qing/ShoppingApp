<template>
  <view class="container">
    <view class="header">
      <text class="title">🏨 酒店预订</text>
    </view>

    <view class="search-box">
      <input class="search-input" placeholder="搜索酒店" v-model="keyword" @confirm="searchHotel" />
      <button class="search-btn" @click="searchHotel">搜索</button>
    </view>

    <view class="main-wrap">
      <view class="left-tab">
        <view v-for="(item, index) in categoryList" :key="index"
          class="tab-item" :class="{ active: activeTab === index }"
          @click="changeTab(index)">
          {{ item }}
        </view>
      </view>

      <view class="right-list">
        <view 
          v-for="item in hotelList" 
          :key="item.id" 
          class="card"
          @click="goHotelDetail(item.id)"
        >
          <image :src="item.image" class="card-img" mode="aspectFill" />
          <view class="card-info">
            <view>
              <text class="card-name">{{ item.name }}</text>
              <text class="card-desc">{{ item.starLevel }}星 · {{ item.category }}</text>
              <text class="card-price">¥{{ item.price }}/晚</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
    <RouteFloat/>
</template>

<script>
export default {
  data() {
    return {
      categoryList: ["全部酒店", "度假型酒店", "商务型酒店", "公寓型酒店", "连锁酒店"],
      activeTab: 0,
      hotelList: [],
      keyword: ""
    }
  },

  onLoad() {
    this.loadData()
  },

  methods: {
    loadData() {
      let cat = this.categoryList[this.activeTab]
      let url = "http://localhost:8080/api/hotels"
      if (cat !== "全部酒店") url += "/category/" + cat

      uni.request({
        url,
        success: (res) => {
          this.hotelList = res.data.map(i => ({
            id: i.id,
            name: i.name,
            starLevel: i.starLevel,
            price: i.price,
            category: i.category,
            image: "http://localhost:8080" + i.coverImage
          }))
        }
      })
    },

    changeTab(index) {
      this.activeTab = index
      this.loadData()
    },

    searchHotel() {
      if (!this.keyword) return this.loadData()
      uni.request({
        url: "http://localhost:8080/api/hotels",
        success: (res) => {
          this.hotelList = res.data
            .filter(i => i.name.includes(this.keyword))
            .map(i => ({
              id: i.id,
              name: i.name,
              starLevel: i.starLevel,
              price: i.price,
              category: i.category,
              image: "http://localhost:8080" + i.coverImage
            }))
        }
      })
    },

    goHotelDetail(id) {
      uni.navigateTo({
        url: "/pages/hotelDetail/hotelDetail?id=" + id
      })
    }
  }
}
</script>

<style scoped>
page { background: #f5f5f5; }
.container { width: 100%; }
.header {
  background: #fff;
  padding: 25rpx 0;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
}
.search-box {
  display: flex;
  padding: 20rpx;
  background: #fff;
}
.search-input {
  flex: 1;
  height: 70rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx 0 0 8rpx;
  padding-left: 20rpx;
}
.search-btn {
  height: 70rpx;
  background: #1677ff;
  color: #fff;
  border: none;
  padding: 0 25rpx;
}
.main-wrap { display: flex; }
.left-tab { width: 220rpx; background: #fff; }
.tab-item {
  padding: 30rpx 20rpx;
  text-align: center;
  font-size: 28rpx;
  border-bottom: 1rpx solid #f2f2f2;
}
.tab-item.active {
  color: #1677ff;
  background: #f0f7ff;
  border-left: 6rpx solid #1677ff;
}
.right-list { flex: 1; padding: 20rpx; }
.card {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}
.card-img { width: 100%; height: 300rpx; }
.card-info { padding: 20rpx; }
.card-name { font-size: 30rpx; font-weight: bold; }
.card-desc { font-size: 24rpx; color: #999; }
.card-price { font-size: 28rpx; color: #ff6b35; }
</style>