
<template>
  <view class="container">
    <!-- 状态栏占位 -->
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
              <!-- 普通文本内容 -->
              <text class="msg-content">{{ msg.content }}</text>
              
              <!-- 航班推荐卡片 -->
              <view v-if="msg.flights && msg.flights.length > 0" class="flight-recommendations">
                <view class="flight-title">✈️ 推荐航班</view>
                <view 
                  class="flight-card-item" 
                  v-for="(flight, fIdx) in msg.flights" 
                  :key="'f'+fIdx"
                  @click="goToFlightSearch(flight)"
                >
                  <view class="flight-route">
                    <text class="flight-from">{{ flight.from }}</text>
                    <text class="flight-arrow">→</text>
                    <text class="flight-to">{{ flight.to }}</text>
                  </view>
                  <view class="flight-date">
                    <text>📅 {{ flight.date }}</text>
                  </view>
                  <view class="flight-action">
                    <text class="flight-price" v-if="flight.price">¥{{ flight.price }}起</text>
                    <text class="flight-book">立即查询 ›</text>
                  </view>
                </view>
              </view>
          
              <!-- 酒店推荐卡片 -->
              <view v-if="msg.hotels && msg.hotels.length > 0" class="hotel-recommendations">
                <view class="hotel-title">🏨 推荐酒店</view>
                <view 
                  class="hotel-card-item" 
                  v-for="(hotel, hIdx) in msg.hotels" 
                  :key="'h'+hIdx"
                  @click="goToHotelDetail(hotel)"
                >
                  <view class="hotel-info">
                    <text class="hotel-name">{{ hotel.name }}</text>
                    <text class="hotel-city">📍 {{ hotel.city }}</text>
                  </view>
                  <view class="hotel-right">
                    <text class="hotel-price" v-if="hotel.price">¥{{ hotel.price }}<text class="hotel-unit">/晚</text></text>
                    <text class="hotel-star" v-if="hotel.star">⭐ {{ hotel.star }}星级</text>
                    <text class="hotel-book">查看预订 ›</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
          <!-- 用户消息（右边） -->
          <view class="message-right" v-else>
            <view class="message-bubble user-bubble">
              <text>{{ msg.content }}</text>
            </view>
            <view class="message-avatar">
              <image class="user-avatar-img" :src="userAvatar" mode="aspectFill"></image>
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
      userAvatar: '/static/default-avatar.png',  // 新增：用户头像
      aiMessages: [],
      aiInput: '',
      aiLoading: false,
    };
  },
  onLoad() {
   this.username = uni.getStorageSync('loginUsername') || '';
   
   // 加载用户头像
   this.loadUserAvatar();
   
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
    // 处理从路线规划页传来的数据
    const pending = uni.getStorageSync('ai_pending_route');
    if (pending) {
      const context = uni.getStorageSync('ai_route_context');
      uni.removeStorageSync('ai_pending_route');
      uni.removeStorageSync('ai_route_context');
      if (context) {
        this.aiMessages.push({ role: 'user', content: context });
        this.aiLoading = true;
        this.scrollToBottom();
        this.callAIApi(context);
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
        uni.switchTab({ url: '/pages/message/message' });
      }
    },
    
    clearHistory() {
      uni.showModal({
        title: '提示',
        content: '确定要清空所有对话记录吗？',
        success: (res) => {
          if (res.confirm) {
            this.aiMessages = [{
              role: 'assistant',
              content: '你好！我是你的AI旅游规划助手 🌍\n告诉我你想去哪里、出发和返回日期，我帮你制定行程方案～'
            }];
            uni.setStorageSync('ai_messages', this.aiMessages);
            uni.showToast({ title: '已清空', icon: 'success' });
          }
        }
      });
    },
	
     loadUserAvatar() {
        // 先从本地缓存读取用户信息
        const userInfo = uni.getStorageSync('userInfo');
        if (userInfo && userInfo.avatar) {
          this.userAvatar = this.getFullImageUrl(userInfo.avatar);
          return;
        }
        
        // 如果没有缓存，从服务器获取
        const token = uni.getStorageSync('token');
        if (!token) return;
        
        uni.request({
          url: 'http://localhost:8080/api/users/userinfo',
          method: 'GET',
          header: {
            'Authorization': 'Bearer ' + token
          },
          success: (res) => {
            if (res.statusCode === 200 && res.data) {
              const userData = res.data;
              if (userData.avatar) {
                this.userAvatar = this.getFullImageUrl(userData.avatar);
                // 更新缓存
                const cached = uni.getStorageSync('userInfo') || {};
                cached.avatar = userData.avatar;
                uni.setStorageSync('userInfo', cached);
              }
            }
          }
        });
      },
      
      // 获取完整图片URL
      getFullImageUrl(path) {
        if (!path) return '/static/default-avatar.png';
        if (path.startsWith('http')) return path;
        if (path.startsWith('/file')) {
          return 'http://localhost:8080' + path;
        }
        return path;
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
      this.callAIApi(userMsg);
    },
   
    callAIApi(userMsg) {
      uni.request({
        url: 'http://localhost:8080/api/ai/chat',
        method: 'POST',
        timeout: 90000,
        data: {
          messages: this.aiMessages,
          username: this.username
        },
        success: (res) => {
          console.log('AI原始回复：', res.data);
          if (res.data && res.data.reply) {
            // 解析航班和酒店标记
            const parsed = this.parseFlightMarkers(res.data.reply);
            console.log('解析后航班数量：', parsed.flights.length);
            console.log('解析后酒店数量：', parsed.hotels.length);
            this.aiMessages.push({ 
              role: 'assistant', 
              content: parsed.content,
              flights: parsed.flights,
              hotels: parsed.hotels
            });
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
parseFlightMarkers(text) {
  console.log('开始解析标记，原始文本：', text);
  
  // 1. 先尝试匹配标准 [HOTEL:...] 格式
  const hotelRegex = /\[HOTEL:([^|]+)\|([^|]+)(?:\|([^|]*))?(?:\|([^|]*))?\]/g;
  const flightRegex = /\[FLIGHT:([^|]+)\|([^|]+)\|([^|]+)(?:\|([\d.]+))?\]/g;
  
  const flights = [];
  let hotels = [];
  let match;
  let content = text;

  // 解析航班
  while ((match = flightRegex.exec(text)) !== null) {
    flights.push({
      from: match[1].trim(),
      to: match[2].trim(),
      date: match[3].trim(),
      price: match[4] ? parseFloat(match[4]) : null
    });
    content = content.replace(match[0], `✈️ 从 ${match[1]} 到 ${match[2]}（${match[3]}）`);
  }

  // 解析标准酒店格式
  while ((match = hotelRegex.exec(text)) !== null) {
    hotels.push({
      name: match[1].trim(),
      city: match[2].trim(),
      price: match[3] && match[3].trim() ? parseFloat(match[3]) : null,
      star: match[4] && match[4].trim() ? parseInt(match[4]) : null
    });
    content = content.replace(match[0], `🏨 ${match[1]}（${match[2]}）`);
  }

  // 2. 如果没有匹配到标准格式，从文本中提取酒店
  if (hotels.length === 0) {
    console.log('未匹配到标准格式，从文本提取酒店...');
    
    // 匹配模式：数字序号 + 酒店名称（可能带星号）
    const extractRegex = /(?:^|\n)\s*(\d+)[.、]\s*\*{0,2}([^*\n]+?)\*{0,2}/g;
    let extractMatch;
    const extractedHotels = [];
    
    while ((extractMatch = extractRegex.exec(text)) !== null) {
      const name = extractMatch[2].trim();
      // 过滤掉非酒店名称
      if (name.length >= 2 && 
          !name.includes('推荐') && 
          !name.includes('特点') && 
          !name.includes('小贴士') &&
          !name.includes('位置') &&
          !name.includes('价格') &&
          !name.includes('星级')) {
        extractedHotels.push({
          name: name,
          index: parseInt(extractMatch[1])
        });
      }
    }
    
    // 去重
    const seen = new Set();
    for (const item of extractedHotels) {
      if (!seen.has(item.name)) {
        seen.add(item.name);
        // 提取对应酒店的价格和星级
        const price = this.extractPriceFromText(text, item.name);
        const star = this.extractStarFromText(text, item.name);
        // 提取城市
        const city = this.extractCityFromText(text);
        hotels.push({
          name: item.name,
          city: city || '未知城市',
          price: price,
          star: star
        });
      }
    }
    
    console.log('从文本提取到酒店数量：', hotels.length);
  }

  console.log('解析结果 - 航班数量：', flights.length, '酒店数量：', hotels.length);
  return { content, flights, hotels };
},

// 从文本中提取价格（作为独立方法）
extractPriceFromText(text, hotelName) {
  const lines = text.split('\n');
  for (let i = 0; i < lines.length; i++) {
    if (lines[i].includes(hotelName) || lines[i].includes(hotelName.replace('**', ''))) {
      for (let j = i + 1; j < Math.min(i + 5, lines.length); j++) {
        const line = lines[j];
        if (line.includes('价格') && line.includes('¥')) {
          const match = line.match(/¥(\d+)/);
          if (match) {
            return parseFloat(match[1]);
          }
        }
        if (line.includes('¥') && !line.includes('特点')) {
          const match = line.match(/¥(\d+)/);
          if (match) {
            return parseFloat(match[1]);
          }
        }
      }
    }
  }
  return null;
},

// 从文本中提取星级（作为独立方法）
extractStarFromText(text, hotelName) {
  const lines = text.split('\n');
  for (let i = 0; i < lines.length; i++) {
    if (lines[i].includes(hotelName) || lines[i].includes(hotelName.replace('**', ''))) {
      for (let j = i + 1; j < Math.min(i + 5, lines.length); j++) {
        const line = lines[j];
        if (line.includes('星级')) {
          const match = line.match(/(\d+)\s*星级/);
          if (match) {
            return parseInt(match[1]);
          }
        }
      }
    }
  }
  return null;
},

// 从文本中提取城市（作为独立方法）
extractCityFromText(text) {
  const cityMatch = text.match(/([^\n]{2,4})(?:市|省)?/);
  if (cityMatch) {
    const city = cityMatch[1];
    if (!city.includes('推荐') && !city.includes('酒店') && !city.includes('特点')) {
      return city;
    }
  }
  return null;
},

scrollToBottom() {
  this.$nextTick(() => {
    setTimeout(() => {
      uni.createSelectorQuery().select('#msg-bottom').boundingClientRect().exec();
    }, 100);
  });
},
    scrollToBottom() {
      this.$nextTick(() => {
        setTimeout(() => {
          uni.createSelectorQuery().select('#msg-bottom').boundingClientRect().exec();
        }, 100);
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
.flight-recommendations {
  margin-top: 16rpx;
  border-top: 2rpx dashed #e8e8e8;
  padding-top: 16rpx;
}

.flight-title {
  font-size: 26rpx;
  color: #1677ff;
  font-weight: bold;
  margin-bottom: 12rpx;
}

.flight-card-item {
  background: #f8f9ff;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  margin-bottom: 12rpx;
  border: 1rpx solid #e8edff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.flight-card-item:active {
  background: #eef2ff;
  transform: scale(0.98);
}

.flight-route {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.flight-from, .flight-to {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.flight-arrow {
  color: #1677ff;
  font-size: 24rpx;
}

.flight-date {
  font-size: 22rpx;
  color: #999;
}

.flight-action {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.flight-price {
  font-size: 24rpx;
  color: #ff4d4f;
  font-weight: bold;
}

.flight-book {
  font-size: 22rpx;
  color: #fff;
  background: linear-gradient(135deg, #1677ff, #0050b3);
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}
.user-avatar-img {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8d5f5, #d4b8e8);
  border: 2rpx solid #fff;
}
/* 酒店推荐卡片样式 */
.hotel-recommendations {
  margin-top: 16rpx;
  border-top: 2rpx dashed #e8e8e8;
  padding-top: 16rpx;
}

.hotel-title {
  font-size: 26rpx;
  color: #1677ff;
  font-weight: bold;
  margin-bottom: 12rpx;
}

.hotel-card-item {
  background: #f8f9ff;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  margin-bottom: 12rpx;
  border: 1rpx solid #e8edff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.hotel-card-item:active {
  background: #eef2ff;
  transform: scale(0.98);
}

.hotel-info {
  flex: 1;
}

.hotel-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
}

.hotel-city {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.hotel-right {
  text-align: right;
  flex-shrink: 0;
}

.hotel-price {
  font-size: 28rpx;
  font-weight: bold;
  color: #ff4d4f;
  display: block;
}

.hotel-unit {
  font-size: 20rpx;
  color: #999;
}

.hotel-star {
  font-size: 22rpx;
  color: #faad14;
  display: block;
  margin-top: 4rpx;
}

.hotel-book {
  font-size: 22rpx;
  color: #1677ff;
  background: #e8f0ff;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  margin-top: 6rpx;
  display: inline-block;
}
</style>