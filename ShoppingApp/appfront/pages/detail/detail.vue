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
const AMAP_KEY = '91b34b28376cdf588bb43f354db27157'

const PROVINCE_CENTERS = {
  '北京市': [39.9042, 116.4074],
  '上海市': [31.2304, 121.4737],
  '天津市': [39.1252, 117.1908],
  '重庆市': [29.5330, 106.5048],
  '四川省': [30.5728, 104.0668],
  '广东省': [23.1292, 113.2644],
  '浙江省': [30.2741, 120.1551],
  '福建省': [26.0745, 119.2965],
  '江苏省': [32.0617, 118.7969],
  '湖北省': [30.5465, 114.3423],
  '湖南省': [28.2569, 112.9410],
  '山东省': [36.6516, 117.0204],
  '河南省': [34.7656, 113.7532],
  '河北省': [38.0455, 114.5020],
  '陕西省': [34.2655, 108.9542]
}

export default {
  data() {
    return {
      id: null,
      detail: {},
      images: [],
      comments: [],
      userScore: 5,
      commentContent: '',

      centerLat: null,
      centerLng: null,
      currentLat: null,
      currentLng: null,
      markers: [],
      nearbyList: [],
      nearbyMarkers: [],

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
        this.setCurrentCoords(d.latitude, d.longitude)
      } else {
        this.geocodeAndSetup(d)
      }
    },

    geocodeAndSetup(d) {
      const address = d.city ? d.city + d.name : (d.province ? d.province + d.name : d.name)
      console.log('[高德] 开始地理编码:', address)
      uni.request({
        url: 'https://restapi.amap.com/v3/geocode/geo',
        data: {
          key: AMAP_KEY,
          address,
          city: d.city || '',
          extensions: 'all',
          s: 'rsx',
          platform: 'WXJS',
          appname: AMAP_KEY,
          sdkversion: '1.2.0',
          logversion: '2.0'
        },
        success: (res) => {
          console.log('[高德] 返回:', JSON.stringify(res.data))
          const data = res.data
          if (data && data.status === '1' && data.geocodes && data.geocodes.length > 0) {
            const loc = data.geocodes[0].location.split(',')
            const lng = parseFloat(loc[0])
            const lat = parseFloat(loc[1])
            console.log('[高德] 成功获取坐标:', lat, lng)
            this.centerLat = lat
            this.centerLng = lng
            this.setCurrentCoords(lat, lng)
            uni.request({
              url: `${BASE_URL}/api/attractions/${this.id}`,
              method: 'PUT',
              data: { ...this.detail, latitude: lat, longitude: lng }
            })
          } else {
            console.warn('[高德] 地理编码无结果:', data)
            this.useProvinceFallback(d)
          }
        },
        fail: (err) => {
          console.error('[高德] 请求失败:', err)
          this.useProvinceFallback(d)
        }
      })
    },

    useProvinceFallback(d) {
      if (d.province && PROVINCE_CENTERS[d.province]) {
        const c = PROVINCE_CENTERS[d.province]
        this.centerLat = c[0]
        this.centerLng = c[1]
        this.setCurrentCoords(c[0], c[1])
      }
    },

    setCurrentCoords(lat, lng) {
      this.currentLat = lat
      this.currentLng = lng
      this.renderMarkers()
    },

    renderMarkers() {
      const currentMarker = this.currentLat && this.currentLng ? {
        id: 0,
        latitude: this.currentLat,
        longitude: this.currentLng,
        title: this.detail.name,
        label: {
          content: this.detail.name,
          color: '#fff',
          fontSize: 13,
          borderRadius: 6,
          bgColor: '#1677ff',
          padding: 6,
          textAlign: 'center'
        },
        width: 36,
        height: 36
      } : null
      this.markers = currentMarker ? [currentMarker, ...this.nearbyMarkers] : this.nearbyMarkers
    },

    getNearby() {
      uni.request({
        url: `${BASE_URL}/api/attractions/${this.id}/nearby?radius=50`,
        success: (res) => {
          this.nearbyList = res.data || []
          this.nearbyMarkers = this.nearbyList.map((item, i) => ({
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
          this.renderMarkers()
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
