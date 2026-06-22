<template>
  <view class="container">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="title">⚙️ 设置</text>
    </view>

    <!-- 用户信息卡片 -->
    <view class="info-card">
      <view class="user-row">
        <image class="avatar" :src="getFullImageUrl(userInfo.avatar)" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ userInfo.username || '用户' }}</text>
          <text class="role">{{ userInfo.role === 'MERCHANT' ? '商家账号' : '普通用户' }}</text>
        </view>
      </view>
    </view>

    <!-- 设置菜单 -->
    <view class="setting-list">
      <!-- 账号安全 -->
      <view class="setting-section">
        <view class="section-title">账号安全</view>
        <view class="setting-item" @click="editField('password')">
          <text class="item-label">修改密码</text>
          <view class="item-right">
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="setting-item" @click="editField('phone')">
          <text class="item-label">绑定手机号</text>
          <view class="item-right">
            <text class="item-value">{{ userInfo.phone || '未绑定' }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="setting-item" @click="editField('email')">
          <text class="item-label">绑定邮箱</text>
          <view class="item-right">
            <text class="item-value">{{ userInfo.email || '未绑定' }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 个人信息 -->
      <view class="setting-section">
        <view class="section-title">个人信息</view>
        <view class="setting-item" @click="editField('avatar')">
          <text class="item-label">头像</text>
          <view class="item-right">
            <image class="avatar-preview" :src="getFullImageUrl(userInfo.avatar)" mode="aspectFill" />
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="setting-item" @click="editField('nickname')">
          <text class="item-label">昵称</text>
          <view class="item-right">
            <text class="item-value">{{ userInfo.nickname || userInfo.username || '未设置' }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="setting-item" @click="editField('bio')">
          <text class="item-label">个人简介</text>
          <view class="item-right">
            <text class="item-value bio">{{ userInfo.bio || '这家伙很懒，什么都没留下' }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 其他设置 -->
      <view class="setting-section">
        <view class="section-title">其他</view>
        <view class="setting-item" @click="clearCache">
          <text class="item-label">清除缓存</text>
          <view class="item-right">
            <text class="item-value">{{ cacheSize }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="setting-item" @click="showAbout">
          <text class="item-label">关于我们</text>
          <view class="item-right">
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="setting-item" @click="showPrivacy">
          <text class="item-label">隐私政策</text>
          <view class="item-right">
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 退出登录 -->
      <view class="logout-section">
        <button class="logout-btn" @click="logout">退出登录</button>
      </view>

      <!-- 版本信息 -->
      <view class="version-info">
        <text class="version-text">版本 v1.0.0</text>
      </view>
    </view>

    <!-- 编辑弹窗 -->
    <view class="modal-mask" v-if="showEditModal" @click="closeEditModal">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ editTitle }}</text>
          <text class="modal-close" @click="closeEditModal">×</text>
        </view>
        <view class="modal-body">
          <!-- 密码修改第一步：验证身份 -->
          <view v-if="editType === 'password' && !isPasswordVerified">
            <view class="input-group">
              <text class="input-label">当前密码</text>
              <input
                class="edit-input"
                v-model="currentPassword"
                placeholder="请输入当前密码"
                password
              />
            </view>
            <view class="input-group">
              <text class="input-label">绑定手机号</text>
              <input
                class="edit-input"
                v-model="verifyPhone"
                placeholder="请输入绑定的手机号"
                type="number"
              />
            </view>
          </view>
          <!-- 密码修改第二步：设置新密码 -->
          <view v-else-if="editType === 'password' && isPasswordVerified">
            <view class="input-group">
              <text class="input-label">新密码</text>
              <input
                class="edit-input"
                v-model="newPassword"
                placeholder="请输入新密码（至少6位）"
                password
              />
            </view>
            <view class="input-group">
              <text class="input-label">确认新密码</text>
              <input
                class="edit-input"
                v-model="confirmPassword"
                placeholder="请再次输入新密码"
                password
              />
            </view>
          </view>
          <!-- 其他编辑 -->
          <view v-else>
            <input
              v-if="editType !== 'textarea'"
              class="edit-input"
              v-model="editValue"
              :placeholder="'请输入' + editTitle"
              :type="editType === 'phone' ? 'number' : 'text'"
            />
            <textarea
              v-else
              class="edit-textarea"
              v-model="editValue"
              :placeholder="'请输入' + editTitle"
              maxlength="200"
              auto-height
            />
          </view>
        </view>
        <view class="modal-footer">
          <button class="cancel-btn" @click="closeEditModal">取消</button>
          <button class="confirm-btn" @click="submitEdit">保存</button>
        </view>
      </view>
    </view>

    <!-- 关于我们弹窗 -->
    <view class="modal-mask" v-if="showAboutModal" @click="showAboutModal = false">
      <view class="about-container" @click.stop>
        <view class="about-header">
          <text class="about-title">关于我们</text>
          <text class="about-close" @click="showAboutModal = false">×</text>
        </view>
        <view class="about-body">
          <image class="logo" src="/static/logo.png" mode="aspectFit" />
          <text class="app-name">旅游资源管理系统</text>
          <text class="app-version">版本 1.0.0</text>
          <text class="app-desc">为您提供优质的旅游服务体验</text>
          <view class="contact-info">
            <text>客服邮箱：service@example.com</text>
            <text>客服电话：400-123-4567</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      userInfo: {},
      token: '',
      cacheSize: '0KB',

      // 编辑弹窗
      showEditModal: false,
      editType: '',
      editTitle: '',
      editValue: '',
      editFieldName: '',

      // 密码修改相关
      currentPassword: '',
      verifyPhone: '',
      newPassword: '',
      confirmPassword: '',
      isPasswordVerified: false,

      // 关于弹窗
      showAboutModal: false
    };
  },
  onShow() {
    this.checkLogin();
    this.loadUserInfo();
    this.calculateCacheSize();
  },
  methods: {
    getFullImageUrl(path) {
      if (!path) return '/static/default-avatar.png';
      if (path.startsWith('http')) return path;
      if (path.startsWith('/file')) {
        return 'http://localhost:8080' + path;
      }
      return path;
    },

    checkLogin() {
      const token = uni.getStorageSync('token');
      const username = uni.getStorageSync('loginUsername');

      if (!token || !username) {
        uni.showToast({ title: '请先登录', icon: 'none' });
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login-register/login-register' });
        }, 1500);
        return;
      }

      this.token = token;
    },

    loadUserInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo');
        this.userInfo = userInfo || {};
      } catch (e) {
        console.error('读取用户信息失败', e);
      }
    },

    calculateCacheSize() {
      try {
        let size = 0;
        const keys = uni.getStorageInfoSync().keys;
        keys.forEach(key => {
          const value = uni.getStorageSync(key);
          const str = JSON.stringify(value);
          size += str.length;
        });

        if (size < 1024) {
          this.cacheSize = size + 'B';
        } else if (size < 1024 * 1024) {
          this.cacheSize = (size / 1024).toFixed(1) + 'KB';
        } else {
          this.cacheSize = (size / (1024 * 1024)).toFixed(1) + 'MB';
        }
      } catch (e) {
        this.cacheSize = '0KB';
      }
    },

    editField(field) {
      this.editType = field;
      this.editFieldName = field;

      // 重置密码相关状态
      this.currentPassword = '';
      this.verifyPhone = '';
      this.newPassword = '';
      this.confirmPassword = '';
      this.isPasswordVerified = false;

      switch(field) {
        case 'nickname':
          this.editTitle = '昵称';
          this.editValue = this.userInfo.nickname || '';
          this.showEditModal = true;
          break;
        case 'phone':
          this.editTitle = '手机号';
          this.editValue = this.userInfo.phone || '';
          this.showEditModal = true;
          break;
        case 'email':
          this.editTitle = '邮箱';
          this.editValue = this.userInfo.email || '';
          this.showEditModal = true;
          break;
        case 'bio':
          this.editTitle = '个人简介';
          this.editValue = this.userInfo.bio || '';
          this.editType = 'textarea';
          this.showEditModal = true;
          break;
        case 'password':
          this.editTitle = '修改密码';
          this.showEditModal = true;
          break;
        case 'avatar':
          this.chooseAvatar();
          return;
        default:
          return;
      }
    },

    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0];
          this.uploadAvatar(tempFilePath);
        }
      });
    },

    uploadAvatar(filePath) {
      uni.showLoading({ title: '上传中...' });

      uni.uploadFile({
        url: 'http://localhost:8080/api/upload',
        filePath: filePath,
        name: 'file',
        success: (uploadRes) => {
          uni.hideLoading();
          let avatarUrl = uploadRes.data;
          try {
            const data = JSON.parse(uploadRes.data);
            avatarUrl = data.url || data;
          } catch(e) {}

          if (avatarUrl && avatarUrl.startsWith('/file')) {
            avatarUrl = 'http://localhost:8080' + avatarUrl;
          }

          this.updateProfile({ avatar: avatarUrl });
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '上传失败', icon: 'none' });
        }
      });
    },

    closeEditModal() {
      this.showEditModal = false;
      this.editValue = '';
      this.currentPassword = '';
      this.verifyPhone = '';
      this.newPassword = '';
      this.confirmPassword = '';
      this.isPasswordVerified = false;
    },

    submitEdit() {
      if (this.editType === 'password') {
        if (!this.isPasswordVerified) {
          // 第一步：验证身份
          this.verifyIdentity();
        } else {
          // 第二步：修改密码
          this.updatePassword();
        }
      } else {
        if (!this.editValue.trim() && this.editType !== 'bio') {
          uni.showToast({ title: '请填写' + this.editTitle, icon: 'none' });
          return;
        }

        const updateData = {};
        switch(this.editType) {
          case 'nickname':
            updateData.username = this.editValue.trim();
            break;
          case 'phone':
            updateData.phone = this.editValue.trim();
            break;
          case 'email':
            updateData.email = this.editValue.trim();
            break;
          case 'bio':
            updateData.bio = this.editValue.trim();
            break;
        }
        this.updateProfile(updateData);
      }
    },

    verifyIdentity() {
      if (!this.currentPassword) {
        uni.showToast({ title: '请输入当前密码', icon: 'none' });
        return;
      }
      if (!this.verifyPhone) {
        uni.showToast({ title: '请输入绑定的手机号', icon: 'none' });
        return;
      }

      if (this.userInfo.phone !== this.verifyPhone) {
        uni.showToast({ title: '手机号不匹配', icon: 'none' });
        return;
      }

      uni.showLoading({ title: '验证中...' });

      uni.request({
        url: 'http://localhost:8080/api/users/login',
        method: 'POST',
        header: {
          'Content-Type': 'application/json'
        },
        data: {
          username: this.userInfo.username,
          password: this.currentPassword
        },
        success: (res) => {
          uni.hideLoading();
          if (res.statusCode === 200 && res.data && res.data.id) {
            this.isPasswordVerified = true;
            uni.showToast({ title: '验证成功', icon: 'success' });
          } else {
            uni.showToast({ title: '当前密码错误', icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    },

    updatePassword() {
      if (!this.newPassword || this.newPassword.length < 6) {
        uni.showToast({ title: '新密码至少6位', icon: 'none' });
        return;
      }
      if (this.newPassword !== this.confirmPassword) {
        uni.showToast({ title: '两次输入的密码不一致', icon: 'none' });
        return;
      }

      uni.showLoading({ title: '修改中...' });

      uni.request({
        url: 'http://localhost:8080/api/users/profile',
        method: 'PUT',
        header: {
          'Authorization': 'Bearer ' + this.token,
          'Content-Type': 'application/json'
        },
        data: {
          password: this.newPassword
        },
        success: (res) => {
          uni.hideLoading();
          if (res.statusCode === 200 && res.data && res.data.id) {
            uni.showToast({ title: '密码修改成功，请重新登录', icon: 'success' });
            this.closeEditModal();

            setTimeout(() => {
              uni.clearStorageSync();
              uni.reLaunch({ url: '/pages/login-register/login-register' });
            }, 1500);
          } else {
            uni.showToast({ title: '修改失败，请稍后重试', icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    },

    updateProfile(updateData) {
      uni.showLoading({ title: '保存中...' });

      uni.request({
        url: 'http://localhost:8080/api/users/profile',
        method: 'PUT',
        header: {
          'Authorization': 'Bearer ' + this.token,
          'Content-Type': 'application/json'
        },
        data: updateData,
        success: (res) => {
          uni.hideLoading();
          if (res.statusCode === 200 && res.data && res.data.id) {
            const updatedUserInfo = { ...this.userInfo, ...updateData };
            uni.setStorageSync('userInfo', updatedUserInfo);
            this.userInfo = updatedUserInfo;

            uni.showToast({ title: '修改成功', icon: 'success' });
            this.closeEditModal();
          } else {
            uni.showToast({ title: '修改失败', icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    },

    clearCache() {
      uni.showModal({
        title: '清除缓存',
        content: '确定要清除所有缓存吗？清除后需要重新登录。',
        success: (res) => {
          if (res.confirm) {
            uni.clearStorageSync();
            uni.showToast({ title: '缓存已清除', icon: 'success' });
            setTimeout(() => {
              uni.reLaunch({ url: '/pages/login-register/login-register' });
            }, 1500);
          }
        }
      });
    },

    showAbout() {
      this.showAboutModal = true;
    },

    showPrivacy() {
      uni.showModal({
        title: '隐私政策',
        content: '我们重视您的隐私保护。本应用收集的信息仅用于提供旅游服务，不会与第三方共享您的个人信息。',
        showCancel: false,
        confirmText: '我知道了'
      });
    },

    logout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
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
  background: linear-gradient(180deg, #f0eaf8 0%, #e8e0f2 100%);
}

.header {
  background: linear-gradient(135deg, #9b7bcf, #7b5cad);
  padding: 32rpx 0;
  text-align: center;
  padding-top: calc(32rpx + constant(safe-area-inset-top));
  padding-top: calc(32rpx + env(safe-area-inset-top));
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(123, 92, 173, 0.15);
}

.header .title {
  color: #fff;
  font-size: 36rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
}

.info-card {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(123, 92, 173, 0.08);
}

.user-row {
  display: flex;
  align-items: center;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  margin-right: 20rpx;
  background: linear-gradient(135deg, #ede5fc, #e0d4f5);
}

.info {
  flex: 1;
}

.name {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #5a3d8c;
  margin-bottom: 8rpx;
}

.role {
  display: block;
  font-size: 26rpx;
  color: #b8a6da;
}

.setting-list {
  margin: 0 20rpx 20rpx;
  background-color: #fff;
  border-radius: 28rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(123, 92, 173, 0.06);
}

.setting-section {
  border-bottom: 20rpx solid #f4f0fa;
}

.section-title {
  padding: 30rpx 30rpx 20rpx;
  font-size: 28rpx;
  color: #b8a6da;
  background-color: #fff;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background-color: #fff;
  border-bottom: 1px solid #f0eaf8;
}

.setting-item:active {
  background-color: #f8f4ff;
}

.item-label {
  font-size: 30rpx;
  color: #5a3d8c;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.item-value {
  font-size: 28rpx;
  color: #b8a6da;
  max-width: 400rpx;
  text-align: right;
}

.item-value.bio {
  max-width: 450rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-arrow {
  font-size: 32rpx;
  color: #d4c4ec;
}

.avatar-preview {
  width: 60rpx;
  height: 60rpx;
  border-radius: 30rpx;
  background: linear-gradient(135deg, #ede5fc, #e0d4f5);
}

.logout-section {
  padding: 40rpx 30rpx;
}

.logout-btn {
  width: 100%;
  background: linear-gradient(135deg, #e0a0a0, #d08080);
  color: #fff;
  border-radius: 50rpx;
  padding: 24rpx;
  font-size: 30rpx;
  border: none;
  box-shadow: 0 4rpx 12rpx rgba(208, 128, 128, 0.3);
}

.version-info {
  padding: 30rpx;
  text-align: center;
}

.version-text {
  font-size: 24rpx;
  color: #ccc0e0;
}

/* 弹窗样式 */
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
}

.modal-container {
  width: 85%;
  background-color: #fff;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 24rpx 48rpx rgba(123, 92, 173, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background: linear-gradient(135deg, #9b7bcf, #7b5cad);
}

.modal-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #fff;
}

.modal-close {
  font-size: 48rpx;
  color: rgba(255,255,255,0.8);
  line-height: 1;
}

.modal-body {
  padding: 40rpx 30rpx;
}

.input-group {
  margin-bottom: 30rpx;
}

.input-label {
  display: block;
  font-size: 28rpx;
  color: #5a3d8c;
  margin-bottom: 12rpx;
}

.edit-input {
  width: 100%;
  padding: 24rpx;
  border: 1px solid #e0d4f5;
  border-radius: 16rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  background: #f8f4ff;
}

.edit-input:focus {
  border-color: #9b7bcf;
}

.edit-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 24rpx;
  border: 1px solid #e0d4f5;
  border-radius: 16rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  background: #f8f4ff;
}

.modal-footer {
  display: flex;
  padding: 20rpx 30rpx 40rpx;
  gap: 20rpx;
}

.cancel-btn, .confirm-btn {
  flex: 1;
  border-radius: 50rpx;
  padding: 24rpx;
  font-size: 28rpx;
  border: none;
}

.cancel-btn {
  background: #f0eaf8;
  color: #b8a6da;
}

.confirm-btn {
  background: linear-gradient(135deg, #9b7bcf, #7b5cad);
  color: #fff;
}

/* 关于弹窗 */
.about-container {
  width: 80%;
  background-color: #fff;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 24rpx 48rpx rgba(123, 92, 173, 0.2);
}

.about-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background: linear-gradient(135deg, #9b7bcf, #7b5cad);
}

.about-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #fff;
}

.about-close {
  font-size: 48rpx;
  color: rgba(255,255,255,0.8);
  line-height: 1;
}

.about-body {
  padding: 40rpx;
  text-align: center;
}

.logo {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 20rpx;
}

.app-name {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #5a3d8c;
  margin-bottom: 10rpx;
}

.app-version {
  display: block;
  font-size: 26rpx;
  color: #b8a6da;
  margin-bottom: 20rpx;
}

.app-desc {
  display: block;
  font-size: 28rpx;
  color: #8a78a8;
  margin-bottom: 30rpx;
}

.contact-info {
  text-align: left;
  background: #f8f4ff;
  padding: 20rpx;
  border-radius: 16rpx;
}

.contact-info text {
  display: block;
  font-size: 26rpx;
  color: #8a78a8;
  margin-bottom: 10rpx;
}
</style>
