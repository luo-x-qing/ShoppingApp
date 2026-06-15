<template>
  <view>
    <router-view></router-view>
    <RouteFloat/>
  </view>
</template>

<script>
import RouteFloat from '@/components/RouteFloat.vue';

export default {
  components: { RouteFloat },
  onLaunch() {
    this.checkLogin();
  },
  onShow() {
    this.checkLogin();
  },
  onHide() {},
  methods: {
    checkLogin() {
      const token = uni.getStorageSync('token');
      if (!token) {
        const pages = getCurrentPages();
        if (pages.length > 0) {
          const route = pages[pages.length - 1].route;
          if (route === 'pages/login-register/login-register') {
            return;
          }
        }
        uni.reLaunch({ url: '/pages/login-register/login-register' });
      }
    }
  }
};
</script>

<style></style>