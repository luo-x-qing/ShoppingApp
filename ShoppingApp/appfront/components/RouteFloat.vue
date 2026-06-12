<template>
  <view>
    <view class="route-float" @click="togglePanel">
      <text class="float-icon">🗺️</text>
      <text class="float-badge" v-if="spots.length > 0">{{ spots.length }}</text>
    </view>
    <view class="overlay" v-if="showPanel" @click="togglePanel"></view>
    <view class="float-panel" v-if="showPanel">
      <view class="panel-header">
        <text class="panel-title">已选景点（{{ spots.length }}）</text>
        <text class="panel-clear" @click="clearAll">清空</text>
      </view>
      <scroll-view class="panel-list" scroll-y>
        <view v-if="spots.length === 0" class="panel-empty">暂无景点</view>
        <view class="panel-item" v-for="(s, i) in spots" :key="s.id">
          <text class="panel-index">{{ i + 1 }}</text>
          <view class="panel-info">
            <text class="panel-name">{{ s.name }}</text>
            <text class="panel-location">{{ s.province }} {{ s.city }}</text>
          </view>
          <text class="panel-remove" @click.stop="removeSpot(i)">✕</text>
        </view>
      </scroll-view>
      <view class="panel-footer">
        <button class="panel-btn" @click="goToFullPlan">去路线规划 →</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      spots: [],
      showPanel: false
    };
  },
  mounted() {
    this.loadSpots();
    this.timer = setInterval(() => this.loadSpots(), 2000);
  },
  beforeUnmount() {
    if (this.timer) clearInterval(this.timer);
  },
  methods: {
    getKey() {
      return "routePlan_" + (uni.getStorageSync("loginUsername") || "default");
    },
    loadSpots() {
      const key = this.getKey();
      const data = uni.getStorageSync(key);
      this.spots = Array.isArray(data) ? data : [];
    },
    togglePanel() {
      this.showPanel = !this.showPanel;
      if (this.showPanel) this.loadSpots();
    },
    removeSpot(i) {
      this.spots.splice(i, 1);
      uni.setStorageSync(this.getKey(), this.spots);
    },
    clearAll() {
      this.spots = [];
      uni.setStorageSync(this.getKey(), []);
    },
    goToFullPlan() {
      this.showPanel = false;
      uni.switchTab({ url: "/pages/ai-plan/ai-plan" });
    }
  }
};
</script>

<style scoped>
.route-float {
  position: fixed;
  right: 30rpx;
  bottom: 140rpx;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.5);
  z-index: 9999;
}
.float-icon {
  font-size: 44rpx;
}
.float-badge {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  min-width: 36rpx;
  height: 36rpx;
  background: #ff4d4f;
  color: #fff;
  border-radius: 18rpx;
  font-size: 22rpx;
  text-align: center;
  line-height: 36rpx;
  padding: 0 6rpx;
}
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.4);
  z-index: 9998;
}
.float-panel {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  max-height: 60vh;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  z-index: 9999;
  display: flex;
  flex-direction: column;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 30rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}
.panel-title {
  font-size: 30rpx;
  font-weight: bold;
}
.panel-clear {
  font-size: 26rpx;
  color: #ff4d4f;
}
.panel-list {
  flex: 1;
  padding: 0 30rpx;
  max-height: 40vh;
}
.panel-empty {
  text-align: center;
  color: #999;
  font-size: 26rpx;
  padding: 40rpx 0;
}
.panel-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}
.panel-index {
  width: 36rpx;
  height: 36rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 50%;
  text-align: center;
  line-height: 36rpx;
  font-size: 20rpx;
  margin-right: 14rpx;
  flex-shrink: 0;
}
.panel-info {
  flex: 1;
}
.panel-name {
  font-size: 28rpx;
  font-weight: 500;
  display: block;
}
.panel-location {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}
.panel-remove {
  width: 40rpx;
  text-align: center;
  color: #ff4d4f;
  font-size: 28rpx;
}
.panel-footer {
  padding: 20rpx 30rpx 40rpx;
}
.panel-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 12rpx;
  padding: 20rpx 0;
  font-size: 28rpx;
  border: none;
}
</style>
