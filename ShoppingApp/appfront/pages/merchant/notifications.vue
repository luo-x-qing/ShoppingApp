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
          <view class="item-footer" v-if="item.relatedName">
            <text class="related">🏨 {{ item.relatedName }}</text>
          </view>
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

    <!-- 酒店申诉弹窗 -->
    <view class="modal-mask" v-if="showHotelAppealModal" @click="closeHotelAppealModal">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">酒店申诉</text>
          <text class="modal-close" @click="closeHotelAppealModal">×</text>
        </view>
        <scroll-view class="modal-body" scroll-y>
          <view class="appeal-info">
            <view class="info-item">
              <text class="info-label">酒店名称：</text>
              <text class="info-value">{{ appealHotelName }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">禁用原因：</text>
              <text class="info-value">{{ appealHotelReason || '违规经营' }}</text>
            </view>
          </view>
          
          <view class="form-item">
            <text class="label">申诉类型</text>
            <picker :range="appealTypes" @change="onAppealTypeChange">
              <view class="picker">{{ appealType || '请选择申诉类型' }}</view>
            </picker>
          </view>
          
          <view class="form-item">
            <text class="label">申诉内容</text>
            <textarea 
              class="appeal-textarea" 
              v-model="appealContent" 
              placeholder="请详细描述您的问题，管理员会尽快处理..."
              maxlength="500"
              auto-height
            />
            <text class="char-count">{{ appealContent.length }}/500</text>
          </view>
          
          <view class="form-item">
            <text class="label">联系方式</text>
            <input class="input" v-model="contactInfo" placeholder="请输入您的联系电话或邮箱" />
          </view>
        </scroll-view>
        <view class="modal-footer">
          <button class="cancel-btn" @click="closeHotelAppealModal">取消</button>
          <button class="submit-btn" @click="submitHotelAppeal">提交申诉</button>
        </view>
      </view>
    </view>

    <!-- 申诉回复弹窗 -->
    <view class="modal-mask" v-if="showReplyModal" @click="showReplyModal = false">
      <view class="reply-container" @click.stop>
        <view class="reply-header">
          <text class="reply-title">{{ replyTitle }}</text>
          <text class="reply-close" @click="showReplyModal = false">×</text>
        </view>
        <view class="reply-body">
          <view class="reply-content">{{ replyContent }}</view>
          <view class="reply-message" v-if="replyMessage">
            <text class="reply-label">📝 管理员回复：</text>
            <text class="reply-text">{{ replyMessage }}</text>
          </view>
        </view>
        <view class="reply-footer">
          <button class="reply-btn" @click="showReplyModal = false">我知道了</button>
        </view>
      </view>
    </view>

    <!-- 账号申诉入口 -->
    <view class="appeal-entry" v-if="userInfo.status && userInfo.status !== 'NORMAL'" @click="goToAccountAppeal">
      <text class="appeal-icon">📢</text>
      <view class="appeal-info">
        <text class="appeal-title">账号状态异常？</text>
        <text class="appeal-desc">点击这里提交申诉，管理员会尽快处理</text>
      </view>
      <text class="appeal-arrow">›</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      notificationList: [],
      unreadCount: 0,
      userInfo: {},
      
      // 酒店申诉弹窗
      showHotelAppealModal: false,
      appealHotelId: null,
      appealHotelName: '',
      appealHotelReason: '',
      appealType: '',
      appealContent: '',
      contactInfo: '',
      appealTypes: ['酒店解封', '审核咨询', '信息修改', '投诉建议', '其他问题'],
      
      // 申诉回复弹窗
      showReplyModal: false,
      replyTitle: '',
      replyContent: '',
      replyMessage: ''
    };
  },
  onShow() {
    this.loadUserInfo();
    this.loadNotifications();
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
    
    loadNotifications() {
      const token = uni.getStorageSync('token');
      if (!token) return;
      
      uni.showLoading({ title: '加载中...' });
      
      uni.request({
        url: 'http://localhost:8080/api/merchant/notifications',
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
        url: 'http://localhost:8080/api/merchant/notifications/read-all',
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
        },
        fail: () => {
          uni.showToast({ title: '操作失败', icon: 'none' });
        }
      });
    },
    
    markAsRead(id) {
      const token = uni.getStorageSync('token');
      if (!token) return;
      
      uni.request({
        url: `http://localhost:8080/api/merchant/notifications/${id}/read`,
        method: 'PUT',
        header: {
          'Authorization': 'Bearer ' + token
        }
      });
    },
    
    viewDetail(item) {
      // 标记为已读
      if (item.status === 'UNREAD') {
        this.markAsRead(item.id);
        item.status = 'READ';
        this.unreadCount--;
      }
      
      // 根据类型处理
      if (item.type === 'HOTEL_BANNED') {
        // 酒店禁用通知 → 弹出申诉弹窗
        this.openHotelAppealModal(item.relatedId, item.relatedName, item.content);
      } else if (item.type === 'HOTEL_UNBANNED') {
        // 酒店启用通知 → 简单提示
        uni.showModal({
          title: item.title,
          content: item.content,
          showCancel: false,
          confirmText: '我知道了'
        });
      } else if (item.type === 'APPEAL_REPLY') {
        // 申诉回复通知 → 弹窗显示管理员的回复
        this.showReplyModal = true;
        this.replyTitle = item.title;
        this.replyContent = item.content;
        this.replyMessage = item.reply || '暂无回复内容';
      } else if (item.type === 'APPEAL_APPROVED') {
        uni.showModal({
          title: item.title,
          content: item.content,
          showCancel: false,
          confirmText: '我知道了'
        });
      } else if (item.type === 'APPEAL_REJECTED') {
        uni.showModal({
          title: item.title,
          content: item.content,
          showCancel: false,
          confirmText: '我知道了'
        });
      } else {
        // 其他通知（账号相关等）→ 弹窗显示详情
        uni.showModal({
          title: item.title,
          content: item.content,
          showCancel: false,
          confirmText: '我知道了'
        });
      }
    },
    
    openHotelAppealModal(hotelId, hotelName, content) {
      this.appealHotelId = hotelId;
      this.appealHotelName = hotelName;
      // 从通知内容中提取禁用原因
      const reasonMatch = content.match(/原因：(.*?)。/);
      this.appealHotelReason = reasonMatch ? reasonMatch[1] : '违规经营';
      this.appealType = '';
      this.appealContent = '';
      this.contactInfo = '';
      this.showHotelAppealModal = true;
    },
    
    closeHotelAppealModal() {
      this.showHotelAppealModal = false;
    },
    
    onAppealTypeChange(e) {
      this.appealType = this.appealTypes[e.detail.value];
    },
    
    submitHotelAppeal() {
      if (!this.appealType) {
        uni.showToast({ title: '请选择申诉类型', icon: 'none' });
        return;
      }
      if (!this.appealContent.trim()) {
        uni.showToast({ title: '请填写申诉内容', icon: 'none' });
        return;
      }
      if (!this.contactInfo.trim()) {
        uni.showToast({ title: '请填写联系方式', icon: 'none' });
        return;
      }
      
      uni.showLoading({ title: '提交中...' });
      
      // 调用申诉接口
      uni.request({
        url: 'http://localhost:8080/api/appeals/submit',
        method: 'POST',
        data: {
          username: this.userInfo.name,
          shopName: this.userInfo.shopName,
          status: this.userInfo.status,
          type: this.appealType,
          content: `【酒店申诉】酒店ID:${this.appealHotelId}，酒店名称:${this.appealHotelName}\n禁用原因:${this.appealHotelReason}\n\n申诉内容:${this.appealContent}`,
          contact: this.contactInfo
        },
        success: (res) => {
          uni.hideLoading();
          if (res.data && (res.data.code === 200 || res.data.success)) {
            uni.showToast({ 
              title: '申诉已提交，管理员会尽快处理', 
              icon: 'success',
              duration: 2000
            });
            this.closeHotelAppealModal();
          } else {
            uni.showToast({ 
              title: res.data?.message || '提交失败', 
              icon: 'none' 
            });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ 
            title: '申诉已提交，管理员会尽快处理', 
            icon: 'success',
            duration: 2000
          });
          this.closeHotelAppealModal();
        }
      });
    },
    
    goToAccountAppeal() {
      uni.navigateTo({
        url: '/pages/merchant/account-appeal'
      });
    },
    
    getIcon(type) {
      if (type === 'MERCHANT_BANNED') return '🔒';
      if (type === 'MERCHANT_UNBANNED') return '✅';
      if (type === 'HOTEL_BANNED') return '🏨⚠️';
      if (type === 'HOTEL_UNBANNED') return '🏨✅';
      if (type === 'APPEAL_APPROVED') return '✅';
      if (type === 'APPEAL_REJECTED') return '❌';
      if (type === 'APPEAL_REPLY') return '📝';
      return '📢';
    },
    
    getIconClass(type) {
      if (type === 'MERCHANT_BANNED') return 'icon-danger';
      if (type === 'HOTEL_BANNED') return 'icon-warning';
      if (type === 'APPEAL_REJECTED') return 'icon-danger';
      if (type === 'APPEAL_REPLY') return 'icon-info';
      if (type === 'MERCHANT_UNBANNED') return 'icon-success';
      if (type === 'HOTEL_UNBANNED') return 'icon-success';
      if (type === 'APPEAL_APPROVED') return 'icon-success';
      return '';
    },
    
    formatTime(timeStr) {
      if (!timeStr) return '';
      try {
        const date = new Date(timeStr);
        const now = new Date();
        const diff = now - date;
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const minutes = Math.floor(diff / (1000 * 60));
        
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
  margin-bottom: 20rpx;
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
  flex-shrink: 0;
}

.item-icon text {
  font-size: 40rpx;
}

.icon-danger {
  background-color: #fff0f0;
}

.icon-warning {
  background-color: #fff7e6;
}

.icon-success {
  background-color: #e6f7e6;
}

.icon-info {
  background-color: #e6f7ff;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
  flex-wrap: wrap;
  gap: 10rpx;
}

.item-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  word-break: break-all;
}

