<template>
  <view class="container">
    <!-- 页面标题 -->
    <view class="page-header">
      <view class="header-left">
        <text class="back-icon" @click="goBack">‹</text>
        <text class="page-title">📊 经营分析</text>
      </view>
      <view class="header-right">
        <picker mode="selector" :range="periodOptions" :range-key="'label'" @change="onPeriodChange">
          <view class="period-selector">
            <text>{{ currentPeriodLabel }}</text>
            <text class="arrow">▼</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="loading-box">
      <text class="loading-icon">⏳</text>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 内容 -->
    <view v-else>
      <!-- 商家信息 -->
      <view class="merchant-info" v-if="merchantInfo">
        <text class="info-title">🏢 我的酒店</text>
        <view class="info-tags">
          <text class="tag">共 {{ merchantInfo.hotelCount }} 家酒店</text>
          <text class="tag" v-for="name in merchantInfo.hotelNames" :key="name">{{ name }}</text>
        </view>
      </view>

      <!-- 统计卡片 -->
      <view class="stats-grid">
        <view class="stat-card blue">
          <text class="stat-number">¥{{ totalRevenue }}</text>
          <text class="stat-label">总收入</text>
        </view>
        <view class="stat-card green">
          <text class="stat-number">{{ totalOrders }}</text>
          <text class="stat-label">总订单</text>
        </view>
        <view class="stat-card orange">
          <text class="stat-number">¥{{ avgPrice }}</text>
          <text class="stat-label">平均客单价</text>
        </view>
        <view class="stat-card red">
          <text class="stat-number">{{ occupancyRate }}%</text>
          <text class="stat-label">平均入住率</text>
        </view>
      </view>

      <!-- 经营分析 Tabs -->
      <view class="analysis-card">
        <view class="card-header">
          📈 经营分析
        </view>
        <view class="card-body">
          <!-- Tabs -->
          <view class="nav-tabs-custom">
            <text class="tab-item" :class="{ active: activeTab === 'overview' }" @click="switchTab('overview')">总览</text>
            <text class="tab-item" :class="{ active: activeTab === 'revenue' }" @click="switchTab('revenue')">收入</text>
            <text class="tab-item" :class="{ active: activeTab === 'orders' }" @click="switchTab('orders')">订单</text>
            <text class="tab-item" :class="{ active: activeTab === 'hotels' }" @click="switchTab('hotels')">酒店排名</text>
          </view>

          <!-- 总览 -->
          <view v-show="activeTab === 'overview'">
            <view class="overview-metrics">
              <view class="metric-item">
                <text class="metric-value blue">¥{{ monthlyMetrics.totalRevenue || 0 }}</text>
                <text class="metric-label">总营收</text>
              </view>
              <view class="metric-item">
                <text class="metric-value green">{{ monthlyMetrics.totalOrders || 0 }}</text>
                <text class="metric-label">总订单</text>
              </view>
              <view class="metric-item">
                <text class="metric-value orange">{{ monthlyMetrics.completedOrders || 0 }}</text>
                <text class="metric-label">已完成</text>
              </view>
              <view class="metric-item">
                <text class="metric-value red">{{ monthlyMetrics.pendingOrders || 0 }}</text>
                <text class="metric-label">待处理</text>
              </view>
            </view>

            <view class="chart-grid">
              <view class="chart-box">
                <text class="chart-title">📊 月度收入趋势</text>
                <!-- 使用 canvas 绘制简单图表 -->
                <canvas canvas-id="revenueCanvas" class="chart-canvas"></canvas>
              </view>
              <view class="chart-box">
                <text class="chart-title">📊 订单状态分布</text>
                <canvas canvas-id="orderStatusCanvas" class="chart-canvas"></canvas>
              </view>
            </view>
          </view>

          <!-- 收入 -->
          <view v-show="activeTab === 'revenue'">
            <view class="chart-grid">
              <view class="chart-box">
                <text class="chart-title">📈 收入趋势（最近{{ periodMonths }}个月）</text>
                <canvas canvas-id="revenueTrendCanvas" class="chart-canvas"></canvas>
              </view>
              <view class="chart-box">
                <text class="chart-title">🏆 各酒店收入排名</text>
                <canvas canvas-id="hotelRevenueCanvas" class="chart-canvas"></canvas>
              </view>
            </view>
          </view>

          <!-- 订单 -->
          <view v-show="activeTab === 'orders'">
            <view class="chart-grid">
              <view class="chart-box">
                <text class="chart-title">📦 订单趋势（最近{{ periodMonths }}个月）</text>
                <canvas canvas-id="orderTrendCanvas" class="chart-canvas"></canvas>
              </view>
              <view class="chart-box">
                <text class="chart-title">📊 订单状态分布</text>
                <canvas canvas-id="orderStatusDistCanvas" class="chart-canvas"></canvas>
              </view>
            </view>
          </view>

          <!-- 酒店排名 -->
          <view v-show="activeTab === 'hotels'">
            <view class="table-wrapper">
              <table class="rank-table">
                <thead>
                  <tr>
                    <th style="width:60px;">排名</th>
                    <th>酒店名称</th>
                    <th>总收入</th>
                    <th>订单数</th>
                    <th>评价数</th>
                    <th>平均评分</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="hotelRankData.length === 0">
                    <td colspan="6" style="text-align:center;color:#999;">暂无数据</td>
                  </tr>
                  <tr v-for="(item, index) in hotelRankData" :key="item.hotelId">
                    <td>
                      <text class="rank-num" :class="getRankClass(index)">{{ index + 1 }}</text>
                    </td>
                    <td>{{ item.hotelName }}</td>
                    <td>¥{{ item.revenue }}</td>
                    <td>{{ item.orderCount }}</td>
                    <td>{{ getCommentCount(item.hotelId) }}</td>
                    <td>{{ getAvgScore(item.hotelId) }}</td>
                  </tr>
                </tbody>
              </table>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      loading: true,
      merchantId: null,
      period: '6months',
      periodOptions: [
        { value: '3months', label: '近3个月' },
        { value: '6months', label: '近6个月' },
        { value: '12months', label: '近12个月' }
      ],
      
      // 数据
      merchantInfo: null,
      totalRevenue: 0,
      totalOrders: 0,
      avgPrice: 0,
      occupancyRate: 0,
      monthlyMetrics: {},
      revenueTrend: [],
      orderTrend: [],
      hotelRankData: [],
      orderStatusDistribution: {},
      hotelCommentStats: [],
      
      activeTab: 'overview'
    };
  },
  
  computed: {
    currentPeriodLabel() {
      const found = this.periodOptions.find(p => p.value === this.period);
      return found ? found.label : '近6个月';
    },
    periodMonths() {
      const map = { '3months': 3, '6months': 6, '12months': 12 };
      return map[this.period] || 6;
    }
  },
  
  onLoad() {
    this.loadMerchantInfo();
  },
  
  onShow() {
    if (this.merchantId) {
      this.loadAnalysisData();
    }
  },
  
  methods: {
    loadMerchantInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo');
        if (userInfo && userInfo.id) {
          this.merchantId = userInfo.id;
          console.log('商家ID:', this.merchantId);
        } else {
          uni.showToast({ title: '请先登录', icon: 'none' });
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login-register/login-register' });
          }, 1500);
        }
      } catch (e) {
        console.error('读取用户信息失败', e);
      }
    },
    
    onPeriodChange(e) {
      const index = e.detail.value;
      this.period = this.periodOptions[index].value;
      this.loadAnalysisData();
    },
    
    loadAnalysisData() {
      if (!this.merchantId) return;
      
      this.loading = true;
      
      uni.request({
        url: `http://localhost:8080/api/merchant/business-analysis`,
        method: 'GET',
        data: {
          merchantId: this.merchantId,
          period: this.period
        },
        success: (res) => {
          console.log('经营分析数据:', res.data);
          if (res.data && res.data.code === 200) {
            const d = res.data.data;
            this.merchantInfo = d.merchantInfo;
            this.totalRevenue = d.totalRevenue || 0;
            this.totalOrders = d.totalOrders || 0;
            this.avgPrice = d.avgPrice || 0;
            this.occupancyRate = d.averageOccupancy || 0;
            this.monthlyMetrics = d.monthlyMetrics || {};
            this.revenueTrend = d.revenueTrend || [];
            this.orderTrend = d.orderTrend || [];
            this.hotelRankData = d.hotelRevenueRank || [];
            this.orderStatusDistribution = d.orderStatusDistribution || {};
            this.hotelCommentStats = d.hotelCommentStats || [];
            
            this.$nextTick(() => {
              this.renderCharts();
            });
          } else {
            uni.showToast({ title: res.data.message || '加载失败', icon: 'none' });
          }
        },
        fail: (err) => {
          console.error('加载经营分析失败:', err);
          uni.showToast({ title: '网络错误，请重试', icon: 'none' });
        },
        complete: () => {
          this.loading = false;
        }
      });
    },
    
    switchTab(tab) {
      this.activeTab = tab;
      this.$nextTick(() => {
        setTimeout(() => {
          this.renderCharts();
        }, 300);
      });
    },
    
    renderCharts() {
      // 使用 Canvas 绘制图表（不用 ECharts）
      if (this.activeTab === 'overview') {
        this.drawRevenueChart();
        this.drawOrderStatusChart();
      }
      if (this.activeTab === 'revenue') {
        this.drawRevenueTrendChart();
        this.drawHotelRevenueChart();
      }
      if (this.activeTab === 'orders') {
        this.drawOrderTrendChart();
        this.drawOrderStatusDistributionChart();
      }
    },
    
    // ========== Canvas 绘图方法 ==========
    
    // 绘制收入柱状图
    drawRevenueChart() {
      const ctx = uni.createCanvasContext('revenueCanvas');
      const data = this.revenueTrend;
      if (!data || data.length === 0) {
        ctx.fillStyle = '#999';
        ctx.font = '16px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText('暂无数据', 200, 140);
        ctx.draw();
        return;
      }
      
      const width = 380;
      const height = 250;
      const padding = { top: 20, bottom: 40, left: 50, right: 20 };
      const chartWidth = width - padding.left - padding.right;
      const chartHeight = height - padding.top - padding.bottom;
      
      const values = data.map(item => item.revenue);
      const maxVal = Math.max(...values, 1);
      const barWidth = Math.min(chartWidth / values.length * 0.6, 30);
      const gap = chartWidth / values.length;
      
      // 绘制背景
      ctx.setFillStyle('#fafafa');
      ctx.fillRect(0, 0, width, height);
      
      // 绘制标题
      ctx.setFillStyle('#333');
      ctx.setFontSize(12);
      ctx.textAlign = 'center';
      ctx.fillText('月度收入趋势', width / 2, 16);
      
      // 绘制柱子
      values.forEach((val, index) => {
        const x = padding.left + index * gap + (gap - barWidth) / 2;
        const barHeight = (val / maxVal) * chartHeight;
        const y = padding.top + chartHeight - barHeight;
        
        // 渐变颜色
        const gradient = ctx.createLinearGradient(x, y, x, padding.top + chartHeight);
        gradient.addColorStop(0, '#1677ff');
        gradient.addColorStop(1, '#69b1ff');
        ctx.setFillStyle(gradient);
        ctx.fillRect(x, y, barWidth, barHeight);
        
        // 绘制数值
        ctx.setFillStyle('#666');
        ctx.setFontSize(10);
        ctx.textAlign = 'center';
        ctx.fillText('¥' + val, x + barWidth / 2, y - 4);
        
        // 绘制月份标签
        const monthLabel = data[index].month.substring(5);
        ctx.setFillStyle('#999');
        ctx.setFontSize(10);
        ctx.fillText(monthLabel, x + barWidth / 2, padding.top + chartHeight + 16);
      });
      
      ctx.draw();
    },
    
    // 绘制饼图（订单状态分布）
    drawOrderStatusChart() {
      const ctx = uni.createCanvasContext('orderStatusCanvas');
      const data = this.orderStatusDistribution;
      if (!data || Object.keys(data).length === 0) {
        ctx.setFillStyle('#999');
        ctx.setFontSize(16);
        ctx.textAlign = 'center';
        ctx.fillText('暂无数据', 200, 140);
        ctx.draw();
        return;
      }
      
      const width = 380;
      const height = 250;
      const centerX = 160;
      const centerY = 130;
      const radius = 80;
      
      const colors = ['#fa8c16', '#1677ff', '#69b1ff', '#52c41a', '#13c2c2', '#722ed1', '#ff4d4f', '#faad14'];
      const entries = Object.entries(data).filter(([_, val]) => val > 0);
      const total = entries.reduce((sum, [_, val]) => sum + val, 0);
      
      if (total === 0) {
        ctx.setFillStyle('#999');
        ctx.setFontSize(16);
        ctx.textAlign = 'center';
        ctx.fillText('暂无数据', 200, 140);
        ctx.draw();
        return;
      }
      
      let startAngle = -Math.PI / 2;
      
      entries.forEach(([name, value], index) => {
        const sliceAngle = (value / total) * 2 * Math.PI;
        const endAngle = startAngle + sliceAngle;
        
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, radius, startAngle, endAngle);
        ctx.closePath();
        ctx.setFillStyle(colors[index % colors.length]);
        ctx.fill();
        
        // 绘制图例
        const legendX = 280;
        const legendY = 30 + index * 28;
        ctx.setFillStyle(colors[index % colors.length]);
        ctx.fillRect(legendX, legendY, 16, 16);
        ctx.setFillStyle('#666');
        ctx.setFontSize(11);
        ctx.textAlign = 'left';
        ctx.fillText(`${name}: ${value}`, legendX + 22, legendY + 12);
        
        startAngle = endAngle;
      });
      
      // 绘制中心文字
      ctx.setFillStyle('#333');
      ctx.setFontSize(14);
      ctx.textAlign = 'center';
      ctx.fillText('订单分布', centerX, centerY + 5);
      
      ctx.draw();
    },
    
    // 绘制收入趋势折线图
    drawRevenueTrendChart() {
      const ctx = uni.createCanvasContext('revenueTrendCanvas');
      const data = this.revenueTrend;
      if (!data || data.length === 0) {
        ctx.setFillStyle('#999');
        ctx.setFontSize(16);
        ctx.textAlign = 'center';
        ctx.fillText('暂无数据', 200, 140);
        ctx.draw();
        return;
      }
      
      const width = 380;
      const height = 250;
      const padding = { top: 20, bottom: 40, left: 50, right: 20 };
      const chartWidth = width - padding.left - padding.right;
      const chartHeight = height - padding.top - padding.bottom;
      
      const values = data.map(item => item.revenue);
      const maxVal = Math.max(...values, 1);
      const minVal = Math.min(...values, 0);
      const range = maxVal - minVal || 1;
      
      // 绘制背景
      ctx.setFillStyle('#fafafa');
      ctx.fillRect(0, 0, width, height);
      
      // 绘制标题
      ctx.setFillStyle('#333');
      ctx.setFontSize(12);
      ctx.textAlign = 'center';
      ctx.fillText('收入趋势', width / 2, 16);
      
      // 绘制折线
      const points = values.map((val, index) => {
        const x = padding.left + (index / (values.length - 1 || 1)) * chartWidth;
        const y = padding.top + chartHeight - ((val - minVal) / range) * chartHeight;
        return { x, y, val };
      });
      
      // 绘制填充区域
      ctx.beginPath();
      ctx.moveTo(points[0].x, padding.top + chartHeight);
      points.forEach(p => ctx.lineTo(p.x, p.y));
      ctx.lineTo(points[points.length - 1].x, padding.top + chartHeight);
      ctx.closePath();
      const gradient = ctx.createLinearGradient(0, padding.top, 0, padding.top + chartHeight);
      gradient.addColorStop(0, 'rgba(22, 119, 255, 0.3)');
      gradient.addColorStop(1, 'rgba(22, 119, 255, 0.05)');
      ctx.setFillStyle(gradient);
      ctx.fill();
      
      // 绘制折线
      ctx.beginPath();
      points.forEach((p, i) => {
        if (i === 0) ctx.moveTo(p.x, p.y);
        else ctx.lineTo(p.x, p.y);
      });
      ctx.setStrokeStyle('#1677ff');
      ctx.setLineWidth(3);
      ctx.stroke();
      
      // 绘制数据点
      points.forEach(p => {
        ctx.beginPath();
        ctx.arc(p.x, p.y, 4, 0, 2 * Math.PI);
        ctx.setFillStyle('#1677ff');
        ctx.fill();
        
        // 显示数值
        ctx.setFillStyle('#666');
        ctx.setFontSize(9);
        ctx.textAlign = 'center';
        ctx.fillText('¥' + p.val, p.x, p.y - 10);
      });
      
      // 绘制X轴标签
      data.forEach((item, index) => {
        const x = padding.left + (index / (data.length - 1 || 1)) * chartWidth;
        ctx.setFillStyle('#999');
        ctx.setFontSize(9);
        ctx.textAlign = 'center';
        ctx.fillText(item.month.substring(5), x, padding.top + chartHeight + 16);
      });
      
      ctx.draw();
    },
    
    // 绘制酒店收入排名柱状图
    drawHotelRevenueChart() {
      const ctx = uni.createCanvasContext('hotelRevenueCanvas');
      const data = this.hotelRankData;
      if (!data || data.length === 0) {
        ctx.setFillStyle('#999');
        ctx.setFontSize(16);
        ctx.textAlign = 'center';
        ctx.fillText('暂无数据', 200, 140);
        ctx.draw();
        return;
      }
      
      const width = 380;
      const height = 250;
      const padding = { top: 20, bottom: 50, left: 50, right: 20 };
      const chartWidth = width - padding.left - padding.right;
      const chartHeight = height - padding.top - padding.bottom;
      
      const values = data.map(item => item.revenue);
      const maxVal = Math.max(...values, 1);
      const barWidth = Math.min(chartWidth / values.length * 0.6, 30);
      const gap = chartWidth / values.length;
      
      ctx.setFillStyle('#fafafa');
      ctx.fillRect(0, 0, width, height);
      
      ctx.setFillStyle('#333');
      ctx.setFontSize(12);
      ctx.textAlign = 'center';
      ctx.fillText('各酒店收入排名', width / 2, 16);
      
      values.forEach((val, index) => {
        const x = padding.left + index * gap + (gap - barWidth) / 2;
        const barHeight = (val / maxVal) * chartHeight;
        const y = padding.top + chartHeight - barHeight;
        
        const gradient = ctx.createLinearGradient(x, y, x, padding.top + chartHeight);
        gradient.addColorStop(0, '#fa8c16');
        gradient.addColorStop(1, '#ffd591');
        ctx.setFillStyle(gradient);
        ctx.fillRect(x, y, barWidth, barHeight);
        
        // 酒店名称（截断）
        let name = data[index].hotelName || '酒店';
        if (name.length > 4) name = name.substring(0, 4) + '..';
        ctx.setFillStyle('#999');
        ctx.setFontSize(9);
        ctx.textAlign = 'center';
        ctx.fillText(name, x + barWidth / 2, padding.top + chartHeight + 16);
        
        // 数值
        ctx.setFillStyle('#666');
        ctx.setFontSize(9);
        ctx.fillText('¥' + val, x + barWidth / 2, y - 4);
      });
      
      ctx.draw();
    },
    
    // 绘制订单趋势
    drawOrderTrendChart() {
      const ctx = uni.createCanvasContext('orderTrendCanvas');
      const data = this.orderTrend;
      if (!data || data.length === 0) {
        ctx.setFillStyle('#999');
        ctx.setFontSize(16);
        ctx.textAlign = 'center';
        ctx.fillText('暂无数据', 200, 140);
        ctx.draw();
        return;
      }
      
      const width = 380;
      const height = 250;
      const padding = { top: 20, bottom: 40, left: 50, right: 20 };
      const chartWidth = width - padding.left - padding.right;
      const chartHeight = height - padding.top - padding.bottom;
      
      const values = data.map(item => item.orders);
      const maxVal = Math.max(...values, 1);
      const minVal = Math.min(...values, 0);
      const range = maxVal - minVal || 1;
      
      ctx.setFillStyle('#fafafa');
      ctx.fillRect(0, 0, width, height);
      
      ctx.setFillStyle('#333');
      ctx.setFontSize(12);
      ctx.textAlign = 'center';
      ctx.fillText('订单趋势', width / 2, 16);
      
      const points = values.map((val, index) => {
        const x = padding.left + (index / (values.length - 1 || 1)) * chartWidth;
        const y = padding.top + chartHeight - ((val - minVal) / range) * chartHeight;
        return { x, y, val };
      });
      
      // 填充区域
      ctx.beginPath();
      ctx.moveTo(points[0].x, padding.top + chartHeight);
      points.forEach(p => ctx.lineTo(p.x, p.y));
      ctx.lineTo(points[points.length - 1].x, padding.top + chartHeight);
      ctx.closePath();
      const gradient = ctx.createLinearGradient(0, padding.top, 0, padding.top + chartHeight);
      gradient.addColorStop(0, 'rgba(82, 196, 26, 0.3)');
      gradient.addColorStop(1, 'rgba(82, 196, 26, 0.05)');
      ctx.setFillStyle(gradient);
      ctx.fill();
      
      // 折线
      ctx.beginPath();
      points.forEach((p, i) => {
        if (i === 0) ctx.moveTo(p.x, p.y);
        else ctx.lineTo(p.x, p.y);
      });
      ctx.setStrokeStyle('#52c41a');
      ctx.setLineWidth(3);
      ctx.stroke();
      
      // 数据点
      points.forEach(p => {
        ctx.beginPath();
        ctx.arc(p.x, p.y, 4, 0, 2 * Math.PI);
        ctx.setFillStyle('#52c41a');
        ctx.fill();
        ctx.setFillStyle('#666');
        ctx.setFontSize(9);
        ctx.textAlign = 'center';
        ctx.fillText(p.val, p.x, p.y - 10);
      });
      
      data.forEach((item, index) => {
        const x = padding.left + (index / (data.length - 1 || 1)) * chartWidth;
        ctx.setFillStyle('#999');
        ctx.setFontSize(9);
        ctx.textAlign = 'center';
        ctx.fillText(item.month.substring(5), x, padding.top + chartHeight + 16);
      });
      
      ctx.draw();
    },
    
    // 绘制订单状态分布饼图
    drawOrderStatusDistributionChart() {
      // 复用之前的饼图绘制逻辑
      this.drawOrderStatusChart();
    },
    
    getCommentCount(hotelId) {
      const found = this.hotelCommentStats.find(item => item.hotelId === hotelId);
      return found ? found.commentCount : 0;
    },
    
    getAvgScore(hotelId) {
      const found = this.hotelCommentStats.find(item => item.hotelId === hotelId);
      return found ? found.avgScore.toFixed(1) : '0.0';
    },
    
    getRankClass(index) {
      if (index === 0) return 'rank-1';
      if (index === 1) return 'rank-2';
      if (index === 2) return 'rank-3';
      return 'rank-other';
    },
    
    goBack() {
      uni.navigateBack();
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

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 10rpx 30rpx;
}

