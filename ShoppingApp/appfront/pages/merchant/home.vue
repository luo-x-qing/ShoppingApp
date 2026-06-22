<template>
  <view class="container">
    <view class="header">
      <view class="header-left">
        <view class="avatar-text" :style="{ backgroundColor: avatarColor }">
          <text class="avatar-letter">{{ avatarLetter }}</text>
        </view>
        <view class="info">
          <text class="shop-name">{{ userInfo.shopName || '商家名称' }}</text>
          <text class="username">{{ userInfo.name }}</text>
          <text class="status" :class="statusClass" v-if="userInfo.status && userInfo.status !== 'NORMAL'">
            {{ statusText }}
          </text>
        </view>
      </view>
      <view class="header-right" @click="goToNotifications">
        <view class="notification-icon">
          <text class="icon">🔔</text>
          <view class="badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</view>
        </view>
      </view>
    </view>

    <!-- 状态提示卡片（非正常状态时显示） -->
    <view class="status-card" v-if="userInfo.status && userInfo.status !== 'NORMAL'">
      <text class="status-icon">{{ statusIcon }}</text>
      <view class="status-info">
        <text class="status-title">{{ statusTitle }}</text>
        <text class="status-desc">{{ statusDesc }}</text>
      </view>
    </view>

    <!-- 申诉入口（账号异常时显示） -->
    <view class="appeal-entry" v-if="userInfo.status && userInfo.status !== 'NORMAL'" @click="goToAppeal">
      <text class="appeal-icon">📢</text>
      <view class="appeal-info">
        <text class="appeal-title">账号状态异常？</text>
        <text class="appeal-desc">点击这里提交申诉，管理员会尽快处理</text>
      </view>
      <text class="appeal-arrow">›</text>
    </view>

    <!-- 正常状态菜单 - 可点击 -->
    <view class="menu" v-if="userInfo.status === 'NORMAL'">
      <view class="menu-item" @click="navigateTo('/pages/merchant/hotel-list')">
        <text class="menu-icon">🏨</text>
        <text>酒店管理</text>
      </view>
      <view class="menu-item" @click="navigateTo('/pages/merchant/order-list')">
        <text class="menu-icon">📋</text>
        <text>订单管理</text>
      </view>
      <view class="menu-item" @click="navigateTo('/pages/merchant/comment-list')">
        <text class="menu-icon">💬</text>
        <text>评价管理</text>
      </view>
      <view class="menu-item" @click="navigateTo('/pages/merchant/messages')">
        <text class="menu-icon">💌</text>
        <text>客户消息</text>
        <view class="message-badge" v-if="messageUnreadCount > 0">
          <text>{{ messageUnreadCount > 99 ? '99+' : messageUnreadCount }}</text>
        </view>
      </view>
      <view class="menu-item" @click="navigateTo('/pages/merchant/analysis')">
        <text class="menu-icon">📈</text>
        <text>经营分析</text>
      </view>
      <view class="menu-item" @click="navigateTo('/pages/merchant/setting')">
        <text class="menu-icon">⚙️</text>
        <text>商家设置</text>
      </view>
    </view>

    <!-- 非正常状态时显示的占位卡片 - 不可点击 -->
    <view class="disabled-menu" v-else>
      <view class="disabled-item">
        <text class="menu-icon">🏨</text>
        <text>酒店管理</text>
        <text class="disabled-tag">{{ statusTag }}</text>
      </view>
      <view class="disabled-item">
        <text class="menu-icon">📋</text>
        <text>订单管理</text>
        <text class="disabled-tag">{{ statusTag }}</text>
      </view>
      <view class="disabled-item">
        <text class="menu-icon">💬</text>
        <text>评价管理</text>
        <text class="disabled-tag">{{ statusTag }}</text>
      </view>
      <view class="disabled-item">
        <text class="menu-icon">💌</text>
        <text>客户消息</text>
        <text class="disabled-tag">{{ statusTag }}</text>
      </view>
      <view class="disabled-item">
        <text class="menu-icon">⚙️</text>
        <text>商家设置</text>
        <text class="disabled-tag">{{ statusTag }}</text>
      </view>
    </view>

    <button class="logout-btn" @click="logout">退出登录</button>
  </view>
</template>

