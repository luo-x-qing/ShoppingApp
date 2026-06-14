<template>
  <view class="container">
    <view class="header">
      <text class="header-title">🗺️ AI 智能路线规划</text>
      <text class="header-subtitle">选择景点，让AI为你规划最佳行程</text>
    </view>

    <!-- 已选景点列表 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">已选景点（{{ spots.length }}）</text>
        <text class="clear-btn" @click="clearAll">清空</text>
      </view>
      <view v-if="spots.length === 0" class="empty">
        <text class="empty-text">尚未添加景点，在景点详情页点击"加入旅游线路"</text>
      </view>
      <view v-for="(s, i) in spots" :key="s.id" class="spot-item">
        <text class="spot-index">{{ i + 1 }}</text>
        <view class="spot-info">
          <text class="spot-name">{{ s.name }}</text>
          <text class="spot-location">{{ s.province }} {{ s.city }}</text>
        </view>
        <text class="remove-btn" @click="removeSpot(i)">✕</text>
      </view>
    </view>

    <!-- 规划参数 -->
    <view class="section" v-if="spots.length > 0">
      <text class="section-title">规划参数</text>
      <view class="param-row">
        <text class="param-label">目标城市</text>
        <text class="param-value">{{ targetCity || '自动识别' }}</text>
      </view>
      <view class="param-row">
        <text class="param-label">行程天数</text>
        <view class="day-selector">
          <text class="day-btn" :class="{ active: days === d }" v-for="d in [1,2,3,4,5]" :key="d" @click="days = d">{{ d }}天</text>
        </view>
      </view>
      <button class="generate-btn" @click="generatePlan" :loading="generating">
        {{ generating ? 'AI生成中...' : '🤖 AI 生成路线' }}
      </button>
    </view>

    <!-- AI规划结果 -->
    <view class="section" v-if="planResult">
      <text class="section-title">🤖 AI 推荐行程</text>
      <view class="result-city">
        <text>📍 {{ planResult.city }} · {{ planResult.days }}天行程</text>
      </view>

      <view class="route-info" v-if="planResult.route && planResult.route.distance">
        <text class="route-stat">🚗 总路程：{{ (planResult.route.distance / 1000).toFixed(1) }} km</text>
        <text class="route-stat">⏱ 预计用时：{{ Math.round(planResult.route.duration / 60) }} 分钟</text>
      </view>

      <view class="spots-result">
        <text class="spots-result-title">推荐景点（按游玩顺序）</text>
        <view v-for="(spot, i) in planResult.spots" :key="i" class="result-spot-item">
          <text class="result-spot-index">{{ i + 1 }}</text>
          <text class="result-spot-name">{{ spot }}</text>
          <text class="result-spot-loc" v-if="planResult.locations && planResult.locations[i]">
            {{ planResult.locations[i].location }}
          </text>
        </view>
      </view>

      <view class="action-row">
        <button class="action-btn" @click="planResult = null">重新规划</button>
      </view>
    </view>
  </view>
    <RouteFloat/>
</template>

<script>
const BASE_URL = 'http://localhost:8080'

