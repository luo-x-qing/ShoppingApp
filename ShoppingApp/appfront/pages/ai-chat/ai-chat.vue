<template>
  <view class="container">
    <!-- 状态栏占位 -->
    <view class="status-bar"></view>
    
    <!-- 顶部导航栏 -->
    <view class="chat-header">
      <view class="header-left">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="title">AI 智能助手</text>
      </view>
      <view class="header-right">
        <text class="clear-btn" @click="clearHistory">清空</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view 
      class="chat-messages" 
      scroll-y="true" 
      :scroll-into-view="scrollToId" 
      :scroll-with-animation="true"
      @scroll="onScroll"
    >
      <view class="message-list-container">
        <!-- 欢迎语（首次进入时显示） -->
        <view v-if="aiMessages.length === 0" class="welcome-box">
          <view class="welcome-icon">🤖</view>
          <text class="welcome-title">你好！我是 AI 旅游规划助手</text>
          <text class="welcome-desc">我可以帮你：</text>
          <view class="welcome-tags">
            <text class="welcome-tag">✈️ 规划行程</text>
            <text class="welcome-tag">🏨 推荐酒店</text>
            <text class="welcome-tag">🎫 推荐景点</text>
            <text class="welcome-tag">🚗 交通建议</text>
          </view>
          <text class="welcome-hint">试试输入：7月1日从北京去杭州玩3天</text>
        </view>

        <!-- 消息列表 -->
        <view 
          class="message-item" 
          v-for="(msg, idx) in aiMessages" 
          :key="idx"
          :class="{ 'message-enter': true }"
        >
          <!-- AI 回复（左边） -->
          <view class="message-left" v-if="msg.role === 'assistant'">
            <view class="message-avatar">
              <view class="small-avatar ai-avatar">🤖</view>
            </view>
            <view class="message-content">
              <view class="message-bubble merchant-bubble">
                <!-- 普通文本内容 - 支持换行 -->
                <text class="msg-content">{{ msg.content }}</text>
                
                <!-- 航班推荐卡片 -->
                <view v-if="msg.flights && msg.flights.length > 0" class="recommendations">
                  <view class="recommend-title flight-title">✈️ 推荐航班</view>
                  <view 
                    class="recommend-card flight-card" 
                    v-for="(flight, fIdx) in msg.flights" 
                    :key="'f'+fIdx"
                    @click="goToFlightSearch(flight)"
                  >
                    <view class="card-route">
                      <text class="card-from">{{ flight.from }}</text>
                      <text class="card-arrow">→</text>
                      <text class="card-to">{{ flight.to }}</text>
                    </view>
                    <view class="card-info">
                      <text class="card-date">📅 {{ flight.date }}</text>
                      <text class="card-price" v-if="flight.price">¥{{ flight.price }}起</text>
                    </view>
                    <text class="card-action">立即查询 ›</text>
                  </view>
                </view>

                <!-- 酒店推荐卡片 -->
                <view v-if="msg.hotels && msg.hotels.length > 0" class="recommendations">
                  <view class="recommend-title hotel-title">🏨 推荐酒店</view>
                  <view 
                    class="recommend-card hotel-card" 
                    v-for="(hotel, hIdx) in msg.hotels" 
                    :key="'h'+hIdx"
                    @click="goToHotelDetail(hotel)"
                  >
                    <view class="card-left">
                      <text class="card-name">{{ hotel.name }}</text>
                      <text class="card-sub">📍 {{ hotel.city }}</text>
                    </view>
                    <view class="card-right">
                      <text class="card-price" v-if="hotel.price">¥{{ hotel.price }}/晚</text>
                      <text class="card-star" v-if="hotel.star">⭐ {{ hotel.star }}星</text>
                      <text class="card-action">查看 ›</text>
                    </view>
                  </view>
                </view>
              </view>
              <!-- 消息时间 -->
              <text class="msg-time">{{ msg.time || '' }}</text>
            </view>
          </view>
          
          <!-- 用户消息（右边） -->
          <view class="message-right" v-else>
            <view class="message-content">
              <view class="message-bubble user-bubble">
                <text class="msg-content">{{ msg.content }}</text>
              </view>
              <text class="msg-time">{{ msg.time || '' }}</text>
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
              <view class="small-avatar ai-avatar">🤖</view>
            </view>
            <view class="message-content">
              <view class="message-bubble merchant-bubble">
                <view class="thinking-dots">
                  <text class="dot">•</text>
                  <text class="dot">•</text>
                  <text class="dot">•</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view id="msg-bottom"></view>
    </scroll-view>

    <!-- 快捷输入 -->
    <view class="quick-inputs" v-if="quickInputs.length > 0 && !aiLoading">
      <scroll-view scroll-x="true" class="quick-scroll" :show-scrollbar="false">
        <text 
          class="quick-item" 
          v-for="(item, idx) in quickInputs" 
          :key="idx"
          @click="sendQuickMessage(item)"
        >{{ item }}</text>
      </scroll-view>
    </view>

    <!-- 底部输入区 -->
    <view class="chat-input-area">
      <view class="input-wrapper">
        <input 
          class="chat-input" 
          v-model="aiInput" 
          placeholder="输入你的旅行计划..." 
          confirm-type="send"
          @confirm="sendAIMessage"
          @input="onAIInput"
          :disabled="aiLoading"
        />
        <button 
          class="send-btn" 
          @click="sendAIMessage" 
          :disabled="aiLoading || !aiInput.trim()"
        >
          <text v-if="!aiLoading">发送</text>
          <text v-else class="sending">⏳</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      userAvatar: '/static/default-avatar.png',
      aiMessages: [],
      aiInput: '',
      aiLoading: false,
      scrollToId: '',
      quickInputs: [
        '🏖️ 推荐旅游城市',
        '🏨 推荐酒店',
        '✈️ 机票查询',
        '🗺️ 行程规划'
      ]
    };
  },
  
  onLoad() {
    this.username = uni.getStorageSync('loginUsername') || '';
    this.loadUserAvatar();
    this.loadHistory();
  },
  
  onShow() {
    this.loadUserAvatar();
    this.handlePendingRoute();
  },
  
  onUnload() {
    this.saveHistory();
  },
  
  methods: {
    // ========== 数据管理 ==========
    loadHistory() {
      const saved = uni.getStorageSync('ai_messages');
      if (saved && saved.length) {
        this.aiMessages = saved;
      }
    },
    
    saveHistory() {
      if (this.aiMessages.length > 0) {
        uni.setStorageSync('ai_messages', this.aiMessages);
      }
    },
    
    loadUserAvatar() {
      const userInfo = uni.getStorageSync('userInfo');
      if (userInfo && userInfo.avatar) {
        this.userAvatar = this.getFullImageUrl(userInfo.avatar);
        return;
      }
      const token = uni.getStorageSync('token');
      if (!token) return;
      uni.request({
        url: 'http://localhost:8080/api/users/userinfo',
        method: 'GET',
        header: { 'Authorization': 'Bearer ' + token },
        success: (res) => {
          if (res.statusCode === 200 && res.data && res.data.avatar) {
            this.userAvatar = this.getFullImageUrl(res.data.avatar);
            const cached = uni.getStorageSync('userInfo') || {};
            cached.avatar = res.data.avatar;
            uni.setStorageSync('userInfo', cached);
          }
        }
      });
    },
    
    getFullImageUrl(path) {
      if (!path) return '/static/default-avatar.png';
      if (path.startsWith('http')) return path;
      if (path.startsWith('/file')) return 'http://localhost:8080' + path;
      return path;
    },
    
    // ========== 路由管理 ==========
    goBack() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        uni.switchTab({ url: '/pages/message/message' });
      }
    },
    
    // ========== 消息管理 ==========
    onAIInput(e) {
      this.aiInput = e.detail.value;
    },
    
    sendQuickMessage(text) {
      this.aiInput = text;
      this.sendAIMessage();
    },
    
    sendAIMessage() {
      if (!this.aiInput.trim() || this.aiLoading) return;
      const userMsg = this.aiInput.trim();
      this.aiMessages.push({ 
        role: 'user', 
        content: userMsg,
        time: this.formatTime(new Date())
      });
      this.aiInput = '';
      this.aiLoading = true;
      this.scrollToBottom();
      this.callAIApi(userMsg);
    },
    
   callAIApi(userMsg) {
     uni.request({
       url: 'http://localhost:8080/api/ai/chat',
       method: 'POST',
       timeout: 120000,
       data: {
         messages: this.aiMessages,
         username: this.username
       },
       success: (res) => {
         console.log('===== AI回复开始 =====');
         console.log('完整响应：', JSON.stringify(res.data));
         
         if (res.data && res.data.reply) {
           const reply = res.data.reply;
           console.log('AI回复内容前500字符：', reply.substring(0, 500));
           console.log('是否包含 [FLIGHT:', reply.includes('[FLIGHT:'));
           console.log('是否包含 [HOTEL:', reply.includes('[HOTEL:'));
           
           try {
             const parsed = this.parseFlightMarkers(reply);
             console.log('解析后航班数量：', parsed.flights.length);
             console.log('解析后酒店数量：', parsed.hotels.length);
             
             this.aiMessages.push({ 
               role: 'assistant', 
               content: parsed.content || reply,
               flights: parsed.flights || [],
               hotels: parsed.hotels || [],
               time: this.formatTime(new Date())
             });
           } catch (e) {
             console.error('解析出错：', e);
             // 解析失败时直接显示原始回复
             this.aiMessages.push({ 
               role: 'assistant', 
               content: reply,
               flights: [],
               hotels: [],
               time: this.formatTime(new Date())
             });
           }
         } else {
           this.aiMessages.push({ 
             role: 'assistant', 
             content: '抱歉，AI 服务暂时不可用，请稍后再试。',
             time: this.formatTime(new Date())
           });
         }
         console.log('===== AI回复结束 =====');
       },
       fail: (err) => {
         console.error('AI请求失败', err);
         let errorMsg = '网络开小差了，请稍后再试。';
         if (err.errMsg && err.errMsg.includes('timeout')) {
           errorMsg = '请求超时，请简化问题或稍后再试。😊';
         }
         this.aiMessages.push({ 
           role: 'assistant', 
           content: errorMsg,
           time: this.formatTime(new Date())
         });
       },
       complete: () => {
         this.aiLoading = false;
         this.scrollToBottom();
         this.saveHistory();
       }
     });
   },

    // ========== 解析功能 ==========
    // 替换你现有的 parseFlightMarkers 方法
    parseFlightMarkers(text) {
      console.log('===== 开始解析 =====');
      console.log('原始文本长度：', text.length);
      console.log('前500字符：', text.substring(0, 500));
      
      const flights = [];
      const hotels = [];
      let content = text;
    
      // 1. 解析航班 [FLIGHT:出发|到达|日期|价格]
      try {
        const flightRegex = /\[FLIGHT:([^|]+)\|([^|]+)\|([^|]+)(?:\|([\d.]+))?\]/g;
        let match;
        while ((match = flightRegex.exec(text)) !== null) {
          console.log('匹配到航班：', match);
          flights.push({
            from: match[1].trim(),
            to: match[2].trim(),
            date: match[3].trim(),
            price: match[4] ? parseFloat(match[4]) : null
          });
        }
        console.log('航班解析完成，数量：', flights.length);
      } catch (e) {
        console.error('航班解析出错：', e);
      }
    
      // 2. 解析酒店 [HOTEL:名称|城市|价格|星级]
      try {
        const hotelRegex = /\[HOTEL:([^|]+)\|([^|]+)(?:\|([^|]*))?(?:\|([^|]*))?\]/g;
        let match;
        while ((match = hotelRegex.exec(text)) !== null) {
          console.log('匹配到酒店：', match);
          hotels.push({
            name: match[1].trim(),
            city: match[2].trim(),
            price: match[3] && match[3].trim() ? parseFloat(match[3]) : null,
            star: match[4] && match[4].trim() ? parseInt(match[4]) : null
          });
        }
        console.log('酒店解析完成，数量：', hotels.length);
      } catch (e) {
        console.error('酒店解析出错：', e);
      }
    
      // 3. 如果没有解析到任何数据，尝试从文本中提取酒店
      if (flights.length === 0 && hotels.length === 0) {
        console.log('未解析到标准格式，尝试从文本提取...');
        try {
          const extractedHotels = this.extractHotelsFromText(text);
          if (extractedHotels.length > 0) {
            hotels.push(...extractedHotels);
            console.log('从文本提取到酒店数量：', extractedHotels.length);
          }
        } catch (e) {
          console.error('文本提取出错：', e);
        }
      }
    
      // 4. 清理文本中的标记（替换为友好显示）
      let displayContent = content;
      // 移除剩余的 [FLIGHT:...] 和 [HOTEL:...] 标记
      displayContent = displayContent.replace(/\[FLIGHT:[^\]]*\]/g, '✈️ 航班推荐');
      displayContent = displayContent.replace(/\[HOTEL:[^\]]*\]/g, '🏨 酒店推荐');
    
      console.log('最终结果 - 航班：', flights.length, '酒店：', hotels.length);
      console.log('===== 解析结束 =====');
      
      return { 
        content: displayContent, 
        flights: flights, 
        hotels: hotels 
      };
    },
    
   extractHotelsFromText(text) {
     console.log('开始从文本提取酒店...');
     const hotels = [];
     const seen = new Set();
     
     // 匹配模式：数字序号 + 酒店名称
     // 例如："1. **如家酒店**" 或 "2. 香格里拉大酒店"
     const extractRegex = /(?:^|\n)\s*(\d+)[.、]\s*\*{0,2}([^*\n]+?)\*{0,2}/g;
     let match;
     
     while ((match = extractRegex.exec(text)) !== null) {
       const name = match[2].trim();
       // 过滤掉非酒店名称
       if (name.length >= 2 && 
           !name.includes('推荐') && 
           !name.includes('特点') && 
           !name.includes('小贴士') &&
           !name.includes('位置') &&
           !name.includes('价格') &&
           !name.includes('星级') &&
           !name.includes('费用') &&
           !name.includes('预算') &&
           !name.includes('注意') &&
           !seen.has(name)) {
         seen.add(name);
         
         // 提取价格
         const price = this.extractPriceFromText(text, name);
         // 提取星级
         const star = this.extractStarFromText(text, name);
         // 提取城市
         const city = this.extractCityFromText(text);
         
         hotels.push({
           name: name,
           city: city || '未知城市',
           price: price,
           star: star
         });
       }
     }
     
     console.log('文本提取完成，酒店数量：', hotels.length);
     return hotels;
   },
    extractPriceFromText(text, hotelName) {
      const lines = text.split('\n');
      for (let i = 0; i < lines.length; i++) {
        if (lines[i].includes(hotelName) || lines[i].includes(hotelName.replace('**', ''))) {
          for (let j = i + 1; j < Math.min(i + 5, lines.length); j++) {
            const line = lines[j];
            const match = line.match(/¥(\d+)/);
            if (match) return parseFloat(match[1]);
          }
        }
      }
      return null;
    },
    
    extractStarFromText(text, hotelName) {
      const lines = text.split('\n');
      for (let i = 0; i < lines.length; i++) {
        if (lines[i].includes(hotelName) || lines[i].includes(hotelName.replace('**', ''))) {
          for (let j = i + 1; j < Math.min(i + 5, lines.length); j++) {
            const match = lines[j].match(/(\d+)\s*星级/);
            if (match) return parseInt(match[1]);
          }
        }
      }
      return null;
    },
    
    extractCityFromText(text) {
      const match = text.match(/([^\n]{2,4})(?:市|省)?/);
      if (match) {
        const city = match[1];
        if (!city.includes('推荐') && !city.includes('酒店') && !city.includes('特点')) {
          return city;
        }
      }
      return null;
    },
    
    // ========== 跳转功能 ==========
    goToFlightSearch(flight) {
      uni.setStorageSync('flight_search_params', {
        fromCity: flight.from,
        toCity: flight.to,
        fromDate: flight.date
      });
      uni.switchTab({ url: '/pages/flight/search-flight' });
    },
    
    goToHotelDetail(hotel) {
      if (!hotel || !hotel.name) {
        uni.showToast({ title: '酒店信息不完整', icon: 'none' });
        return;
      }
      uni.showLoading({ title: '查找酒店...' });
      uni.request({
        url: 'http://localhost:8080/api/hotels/search',
        method: 'GET',
        data: { keyword: hotel.name },
        success: (res) => {
          uni.hideLoading();
          let hotels = [];
          if (res.data && res.data.code === 200) {
            hotels = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            hotels = res.data;
          }
          
          if (hotels.length === 0) {
            uni.showModal({
              title: '未找到该酒店',
              content: `数据库中暂无"${hotel.name}"，是否查看所有酒店？`,
              success: (res) => {
                if (res.confirm) {
                  uni.navigateTo({ url: '/pages/hotel/hotelList' });
                }
              }
            });
            return;
          }
          
          let matched = hotels.find(h => h.name === hotel.name);
          if (!matched) {
            matched = hotels.find(h => h.name.includes(hotel.name) || hotel.name.includes(h.name));
          }
          if (!matched && hotels.length > 0) matched = hotels[0];
          
          if (matched && matched.id) {
            uni.navigateTo({ url: `/pages/hotelDetail/hotelDetail?id=${matched.id}` });
          } else {
            uni.showToast({ title: '无法找到对应酒店', icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '查询失败', icon: 'none' });
        }
      });
    },
    
    // ========== UI 辅助 ==========
    scrollToBottom() {
      this.scrollToId = 'msg-bottom';
      this.$nextTick(() => {
        setTimeout(() => {
          uni.createSelectorQuery().select('#msg-bottom').boundingClientRect().exec();
        }, 300);
      });
    },
    
    onScroll(e) {
      // 滚动事件（可用于标记已读等）
    },
    
    formatTime(date) {
      if (!date) return '';
      const d = new Date(date);
      const hours = String(d.getHours()).padStart(2, '0');
      const minutes = String(d.getMinutes()).padStart(2, '0');
      return `${hours}:${minutes}`;
    },
    
    // ========== 清空功能 ==========
    clearHistory() {
      if (this.aiMessages.length === 0) {
        uni.showToast({ title: '暂无对话记录', icon: 'none' });
        return;
      }
      uni.showModal({
        title: '提示',
        content: '确定要清空所有对话记录吗？',
        success: (res) => {
          if (res.confirm) {
            this.aiMessages = [];
            uni.removeStorageSync('ai_messages');
            uni.showToast({ title: '已清空', icon: 'success' });
          }
        }
      });
    },
    
    // ========== 处理从其他页面传来的数据 ==========
    handlePendingRoute() {
      const pending = uni.getStorageSync('ai_pending_route');
      if (pending) {
        const context = uni.getStorageSync('ai_route_context');
        uni.removeStorageSync('ai_pending_route');
        uni.removeStorageSync('ai_route_context');
        if (context) {
          this.aiMessages.push({ 
            role: 'user', 
            content: context,
            time: this.formatTime(new Date())
          });
          this.aiLoading = true;
          this.scrollToBottom();
          this.callAIApi(context);
        }
      }
    }
  }
};
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
}

