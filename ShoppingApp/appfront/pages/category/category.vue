<template>
  <view class="container">
    <view class="header">
      <text class="title">🏨 酒店预订</text>
    </view>

    <!-- 位置选择和排序栏 -->
    <view class="filter-bar">
      <view class="location-selector" @click="openLocationPicker">
        <text class="location-icon">📍</text>
        <text class="location-text">{{ currentLocation.name || '选择位置' }}</text>
        <text class="arrow-down">▼</text>
      </view>
      
      <view class="sort-options">
        <view 
          class="sort-item" 
          :class="{ active: currentSort === 'price' }"
          @click="changeSort('price')"
        >
          <text>价格</text>
          <view class="sort-arrows">
            <text class="arrow-up" :class="{ active: currentSort === 'price' && sortOrder === 'asc' }">▲</text>
            <text class="arrow-down-sort" :class="{ active: currentSort === 'price' && sortOrder === 'desc' }">▼</text>
          </view>
        </view>
        <view 
          class="sort-item" 
          :class="{ active: currentSort === 'distance' }"
          @click="changeSort('distance')"
        >
          <text>距离最近</text>
          <view class="sort-arrows">
            <text class="arrow-up" :class="{ active: currentSort === 'distance' && sortOrder === 'asc' }">▲</text>
            <text class="arrow-down-sort" :class="{ active: currentSort === 'distance' && sortOrder === 'desc' }">▼</text>
          </view>
        </view>
        <view 
          class="sort-item" 
          :class="{ active: currentSort === 'rating' }"
          @click="changeSort('rating')"
        >
          <text>评分最高</text>
          <view class="sort-arrows">
            <text class="arrow-up" :class="{ active: currentSort === 'rating' && sortOrder === 'asc' }">▲</text>
            <text class="arrow-down-sort" :class="{ active: currentSort === 'rating' && sortOrder === 'desc' }">▼</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 搜索区域 - 带实时建议 -->
    <view class="search-box">
      <view class="search-input-wrapper">
        <input 
          class="search-input" 
          placeholder="搜索酒店" 
          v-model="keyword" 
          @input="onSearchInput"
          @confirm="onSearchConfirm"
          @focus="showSuggest = true"
        />
        <button class="search-btn" @click="onSearchConfirm">搜索</button>
      </view>
      
      <!-- 搜索建议下拉列表 -->
      <view class="suggest-dropdown" v-if="showSuggest && suggestList.length > 0">
        <view 
          v-for="item in suggestList" 
          :key="item.id" 
          class="suggest-item-dropdown"
          @click="goHotelDetail(item.id, item.name)"
        >
          <view class="suggest-name">{{ item.name }}</view>
          <view class="suggest-info">
            <text class="suggest-category">{{ item.category }}</text>
            <text class="suggest-price">¥{{ item.price }}/晚</text>
          </view>
        </view>
      </view>
    </view>

    <view class="main-wrap">
      <!-- 左侧菜单 -->
      <view class="left-tab">
        <view v-for="(item, index) in categoryList" :key="index"
          class="tab-item" :class="{ active: activeTab === index }"
          @click="changeTab(index)">
          {{ item }}
        </view>
      </view>

      <!-- 右侧酒店列表 - 保持不变 -->
      <scroll-view class="right-list" scroll-y :style="{ height: scrollHeight + 'px' }">
        <view 
          v-for="item in hotelList" 
          :key="item.id" 
          class="card"
          @click="goHotelDetail(item.id, item.name)"
        >
          <image :src="item.image" class="card-img" mode="aspectFill" />
          <view class="card-info">
            <view>
              <text class="card-name">{{ item.name }}</text>
              <text class="card-desc">
                {{ item.starLevel }}星 · {{ item.category }}
                <text v-if="item.distanceText" class="distance-text"> · {{ item.distanceText }}</text>
              </text>
              <view class="rating-row" v-if="item.rating > 0">
                <text class="rating-star">⭐</text>
                <text class="rating-score">{{ item.rating }}</text>
              </view>
              <text class="card-price">¥{{ item.price }}/晚</text>
            </view>
          </view>
        </view>
        <!-- 空状态 -->
        <view v-if="hotelList.length === 0 && !loading" class="empty-state">
          <text>暂无酒店</text>
        </view>
        <view v-if="loading" class="loading-state">
          <text>加载中...</text>
        </view>
      </scroll-view>
    </view>

    <!-- 位置选择弹窗 -->
    <view class="modal-mask" v-if="showLocationPicker" @click="closeLocationPicker">
      <view class="map-modal-container" @click.stop>
        <view class="map-modal-header">
          <text class="map-modal-title">选择位置</text>
          <text class="map-modal-close" @click="closeLocationPicker">×</text>
        </view>
        
        <view class="map-search-bar">
          <input 
            class="map-search-input" 
            v-model="searchKeyword" 
            placeholder="搜索地点" 
            confirm-type="search"
            @input="onMapSearchInput"
            @confirm="searchLocation"
          />
          <button class="map-search-btn" @click="searchLocation">搜索</button>
        </view>
        
        <view class="suggest-list" v-if="mapSuggestList.length > 0">
          <scroll-view class="suggest-scroll" scroll-y>
            <view 
              v-for="(item, index) in mapSuggestList" 
              :key="index" 
              class="suggest-item"
              @click="selectSuggest(item)"
            >
              <view class="suggest-name">{{ item.name }}</view>
              <view class="suggest-address">{{ item.address }}</view>
            </view>
          </scroll-view>
        </view>
        
        <map 
          id="mapPicker" 
          class="map-view" 
          :latitude="mapCenter.latitude" 
          :longitude="mapCenter.longitude"
          :markers="markers"
          :show-location="true"
          :enable-zoom="true"
          :enable-scroll="true"
          @tap="onMapTap"
        ></map>
        
        <view class="poi-panel" v-if="nearbyPois.length > 0">
          <view class="poi-header">
            <text class="poi-title">附近地址</text>
            <text class="poi-close" @click="nearbyPois = []">收起</text>
          </view>
          <scroll-view class="poi-list" scroll-y>
            <view 
              v-for="(poi, index) in nearbyPois" 
              :key="index" 
              class="poi-item"
              @click="selectPoi(poi)"
            >
              <view class="poi-name">{{ poi.name }}</view>
              <view class="poi-address">{{ poi.address }}</view>
            </view>
          </scroll-view>
        </view>
        
        <view class="map-selected-info" v-if="selectedLocation">
          <view class="selected-title">已选位置</view>
          <view class="selected-address">{{ selectedLocation.address }}</view>
        </view>
        
        <view class="map-modal-footer">
          <button class="map-cancel-btn" @click="closeLocationPicker">取消</button>
          <button class="map-confirm-btn" @click="confirmLocation">确认</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      categoryList: ["全部酒店", "度假型酒店", "商务型酒店", "公寓型酒店", "连锁酒店"],
      activeTab: 0,
      hotelList: [],
      keyword: "",
      loading: false,
      
      // 搜索建议相关
      showSuggest: false,
      suggestList: [],
      
      // 位置相关
      showLocationPicker: false,
      searchKeyword: '',
      mapSuggestList: [],
      mapCenter: {
        latitude: 26.0822,
        longitude: 119.2955
      },
      markers: [],
      selectedLocation: null,
      nearbyPois: [],
      currentLocation: {
        name: '',
        lat: null,
        lng: null
      },
      
      // 排序相关
      currentSort: '',
      sortOrder: 'asc',
      
      originalHotelList: [],
      
      // 滚动区域高度
      scrollHeight: 0
    }
  },

  onLoad() {
    this.getUserLocation()
    this.loadData()
    this.calcScrollHeight()
  },

  onReady() {
    this.calcScrollHeight()
  },

  // 点击其他地方关闭建议
  onPageTap() {
    this.showSuggest = false
  },

  methods: {
    // 计算滚动区域高度
    calcScrollHeight() {
      const systemInfo = uni.getSystemInfoSync()
      const windowHeight = systemInfo.windowHeight
      const windowWidth = systemInfo.windowWidth
      
      const rpxToPx = (rpx) => {
        return rpx * windowWidth / 750
      }
      
      const totalHeaderHeight = rpxToPx(280)
      this.scrollHeight = windowHeight - totalHeaderHeight - 20
      
      console.log('滚动区域高度:', this.scrollHeight)
    },

    getUserLocation() {
      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          this.currentLocation = {
            name: '我的位置',
            lat: res.latitude,
            lng: res.longitude
          }
          this.loadData()
        },
        fail: () => {
          console.log('获取位置失败')
        }
      })
    },

    // 加载全部酒店列表（过滤掉已停业的酒店）
    loadData() {
      this.loading = true
      let cat = this.categoryList[this.activeTab]
      let url = "http://localhost:8080/api/hotels"
      if (cat !== "全部酒店") url += "/category/" + cat
      
      console.log('请求URL:', url)

      uni.request({
        url,
        method: 'GET',
        success: (res) => {
          console.log('接口返回数据:', res.data)
          
          let hotelData = []
          if (res.data && res.data.code === 200) {
            hotelData = res.data.data || []
          } else if (Array.isArray(res.data)) {
            hotelData = res.data
          } else {
            hotelData = []
          }
          
          // 过滤掉状态为"已停业"的酒店
          const availableHotels = hotelData.filter(item => item.status !== '已停业')
          
          this.originalHotelList = availableHotels.map(item => {
            const distanceValue = this.calculateDistanceValue(item.latitude, item.longitude)
            return {
              id: item.id,
              name: item.name,
              starLevel: item.starLevel,
              price: item.price,
              category: item.category,
              rating: item.avgRating || 0,
              reviewCount: item.reviewCount || 0,
              latitude: item.latitude,
              longitude: item.longitude,
              image: this.getImageUrl(item.coverImage),
              distanceText: this.formatDistance(distanceValue),
              distanceValue: distanceValue
            }
          })
          
          this.applySort()
        },
        fail: (err) => {
          console.error('请求失败:', err)
          uni.showToast({ title: '加载失败', icon: 'none' })
        },
        complete: () => {
          this.loading = false
        }
      })
    },

    // 实时搜索建议（输入时触发）- 只显示营业中的酒店
    onSearchInput(e) {
      const keyword = e.detail.value
      this.keyword = keyword
      
      if (!keyword.trim()) {
        this.suggestList = []
        this.showSuggest = false
        return
      }
      
      // 调用后端搜索接口获取建议
      uni.request({
        url: 'http://localhost:8080/api/hotels/search',
        method: 'GET',
        data: { keyword: keyword.trim() },
        success: (res) => {
          let hotelData = []
          if (res.data && res.data.code === 200) {
            hotelData = res.data.data || []
          } else if (Array.isArray(res.data)) {
            hotelData = res.data
          }
          
          // 过滤掉已停业的酒店
          const availableHotels = hotelData.filter(item => item.status !== '已停业')
          
          // 最多显示5条建议
          this.suggestList = availableHotels.slice(0, 5).map(item => ({
            id: item.id,
            name: item.name,
            category: item.category,
            price: item.price,
            starLevel: item.starLevel,
            image: this.getImageUrl(item.coverImage)
          }))
          this.showSuggest = this.suggestList.length > 0
        },
        fail: (err) => {
          console.error('获取搜索建议失败:', err)
        }
      })
    },

    // 点击搜索按钮或回车
    onSearchConfirm() {
      this.showSuggest = false
      if (this.keyword.trim()) {
        // 如果有搜索结果，跳转到第一个结果的详情页
        if (this.suggestList.length > 0) {
          this.goHotelDetail(this.suggestList[0].id, this.suggestList[0].name)
        } else {
          uni.showToast({ title: '未找到相关酒店', icon: 'none' })
        }
      }
    },

    // 跳转酒店详情页
    goHotelDetail(id, name) {
      this.showSuggest = false
      uni.navigateTo({
        url: "/pages/hotelDetail/hotelDetail?id=" + id
      })
    },

    calculateDistanceValue(lat, lng) {
      if (!this.currentLocation.lat || !this.currentLocation.lng || !lat || !lng) {
        return null
      }
      
      const R = 6371
      const dLat = this.toRad(lat - this.currentLocation.lat)
      const dLng = this.toRad(lng - this.currentLocation.lng)
      const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(this.toRad(this.currentLocation.lat)) * Math.cos(this.toRad(lat)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
      const distance = R * c
      
      return distance
    },

    formatDistance(distanceKm) {
      if (distanceKm === null) return null
      if (distanceKm < 1) {
        return `${Math.round(distanceKm * 1000)}m`
      }
      return `${distanceKm.toFixed(1)}km`
    },

    toRad(deg) {
      return deg * Math.PI / 180
    },

    getImageUrl(path) {
      if (!path) return '/static/default-hotel.png'
      if (path.startsWith('http')) return path
      if (path.startsWith('/file')) return 'http://localhost:8080' + path
      if (path.startsWith('/uploads')) return 'http://localhost:8080' + path
      return 'http://localhost:8080' + path
    },

    applySort() {
      let sortedList = [...this.originalHotelList]
      
      if (this.currentSort === 'price') {
        sortedList.sort((a, b) => {
          const priceA = a.price || 0
          const priceB = b.price || 0
          return this.sortOrder === 'asc' ? priceA - priceB : priceB - priceA
        })
      } else if (this.currentSort === 'distance') {
        sortedList.sort((a, b) => {
          const distA = a.distanceValue !== null ? a.distanceValue : Infinity
          const distB = b.distanceValue !== null ? b.distanceValue : Infinity
          return this.sortOrder === 'asc' ? distA - distB : distB - distA
        })
      } else if (this.currentSort === 'rating') {
        sortedList.sort((a, b) => {
          const ratingA = a.rating || 0
          const ratingB = b.rating || 0
          return this.sortOrder === 'asc' ? ratingA - ratingB : ratingB - ratingA
        })
      }
      
      this.hotelList = sortedList
    },

    changeTab(index) {
      this.activeTab = index
      this.loadData()
    },

    changeSort(type) {
      if (this.currentSort === type) {
        this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc'
      } else {
        this.currentSort = type
        this.sortOrder = 'asc'
      }
      
      if (type === 'distance' && (!this.currentLocation.lat || !this.currentLocation.lng)) {
        uni.showModal({
          title: '提示',
          content: '请先选择您的位置',
          confirmText: '去选择',
          success: (res) => {
            if (res.confirm) {
              this.openLocationPicker()
            }
          }
        })
        return
      }
      
      this.applySort()
    },

    openLocationPicker() {
      this.showLocationPicker = true
      this.searchKeyword = ''
      this.mapSuggestList = []
      this.nearbyPois = []
      
      if (this.currentLocation.lat && this.currentLocation.lng) {
        this.mapCenter = {
          latitude: this.currentLocation.lat,
          longitude: this.currentLocation.lng
        }
        this.markers = [{
          id: Date.now(),
          latitude: this.currentLocation.lat,
          longitude: this.currentLocation.lng,
          title: '我的位置',
          width: 30,
          height: 30
        }]
        this.selectedLocation = {
          address: this.currentLocation.name,
          lat: this.currentLocation.lat,
          lng: this.currentLocation.lng
        }
      }
    },

    closeLocationPicker() {
      this.showLocationPicker = false
    },

    onMapSearchInput(e) {
      const keyword = e.detail.value
      this.searchKeyword = keyword
      
      if (!keyword.trim()) {
        this.mapSuggestList = []
        return
      }
      
      uni.request({
        url: 'http://localhost:8080/api/map/search',
        method: 'GET',
        data: { keyword: keyword },
        success: (res) => {
          if (res.data && res.data.success) {
            this.mapSuggestList = res.data.data || []
          } else {
            this.mapSuggestList = []
          }
        }
      })
    },

    searchLocation() {
      if (!this.searchKeyword.trim()) {
        uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
        return
      }
      
      uni.showLoading({ title: '搜索中...' })
      
      uni.request({
        url: 'http://localhost:8080/api/map/geocode',
        method: 'GET',
        data: { address: this.searchKeyword },
        success: (res) => {
          uni.hideLoading()
          if (res.data && res.data.success) {
            const { latitude, longitude, address } = res.data
            this.mapCenter = { latitude, longitude }
            this.markers = [{
              id: Date.now(),
              latitude,
              longitude,
              title: address,
              width: 30,
              height: 30
            }]
            this.selectedLocation = { address, lat: latitude, lng: longitude }
            this.getNearbyAddresses(latitude, longitude)
          } else {
            uni.showToast({ title: '未找到该地点', icon: 'none' })
          }
        }
      })
    },

    selectSuggest(item) {
      if (item.latitude && item.longitude) {
        this.mapCenter = {
          latitude: item.latitude,
          longitude: item.longitude
        }
        this.markers = [{
          id: Date.now(),
          latitude: item.latitude,
          longitude: item.longitude,
          title: item.name,
          width: 30,
          height: 30
        }]
        this.selectedLocation = {
          address: item.address || item.name,
          lat: item.latitude,
          lng: item.longitude
        }
        this.mapSuggestList = []
        this.getNearbyAddresses(item.latitude, item.longitude)
      } else {
        this.searchKeyword = item.name
        this.mapSuggestList = []
        this.searchLocation()
      }
    },

    onMapTap(e) {
      const { latitude, longitude } = e.detail
      if (!latitude || !longitude) return
      
      this.markers = [{
        id: Date.now(),
        latitude,
        longitude,
        title: '选中的位置',
        width: 30,
        height: 30
      }]
      
      this.getNearbyAddresses(latitude, longitude)
    },

    getNearbyAddresses(lat, lng) {
      uni.request({
        url: 'http://localhost:8080/api/map/reverse-geocode',
        method: 'GET',
        data: { lat, lng },
        success: (res) => {
          if (res.data && res.data.success) {
            this.nearbyPois = res.data.nearbyPois || []
            if (res.data.address) {
              this.selectedLocation = {
                address: res.data.address,
                lat: lat,
                lng: lng
              }
            }
          }
        }
      })
    },

    selectPoi(poi) {
      this.selectedLocation = {
        address: poi.address || poi.name,
        lat: this.mapCenter.latitude,
        lng: this.mapCenter.longitude
      }
      this.nearbyPois = []
      uni.showToast({ title: '已选择：' + (poi.name), icon: 'success' })
    },

    confirmLocation() {
      if (this.selectedLocation && this.selectedLocation.address) {
        this.currentLocation = {
          name: this.selectedLocation.address,
          lat: this.selectedLocation.lat,
          lng: this.selectedLocation.lng
        }
        uni.showToast({ title: '位置已选择', icon: 'success' })
        this.closeLocationPicker()
        
        this.originalHotelList = this.originalHotelList.map(item => {
          const distanceValue = this.calculateDistanceValue(item.latitude, item.longitude)
          return {
            ...item,
            distanceText: this.formatDistance(distanceValue),
            distanceValue: distanceValue
          }
        })
        
        if (this.currentSort === 'distance') {
          this.applySort()
        }
      } else {
        uni.showToast({ title: '请先在地图上选择位置', icon: 'none' })
      }
    }
  }
}
</script>

