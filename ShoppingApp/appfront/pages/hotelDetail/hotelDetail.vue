<template>
  <view class="container">
    <!-- 顶部轮播 + 收藏 -->
    <view class="top-box">
      <swiper class="swiper" indicator-dots autoplay circular>
        <swiper-item v-for="img in images" :key="img">
          <image :src="img" class="swiper-img" mode="aspectFill" />
        </swiper-item>
      </swiper>
      <view class="collect-star" @click="toggleCollect">
        <text class="star-text">{{ isCollected ? '★' : '☆' }}</text>
      </view>
    </view>

    <!-- 酒店信息卡片（自动计算平均分） -->
    <view class="info-card">
      <view class="name-row">
        <text class="name">{{ hotel.name }}</text>
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
        <text class="price">¥{{ hotel.price }}/晚</text>
      </view>

      <view class="address-row">
        <text class="address">📍 {{ hotel.address || '暂无地址' }}</text>
      </view>
    </view>

    <button class="book-btn" @click="goToBook">立即预订</button>

    <!-- 评价区域 -->
    <view class="comment-section">
      <text class="title">用户评价</text>
      <view v-for="item in commentList" :key="item.id" class="comment-item">
        <view class="comment-top">
          <text class="user">用户评价</text>
          <text class="comment-score">评分：{{ item.score }}分</text>
        </view>
        <text class="content">{{ item.content }}</text>
        <text class="time">{{ item.createTime }}</text>
      </view>
      <view v-if="commentList.length===0" class="empty">暂无评价</view>
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
      commentList: [],
      isCollected: false,
      username: "",
      avgScore: 4.8
    }
  },
  onLoad(options) {
    this.id = options.id
    this.username = uni.getStorageSync("loginUsername")
    this.getHotel()
    this.getComments()
    this.checkCollect()
  },
  methods: {
    // 计算酒店平均分
    calcAvgScore() {
      if (this.commentList.length === 0) {
        this.avgScore = 4.8
        return
      }
      let total = 0
      this.commentList.forEach(item => {
        total += item.score || 5
      })
      let avg = total / this.commentList.length
      this.avgScore = avg.toFixed(1)
    },

    getHotel() {
      uni.request({
        url: "http://localhost:8080/api/hotels/" + this.id,
        success: (res) => {
          this.hotel = res.data
          this.images = res.data.detailImages 
            ? res.data.detailImages.map(i => "http://localhost:8080" + i.imageUrl) 
            : []
        }
      })
    },

    getComments() {
      uni.request({
        url: "http://localhost:8080/api/hotel-comments/hotel/" + this.id,
        success: (res) => {
          this.commentList = res.data
          this.calcAvgScore()
        }
      })
    },

    goToBook() {
      uni.navigateTo({
        url: "/pages/booking/booking?id=" + this.id 
          + "&name=" + this.hotel.name 
          + "&price=" + this.hotel.price 
          + "&username=" + this.username
      })
    },

    checkCollect() {
      const key = "myCollection_" + this.username
      let data = uni.getStorageSync(key) || []
      this.isCollected = data.some(item => item.id === this.hotel.id)
    },

    toggleCollect() {
      const key = "myCollection_" + this.username
      let data = uni.getStorageSync(key) || []
      const index = data.findIndex(item => item.id === this.hotel.id)

      if (index > -1) {
        data.splice(index, 1)
        this.isCollected = false
        uni.showToast({ title: "取消收藏" })
      } else {
        data.push(this.hotel)
        this.isCollected = true
        uni.showToast({ title: "收藏成功" })
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

.star-text {
  font-size: 40rpx;
  color: #fff;
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
  font-size: 40rpx;
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

.book-btn {
  background: #1677ff;
  color: #fff;
  margin: 20rpx 30rpx;
  border-radius: 12rpx;
  padding: 22rpx 0;
  font-size: 32rpx;
  border: none;
}

.comment-section {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.title {
  font-size: 34rpx;
  font-weight: bold;
  margin-bottom: 24rpx;
  color: #222;
}

.comment-item {
  border-bottom: 1rpx solid #f0f0f0;
  padding: 20rpx 0;
}

.comment-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.user {
  font-size: 26rpx;
  color: #555;
}

.comment-score {
  font-size: 24rpx;
  color: #ff7d30;
}

.content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
  margin-bottom: 8rpx;
  display: block;
}

.time {
  font-size: 22rpx;
  color: #999;
}

.empty {
  color: #999;
  font-size: 26rpx;
  text-align: center;
  padding: 30rpx 0;
}
</style>