<template>
  <view class="container">
    <view class="setting-list">
      <!-- 基本信息 -->
      <view class="setting-section">
        <view class="section-title">基本信息</view>
        <view class="setting-item" @click="editField('shopName')">
          <text class="item-label">商家名称</text>
          <view class="item-right">
            <text class="item-value">{{ userInfo.shopName || '未设置' }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="setting-item" @click="editField('phone')">
          <text class="item-label">联系电话</text>
          <view class="item-right">
            <text class="item-value">{{ userInfo.phone || '未设置' }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="setting-item" @click="editField('email')">
          <text class="item-label">电子邮箱</text>
          <view class="item-right">
            <text class="item-value">{{ userInfo.email || '未设置' }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 商家介绍 -->
      <view class="setting-section">
        <view class="section-title">商家介绍</view>
        <view class="setting-item" @click="editField('description')">
          <text class="item-label">商家简介</text>
          <view class="item-right">
            <text class="item-value description">{{ userInfo.shopDescription || '暂无简介' }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 账号安全 -->
      <view class="setting-section">
        <view class="section-title">账号安全</view>
        <view class="setting-item" @click="editField('password')">
          <text class="item-label">修改密码</text>
          <view class="item-right">
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="setting-item" @click="showLogoutConfirm">
          <text class="item-label">退出登录</text>
          <view class="item-right">
            <text class="item-arrow">›</text>
          </view>
        </view>
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
          <input 
            class="edit-input" 
            v-model="editValue" 
            :placeholder="'请输入' + editTitle"
            :type="editType === 'phone' ? 'number' : 'text'"
            :password="editType === 'password'"
          />
          <textarea 
            v-if="editType === 'description'"
            class="edit-textarea" 
            v-model="editValue" 
            placeholder="请输入商家简介"
            maxlength="200"
            auto-height
          />
        </view>
        <view class="modal-footer">
          <button class="cancel-btn" @click="closeEditModal">取消</button>
          <button class="confirm-btn" @click="submitEdit">保存</button>
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
      
      // 编辑弹窗
      showEditModal: false,
      editType: '',
      editTitle: '',
      editValue: '',
      editFieldName: ''
    };
  },
  onShow() {
    this.loadUserInfo();
    this.token = uni.getStorageSync('token');
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
    
    editField(field) {
      this.editType = field;
      this.editFieldName = field;
      
      switch(field) {
        case 'shopName':
          this.editTitle = '商家名称';
          this.editValue = this.userInfo.shopName || '';
          break;
        case 'phone':
          this.editTitle = '联系电话';
          this.editValue = this.userInfo.phone || '';
          break;
        case 'email':
          this.editTitle = '电子邮箱';
          this.editValue = this.userInfo.email || '';
          break;
        case 'description':
          this.editTitle = '商家简介';
          this.editValue = this.userInfo.shopDescription || '';
          break;
        case 'password':
          this.editTitle = '修改密码';
          this.editValue = '';
          break;
        default:
          return;
      }
      
      this.showEditModal = true;
    },
    
    closeEditModal() {
      this.showEditModal = false;
      this.editValue = '';
    },
    
    submitEdit() {
      if (this.editType === 'password') {
        if (!this.editValue || this.editValue.length < 6) {
          uni.showToast({ title: '密码至少6位', icon: 'none' });
          return;
        }
        this.updatePassword();
      } else {
        if (!this.editValue.trim()) {
          uni.showToast({ title: '请填写' + this.editTitle, icon: 'none' });
          return;
        }
        this.updateProfile();
      }
    },
    
    updateProfile() {
      const updateData = {};
      
      switch(this.editType) {
        case 'shopName':
          updateData.shopName = this.editValue.trim();
          break;
        case 'phone':
          updateData.phone = this.editValue.trim();
          break;
        case 'email':
          updateData.email = this.editValue.trim();
          break;
        case 'description':
          updateData.shopDescription = this.editValue.trim();
          break;
      }
      
      uni.request({
        url: 'http://localhost:8080/api/users/merchant/info',
        method: 'PUT',
        header: {
          'Authorization': 'Bearer ' + this.token,
          'Content-Type': 'application/json'
        },
        data: updateData,
        success: (res) => {
          if (res.statusCode === 200 && res.data) {
            // 更新本地存储
            const updatedUserInfo = { ...this.userInfo, ...updateData };
            uni.setStorageSync('userInfo', updatedUserInfo);
            this.userInfo = updatedUserInfo;
            
            uni.showToast({ title: '修改成功', icon: 'success' });
            this.closeEditModal();
          } else {
            uni.showToast({ title: res.data?.message || '修改失败', icon: 'none' });
          }
        },
        fail: () => {
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    },
    
    updatePassword() {
      uni.request({
        url: 'http://localhost:8080/api/users/profile',
        method: 'PUT',
        header: {
          'Authorization': 'Bearer ' + this.token,
          'Content-Type': 'application/json'
        },
        data: {
          password: this.editValue
        },
        success: (res) => {
          if (res.statusCode === 200 && res.data) {
            uni.showToast({ title: '密码修改成功，请重新登录', icon: 'success' });
            this.closeEditModal();
            
            setTimeout(() => {
              uni.clearStorageSync();
              uni.reLaunch({ url: '/pages/login-register/login-register' });
            }, 1500);
          } else {
            uni.showToast({ title: res.data?.message || '修改失败', icon: 'none' });
          }
        },
        fail: () => {
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    },
    
    showLogoutConfirm() {
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
  background-color: #f5f5f5;
  padding: 20rpx;
}

.setting-list {
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}

.setting-section {
  border-bottom: 20rpx solid #f5f5f5;
}

.section-title {
  padding: 30rpx 30rpx 20rpx;
  font-size: 28rpx;
  color: #999;
  background-color: #fff;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.setting-item:active {
  background-color: #f9f9f9;
}

.item-label {
  font-size: 30rpx;
  color: #333;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.item-value {
  font-size: 28rpx;
  color: #999;
  max-width: 400rpx;
  text-align: right;
}

.item-value.description {
  max-width: 500rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-arrow {
  font-size: 32rpx;
  color: #ccc;
}

.version-info {
  padding: 40rpx;
  text-align: center;
}

.version-text {
  font-size: 24rpx;
  color: #ccc;
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
  border-radius: 30rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #eee;
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
}

.modal-body {
  padding: 40rpx 30rpx;
}

.edit-input {
  width: 100%;
  padding: 24rpx;
  border: 1px solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.edit-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 24rpx;
  border: 1px solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
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
  background-color: #f5f5f5;
  color: #666;
}

.confirm-btn {
  background-color: #f0e68c;
  color: #333;
}
</style>