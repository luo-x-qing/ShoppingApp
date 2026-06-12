<template>
  <view class="container">
    <view class="header">
      <text class="title">酒店预订</text>
    </view>

    <view class="info-card">
      <text class="hotel-name">{{ hotelName }}</text>
      <text class="price">¥{{ price }}/晚</text>
    </view>

    <view class="form-box">
      <view class="form-item">
        <text>入住日期</text>
        <picker mode="date" :value="checkInDate" @change="bindCheckInChange">
          <view class="date">{{ checkInDate }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text>退房日期</text>
        <picker mode="date" :value="checkOutDate" @change="bindCheckOutChange">
          <view class="date">{{ checkOutDate }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text>入住天数</text>
        <text class="days">{{ days }} 晚</text>
      </view>
      <view class="form-item">
        <text>总价</text>
        <text class="total-price">¥{{ totalPrice }}</text>
      </view>
    </view>

    <view class="btn-box">
      <button class="submit-btn" @click="submitOrder">确认预订</button>
    </view>
  </view>
    <RouteFloat/>
</template>

<script>
export default {
  data() {
    return {
      id: "",
      hotelName: "",
      price: 0,
      checkInDate: "",
      checkOutDate: "",
      days: 1,
      totalPrice: 0,
      username: ""
    };
  },

  onLoad(options) {
    const today = new Date();
    const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000);
    const formatDate = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;

    this.id = options.id;
    this.hotelName = options.name;
    this.price = Number(options.price);
    this.checkInDate = formatDate(today);
    this.checkOutDate = formatDate(tomorrow);
    this.days = 1;
    this.totalPrice = Number(options.price);

    // 🔥 双重保险：参数拿不到就直接从缓存拿
    this.username = options.username || uni.getStorageSync("loginUsername");
  },

  methods: {
    bindCheckInChange(e) {
      this.checkInDate = e.detail.value;
      this.calcDays();
    },

    bindCheckOutChange(e) {
      this.checkOutDate = e.detail.value;
      this.calcDays();
    },

    calcDays() {
      const inDate = new Date(this.checkInDate);
      const outDate = new Date(this.checkOutDate);
      const diff = (outDate - inDate) / (1000 * 60 * 60 * 24);
      const days = diff > 0 ? diff : 1;
      this.days = days;
      this.totalPrice = days * this.price;
    },

    // ✅ 强制把 username 传给后端
    submitOrder() {
      uni.request({
        url: "http://localhost:8080/api/hotel-orders",
        method: "POST",
        data: {
          hotelId: this.id,
          name: this.hotelName,
          price: this.totalPrice,
          checkIn: this.checkInDate,
          checkOut: this.checkOutDate,
          username: this.username // 🔥 这里必须存在！
        },
        success: (res) => {
          uni.showToast({ title: "预订成功", icon: "success" });
          setTimeout(() => {
            uni.navigateBack({ delta: 2 });
          }, 1500);
        }
      });
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
}
.header {
  background: #fff;
  padding: 25rpx 0;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
}
.info-card {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 12rpx;
}
.hotel-name {
  font-size: 34rpx;
  font-weight: bold;
  display: block;
}
.price {
  font-size: 28rpx;
  color: #ff6b35;
  margin-top: 10rpx;
}
.form-box {
  background: #fff;
  margin: 0 20rpx;
  padding: 30rpx;
  border-radius: 12rpx;
}
.form-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eee;
  font-size: 28rpx;
}
.date {
  color: #1677ff;
}
.days, .total-price {
  color: #ff6b35;
  font-weight: bold;
}
.btn-box {
  padding: 30rpx 20rpx;
}
.submit-btn {
  width: 100%;
  background: #1677ff;
  color: #fff;
  border-radius: 12rpx;
  padding: 15rpx 0;
  font-size: 30rpx;
}
</style>