.item-time {
  font-size: 22rpx;
  color: #999;
  flex-shrink: 0;
}

.item-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}

.item-footer {
  margin-top: 12rpx;
}

.related {
  font-size: 22rpx;
  color: #1677ff;
  background-color: #e6f7ff;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  display: inline-block;
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

/* 申诉入口 */
.appeal-entry {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #fff9e6, #fff5d9);
  padding: 30rpx;
  border-radius: 20rpx;
  margin-top: 30rpx;
  border: 1px solid #f0e68c;
}

.appeal-icon {
  font-size: 48rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.appeal-info {
  flex: 1;
  min-width: 0;
}

.appeal-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
  word-break: break-all;
}

.appeal-desc {
  font-size: 24rpx;
  color: #666;
  word-break: break-all;
}

.appeal-arrow {
  font-size: 40rpx;
  color: #999;
  flex-shrink: 0;
}

/* 申诉弹窗样式 - 修复边界问题 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  box-sizing: border-box;
}

.modal-container {
  width: 100%;
  max-width: 600rpx;
  max-height: 80vh;
  background-color: #fff;
  border-radius: 30rpx;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.modal-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.modal-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
  padding: 0 10rpx;
}

.modal-body {
  flex: 1;
  padding: 30rpx;
  overflow-y: auto;
  box-sizing: border-box;
}

.modal-footer {
  display: flex;
  padding: 20rpx 30rpx;
  border-top: 1px solid #eee;
  gap: 20rpx;
  padding-bottom: 40rpx;
  flex-shrink: 0;
}

.cancel-btn, .submit-btn {
  flex: 1;
  border-radius: 50rpx;
  padding: 24rpx;
  font-size: 28rpx;
  border: none;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.submit-btn {
  background-color: #f0e68c;
  color: #333;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
  font-weight: 500;
}

.picker {
  width: 100%;
  padding: 24rpx;
  border: 1px solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  background-color: #fff;
  box-sizing: border-box;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.appeal-textarea {
  width: 100%;
  min-height: 150rpx;
  border: 1px solid #e0e0e0;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.char-count {
  font-size: 22rpx;
  color: #999;
  text-align: right;
  display: block;
  margin-top: 8rpx;
}

.input {
  width: 100%;
  padding: 24rpx;
  border: 1px solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  background-color: #fff;
  box-sizing: border-box;
}

.appeal-info {
  background-color: #f8f9fa;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
  word-break: break-all;
}

.info-item {
  display: flex;
  margin-bottom: 12rpx;
  flex-wrap: wrap;
}

.info-label {
  width: 140rpx;
  font-size: 26rpx;
  color: #666;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
  word-break: break-all;
}

.status-pending {
  color: #856404;
}

.status-rejected {
  color: #721c24;
}

.status-banned {
  color: #383d41;
}

.contact-tips {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: #fff9e6;
  border-radius: 12rpx;
  margin-top: 20rpx;
  flex-wrap: wrap;
}

.tips-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
  flex-shrink: 0;
}

.tips-text {
  font-size: 24rpx;
  color: #999;
  flex: 1;
  word-break: break-all;
}

/* 申诉回复弹窗样式 */
.reply-container {
  width: 80%;
  max-width: 560rpx;
  background-color: #fff;
  border-radius: 30rpx;
  overflow: hidden;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.reply-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.reply-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
  padding: 0 10rpx;
}

.reply-body {
  padding: 30rpx;
  max-height: 400rpx;
  overflow-y: auto;
}

.reply-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 20rpx;
  word-break: break-all;
}

.reply-message {
  background-color: #f0f7ff;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-top: 15rpx;
  word-break: break-all;
}

.reply-label {
  font-size: 26rpx;
  font-weight: bold;
  color: #1677ff;
  display: block;
  margin-bottom: 10rpx;
}

.reply-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
  word-break: break-all;
}

.reply-footer {
  padding: 20rpx 30rpx 40rpx;
  flex-shrink: 0;
}

.reply-btn {
  width: 100%;
  background-color: #f0e68c;
  color: #333;
  border-radius: 44rpx;
  padding: 24rpx;
  font-size: 28rpx;
  border: none;
}
</style>