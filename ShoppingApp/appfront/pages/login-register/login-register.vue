<template>
  <view class="container">
    <!-- 角色选择标签页 -->
    <view class="role-tabs">
      <view 
        class="tab-item" 
        :class="{ active: userRole === 'user' }" 
        @click="switchRole('user')"
      >
        用户登录
      </view>
      <view 
        class="tab-item" 
        :class="{ active: userRole === 'merchant' }" 
        @click="switchRole('merchant')"
      >
        商家登录
      </view>
    </view>

    <!-- 登录表单 -->
    <view class="form" v-if="isLogin">
      <input class="input" placeholder="用户名" v-model="username" />
      <input class="input" placeholder="密码" password="true" v-model="password" />
      
      <view class="button-group">
        <button class="button" @click="login">登录</button>
        <button class="button" @click="toggleForm">{{ isLogin ? '注册' : '返回登录' }}</button>
      </view>
    </view>

    <!-- 注册表单 -->
    <view class="form" v-else>
      <input class="input" placeholder="用户名" v-model="username" />
      <input class="input" placeholder="密码" password="true" v-model="password" />
      <input class="input" placeholder="确认密码" password="true" v-model="confirmPassword" />
      
      <!-- 商家注册时额外填写的信息 -->
      <view v-if="userRole === 'merchant'">
        <input class="input" placeholder="商家名称" v-model="shopName" />
        <input class="input" placeholder="联系电话" v-model="phone" />
        <input class="input" placeholder="电子邮箱" v-model="email" />
        <input class="input" placeholder="商家简介" v-model="shopDescription" />
      </view>
      
      <view class="button-group">
        <button class="button" @click="register">注册</button>
        <button class="button" @click="toggleForm">返回登录</button>
      </view>
    </view>

    <!-- 申诉弹窗 -->
    <view class="modal-mask" v-if="showAppealModal" @click="closeAppealModal">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">联系管理员</text>
          <text class="modal-close" @click="closeAppealModal">×</text>
        </view>
        <scroll-view class="modal-body" scroll-y>
          <view class="appeal-info">
            <view class="info-item">
              <text class="info-label">当前状态：</text>
              <text class="info-value" :class="appealStatusClass">{{ appealStatusText }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">用户名：</text>
              <text class="info-value">{{ appealUsername }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">商家名称：</text>
              <text class="info-value">{{ appealShopName || '无' }}</text>
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
          
          <view class="contact-tips">
            <text class="tips-icon">📞</text>
            <text class="tips-text">也可直接联系管理员：admin@example.com | 客服电话：400-123-4567</text>
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
      // 表单数据
      username: '',
      password: '',
      confirmPassword: '',
      shopName: '',
      phone: '',
      email: '',
      shopDescription: '',
      
      // 界面状态
      isLogin: true,
      userRole: 'user',
      
      // 申诉相关
      showAppealModal: false,
      appealUsername: '',
      appealShopName: '',
      appealStatus: '',
      appealType: '',
      appealContent: '',
      contactInfo: '',
      appealTypes: ['账号解封', '审核咨询', '信息修改', '投诉建议', '其他问题']
    };
  },
  computed: {
    appealStatusText() {
      const status = this.appealStatus;
      if (status === 'PENDING') return '待审核';
      if (status === 'REJECTED') return '已拒绝';
      if (status === 'BANNED') return '已禁用';
      return '未知状态';
    },
    appealStatusClass() {
      const status = this.appealStatus;
      if (status === 'PENDING') return 'status-pending';
      if (status === 'REJECTED') return 'status-rejected';
      if (status === 'BANNED') return 'status-banned';
      return '';
    }
  },
  methods: {
    switchRole(role) {
      this.userRole = role;
      this.clearForm();
    },
    
    toggleForm() {
      this.isLogin = !this.isLogin;
      this.clearForm();
    },
    
    clearForm() {
      this.username = '';
      this.password = '';
      this.confirmPassword = '';
      this.shopName = '';
      this.phone = '';
      this.email = '';
      this.shopDescription = '';
    },
    
    onAppealTypeChange(e) {
      this.appealType = this.appealTypes[e.detail.value];
    },
    
    openAppealModal(username, shopName, status) {
      console.log('打开申诉弹窗:', {username, shopName, status});
      this.appealUsername = username;
      this.appealShopName = shopName || '';
      this.appealStatus = status;
      this.appealType = '';
      this.appealContent = '';
      this.contactInfo = '';
      this.showAppealModal = true;
    },
    
    closeAppealModal() {
      this.showAppealModal = false;
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
          username: this.appealUsername,
          shopName: this.appealShopName,
          status: this.appealStatus,
          type: this.appealType,
          content: this.appealContent,
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
            this.closeAppealModal();
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
          this.closeAppealModal();
        }
      });
    },
    
    // 登录
    login() {
      if (!this.username || !this.password) {
        uni.showToast({ title: '请输入账号密码', icon: 'none' });
        return;
      }
      
      const loginUrl = 'http://localhost:8080/api/users/login';
      
      uni.showLoading({ title: '登录中...' });
      
      uni.request({
        url: loginUrl,
        method: 'POST',
        data: {
          username: this.username,
          password: this.password
        },
        success: (res) => {
          uni.hideLoading();
          console.log('登录返回完整数据:', JSON.stringify(res.data));
          
          if (res.statusCode === 200 && res.data) {
            const userData = res.data;
            
            console.log('用户数据字段:', Object.keys(userData));
            console.log('status值:', userData.status);
            console.log('role值:', userData.role);
            
            if (this.userRole === 'merchant' && userData.role !== 'MERCHANT') {
              uni.showToast({ title: '该账号不是商家账号', icon: 'none' });
              return;
            }
            if (this.userRole === 'user' && userData.role !== 'USER') {
              uni.showToast({ title: '该账号不是普通用户账号', icon: 'none' });
              return;
            }
            
            // 商家状态检查
            if (this.userRole === 'merchant') {
              const status = userData.status || 'NORMAL';
              const statusUpper = status.toUpperCase();
              console.log('处理后的状态:', statusUpper);
              
              if (statusUpper === 'PENDING') {
                console.log('弹出待审核提示');
                uni.showModal({
                  title: '账号待审核',
                  content: '您的商家账号正在审核中，请等待管理员审核。如需联系管理员，请点击申诉。',
                  confirmText: '申诉',
                  cancelText: '知道了',
                  success: (modalRes) => {
                    console.log('弹窗结果:', modalRes);
                    if (modalRes.confirm) {
                      this.openAppealModal(userData.username, userData.shopName, 'PENDING');
                    }
                  }
                });
                return;
              } else if (statusUpper === 'REJECTED') {
                console.log('弹出已拒绝提示');
                uni.showModal({
                  title: '审核未通过',
                  content: '您的商家账号审核未通过。如需申诉或了解详情，请联系管理员。',
                  confirmText: '申诉',
                  cancelText: '知道了',
                  success: (modalRes) => {
                    console.log('弹窗结果:', modalRes);
                    if (modalRes.confirm) {
                      this.openAppealModal(userData.username, userData.shopName, 'REJECTED');
                    }
                  }
                });
                return;
              } else if (statusUpper === 'BANNED') {
                console.log('=== 进入 BANNED 分支，准备弹窗 ===');
                uni.showModal({
                  title: '账号已禁用',
                  content: '您的商家账号已被禁用。如需申诉，请联系管理员。',
                  confirmText: '申诉',
                  cancelText: '知道了',
                  success: (modalRes) => {
                    console.log('BANNED弹窗点击结果:', modalRes);
                    if (modalRes.confirm) {
                      console.log('用户点击了申诉，打开申诉弹窗');
                      this.openAppealModal(userData.username, userData.shopName, 'BANNED');
                    }
                  },
                  fail: (err) => {
                    console.error('showModal 失败:', err);
                    uni.showToast({
                      title: '账号已禁用，请联系管理员',
                      icon: 'none',
                      duration: 3000
                    });
                  }
                });
                return;
              } else if (statusUpper !== 'NORMAL') {
                uni.showToast({ 
                  title: '账号状态异常(' + statusUpper + ')，请联系管理员', 
                  icon: 'none',
                  duration: 2000
                });
                return;
              }
            }
            
            // 保存登录信息
            uni.setStorageSync('token', userData.token);
            uni.setStorageSync('loginUsername', this.username);
            uni.setStorageSync('userRole', userData.role);
            uni.setStorageSync('userInfo', {
              id: userData.id,
              name: this.username,
              role: userData.role,
              status: userData.status || 'NORMAL',
              shopName: userData.shopName || '',
              phone: userData.phone || '',
              email: userData.email || '',
              avatar: userData.avatar || ''
            });
            
            uni.showToast({
              title: '登录成功',
              icon: 'success',
              complete: () => {
                setTimeout(() => {
                  if (userData.role === 'MERCHANT') {
                    uni.reLaunch({ url: '/pages/merchant/home' });
                  } else {
                    uni.switchTab({ url: '/pages/home/home' });
                  }
                }, 500);
              }
            });
          } else {
            uni.showToast({
              title: res.data?.message || '登录失败',
              icon: 'none'
            });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('登录请求失败:', err);
          uni.showToast({ title: '网络异常', icon: 'none' });
        }
      });
    },

    // 注册
    register() {
      if (!this.username || !this.password) {
        uni.showToast({ title: '请完善信息', icon: 'none' });
        return;
      }
      if (this.password !== this.confirmPassword) {
        uni.showToast({ title: '两次密码不一致', icon: 'none' });
        return;
      }
      
      if (this.userRole === 'merchant') {
        if (!this.shopName) {
          uni.showToast({ title: '请填写商家名称', icon: 'none' });
          return;
        }
        if (!this.phone) {
          uni.showToast({ title: '请填写联系电话', icon: 'none' });
          return;
        }
      }
      
      const registerUrl = this.userRole === 'merchant' 
        ? 'http://localhost:8080/api/users/register-merchant'
        : 'http://localhost:8080/api/users/register';
      
      const requestData = this.userRole === 'merchant' ? {
        username: this.username,
        password: this.password,
        shopName: this.shopName,
        phone: this.phone,
        email: this.email,
        shopDescription: this.shopDescription
      } : {
        username: this.username,
        password: this.password
      };
      
      uni.showLoading({ title: '注册中...' });
      
      uni.request({
        url: registerUrl,
        method: 'POST',
        data: requestData,
        success: (res) => {
          uni.hideLoading();
          console.log('注册返回:', res.data);
          
          if (res.statusCode === 200 && res.data.success) {
            if (this.userRole === 'merchant') {
              uni.showModal({
                title: '注册成功',
                content: '商家注册成功，请等待管理员审核。审核通过后即可登录使用。',
                confirmText: '知道了',
                showCancel: false
              });
            } else {
              uni.showToast({ 
                title: '注册成功', 
                icon: 'success' 
              });
            }
            this.isLogin = true;
            this.clearForm();
          } else {
            uni.showToast({ 
              title: res.data?.message || '注册失败', 
              icon: 'none' 
            });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '网络异常', icon: 'none' });
        }
      });
    }
  }
};
</script>