/* ===== 状态栏 ===== */
.status-bar {
  height: var(--status-bar-height);
  background: linear-gradient(135deg, #667eea, #764ba2);
}

/* ===== 顶部导航栏 ===== */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}
.header-left {
  display: flex;
  align-items: center;
}
.back-btn {
  font-size: 44rpx;
  font-weight: bold;
  padding: 10rpx 20rpx;
  margin-left: -10rpx;
}
.title {
  font-size: 34rpx;
  font-weight: bold;
  letter-spacing: 1px;
}
.clear-btn {
  font-size: 26rpx;
  padding: 8rpx 20rpx;
  background: rgba(255,255,255,0.2);
  border-radius: 30rpx;
}

/* ===== 消息区域 ===== */
.chat-messages {
  flex: 1;
  padding: 20rpx 20rpx 20rpx 30rpx;
  overflow-y: auto;
}
.message-list-container {
  display: flex;
  flex-direction: column;
}
.message-item {
  margin-bottom: 30rpx;
  width: 100%;
  animation: messageIn 0.3s ease-out;
}
@keyframes messageIn {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 欢迎框 ===== */
.welcome-box {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
  text-align: center;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.08);
}
.welcome-icon {
  font-size: 80rpx;
  margin-bottom: 16rpx;
}
.welcome-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
}
.welcome-desc {
  font-size: 26rpx;
  color: #999;
  display: block;
  margin-bottom: 20rpx;
}
.welcome-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
}
.welcome-tag {
  background: #f0f0ff;
  color: #667eea;
  padding: 8rpx 24rpx;
  border-radius: 30rpx;
  font-size: 24rpx;
}
.welcome-hint {
  font-size: 24rpx;
  color: #ccc;
}

