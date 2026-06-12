<template>
  <view class="container">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="title">👤 个人中心</text>
    </view>

    <!-- 用户信息卡片 -->
    <view class="info-card">
      <view class="user-row">
        <image class="avatar" :src="userInfo.avatar" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ userInfo.name }}</text>
          <text class="desc">{{ userInfo.desc }}</text>
        </view>
      </view>
      <button class="edit-btn" @click="showEditModal = true">编辑信息</button>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-box">
      <view class="menu-item" @click="goToOrders">
        <text class="menu-text">我的订单</text>
        <text class="arrow">></text>
      </view>
      <view class="menu-item" @click="goToCart">
        <text class="menu-text">🛒 购物车</text>
        <text class="arrow">></text>
      </view>
      <view class="menu-item" @click="goToCollection">
        <text class="menu-text">我的收藏夹</text>
        <text class="arrow">></text>
      </view>
      
      <!-- 退出登录 -->
      <view class="menu-item" @click="logout" style="color:#ff5722;">
        <text class="menu-text">退出登录</text>
        <text class="arrow">></text>
      </view>
    </view>

    <!-- 编辑弹窗 -->
    <view class="modal-overlay" v-if="showEditModal" @click="showEditModal = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">编辑个人信息</text>

        <view class="form-item">
          <text class="label">用户名</text>
          <input class="input" v-model="editForm.name" placeholder="请输入用户名" />
        </view>
        <view class="form-item">
          <text class="label">简介</text>
          <textarea class="textarea" v-model="editForm.desc" placeholder="介绍一下自己吧"></textarea>
        </view>

        <button class="save-btn" @click="saveInfo">保存</button>
      </view>
    </view>
  </view>
    <RouteFloat/>
</template>

<script>
// 默认用户资料（新用户默认值）
const defaultUser = {
  name: "点击编辑",
  desc: "这家伙很懒，什么都没留下",
  avatar: "/static/img/5.jpg"
};

export default {
  name: "profile",
  data() {
    return {
      showEditModal: false,
      userInfo: { ...defaultUser },
      editForm: { name: "", desc: "" }
    };
  },

  onShow() {
    // 1. 先判断登录状态
    const token = uni.getStorageSync('token');
    const username = uni.getStorageSync('loginUsername');
    
    if (!token || !username) {
      uni.reLaunch({ url: '/pages/login-register/login-register' });
      return;
    }

    // 2. 加载【当前登录用户】自己的资料
    this.loadUserProfile(username);
  },

  methods: {
    // 加载对应用户的资料
    loadUserProfile(username) {
      const key = `userProfile_${username}`;
      const saved = uni.getStorageSync(key);
      
      if (saved) {
        this.userInfo = saved;
      } else {
        this.userInfo = { ...defaultUser };
      }
    },

    goToOrders() {
      uni.navigateTo({ url: "/pages/my-orders/my-orders" });
    },
    goToCart() {
      uni.navigateTo({ url: "/pages/cart/cart" });
    },
    goToCollection() {
      uni.navigateTo({ url: "/pages/collection/collection" });
    },

    // 退出登录
    logout() {
      uni.showModal({
        title: '提示',
        content: '确定退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync('token');
            uni.removeStorageSync('loginUsername');
            uni.reLaunch({ url: '/pages/login-register/login-register' });
          }
        }
      })
    },

    // 保存【当前用户】自己的资料
    saveInfo() {
      const username = uni.getStorageSync('loginUsername');
      if (!username) return;

      this.userInfo.name = this.editForm.name || this.userInfo.name;
      
      this.userInfo.desc = this.editForm.desc || this.userInfo.desc;

      // 按用户名保存
      const key = `userProfile_${username}`;
      uni.setStorageSync(key, this.userInfo);

      this.showEditModal = false;
      uni.showToast({ title: "保存成功", icon: "success" });
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
  margin-bottom: 20rpx;
}

.info-card {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 12rpx;
}
.user-row {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}
.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  margin-right: 20rpx;
}
.info {
  flex: 1;
}

.name {
  display: block;
  font-size: 36rpx !important;
  font-weight: bold !important;
  color: #222;
  margin-bottom: 8rpx;
}

.id, .desc {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 8rpx;
}

.edit-btn {
  width: 100%;
  background: #1677ff;
  color: #fff;
  border-radius: 12rpx;
  padding: 15rpx 0;
  font-size: 28rpx;
  border: none;
  margin-top: 10rpx;
}

.menu-box {
  background: #fff;
  margin: 0 20rpx;
  border-radius: 12rpx;
  overflow: hidden;
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  font-size: 28rpx;
  border-bottom: 1rpx solid #eee;
}
.menu-item:last-child {
  border-bottom: none;
}
.menu-text {
  color: #333;
}
.arrow {
  color: #999;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.modal-content {
  width: 80%;
  background: #fff;
  border-radius: 12rpx;
  padding: 40rpx;
}
.modal-title {
  display: block;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 30rpx;
}
.form-item {
  margin-bottom: 25rpx;
}
.label {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 8rpx;
  display: block;
}
.input, .textarea {
  width: 100%;
  border: 1rpx solid #eee;
  border-radius: 12rpx;
  padding: 28rpx 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  background: #fafafa;
}
.textarea {
  height: 120rpx;
}
.save-btn {
  width: 100%;
  background: #1677ff;
  color: #fff;
  border-radius: 12rpx;
  padding: 20rpx 0;
  font-size: 28rpx;
  border: none;
  margin-top: 20rpx;
}
</style>