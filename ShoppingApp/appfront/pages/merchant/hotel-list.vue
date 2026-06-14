<template>
  <view class="container">
    <view class="header-bar">
      <view class="title">酒店管理</view>
      <button class="add-btn" @click="openAddModal">+ 添加酒店</button>
    </view>

    <!-- 酒店列表 -->
    <view class="hotel-list">
      <view class="hotel-card" v-for="hotel in hotelList" :key="hotel.id">
        <image class="hotel-cover" :src="getImageUrl(hotel.coverImage)" mode="aspectFill"></image>
        <view class="hotel-info">
          <text class="hotel-name">{{ hotel.name }}</text>
          <text class="hotel-address">📍 {{ hotel.address || '暂无地址' }}</text>
          <view class="hotel-stats">
            <text class="star-level">{{ getStarText(hotel.starLevel) }}</text>
            <text class="price">¥{{ hotel.price }}/晚</text>
          </view>
          
          <!-- 状态显示和切换 -->
          <view class="hotel-status-row">
            <text class="status-label">状态：</text>
            <text class="status-value" :class="hotel.status === '营业中' ? 'status-open' : 'status-closed'">
              {{ hotel.status || '营业中' }}
            </text>
            <button class="status-toggle-btn" @click="toggleHotelStatus(hotel)">
              {{ hotel.status === '营业中' ? '设为停业' : '设为营业' }}
            </button>
          </view>
          
          <view class="hotel-category" v-if="hotel.category">
            <text class="category-tag">{{ hotel.category }}</text>
          </view>
        </view>
        <view class="hotel-actions">
          <button class="edit-btn" @click="openEditModal(hotel)">编辑</button>
          <button class="room-btn" @click="manageRooms(hotel)">房型管理</button>
          <button class="delete-btn" @click="deleteHotel(hotel.id)">删除</button>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="hotelList.length === 0 && !loading">
      <text>暂无酒店，点击上方按钮添加</text>
    </view>

    <!-- 加载中 -->
    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>

    <!-- ========== 添加/编辑酒店弹窗 ========== -->
    <view class="modal-mask" v-if="showModal" @click="closeModal">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isEdit ? '编辑酒店' : '添加酒店' }}</text>
          <text class="modal-close" @click="closeModal">×</text>
        </view>

        <scroll-view class="modal-body" scroll-y>
          <view class="form-item">
            <text class="label">酒店名称 <text class="required">*</text></text>
            <input class="input" v-model="form.name" placeholder="请输入酒店名称" />
          </view>

          <view class="form-item">
            <text class="label">酒店分类 <text class="required">*</text></text>
            <picker :range="categoryOptions" @change="onCategoryChange">
              <view class="picker">{{ form.category || '请选择' }}</view>
            </picker>
          </view>

          <view class="form-item">
            <text class="label">酒店地址 <text class="required">*</text></text>
            <view class="address-row">
              <input class="input address-input" v-model="form.address" placeholder="请输入详细地址" />
              <button class="map-btn" @click="openMapPicker">🗺️ 百度地图选点</button>
            </view>
          </view>

          <view class="form-item">
            <text class="label">星级 <text class="required">*</text></text>
            <view class="star-box">
              <text v-for="i in 5" :key="i" class="star" :class="{ active: form.starLevel >= i }" @click="form.starLevel = i">★</text>
            </view>
          </view>

          <view class="form-item">
            <text class="label">价格（元/晚） <text class="required">*</text></text>
            <input class="input" type="digit" v-model="form.price" placeholder="请输入价格" />
          </view>

          <view class="form-item">
            <text class="label">总房间数</text>
            <input class="input" type="number" v-model="form.totalRooms" placeholder="请输入总房间数" />
          </view>

          <view class="form-item">
            <text class="label">封面图</text>
            <view class="upload-box" @click="uploadCover">
              <image v-if="form.coverImage" :src="getImageUrl(form.coverImage)" class="upload-image" mode="aspectFill"></image>
              <view v-else class="upload-placeholder">
                <text class="upload-icon">+</text>
                <text class="upload-text">点击上传</text>
              </view>
            </view>
          </view>

          <view class="form-item">
            <text class="label">详情图（可多张）</text>
            <view class="detail-upload-list">
              <view v-for="(img, idx) in form.detailImages" :key="idx" class="detail-image-item">
                <image :src="getImageUrl(img.imageUrl || img)" class="detail-image" mode="aspectFill"></image>
                <text class="detail-delete" @click="removeDetailImage(idx)">×</text>
              </view>
              <view class="upload-btn" @click="uploadDetailImage">
                <text class="upload-icon">+</text>
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="cancel-btn" @click="closeModal">取消</button>
          <button class="confirm-btn" @click="submitHotel">保存</button>
        </view>
      </view>
    </view>

    <!-- ========== 房型管理弹窗 ========== -->
    <view class="modal-mask" v-if="showRoomModal" @click="closeRoomModal">
      <view class="room-modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">房型管理 - {{ currentHotel ? currentHotel.name : '' }}</text>
          <button class="add-room-btn" @click="openAddRoomModal">+ 添加房型</button>
          <text class="modal-close" @click="closeRoomModal">×</text>
        </view>

        <scroll-view class="modal-body" scroll-y>
          <view class="room-list">
            <view class="room-card" v-for="room in roomList" :key="room.id">
              <view class="room-info">
                <text class="room-name">{{ room.typeName }}</text>
                <text class="room-desc">{{ room.size }} | {{ room.bedType }} | {{ room.windowStatus }}</text>
                <view class="room-breakfast" v-if="room.breakfast">🍳 {{ room.breakfast }}</view>
                <view class="room-stats">
                  <text class="room-price">¥{{ room.price }}/晚</text>
                  <text class="room-stock">库存: {{ room.availableCount }}/{{ room.totalCount }}</text>
                </view>
              </view>
              <view class="room-actions">
                <button class="room-edit-btn" @click="openEditRoomModal(room)">编辑</button>
                <button class="room-delete-btn" @click="deleteRoom(room.id)">删除</button>
              </view>
            </view>
          </view>
          
          <view class="empty-state" v-if="roomList.length === 0">
            <text>暂无房型，点击上方按钮添加</text>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="cancel-btn" @click="closeRoomModal">关闭</button>
        </view>
      </view>
    </view>

    <!-- ========== 添加/编辑房型弹窗 ========== -->
    <view class="modal-mask" v-if="showRoomFormModal" @click="closeRoomFormModal">
      <view class="room-form-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isEditRoom ? '编辑房型' : '添加房型' }}</text>
          <text class="modal-close" @click="closeRoomFormModal">×</text>
        </view>

        <scroll-view class="modal-body" scroll-y>
          <view class="form-item">
            <text class="label">房型名称 <text class="required">*</text></text>
            <input class="input" v-model="roomForm.typeName" placeholder="如：大床房、双床房" />
          </view>

          <view class="form-item">
            <text class="label">房间价格 <text class="required">*</text></text>
            <input class="input" type="digit" v-model="roomForm.price" placeholder="请输入价格" />
          </view>

          <view class="form-item">
            <text class="label">总数量 <text class="required">*</text></text>
            <input class="input" type="number" v-model="roomForm.totalCount" placeholder="请输入房间总数量" />
          </view>

          <view class="form-item">
            <text class="label">可预订数量 <text class="required">*</text></text>
            <input class="input" type="number" v-model="roomForm.availableCount" placeholder="请输入可预订数量" />
          </view>

          <view class="form-item">
            <text class="label">房间面积</text>
            <input class="input" v-model="roomForm.size" placeholder="如：25㎡" />
          </view>

          <view class="form-item">
            <text class="label">床型</text>
            <picker :range="bedOptions" @change="onBedChange">
              <view class="picker">{{ roomForm.bedType || '请选择床型' }}</view>
            </picker>
          </view>

          <view class="form-item">
            <text class="label">窗户</text>
            <picker :range="windowOptions" @change="onWindowChange">
              <view class="picker">{{ roomForm.windowStatus || '请选择' }}</view>
            </picker>
          </view>

          <view class="form-item">
            <text class="label">早餐</text>
            <picker :range="breakfastOptions" @change="onBreakfastChange">
              <view class="picker">{{ roomForm.breakfast || '请选择' }}</view>
            </picker>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="cancel-btn" @click="closeRoomFormModal">取消</button>
          <button class="confirm-btn" @click="submitRoom">保存</button>
        </view>
      </view>
    </view>

    <!-- ========== 百度地图选点弹窗 ========== -->
    <view class="modal-mask" v-if="showMapPicker" @click="closeMapPicker">
      <view class="map-modal-container" @click.stop>
        <view class="map-modal-header">
          <text class="map-modal-title">百度地图 - 选择酒店位置</text>
          <text class="map-modal-close" @click="closeMapPicker">×</text>
        </view>
        
        <!-- 搜索框 -->
        <view class="map-search-bar">
          <input 
            class="map-search-input" 
            v-model="searchKeyword" 
            placeholder="搜索地点（如：福州三坊七巷）" 
            confirm-type="search"
            @input="onSearchInput"
            @confirm="searchLocation"
          />
          <button class="map-search-btn" @click="searchLocation">搜索</button>
        </view>
        
        <!-- 搜索建议列表 -->
        <view class="suggest-list" v-if="suggestList.length > 0">
          <scroll-view class="suggest-scroll" scroll-y>
            <view 
              v-for="(item, index) in suggestList" 
              :key="index" 
              class="suggest-item"
              @click="selectSuggest(item)"
            >
              <view class="suggest-name">{{ item.name }}</view>
              <view class="suggest-address">{{ item.address }}</view>
            </view>
          </scroll-view>
        </view>
        
        <!-- 地图组件 -->
        <map 
          id="mapPicker" 
          class="map-view" 
          :latitude="mapCenter.latitude" 
          :longitude="mapCenter.longitude"
          :markers="markers"
          :show-location="false"
          :enable-zoom="true"
          :enable-scroll="true"
          @tap="onMapTap"
        ></map>
        
        <!-- 附近地址列表（点击地图后显示） -->
        <view class="poi-panel" v-if="nearbyPois.length > 0">
          <view class="poi-header">
            <text class="poi-title">附近地址（点击选择）</text>
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
        
        <!-- 当前选中的位置 -->
        <view class="map-selected-info" v-if="selectedLocation">
          <view class="selected-title">已选位置</view>
          <view class="selected-address">{{ selectedLocation.address }}</view>
        </view>
        
        <view class="map-modal-footer">
          <button class="map-cancel-btn" @click="closeMapPicker">取消</button>
          <button class="map-confirm-btn" @click="confirmLocation">确认选择</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      merchantId: null,
      hotelList: [],
      loading: false,
      showModal: false,
      isEdit: false,
      showMapPicker: false,
      
      // 房型管理相关
      showRoomModal: false,
      showRoomFormModal: false,
      isEditRoom: false,
      currentHotel: null,
      roomList: [],
      roomForm: {
        id: null,
        hotelId: null,
        typeName: '',
        price: '',
        totalCount: '',
        availableCount: '',
        size: '',
        bedType: '',
        windowStatus: '',
        breakfast: ''
      },
      
      bedOptions: ['大床', '双床', '单人床', '榻榻米', '圆床'],
      windowOptions: ['有窗', '无窗'],
      breakfastOptions: ['含早', '不含早'],
      
      form: {
        id: null,
        name: '',
        category: '',
        address: '',
        starLevel: 3,
        price: '',
        totalRooms: '',
        coverImage: '',
        detailImages: [],
        latitude: null,
        longitude: null
      },
      
      categoryOptions: ['度假型酒店', '商务型酒店', '公寓型酒店', '连锁酒店'],
      
      // 地图相关
      searchKeyword: '',
      suggestList: [],
      mapCenter: {
        latitude: 26.0822,
        longitude: 119.2955
      },
      markers: [],
      selectedLocation: null,
      nearbyPois: []
    };
  },
  
  onShow() {
    const userInfo = uni.getStorageSync('userInfo');
    if (userInfo && userInfo.id) {
      this.merchantId = userInfo.id;
      console.log('商家ID:', this.merchantId);
      this.loadHotels();
    } else {
      uni.showToast({ title: '请先登录', icon: 'none' });
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/login-register/login-register' });
      }, 1500);
    }
  },
  
  methods: {
    getStarText(level) {
      if (!level) return '☆☆☆☆☆';
      return '★'.repeat(level) + '☆'.repeat(5 - level);
    },
    
    getImageUrl(path) {
      if (!path) return '/static/default-hotel.png';
      if (path.startsWith('http')) return path;
      if (path.startsWith('/file')) return 'http://localhost:8080' + path;
      return path;
    },
    
    loadHotels() {
      if (!this.merchantId) {
        console.log('未获取到商家ID');
        return;
      }
      
      this.loading = true;
      uni.request({
        url: `http://localhost:8080/api/hotels/merchant/${this.merchantId}`,
        method: 'GET',
        success: (res) => {
          console.log('酒店列表返回:', res.data);
          if (res.data && res.data.code === 200) {
            this.hotelList = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            this.hotelList = res.data;
          } else {
            this.hotelList = [];
          }
        },
        fail: (err) => {
          console.error('获取酒店列表失败', err);
          this.hotelList = [];
        },
        complete: () => {
          this.loading = false;
        }
      });
    },
    
    // ========== 酒店状态管理 ==========
    toggleHotelStatus(hotel) {
      const newStatus = hotel.status === '营业中' ? '已停业' : '营业中';
      const confirmTitle = hotel.status === '营业中' ? '确认停业' : '确认恢复营业';
      const confirmContent = hotel.status === '营业中' 
        ? `确定要将"${hotel.name}"设为停业吗？停业后用户将无法预订。` 
        : `确定要将"${hotel.name}"恢复营业吗？`;
      
      uni.showModal({
        title: confirmTitle,
        content: confirmContent,
        confirmColor: hotel.status === '营业中' ? '#ff6b6b' : '#52c41a',
        success: (res) => {
          if (res.confirm) {
            this.updateHotelStatus(hotel.id, newStatus);
          }
        }
      });
    },
    
    updateHotelStatus(hotelId, newStatus) {
      uni.showLoading({ title: '更新中...' });
      
      uni.request({
        url: `http://localhost:8080/api/hotels/${hotelId}`,
        method: 'GET',
        success: (getRes) => {
          let hotelData = getRes.data;
          if (getRes.data && getRes.data.code === 200) {
            hotelData = getRes.data.data;
          }
          
          if (!hotelData) {
            uni.hideLoading();
            uni.showToast({ title: '获取酒店信息失败', icon: 'none' });
            return;
          }
          
          hotelData.status = newStatus;
          
          uni.request({
            url: `http://localhost:8080/api/hotels/${hotelId}`,
            method: 'PUT',
            header: { 'Content-Type': 'application/json' },
            data: hotelData,
            success: (res) => {
              uni.hideLoading();
              if (res.data && res.data.code === 200) {
                uni.showToast({ 
                  title: newStatus === '营业中' ? '已恢复营业' : '已设为停业', 
                  icon: 'success' 
                });
                this.loadHotels();
              } else {
                uni.showToast({ title: '操作失败', icon: 'none' });
              }
            },
            fail: (err) => {
              uni.hideLoading();
              console.error('更新状态失败', err);
              uni.showToast({ title: '网络错误', icon: 'none' });
            }
          });
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('获取酒店信息失败', err);
          uni.showToast({ title: '获取酒店信息失败', icon: 'none' });
        }
      });
    },
    
    // ========== 房型管理 ==========
    manageRooms(hotel) {
      this.currentHotel = hotel;
      this.showRoomModal = true;
      this.loadRooms(hotel.id);
    },
    
    loadRooms(hotelId) {
      uni.request({
        url: 'http://localhost:8080/api/room-types/hotel/' + hotelId,
        method: 'GET',
        success: (res) => {
          if (res.data && res.data.code === 200) {
            this.roomList = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            this.roomList = res.data;
          } else {
            this.roomList = [];
          }
        },
        fail: (err) => {
          console.error('获取房型失败', err);
          this.roomList = [];
        }
      });
    },
    
    openAddRoomModal() {
      this.isEditRoom = false;
      this.roomForm = {
        id: null,
        hotelId: this.currentHotel.id,
        typeName: '',
        price: '',
        totalCount: '',
        availableCount: '',
        size: '',
        bedType: '',
        windowStatus: '',
        breakfast: ''
      };
      this.showRoomFormModal = true;
    },
    
    openEditRoomModal(room) {
      this.isEditRoom = true;
      this.roomForm = {
        id: room.id,
        hotelId: room.hotelId || this.currentHotel.id,
        typeName: room.typeName,
        price: room.price,
        totalCount: room.totalCount,
        availableCount: room.availableCount,
        size: room.size || '',
        bedType: room.bedType || '',
        windowStatus: room.windowStatus || '',
        breakfast: room.breakfast || ''
      };
      this.showRoomFormModal = true;
    },
    
    onBedChange(e) {
      this.roomForm.bedType = this.bedOptions[e.detail.value];
    },
    
    onWindowChange(e) {
      this.roomForm.windowStatus = this.windowOptions[e.detail.value];
    },
    
    onBreakfastChange(e) {
      this.roomForm.breakfast = this.breakfastOptions[e.detail.value];
    },
    
    submitRoom() {
      if (!this.roomForm.typeName) {
        uni.showToast({ title: '请输入房型名称', icon: 'none' });
        return;
      }
      if (!this.roomForm.price || this.roomForm.price <= 0) {
        uni.showToast({ title: '请输入有效价格', icon: 'none' });
        return;
      }
      if (!this.roomForm.totalCount || this.roomForm.totalCount <= 0) {
        uni.showToast({ title: '请输入总数量', icon: 'none' });
        return;
      }
      if (!this.roomForm.availableCount || this.roomForm.availableCount <= 0) {
        uni.showToast({ title: '请输入可预订数量', icon: 'none' });
        return;
      }
      
      const submitData = {
        typeName: this.roomForm.typeName,
        price: parseFloat(this.roomForm.price),
        totalCount: parseInt(this.roomForm.totalCount),
        availableCount: parseInt(this.roomForm.availableCount),
        size: this.roomForm.size || '',
        bedType: this.roomForm.bedType || '',
        windowStatus: this.roomForm.windowStatus || '',
        breakfast: this.roomForm.breakfast || '',
        hotel: {
          id: parseInt(this.roomForm.hotelId)
        }
      };
      
      let url = '';
      let method = '';
      if (this.isEditRoom) {
        url = 'http://localhost:8080/api/room-types/' + this.roomForm.id;
        method = 'PUT';
        submitData.id = this.roomForm.id;
      } else {
        url = 'http://localhost:8080/api/room-types';
        method = 'POST';
      }
      
      console.log('提交的房型数据：', submitData);
      
      uni.showLoading({ title: '保存中...' });
      
      uni.request({
        url: url,
        method: method,
        header: { 'Content-Type': 'application/json' },
        data: submitData,
        success: (res) => {
          uni.hideLoading();
          console.log('房型提交结果：', res);
          
          if (res.statusCode === 200 && res.data && res.data.code === 200) {
            uni.showToast({ title: this.isEditRoom ? '修改成功' : '添加成功', icon: 'success' });
            this.closeRoomFormModal();
            this.loadRooms(this.currentHotel.id);
          } else {
            uni.showToast({ title: (res.data && res.data.message) || '操作失败', icon: 'none' });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('提交失败', err);
          uni.showToast({ title: '网络错误，请重试', icon: 'none' });
        }
      });
    },
    
    deleteRoom(roomId) {
      uni.showModal({
        title: '确认删除',
        content: '确定要删除该房型吗？',
        confirmColor: '#ff6b6b',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '删除中...' });
            uni.request({
              url: 'http://localhost:8080/api/room-types/' + roomId,
              method: 'DELETE',
              success: () => {
                uni.showToast({ title: '删除成功', icon: 'success' });
                this.loadRooms(this.currentHotel.id);
              },
              fail: (err) => {
                console.error('删除失败', err);
                uni.showToast({ title: '删除失败', icon: 'none' });
              },
              complete: () => {
                uni.hideLoading();
              }
            });
          }
        }
      });
    },
    
    closeRoomModal() {
      this.showRoomModal = false;
      this.currentHotel = null;
      this.roomList = [];
    },
    
    closeRoomFormModal() {
      this.showRoomFormModal = false;
    },
    
    // ========== 酒店管理方法 ==========
    openAddModal() {
      this.isEdit = false;
      this.form = {
        id: null,
        name: '',
        category: '',
        address: '',
        starLevel: 3,
        price: '',
        totalRooms: '',
        coverImage: '',
        detailImages: [],
        latitude: null,
        longitude: null
      };
      this.showModal = true;
    },
    
    openEditModal(hotel) {
      this.isEdit = true;
      this.form = {
        id: hotel.id,
        name: hotel.name,
        category: hotel.category || '',
        address: hotel.address || '',
        starLevel: hotel.starLevel || 3,
        price: hotel.price,
        totalRooms: hotel.totalRooms || '',
        coverImage: hotel.coverImage || '',
        detailImages: hotel.detailImages || [],
        latitude: hotel.latitude || null,
        longitude: hotel.longitude || null
      };
      this.showModal = true;
    },
    
    closeModal() {
      this.showModal = false;
    },
    
    onCategoryChange(e) {
      this.form.category = this.categoryOptions[e.detail.value];
    },
    
    // ========== 百度地图选点方法 ==========
    openMapPicker() {
      this.showMapPicker = true;
      this.searchKeyword = '';
      this.suggestList = [];
      this.selectedLocation = null;
      this.nearbyPois = [];
      
      if (this.form.latitude && this.form.longitude) {
        this.mapCenter = {
          latitude: this.form.latitude,
          longitude: this.form.longitude
        };
        this.markers = [{
          id: Date.now(),
          latitude: this.form.latitude,
          longitude: this.form.longitude,
          title: this.form.address || '酒店位置',
          width: 30,
          height: 30
        }];
        this.selectedLocation = {
          address: this.form.address,
          lat: this.form.latitude,
          lng: this.form.longitude
        };
      } else {
        this.mapCenter = {
          latitude: 26.0822,
          longitude: 119.2955
        };
        this.markers = [];
      }
    },
    
    closeMapPicker() {
      this.showMapPicker = false;
    },
    
    onSearchInput(e) {
      const keyword = e.detail.value;
      this.searchKeyword = keyword;
      
      if (!keyword.trim()) {
        this.suggestList = [];
        return;
      }
      
      uni.request({
        url: 'http://localhost:8080/api/map/search',
        method: 'GET',
        data: { keyword: keyword, region: keyword },
        success: (res) => {
          if (res.statusCode === 200 && res.data.success) {
            this.suggestList = res.data.data || [];
          } else {
            this.suggestList = [];
          }
        },
        fail: () => {
          this.suggestList = [];
        }
      });
    },
    
    selectSuggest(item) {
      this.searchKeyword = item.name;
      this.suggestList = [];
      
      if (item.latitude && item.longitude) {
        this.mapCenter = {
          latitude: item.latitude,
          longitude: item.longitude
        };
        this.markers = [{
          id: Date.now(),
          latitude: item.latitude,
          longitude: item.longitude,
          title: item.name,
          width: 30,
          height: 30
        }];
        this.selectedLocation = {
          address: item.address || item.name,
          lat: item.latitude,
          lng: item.longitude
        };
        this.getNearbyAddresses(item.latitude, item.longitude);
      } else {
        this.searchLocationDirect(item.name);
      }
    },
    
    searchLocation() {
      if (!this.searchKeyword.trim()) {
        uni.showToast({ title: '请输入搜索关键词', icon: 'none' });
        return;
      }
      this.suggestList = [];
      this.searchLocationDirect(this.searchKeyword);
    },
    
    searchLocationDirect(address) {
      uni.showLoading({ title: '搜索中...' });
      
      uni.request({
        url: 'http://localhost:8080/api/map/geocode',
        method: 'GET',
        data: { address: address },
        success: (res) => {
          uni.hideLoading();
          if (res.statusCode === 200 && res.data.success) {
            const lat = res.data.latitude;
            const lng = res.data.longitude;
            const addr = res.data.address;
            
            this.mapCenter = { latitude: lat, longitude: lng };
            this.markers = [{
              id: Date.now(),
              latitude: lat,
              longitude: lng,
              title: addr,
              width: 30,
              height: 30
            }];
            this.selectedLocation = { address: addr, lat: lat, lng: lng };
            this.getNearbyAddresses(lat, lng);
          } else {
            uni.showToast({ title: res.data?.message || '未找到该地点', icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '搜索失败', icon: 'none' });
        }
      });
    },
    
    onMapTap(e) {
      const { latitude, longitude } = e.detail;
      if (!latitude || !longitude) return;
      
      this.markers = [{
        id: Date.now(),
        latitude: latitude,
        longitude: longitude,
        title: '选中的位置',
        width: 30,
        height: 30
      }];
      
      this.getNearbyAddresses(latitude, longitude);
    },
    
    getNearbyAddresses(lat, lng) {
      uni.showLoading({ title: '获取地址列表...' });
      
      uni.request({
        url: 'http://localhost:8080/api/map/reverse-geocode',
        method: 'GET',
        data: { lat: lat, lng: lng },
        success: (res) => {
          uni.hideLoading();
          if (res.statusCode === 200 && res.data.success) {
            this.nearbyPois = res.data.nearbyPois || [];
            if (res.data.address) {
              this.selectedLocation = {
                address: res.data.address,
                lat: lat,
                lng: lng
              };
            }
            if (this.nearbyPois.length === 0) {
              uni.showToast({ title: '该位置无附近地址，可手动输入', icon: 'none' });
            }
          } else {
            uni.showToast({ title: res.data?.message || '获取地址失败', icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    },
    
    selectPoi(poi) {
      this.selectedLocation = {
        address: poi.address || poi.name,
        lat: this.mapCenter.latitude,
        lng: this.mapCenter.longitude
      };
      this.nearbyPois = [];
      uni.showToast({ title: '已选择：' + (poi.name), icon: 'success' });
    },
    
    confirmLocation() {
      if (this.selectedLocation && this.selectedLocation.address) {
        this.form.address = this.selectedLocation.address;
        this.form.latitude = this.selectedLocation.lat;
        this.form.longitude = this.selectedLocation.lng;
        uni.showToast({ title: '地址已选择', icon: 'success' });
        this.closeMapPicker();
      } else {
        uni.showToast({ title: '请先在地图上选择位置', icon: 'none' });
      }
    },
    
    uploadCover() {
      uni.chooseImage({
        count: 1,
        success: (res) => {
          const tempFile = res.tempFilePaths[0];
          uni.showLoading({ title: '上传中...' });
          this.uploadImageToServer(tempFile, (url) => {
            this.form.coverImage = url;
            uni.hideLoading();
          });
        }
      });
    },
    
    uploadDetailImage() {
      uni.chooseImage({
        count: 9,
        success: (res) => {
          uni.showLoading({ title: '上传中...' });
          const tempFiles = res.tempFilePaths;
          let completed = 0;
          if (tempFiles.length === 0) {
            uni.hideLoading();
            return;
          }
          tempFiles.forEach(file => {
            this.uploadImageToServer(file, (url) => {
              if (url) {
                this.form.detailImages.push({ imageUrl: url });
              }
              completed++;
              if (completed === tempFiles.length) {
                uni.hideLoading();
              }
            });
          });
        }
      });
    },
    
    removeDetailImage(index) {
      this.form.detailImages.splice(index, 1);
    },
    
    uploadImageToServer(filePath, callback) {
      uni.uploadFile({
        url: 'http://localhost:8080/api/upload',
        filePath: filePath,
        name: 'file',
        success: (res) => {
          try {
            const data = JSON.parse(res.data);
            callback(data.url || data);
          } catch (e) {
            callback(res.data);
          }
        },
        fail: (err) => {
          console.error('上传失败', err);
          callback(null);
        }
      });
    },
    
    submitHotel() {
      const userInfo = uni.getStorageSync('userInfo');
      const merchantId = userInfo && userInfo.id ? userInfo.id : null;
      
      console.log('=== 提交酒店 ===');
      console.log('商家ID:', merchantId);
      
      if (!this.form.name || this.form.name.trim() === '') {
        uni.showToast({ title: '请输入酒店名称', icon: 'none' });
        return;
      }
      if (!this.form.category) {
        uni.showToast({ title: '请选择酒店分类', icon: 'none' });
        return;
      }
      if (!this.form.address || this.form.address.trim() === '') {
        uni.showToast({ title: '请输入酒店地址', icon: 'none' });
        return;
      }
      if (!this.form.price || this.form.price <= 0) {
        uni.showToast({ title: '请输入有效价格', icon: 'none' });
        return;
      }
      
      if (!merchantId) {
        uni.showToast({ title: '未获取到商家信息，请重新登录', icon: 'none' });
        return;
      }
      
      const submitData = {
        name: this.form.name,
        category: this.form.category,
        address: this.form.address,
        starLevel: parseInt(this.form.starLevel),
        price: parseFloat(this.form.price),
        totalRooms: this.form.totalRooms ? parseInt(this.form.totalRooms) : 0,
        coverImage: this.form.coverImage || '',
        latitude: this.form.latitude,
        longitude: this.form.longitude,
        merchantId: merchantId,
        status: "营业中"
      };
      
      console.log('提交数据:', JSON.stringify(submitData));
      
      let url = '';
      let method = '';
      if (this.isEdit) {
        url = 'http://localhost:8080/api/hotels/' + this.form.id;
        method = 'PUT';
      } else {
        url = 'http://localhost:8080/api/hotels';
        method = 'POST';
      }
      
      uni.showLoading({ title: '保存中...' });
      
      uni.request({
        url: url,
        method: method,
        header: { 'Content-Type': 'application/json' },
        data: submitData,
        success: (res) => {
          console.log('提交结果:', res.data);
          uni.hideLoading();
          
          if (res.data && res.data.code === 200) {
            uni.showToast({ title: this.isEdit ? '修改成功' : '添加成功', icon: 'success' });
            this.closeModal();
            this.loadHotels();
          } else {
            uni.showToast({ title: res.data?.message || '操作失败', icon: 'none' });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('提交失败:', err);
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    },
    
    deleteHotel(id) {
      if (!id) {
        console.error('删除失败：酒店ID为空');
        uni.showToast({ title: '删除失败', icon: 'none' });
        return;
      }
      
      uni.showModal({
        title: '确认删除',
        content: '确定要删除该酒店吗？删除后不可恢复。',
        confirmColor: '#ff6b6b',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '删除中...' });
            uni.request({
              url: 'http://localhost:8080/api/hotels/' + id,
              method: 'DELETE',
              success: () => {
                uni.showToast({ title: '删除成功', icon: 'success' });
                this.loadHotels();
              },
              fail: (err) => {
                console.error('删除失败', err);
                uni.showToast({ title: '删除失败', icon: 'none' });
              },
              complete: () => {
                uni.hideLoading();
              }
            });
          }
        }
      });
    }
  }
};
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
  box-sizing: border-box;
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  margin-bottom: 20rpx;
}

.title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
}

.add-btn {
  background-color: #f0e68c;
  color: #333;
  border-radius: 50rpx;
  padding: 10rpx 30rpx;
  font-size: 28rpx;
  border: none;
}

.hotel-list {
  padding-bottom: 20rpx;
}

.hotel-card {
  background-color: #fff;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.hotel-cover {
  width: 100%;
  height: 280rpx;
}

.hotel-info {
  padding: 24rpx;
}

.hotel-name {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.hotel-address {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hotel-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.star-level {
  color: #ffc107;
  font-size: 24rpx;
}

.price {
  color: #ff6b6b;
  font-size: 30rpx;
  font-weight: bold;
}

.hotel-status-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
  gap: 10rpx;
}

.status-label {
  font-size: 24rpx;
  color: #666;
}

.status-value {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.status-open {
  background-color: #e6f7e6;
  color: #52c41a;
}

.status-closed {
  background-color: #fff0f0;
  color: #ff6b6b;
}

.status-toggle-btn {
  background-color: #f0e68c;
  color: #333;
  font-size: 22rpx;
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
  border: none;
  height: auto;
  line-height: 1.4;
  margin-left: auto;
}

.category-tag {
  background-color: #f0e68c20;
  color: #d4a017;
  padding: 4rpx 20rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  display: inline-block;
}

.hotel-actions {
  display: flex;
  border-top: 1px solid #eee;
}

.edit-btn, .room-btn, .delete-btn {
  flex: 1;
  border-radius: 0;
  font-size: 28rpx;
  padding: 24rpx;
  background-color: #fff;
  border: none;
}

.edit-btn {
  color: #f0e68c;
  border-right: 1px solid #eee;
}

.room-btn {
  color: #1677ff;
  border-right: 1px solid #eee;
}

.delete-btn {
  color: #ff6b6b;
}

.room-card {
  background-color: #f9f9f9;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  padding: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #eee;
}

.room-info {
  flex: 1;
}

.room-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
}

.room-desc {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-bottom: 8rpx;
}

.room-breakfast {
  font-size: 22rpx;
  color: #52c41a;
  margin-bottom: 8rpx;
}

.room-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10rpx;
}

.room-price {
  font-size: 32rpx;
  font-weight: bold;
  color: #ff6b6b;
}

.room-stock {
  font-size: 22rpx;
  color: #999;
}

.room-actions {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.room-edit-btn, .room-delete-btn {
  padding: 15rpx 30rpx;
  border-radius: 12rpx;
  font-size: 26rpx;
  border: none;
}

.room-edit-btn {
  background-color: #f0e68c;
  color: #333;
}

.room-delete-btn {
  background-color: #ff6b6b20;
  color: #ff6b6b;
}

.add-room-btn {
  background-color: #f0e68c;
  color: #333;
  border-radius: 50rpx;
  padding: 6rpx 30rpx;
  font-size: 26rpx;
  border: none;
  margin-left: 20rpx;
}

.room-modal-container {
  width: 100%;
  max-height: 85vh;
  background-color: #fff;
  border-radius: 30rpx 30rpx 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 编辑房型弹窗容器 - 修复溢出问题 */
.room-form-container {
  width: 90%;
  max-width: 650rpx;
  max-height: 80vh;
  background-color: #fff;
  border-radius: 30rpx;
  display: flex;
  flex-direction: column;
  margin: auto;
  overflow: hidden;
  position: relative;
  top: 50%;
  transform: translateY(-50%);
}

/* 弹窗主体滚动区域 - 确保内容不溢出 */
.room-form-container .modal-body {
  flex: 1;
  padding: 30rpx;
  overflow-y: auto;
  box-sizing: border-box;
}

/* 所有表单输入框确保不溢出父容器 */
.room-form-container .form-item {
  margin-bottom: 30rpx;
  width: 100%;
  box-sizing: border-box;
}

.room-form-container .input,
.room-form-container .picker {
  width: 100%;
  padding: 24rpx;
  border: 1px solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  background-color: #fff;
  box-sizing: border-box;
}

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

.modal-container {
  width: 100%;
  max-height: 85vh;
  background-color: #fff;
  border-radius: 30rpx 30rpx 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.modal-title {
  font-size: 34rpx;
  font-weight: bold;
}

.modal-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}

.modal-body {
  flex: 1;
  padding: 30rpx;
  overflow-y: auto;
  box-sizing: border-box;
}

.modal-footer {
  display: flex;
  padding: 20rpx 30rpx;
  border-top: 1px solid #eee;
  gap: 20rpx;
  padding-bottom: 50rpx;
  flex-shrink: 0;
}

.cancel-btn, .confirm-btn {
  flex: 1;
  border-radius: 50rpx;
  padding: 24rpx;
  font-size: 28rpx;
  border: none;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.confirm-btn {
  background-color: #f0e68c;
  color: #333;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
  font-weight: 500;
}

.required {
  color: #ff6b6b;
}

.input, .picker {
  width: 100%;
  padding: 24rpx;
  border: 1px solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  background-color: #fff;
  box-sizing: border-box;
}

.address-row {
  display: flex;
  gap: 15rpx;
}

.address-input {
  flex: 1;
}

.map-btn {
  background-color: #f0e68c;
  color: #333;
  font-size: 24rpx;
  padding: 0 24rpx;
  border-radius: 12rpx;
  border: none;
}

.star-box {
  display: flex;
  gap: 15rpx;
}

.star {
  font-size: 48rpx;
  color: #ccc;
}

.star.active {
  color: #ffc107;
}

.upload-box {
  width: 180rpx;
  height: 180rpx;
  border: 2rpx dashed #e0e0e0;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background-color: #fafafa;
}

.upload-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-placeholder {
  text-align: center;
}

.upload-icon {
  font-size: 48rpx;
  color: #ccc;
  display: block;
}

.upload-text {
  font-size: 22rpx;
  color: #ccc;
  display: block;
  margin-top: 8rpx;
}

.detail-upload-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.detail-image-item {
  position: relative;
  width: 160rpx;
  height: 160rpx;
}

.detail-image {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
  object-fit: cover;
}

.detail-delete {
  position: absolute;
  top: -12rpx;
  right: -12rpx;
  background-color: #ff6b6b;
  color: #fff;
  width: 40rpx;
  height: 40rpx;
  border-radius: 20rpx;
  text-align: center;
  line-height: 36rpx;
  font-size: 28rpx;
}

.upload-btn {
  width: 160rpx;
  height: 160rpx;
  border: 2rpx dashed #e0e0e0;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}

.empty-state, .loading {
  text-align: center;
  padding: 100rpx;
  color: #999;
  font-size: 28rpx;
}

/* 百度地图弹窗样式 */
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
  background-color: #f0e68c;
  color: #333;
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

.suggest-item:active {
  background-color: #f5f5f5;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  background-color: #f0e68c;
  color: #333;
}
</style>