/* ===== 消息布局 ===== */
.message-left {
  display: flex;
  align-items: flex-start;
}
.message-left .message-avatar {
  margin-right: 16rpx;
}
.message-right {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
}
.message-right .message-avatar {
  margin-left: 16rpx;
}
.message-content {
  max-width: 75%;
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
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}
.ai-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}
.user-avatar-img {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8d5f5, #d4b8e8);
  border: 2rpx solid #fff;
  flex-shrink: 0;
}

.message-bubble {
  padding: 20rpx 28rpx;
  border-radius: 30rpx;
  font-size: 28rpx;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}
.user-bubble {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 30rpx 30rpx 8rpx 30rpx;
}
.merchant-bubble {
  background: #fff;
  color: #333;
  border-radius: 30rpx 30rpx 30rpx 8rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.06);
}
.msg-content {
  display: block;
  line-height: 1.6;
}
.msg-time {
  display: block;
  font-size: 20rpx;
  color: #ccc;
  margin-top: 8rpx;
  text-align: right;
}
.message-right .msg-time {
  text-align: left;
}

/* ===== 思考动画 ===== */
.thinking-dots {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 4rpx 0;
}
.thinking-dots .dot {
  font-size: 40rpx;
  color: #999;
  animation: dotBounce 1.2s ease-in-out infinite;
}
.thinking-dots .dot:nth-child(2) { animation-delay: 0.2s; }
.thinking-dots .dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.3; }
  40% { transform: scale(1); opacity: 1; }
}