<style scoped>
/* 禁止页面整体滚动 */
page {
  background: #f5f5f5;
  height: 100%;
  overflow: hidden;
}

.container { 
  width: 100%; 
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 固定头部区域 */
.header {
  background: #fff;
  padding: 25rpx 0;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
  flex-shrink: 0;
}

/* 固定筛选栏 */
.filter-bar {
  background: #fff;
  padding: 20rpx 10rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #eee;
  flex-shrink: 0;
}

.location-selector {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  background: #f5f5f5;
  border-radius: 40rpx;
  max-width: 300rpx;
}

.location-icon {
  font-size: 28rpx;
}

.location-text {
  font-size: 26rpx;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.arrow-down {
  font-size: 20rpx;
  color: #999;
}

.sort-options {
  display: flex;
  gap: 30rpx;
}

.sort-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 26rpx;
  color: #666;
  padding: 8rpx 0;
}

.sort-item.active {
  color: #1677ff;
  font-weight: 500;
}

.sort-arrows {
  display: flex;
  flex-direction: column;
  margin-left: 4rpx;
}

.arrow-up, .arrow-down-sort {
  font-size: 16rpx;
  line-height: 1;
  color: #ccc;
}

.arrow-up.active, .arrow-down-sort.active {
  color: #1677ff;
}

/* 搜索区域 - 带下拉建议 */
.search-box {
  background: #fff;
  flex-shrink: 0;
  position: relative;
  z-index: 100;
}

.search-input-wrapper {
  display: flex;
  padding: 20rpx;
}

.search-input {
  flex: 1;
  height: 70rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx 0 0 8rpx;
  padding-left: 20rpx;
  font-size: 28rpx;
}

.search-btn {
  height: 70rpx;
  background: #1677ff;
  color: #fff;
  border: none;
  padding: 0 25rpx;
  font-size: 28rpx;
  border-radius: 0 8rpx 8rpx 0;
}

/* 搜索建议下拉列表 */
.suggest-dropdown {
  position: absolute;
  top: 110rpx;
  left: 20rpx;
  right: 20rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.15);
  z-index: 200;
  max-height: 500rpx;
  overflow-y: auto;
}

