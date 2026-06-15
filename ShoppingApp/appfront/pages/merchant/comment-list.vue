<template>
  <view class="container">
    <!-- 统计卡片 -->
    <view class="stats-card">
      <view class="stat-item" @click="filterByRating('all')">
        <text class="stat-number">{{ totalCount }}</text>
        <text class="stat-label">全部评价</text>
      </view>
      <view class="stat-item" @click="filterByRating(5)">
        <text class="stat-number">{{ ratingCounts[5] || 0 }}</text>
        <text class="stat-label">5星好评</text>
      </view>
      <view class="stat-item" @click="filterByRating(4)">
        <text class="stat-number">{{ ratingCounts[4] || 0 }}</text>
        <text class="stat-label">4星</text>
      </view>
      <view class="stat-item" @click="filterByRating(3)">
        <text class="stat-number">{{ ratingCounts[3] || 0 }}</text>
        <text class="stat-label">3星</text>
      </view>
      <view class="stat-item" @click="filterByRating(2)">
        <text class="stat-number">{{ ratingCounts[2] || 0 }}</text>
        <text class="stat-label">2星</text>
      </view>
      <view class="stat-item" @click="filterByRating(1)">
        <text class="stat-number">{{ ratingCounts[1] || 0 }}</text>
        <text class="stat-label">1星</text>
      </view>
    </view>

    <!-- 酒店筛选 -->
    <view class="hotel-filter" v-if="hotelList.length > 0">
      <picker mode="selector" :range="hotelNames" @change="onHotelChange">
        <view class="filter-select">
          <text>{{ selectedHotelName || '全部酒店' }}</text>
          <text class="arrow">▼</text>
        </view>
      </picker>
    </view>

    <!-- 评价列表 -->
    <view class="comment-list" v-if="commentList.length > 0">
      <view class="comment-card" v-for="comment in commentList" :key="comment.id">
        <!-- 订单信息 -->
        <view class="order-info">
          <text class="hotel-name">{{ comment.hotelName || getHotelName(comment.hotelId) }}</text>
          <text class="order-time">{{ formatDate(comment.createTime) }}</text>
        </view>
        
        <!-- 评分（兼容 rating 和 score 字段） -->
        <view class="rating-row">
          <view class="stars">
            <text 
              class="star" 
              v-for="n in 5" 
              :key="n"
              :class="{ active: n <= (comment.rating || comment.score || 0) }"
            >★</text>
          </view>
          <text class="rating-score">{{ comment.rating || comment.score || 0 }}分</text>
        </view>
        
        <!-- 评价内容 -->
        <view class="comment-content">
          <text>{{ comment.content || comment.comment || '用户没有填写评价内容' }}</text>
        </view>
        
        <!-- 用户信息 -->
        <view class="user-info">
          <text class="user-name">用户：{{ comment.username || '匿名用户' }}</text>
          <text class="order-id">订单号：{{ comment.orderId }}</text>
        </view>
        
        <!-- 回复区域 -->
        <view class="reply-area" v-if="comment.reply && comment.reply.trim()">
          <view class="reply-header">
            <text class="reply-label">商家回复：</text>
          </view>
          <text class="reply-content">{{ comment.reply }}</text>
          <view class="reply-action" style="margin-top: 15rpx;">
            <button class="edit-reply-btn" @click="showEditReply(comment)">修改回复</button>
          </view>
        </view>
        
        <!-- 回复输入框（未回复时） -->
        <view class="reply-input-area" v-else>
          <input 
            class="reply-input" 
            :value="replyContents[comment.id]"
            @input="(e) => { replyContents[comment.id] = e.detail.value }"
            placeholder="回复用户..."
          />
          <button 
            class="reply-btn" 
            @click="submitReply(comment)"
            :disabled="!replyContents[comment.id] || submittingId === comment.id"
          >{{ submittingId === comment.id ? '提交中...' : '回复' }}</button>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-box" v-else>
      <text class="empty-icon">💬</text>
      <text class="empty-text">暂无评价</text>
    </view>

    <!-- 修改回复弹窗 -->
    <view class="dialog-mask" v-if="showEditDialog" @click="closeEditDialog">
      <view class="dialog-container" @click.stop>
        <view class="dialog-header">
          <text class="dialog-title">修改回复</text>
          <text class="close-btn" @click="closeEditDialog">✕</text>
        </view>
        <view class="dialog-content">
          <textarea 
            class="edit-reason-input" 
            v-model="editReplyContent"
            placeholder="请输入新的回复内容"
            maxlength="500"
            auto-height
          />
          <button class="btn-submit" @click="updateReply" :disabled="updatingReply">确认修改</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      commentList: [],
      hotelList: [],
      hotelNames: [],
      selectedHotelId: null,
      selectedHotelName: '全部酒店',
      selectedRating: null, // all, 1,2,3,4,5
      totalCount: 0,
      ratingCounts: {},
      merchantId: null,
      
      // 回复相关
      replyContents: {},
      submittingId: null,  // 正在提交回复的评论ID
      
      // 编辑回复弹窗
      showEditDialog: false,
      editCommentId: null,
      editReplyContent: '',
      updatingReply: false
    };
  },
  
  onShow() {
    this.loadMerchantInfo();
  },
  
  methods: {
    // 加载商家信息
    loadMerchantInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo');
        console.log('商家信息：', userInfo);
        if (userInfo && userInfo.id) {
          this.merchantId = userInfo.id;
          this.loadMerchantHotels();
        } else {
          uni.showToast({ title: '请先登录', icon: 'none' });
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login-register/login-register' });
          }, 1500);
        }
      } catch (e) {
        console.error('读取商家信息失败', e);
      }
    },
    
    // 获取商家酒店列表
    loadMerchantHotels() {
      uni.request({
        url: `http://localhost:8080/api/merchant/hotels?merchantId=${this.merchantId}`,
        method: 'GET',
        success: (res) => {
          console.log('酒店列表返回：', res.data);
          if (res.data && res.data.code === 200) {
            this.hotelList = res.data.data || [];
            this.hotelNames = ['全部酒店', ...this.hotelList.map(h => h.name)];
            this.hotelMap = {};
            this.hotelList.forEach(h => {
              this.hotelMap[h.id] = h.name;
            });
            this.loadComments();
          } else {
            console.log('获取酒店列表失败或没有酒店');
            this.loadComments(); // 仍然尝试加载评价
          }
        },
        fail: (err) => {
          console.error('获取酒店列表失败', err);
          this.loadComments();
        }
      });
    },
    
    // 获取酒店名称
    getHotelName(hotelId) {
      return this.hotelMap ? (this.hotelMap[hotelId] || '未知酒店') : '酒店';
    },
    
    // 加载评价列表
    loadComments() {
      if (!this.merchantId) {
        console.log('商家ID为空');
        return;
      }
      
      uni.showLoading({ title: '加载中...' });
      
      let url = `http://localhost:8080/api/hotel-comments/merchant?merchantId=${this.merchantId}`;
      
      if (this.selectedHotelId) {
        url += `&hotelId=${this.selectedHotelId}`;
      }
      
      if (this.selectedRating && this.selectedRating !== 'all') {
        url += `&rating=${this.selectedRating}`;
      }
      
      console.log('请求URL：', url);
      
      uni.request({
        url: url,
        method: 'GET',
        success: (res) => {
          uni.hideLoading();
          console.log('评价列表返回：', res.data);
          
          if (res.data && res.data.code === 200) {
            let comments = res.data.data || [];
            console.log('评价数量：', comments.length);
            
            if (comments.length > 0) {
              console.log('第一条评价数据：', comments[0]);
            }
            
            this.commentList = comments;
            this.calculateStats();
          } else {
            console.log('返回code不是200：', res.data);
            this.commentList = [];
            this.totalCount = 0;
            this.ratingCounts = {};
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('获取评价失败：', err);
          uni.showToast({ title: '获取评价失败', icon: 'none' });
          this.commentList = [];
        }
      });
    },
    
    // 计算统计数据
    calculateStats() {
      this.totalCount = this.commentList.length;
      this.ratingCounts = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
      
      this.commentList.forEach(comment => {
        const rating = comment.rating || comment.score;
        if (rating >= 1 && rating <= 5) {
          this.ratingCounts[rating]++;
        }
      });
      
      console.log('统计结果：', this.ratingCounts);
    },
    
    // 按评分筛选
    filterByRating(rating) {
      console.log('筛选评分：', rating);
      this.selectedRating = rating;
      this.loadComments();
    },
    
    // 切换酒店
    onHotelChange(e) {
      const index = e.detail.value;
      console.log('选择酒店索引：', index);
      if (index === 0) {
        this.selectedHotelId = null;
        this.selectedHotelName = '全部酒店';
      } else {
        const hotel = this.hotelList[index - 1];
        this.selectedHotelId = hotel.id;
        this.selectedHotelName = hotel.name;
      }
      console.log('选中酒店ID：', this.selectedHotelId);
      this.loadComments();
    },
    
    // 提交回复
    submitReply(comment) {
      // 确保 comment 对象有正确的 id
      const commentId = comment.id || comment.commentId;
      if (!commentId) {
        console.error('评论ID不存在：', comment);
        uni.showToast({ title: '评论ID无效', icon: 'none' });
        return;
      }
      
      const replyContent = this.replyContents[commentId];
      if (!replyContent || !replyContent.trim()) {
        uni.showToast({ title: '请输入回复内容', icon: 'none' });
        return;
      }
      
      this.submittingId = commentId;
      uni.showLoading({ title: '提交中...' });
      
      console.log(`回复评论 ${commentId}，内容：`, replyContent.trim());
      
      uni.request({
        url: `http://localhost:8080/api/hotel-comments/${commentId}/reply`,
        method: 'POST',
        header: {
          'Content-Type': 'application/json'
        },
        data: {
          reply: replyContent.trim()
        },
        success: (res) => {
          uni.hideLoading();
          console.log('回复结果：', res.data);
          
          if (res.data && (res.data.code === 200 || res.data.success)) {
            uni.showToast({ title: '回复成功', icon: 'success' });
            
            // 更新本地数据
            comment.reply = replyContent.trim();
            this.$set(this.replyContents, commentId, '');
            
            // 重新加载列表以获取最新数据
            setTimeout(() => {
              this.loadComments();
            }, 500);
          } else {
            const errorMsg = res.data?.message || res.data?.msg || '回复失败';
            uni.showToast({ title: errorMsg, icon: 'none' });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('回复失败：', err);
          uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' });
        },
        complete: () => {
          this.submittingId = null;
        }
      });
    },
    
    // 显示编辑回复弹窗
    showEditReply(comment) {
      const commentId = comment.id || comment.commentId;
      this.editCommentId = commentId;
      this.editReplyContent = comment.reply || '';
      this.showEditDialog = true;
    },
    
    // 关闭编辑弹窗
    closeEditDialog() {
      this.showEditDialog = false;
      this.editCommentId = null;
      this.editReplyContent = '';
    },
    
    // 更新回复
    updateReply() {
      if (!this.editReplyContent.trim()) {
        uni.showToast({ title: '请输入回复内容', icon: 'none' });
        return;
      }
      
      this.updatingReply = true;
      uni.showLoading({ title: '更新中...' });
      
      console.log(`更新回复 ${this.editCommentId}，内容：`, this.editReplyContent.trim());
      
      uni.request({
        url: `http://localhost:8080/api/hotel-comments/${this.editCommentId}/reply`,
        method: 'PUT',
        header: {
          'Content-Type': 'application/json'
        },
        data: {
          reply: this.editReplyContent.trim()
        },
        success: (res) => {
          uni.hideLoading();
          console.log('更新回复结果：', res.data);
          
          if (res.data && (res.data.code === 200 || res.data.success)) {
            uni.showToast({ title: '修改成功', icon: 'success' });
            
            // 更新本地数据
            const comment = this.commentList.find(c => (c.id || c.commentId) === this.editCommentId);
            if (comment) {
              comment.reply = this.editReplyContent.trim();
            }
            this.closeEditDialog();
          } else {
            const errorMsg = res.data?.message || res.data?.msg || '修改失败';
            uni.showToast({ title: errorMsg, icon: 'none' });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('修改回复失败：', err);
          uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' });
        },
        complete: () => {
          this.updatingReply = false;
        }
      });
    },
    
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return '--';
      try {
        const date = new Date(dateStr);
        if (isNaN(date.getTime())) return dateStr;
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hour = String(date.getHours()).padStart(2, '0');
        const minute = String(date.getMinutes()).padStart(2, '0');
        return `${year}-${month}-${day} ${hour}:${minute}`;
      } catch (e) {
        return dateStr;
      }
    }
  }
};
</script>

