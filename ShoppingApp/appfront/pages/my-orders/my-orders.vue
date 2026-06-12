<template>
  <view class="container">
    <!-- 顶部切换标签 -->
    <view class="tabs">
      <view class="tab" :class="activeTab === 1 ? 'active' : ''" @click="switchTab(1)">酒店订单</view>
      <view class="tab" :class="activeTab === 2 ? 'active' : ''" @click="switchTab(2)">机票订单</view>
    </view>

    <!-- 酒店订单区域 -->
    <view v-if="activeTab === 1">
      <view class="empty-box" v-if="orderList.length === 0">暂无酒店订单</view>
      <view class="order-item" v-for="item in orderList" :key="item.id">
        <view class="item-top">
          <text>酒店预订</text>
          <text class="price">¥{{ item.price }}</text>
        </view>
        <view class="item-row">酒店：{{ item.name }}</view>
        <view class="item-row">入住：{{ item.checkIn }}</view>
        <view class="item-row">退房：{{ item.checkOut }}</view>

        <!-- 评价模块 + 星级评分 -->
        <view v-if="!item.commented" class="comment-area">
          <view class="star-row">
            <text class="star-title">评分：</text>
            <text 
              class="star-item" 
              v-for="n in 5" 
              :key="n"
              :style="{color: n <= item.score ? '#ffbc36' : '#ccc'}"
              @click="item.score = n"
            >★</text>
          </view>

          <input class="comment-input" v-model="item.content" placeholder="写下你的评价..." />
          <button class="comment-btn" @click="submitComment(item)">提交评价</button>
        </view>
        <view v-else class="comment-tip">✅ 该订单已完成评价，不可重复提交</view>
      </view>
    </view>

    <!-- 机票订单区域 -->
    <view v-if="activeTab === 2">
      <view class="empty-box" v-if="flightOrders.length === 0">暂无机票订单</view>
      <view class="order-item" v-for="item in flightOrders" :key="item.id">
        <view class="item-top">
          <text>机票预订</text>
          <text class="price">¥{{ item.price }}</text>
        </view>
        <view class="item-row">航班号：{{ item.flightNumber }}</view>
        <view class="item-row">{{ item.departCity }} → {{ item.arriveCity }}</view>
        <view class="item-row">出发时间：{{ item.departTime }}</view>
        <view class="item-row">乘客姓名：{{ item.userName }}</view>
      </view>
    </view>
  </view>
    <RouteFloat/>
</template>

<script>
export default {
  data() {
    return {
      activeTab: 1,
      orderList: [],
      flightOrders: [],
      username: ""
    };
  },

  onShow() {
    this.username = uni.getStorageSync("loginUsername") || "";
    this.loadOrderData();
  },

  methods: {
    switchTab(type) {
      this.activeTab = type;
      this.loadOrderData();
    },

    loadOrderData() {
      if (this.activeTab === 1) {
        this.getHotelOrders();
      } else {
        this.getFlightOrders();
      }
    },

    // 酒店订单
    getHotelOrders() {
      uni.request({
        url: "http://localhost:8080/api/hotel-orders/user?username=" + this.username,
        method: "GET",
        success: (res) => {
          this.orderList = (res.data || []).map(item => {
            item.content = "";
            item.score = 5;
            item.commented = false;
            return item;
          });
          this.checkAllCommentedOrders();
        }
      });
    },

    // 🔥 修复：获取所有评价，标记已评价订单
    checkAllCommentedOrders() {
      uni.request({
        url: "http://localhost:8080/api/hotel-comments/all",
        success: (res) => {
          const commentedOrderIds = res.data.map(c => c.orderId);
          this.orderList.forEach(item => {
            if (commentedOrderIds.includes(item.id)) {
              item.commented = true;
            }
          });
        }
      });
    },

    // 提交评价
    submitComment(item) {
      if (!item.content) return uni.showToast({ title: "请输入评价内容", icon: "none" });
      if (item.commented) return uni.showToast({ title: "已评价", icon: "none" });

      uni.request({
        url: "http://localhost:8080/api/hotel-comments",
        method: "POST",
        data: {
          hotelId: item.hotelId,
          orderId: item.id,
          content: item.content,
          score: item.score
        },
        success: (res) => {
          if (res.data === "已评价") {
            uni.showToast({ title: "该订单已评价", icon: "none" });
            item.commented = true;
          } else {
            uni.showToast({ title: "评价成功" });
            item.commented = true;
          }
        }
      });
    },

    // 机票订单
    getFlightOrders() {
      uni.request({
        url: "http://localhost:8080/api/flight-orders",
        method: "GET",
        success: (res) => {
          let all = res.data || [];
          this.flightOrders = all.filter(item => item.loginUsername === this.username);
        }
      });
    }
  }
};
</script>

<style scoped>
page {
  background-color: #f5f5f5;
}
.container {
  padding: 20rpx;
}
.tabs {
  display: flex;
  background-color: #fff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 25rpx 0;
  font-size: 32rpx;
}
.tab.active {
  background-color: #1677ff;
  color: #fff;
  border-radius: 12rpx;
}
.order-item {
  background-color: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}
.item-top {
  display: flex;
  justify-content: space-between;
  font-size: 34rpx;
  font-weight: bold;
  margin-bottom: 15rpx;
}
.price {
  color: #ff4d4f;
}
.item-row {
  font-size: 30rpx;
  color: #333;
  line-height: 2;
}

/* 星级评分 */
.star-row {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}
.star-title {
  font-size: 28rpx;
  color: #333;
  margin-right: 10rpx;
}
.star-item {
  font-size: 36rpx;
  margin-right: 8rpx;
}

.comment-area {
  margin-top: 20rpx;
}
.comment-input {
  width: 100%;
  height: 70rpx;
  border: 1rpx solid #eee;
  border-radius: 8rpx;
  padding: 0 15rpx;
  margin-bottom: 10rpx;
  font-size: 28rpx;
}
.comment-btn {
  width: 100%;
  height: 70rpx;
  background-color: #1677ff;
  color: #fff;
  border-radius: 8rpx;
  font-size: 28rpx;
}
.comment-tip {
  margin-top: 20rpx;
  font-size: 28rpx;
  color: #999;
}
.empty-box {
  text-align: center;
  padding: 80rpx 0;
  font-size: 30rpx;
  color: #999;
}
</style>