export default {
  data() {
    return {
      spots: [],
      days: 3,
      generating: false,
      planResult: null
    };
  },
  computed: {
    routePlanKey() {
      return "routePlan_" + (uni.getStorageSync("loginUsername") || "default");
    },
    targetCity() {
      const cities = [...new Set(this.spots.map(s => s.city).filter(Boolean))];
      return cities.length > 0 ? cities.join('、') : '';
    }
  },
  onShow() {
    this.loadSpots();
  },
  methods: {
    loadSpots() {
      const data = uni.getStorageSync(this.routePlanKey);
      this.spots = Array.isArray(data) ? data : [];
    },
    removeSpot(index) {
      this.spots.splice(index, 1);
      uni.setStorageSync(this.routePlanKey, this.spots);
      this.planResult = null;
    },
    clearAll() {
      uni.showModal({
        title: '提示',
        content: '确定清空所有已选景点？',
        success: (res) => {
          if (res.confirm) {
            this.spots = [];
            uni.setStorageSync(this.routePlanKey, []);
            this.planResult = null;
          }
        }
      });
    },
    generatePlan() {
      if (!this.targetCity) {
        uni.showToast({ title: '景点缺少城市信息', icon: 'none' });
        return;
      }
      this.generating = true;
      const city = this.targetCity.split('、')[0];
      uni.request({
        url: `${BASE_URL}/travel/plan?city=${encodeURIComponent(city)}&days=${this.days}`,
        method: 'GET',
        success: (res) => {
          if (res.data.error) {
            uni.showToast({ title: res.data.error, icon: 'none' });
            return;
          }
          this.planResult = res.data;
          uni.showToast({ title: '路线生成成功', icon: 'success' });
        },
        fail: () => {
          uni.showToast({ title: 'AI规划失败，请重试', icon: 'none' });
        },
        complete: () => {
          this.generating = false;
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
  padding: 20rpx;
  padding-bottom: 40rpx;
}
.header {
  text-align: center;
  padding: 30rpx 0;
}
.header-title {
  font-size: 38rpx;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.header-subtitle {
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
  display: block;
}
.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}
.section-title {
  font-size: 30rpx;
  font-weight: bold;
}
.clear-btn {
  font-size: 26rpx;
  color: #ff4d4f;
}
.empty {
  padding: 40rpx 0;
  text-align: center;
}
.empty-text {
  font-size: 26rpx;
  color: #999;
}
.spot-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.spot-item:last-child {
  border-bottom: none;
}
.spot-index {
  width: 40rpx;
  height: 40rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 50%;
  text-align: center;
  line-height: 40rpx;
  font-size: 22rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}
.spot-info {
  flex: 1;
}
.spot-name {
  font-size: 28rpx;
  font-weight: 500;
  display: block;
}
.spot-location {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}
.remove-btn {
  width: 40rpx;
  height: 40rpx;
  text-align: center;
  line-height: 40rpx;
  color: #ff4d4f;
  font-size: 28rpx;
}
.param-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.param-label {
  font-size: 28rpx;
  color: #333;
}
.param-value {
  font-size: 28rpx;
  color: #667eea;
  font-weight: 500;
}
.day-selector {
  display: flex;
  gap: 12rpx;
}
.day-btn {
  padding: 8rpx 20rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #666;
}
.day-btn.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-color: transparent;
}
.generate-btn {
  width: 100%;
  margin-top: 24rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 12rpx;
  padding: 20rpx 0;
  font-size: 30rpx;
  border: none;
}
.result-city {
  font-size: 28rpx;
  color: #667eea;
  font-weight: 500;
  margin-bottom: 16rpx;
}
.route-info {
  background: #f9f9ff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}
.route-stat {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
}
.spots-result {
  margin-top: 16rpx;
}
.spots-result-title {
  font-size: 26rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 12rpx;
}
.result-spot-item {
  display: flex;
  align-items: center;
  padding: 12rpx 0;
}
.result-spot-index {
  width: 36rpx;
  height: 36rpx;
  background: #ff9800;
  color: #fff;
  border-radius: 50%;
  text-align: center;
  line-height: 36rpx;
  font-size: 20rpx;
  margin-right: 12rpx;
  flex-shrink: 0;
}
.result-spot-name {
  font-size: 26rpx;
  flex: 1;
}
.result-spot-loc {
  font-size: 20rpx;
  color: #999;
}
.action-row {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}
.action-btn {
  flex: 1;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 12rpx;
  padding: 18rpx 0;
  font-size: 28rpx;
  border: none;
}
.action-btn.secondary {
  background: #fff;
  color: #667eea;
  border: 1rpx solid #667eea;
}
</style>