<script>
export default {
  data() {
    return {
      userInfo: {},
      unreadCount: 0,
      messageUnreadCount: 0,
      avatarColor: '#f0e68c'
    };
  },
  computed: {
    statusClass() {
      const status = this.userInfo.status;
      if (status === 'PENDING') return 'status-pending';
      if (status === 'REJECTED') return 'status-rejected';
      if (status === 'BANNED') return 'status-banned';
      return '';
    },
    statusText() {
      const status = this.userInfo.status;
      if (status === 'PENDING') return '待审核';
      if (status === 'REJECTED') return '已拒绝';
      if (status === 'BANNED') return '已禁用';
      return '';
    },
    statusTag() {
      const status = this.userInfo.status;
      if (status === 'PENDING') return '待审核中';
      if (status === 'REJECTED') return '审核未通过';
      if (status === 'BANNED') return '已禁用';
      return '暂不可用';
    },
    statusIcon() {
      const status = this.userInfo.status;
      if (status === 'PENDING') return '⏳';
      if (status === 'REJECTED') return '❌';
      if (status === 'BANNED') return '🔒';
      return '';
    },
    statusTitle() {
      const status = this.userInfo.status;
      if (status === 'PENDING') return '账号待审核';
      if (status === 'REJECTED') return '审核未通过';
      if (status === 'BANNED') return '账号已禁用';
      return '';
    },
    statusDesc() {
      const status = this.userInfo.status;
      if (status === 'PENDING') return '您的商家账号正在审核中，请等待管理员审核通过后使用全部功能。';
      if (status === 'REJECTED') return '您的商家账号审核未通过，请联系管理员了解详情。';
      if (status === 'BANNED') return '您的商家账号已被禁用，请联系管理员申诉。';
      return '';
    },
    avatarLetter() {
      const shopName = this.userInfo.shopName || this.userInfo.name || '商';
      const firstChar = shopName.charAt(0);
      return /[a-zA-Z]/.test(firstChar) ? firstChar.toUpperCase() : firstChar;
    }
  },
  onShow() {
    this.loadUserInfo();
    this.checkMerchantStatus();
    this.loadUnreadCount();
    this.loadMessageUnreadCount();
  },
  methods: {
    loadUserInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo');
        this.userInfo = userInfo || {};
        this.generateAvatarColor();
      } catch (e) {
        console.error('读取用户信息失败', e);
      }
    },

    generateAvatarColor() {
      const shopName = this.userInfo.shopName || this.userInfo.name || '商家';
      const colors = [
        '#f0e68c', '#ff6b6b', '#4ecdc4', '#45b7d1', '#96ceb4',
        '#ffeaa7', '#dfe6e9', '#74b9ff', '#a29bfe', '#fd79a8',
        '#00cec9', '#e17055', '#d63031', '#6c5ce7', '#a8e6cf'
      ];
      let sum = 0;
      for (let i = 0; i < shopName.length; i++) {
        sum += shopName.charCodeAt(i);
      }
      const index = sum % colors.length;
      this.avatarColor = colors[index];
    },

    loadUnreadCount() {
      const token = uni.getStorageSync('token');
      if (!token) return;

      uni.request({
        url: 'http://localhost:8080/api/merchant/notifications/unread-count',
        method: 'GET',
        header: {
          'Authorization': 'Bearer ' + token
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

    loadMessageUnreadCount() {
      const merchantId = this.userInfo.id;
      if (!merchantId) {
        this.messageUnreadCount = 0;
        return;
      }

      uni.request({
        url: `http://localhost:8080/api/messages/merchant/total-unread?merchantId=${merchantId}`,
        method: 'GET',
        success: (res) => {
          if (res.data && res.data.code === 200) {
            this.messageUnreadCount = res.data.data || 0;
          } else {
            this.messageUnreadCount = 0;
          }
        },
        fail: () => {
          this.messageUnreadCount = 0;
        }
      });
    },

    checkMerchantStatus() {
      const status = this.userInfo.status;

      if (status && status !== 'NORMAL') {
        let title = '';
        let content = '';

        if (status === 'PENDING') {
          title = '账号待审核';
          content = '您的商家账号正在审核中，请等待管理员审核通过后使用全部功能。';
        } else if (status === 'REJECTED') {
          title = '审核未通过';
          content = '您的商家账号审核未通过，请联系管理员了解详情。';
        } else if (status === 'BANNED') {
          title = '账号已禁用';
          content = '您的商家账号已被禁用，请联系管理员申诉。';
        }

        if (title) {
          uni.showModal({
            title: title,
            content: content,
            showCancel: false,
            confirmText: '我知道了',
            success: (res) => {
              if (res.confirm) {
                if (status === 'REJECTED' || status === 'BANNED') {
                  setTimeout(() => {
                    uni.reLaunch({ url: '/pages/login-register/login-register' });
                  }, 2000);
                }
              }
            }
          });
        }
      }
    },

    checkStatusAndAlert() {
      const status = this.userInfo.status;
      if (status !== 'NORMAL') {
        let message = '';
        if (status === 'PENDING') {
          message = '账号正在审核中，暂无法使用此功能';
        } else if (status === 'REJECTED') {
          message = '账号审核未通过，暂无法使用此功能';
        } else if (status === 'BANNED') {
          message = '商家已被禁用，暂无法使用此功能';
        } else {
          message = '账号状态异常，暂无法使用此功能';
        }

        uni.showToast({
          title: message,
          icon: 'none',
          duration: 2000
        });
        return false;
      }
      return true;
    },

    navigateTo(url) {
      if (this.checkStatusAndAlert()) {
        uni.navigateTo({ url });
      }
    },

    goToNotifications() {
      if (this.checkStatusAndAlert()) {
        uni.navigateTo({ url: '/pages/merchant/notifications' });
      }
    },

    goToAppeal() {
      uni.navigateTo({
        url: '/pages/merchant/appeal'
      });
    },

    logout() {
      uni.clearStorageSync();
      uni.reLaunch({
        url: '/pages/login-register/login-register'
      });
    }
  }
};
</script>

<style scoped>
.container {
  padding: 30rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
  box-sizing: border-box;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #f0e68c, #e6d058);
  padding: 40rpx;
  border-radius: 20rpx;
  margin-bottom: 30rpx;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.info {
  flex: 1;
}

.shop-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  display: block;
}

.username {
  font-size: 28rpx;
  color: #666;
  margin-top: 10rpx;
  display: block;
}

.status {
  display: inline-block;
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  margin-top: 10rpx;
}

.status-pending {
  background-color: #fff3cd;
  color: #856404;
}

.status-rejected {
  background-color: #f8d7da;
  color: #721c24;
}

.status-banned {
  background-color: #e2e3e5;
  color: #383d41;
}

.header-right {
  position: relative;
}

.notification-icon {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  border-radius: 40rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.notification-icon .icon {
  font-size: 44rpx;
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

/* 状态提示卡片 */
.status-card {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  display: flex;
  align-items: center;
  border-left: 8rpx solid;
}

.status-card .status-icon {
  font-size: 60rpx;
  margin-right: 20rpx;
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.status-desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
}

/* 申诉入口 */
.appeal-entry {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #fff9e6, #fff5d9);
  padding: 30rpx;
  border-radius: 20rpx;
  margin-bottom: 30rpx;
  border: 1px solid #f0e68c;
}

.appeal-icon {
  font-size: 48rpx;
  margin-right: 20rpx;
}

.appeal-info {
  flex: 1;
}

.appeal-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.appeal-desc {
  font-size: 24rpx;
  color: #666;
}

.appeal-arrow {
  font-size: 40rpx;
  color: #999;
}

/* 正常菜单 */
.menu {
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 30rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #eee;
  position: relative;
}

.menu-item:active {
  background-color: #f5f5f5;
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.message-badge {
  margin-left: auto;
  min-width: 36rpx;
  height: 36rpx;
  background-color: #ff4d4f;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

.message-badge text {
  color: #fff;
  font-size: 20rpx;
  font-weight: bold;
}

/* 禁用状态菜单 */
.disabled-menu {
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 30rpx;
  opacity: 0.6;
}

.disabled-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #eee;
}

.disabled-tag {
  margin-left: auto;
  font-size: 22rpx;
  color: #999;
  background-color: #f5f5f5;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.logout-btn {
  background-color: #ff6b6b;
  color: #fff;
  border-radius: 50rpx;
  margin-top: 30rpx;
}

.logout-btn:active {
  transform: scale(0.98);
}

/* 文字头像 */
.avatar-text {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  margin-right: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.avatar-letter {
  font-size: 52rpx;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}
</style>
