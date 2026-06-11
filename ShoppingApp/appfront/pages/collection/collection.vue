<template>
  <view class="container">
    <!-- 切换标签：酒店 / 景点 -->
    <view class="tab-bar">
      <view class="tab-item" :class="tab === 0 ? 'active' : ''" @click="tab = 0">
        酒店收藏
      </view>
      <view class="tab-item" :class="tab === 1 ? 'active' : ''" @click="tab = 1">
        景点收藏
      </view>
    </view>

    <!-- 列表内容 -->
    <scroll-view scroll-y class="list-content">
      <!-- 酒店收藏 -->
      <view v-if="tab === 0">
        <view class="empty" v-if="hotelList.length === 0">
          暂无酒店收藏
        </view>

        <view class="item-card" v-for="(item, index) in hotelList" :key="index">
          <image class="item-img" :src="item.image" mode="aspectFill" />
          <view class="item-info">
            <text class="item-name">{{ item.name }}</text>
            <text class="item-price">¥{{ item.price }}/晚</text>
          </view>
          <view class="del-btn" @click="deleteHotel(index)">删除</view>
        </view>
      </view>

      <!-- 景点收藏 -->
      <view v-if="tab === 1">
        <view class="empty" v-if="spotList.length === 0">
          暂无景点收藏
        </view>

        <view class="item-card" v-for="(item, index) in spotList" :key="index">
          <image class="item-img" :src="item.image" mode="aspectFill" />
          <view class="item-info">
            <text class="item-name">{{ item.name }}</text>
          </view>
          <view class="del-btn" @click="deleteSpot(index)">删除</view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      tab: 0,
      hotelList: [],
      spotList: [],
      username: ""
    };
  },

  onShow() {
    // 获取当前登录的用户名
    this.username = uni.getStorageSync("loginUsername") || "";
    this.loadCollection();
  },

  methods: {
    loadCollection() {
      // 每个用户独立缓存，互不干扰
      const key = "myCollection_" + this.username;
      let data = uni.getStorageSync(key) || [];
      this.hotelList = data.filter((i) => i.price !== undefined);
      this.spotList = data.filter((i) => i.price === undefined);
    },

    deleteHotel(idx) {
      const key = "myCollection_" + this.username;
      let all = uni.getStorageSync(key) || [];
      let target = this.hotelList[idx];
      let result = all.filter((i) => i.id !== target.id);

      uni.setStorageSync(key, result);
      this.loadCollection();
      uni.showToast({ title: "已删除" });
    },

    deleteSpot(idx) {
      const key = "myCollection_" + this.username;
      let all = uni.getStorageSync(key) || [];
      let target = this.spotList[idx];
      let result = all.filter((i) => i.id !== target.id);

      uni.setStorageSync(key, result);
      this.loadCollection();
      uni.showToast({ title: "已删除" });
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
  background: #f5f5f5;
  min-height: 100vh;
}

/* 顶部标题栏（和个人中心、酒店页面统一） */
.header {
  background: #fff;
  padding: 25rpx 0;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

/* 标签栏 */
.tab-bar {
  display: flex;
  background: #fff;
  margin-bottom: 20rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  position: relative;
}

.tab-item.active {
  color: #1677ff;
  font-weight: bold;
}

.tab-item.active::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 4rpx;
  background: #1677ff;
  border-radius: 2rpx;
}

/* 列表 */
.list-content {
  padding: 0 20rpx 20rpx;
}

.item-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.item-img {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 30rpx;
  font-weight: bold;
  display: block;
  color: #333;
}

.item-price {
  font-size: 24rpx;
  color: #ff6b35;
  margin-top: 6rpx;
}

.del-btn {
  color: #1677ff;
  font-size: 26rpx;
  padding: 8rpx 12rpx;
  border-radius: 8rpx;
  background: #fff0f0;
}

.empty {
  text-align: center;
  padding: 80rpx 0;
  color: #999;
  font-size: 28rpx;
}
</style>