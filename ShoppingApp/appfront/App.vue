<template>
  <view class="container">
    <router-view></router-view>
  </view>
</template>

<script>
export default {
  created() {
    const token = uni.getStorageSync('token');
    if (!token) {
      uni.redirectTo({
        url: '/pages/login-register/login-register'
      });
    }
  }
};
</script>

<style>

</style>