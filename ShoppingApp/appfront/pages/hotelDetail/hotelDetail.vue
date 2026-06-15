<template>
  <view class="container">
    <!-- 顶部轮播 + 收藏 -->
    <view class="top-box">
      <swiper class="swiper" indicator-dots autoplay circular>
        <swiper-item v-for="(img, idx) in images" :key="idx">
          <image :src="img" class="swiper-img" mode="aspectFill" />
        </swiper-item>
      </swiper>
      <view class="collect-star" @click="toggleCollect">
        <text class="star-text">{{ isCollected ? '★' : '☆' }}</text>
      </view>
    </view>

    <!-- 酒店信息卡片 -->
    <view class="info-card">
      <view class="name-row">
        <text class="name">{{ hotel.name || '酒店名称' }}</text>
        <view class="star-box">
          <text class="star">★</text>
          <text class="star-text">{{ hotel.star || 5 }}星级</text>
        </view>
      </view>

      <view class="score-row">
        <view class="score-tag">
          <text class="score-num">{{ avgScore }}</text>
          <text class="score-desc">分</text>
        </view>
        <text class="price">¥{{ hotel.minPrice || hotel.price || 0 }}/晚起</text>
      </view>

      <view class="address-row">
        <text class="address">📍 {{ hotel.address || '暂无地址' }}</text>
      </view>
      
      <!-- 设施信息 -->
      <view class="facilities-row" v-if="hotel.facilities">
        <text class="facilities">🏨 设施：{{ hotel.facilities }}</text>
      </view>
    </view>

    <!-- 房型列表 -->
    <view class="room-section" v-if="roomTypes.length > 0">
      <text class="section-title">🏠 选择房型</text>
      <view class="room-list">
        <view class="room-item" v-for="room in roomTypes" :key="room.id" @click="selectRoom(room)">
          <view class="room-info">
            <text class="room-name">{{ room.typeName }}</text>
            <text class="room-desc">{{ room.size }} | {{ room.bedType }} | {{ room.windowStatus }}</text>
            <text class="room-breakfast" v-if="room.breakfast">🍳 {{ room.breakfast }}</text>
          </view>
          <view class="room-right">
            <text class="room-price">¥{{ room.price }}</text>
            <text class="room-unit">/晚</text>
            <view class="room-stock">
              <text>剩余 {{ room.availableCount }} 间</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 评价区域 -->
    <view class="comment-section">
      <view class="comment-header">
        <text class="title">📝 用户评价</text>
        <text class="comment-count" v-if="commentList.length > 0">共{{ commentList.length }}条评价</text>
      </view>
      <view v-for="item in commentList" :key="item.id" class="comment-item">
        <view class="comment-top">
          <text class="user">👤 {{ item.username || '匿名用户' }}</text>
          <text class="comment-score">评分：{{ item.score || item.rating }}分</text>
          <text class="comment-time">{{ formatTime(item.createTime) }}</text>
        </view>
        <text class="content">{{ item.content || item.comment }}</text>
        
        <!-- 商家回复区域 -->
        <view class="merchant-reply" v-if="item.reply && item.reply.trim()">
          <view class="reply-header">
            <text class="reply-icon">🏨</text>
            <text class="reply-label">商家回复：</text>
          </view>
          <text class="reply-content">{{ item.reply }}</text>
        </view>
      </view>
      <view v-if="commentList.length === 0" class="empty">暂无评价，快来成为第一个评价的人吧~</view>
    </view>

    <!-- 底部占位空白（防止被底部栏覆盖） -->
    <view class="bottom-placeholder"></view>

    <!-- 底部预订按钮 -->
    <view class="bottom-bar">
      <view class="price-info">
        <text class="price-label">起价</text>
        <text class="price-value">¥{{ hotel.minPrice || hotel.price || 0 }}</text>
        <text class="price-unit">/晚</text>
      </view>
      <button class="book-btn" @click="goToBook">立即预订</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      id: null,
      hotel: {},
      images: [],
      roomTypes: [],
      commentList: [],
      isCollected: false,
      username: "",
      avgScore: 0
    }
  },
  
  onLoad(options) {
    this.id = options.id
    this.username = uni.getStorageSync("loginUsername") || ""
    // 同时从 userInfo 获取用户名备用
    const userInfo = uni.getStorageSync('userInfo')
    if (userInfo && userInfo.username) {
      this.username = userInfo.username
    }
    console.log("酒店ID：", this.id)
    console.log("当前用户：", this.username)
    
    this.getHotel()
    this.getRoomTypes()
    this.getComments()
    this.checkCollect()
  },
  
  methods: {
    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return '刚刚'
      try {
        const date = new Date(timeStr)
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        return `${year}-${month}-${day}`
      } catch (e) {
        return timeStr
      }
    },
    
    // 计算酒店平均分
    calcAvgScore() {
      if (this.commentList.length === 0) {
        this.avgScore = 0
        return
      }
      let total = 0
      this.commentList.forEach(item => {
        const score = item.score || item.rating || 5
        total += score
      })
      let avg = total / this.commentList.length
      this.avgScore = avg.toFixed(1)
    },

    // 获取酒店详情
    getHotel() {
      uni.request({
        url: "http://localhost:8080/api/hotels/" + this.id,
        method: "GET",
        success: (res) => {
          console.log("酒店详情返回：", res.data)
          
          let hotelData = res.data
          if (res.data && res.data.code === 200) {
            hotelData = res.data.data
          }
          
          this.hotel = hotelData || {}
          console.log("酒店merchantId：", this.hotel.merchantId)
          
          if (this.hotel.detailImages && this.hotel.detailImages.length > 0) {
            this.images = this.hotel.detailImages.map(img => {
              if (img.imageUrl) {
                return "http://localhost:8080" + img.imageUrl
              }
              return img
            })
          } else if (this.hotel.image) {
            this.images = ["http://localhost:8080" + this.hotel.image]
          } else {
            this.images = ["/static/default-hotel.png"]
          }
        },
        fail: (err) => {
          console.error("获取酒店详情失败：", err)
          uni.showToast({ title: "加载失败", icon: "none" })
        }
      })
    },
    
    // 获取房型列表
    getRoomTypes() {
      uni.request({
        url: "http://localhost:8080/api/room-types/hotel/" + this.id,
        method: "GET",
        success: (res) => {
          console.log("房型列表返回：", res.data)
          
          let rooms = []
          if (res.data && res.data.code === 200) {
            rooms = res.data.data || []
          } else if (Array.isArray(res.data)) {
            rooms = res.data
          } else {
            rooms = []
          }
          
          this.roomTypes = rooms
        },
        fail: (err) => {
          console.error("获取房型失败：", err)
        }
      })
    },

    // 获取评价（包含商家回复）
    getComments() {
      uni.request({
        url: "http://localhost:8080/api/hotel-comments/hotel/" + this.id,
        method: "GET",
        success: (res) => {
          console.log("评价列表返回：", res.data)
          
          let comments = []
          if (res.data && res.data.code === 200) {
            comments = res.data.data || []
          } else if (Array.isArray(res.data)) {
            comments = res.data
          } else {
            comments = []
          }
          
          // 打印每条评价的回复信息
          comments.forEach(comment => {
            console.log(`评价ID: ${comment.id}, 回复: ${comment.reply || '无回复'}`)
          })
          
          this.commentList = comments
          this.calcAvgScore()
        },
        fail: (err) => {
          console.error("获取评价失败：", err)
          this.commentList = []
        }
      })
    },

    // 选择房型
    selectRoom(room) {
      console.log("选择的房型：", room)
      this.goToBookWithRoom(room)
    },

    // 跳转预订
    goToBook() {
      if (this.roomTypes.length > 0) {
        uni.showToast({ 
          title: "请先选择房型", 
          icon: "none" 
        })
      } else {
        uni.navigateTo({
          url: "/pages/booking/booking?id=" + this.id 
            + "&name=" + encodeURIComponent(this.hotel.name || '酒店')
            + "&price=" + (this.hotel.minPrice || this.hotel.price || 0)
            + "&username=" + this.username
            + "&merchantId=" + (this.hotel.merchantId || '')
        })
      }
    },
    
    // 带房型预订
    goToBookWithRoom(room) {
      uni.navigateTo({
        url: "/pages/booking/booking?id=" + this.id 
          + "&name=" + encodeURIComponent(this.hotel.name || '酒店')
          + "&price=" + room.price
          + "&roomTypeId=" + room.id
          + "&roomTypeName=" + encodeURIComponent(room.typeName)
          + "&username=" + this.username
          + "&merchantId=" + (this.hotel.merchantId || '')
      })
    },

    // 检查收藏
    checkCollect() {
      if (!this.username) return
      const key = "myCollection_" + this.username
      let data = uni.getStorageSync(key) || []
      this.isCollected = data.some(item => item.id == this.id)
    },

    // 切换收藏
    toggleCollect() {
      if (!this.username) {
        uni.showToast({ title: "请先登录", icon: "none" })
        setTimeout(() => {
          uni.navigateTo({ url: "/pages/login-register/login-register" })
        }, 1500)
        return
      }
      
      const key = "myCollection_" + this.username
      let data = uni.getStorageSync(key) || []
      const index = data.findIndex(item => item.id == this.id)

      if (index > -1) {
        data.splice(index, 1)
        this.isCollected = false
        uni.showToast({ title: "取消收藏", icon: "none" })
      } else {
        data.push(this.hotel)
        this.isCollected = true
        uni.showToast({ title: "收藏成功", icon: "success" })
      }
      uni.setStorageSync(key, data)
    }
  }
}
</script>

