<template>
  <view class="container">
    <view v-if="isLogin">
      <view class="form">
        <input class="input" placeholder="用户名" v-model="username" />
        <input class="input" placeholder="密码" password="true" v-model="password" />
        <view class="button-group">
          <button class="button" @click="login">登录</button>
          <button class="button" @click="toggleForm">注册</button>
        </view>
      </view>
    </view>
    <view v-else>
      <view class="form">
        <input class="input" placeholder="用户名" v-model="username" />
        <input class="input" placeholder="密码" password="true" v-model="password" />
        <input class="input" placeholder="确认密码" password="true" v-model="confirmPassword" />
        <view class="button-group">
          <button class="button" @click="register">注册</button>
          <button class="button" @click="toggleForm">返回登录</button>
        </view>
      </view>
    </view>
  </view>
    <RouteFloat/>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      password: '',
      confirmPassword: '',
      isLogin: true
    };
  },
  methods: {
    toggleForm() {
      this.isLogin = !this.isLogin;
      this.username = '';
      this.password = '';
      this.confirmPassword = '';
    },
    login() {
      if (!this.username || !this.password) {
        uni.showToast({ title: '请输入账号密码', icon: 'none' })
        return
      }
    
      uni.request({
        url: 'http://localhost:8080/api/users/login',
        method: 'POST',
        data: {
          username: this.username,
          password: this.password
        },
        success: (res) => {
          if (res.statusCode === 200 && res.data) {
            // 保存 token 和用户名
            uni.setStorageSync('token', res.data.token || 'login');
            
            // ======================
            // 🔥 🔥 🔥 必须加这一行！！！
            // ======================
            uni.setStorageSync('loginUsername', this.username);
    
            uni.setStorageSync('userInfo', {
              name: this.username,
              id: '',
              desc: '用户'
            });
    
            uni.showToast({
              title: '登录成功',
              icon: 'success',
              complete: () => {
                setTimeout(() => {
                  uni.switchTab({ url: '/pages/home/home' });
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
        fail: () => {
          uni.showToast({ title: '网络异常', icon: 'none' });
        }
      });
    },

    register() {
      if (!this.username || !this.password) {
        uni.showToast({ title: '请完善信息', icon: 'none' })
        return
      }
      if (this.password !== this.confirmPassword) {
        uni.showToast({ title: '两次密码不一致', icon: 'none' });
        return;
      }

      uni.request({
        url: 'http://localhost:8080/api/users/register',
        method: 'POST',
        data: {
          username: this.username,
          password: this.password
        },
        success: (res) => {
          if (res.statusCode === 200) {
            uni.showToast({ title: '注册成功', icon: 'success' });
            this.toggleForm();
          } else {
            uni.showToast({ title: res.data?.message || '注册失败', icon: 'none' });
          }
        },
        fail: () => {
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
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #fffde7;
  padding: 20px;
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
</style>