/* ===== 推荐卡片 ===== */
.recommendations {
  margin-top: 16rpx;
  border-top: 2rpx dashed #e8e8e8;
  padding-top: 16rpx;
}
.recommend-title {
  font-size: 26rpx;
  font-weight: bold;
  margin-bottom: 12rpx;
}
.flight-title { color: #667eea; }
.hotel-title { color: #ff6b6b; }

.recommend-card {
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  margin-bottom: 12rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.2s;
}
.recommend-card:active {
  transform: scale(0.97);
}
.flight-card {
  background: #f0f4ff;
  border: 1rpx solid #d4e0ff;
}
.hotel-card {
  background: #fff5f5;
  border: 1rpx solid #ffd4d4;
}

.card-route {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.card-from, .card-to {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}
.card-arrow {
  color: #667eea;
  font-size: 24rpx;
}
.card-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.card-date {
  font-size: 22rpx;
  color: #999;
}
.card-price {
  font-size: 24rpx;
  font-weight: bold;
  color: #ff4d4f;
}
.card-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
}
.card-sub {
  font-size: 22rpx;
  color: #999;
  display: block;
}
.card-star {
  font-size: 22rpx;
  color: #faad14;
}
.card-action {
  font-size: 22rpx;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}
.hotel-card .card-action {
  color: #ff6b6b;
  background: rgba(255, 107, 107, 0.1);
}
.card-left {
  flex: 1;
}
.card-right {
  text-align: right;
  flex-shrink: 0;
  margin-left: 16rpx;
}

/* ===== 快捷输入 ===== */
.quick-inputs {
  padding: 10rpx 30rpx 6rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
}
.quick-scroll {
  white-space: nowrap;
  display: flex;
}
.quick-item {
  display: inline-block;
  padding: 10rpx 24rpx;
  background: #f5f5f5;
  border-radius: 30rpx;
  font-size: 24rpx;
  color: #666;
  margin-right: 16rpx;
  flex-shrink: 0;
}
.quick-item:active {
  background: #e8e8e8;
}

/* ===== 底部输入区 ===== */
.chat-input-area {
  padding: 16rpx 30rpx;
  padding-bottom: calc(16rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
}
.input-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.chat-input {
  flex: 1;
  height: 72rpx;
  background: #f5f7fa;
  border-radius: 36rpx;
  padding: 0 28rpx;
  font-size: 28rpx;
}
.send-btn {
  width: 110rpx;
  height: 72rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 36rpx;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  padding: 0;
}
.send-btn[disabled] {
  opacity: 0.5;
}
.send-btn::after {
  border: none;
}
.sending {
  font-size: 32rpx;
}
</style>