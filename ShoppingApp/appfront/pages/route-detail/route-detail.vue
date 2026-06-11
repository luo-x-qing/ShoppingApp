<template>
  <view class="container">
    <view v-if="loading" class="loading">加载中...</view>
    <view v-else>
      <view class="hero">
        <text class="hero-name">{{ route.name }}</text>
        <text class="hero-price">¥{{ route.price }}</text>
      </view>

      <view class="info-grid">
        <view class="info-item">
          <text class="info-label">行程天数</text>
          <text class="info-value">{{ route.days }}天</text>
        </view>
        <view class="info-item" v-if="route.distance">
          <text class="info-label">总路程</text>
          <text class="info-value">{{ route.distance }}km</text>
        </view>
        <view class="info-item" v-if="route.duration">
          <text class="info-label">预计用时</text>
          <text class="info-value">{{ route.duration }}</text>
        </view>
      </view>

      <view class="section" v-if="route.intro">
        <text class="section-title">路线介绍</text>
        <text class="section-content">{{ route.intro }}</text>
      </view>

      <view class="section">
        <text class="section-title">行程安排</text>
        <view v-if="!route.routeScenics || route.routeScenics.length === 0" class="empty">暂无行程数据</view>
        <view
          v-for="day in dayGroups"
          :key="day.dayNumber"
          class="day-card"
        >
          <view class="day-header">
            <text class="day-tag">第{{ day.dayNumber }}天</text>
          </view>
          <view
            v-for="rs in day.scenics"
            :key="rs.id"
            class="scenic-item"
            @click="goToScenic(rs.scenic.id)"
          >
            <image
              :src="'http://localhost:8080' + (rs.scenic.photo || '')"
              class="scenic-img"
              mode="aspectFill"
            />
            <view class="scenic-info">
              <text class="scenic-name">{{ rs.scenic.name }}</text>
              <text class="scenic-desc">{{ rs.scenic.description }}</text>
              <text class="scenic-price">门票 ¥{{ rs.scenic.ticketPrice || 0 }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section" v-if="route.serviceInclude">
        <text class="section-title">费用包含</text>
        <text class="section-content">{{ route.serviceInclude }}</text>
      </view>

      <view class="section" v-if="route.serviceExclude">
        <text class="section-title">费用不含</text>
        <text class="section-content">{{ route.serviceExclude }}</text>
      </view>

      <view class="section" v-if="route.notice">
        <text class="section-title">温馨提示</text>
        <text class="section-content">{{ route.notice }}</text>
      </view>

      <view class="apply-bar">
        <view class="apply-summary">
          <text class="apply-count">{{ totalScenicCount }}个景区</text>
          <text class="apply-total">门票合计 ¥{{ totalTicketPrice }}</text>
        </view>
        <button class="apply-btn" @click="applyRoute">应用此路线</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      routeId: null,
      route: {},
      loading: true
    };
  },
  computed: {
    dayGroups() {
      const scenics = this.route.routeScenics || [];
      const map = {};
      scenics.forEach(rs => {
        const day = rs.dayNumber || 1;
        if (!map[day]) map[day] = [];
        map[day].push(rs);
      });
      return Object.keys(map).sort().map(k => ({
        dayNumber: k,
        scenics: map[k]
      }));
    },
    totalScenicCount() {
      return (this.route.routeScenics || []).length;
    },
    totalTicketPrice() {
      const scenics = this.route.routeScenics || [];
      return scenics.reduce((sum, rs) => sum + (rs.scenic && rs.scenic.ticketPrice || 0), 0);
    }
  },
  onLoad(options) {
    this.routeId = options.id;
    this.getRouteDetail();
  },
  methods: {
    getRouteDetail() {
      uni.request({
        url: `http://localhost:8080/tour-route/${this.routeId}/detail`,
        method: "GET",
        success: (res) => {
          this.route = res.data;
          this.loading = false;
        },
        fail: () => {
          uni.showToast({ title: "加载失败", icon: "none" });
          this.loading = false;
        }
      });
    },
    goToScenic(scenicId) {
      if (scenicId) {
        uni.navigateTo({ url: "/pages/detail/detail?id=" + scenicId });
      }
    },
    applyRoute() {
      const username = uni.getStorageSync("loginUsername");
      if (!username) {
        uni.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      uni.showLoading({ title: "添加中..." });
      uni.request({
        url: "http://localhost:8080/api/cart/add-route",
        method: "POST",
        data: { username, routeId: this.routeId },
        success: (res) => {
          uni.hideLoading();
          uni.showModal({
            title: "添加成功",
            content: "该路线所有景区门票已加入购物车",
            success: (confirmRes) => {
              if (confirmRes.confirm) {
                uni.navigateTo({ url: "/pages/cart/cart" });
              }
            }
          });
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: "添加失败", icon: "none" });
        }
      });
    }
  }
};
</script>

<style scoped>
page {
  background: #f5f5f5;
}
.container {
  padding-bottom: 140rpx;
}
.loading {
  text-align: center;
  padding: 100rpx;
  color: #999;
  font-size: 28rpx;
}
.hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.hero-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  flex: 1;
}
.hero-price {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffd700;
}
.info-grid {
  display: flex;
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 20rpx;
}
.info-item {
  flex: 1;
  text-align: center;
  border-right: 1rpx solid #eee;
}
.info-item:last-child {
  border-right: none;
}
.info-label {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 6rpx;
}
.info-value {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}
.section {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 30rpx;
}
.section-title {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 16rpx;
}
.section-content {
  font-size: 28rpx;
  color: #555;
  line-height: 1.7;
  display: block;
  white-space: pre-wrap;
}
.empty {
  text-align: center;
  color: #999;
  font-size: 26rpx;
  padding: 40rpx;
}
.day-card {
  margin-bottom: 20rpx;
}
.day-header {
  margin-bottom: 12rpx;
}
.day-tag {
  background: #1677ff;
  color: #fff;
  padding: 6rpx 20rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}
.scenic-item {
  display: flex;
  padding: 16rpx;
  background: #f9f9f9;
  border-radius: 12rpx;
  margin-bottom: 10rpx;
}
.scenic-img {
  width: 160rpx;
  height: 120rpx;
  border-radius: 8rpx;
  margin-right: 16rpx;
}
.scenic-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.scenic-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}
.scenic-desc {
  font-size: 22rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.scenic-price {
  font-size: 24rpx;
  color: #ff4d4f;
}

.apply-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.08);
  z-index: 100;
}
.apply-summary {
  display: flex;
  flex-direction: column;
}
.apply-count {
  font-size: 26rpx;
  color: #999;
}
.apply-total {
  font-size: 32rpx;
  font-weight: bold;
  color: #ff4d4f;
}
.apply-btn {
  background: #1677ff;
  color: #fff;
  border-radius: 12rpx;
  padding: 20rpx 50rpx;
  font-size: 30rpx;
  border: none;
}
</style>
