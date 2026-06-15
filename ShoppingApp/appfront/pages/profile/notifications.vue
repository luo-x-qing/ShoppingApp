<template>
  <view class="container">
    <view class="header">
      <text class="title">消息通知</text>
      <view class="header-actions">
        <text class="mark-all" @click="markAllAsRead" v-if="unreadCount > 0">全部已读</text>
      </view>
    </view>

    <!-- 未读数量提示 -->
    <view class="unread-tip" v-if="unreadCount > 0">
      <text class="tip-text">您有 {{ unreadCount }} 条未读消息</text>
    </view>

    <!-- 通知列表 -->
    <view class="notification-list" v-if="notificationList.length > 0">
      <view 
        class="notification-item" 
        v-for="item in notificationList" 
        :key="item.id"
        :class="{ unread: item.status === 'UNREAD' }"
        @click="viewDetail(item)"
      >
        <view class="item-icon" :class="getIconClass(item.type)">
          <text>{{ getIcon(item.type) }}</text>
        </view>
        <view class="item-content">
          <view class="item-header">
            <text class="item-title">{{ item.title }}</text>
            <text class="item-time">{{ formatTime(item.createTime) }}</text>
          </view>
          <text class="item-desc">{{ item.content }}</text>
        </view>
        <view class="item-badge" v-if="item.status === 'UNREAD'">
          <text class="badge">新</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-box" v-else>
      <text class="empty-icon">🔔</text>
      <text class="empty-text">暂无通知消息</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      notificationList: [],
      unreadCount: 0
    };
  },
  onShow() {
    this.loadNotifications();
  },
  methods: {
    loadNotifications() {
      const token = uni.getStorageSync('token');
      if (!token) return;
      
      uni.showLoading({ title: '加载中...' });
      
      uni.request({
        url: 'http://localhost:8080/api/user/notifications',
        method: 'GET',
        header: {
          'Authorization': 'Bearer ' + token
        },
        success: (res) => {
          uni.hideLoading();
          if (res.data && res.data.code === 200) {
            this.notificationList = res.data.data || [];
            this.unreadCount = this.notificationList.filter(n => n.status === 'UNREAD').length;
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '加载失败', icon: 'none' });
        }
      });
    },
    
    markAllAsRead() {
      const token = uni.getStorageSync('token');
      if (!token) return;
      
      uni.request({
        url: 'http://localhost:8080/api/user/notifications/read-all',
        method: 'PUT',
        header: {
          'Authorization': 'Bearer ' + token
        },
        success: (res) => {
          if (res.data && res.data.code === 200) {
            this.notificationList.forEach(n => {
              n.status = 'READ';
            });
            this.unreadCount = 0;
            uni.showToast({ title: '已全部标记为已读', icon: 'success' });
          }
        }
      });
    },
    
    markAsRead(id) {
      const token = uni.getStorageSync('token');
      if (!token) return;
      
      uni.request({
        url: `http://localhost:8080/api/user/notifications/${id}/read`,
        method: 'PUT',
        header: {
          'Authorization': 'Bearer ' + token
        }
      });
    },
    
    viewDetail(item) {
      if (item.status === 'UNREAD') {
        this.markAsRead(item.id);
        item.status = 'READ';
        this.unreadCount--;
      }
      
      uni.showModal({
        title: item.title,
        content: item.content,
        showCancel: false,
        confirmText: '我知道了'
      });
    },
    
    getIcon(type) {
      if (type === 'USER_BANNED') return '🔇';
      if (type === 'USER_UNBANNED') return '✅';
      return '📢';
    },
    
    getIconClass(type) {
      if (type === 'USER_BANNED') return 'icon-danger';
      if (type === 'USER_UNBANNED') return 'icon-success';
      return '';
    },
    
    formatTime(timeStr) {
      if (!timeStr) return '';
      try {
        const date = new Date(timeStr);
        const now = new Date();
        const diff = now - date;
        const minutes = Math.floor(diff / (1000 * 60));
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        
        if (minutes < 1) return '刚刚';
        if (minutes < 60) return `${minutes}分钟前`;
        if (hours < 24) return `${hours}小时前`;
        if (days < 7) return `${days}天前`;
        
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${month}-${day}`;
      } catch (e) {
        return timeStr;
      }
    }
  }
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background-color: #fff;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.mark-all {
  font-size: 26rpx;
  color: #1677ff;
}

.unread-tip {
  background-color: #fff7e6;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  text-align: center;
}

.tip-text {
  font-size: 26rpx;
  color: #fa8c16;
}

.notification-list {
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}

.notification-item {
  display: flex;
  padding: 30rpx;
  border-bottom: 1px solid #eee;
  position: relative;
}

.notification-item.unread {
  background-color: #f0f7ff;
}

.item-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.item-icon text {
  font-size: 40rpx;
}

.icon-danger {
  background-color: #fff0f0;
}

.icon-success {
  background-color: #e6f7e6;
}

.item-content {
  flex: 1;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.item-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.item-time {
  font-size: 22rpx;
  color: #999;
}

.item-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.4;
}

.item-badge {
  position: absolute;
  top: 30rpx;
  right: 30rpx;
}

.badge {
  background-color: #ff4d4f;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  background-color: #fff;
  border-radius: 20rpx;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 30rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>