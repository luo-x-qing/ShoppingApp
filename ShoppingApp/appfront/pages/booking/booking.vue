<template>
  <view class="container">
    <view class="header">
      <text class="title">酒店预订</text>
    </view>

    <!-- 酒店信息 -->
    <view class="info-card">
      <text class="hotel-name">{{ hotelName }}</text>
      <text class="price">¥{{ price }}/晚起</text>
    </view>

    <!-- 房型选择 -->
    <view class="room-selector" @click="showRoomPicker = true">
      <view class="selector-label">
        <text>选择房型</text>
        <text class="required">*</text>
      </view>
      <view class="selector-value">
        <text :class="roomTypeName ? 'selected' : 'placeholder'">
          {{ roomTypeName || '请选择房型' }}
        </text>
        <text class="arrow">›</text>
      </view>
    </view>

    <!-- 房型选择弹窗 -->
    <view class="room-picker-mask" v-if="showRoomPicker" @click="showRoomPicker = false">
      <view class="room-picker-container" @click.stop>
        <view class="picker-header">
          <text class="picker-title">选择房型</text>
          <text class="close-btn" @click="showRoomPicker = false">✕</text>
        </view>
        <scroll-view class="room-list" scroll-y="true">
          <view 
            class="room-option" 
            v-for="room in roomTypes" 
            :key="room.id"
            @click="selectRoom(room)"
          >
            <view class="room-option-info">
              <text class="room-option-name">{{ room.typeName }}</text>
              <text class="room-option-desc">{{ room.size }} | {{ room.bedType }} | {{ room.windowStatus }}</text>
              <text class="room-option-breakfast" v-if="room.breakfast">🍳 {{ room.breakfast }}</text>
            </view>
            <view class="room-option-right">
              <text class="room-option-price">¥{{ room.price }}</text>
              <text class="room-option-unit">/晚</text>
              <text class="room-option-stock">剩余{{ room.availableCount }}间</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 入住信息 -->
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
        <text>房间数量</text>
        <view class="room-count">
          <text class="count-btn" @click="decreaseCount">-</text>
          <text class="count-num">{{ roomCount }}</text>
          <text class="count-btn" @click="increaseCount">+</text>
        </view>
      </view>
      <view class="form-item">
        <text>入住天数</text>
        <text class="days">{{ days }} 晚</text>
      </view>
      <view class="form-item">
        <text>联系电话</text>
        <input class="phone-input" v-model="contactPhone" placeholder="请输入手机号" type="number" maxlength="11" />
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
      roomCount: 1,
      contactPhone: "",
      username: "",
      
      // 房型相关
      roomTypes: [],
      selectedRoom: null,
      roomTypeId: null,
      roomTypeName: "",
      showRoomPicker: false,

      // 日期限制
      minCheckInDate: "",
      minCheckOutDate: ""
    };
  },

  onLoad(options) {
    console.log("预订页面接收参数：", options);
    
    // 获取当前日期
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, "0");
    const day = String(today.getDate()).padStart(2, "0");
    const todayStr = `${year}-${month}-${day}`;
    
    // 计算明天的日期
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tYear = tomorrow.getFullYear();
    const tMonth = String(tomorrow.getMonth() + 1).padStart(2, "0");
    const tDay = String(tomorrow.getDate()).padStart(2, "0");
    const tomorrowStr = `${tYear}-${tMonth}-${tDay}`;
    
    // 计算后天的日期
    const dayAfterTomorrow = new Date(tomorrow);
    dayAfterTomorrow.setDate(dayAfterTomorrow.getDate() + 1);
    const dYear = dayAfterTomorrow.getFullYear();
    const dMonth = String(dayAfterTomorrow.getMonth() + 1).padStart(2, "0");
    const dDay = String(dayAfterTomorrow.getDate()).padStart(2, "0");
    const dayAfterTomorrowStr = `${dYear}-${dMonth}-${dDay}`;

    this.id = options.id;
    this.hotelName = decodeURIComponent(options.name || '酒店');
    this.price = Number(options.price) || 0;
    
    // 如果从详情页传入了房型信息
    if (options.roomTypeId) {
      this.roomTypeId = options.roomTypeId;
      this.roomTypeName = decodeURIComponent(options.roomTypeName || '');
      this.selectedRoom = { id: this.roomTypeId, typeName: this.roomTypeName, price: this.price };
    }
    
    // 设置日期限制：入住日期最早为明天（不能选择今天及以前）
    // 使用今天作为起始，这样用户可以看到今天之前的日期是灰色的不可选状态
    this.minCheckInDate = todayStr;
    this.checkInDate = tomorrowStr;
    
    // 退房日期最早为明天的日期
    this.minCheckOutDate = tomorrowStr;
    this.checkOutDate = dayAfterTomorrowStr;
    
    this.days = 1;
    this.totalPrice = this.price;
    this.roomCount = 1;
    this.contactPhone = "";
    
    // 获取用户名
    this.username = options.username || uni.getStorageSync("loginUsername") || "";
    
    // 加载房型列表
    this.getRoomTypes();
  },

  methods: {
    // 获取房型列表
    getRoomTypes() {
      uni.request({
        url: "http://localhost:8080/api/room-types/hotel/" + this.id,
        method: "GET",
        success: (res) => {
          console.log("房型列表返回：", res.data);
          
          let rooms = [];
          if (res.data && res.data.code === 200) {
            rooms = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            rooms = res.data;
          }
          
          this.roomTypes = rooms;
        },
        fail: (err) => {
          console.error("获取房型失败：", err);
        }
      });
    },
    
    // 选择房型
    selectRoom(room) {
      this.selectedRoom = room;
      this.roomTypeId = room.id;
      this.roomTypeName = room.typeName;
      this.price = room.price;
      this.showRoomPicker = false;
      this.calcDays();
      
      uni.showToast({ title: `已选择${room.typeName}`, icon: "success" });
    },
    
    // 入住日期变化
    bindCheckInChange(e) {
      const newCheckIn = e.detail.value;
      
      // 验证入住日期是否在今天之前
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const selectedDate = new Date(newCheckIn);
      selectedDate.setHours(0, 0, 0, 0);
      
      if (selectedDate < today) {
        uni.showToast({
          title: '不能选择今天之前的日期',
          icon: 'none'
        });
        // 重置为明天
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);
        this.checkInDate = this.formatDateStr(tomorrow);
        return;
      }
      
      this.checkInDate = newCheckIn;
      
      // 更新退房日期的最小值：至少为入住日期的下一天
      const checkInDateObj = new Date(newCheckIn);
      const minOutDate = new Date(checkInDateObj);
      minOutDate.setDate(minOutDate.getDate() + 1);
      this.minCheckOutDate = this.formatDateStr(minOutDate);
      
      // 如果当前退房日期早于新的最小退房日期，自动调整
      if (this.checkOutDate < this.minCheckOutDate) {
        this.checkOutDate = this.minCheckOutDate;
      }
      
      this.calcDays();
    },

    // 退房日期变化
    bindCheckOutChange(e) {
      const newCheckOut = e.detail.value;
      
      // 验证退房日期是否晚于入住日期
      if (newCheckOut <= this.checkInDate) {
        uni.showToast({
          title: '退房日期必须晚于入住日期',
          icon: 'none'
        });
        // 重置为最小有效日期（入住日期的下一天）
        const checkInDateObj = new Date(this.checkInDate);
        const minOutDate = new Date(checkInDateObj);
        minOutDate.setDate(minOutDate.getDate() + 1);
        this.checkOutDate = this.formatDateStr(minOutDate);
        return;
      }
      
      this.checkOutDate = newCheckOut;
      this.calcDays();
    },

    // 格式化日期
    formatDateStr(date) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    },
    
    decreaseCount() {
      if (this.roomCount > 1) {
        this.roomCount--;
        this.calcDays();
      }
    },
    
    increaseCount() {
      if (this.selectedRoom && this.roomCount >= this.selectedRoom.availableCount) {
        uni.showToast({ title: `最多可订${this.selectedRoom.availableCount}间`, icon: "none" });
        return;
      }
      this.roomCount++;
      this.calcDays();
    },

    calcDays() {
      const inDate = new Date(this.checkInDate);
      const outDate = new Date(this.checkOutDate);
      const diff = (outDate - inDate) / (1000 * 60 * 60 * 24);
      const days = diff > 0 ? diff : 1;
      this.days = days;
      this.totalPrice = days * this.price * this.roomCount;
    },

    submitOrder() {
      // 校验
      if (!this.roomTypeId) {
        uni.showToast({ title: "请选择房型", icon: "none" });
        return;
      }
      if (!this.contactPhone) {
        uni.showToast({ title: "请输入联系电话", icon: "none" });
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(this.contactPhone)) {
        uni.showToast({ title: "手机号格式不正确", icon: "none" });
        return;
      }
      
      // 验证日期
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const checkIn = new Date(this.checkInDate);
      checkIn.setHours(0, 0, 0, 0);
      
      if (checkIn < today) {
        uni.showToast({ title: "入住日期不能是今天之前", icon: "none" });
        return;
      }
      
      if (this.checkOutDate <= this.checkInDate) {
        uni.showToast({ title: "退房日期必须晚于入住日期", icon: "none" });
        return;
      }
      
      // 构建提交数据
      const submitData = {
        hotelId: parseInt(this.id),
        name: this.hotelName,
        roomTypeId: parseInt(this.roomTypeId),
        roomTypeName: this.roomTypeName,
        roomCount: this.roomCount,
        price: this.totalPrice,
        checkIn: this.checkInDate,
        checkOut: this.checkOutDate,
        username: this.username,
        contactPhone: this.contactPhone,
        status: "待支付"
      };
      
      console.log("提交订单数据：", submitData);
      
      uni.showLoading({ title: "提交中..." });
      
      uni.request({
        url: "http://localhost:8080/api/hotel-orders",
        method: "POST",
        header: {
          "Content-Type": "application/json"
        },
        data: submitData,
        success: (res) => {
          uni.hideLoading();
          console.log("订单提交结果：", res.data);
          
          if (res.data && res.data.code === 200) {
            uni.showToast({ title: "预订成功！", icon: "success" });
            setTimeout(() => {
              uni.switchTab({ url: "/pages/profile/profile" });
            }, 1500);
          } else {
            uni.showToast({ 
              title: res.data.message || "预订失败", 
              icon: "none" 
            });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error("提交订单失败：", err);
          uni.showToast({ title: "网络错误，请重试", icon: "none" });
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
  padding-bottom: 40rpx;
}
.header {
  background: linear-gradient(135deg, #1677ff, #0050b3);
  padding: 40rpx 0;
  text-align: center;
}
.header .title {
  color: #fff;
  font-size: 36rpx;
  font-weight: bold;
}

/* 酒店信息卡片 */
.info-card {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}
.hotel-name {
  font-size: 34rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 10rpx;
  color: #333;
}
.price {
  font-size: 32rpx;
  color: #ff6b35;
  font-weight: bold;
  display: block;
}

/* 房型选择器 */
.room-selector {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}
.selector-label {
  font-size: 28rpx;
  color: #333;
}
.required {
  color: #ff4d4f;
  margin-left: 4rpx;
}
.selector-value {
  display: flex;
  align-items: center;
  color: #999;
}
.selector-value .selected {
  color: #1677ff;
  font-weight: bold;
}
.selector-value .placeholder {
  color: #999;
}
.arrow {
  margin-left: 10rpx;
  font-size: 32rpx;
  color: #ccc;
}

/* 房型选择弹窗 */
.room-picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}
.room-picker-container {
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  height: 60vh;
  display: flex;
  flex-direction: column;
}
.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}
.picker-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}
.close-btn {
  font-size: 40rpx;
  color: #999;
  padding: 10rpx;
}
.room-list {
  flex: 1;
  padding: 20rpx 30rpx;
}
.room-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.room-option-info {
  flex: 1;
}
.room-option-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}
.room-option-desc {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-bottom: 6rpx;
}
.room-option-breakfast {
  font-size: 22rpx;
  color: #52c41a;
}
.room-option-right {
  text-align: right;
}
.room-option-price {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff6b35;
}
.room-option-unit {
  font-size: 22rpx;
  color: #999;
}
.room-option-stock {
  font-size: 22rpx;
  color: #999;
  display: block;
  margin-top: 6rpx;
}

/* 表单 */
.form-box {
  background: #fff;
  margin: 0 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}
.form-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eee;
  font-size: 28rpx;
}
.form-item:last-child {
  border-bottom: none;
}
.date {
  color: #1677ff;
}
.days, .total-price {
  color: #ff6b35;
  font-weight: bold;
}
.room-count {
  display: flex;
  align-items: center;
  gap: 30rpx;
}
.count-btn {
  width: 50rpx;
  height: 50rpx;
  background: #f5f5f5;
  border-radius: 50%;
  text-align: center;
  line-height: 50rpx;
  font-size: 36rpx;
  color: #1677ff;
}
.count-num {
  font-size: 30rpx;
  font-weight: bold;
  min-width: 50rpx;
  text-align: center;
}
.phone-input {
  text-align: right;
  color: #333;
  flex: 1;
}
.btn-box {
  padding: 40rpx 20rpx;
}
.submit-btn {
  width: 100%;
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
  border-radius: 44rpx;
  padding: 25rpx 0;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
}
</style>