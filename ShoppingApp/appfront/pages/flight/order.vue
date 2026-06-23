<template>
  <view class="container">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="title">📝 填写订单</text>
    </view>

    <!-- 航班信息卡片 -->
    <view class="flight-card">
      <view class="card-title">航班信息</view>
      
      <view class="flight-row">
        <text class="label">航班号</text>
        <text class="value">{{ flightInfo.flightNumber || '--' }}</text>
      </view>
      
      <view class="flight-row">
        <text class="label">航空公司</text>
        <text class="value">{{ flightInfo.airline || '--' }}</text>
      </view>
      
      <view class="flight-row">
        <text class="label">出发城市</text>
        <text class="value">{{ departureCity || '--' }}</text>
      </view>
      
      <view class="flight-row">
        <text class="label">到达城市</text>
        <text class="value">{{ arrivalCity || '--' }}</text>
      </view>
      
      <view class="flight-row">
        <text class="label">出发时间</text>
        <text class="value">{{ formatDisplayDateTime(flightInfo.departureTime) }}</text>
      </view>
      
      <view class="flight-row">
        <text class="label">到达时间</text>
        <text class="value">{{ formatDisplayDateTime(flightInfo.arrivalTime) }}</text>
      </view>
      
      <view class="flight-row">
        <text class="label">飞行时长</text>
        <text class="value">{{ flightInfo.duration || '--' }}</text>
      </view>
      
      <view class="flight-row">
        <text class="label">单价</text>
        <text class="value price">¥{{ flightInfo.price || '--' }}</text>
      </view>
    </view>

    <!-- 乘机人信息 -->
    <view class="passenger-card">
      <view class="card-title">乘机人信息</view>
      
      <view class="form-item">
        <text class="label">乘机人数</text>
        <picker mode="selector" :range="passengerCounts" @change="onPassengerCountChange">
          <view class="input-box">
            <text>{{ orderData.passengerCount }}人</text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>
      
      <!-- 动态乘机人表单 -->
      <view v-for="(passenger, index) in passengerList" :key="index" class="passenger-section">
        <view class="passenger-title">乘机人{{ index + 1 }}</view>
        
        <view class="form-item">
          <text class="label">乘客姓名</text>
          <input 
            class="input-box" 
            v-model="passenger.userName" 
            placeholder="请输入真实姓名" 
            maxlength="20"
          />
        </view>
        
        <view class="form-item">
          <text class="label">身份证号</text>
          <input 
            class="input-box" 
            v-model="passenger.idCard" 
            placeholder="请输入身份证号" 
            maxlength="18"
          />
        </view>
        
        <view class="form-item">
          <text class="label">联系电话</text>
          <input 
            class="input-box" 
            v-model="passenger.userPhone" 
            placeholder="请输入11位手机号" 
            type="number" 
            maxlength="11"
          />
        </view>
      </view>
      
      <view class="form-item total-row">
        <text class="label">总票价</text>
        <text class="input-box total-price">¥{{ totalPrice }}</text>
      </view>
    </view>

    <button class="submit-btn" @click="submitOrder" :disabled="isSubmitting">
      {{ isSubmitting ? '提交中...' : '提交订单' }}
    </button>
  </view>
</template>

