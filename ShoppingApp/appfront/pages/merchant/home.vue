<template>
  <view class="container">
    <view class="header">
      <view class="header-left">
        <image class="avatar" :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill"></image>
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
      </view>
      <view class="menu-item" @click="navigateTo('/pages/merchant/setting')">
        <text class="menu-icon">⚙️</text>
        <text>商家设置</text>
      </view>
    </view>
    
    <!-- 非正常状态时显示的占位卡片 -->
    <view class="disabled-menu" v-else>
      <view class="disabled-item">
        <text class="menu-icon">🏨</text>
        <text>酒店管理</text>
        <text class="disabled-tag">暂不可用</text>
      </view>
      <view class="disabled-item">
        <text class="menu-icon">📋</text>
        <text>订单管理</text>
        <text class="disabled-tag">暂不可用</text>
      </view>
      <view class="disabled-item">
        <text class="menu-icon">💬</text>
        <text>评价管理</text>
        <text class="disabled-tag">暂不可用</text>
      </view>
      <view class="disabled-item">
        <text class="menu-icon">💌</text>
        <text>客户消息</text>
        <text class="disabled-tag">暂不可用</text>
      </view>
      <view class="disabled-item">
        <text class="menu-icon">⚙️</text>
        <text>商家设置</text>
        <text class="disabled-tag">暂不可用</text>
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
      unreadCount: 0
    };
  },
  computed: {
    // 状态样式类
    statusClass() {
      const status = this.userInfo.status;
      if (status === 'PENDING') return 'status-pending';
      if (status === 'REJECTED') return 'status-rejected';
      if (status === 'BANNED') return 'status-banned';
      return '';
    },
    // 状态简短文字
    statusText() {
      const status = this.userInfo.status;
      if (status === 'PENDING') return '待审核';
      if (status === 'REJECTED') return '已拒绝';
      if (status === 'BANNED') return '已禁用';
      return '';
    },
    // 状态图标
    statusIcon() {
      const status = this.userInfo.status;
      if (status === 'PENDING') return '⏳';
      if (status === 'REJECTED') return '❌';
      if (status === 'BANNED') return '🔒';
      return '';
    },
    // 状态标题
    statusTitle() {
      const status = this.userInfo.status;
      if (status === 'PENDING') return '账号待审核';
      if (status === 'REJECTED') return '审核未通过';
      if (status === 'BANNED') return '账号已禁用';
      return '';
    },
    // 状态描述
    statusDesc() {
      const status = this.userInfo.status;
      if (status === 'PENDING') return '您的商家账号正在审核中，请等待管理员审核通过后使用全部功能。';
      if (status === 'REJECTED') return '您的商家账号审核未通过，请联系管理员了解详情。';
      if (status === 'BANNED') return '您的商家账号已被禁用，请联系管理员。';
      return '';
    }
  },
  onShow() {
    this.loadUserInfo();
    this.checkMerchantStatus();
    this.loadUnreadCount();
  },
  methods: {
    loadUserInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo');
        this.userInfo = userInfo || {};
      } catch (e) {
        console.error('读取用户信息失败', e);
      }
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
    
    checkMerchantStatus() {
      const status = this.userInfo.status;
      
      // 非正常状态时显示提示弹窗
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
          content = '您的商家账号已被禁用，请联系管理员。';
        }
        
        if (title) {
          uni.showModal({
            title: title,
            content: content,
            showCancel: false,
            confirmText: '我知道了',
            success: (res) => {
              if (res.confirm) {
                // 如果是已拒绝或已禁用，可以跳转到登录页
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
    
    navigateTo(url) {
      // 只有正常状态才能跳转
      if (this.userInfo.status !== 'NORMAL') {
        uni.showToast({ 
          title: this.statusText + '，无法使用此功能', 
          icon: 'none' 
        });
        return;
      }
      uni.navigateTo({ url });
    },
    
    goToNotifications() {
      uni.navigateTo({
        url: '/pages/merchant/notifications'
      });
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

<style>
.container {
  padding: 30rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
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

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  background-color: #fff;
  margin-right: 30rpx;
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

/* 右侧通知图标 */
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
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 40rpx;
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
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
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
</style>