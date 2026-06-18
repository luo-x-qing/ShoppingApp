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
      <view class="param-row">
        <text class="param-label">预算金额</text>
        <input class="param-input" type="number" v-model="budget" placeholder="总预算（元）" />
      </view>
      <view class="param-row">
        <text class="param-label">出行人数</text>
        <view class="day-selector">
          <text class="day-btn" :class="{ active: travelers === n }" v-for="n in [1,2,3,4,5,6]" :key="n" @click="travelers = n">{{ n }}人</text>
        </view>
      </view>
      <button class="generate-btn" @click="generatePlan" :loading="generating">
        {{ generating ? 'AI生成中...' : '🤖 AI 生成路线' }}
      </button>
    </view>

    <!-- AI规划结果 -->
    <view class="section" v-if="planResult">
      <text class="section-title">🤖 AI 推荐行程</text>

      <view class="plan-summary">
        <text class="summary-line">📍 {{ planResult.city }} · {{ planResult.days }}天 · {{ planResult.travelers }}人</text>
        <text class="summary-line">💰 预算 ¥{{ planResult.budget || '不限' }} · 预估 ¥{{ planResult.totalBudget }}</text>
      </view>

      <view v-for="(day, di) in planResult.itinerary" :key="di" class="day-card">
        <view class="day-header">
          <text class="day-num">第 {{ day.day }} 天</text>
          <text class="day-count">{{ day.schedule.length }} 项</text>
        </view>
        <view
          v-for="(item, si) in day.schedule"
          :key="si"
          class="schedule-item"
          :class="'type-' + item.type"
        >
          <view class="schedule-line">
            <text class="schedule-time">{{ item.time }}</text>
            <text class="schedule-type">{{ typeLabel(item.type) }}</text>
          </view>
          <text class="schedule-content">{{ item.content }}</text>
          <text v-if="item.location" class="schedule-loc">📍 {{ item.location }}</text>
        </view>
      </view>

      <!-- Route map -->
      <view class="map-section" v-if="allSpots.length > 0">
        <view class="map-section-header">
          <text class="map-section-title">🗺️ 路线地图（编号即游玩顺序）</text>
        </view>
        <view class="map-wrapper">
          <map
            id="routeMap"
            class="route-map"
            :latitude="mapCenter.lat"
            :longitude="mapCenter.lng"
            :markers="mapMarkers"
            :polyline="mapPolyline"
            :include-points="mapIncludePoints"
            :enable-zoom="true"
            :enable-scroll="true"
          />
        </view>
      </view>

      <view v-if="planResult.tips" class="tips-box">
        <text class="tips-text">💡 {{ planResult.tips }}</text>
      </view>

      <view class="action-row">
        <button class="action-btn primary" @click="sendToAIChat" :disabled="sending">🤖 发送给AI智能助手</button>
        <button class="action-btn secondary" @click="resetPlan">重新规划</button>
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
      budget: '',
      travelers: 1,
      generating: false,
      sending: false,
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
    },
    allSpots() {
      const r = this.planResult
      if (!r || !r.scenicSpots) return []
      return r.scenicSpots.filter(s => s.lat && s.lng)
    },
    mapMarkers() {
      return this.allSpots.map((s, i) => ({
        id: i,
        latitude: s.lat,
        longitude: s.lng,
        title: s.name,
        label: {
          content: `${s.order}`,
          color: '#fff',
          fontSize: 13,
          borderRadius: 8,
          bgColor: '#ff6b35',
          padding: 6,
          textAlign: 'center'
        },
        width: 30,
        height: 30
      }))
    },
    mapCenter() {
      const arr = this.allSpots
      if (arr.length === 0) return { lat: 0, lng: 0 }
      const sum = arr.reduce((s, p) => ({ lat: s.lat + p.lat, lng: s.lng + p.lng }), { lat: 0, lng: 0 })
      return { lat: sum.lat / arr.length, lng: sum.lng / arr.length }
    },
    mapPolyline() {
      if (this.allSpots.length < 2) return []
      const sorted = [...this.allSpots].sort((a, b) => a.order - b.order)
      const points = sorted.map(s => ({ latitude: s.lat, longitude: s.lng }))
      return [{ points, color: '#ff6b35', width: 4 }]
    },
    mapIncludePoints() {
      return this.allSpots.map(s => ({ latitude: s.lat, longitude: s.lng }))
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
      const spotsParam = this.spots.map(s => s.name).join('，');
      const budgetVal = parseInt(this.budget) || 0;

      uni.request({
        url: `${BASE_URL}/travel/plan`,
        method: 'GET',
        data: {
          city,
          days: this.days,
          budget: budgetVal,
          travelers: this.travelers,
          spots: spotsParam
        },
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
    },
    resetPlan() {
      this.planResult = null
    },
    typeLabel(type) {
      const map = {
        breakfast: '🍳 早餐',
        lunch: '🍜 午餐',
        dinner: '🍽 晚餐',
        scenic: '🏞 景点',
        transport: '🚗 交通',
        hotel: '🏨 住宿',
        free: '🆓 自由活动'
      }
      return map[type] || type
    },
    sendToAIChat() {
      if (!this.planResult || this.sending) return
      this.sending = true
      const r = this.planResult
      let text = `这是我规划的${r.city}${r.days}天行程（预算¥${r.budget || '不限'}，${r.travelers}人）：\n\n`
      ;(r.itinerary || []).forEach(day => {
        text += `【第${day.day}天】\n`
        ;(day.schedule || []).forEach(item => {
          text += `  ${item.time} ${this.typeLabel(item.type)} ${item.content}`
          if (item.location) text += `（${item.location}）`
          text += '\n'
        })
        text += '\n'
      })
      if (r.tips) text += `💡 ${r.tips}\n\n`
      text += '请帮我优化这个行程，给出改进建议！'

      uni.setStorageSync('ai_route_context', text)
      uni.setStorageSync('ai_pending_route', true)
      this.sending = false
      uni.navigateTo({ url: '/pages/ai-chat/ai-chat' })
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
.plan-summary {
  background: linear-gradient(135deg, #667eea15, #764ba215);
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}
.summary-line {
  font-size: 26rpx;
  color: #555;
  display: block;
  line-height: 1.7;
}

.day-card {
  background: #fafafa;
  border-radius: 14rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  border-left: 6rpx solid #667eea;
}
.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.day-num {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}
.day-count {
  font-size: 22rpx;
  color: #999;
}

.schedule-item {
  padding: 14rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  position: relative;
  padding-left: 30rpx;
  border-left: 4rpx solid transparent;
}
.schedule-item:last-child {
  border-bottom: none;
}
.schedule-item.type-scenic {
  border-left-color: #ff6b35;
}
.schedule-item.type-breakfast,
.schedule-item.type-lunch,
.schedule-item.type-dinner {
  border-left-color: #ff9800;
}
.schedule-item.type-transport {
  border-left-color: #2196f3;
}
.schedule-item.type-hotel {
  border-left-color: #4caf50;
}
.schedule-item.type-free {
  border-left-color: #9e9e9e;
}

.schedule-line {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 6rpx;
}
.schedule-time {
  font-size: 24rpx;
  font-weight: bold;
  color: #667eea;
  background: #f0f0ff;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
}
.schedule-type {
  font-size: 22rpx;
  color: #999;
}
.schedule-content {
  font-size: 26rpx;
  color: #333;
  display: block;
  line-height: 1.5;
}
.schedule-loc {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.action-row {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}
.action-btn {
  flex: 1;
  border-radius: 12rpx;
  padding: 18rpx 0;
  font-size: 28rpx;
  border: none;
  text-align: center;
}
.action-btn.primary {
  background: #1677ff;
  color: #fff;
}
.action-btn.secondary {
  background: #fff;
  color: #667eea;
  border: 1rpx solid #667eea;
}

.map-section {
  margin-top: 24rpx;
}
.map-section-title {
  font-size: 26rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 12rpx;
}
.map-wrapper {
  width: 100%;
  height: 500rpx;
  border-radius: 16rpx;
  overflow: hidden;
}
.route-map {
  width: 100%;
  height: 500rpx;
}

.tips-box {
  background: #fffbe6;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-top: 20rpx;
}
.tips-text {
  font-size: 24rpx;
  color: #b8860b;
  line-height: 1.6;
}

.param-input {
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  padding: 8rpx 16rpx;
  font-size: 26rpx;
  width: 200rpx;
  text-align: right;
}
</style>
