<template>
  <view class="page">
    <map
      id="map"
      class="map"
      :latitude="centerLat"
      :longitude="centerLng"
      :markers="markers"
      :enable-zoom="true"
      :enable-scroll="true"
      :show-location="true"
      @markertap="onMarkerTap"
    />

    <view
      class="bottom-sheet"
      :class="{ dragging: isDragging }"
      :style="`transform: translateY(${sheetOffset}px)`"
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
    >
      <view class="sheet-handle" @click="toggleSheet">
        <view class="handle-bar" />
      </view>

      <view class="peek-content">
        <text class="peek-name">{{ detail.name || '景点详情' }}</text>
        <text class="peek-score">⭐ {{ (detail.score || 0).toFixed(1) }}分</text>
        <button class="peek-btn" @click.stop="addToRoutePlan">+ 路线</button>
      </view>

      <scroll-view class="sheet-scroll" scroll-y :show-scrollbar="false">
        <swiper class="swiper" indicator-dots circular autoplay>
          <swiper-item v-for="(img, i) in images" :key="i">
            <image :src="img" class="swiper-img" mode="aspectFill" />
          </swiper-item>
        </swiper>

        <view class="section">
          <view class="info-grid">
            <text class="info-item">📍 {{ detail.province }} {{ detail.city }}</text>
            <text class="info-item">🏷 {{ detail.type || '未分类' }}</text>
            <text class="info-item">⭐ {{ detail.level || '未评级' }}</text>
            <text class="info-item">🕒 {{ detail.openTime || '待确认' }}</text>
            <text class="info-item">🎫 ¥{{ detail.ticketPrice != null ? detail.ticketPrice : '待确认' }}</text>
          </view>

          <text class="section-title">景区介绍</text>
          <text class="desc-text">{{ detail.description || '暂无介绍' }}</text>
          <button class="action-btn" @click="addToRoutePlan">加入旅游线路</button>
        </view>

        <view class="section">
          <text class="section-title">附近景点 ({{ nearbyList.length }})</text>
          <view v-if="nearbyList.length === 0" class="empty-text">暂无附近景点</view>
          <view
            v-for="item in nearbyList"
            :key="item.id"
            class="nearby-item"
            @click="goNearby(item)"
          >
            <text class="nearby-name">{{ item.name }}</text>
            <text class="nearby-dist">{{ item.distance }}km</text>
            <text class="nearby-score">⭐ {{ item.score || '暂无' }}</text>
          </view>
        </view>

        <view class="section">
          <text class="section-title">发表评价</text>
          <view class="star-select">
            <text v-for="n of 5" :key="n" class="star-opt" @click="selectStar(n)">
              {{ n <= userScore ? '⭐' : '☆' }}
            </text>
          </view>
          <textarea v-model="commentContent" placeholder="写下你的游玩感受..." maxlength="200" class="comment-input" />
          <button class="action-btn primary" @click="submitComment">提交评价</button>
        </view>

        <view class="section">
          <text class="section-title">游客评价</text>
          <view v-if="comments.length === 0" class="empty-text">暂无评价</view>
          <view v-for="item in comments" :key="item.id" class="comment-item">
            <view class="comment-user">{{ item.userName || '匿名游客' }}</view>
            <view class="comment-stars">
              <text v-for="n of 5" :key="n">{{ n <= item.score ? '⭐' : '☆' }}</text>
            </view>
            <text class="comment-content">{{ item.content }}</text>
            <text class="comment-time">{{ item.createTime }}</text>
          </view>
        </view>

        <view style="height:60rpx" />
      </scroll-view>
    </view>

    <RouteFloat />
  </view>
</template>

<script>
const BASE_URL = 'http://localhost:8080'

