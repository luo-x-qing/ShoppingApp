<template>
  <view class="container">
    <!-- 头部背景 -->
    <view class="header-bg"></view>
    
    <!-- 主要内容 -->
    <view class="main-content">
      <!-- 标签切换 -->
      <view class="tab-bar">
        <view class="tab-item" :class="tab === 0 ? 'active' : ''" @click="switchTab(0)">
          <text class="tab-icon">🏨</text>
          <text class="tab-text">酒店收藏</text>
          <text class="tab-badge" v-if="hotelList.length">{{ hotelList.length }}</text>
        </view>
        <view class="tab-item" :class="tab === 1 ? 'active' : ''" @click="switchTab(1)">
          <text class="tab-icon">🏛️</text>
          <text class="tab-text">景点收藏</text>
          <text class="tab-badge" v-if="spotList.length">{{ spotList.length }}</text>
        </view>
      </view>

      <!-- 列表内容 -->
      <scroll-view 
        scroll-y 
        class="list-content" 
        :style="{ height: scrollHeight }"
        :refresher-enabled="true"
        @refresherrefresh="onRefresh"
        :refresher-triggered="refreshing"
      >
        <!-- 酒店收藏 -->
        <view v-if="tab === 0">
          <view class="empty-state" v-if="hotelList.length === 0">
            <view class="empty-icon">🏝️</view>
            <text class="empty-title">暂无酒店收藏</text>
            <text class="empty-desc">去探索你喜欢的酒店吧~</text>
            <button class="explore-btn" @click="goExploreHotel">去探索</button>
          </view>

          <view 
            class="hotel-card" 
            v-for="(item, index) in hotelList" 
            :key="item.id || index" 
            @click="viewHotelDetail(item)"
          >
            <view class="card-image-wrapper">
              <image class="card-image" :src="getHotelImage(item)" mode="aspectFill"></image>
              <view class="card-tag" v-if="item.starLevel">
                <text class="tag-text">{{ item.starLevel }}星级</text>
              </view>
            </view>
            
            <view class="card-content">
              <view class="card-header">
                <text class="hotel-name">{{ item.name }}</text>
                <view class="delete-btn" @click.stop="deleteHotel(index)">
                  <text class="delete-icon">🗑️</text>
                </view>
              </view>
              
              <view class="hotel-location" v-if="item.address">
                <text class="location-icon">📍</text>
                <text class="location-text">{{ item.address }}</text>
              </view>
              
              <view class="hotel-footer">
                <view class="price-info">
                  <text class="price-symbol">¥</text>
                  <text class="price-value">{{ item.price }}</text>
                  <text class="price-unit">/晚</text>
                </view>
                <view class="rating-info" v-if="item.avgRating">
                  <text class="rating-star">⭐</text>
                  <text class="rating-value">{{ item.avgRating }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 景点收藏 -->
        <view v-if="tab === 1">
          <view class="empty-state" v-if="spotList.length === 0">
            <view class="empty-icon">🗺️</view>
            <text class="empty-title">暂无景点收藏</text>
            <text class="empty-desc">去发现美丽的景点吧~</text>
            <button class="explore-btn" @click="goExploreSpot">去探索</button>
          </view>

          <view 
            class="spot-card" 
            v-for="(item, index) in spotList" 
            :key="item.id || index" 
            @click="viewSpotDetail(item)"
          >
            <view class="spot-image-wrapper">
              <image class="spot-image" :src="getSpotImage(item)" mode="aspectFill"></image>
            </view>
            
            <view class="spot-content">
              <view class="spot-header">
                <text class="spot-name">{{ item.name }}</text>
                <view class="delete-btn" @click.stop="deleteSpot(index)">
                  <text class="delete-icon">🗑️</text>
                </view>
              </view>
              
              <view class="spot-location" v-if="item.address">
                <text class="location-icon">📍</text>
                <text class="location-text">{{ item.address }}</text>
              </view>
              
              <view class="spot-tags" v-if="item.category">
                <view class="tag">
                  <text class="tag-text-small">{{ item.category }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
export default {
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
    this.username = uni.getStorageSync("loginUsername") || "";
    this.loadCollection();
  },

  methods: {
    getScreenHeight() {
      try {
        const windowInfo = uni.getWindowInfo();
        const screenHeight = windowInfo.windowHeight;
        this.scrollHeight = (screenHeight - 200) + "px";
      } catch (e) {
        this.scrollHeight = "calc(100vh - 200rpx)";
      }
    },

    getHotelImage(item) {
      const url = item?.coverImage || item?.image;
      if (!url) return this.defaultHotelImage;
      if (url.startsWith("data:")) return url;
      if (url.startsWith("http")) return url;
      if (url.startsWith("/")) return this.baseUrl + url;
      return this.baseUrl + "/" + url;
    },

    getSpotImage(item) {
      const url = item?.coverImage || item?.image || item?.picUrl;
      if (!url) return this.defaultSpotImage;
      if (url.startsWith("data:")) return url;
      if (url.startsWith("http")) return url;
      if (url.startsWith("/")) return this.baseUrl + url;
      return this.baseUrl + "/" + url;
    },

    switchTab(index) {
      this.tab = index;
    },

    loadCollection() {
      const key = "myCollection_" + this.username;
      let data = uni.getStorageSync(key) || [];
      
      this.hotelList = data.filter((i) => i && i.price !== undefined && i.price !== null);
      this.spotList = data.filter((i) => i && (i.price === undefined || i.price === null));
      
      console.log("加载收藏完成 - 酒店:", this.hotelList.length, "景点:", this.spotList.length);
    },

    deleteHotel(idx) {
      if (idx < 0 || idx >= this.hotelList.length) {
        console.error("无效的酒店索引:", idx);
        uni.showToast({ title: "删除失败", icon: "none" });
        return;
      }
      
      const target = this.hotelList[idx];
      if (!target || !target.id) {
        console.error("酒店数据无效:", target);
        uni.showToast({ title: "数据错误", icon: "none" });
        return;
      }
      
      uni.showModal({
        title: '提示',
        content: '确定要删除这个酒店收藏吗？',
        success: (res) => {
          if (res.confirm) {
            const key = "myCollection_" + this.username;
            let all = uni.getStorageSync(key) || [];
            let result = all.filter((i) => i && i.id !== target.id);
            uni.setStorageSync(key, result);
            this.loadCollection();
            uni.showToast({ title: "已删除", icon: "success" });
          }
        }
      });
    },

    deleteSpot(idx) {
      if (idx < 0 || idx >= this.spotList.length) {
        console.error("无效的景点索引:", idx);
        uni.showToast({ title: "删除失败", icon: "none" });
        return;
      }
      
      const target = this.spotList[idx];
      if (!target || !target.id) {
        console.error("景点数据无效:", target);
        uni.showToast({ title: "数据错误", icon: "none" });
        return;
      }
      
      uni.showModal({
        title: '提示',
        content: '确定要删除这个景点收藏吗？',
        success: (res) => {
          if (res.confirm) {
            const key = "myCollection_" + this.username;
            let all = uni.getStorageSync(key) || [];
            let result = all.filter((i) => i && i.id !== target.id);
            uni.setStorageSync(key, result);
            this.loadCollection();
            uni.showToast({ title: "已删除", icon: "success" });
          }
        }
      });
    },

    viewHotelDetail(item) {
      console.log("跳转酒店详情，ID:", item?.id, "名称:", item?.name);
      
      if (!item || !item.id) {
        uni.showToast({ title: "酒店信息错误", icon: "none" });
        return;
      }
      
      uni.navigateTo({
        url: `/pages/hotelDetail/hotelDetail?id=${item.id}`,
        success: () => {
          console.log("跳转酒店详情成功");
        },
        fail: (err) => {
          console.error("跳转酒店详情失败:", err);
          uni.showToast({ 
            title: "页面跳转失败，请检查路径", 
            icon: "none",
            duration: 2000
          });
        }
      });
    },

    viewSpotDetail(item) {
      console.log("跳转景点详情，ID:", item?.id, "名称:", item?.name);
      
      if (!item || !item.id) {
        uni.showToast({ title: "景点信息错误", icon: "none" });
        return;
      }
      
      uni.navigateTo({
        url: `/pages/detail/detail?id=${item.id}`,
        success: () => {
          console.log("跳转景点详情成功");
        },
        fail: (err) => {
          console.error("跳转景点详情失败:", err);
          uni.showToast({ 
            title: "页面跳转失败，请检查路径", 
            icon: "none",
            duration: 2000
          });
        }
      });
    },

    // 酒店去探索 - 跳转到酒店页面
    goExploreHotel() {
      uni.switchTab({
        url: '/pages/category/category'
      });
    },

    // 景点去探索 - 跳转到景点页面
    goExploreSpot() {
      uni.switchTab({
        url: '/pages/home/home'
      });
    },

    onRefresh() {
      this.refreshing = true;
      this.loadCollection();
      setTimeout(() => {
        this.refreshing = false;
        uni.showToast({ title: "刷新成功", icon: "success" });
      }, 1000);
    }
  }
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f4f8 0%, #d1eef0 100%);
  position: relative;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 240rpx;
  background: linear-gradient(135deg, #4A90E2 0%, #5BA0E8 100%);
  border-radius: 0 0 40rpx 40rpx;
}

.main-content {
  position: relative;
  z-index: 1;
  padding: 20rpx 30rpx;
}

.tab-bar {
  display: flex;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 60rpx;
  padding: 6rpx;
  margin-bottom: 30rpx;
  margin-top: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx;
  border-radius: 54rpx;
  transition: all 0.3s ease;
  position: relative;
}

.tab-item.active {
  background: linear-gradient(135deg, #4A90E2 0%, #5BA0E8 100%);
  box-shadow: 0 6rpx 16rpx rgba(74, 144, 226, 0.25);
}

.tab-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.tab-text {
  font-size: 28rpx;
  color: #666;
  font-weight: 500;
}

.active .tab-text {
  color: #fff;
}

.tab-badge {
  position: absolute;
  top: -8rpx;
  right: 20rpx;
  background: #FF6B6B;
  color: #fff;
  font-size: 20rpx;
  padding: 2rpx 10rpx;
  border-radius: 20rpx;
}

.list-content {
  padding-bottom: 30rpx;
}

.hotel-card {
  background: #fff;
  border-radius: 28rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.hotel-card:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.card-image-wrapper {
  position: relative;
  height: 340rpx;
  overflow: hidden;
  background: #f5f7fa;
}

.card-image {
  width: 100%;
  height: 100%;
}

.card-tag {
  position: absolute;
  top: 20rpx;
  left: 20rpx;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(8rpx);
  padding: 6rpx 18rpx;
  border-radius: 30rpx;
}

.tag-text {
  color: #fff;
  font-size: 24rpx;
}

.card-content {
  padding: 24rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.hotel-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #2c3e50;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.delete-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.delete-btn:active {
  background: #fee;
  transform: scale(0.95);
}

.delete-icon {
  font-size: 30rpx;
  opacity: 0.55;
}

.hotel-location,
.spot-location {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.location-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.location-text {
  font-size: 24rpx;
  color: #aaa;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hotel-footer {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.price-info {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 26rpx;
  color: #E86A3A;
  font-weight: 600;
}

.price-value {
  font-size: 40rpx;
  color: #E86A3A;
  font-weight: bold;
  margin: 0 4rpx;
}

.price-unit {
  font-size: 22rpx;
  color: #bbb;
}

.rating-info {
  display: flex;
  align-items: center;
}

.rating-star {
  font-size: 26rpx;
  margin-right: 6rpx;
}

.rating-value {
  font-size: 26rpx;
  color: #F4B942;
  font-weight: 600;
}

.spot-card {
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  display: flex;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.spot-card:active {
  transform: scale(0.98);
}

.spot-image-wrapper {
  width: 200rpx;
  height: 200rpx;
  overflow: hidden;
  background: #f5f7fa;
}

.spot-image {
  width: 100%;
  height: 100%;
}

.spot-content {
  flex: 1;
  padding: 24rpx;
}

.spot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.spot-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #2c3e50;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.spot-tags {
  margin-top: 12rpx;
}

.tag {
  display: inline-block;
  background: #f0f4f8;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.tag-text-small {
  font-size: 22rpx;
  color: #7f8c8d;
}

.empty-state {
  text-align: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-title {
  font-size: 32rpx;
  color: #5a6e7a;
  display: block;
  margin-bottom: 12rpx;
  font-weight: 500;
}

.empty-desc {
  font-size: 26rpx;
  color: #8aa0ae;
  display: block;
  margin-bottom: 36rpx;
}

.explore-btn {
  background: linear-gradient(135deg, #4A90E2 0%, #5BA0E8 100%);
  color: #fff;
  border: none;
  border-radius: 48rpx;
  padding: 20rpx 50rpx;
  font-size: 28rpx;
  width: auto;
  display: inline-block;
  font-weight: 500;
  box-shadow: 0 4rpx 12rpx rgba(74, 144, 226, 0.3);
}
</style>