.header-left {
  display: flex;
  align-items: center;
}

.back-icon {
  font-size: 44rpx;
  color: #333;
  margin-right: 16rpx;
  padding: 0 10rpx;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.period-selector {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 12rpx 24rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #333;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.06);
}

.period-selector .arrow {
  font-size: 20rpx;
  color: #999;
  margin-left: 8rpx;
}

.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.loading-icon {
  font-size: 60rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
  margin-top: 20rpx;
}

.merchant-info {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.06);
}

.info-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
}

.info-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag {
  font-size: 24rpx;
  color: #666;
  background: #f5f5f5;
  padding: 6rpx 18rpx;
  border-radius: 20rpx;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.stat-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.06);
}

.stat-card .stat-number {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
}

.stat-card .stat-label {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

.stat-card.blue .stat-number { color: #1677ff; }
.stat-card.green .stat-number { color: #52c41a; }
.stat-card.orange .stat-number { color: #fa8c16; }
.stat-card.red .stat-number { color: #ff4d4f; }

.analysis-card {
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.06);
  overflow: hidden;
}

.card-header {
  padding: 20rpx 30rpx;
  background: linear-gradient(135deg, #1677ff, #0050b3);
  color: #fff;
  font-size: 30rpx;
  font-weight: bold;
}

.card-body {
  padding: 20rpx 24rpx 24rpx;
}

.nav-tabs-custom {
  display: flex;
  border-bottom: 2rpx solid #f0f0f0;
  margin-bottom: 20rpx;
}

.tab-item {
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  color: #666;
  border-bottom: 4rpx solid transparent;
  margin-bottom: -2rpx;
}

.tab-item.active {
  color: #1677ff;
  border-bottom-color: #1677ff;
  font-weight: bold;
}

.overview-metrics {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.metric-item {
  text-align: center;
  padding: 16rpx;
  background: #fafafa;
  border-radius: 12rpx;
}

.metric-item .metric-value {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
}

.metric-item .metric-label {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.metric-item .metric-value.blue { color: #1677ff; }
.metric-item .metric-value.green { color: #52c41a; }
.metric-item .metric-value.orange { color: #fa8c16; }
.metric-item .metric-value.red { color: #ff4d4f; }

.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.chart-box {
  background: #fafafa;
  border-radius: 12rpx;
  padding: 16rpx;
}

.chart-title {
  font-size: 24rpx;
  font-weight: 500;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
}

.chart-canvas {
  width: 100%;
  height: 280px;
}

.table-wrapper {
  overflow-x: auto;
}

.rank-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 26rpx;
}

.rank-table th {
  background: #fafafa;
  padding: 16rpx 20rpx;
  text-align: left;
  font-weight: bold;
  color: #555;
  border-bottom: 2rpx solid #f0f0f0;
}

.rank-table td {
  padding: 16rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
  color: #333;
}

.rank-num {
  display: inline-block;
  width: 48rpx;
  height: 48rpx;
  line-height: 48rpx;
  text-align: center;
  border-radius: 50%;
  font-weight: bold;
  font-size: 24rpx;
}

.rank-1 { background: #ffd700; color: #fff; }
.rank-2 { background: #c0c0c0; color: #fff; }
.rank-3 { background: #cd7f32; color: #fff; }
.rank-other { background: #f0f0f0; color: #999; }

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }
  .overview-metrics {
    grid-template-columns: 1fr 1fr;
  }
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>