export default {
  data() {
    return {
      id: null,
      detail: {},
      images: [],
      comments: [],
      userScore: 5,
      commentContent: '',

      centerLat: 39.9042,
      centerLng: 116.4074,
      markers: [],
      nearbyList: [],

      sheetOffset: 0,
      maxOffset: 0,
      dragStartY: 0,
      dragBaseOffset: 0,
      isDragging: false
    }
  },

  onLoad(options) {
    this.id = options.id
    this.calcSheetHeight()
    this.loadData()
  },

  onReady() {
    this.calcSheetHeight()
  },

  methods: {
    calcSheetHeight() {
      const sys = uni.getSystemInfoSync()
      const sheetH = sys.windowHeight * 0.75
      const peekH = 140 * sys.windowWidth / 750
      this.maxOffset = Math.max(sheetH - peekH, 200)
      this.sheetOffset = this.maxOffset
    },

    toggleSheet() {
      this.sheetOffset = this.sheetOffset >= this.maxOffset - 10 ? 0 : this.maxOffset
    },

    onTouchStart(e) {
      this.dragStartY = e.touches[0].clientY
      this.dragBaseOffset = this.sheetOffset
      this.isDragging = true
    },

    onTouchMove(e) {
      if (!this.isDragging) return
      const delta = this.dragStartY - e.touches[0].clientY
      this.sheetOffset = Math.max(0, Math.min(this.maxOffset, this.dragBaseOffset - delta))
    },

    onTouchEnd() {
      this.isDragging = false
      const mid = this.maxOffset / 2
      this.sheetOffset = this.sheetOffset > mid ? this.maxOffset : 0
    },

    loadData() {
      this.getDetail()
      this.getComments()
    },

    getDetail() {
      uni.request({
        url: `${BASE_URL}/api/attractions/${this.id}`,
        success: (res) => {
          this.detail = res.data
          this.setupMap()
          this.getNearby()
          if (res.data.photo) {
            this.images = [res.data.photo.startsWith('http') ? res.data.photo : BASE_URL + res.data.photo]
          }
          if (this.images.length === 0) {
            this.images = ['/static/home/6.jpg']
          }
        }
      })
    },

    setupMap() {
      const d = this.detail
      if (d.latitude && d.longitude) {
        this.centerLat = d.latitude
        this.centerLng = d.longitude
      }
      const currentMarker = {
        id: 0,
        latitude: d.latitude || this.centerLat,
        longitude: d.longitude || this.centerLng,
        title: d.name,
        label: {
          content: d.name,
          color: '#fff',
          fontSize: 13,
          borderRadius: 6,
          bgColor: '#1677ff',
          padding: 6,
          textAlign: 'center'
        },
        width: 36,
        height: 36
      }
      this.markers = [currentMarker]
    },

    getNearby() {
      uni.request({
        url: `${BASE_URL}/api/attractions/${this.id}/nearby?radius=50`,
        success: (res) => {
          this.nearbyList = res.data || []
          const nearbyMarkers = this.nearbyList.map((item, i) => ({
            id: i + 1,
            latitude: item.latitude,
            longitude: item.longitude,
            title: item.name,
            label: {
              content: `${item.name} (${item.distance}km)`,
              color: '#333',
              fontSize: 11,
              borderRadius: 4,
              bgColor: 'rgba(255,255,255,0.9)',
              padding: 4,
              textAlign: 'center'
            },
            width: 24,
            height: 24
          }))
          const current = this.markers.length > 0 ? this.markers[0] : null
          this.markers = current ? [current, ...nearbyMarkers] : nearbyMarkers
        }
      })
    },

    onMarkerTap(e) {
      const markerId = e.detail.markerId
      if (markerId === 0) return
      const item = this.nearbyList[markerId - 1]
      if (item) this.goNearby(item)
    },

    goNearby(item) {
      uni.navigateTo({ url: `/pages/detail/detail?id=${item.id}` })
    },

    getComments() {
      uni.request({
        url: `${BASE_URL}/api/comments/attraction/${this.id}`,
        success: (res) => {
          this.comments = res.data || []
          this.calcAvgScore()
        }
      })
    },

    calcAvgScore() {
      if (this.comments.length === 0) {
        this.detail.score = 0
        return
      }
      const total = this.comments.reduce((s, c) => s + c.score, 0)
      const avg = total / this.comments.length
      this.detail.score = avg
      uni.request({
        url: `${BASE_URL}/api/attractions/${this.id}/score`,
        method: 'PUT',
        data: { score: avg }
      })
    },

    selectStar(n) {
      this.userScore = n
    },

    addToRoutePlan() {
      const username = uni.getStorageSync('loginUsername')
      if (!username) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      const key = 'routePlan_' + username
      let spots = uni.getStorageSync(key) || []
      if (!Array.isArray(spots)) spots = []
      if (spots.some(s => s.id == this.id)) {
        uni.showToast({ title: '已在规划列表中', icon: 'none' })
        return
      }
      spots.push({
        id: this.id,
        name: this.detail.name,
        province: this.detail.province,
        city: this.detail.city,
        photo: this.detail.photo
      })
      uni.setStorageSync(key, spots)
      uni.showToast({ title: '已加入旅游线路规划' })
    },

    submitComment() {
      if (!this.commentContent.trim()) {
        uni.showToast({ title: '请输入评价内容', icon: 'none' })
        return
      }
      uni.request({
        url: `${BASE_URL}/api/comments`,
        method: 'POST',
        data: {
          attractionId: this.id,
          content: this.commentContent,
          score: this.userScore,
          userName: '匿名游客'
        },
        success: () => {
          uni.showToast({ title: '评价提交成功' })
          this.commentContent = ''
          this.userScore = 5
          this.getComments()
        }
      })
    }
  }
}
</script>

