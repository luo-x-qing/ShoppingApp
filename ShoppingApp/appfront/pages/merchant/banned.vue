<template>
  <view class="container">
    <!-- 背景装饰 -->
    <view class="bg-decoration">
      <view class="circle circle-1"></view>
      <view class="circle circle-2"></view>
      <view class="circle circle-3"></view>
    </view>

    <!-- 禁用图标 -->
    <view class="icon-area">
      <view class="icon-circle">
        <text class="icon-emoji">🚫</text>
      </view>
    </view>

    <!-- 提示信息 -->
    <view class="info-card">
      <text class="title">账号已禁用</text>
      <text class="subtitle">您的商家账号已被管理员禁用</text>
      
      <view class="info-box">
        <view class="info-row">
          <text class="info-label">商家名称：</text>
          <text class="info-value">{{ shopName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">用户名：</text>
          <text class="info-value">{{ username }}</text>
        </view>
        <view class="info-row" v-if="banReason">
          <text class="info-label">禁用原因：</text>
          <text class="info-value reason-text">{{ banReason }}</text>
        </view>
      </view>

      <text class="description">
        您的账号因违反平台规则已被禁用，无法使用商家功能。如需申诉，请点击下方按钮联系管理员。
      </text>

      <!-- 操作按钮 -->
      <view class="button-group">
        <button class="appeal-btn" @click="openAppeal">提交申诉</button>
        <button class="logout-btn" @click="logout">返回登录</button>
      </view>
    </view>

    <!-- 申诉弹窗 -->
    <view class="modal-mask" v-if="showAppealModal" @click="closeAppealModal">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">📢 提交申诉</text>
          <text class="modal-close" @click="closeAppealModal">×</text>
        </view>
        <scroll-view class="modal-body" scroll-y>
          <view class="appeal-info">
            <view class="info-item">
              <text class="info-label">当前状态：</text>
              <text class="info-value status-banned">已禁用</text>
            </view>
            <view class="info-item">
              <text class="info-label">商家名称：</text>
              <text class="info-value">{{ shopName }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">用户名：</text>
              <text class="info-value">{{ username }}</text>
            </view>
          </view>
          
          <view class="form-item">
            <text class="label">申诉类型 <text class="required">*</text></text>
            <picker :range="appealTypes" @change="onAppealTypeChange">
              <view class="picker">
                {{ appealType || '请选择申诉类型' }}
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>
          
          <view class="form-item">
            <text class="label">申诉内容 <text class="required">*</text></text>
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
            <text class="label">联系方式 <text class="required">*</text></text>
            <input class="input-field" v-model="contactInfo" placeholder="请输入您的联系电话或邮箱" />
          </view>
          
          <view class="contact-tips">
            <text class="tips-icon">💡</text>
            <text class="tips-text">也可直接联系管理员：service@example.com | 客服电话：400-123-4567</text>
          </view>
        </scroll-view>
        <view class="modal-footer">
          <button class="cancel-btn" @click="closeAppealModal">取消</button>
          <button class="submit-btn" @click="submitAppeal">提交申诉</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      shopName: '',
      banReason: '',
      
      // 申诉相关
      showAppealModal: false,
      appealType: '',
      appealContent: '',
      contactInfo: '',
      appealTypes: ['账号解封', '误封申诉', '规则咨询', '其他问题']
    };
  },
  onLoad(options) {
    this.username = decodeURIComponent(options.username || '');
    this.shopName = decodeURIComponent(options.shopName || '');
    this.banReason = options.reason || '违规经营';
  },
  methods: {
    openAppeal() {
      this.showAppealModal = true;
    },
    
    closeAppealModal() {
      this.showAppealModal = false;
      // 重置表单
      this.appealType = '';
      this.appealContent = '';
      this.contactInfo = '';
    },
    
    onAppealTypeChange(e) {
      this.appealType = this.appealTypes[e.detail.value];
    },
    
    submitAppeal() {
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
      
      uni.request({
        url: 'http://localhost:8080/api/appeals/submit',
        method: 'POST',
        data: {
          username: this.username,
          shopName: this.shopName,
          status: 'BANNED',
          type: this.appealType,
          content: this.appealContent,
          contact: this.contactInfo
        },
        success: (res) => {
          uni.hideLoading();
          uni.showToast({ 
            title: '申诉已提交，管理员会尽快处理', 
            icon: 'success',
            duration: 2000
          });
          this.closeAppealModal();
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ 
            title: '申诉已提交，管理员会尽快处理', 
            icon: 'success',
            duration: 2000
          });
          this.closeAppealModal();
        }
      });
    },
    
    logout() {
      // 清除本地存储
      uni.removeStorageSync('token');
      uni.removeStorageSync('loginUsername');
      uni.removeStorageSync('userRole');
      uni.removeStorageSync('userInfo');
      
      // 跳转到登录页
      uni.reLaunch({ url: '/pages/login-register/login-register' });
    }
  }
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 600rpx;
  height: 600rpx;
  top: -200rpx;
  right: -200rpx;
}