<script>
export default {
  data() {
    return {
      flightInfo: {},
      orderData: {
        passengerCount: 1,
        loginUsername: ''
      },
      passengerList: [
        {
          userName: '',
          idCard: '',
          userPhone: ''
        }
      ],
      passengerCounts: [1, 2, 3, 4, 5],
      isSubmitting: false
    };
  },
  
  computed: {
    totalPrice() {
      const price = parseFloat(this.flightInfo.price) || 0;
      return price * this.orderData.passengerCount;
    },
    
    departureCity() {
      const airport = this.flightInfo.departureAirport || '';
      return airport.split('机场')[0] || airport || '--';
    },
    
    arrivalCity() {
      const airport = this.flightInfo.arrivalAirport || '';
      return airport.split('机场')[0] || airport || '--';
    }
  },
  
  onLoad() {
    console.log('订单页面加载');
    
    // 获取从搜索页面传递过来的航班信息
    const flight = uni.getStorageSync('selectedFlight');
    console.log('获取到的航班信息：', flight);
    
    if (flight && Object.keys(flight).length > 0) {
      this.flightInfo = flight;
    } else {
      uni.showToast({ 
        title: '请先选择航班', 
        icon: 'none',
        duration: 2000
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 2000);
    }
    
    // 获取登录用户名
    const loginUsername = uni.getStorageSync('loginUsername');
    this.orderData.loginUsername = loginUsername || '';
    
    // 如果没有登录，提示去登录
    if (!this.orderData.loginUsername) {
      uni.showToast({ 
        title: '请先登录', 
        icon: 'none',
        duration: 2000
      });
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/login-register/login-register'
        });
      }, 2000);
    }
    
    console.log('订单数据初始化完成', this.flightInfo);
  },
  
  methods: {
    // 乘机人数变化
    onPassengerCountChange(e) {
      const newCount = this.passengerCounts[e.detail.value];
      this.orderData.passengerCount = newCount;
      
      // 动态调整乘机人列表
      const currentCount = this.passengerList.length;
      if (newCount > currentCount) {
        // 增加乘机人
        for (let i = currentCount; i < newCount; i++) {
          this.passengerList.push({
            userName: '',
            idCard: '',
            userPhone: ''
          });
        }
      } else if (newCount < currentCount) {
        // 减少乘机人
        this.passengerList = this.passengerList.slice(0, newCount);
      }
    },
    
    // 格式化显示日期时间（用于页面展示）
    formatDisplayDateTime(dateTimeStr) {
      if (!dateTimeStr) return '--';
      // 如果是 "2026-06-15 08:00" 格式，直接返回
      if (dateTimeStr.includes(' ')) {
        return dateTimeStr;
      }
      return dateTimeStr;
    },
    
    // 格式化时间用于提交（转为 LocalDateTime 格式）
    formatDateTimeForSubmit(dateTimeStr) {
      if (!dateTimeStr) return null;
      try {
        // 如果是 "2026-06-15 08:00" 格式，转换为 "2026-06-15T08:00:00"
        if (dateTimeStr.includes(' ') && !dateTimeStr.includes('T')) {
          const [date, time] = dateTimeStr.split(' ');
          const timeWithSeconds = time.split(':').length === 2 ? time + ':00' : time;
          return `${date}T${timeWithSeconds}`;
        }
        // 如果已经是 ISO 格式
        if (dateTimeStr.includes('T')) {
          return dateTimeStr;
        }
        return dateTimeStr;
      } catch (e) {
        console.error('日期解析错误：', e);
        return null;
      }
    },
    
    // 提交订单 - 为每个乘客单独创建订单
    async submitOrder() {
      console.log('提交订单', this.orderData);
      console.log('乘机人列表', this.passengerList);
      
      // 校验登录
      if (!this.orderData.loginUsername) {
        uni.showToast({ title: '请先登录', icon: 'none' });
        setTimeout(() => {
          uni.navigateTo({
            url: '/pages/login-register/login-register'
          });
        }, 1500);
        return;
      }
      
      // 校验所有乘机人信息
      for (let i = 0; i < this.passengerList.length; i++) {
        const passenger = this.passengerList[i];
        const passengerNum = i + 1;
        
        // 校验姓名
        if (!passenger.userName || passenger.userName.trim() === '') {
          uni.showToast({ title: `请输入乘机人${passengerNum}的姓名`, icon: 'none' });
          return;
        }
        if (passenger.userName.length < 2) {
          uni.showToast({ title: `请输入乘机人${passengerNum}的真实姓名`, icon: 'none' });
          return;
        }
        
        // 校验身份证号
        if (!passenger.idCard || passenger.idCard.trim() === '') {
          uni.showToast({ title: `请输入乘机人${passengerNum}的身份证号`, icon: 'none' });
          return;
        }
        const idCardRegex = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/;
        if (!idCardRegex.test(passenger.idCard)) {
          uni.showToast({ title: `乘机人${passengerNum}身份证号格式不正确`, icon: 'none' });
          return;
        }
        
        // 校验手机号
        if (!passenger.userPhone) {
          uni.showToast({ title: `请输入乘机人${passengerNum}的联系电话`, icon: 'none' });
          return;
        }
        if (!/^1[3-9]\d{9}$/.test(passenger.userPhone)) {
          uni.showToast({ title: `乘机人${passengerNum}手机号格式不正确`, icon: 'none' });
          return;
        }
      }
      
      this.isSubmitting = true;
      uni.showLoading({ title: '正在提交订单...', mask: true });
      
      // 为每个乘客单独创建订单
      const price = parseFloat(this.flightInfo.price) || 0;
      let successCount = 0;
      let failCount = 0;
      
      for (let i = 0; i < this.passengerList.length; i++) {
        const passenger = this.passengerList[i];
        const passengerNum = i + 1;
        
        // 构建单个订单数据
        const submitData = {
          flightNumber: this.flightInfo.flightNumber,
          departCity: this.departureCity,
          arriveCity: this.arrivalCity,
          departTime: this.formatDateTimeForSubmit(this.flightInfo.departureTime),
          arriveTime: this.formatDateTimeForSubmit(this.flightInfo.arrivalTime),
          price: price, // 单个乘客的票价
          username: this.orderData.loginUsername,
          passengerName: passenger.userName,
          passengerIdCard: passenger.idCard,
          contactPhone: passenger.userPhone,
          status: '待支付'
        };
        
        console.log(`提交第${passengerNum}个订单：`, submitData);
        
        // 调用后端接口创建订单
        try {
          const result = await this.createOrder(submitData);
          if (result.success) {
            successCount++;
          } else {
            failCount++;
          }
        } catch (error) {
          console.error(`第${passengerNum}个订单提交失败：`, error);
          failCount++;
        }
      }
      
      uni.hideLoading();
      
      // 显示提交结果
      if (successCount === this.passengerList.length) {
        // 全部成功
        uni.showToast({ 
          title: `成功预订${successCount}个订单！`, 
          icon: 'success' 
        });
        
        // 清除选中的航班缓存
        uni.removeStorageSync('selectedFlight');
        
        // 跳转到订单页面
        setTimeout(() => {
          uni.switchTab({
            url: '/pages/profile/profile'
          });
        }, 1500);
      } else if (successCount > 0) {
        // 部分成功
        uni.showModal({
          title: '提交结果',
          content: `成功提交${successCount}个订单，失败${failCount}个，请检查后重试`,
          showCancel: false,
          success: () => {
            if (successCount > 0) {
              uni.removeStorageSync('selectedFlight');
              setTimeout(() => {
                uni.switchTab({
                  url: '/pages/profile/profile'
                });
              }, 500);
            }
          }
        });
      } else {
        // 全部失败
        uni.showToast({ 
          title: '订单提交失败，请重试', 
          icon: 'none' 
        });
      }
      
      this.isSubmitting = false;
    },
    
    // 创建单个订单的异步方法
    createOrder(submitData) {
      return new Promise((resolve, reject) => {
        uni.request({
          url: 'http://localhost:8080/api/flight-orders',
          method: 'POST',
          header: {
            'Content-Type': 'application/json'
          },
          data: submitData,
          success: (res) => {
            console.log('单个订单提交结果：', res);
            if (res.statusCode === 200 && res.data.code === 200) {
              resolve({ success: true, data: res.data });
            } else {
              resolve({ success: false, message: res.data.message || '提交失败' });
            }
          },
          fail: (err) => {
            console.error('订单提交失败：', err);
            reject(err);
          }
        });
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
  width: 100%;
  min-height: 100vh;
  padding-bottom: 40rpx;
  box-sizing: border-box;
}

.header {
  background: linear-gradient(135deg, #1677ff, #0050b3);
  padding: 40rpx 0;
  text-align: center;
}

.header .title {
  color: #fff;
  font-size: 40rpx;
  font-weight: bold;
}

.flight-card, .passenger-card {
  background: #fff;
  margin: 30rpx;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
  margin-bottom: 20rpx;
}

.flight-row, .form-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.flight-row:last-child, .form-item:last-child {
  border-bottom: none;
}

.label {
  font-size: 28rpx;
  color: #666;
  width: 160rpx;
}

.value {
  font-size: 28rpx;
  color: #333;
  flex: 1;
  text-align: right;
}

.price {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff4d4f;
}

.input-box {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 28rpx;
  color: #333;
  text-align: right;
}

.input-box input {
  text-align: right;
  width: 100%;
}

.total-row {
  margin-top: 10rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #eee;
}

.total-price {
  font-size: 40rpx;
  font-weight: bold;
  color: #ff4d4f;
}

.arrow {
  margin-left: 10rpx;
  color: #ccc;
  font-size: 32rpx;
}

.submit-btn {
  width: 90%;
  height: 88rpx;
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
  border-radius: 44rpx;
  margin: 60rpx auto 30rpx;
  font-size: 34rpx;
  font-weight: bold;
  display: block;
}

.submit-btn[disabled] {
  opacity: 0.6;
}

.passenger-section {
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.passenger-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.passenger-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #1677ff;
  margin: 20rpx 0 10rpx 0;
  padding-left: 10rpx;
  border-left: 4rpx solid #1677ff;
}
</style>