<template>
  <view class="container">
    <!-- 顶部背景 -->
    <view class="top-bg"></view>
    
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <text class="nav-title">个人中心</text>
      <view class="notification-icon" @click="goToNotifications">
        <text class="icon">🔔</text>
        <view class="badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</view>
      </view>
    </view>
    
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-info">
        <image class="avatar" :src="avatarUrl" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ userInfo.username || userInfo.name || '用户' }}</text>
          <text class="desc">{{ userInfo.bio || userInfo.desc || '这家伙很懒，什么都没留下' }}</text>
        </view>
      </view>
      
      <!-- 统计数据 -->
      <view class="stats-row">
        <view class="stat-item" @click="goToOrders">
          <text class="stat-num">{{ orderCount }}</text>
          <text class="stat-label">订单</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item" @click="goToCollection">
          <text class="stat-num">{{ collectionCount }}</text>
          <text class="stat-label">收藏</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-grid">
      <view class="menu-item" @click="goToOrders">
        <view class="menu-icon orders">📋</view>
        <text class="menu-text">我的订单</text>
      </view>
      <view class="menu-item" @click="goToCollection">
        <view class="menu-icon collection">⭐</view>
        <text class="menu-text">我的收藏</text>
      </view>
      <view class="menu-item" @click="goToSetting">
        <view class="menu-icon setting">⚙️</view>
        <text class="menu-text">设置</text>
      </view>
    </view>

    <!-- 其他功能 -->
    <view class="other-menu">
      <view class="other-item" @click="showAbout">
        <text class="other-text">关于我们</text>
        <text class="other-arrow">›</text>
      </view>
      <view class="other-item" @click="showFeedback">
        <text class="other-text">意见反馈</text>
        <text class="other-arrow">›</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <button class="logout-btn" @click="logout">退出登录</button>

    <!-- 版本信息 -->
    <view class="version">
      <text class="version-text">版本 v1.0.0</text>
    </view>

    <!-- 关于弹窗 -->
    <view class="modal-overlay" v-if="showAboutModal" @click="showAboutModal = false">
      <view class="about-modal" @click.stop>
        <image class="about-logo" src="/static/logo.png" mode="aspectFit" />
        <text class="about-name">旅游资源管理系统</text>
        <text class="about-version">版本 1.0.0</text>
        <text class="about-desc">为您提供优质的旅游服务体验</text>
        <view class="about-contact">
          <text>📧 service@example.com</text>
          <text>📞 400-123-4567</text>
        </view>
        <button class="about-close" @click="showAboutModal = false">关闭</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: "profile",
  data() {
    return {
      showAboutModal: false,
      userInfo: {},
      orderCount: 0,
      collectionCount: 0,
      unreadCount: 0,
      token: ''
    };
  },

  computed: {
    avatarUrl() {
      if (this.userInfo.avatar) {
        if (this.userInfo.avatar.startsWith('http')) {
          return this.userInfo.avatar;
        }
        if (this.userInfo.avatar.startsWith('/file')) {
          return 'http://localhost:8080' + this.userInfo.avatar;
        }
        return this.userInfo.avatar;
      }
      return '/static/default-avatar.png';
    }
  },

  onShow() {
    // 判断登录状态
    const token = uni.getStorageSync('token');
    const username = uni.getStorageSync('loginUsername');
    
    if (!token || !username) {
      uni.reLaunch({ url: '/pages/login-register/login-register' });
      return;
    }
    
    this.token = token;
    
    // 加载用户资料
    this.loadUserProfile();
    // 加载统计数据
    this.loadStats();
    // 加载未读通知数量
    this.loadUnreadCount();
  },

  methods: {
    loadUserProfile() {
      // 先从本地缓存读取
      const storedUserInfo = uni.getStorageSync('userInfo');
      
      if (storedUserInfo && Object.keys(storedUserInfo).length > 0) {
        this.userInfo = {
          id: storedUserInfo.id,
          username: storedUserInfo.username || storedUserInfo.name,
          bio: storedUserInfo.bio || storedUserInfo.desc,
          avatar: storedUserInfo.avatar,
          phone: storedUserInfo.phone,
          email: storedUserInfo.email,
          role: storedUserInfo.role
        };
      } else {
        // 如果没有缓存，从服务器获取
        this.fetchUserInfoFromServer();
      }
    },
    
    fetchUserInfoFromServer() {
      uni.request({
        url: 'http://localhost:8080/api/users/userinfo',
        method: 'GET',
        header: {
          'Authorization': 'Bearer ' + this.token
        },
        success: (res) => {
          if (res.statusCode === 200 && res.data) {
            const userData = res.data;
            this.userInfo = {
              id: userData.id,
              username: userData.username,
              bio: userData.bio || '',
              avatar: userData.avatar || '',
              phone: userData.phone || '',
              email: userData.email || '',
              role: userData.role
            };
            
            // 更新缓存
            uni.setStorageSync('userInfo', this.userInfo);
          }
        },
        fail: (err) => {
          console.error('获取用户信息失败', err);
        }
      });
    },
    
    loadStats() {
      // 获取订单数量
      const username = uni.getStorageSync('loginUsername');
      uni.request({
        url: `http://localhost:8080/api/hotel-orders/user?username=${username}`,
        method: 'GET',
        success: (res) => {
          let orders = [];
          if (res.data && res.data.code === 200) {
            orders = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            orders = res.data;
          }
          this.orderCount = orders.length;
        },
        fail: () => {
          this.orderCount = 0;
        }
      });
      
      // 获取收藏数量
      const key = `myCollection_${username}`;
      const collections = uni.getStorageSync(key) || [];
      this.collectionCount = collections.length;
    },
    
    loadUnreadCount() {
      if (!this.token) return;
      
      uni.request({
        url: 'http://localhost:8080/api/user/notifications/unread-count',
        method: 'GET',
        header: {
          'Authorization': 'Bearer ' + this.token
        },
        success: (res) => {
          if (res.data && res.data.code === 200) {
            this.unreadCount = res.data.data.count || 0;
          }
        },
        fail: () => {
          console.log('获取未读数量失败');
        }
      });
    },

    goToOrders() {
      uni.navigateTo({ url: "/pages/my-orders/my-orders" });
    },
    
    goToCollection() {
      uni.navigateTo({ url: "/pages/collection/collection" });
    },
    
    goToSetting() {
      uni.navigateTo({ url: "/pages/profile/setting" });
    },
    
    goToNotifications() {
      uni.navigateTo({ url: "/pages/profile/notifications" });
    },
    
    showAbout() {
      this.showAboutModal = true;
    },
    
    showFeedback() {
      uni.showModal({
        title: '意见反馈',
        content: '请将您的意见发送至：service@example.com',
        showCancel: false,
        confirmText: '我知道了'
      });
    },

    logout() {
      uni.showModal({
        title: '提示',
        content: '确定退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            uni.clearStorageSync();
            uni.reLaunch({ url: '/pages/login-register/login-register' });
          }
        }
      });
    }
  }
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  position: relative;
}

