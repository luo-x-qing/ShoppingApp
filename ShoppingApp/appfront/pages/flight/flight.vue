<template>
  <view class="container">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="title">✈️ 机票预订</text>
    </view>

    <!-- 航班选择下拉，信息自动填充 -->
    <view class="form-item">
      <text class="label">选择航班</text>
      <picker mode="selector" :value="flightIndex" :range="flightList" @change="onFlightChange">
        <view class="input-box">{{ form.flightNumber || "请选择航班" }}</view>
      </picker>
    </view>

    <view class="form-item">
      <text class="label">出发城市</text>
      <view class="input-box">{{ form.departCity }}</view>
    </view>

    <view class="form-item">
      <text class="label">到达城市</text>
      <view class="input-box">{{ form.arriveCity }}</view>
    </view>

    <view class="form-item">
      <text class="label">出发时间</text>
      <view class="input-box">{{ form.departTime }}</view>
    </view>

    <view class="form-item">
      <text class="label">到达时间</text>
      <view class="input-box">{{ form.arriveTime }}</view>
    </view>

    <view class="form-item">
      <text class="label">乘客姓名</text>
      <input class="input-box" v-model="form.userName" placeholder="请输入姓名" />
    </view>

    <view class="form-item">
      <text class="label">联系电话</text>
      <input class="input-box" v-model="form.userPhone" placeholder="请输入11位手机号" type="number" maxlength="11" />
    </view>

    <view class="form-item">
      <text class="label">票价</text>
      <view class="input-box">¥{{ form.price }}</view>
    </view>

    <button class="submit-btn" @click="submitOrder">提交订单</button>
  </view>
    <RouteFloat/>
</template>

<script>
export default {
  data() {
    return {
      flightIndex: 0,
      flightList: ["CA1234 北京→广州", "MU5101 上海→北京", "CZ3521 深圳→上海"],
      flightData: [
        { flightNumber: "CA1234", departCity: "北京", arriveCity: "广州", departTime: "2025-05-20 08:00", arriveTime: "2025-05-20 10:30", price: 880 },
        { flightNumber: "MU5101", departCity: "上海", arriveCity: "北京", departTime: "2025-05-21 09:00", arriveTime: "2025-05-21 11:20", price: 920 },
        { flightNumber: "CZ3521", departCity: "深圳", arriveCity: "上海", departTime: "2025-05-22 14:00", arriveTime: "2025-05-22 16:10", price: 760 }
      ],
      form: {
        flightNumber: "",
        departCity: "",
        arriveCity: "",
        departTime: "",
        arriveTime: "",
        userName: "",
        userPhone: "",
        price: 0,
        loginUsername: ""
      }
    };
  },
  onLoad() {
    this.form.loginUsername = uni.getStorageSync("loginUsername") || "";
    this.onFlightChange({ detail: { value: 0 } });
  },
  methods: {
    onFlightChange(e) {
      const idx = e.detail.value;
      this.flightIndex = idx;
      const data = this.flightData[idx];
      this.form = { ...this.form, ...data };
    },

    submitOrder() {
      if (!this.form.userName) {
        uni.showToast({ title: "请输入姓名", icon: "none" });
        return;
      }
      if (!/^1\d{10}$/.test(this.form.userPhone)) {
        uni.showToast({ title: "手机号格式错误", icon: "none" });
        return;
      }

      this.form.loginUsername = uni.getStorageSync("loginUsername") || "";

      uni.request({
        url: "http://localhost:8080/api/flight-orders",
        method: "POST",
        header: { "Content-Type": "application/json" },
        data: this.form,
        success: () => {
          uni.showToast({ title: "预订成功", icon: "success" });
          setTimeout(() => {
            uni.navigateTo({ url: "/pages/my-orders/my-orders" });
          }, 1200);
        },
        fail: () => {
          uni.showToast({ title: "提交失败", icon: "none" });
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

/* 顶部标题 与HOME完全一致 */
.header {
  background: #fff;
  padding: 25rpx 0;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
}

.form-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 26rpx 30rpx;
  border-bottom: 1rpx solid #eeeeee;
  background: #fff;
  font-size: 32rpx;
}
.label {
  color: #333333;
}
.input-box {
  width: 60%;
  text-align: right;
  color: #666666;
}
.submit-btn {
  width: 90%;
  height: 92rpx;
  background-color: #1677ff;
  color: #ffffff;
  border-radius: 16rpx;
  margin: 60rpx auto;
  font-size: 34rpx;
  display: block;
}
</style>