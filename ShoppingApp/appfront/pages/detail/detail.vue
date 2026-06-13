<template>
  <view class="container">
    <!-- 轮播图 -->
    <swiper class="swiper" indicator-dots circular autoplay>
      <swiper-item v-for="(img, i) in images" :key="i">
        <image :src="img" class="swiper-img" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <!-- 景区信息 -->
    <view class="info-card">
      <text class="name">{{ detail.name }}</text>

      <!-- 总星级展示（动态更新平均分） -->
      <view class="score-row">
        <view class="stars">
          <text class="star" v-for="n of 5" :key="n">
            {{ n <= Math.round(detail.score || 0) ? '⭐' : '☆' }}
          </text>
        </view>
        <text class="score">{{ (detail.score || 0).toFixed(1) }} 分</text>
      </view>

      <!-- 景区属性 -->
      <view class="attrs">
        <text class="attr">📍 省份：{{ detail.province }}</text>
        <text class="attr" v-if="detail.city">🏙 城市：{{ detail.city }}</text>
        <text class="attr">🏷 类型：{{ detail.type || "未分类" }}</text>
        <text class="attr">⭐ 等级：{{ detail.level || "未评级" }}</text>
        <text class="attr">🕒 开放：{{ detail.openTime || "待确认" }}</text>
        <text class="attr">🎫 门票：¥{{ detail.ticketPrice != null ? detail.ticketPrice : "待确认" }}</text>
      </view>

      <text class="desc-title">景区介绍</text>
      <text class="desc">{{ detail.description }}</text>
      <button class="add-route-btn" @click="addToRoutePlan">加入旅游线路</button>
    </view>

    <!-- 发布评论：星级选择 + 内容输入 -->
    <view class="publish-box">
      <text class="publish-title">发表评价</text>
      <!-- 选择星级 -->
      <view class="select-star">
        <text 
          v-for="n of 5" 
          :key="n" 
          class="star-item"
          @click="selectStar(n)"
        >
          {{ n <= userScore ? '⭐' : '☆' }}
        </text>
      </view>
      <textarea 
        v-model="commentContent" 
        placeholder="写下你的游玩感受..."
        maxlength="200"
      ></textarea>
      <button class="submit-btn" @click="submitComment">提交评价</button>
    </view>

    <!-- 所有用户评论列表 -->
    <view class="comment-section">
      <text class="title">游客评价</text>
      <view v-if="comments.length === 0" class="empty">暂无评价</view>
      <view class="comment-item" v-for="item in comments" :key="item.id">
        <view class="user">{{ item.userName || "匿名游客" }}</view>
        <view class="item-star">
          <text v-for="n of 5" :key="n">{{ n <= item.score ? '⭐' : '☆' }}</text>
        </view>
        <text class="content">{{ item.content }}</text>
        <text class="time">{{ item.createTime }}</text>
      </view>
    </view>

  </view>
    <RouteFloat/>
</template>