/* 顶部背景渐变 */
.top-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 320rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 0 0 40rpx 40rpx;
}

/* 顶部导航栏 */
.nav-bar {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 30rpx 0;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.notification-icon {
  position: relative;
  width: 70rpx;
  height: 70rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 35rpx;
}

.notification-icon .icon {
  font-size: 36rpx;
  color: #fff;
}

.notification-icon .badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  background-color: #ff4d4f;
  color: #fff;
  font-size: 20rpx;
  padding: 2rpx 10rpx;
  border-radius: 20rpx;
  min-width: 32rpx;
  text-align: center;
}

/* 用户卡片 */
.user-card {
  position: relative;
  background: #fff;
  margin: 20rpx 30rpx 20rpx;
  border-radius: 30rpx;
  padding: 40rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.08);
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  border: 3rpx solid #fff;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.info {
  flex: 1;
  margin-left: 25rpx;
}

.name {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.desc {
  display: block;
  font-size: 26rpx;
  color: #999;
}

.stats-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-top: 30rpx;
  border-top: 1rpx solid #eee;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.stat-divider {
  width: 1rpx;
  height: 50rpx;
  background-color: #eee;
}

/* 功能菜单网格 */
.menu-grid {
  display: flex;
  justify-content: space-around;
  background: #fff;
  margin: 20rpx 30rpx;
  padding: 30rpx 20rpx;
  border-radius: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.menu-icon {
  width: 90rpx;
  height: 90rpx;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  margin-bottom: 15rpx;
}

.menu-icon.orders {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.menu-icon.collection {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.menu-icon.setting {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}

.menu-text {
  font-size: 26rpx;
  color: #666;
}

/* 其他菜单 */
.other-menu {
  background: #fff;
  margin: 20rpx 30rpx;
  border-radius: 30rpx;
  overflow: hidden;
}

.other-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.other-item:last-child {
  border-bottom: none;
}

.other-text {
  font-size: 28rpx;
  color: #333;
}

.other-arrow {
  font-size: 32rpx;
  color: #ccc;
}

/* 退出登录按钮 */
.logout-btn {
  background: #fff;
  margin: 30rpx 30rpx 20rpx;
  border-radius: 50rpx;
  padding: 28rpx;
  font-size: 30rpx;
  color: #ff6b6b;
  border: none;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

/* 版本信息 */
.version {
  text-align: center;
  padding: 30rpx;
}

.version-text {
  font-size: 24rpx;
  color: #ccc;
}

/* 关于弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.about-modal {
  width: 80%;
  background: #fff;
  border-radius: 40rpx;
  padding: 50rpx 40rpx;
  text-align: center;
}

.about-logo {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 20rpx;
}

.about-name {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.about-version {
  display: block;
  font-size: 26rpx;
  color: #999;
  margin-bottom: 20rpx;
}

.about-desc {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 30rpx;
}

.about-contact {
  background: #f8f8f8;
  padding: 20rpx;
  border-radius: 20rpx;
  margin-bottom: 30rpx;
}

.about-contact text {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
}

.about-close {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 50rpx;
  padding: 24rpx;
  font-size: 30rpx;
  border: none;
}
</style>