<style scoped>
page {
  background: #f5f7fa;
  margin: 0;
  padding: 0;
}

.container {
  width: 100%;
  box-sizing: border-box;
  padding-bottom: 0;
}

.top-box {
  position: relative;
}

.swiper {
  width: 100%;
  height: 420rpx;
}

.swiper-img {
  width: 100%;
  height: 100%;
}

.collect-star {
  position: absolute;
  top: 30rpx;
  right: 30rpx;
  z-index: 99;
  width: 70rpx;
  height: 70rpx;
  background: rgba(0, 0, 0, 0.35);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.collect-star .star-text {
  font-size: 40rpx;
  color: #ffbc36;
}

.info-card {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.name {
  font-size: 36rpx;
  font-weight: bold;
  color: #222;
}

.star-box {
  display: flex;
  align-items: center;
}

.star {
  color: #ffbc36;
  font-size: 26rpx;
  margin-right: 6rpx;
}

.star-text {
  font-size: 24rpx;
  color: #666;
}

.score-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.score-tag {
  background: #fff2e5;
  color: #ff7d30;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
  font-weight: bold;
}

.score-num {
  font-size: 30rpx;
}

.score-desc {
  font-size: 24rpx;
  margin-left: 4rpx;
}

.price {
  font-size: 32rpx;
  color: #ff6b35;
  font-weight: bold;
}

.address {
  font-size: 26rpx;
  color: #888;
}

.facilities-row {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.facilities {
  font-size: 26rpx;
  color: #666;
}

/* 房型区域 */
.room-section {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #222;
  display: block;
  margin-bottom: 20rpx;
}

.room-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.room-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #f8f9fc;
  border-radius: 12rpx;
  border: 1rpx solid #eee;
}

.room-info {
  flex: 1;
}

.room-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.room-desc {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-bottom: 6rpx;
}

.room-breakfast {
  font-size: 22rpx;
  color: #52c41a;
}

.room-right {
  text-align: right;
}

.room-price {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff6b35;
}

.room-unit {
  font-size: 22rpx;
  color: #999;
}

.room-stock {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

/* 评价区域 */
.comment-section {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 24rpx;
}

.title {
  font-size: 32rpx;
  font-weight: bold;
  color: #222;
}

.comment-count {
  font-size: 24rpx;
  color: #999;
}

.comment-item {
  border-bottom: 1rpx solid #f0f0f0;
  padding: 20rpx 0;
}

.comment-top {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}

.user {
  font-size: 26rpx;
  color: #555;
  margin-right: 20rpx;
}

.comment-score {
  font-size: 24rpx;
  color: #ff7d30;
  margin-right: 20rpx;
}

.comment-time {
  font-size: 22rpx;
  color: #999;
  margin-left: auto;
}

.content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
  margin-bottom: 12rpx;
  display: block;
}

/* 商家回复区域 */
.merchant-reply {
  background-color: #f8f9fc;
  border-left: 4rpx solid #1677ff;
  padding: 16rpx 20rpx;
  margin-top: 12rpx;
  border-radius: 8rpx;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.reply-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.reply-label {
  font-size: 24rpx;
  font-weight: bold;
  color: #1677ff;
}

.reply-content {
  font-size: 26rpx;
  color: #555;
  line-height: 1.5;
  display: block;
}

.empty {
  color: #999;
  font-size: 26rpx;
  text-align: center;
  padding: 30rpx 0;
}

/* 底部占位空白 - 防止内容被底部栏覆盖 */
.bottom-placeholder {
  height: 180rpx;
  width: 100%;
}

/* 底部栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom, 0));
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.price-info {
  display: flex;
  align-items: baseline;
}

.price-label {
  font-size: 24rpx;
  color: #999;
  margin-right: 8rpx;
}

.price-value {
  font-size: 44rpx;
  font-weight: bold;
  color: #ff6b35;
}

.price-unit {
  font-size: 22rpx;
  color: #999;
  margin-left: 4rpx;
}

.book-btn {
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
  padding: 20rpx 50rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: bold;
  border: none;
  margin: 0;
}
</style>