<script>
export default {
  data() {
    return {
      id: null,
      detail: {},
      images: [],
      comments: [],
      userScore: 5,
      commentContent: "",
      routePlanCount: 0
    };
  },

  onLoad(options) {
    this.id = options.id;
    this.getDetail();
    this.getComments();
    this.getRoutePlanCount();
  },

  onShow() {
    this.getRoutePlanCount();
  },

  methods: {
    getRoutePlanKey() {
      return "routePlan_" + (uni.getStorageSync("loginUsername") || "default");
    },

    getRoutePlanCount() {
      const spots = uni.getStorageSync(this.getRoutePlanKey());
      this.routePlanCount = Array.isArray(spots) ? spots.length : 0;
    },

    getDetail() {
      uni.request({
        url: `http://localhost:8080/api/attractions/${this.id}`,
        success: (res) => {
          this.detail = res.data;
          if (res.data.photo) {
            this.images = [res.data.photo.startsWith("http") ? res.data.photo : "http://localhost:8080" + res.data.photo];
          }
          if (this.images.length === 0) {
            this.images = ["/static/home/6.jpg"];
          }
        }
      });
    },

    getComments() {
      uni.request({
        url: `http://localhost:8080/api/comments/attraction/${this.id}`,
        success: (res) => {
          this.comments = res.data;
          this.calcAvgScore();
        }
      });
    },

    calcAvgScore() {
      if (this.comments.length === 0) {
        this.detail.score = 0;
        return;
      }
      const total = this.comments.reduce((sum, c) => sum + c.score, 0);
      const avg = total / this.comments.length;
      this.detail.score = avg;

      uni.request({
        url: `http://localhost:8080/api/attractions/${this.id}/score`,
        method: "PUT",
        data: { score: avg }
      });
    },

    selectStar(num) {
      this.userScore = num;
    },

    addToRoutePlan() {
      const username = uni.getStorageSync("loginUsername");
      if (!username) {
        uni.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      const key = this.getRoutePlanKey();
      let spots = uni.getStorageSync(key) || [];
      if (!Array.isArray(spots)) spots = [];
      if (spots.some(s => s.id == this.id)) {
        uni.showToast({ title: "已在规划列表中", icon: "none" });
        return;
      }
      spots.push({
        id: this.id,
        name: this.detail.name,
        province: this.detail.province,
        city: this.detail.city,
        photo: this.detail.photo
      });
      uni.setStorageSync(key, spots);
      this.routePlanCount = spots.length;
      uni.showToast({ title: "已加入旅游线路规划" });
    },

    submitComment() {
      if (!this.commentContent.trim()) {
        uni.showToast({ title: "请输入评价内容", icon: "none" });
        return;
      }

      uni.request({
        url: "http://localhost:8080/api/comments",
        method: "POST",
        data: {
          attractionId: this.id,
          content: this.commentContent,
          score: this.userScore,
          userName: "匿名游客"
        },
        success: () => {
          uni.showToast({ title: "评价提交成功" });
          this.commentContent = "";
          this.userScore = 5;
          this.getComments();
        }
      });
    }
  }
};
</script>

<style scoped>
page {
  background: #f5f5f5;
}
.swiper {
  width: 100%;
  height: 400rpx;
}
.swiper-img {
  width: 100%;
  height: 100%;
}
.info-card {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}
.name {
  font-size: 40rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
  display: block;
}
.score-row {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}
.stars {
  display: flex;
}
.star {
  font-size: 28rpx;
  color: #ff9f00;
}
.score {
  margin-left: 10rpx;
  font-size: 26rpx;
  color: #ff6b35;
}
.attrs {
  line-height: 2;
  font-size: 26rpx;
  margin-bottom: 20rpx;
}
.attr {
  display: block;
}
.desc-title {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
  display: block;
}
.desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}
.add-route-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 8rpx;
  padding: 16rpx 0;
  font-size: 28rpx;
  margin-top: 20rpx;
  border: none;
}

/* 发表评价 */
.publish-box {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}
.publish-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 15rpx;
  display: block;
}
.select-star {
  display: flex;
  gap: 10rpx;
  margin-bottom: 20rpx;
}
.star-item {
  font-size: 36rpx;
}
textarea {
  width: 100%;
  height: 140rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  padding: 15rpx;
  box-sizing: border-box;
  margin-bottom: 15rpx;
}
.submit-btn {
  width: 100%;
  background: #1677ff;
  color: #fff;
  border-radius: 8rpx;
  padding: 12rpx 0;
}

/* 评论列表 */
.comment-section {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}
.title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  display: block;
}
.empty {
  color: #999;
  font-size: 26rpx;
}
.comment-item {
  border-bottom: 1rpx solid #eee;
  padding: 20rpx 0;
}
.user {
  font-size: 28rpx;
  font-weight: bold;
}
.item-star {
  margin: 6rpx 0;
}
.content {
  font-size: 26rpx;
  color: #333;
  line-height: 1.6;
  display: block;
}
.time {
  font-size: 22rpx;
  color: #999;
  margin-top: 6rpx;
}
</style>