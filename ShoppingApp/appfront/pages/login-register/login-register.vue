<template>
  <view class="container">
    <!-- 背景装饰 -->
    <view class="bg-decoration">
      <view class="circle circle-1"></view>
      <view class="circle circle-2"></view>
      <view class="circle circle-3"></view>
    </view>

    <!-- Logo 区域 -->
    <view class="logo-area">
      <view class="logo-icon">🏨</view>
      <text class="logo-text">旅游资源管理系统</text>
      <text class="logo-slogan">探索世界，从心开始</text>
    </view>

    <!-- 角色选择标签页 -->
    <view class="role-tabs">
      <view 
        class="tab-item" 
        :class="{ active: userRole === 'user' }" 
        @click="switchRole('user')"
      >
        <text class="tab-icon">👤</text>
        <text>用户登录</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: userRole === 'merchant' }" 
        @click="switchRole('merchant')"
      >
        <text class="tab-icon">🏪</text>
        <text>商家登录</text>
      </view>
    </view>

    <!-- 登录表单 -->
    <view class="form-card" v-if="isLogin">
      <view class="form-header">
        <text class="form-title">欢迎回来</text>
        <text class="form-subtitle">请登录您的账号</text>
      </view>
      
      <view class="form-body">
        <view class="input-group">
          <view class="input-icon">👤</view>
          <input class="input" placeholder="用户名 / 手机号" v-model="username" />
        </view>
        
        <view class="input-group">
          <view class="input-icon">🔒</view>
          <input class="input" placeholder="密码" password="true" v-model="password" />
        </view>
        
        <view class="options-row">
          <label class="checkbox">
            <checkbox value="remember" :checked="rememberPwd" @click="rememberPwd = !rememberPwd" />
            <text>记住密码</text>
          </label>
          <text class="forgot-pwd">忘记密码？</text>
        </view>
        
        <button class="login-btn" @click="login">登录</button>
        
        <view class="register-tip">
          <text>还没有账号？</text>
          <text class="register-link" @click="toggleForm">立即注册</text>
        </view>
      </view>
    </view>

    <!-- 注册表单 -->
    <view class="form-card" v-else>
      <view class="form-header">
        <text class="form-title">创建账号</text>
        <text class="form-subtitle">{{ userRole === 'merchant' ? '加入商家平台' : '开启旅游之旅' }}</text>
      </view>
      
      <view class="form-body">
        <view class="input-group">
          <view class="input-icon">👤</view>
          <input class="input" placeholder="用户名" v-model="username" />
        </view>
        
        <view class="input-group">
          <view class="input-icon">🔒</view>
          <input class="input" placeholder="密码" password="true" v-model="password" />
        </view>
        
        <view class="input-group">
          <view class="input-icon">✓</view>
          <input class="input" placeholder="确认密码" password="true" v-model="confirmPassword" />
        </view>
        
        <!-- 商家注册时额外填写的信息 -->
        <view v-if="userRole === 'merchant'">
          <view class="input-group">
            <view class="input-icon">🏢</view>
            <input class="input" placeholder="商家名称" v-model="shopName" />
          </view>
          
          <view class="input-group">
            <view class="input-icon">📞</view>
            <input class="input" placeholder="联系电话" v-model="phone" type="number" />
          </view>
          
          <view class="input-group">
            <view class="input-icon">📧</view>
            <input class="input" placeholder="电子邮箱" v-model="email" type="email" />
          </view>
          
          <view class="input-group textarea-group">
            <view class="input-icon">📝</view>
            <textarea class="input textarea" placeholder="商家简介（选填）" v-model="shopDescription" maxlength="200" />
          </view>
        </view>
        
        <button class="register-btn" @click="register">注册</button>
        
        <view class="register-tip">
          <text>已有账号？</text>
          <text class="register-link" @click="toggleForm">返回登录</text>
        </view>
      </view>
    </view>

    <!-- 申诉弹窗 -->
    <view class="modal-mask" v-if="showAppealModal" @click="closeAppealModal">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">📢 联系管理员</text>
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
            <view class="info-item" v-if="appealShopName">
              <text class="info-label">商家名称：</text>
              <text class="info-value">{{ appealShopName }}</text>
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
            <input class="input" v-model="contactInfo" placeholder="请输入您的联系电话或邮箱" />
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
      // 表单数据
      username: '',
      password: '',
      confirmPassword: '',
      shopName: '',
      phone: '',
      email: '',
      shopDescription: '',
      rememberPwd: false,
      
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
  onLoad() {
    // 检查是否有记住的密码
    const savedUsername = uni.getStorageSync('savedUsername');
    const savedPassword = uni.getStorageSync('savedPassword');
    if (savedUsername && savedPassword) {
      this.username = savedUsername;
      this.password = savedPassword;
      this.rememberPwd = true;
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
    
    // 登录
    login() {
      if (!this.username || !this.password) {
        uni.showToast({ title: '请输入账号密码', icon: 'none' });
        return;
      }
      
      // 记住密码
      if (this.rememberPwd) {
        uni.setStorageSync('savedUsername', this.username);
        uni.setStorageSync('savedPassword', this.password);
      } else {
        uni.removeStorageSync('savedUsername');
        uni.removeStorageSync('savedPassword');
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
          
          if (res.statusCode === 200 && res.data) {
            const userData = res.data;
            
            // 角色验证
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
              const status = (userData.status || 'NORMAL').toUpperCase();
              
              // 保存用户信息到本地存储（用于后续页面显示）
              uni.setStorageSync('token', userData.token);
              uni.setStorageSync('loginUsername', this.username);
              uni.setStorageSync('userRole', userData.role);
              uni.setStorageSync('userInfo', {
                id: userData.id,
                name: this.username,
                username: userData.username || this.username,
                role: userData.role,
                status: userData.status || 'NORMAL',
                shopName: userData.shopName || '',
                phone: userData.phone || '',
                email: userData.email || '',
                avatar: userData.avatar || '',
                bio: userData.bio || ''
              });
              
              // 待审核状态 - 跳转到等待审核页
              if (status === 'PENDING') {
                uni.showToast({ title: '登录成功', icon: 'success' });
                setTimeout(() => {
                  uni.redirectTo({ 
                    url: '/pages/merchant/pending?username=' + encodeURIComponent(userData.username) + '&shopName=' + encodeURIComponent(userData.shopName || '')
                  });
                }, 500);
                return;
              }
              
              // 已拒绝状态 - 跳转到拒绝提示页
              if (status === 'REJECTED') {
                uni.showToast({ title: '登录成功', icon: 'success' });
                setTimeout(() => {
                  uni.redirectTo({ 
                    url: '/pages/merchant/rejected?username=' + encodeURIComponent(userData.username) + '&shopName=' + encodeURIComponent(userData.shopName || '')
                  });
                }, 500);
                return;
              }
              
              // 已禁用状态 - 跳转到禁用提示页
              if (status === 'BANNED') {
                uni.showToast({ title: '登录成功', icon: 'success' });
                setTimeout(() => {
                  uni.redirectTo({ 
                    url: '/pages/merchant/banned?username=' + encodeURIComponent(userData.username) + '&shopName=' + encodeURIComponent(userData.shopName || '')
                  });
                }, 500);
                return;
              }
              
              // 正常状态 - 进入商家主页
              if (status === 'NORMAL') {
                uni.showToast({ title: '登录成功', icon: 'success' });
                setTimeout(() => {
                  uni.reLaunch({ url: '/pages/merchant/home' });
                }, 500);
                return;
              }
              
              // 其他未知状态
              uni.showToast({ 
                title: '账号状态异常，请联系管理员', 
                icon: 'none'
              });
              return;
            }
            
            // ✅ 保存完整用户信息
            uni.setStorageSync('token', userData.token);
            uni.setStorageSync('loginUsername', this.username);
            uni.setStorageSync('userRole', userData.role);
            uni.setStorageSync('userInfo', {
              id: userData.id,
              name: this.username,
              username: userData.username || this.username,
              role: userData.role,
              status: userData.status || 'NORMAL',
              shopName: userData.shopName || '',
              phone: userData.phone || '',
              email: userData.email || '',
              avatar: userData.avatar || '',
              bio: userData.bio || ''
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
              title: res.data?.message || '用户名或密码错误',
              icon: 'none'
            });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
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
      if (this.password.length < 6) {
        uni.showToast({ title: '密码至少6位', icon: 'none' });
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
          
          if (res.statusCode === 200 && res.data.success) {
            if (this.userRole === 'merchant') {
              uni.showModal({
                title: '注册成功',
                content: '商家注册成功，请等待管理员审核。审核通过后即可登录使用。',
                confirmText: '知道了',
                showCancel: false
              });
            } else {
              uni.showToast({ title: '注册成功', icon: 'success' });
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

/* Logo 区域 */
.logo-area {
  text-align: center;
  margin-bottom: 60rpx;
  z-index: 1;
}

.logo-icon {
  font-size: 80rpx;
  width: 140rpx;
  height: 140rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 70rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20rpx;
  backdrop-filter: blur(10px);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.logo-text {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 12rpx;
}

.logo-slogan {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 角色选择标签页 */
.role-tabs {
  display: flex;
  width: 100%;
  max-width: 600rpx;
  margin-bottom: 40rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 60rpx;
  padding: 8rpx;
  backdrop-filter: blur(10px);
  z-index: 1;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
  border-radius: 56rpx;
  transition: all 0.3s ease;
}

.tab-item.active {
  background: #fff;
  color: #667eea;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.tab-icon {
  font-size: 32rpx;
}

/* 表单卡片 */
.form-card {
  width: 100%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 48rpx;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.2);
  z-index: 1;
}

.form-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 48rpx 40rpx;
  text-align: center;
}

.form-title {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 12rpx;
}

.form-subtitle {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.form-body {
  padding: 48rpx 40rpx;
}

/* 输入框组 */
.input-group {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 60rpx;
  margin-bottom: 24rpx;
  padding: 8rpx 24rpx;
  transition: all 0.3s ease;
}

.input-group:focus-within {
  background: #fff;
  box-shadow: 0 0 0 2rpx #667eea;
}

.input-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
  color: #999;
}

.input {
  flex: 1;
  padding: 24rpx 0;
  font-size: 28rpx;
  background: transparent;
}

.textarea-group {
  align-items: flex-start;
  padding-top: 20rpx;
}

.textarea {
  min-height: 120rpx;
  line-height: 1.5;
}

/* 选项行 */
.options-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
  padding: 0 8rpx;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 24rpx;
  color: #666;
}

.checkbox checkbox {
  transform: scale(0.8);
}

.forgot-pwd {
  font-size: 24rpx;
  color: #667eea;
}

/* 按钮 */
.login-btn, .register-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 60rpx;
  padding: 28rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 20rpx rgba(102, 126, 234, 0.3);
}

.login-btn:active, .register-btn:active {
  transform: scale(0.98);
}

.register-tip {
  text-align: center;
  font-size: 26rpx;
  color: #999;
}

.register-link {
  color: #667eea;
  margin-left: 12rpx;
  font-weight: 500;
}

/* 申诉弹窗 */
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
  border-radius: 48rpx;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1px solid #eee;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
}

.modal-body {
  flex: 1;
  padding: 32rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.modal-footer {
  display: flex;
  padding: 24rpx 32rpx;
  border-top: 1px solid #eee;
  gap: 20rpx;
  padding-bottom: 40rpx;
}

.cancel-btn, .submit-btn {
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

.form-item {
  margin-bottom: 32rpx;
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
  padding: 24rpx;
  border: 1px solid #e0e0e0;
  border-radius: 60rpx;
  font-size: 28rpx;
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.picker-arrow {
  font-size: 36rpx;
  color: #ccc;
}

.appeal-textarea {
  width: 100%;
  min-height: 180rpx;
  border: 1px solid #e0e0e0;
  border-radius: 24rpx;
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
  padding: 24rpx;
  border-radius: 24rpx;
  margin-bottom: 32rpx;
}

.info-item {
  display: flex;
  margin-bottom: 16rpx;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 160rpx;
  font-size: 26rpx;
  color: #666;
}

.info-value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
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
  background: linear-gradient(135deg, #fff9e6, #fff5d9);
  border-radius: 20rpx;
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
  line-height: 1.4;
}
</style>