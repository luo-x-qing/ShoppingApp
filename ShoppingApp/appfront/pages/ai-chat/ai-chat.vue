<template>
  <view class="container">
    <!-- 状态栏占位，适配刘海屏 -->
    <view class="status-bar"></view>
    
    <!-- 顶部导航栏 -->
    <view class="chat-header">
      <text class="back-btn" @click="goBack">←</text>
      <text class="title">AI 智能助手</text>
      <text class="clear-btn" @click="clearHistory">清空</text>
    </view>

    <!-- 消息列表 -->
    <scroll-view class="chat-messages" scroll-y="true" scroll-into-view="msg-bottom" :scroll-with-animation="true">
      <view class="message-list-container">
        <view class="message-item" v-for="(msg, idx) in aiMessages" :key="idx">
          <!-- AI 回复（左边） -->
          <view class="message-left" v-if="msg.role === 'assistant'">
            <view class="message-avatar">
              <view class="small-avatar">🤖</view>
            </view>
            <view class="message-bubble merchant-bubble">
              <text>{{ msg.content }}</text>
            </view>
          </view>
          <!-- 用户消息（右边） -->
          <view class="message-right" v-else>
            <view class="message-bubble user-bubble">
              <text>{{ msg.content }}</text>
            </view>
            <view class="message-avatar">
              <view class="user-avatar">👤</view>
            </view>
          </view>
        </view>

        <!-- AI 正在思考的加载气泡 -->
        <view class="message-item" v-if="aiLoading">
          <view class="message-left">
            <view class="message-avatar">
              <view class="small-avatar">🤖</view>
            </view>
            <view class="message-bubble merchant-bubble">
              <text class="loading-dots">AI 正在思考</text>
            </view>
          </view>
        </view>
      </view>
      <view id="msg-bottom"></view>
    </scroll-view>

    <!-- 底部输入区 -->
    <view class="chat-input-area">
      <input 
        class="chat-input" 
        v-model="aiInput" 
        placeholder="输入你的旅行计划..." 
        confirm-type="send"
        @confirm="sendAIMessage"
        @input="onAIInput"
      />
      <button class="send-btn" @click="sendAIMessage" :disabled="aiLoading">发送</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      aiMessages: [],
      aiInput: '',
      aiLoading: false,
    };
  },
  onLoad() {
    this.username = uni.getStorageSync('loginUsername') || '';
    const saved = uni.getStorageSync('ai_messages');
    if (saved && saved.length) {
      this.aiMessages = saved;
    } else {
      this.aiMessages = [
        {
          role: 'assistant',
          content: '你好！我是你的AI旅游规划助手 🌍\n告诉我你想去哪里、出发和返回日期，我帮你制定行程方案～'
        }
      ];
    }
  },
  onShow() {
    const pending = uni.getStorageSync('ai_pending_route')
    if (pending) {
      const context = uni.getStorageSync('ai_route_context')
      uni.removeStorageSync('ai_pending_route')
      uni.removeStorageSync('ai_route_context')
      if (context) {
        this.aiMessages.push({ role: 'user', content: context })
        this.aiLoading = true
        this.scrollToBottom()
        uni.request({
          url: 'http://localhost:8080/api/ai/chat',
          method: 'POST',
          timeout: 90000,
          data: { messages: this.aiMessages, username: this.username },
          success: (res) => {
            if (res.data && res.data.reply) {
              this.aiMessages.push({ role: 'assistant', content: res.data.reply })
            } else {
              this.aiMessages.push({ role: 'assistant', content: '抱歉，AI 服务暂时不可用。' })
            }
          },
          fail: () => {
            this.aiMessages.push({ role: 'assistant', content: '网络开小差了，请稍后再试。' })
          },
          complete: () => {
            this.aiLoading = false
            this.scrollToBottom()
            uni.setStorageSync('ai_messages', this.aiMessages)
          }
        })
      }
    }
  },
  onUnload() {
    uni.setStorageSync('ai_messages', this.aiMessages);
  },
  methods: {
    goBack() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        // 如果没有上一页（比如从首页直接进入），则跳转到消息页或首页
        uni.switchTab({
          url: '/pages/message/message'   // 或你的首页路径
        });
      }
    },
    clearHistory() {
      uni.showModal({
        title: '提示',
        content: '确定要清空所有对话记录吗？',
        success: (res) => {
          if (res.confirm) {
            this.aiMessages = [
              {
                role: 'assistant',
                content: '你好！我是你的AI旅游规划助手 🌍\n告诉我你想去哪里、出发和返回日期，我帮你制定行程方案～'
              }
            ];
            uni.setStorageSync('ai_messages', this.aiMessages);
            uni.showToast({ title: '已清空', icon: 'success' });
          }
        }
      });
    },
    onAIInput(e) {
      this.aiInput = e.detail.value;
    },
    sendAIMessage() {
      if (!this.aiInput.trim() || this.aiLoading) return;
      const userMsg = this.aiInput.trim();
      this.aiMessages.push({ role: 'user', content: userMsg });
      this.aiInput = '';
      this.aiLoading = true;
      this.scrollToBottom();

      uni.request({
        url: 'http://localhost:8080/api/ai/chat',
        method: 'POST',
        timeout: 90000,
        data: {
          messages: this.aiMessages,
          username: this.username
        },
        success: (res) => {
          if (res.data && res.data.reply) {
            this.aiMessages.push({ role: 'assistant', content: res.data.reply });
          } else {
            this.aiMessages.push({ role: 'assistant', content: '抱歉，AI 服务暂时不可用。' });
          }
        },
        fail: (err) => {
          console.error('AI请求失败', err);
          this.aiMessages.push({ role: 'assistant', content: '网络开小差了，请稍后再试。' });
        },
        complete: () => {
          this.aiLoading = false;
          this.scrollToBottom();
          uni.setStorageSync('ai_messages', this.aiMessages);
        }
      });
    },
    scrollToBottom() {
      this.$nextTick(() => {
        uni.createSelectorQuery().select('#msg-bottom').boundingClientRect().exec();
      });
    }
  }
};
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

