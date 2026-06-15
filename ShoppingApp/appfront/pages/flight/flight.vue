<template>
  <view class="redirect-view">
    <view class="loading">加载中...</view>
  </view>
</template>

<script>
export default {
  onLoad() {
    // 重定向到机票搜索页面
    uni.redirectTo({
      url: '/pages/flight/search-flight',
      fail: (err) => {
        console.error('跳转失败：', err);
        uni.showToast({
          title: '页面跳转失败',
          icon: 'none'
        });
      }
    });
  }
};
</script>

<style scoped>
.redirect-view {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
.loading {
  font-size: 32rpx;
  color: #999;
}
</style>