.circle-2 {
  width: 400rpx;
  height: 400rpx;
  bottom: -100rpx;
  left: -100rpx;
}

.circle-3 {
  width: 300rpx;
  height: 300rpx;
  top: 40%;
  left: -100rpx;
}

/* 图标区域 */
.icon-area {
  margin-bottom: 40rpx;
  z-index: 1;
}

.icon-circle {
  width: 160rpx;
  height: 160rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.icon-emoji {
  font-size: 80rpx;
}

/* 信息卡片 */
.info-card {
  width: 100%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 48rpx;
  padding: 48rpx 40rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.2);
  z-index: 1;
}

.title {
  display: block;
  font-size: 44rpx;
  font-weight: bold;
  color: #ff4757;
  text-align: center;
  margin-bottom: 16rpx;
}

.subtitle {
  display: block;
  font-size: 28rpx;
  color: #666;
  text-align: center;
  margin-bottom: 32rpx;
}

.info-box {
  background-color: #f8f9fa;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 32rpx;
}

.info-row {
  display: flex;
  margin-bottom: 16rpx;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 160rpx;
  font-size: 26rpx;
  color: #666;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
  word-break: break-all;
  word-wrap: break-word;
  line-height: 1.4;
}

.reason-text {
  color: #ff4757;
}

.description {
  display: block;
  font-size: 26rpx;
  color: #999;
  line-height: 1.6;
  margin-bottom: 40rpx;
  text-align: center;
}

/* 按钮组 */
.button-group {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.appeal-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 60rpx;
  padding: 28rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
}

.appeal-btn:active {
  transform: scale(0.98);
}

.logout-btn {
  background-color: #f5f5f5;
  color: #666;
  border-radius: 60rpx;
  padding: 28rpx;
  font-size: 32rpx;
  border: none;
}

.logout-btn:active {
  transform: scale(0.98);
}

/* ========== 申诉弹窗样式 ========== */
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
}

.modal-container {
  width: 90%;
  max-width: 650rpx;
  max-height: 85vh;
  background-color: #fff;
  border-radius: 48rpx;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  flex-shrink: 0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.modal-close {
  font-size: 48rpx;
  color: #fff;
  line-height: 1;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  flex: 1;
  padding: 32rpx;
  overflow-y: auto;
  overflow-x: hidden;
  max-height: 60vh;
}

.modal-footer {
  display: flex;
  padding: 24rpx 32rpx 32rpx;
  border-top: 1px solid #eee;
  gap: 20rpx;
  flex-shrink: 0;
  background-color: #fff;
}

.cancel-btn,
.submit-btn {
  flex: 1;
  border-radius: 60rpx;
  padding: 24rpx;
  font-size: 28rpx;
  border: none;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.cancel-btn:active,
.submit-btn:active {
  transform: scale(0.98);
}

/* 表单样式 */
.form-item {
  margin-bottom: 28rpx;
  width: 100%;
}

.label {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.required {
  color: #ff4757;
}

.picker {
  width: 100%;
  padding: 22rpx 24rpx;
  border: 1px solid #e0e0e0;
  border-radius: 60rpx;
  font-size: 28rpx;
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-sizing: border-box;
}

.picker-arrow {
  font-size: 36rpx;
  color: #ccc;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.appeal-textarea {
  width: 100%;
  min-height: 160rpx;
  border: 1px solid #e0e0e0;
  border-radius: 24rpx;
  padding: 20rpx;
  font-size: 28rpx;
  line-height: 1.5;
  box-sizing: border-box;
}

.char-count {
  font-size: 22rpx;
  color: #999;
  text-align: right;
  display: block;
  margin-top: 8rpx;
}

.input-field {
  width: 100%;
  padding: 22rpx 24rpx;
  border: 1px solid #e0e0e0;
  border-radius: 60rpx;
  font-size: 28rpx;
  background-color: #fff;
  box-sizing: border-box;
}

/* 申诉信息卡片 */
.appeal-info {
  background-color: #f8f9fa;
  padding: 24rpx;
  border-radius: 24rpx;
  margin-bottom: 28rpx;
  width: 90%;
  box-sizing: border-box;
}

.info-item {
  display: flex;
  margin-bottom: 16rpx;
  width: 100%;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 150rpx;
  font-size: 26rpx;
  color: #666;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
  word-break: break-all;
  word-wrap: break-word;
  line-height: 1.4;
}

.status-banned {
  color: #ff4757;
}

/* 联系方式提示 */
.contact-tips {
  display: flex;
  align-items: flex-start;
  padding: 20rpx;
  background: linear-gradient(135deg, #fff9e6, #fff5d9);
  border-radius: 20rpx;
  margin-top: 20rpx;
  width: 100%;
  box-sizing: border-box;
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
  line-height: 1.4;
  word-break: break-all;
  word-wrap: break-word;
}
</style>