.suggest-item-dropdown {
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.suggest-item-dropdown:active {
  background-color: #f5f5f5;
}

.suggest-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  display: block;
}

.suggest-info {
  display: flex;
  justify-content: space-between;
  margin-top: 8rpx;
}

.suggest-category {
  font-size: 24rpx;
  color: #999;
}

.suggest-price {
  font-size: 26rpx;
  color: #ff6b35;
  font-weight: bold;
}

/* 主内容区域 */
.main-wrap { 
  display: flex; 
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* 左侧菜单 */
.left-tab { 
  width: 220rpx; 
  background: #fff;
  flex-shrink: 0;
  overflow-y: auto;
}

.tab-item {
  padding: 30rpx 20rpx;
  text-align: center;
  font-size: 28rpx;
  border-bottom: 1rpx solid #f2f2f2;
}

.tab-item.active {
  color: #1677ff;
  background: #f0f7ff;
  border-left: 6rpx solid #1677ff;
}

/* 右侧酒店列表 */
.right-list { 
  flex: 1;
  padding: 20rpx;
}

.card {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.card-img { width: 100%; height: 300rpx; }
.card-info { padding: 20rpx; }
.card-name { font-size: 30rpx; font-weight: bold; display: block; }
.card-desc { font-size: 24rpx; color: #999; display: block; margin-top: 8rpx; }
.distance-text { color: #1677ff; }
.rating-row {
  display: flex;
  align-items: center;
  margin-top: 8rpx;
}
.rating-star { font-size: 24rpx; }
.rating-score { font-size: 24rpx; color: #ff6b35; margin-left: 4rpx; font-weight: bold; }
.card-price { font-size: 28rpx; color: #ff6b35; display: block; margin-top: 12rpx; font-weight: bold; }
.empty-state, .loading-state {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}

/* 地图弹窗样式 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.map-modal-container {
  width: 100%;
  height: 85vh;
  background-color: #fff;
  border-radius: 30rpx 30rpx 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.map-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #eee;
}

.map-modal-title {
  font-size: 34rpx;
  font-weight: bold;
}

.map-modal-close {
  font-size: 48rpx;
  color: #999;
}

.map-search-bar {
  display: flex;
  padding: 20rpx 30rpx;
  gap: 15rpx;
  border-bottom: 1px solid #eee;
}

.map-search-input {
  flex: 1;
  padding: 20rpx;
  border: 1px solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  background-color: #f5f5f5;
}

.map-search-btn {
  background-color: #1677ff;
  color: #fff;
  font-size: 26rpx;
  padding: 0 30rpx;
  border-radius: 12rpx;
  border: none;
}

.suggest-list {
  max-height: 300rpx;
  background-color: #fff;
  margin: 0 30rpx;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  border: 1px solid #eee;
  border-top: none;
  overflow: hidden;
  z-index: 20;
}

.suggest-scroll {
  max-height: 300rpx;
}

.suggest-item {
  padding: 20rpx 30rpx;
  border-bottom: 1px solid #eee;
}

.suggest-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.suggest-address {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.map-view {
  flex: 1;
  width: 100%;
  min-height: 300rpx;
}

.poi-panel {
  max-height: 40%;
  background-color: #fff;
  border-top: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.poi-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  border-bottom: 1px solid #eee;
  background-color: #f9f9f9;
}

.poi-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.poi-close {
  font-size: 26rpx;
  color: #999;
}

.poi-list {
  max-height: 400rpx;
  overflow-y: auto;
}

.poi-item {
  padding: 20rpx 30rpx;
  border-bottom: 1px solid #f0f0f0;
}

.poi-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 6rpx;
}

.poi-address {
  font-size: 24rpx;
  color: #999;
}

.map-selected-info {
  padding: 20rpx 30rpx;
  background-color: #f5f5f5;
  border-top: 1px solid #eee;
}

.selected-title {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.selected-address {
  font-size: 28rpx;
  color: #333;
  word-break: break-all;
}

.map-modal-footer {
  display: flex;
  padding: 20rpx 30rpx;
  gap: 20rpx;
  border-top: 1px solid #eee;
  padding-bottom: 50rpx;
}

.map-cancel-btn, .map-confirm-btn {
  flex: 1;
  border-radius: 50rpx;
  padding: 24rpx;
  font-size: 28rpx;
  border: none;
}

.map-cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.map-confirm-btn {
  background-color: #1677ff;
  color: #fff;
}
</style>