/* 状态栏占位，自动适配刘海屏 */
.status-bar {
  height: var(--status-bar-height);
  background: linear-gradient(135deg, #1677ff, #0050b3);
}

/* 顶部导航栏 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: white;
}
.back-btn {
  font-size: 44rpx;
  font-weight: bold;
  padding: 10rpx 20rpx;
  margin-left: -10rpx;
}
.title {
  font-size: 36rpx;
  font-weight: bold;
  letter-spacing: 1px;
}
.clear-btn {
  font-size: 28rpx;
  padding: 8rpx 20rpx;
  background: rgba(255,255,255,0.2);
  border-radius: 30rpx;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  padding: 30rpx 20rpx 30rpx 30rpx;
  overflow-y: auto;
}
.message-list-container {
  display: flex;
  flex-direction: column;
}
.message-item {
  margin-bottom: 30rpx;
  width: 100%;
}

.message-left {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
}
.message-left .message-avatar {
  margin-right: 16rpx;
}
.message-right {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  transform: translateX(-40rpx);
}
.message-right .message-bubble {
  margin-right: 8rpx;
}
.message-avatar {
  width: 70rpx;
  height: 70rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.small-avatar {
  width: 60rpx;
  height: 60rpx;
  background: #e8f4ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}
.user-avatar {
  width: 60rpx;
  height: 60rpx;
  background: linear-gradient(135deg, #1677ff, #0050b3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #fff;
}
.message-bubble {
  max-width: 60%;
  padding: 20rpx 28rpx;
  border-radius: 30rpx;
  font-size: 28rpx;
  line-height: 1.5;
  word-wrap: break-word;
  white-space: pre-wrap;
}
.user-bubble {
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
  border-radius: 30rpx 30rpx 8rpx 30rpx;
}
.merchant-bubble {
  background: #fff;
  color: #333;
  border-radius: 30rpx 30rpx 30rpx 8rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05);
}

.loading-dots {
  display: inline-block;
  position: relative;
  min-width: 80rpx;
}
.loading-dots::after {
  content: '...';
  position: absolute;
  width: 40rpx;
  animation: dotPulse 1.5s infinite;
}
@keyframes dotPulse {
  0% { content: '.'; }
  33% { content: '..'; }
  66% { content: '...'; }
  100% { content: '.'; }
}

/* 底部输入区 */
.chat-input-area {
  display: flex;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #eee;
  background: #fff;
  align-items: center;
  gap: 16rpx;
}
.chat-input {
  flex: 1;
  height: 70rpx;
  background: #f5f5f5;
  border-radius: 35rpx;
  padding: 0 25rpx;
  font-size: 28rpx;
}
.send-btn {
  width: 100rpx;
  height: 60rpx;
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
  border-radius: 30rpx;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  line-height: 1;
  padding: 0;
}
.send-btn[disabled] {
  opacity: 0.6;
}
.send-btn::after {
  border: none;
}
</style>