<style scoped>
.container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

/* 统计卡片 */
.stats-card {
  display: flex;
  flex-wrap: wrap;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.stat-item {
  flex: 1;
  min-width: 16%;
  text-align: center;
  padding: 15rpx 0;
  border-radius: 12rpx;
  margin: 5rpx;
}

.stat-number {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  display: block;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

/* 酒店筛选 */
.hotel-filter {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.filter-select {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
}

.arrow {
  font-size: 24rpx;
  color: #999;
}

/* 评价列表 */
.comment-list {
  margin-bottom: 20rpx;
}

.comment-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 15rpx;
  border-bottom: 1rpx solid #eee;
}

.hotel-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.order-time {
  font-size: 24rpx;
  color: #999;
}

.rating-row {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.stars {
  margin-right: 15rpx;
}

.star {
  font-size: 32rpx;
  color: #ddd;
  margin-right: 5rpx;
}

.star.active {
  color: #ffbc36;
}

.rating-score {
  font-size: 26rpx;
  color: #ffbc36;
}

.comment-content {
  background-color: #f9f9f9;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.user-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
  font-size: 24rpx;
  color: #999;
}

/* 回复区域 */
.reply-area {
  background-color: #f0f7ff;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-top: 15rpx;
}

.reply-header {
  margin-bottom: 10rpx;
}

.reply-label {
  font-size: 26rpx;
  color: #1677ff;
}

.reply-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

.reply-input-area {
  display: flex;
  gap: 15rpx;
  margin-top: 20rpx;
}

.reply-input {
  flex: 1;
  height: 70rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
}

.reply-btn {
  width: 140rpx;
  height: 70rpx;
  background-color: #1677ff;
  color: #fff;
  border-radius: 8rpx;
  font-size: 26rpx;
  line-height: 70rpx;
  padding: 0;
}

.reply-btn[disabled] {
  background-color: #ccc;
  color: #666;
}

.reply-action {
  margin-top: 20rpx;
}

.edit-reply-btn {
  background-color: #fff;
  color: #1677ff;
  border: 1rpx solid #1677ff;
  border-radius: 8rpx;
  font-size: 26rpx;
  height: 70rpx;
  line-height: 70rpx;
}

/* 空状态 */
.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
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

/* 弹窗样式 */
.dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-container {
  width: 600rpx;
  background: #fff;
  border-radius: 30rpx;
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.dialog-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  font-size: 40rpx;
  color: #999;
  padding: 10rpx;
}

.dialog-content {
  padding: 30rpx;
}

.edit-reason-input {
  width: 100%;
  min-height: 150rpx;
  border: 1rpx solid #ddd;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  margin-bottom: 30rpx;
}

.btn-submit {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
}
</style>