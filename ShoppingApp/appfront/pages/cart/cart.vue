<template>
  <view class="container">
    <view class="header">
      <text class="title">🛒 购物车</text>
      <text class="clear-btn" @click="clearCart" v-if="items.length > 0">清空</text>
    </view>

    <view v-if="items.length === 0" class="empty">
      <text class="empty-icon">🛒</text>
      <text class="empty-text">购物车是空的</text>
      <text class="empty-hint">快去路线详情页添加门票吧</text>
    </view>

    <view v-else class="list">
      <view class="item-card" v-for="item in items" :key="item.id">
        <view class="item-left">
          <text class="item-name">{{ item.scenicName }}</text>
          <text class="item-price">¥{{ item.ticketPrice || 0 }}</text>
          <text class="item-source" v-if="item.routeId">来自路线推荐</text>
        </view>
        <view class="item-right">
          <view class="qty-control">
            <text class="qty-btn" @click="decreaseQty(item)">−</text>
            <text class="qty-num">{{ item.quantity }}</text>
            <text class="qty-btn" @click="increaseQty(item)">+</text>
          </view>
          <text class="item-subtotal">¥{{ ((item.ticketPrice || 0) * item.quantity).toFixed(2) }}</text>
          <text class="del-btn" @click="removeItem(item)">删除</text>
        </view>
      </view>
    </view>

    <view class="bottom-bar" v-if="items.length > 0">
      <view class="total">
        <text class="total-label">合计：</text>
        <text class="total-price">¥{{ totalPrice }}</text>
      </view>
      <button class="checkout-btn" @click="checkout">去结算</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      cart: null,
      items: []
    };
  },
  computed: {
    totalPrice() {
      return this.items
        .reduce((sum, item) => sum + (item.ticketPrice || 0) * item.quantity, 0)
        .toFixed(2);
    }
  },
  onShow() {
    this.loadCart();
  },
  methods: {
    getUsername() {
      return uni.getStorageSync("loginUsername") || "";
    },
    loadCart() {
      const username = this.getUsername();
      if (!username) {
        this.items = [];
        return;
      }
      uni.request({
        url: "http://localhost:8080/api/cart?username=" + encodeURIComponent(username),
        method: "GET",
        success: (res) => {
          this.cart = res.data;
          this.items = (res.data && res.data.items) || [];
        },
        fail: () => {
          this.items = [];
        }
      });
    },
    increaseQty(item) {
      const qty = (item.quantity || 0) + 1;
      this.updateQty(item.id, qty);
    },
    decreaseQty(item) {
      const qty = (item.quantity || 0) - 1;
      if (qty <= 0) {
        this.removeItem(item);
        return;
      }
      this.updateQty(item.id, qty);
    },
    updateQty(itemId, quantity) {
      const username = this.getUsername();
      uni.request({
        url: `http://localhost:8080/api/cart/item/${itemId}?username=${encodeURIComponent(username)}`,
        method: "PUT",
        data: { quantity },
        success: () => {
          this.loadCart();
        }
      });
    },
    removeItem(item) {
      const username = this.getUsername();
      uni.showModal({
        title: "提示",
        content: "确定删除此商品？",
        success: (res) => {
          if (res.confirm) {
            uni.request({
              url: `http://localhost:8080/api/cart/item/${item.id}?username=${encodeURIComponent(username)}`,
              method: "DELETE",
              success: () => {
                this.loadCart();
                uni.showToast({ title: "已删除" });
              }
            });
          }
        }
      });
    },
    clearCart() {
      const username = this.getUsername();
      uni.showModal({
        title: "提示",
        content: "确定清空购物车？",
        success: (res) => {
          if (res.confirm) {
            uni.request({
              url: `http://localhost:8080/api/cart/clear?username=${encodeURIComponent(username)}`,
              method: "DELETE",
              success: () => {
                this.loadCart();
                uni.showToast({ title: "已清空" });
              }
            });
          }
        }
      });
    },
    checkout() {
      uni.showToast({ title: "结算功能开发中", icon: "none" });
    }
  }
};
</script>

<style scoped>
page {
  background: #f5f5f5;
}
.container {
  min-height: 100vh;
  padding-bottom: 140rpx;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 25rpx 30rpx;
}
.title {
  font-size: 34rpx;
  font-weight: bold;
}
.clear-btn {
  font-size: 26rpx;
  color: #ff4d4f;
}
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
}
.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}
.empty-text {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}
.empty-hint {
  font-size: 26rpx;
  color: #999;
  margin-top: 10rpx;
}
.list {
  padding: 20rpx;
}
.item-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
}
.item-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.item-name {
  font-size: 30rpx;
  font-weight: bold;
}
.item-price {
  font-size: 28rpx;
  color: #ff4d4f;
}
.item-source {
  font-size: 22rpx;
  color: #1677ff;
  background: #e8f0ff;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  align-self: flex-start;
}
.item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12rpx;
}
.qty-control {
  display: flex;
  align-items: center;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
}
.qty-btn {
  width: 60rpx;
  height: 50rpx;
  text-align: center;
  line-height: 50rpx;
  font-size: 30rpx;
  color: #333;
}
.qty-num {
  width: 60rpx;
  height: 50rpx;
  text-align: center;
  line-height: 50rpx;
  font-size: 28rpx;
  border-left: 1rpx solid #ddd;
  border-right: 1rpx solid #ddd;
}
.item-subtotal {
  font-size: 28rpx;
  font-weight: bold;
  color: #ff4d4f;
}
.del-btn {
  font-size: 24rpx;
  color: #999;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.08);
  z-index: 100;
}
.total {
  display: flex;
  align-items: baseline;
}
.total-label {
  font-size: 28rpx;
  color: #333;
}
.total-price {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff4d4f;
}
.checkout-btn {
  background: #1677ff;
  color: #fff;
  border-radius: 12rpx;
  padding: 20rpx 60rpx;
  font-size: 30rpx;
  border: none;
}
</style>