<style scoped>
page {
  background: #f5f5f5;
  overflow: hidden;
}

.page {
  width: 100%;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.map {
  width: 100%;
  height: 100vh;
  position: absolute;
  top: 0;
  left: 0;
}

.bottom-sheet {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 75vh;
  background: #f5f5f5;
  border-radius: 24rpx 24rpx 0 0;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.12);
  z-index: 10;
  transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  will-change: transform;
}

.bottom-sheet.dragging {
  transition: none;
}

.sheet-handle {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16rpx 0 8rpx;
  cursor: pointer;
}

.handle-bar {
  width: 60rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: #ccc;
}

.peek-content {
  display: flex;
  align-items: center;
  padding: 0 30rpx 16rpx;
  gap: 12rpx;
}

.peek-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.peek-score {
  font-size: 26rpx;
  color: #ff6b35;
  flex-shrink: 0;
}

.peek-btn {
  flex-shrink: 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 10rpx 20rpx;
  font-size: 24rpx;
  line-height: 1.4;
}

.sheet-scroll {
  height: calc(75vh - 100rpx);
}

.swiper {
  width: 100%;
  height: 360rpx;
}

.swiper-img {
  width: 100%;
  height: 100%;
}

.section {
  background: #fff;
  margin: 16rpx 20rpx;
  padding: 24rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 14rpx;
  display: block;
}

.info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.info-item {
  font-size: 24rpx;
  color: #555;
  background: #f0f5ff;
  padding: 6rpx 14rpx;
  border-radius: 6rpx;
}

.desc-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.7;
  display: block;
}

.action-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 8rpx;
  padding: 14rpx 0;
  font-size: 28rpx;
  margin-top: 20rpx;
  border: none;
}

.action-btn.primary {
  background: #1677ff;
}

.empty-text {
  color: #999;
  font-size: 26rpx;
  padding: 20rpx 0;
  text-align: center;
}

.nearby-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.nearby-item:last-child {
  border-bottom: none;
}

.nearby-name {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.nearby-dist {
  font-size: 22rpx;
  color: #999;
  margin: 0 16rpx;
}

.nearby-score {
  font-size: 24rpx;
  color: #ff6b35;
}

.star-select {
  display: flex;
  gap: 10rpx;
  margin-bottom: 16rpx;
}

.star-opt {
  font-size: 40rpx;
}

.comment-input {
  width: 100%;
  height: 140rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  padding: 15rpx;
  box-sizing: border-box;
  margin-bottom: 12rpx;
  font-size: 26rpx;
}

.comment-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-user {
  font-size: 28rpx;
  font-weight: bold;
}

.comment-stars {
  margin: 6rpx 0;
}

.comment-content {
  font-size: 26rpx;
  color: #333;
  line-height: 1.6;
  display: block;
}

.comment-time {
  font-size: 22rpx;
  color: #999;
  margin-top: 6rpx;
  display: block;
}
</style>