<style>
.container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #fffde7;
  padding: 20px;
}

.role-tabs {
  display: flex;
  width: 80%;
  margin-bottom: 30rpx;
  background-color: #fff9e6;
  border-radius: 16rpx;
  overflow: hidden;
  border: 1px solid #f0e68c;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  font-size: 28rpx;
  color: #666;
  background-color: #fff9e6;
  transition: all 0.3s;
}

.tab-item.active {
  background-color: #f0e68c;
  color: #333;
  font-weight: bold;
}

.form {
  width: 80%;
  background-color: #fff9e6;
  padding: 30rpx;
  border-radius: 10px;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
}

.input {
  margin-bottom: 20rpx;
  padding: 20rpx;
  border: 1px solid #f0e68c;
  border-radius: 8rpx;
  background-color: #fff;
  font-size: 28rpx;
}

.button-group {
  display: flex;
  justify-content: space-between;
  margin-top: 20rpx;
}

.button {
  padding: 20rpx;
  background-color: #f0e68c;
  color: #333;
  border-radius: 8rpx;
  font-size: 28rpx;
  flex: 1;
  margin: 0 6rpx;
}

/* 申诉弹窗样式 */
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
  max-height: 85vh;
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
  flex: 1;
  padding: 30rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.modal-footer {
  display: flex;
  padding: 20rpx 30rpx;
  border-top: 1px solid #eee;
  gap: 20rpx;
  padding-bottom: 40rpx;
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

.appeal-info {
  background-color: #f8f9fa;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
}

.info-item {
  display: flex;
  margin-bottom: 12rpx;
}

.info-label {
  width: 140rpx;
  font-size: 26rpx;
  color: #666;
}

.info-value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
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
}

.tips-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.tips-text {
  font-size: 24rpx;
  color: #999;
  flex: 1;
}
</style>