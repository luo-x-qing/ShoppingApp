"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
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
        typeName: "",
        price: "",
        totalCount: "",
        availableCount: "",
        size: "",
        bedType: "",
        windowStatus: "",
        breakfast: ""
      },
      bedOptions: ["大床", "双床", "单人床", "榻榻米", "圆床"],
      windowOptions: ["有窗", "无窗"],
      breakfastOptions: ["含早", "不含早"],
      form: {
        id: null,
        name: "",
        category: "",
        address: "",
        starLevel: 3,
        price: "",
        totalRooms: "",
        coverImage: "",
        detailImages: [],
        latitude: null,
        longitude: null
      },
      categoryOptions: ["度假型酒店", "商务型酒店", "公寓型酒店", "连锁酒店"],
      // 地图相关
      searchKeyword: "",
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
    if (!this.checkMerchantStatus()) {
      return;
    }
    const userInfo = common_vendor.index.getStorageSync("userInfo");
    if (userInfo && userInfo.id) {
      this.merchantId = userInfo.id;
      common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:380", "商家ID:", this.merchantId);
      this.loadHotels();
    } else {
      common_vendor.index.showToast({ title: "请先登录", icon: "none" });
      setTimeout(() => {
        common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
      }, 1500);
    }
  },
  methods: {
    // ========== 商家状态校验方法 ==========
    checkMerchantStatus(showError = true) {
      const userInfo = common_vendor.index.getStorageSync("userInfo");
      if (!userInfo || userInfo.status !== "NORMAL") {
        if (showError) {
          common_vendor.index.showModal({
            title: "账号异常",
            content: userInfo && userInfo.status === "PENDING" ? "您的商家账号正在审核中，请等待审核通过后使用酒店管理功能。" : userInfo && userInfo.status === "REJECTED" ? "您的商家账号审核未通过，无法使用酒店管理功能。" : userInfo && userInfo.status === "BANNED" ? "您的商家账号已被禁用，无法使用酒店管理功能。" : "您的商家账号状态异常，无法使用酒店管理功能。",
            showCancel: false,
            confirmText: "返回首页",
            success: () => {
              common_vendor.index.reLaunch({ url: "/pages/merchant/home" });
            }
          });
        }
        return false;
      }
      return true;
    },
    getStarText(level) {
      if (!level)
        return "☆☆☆☆☆";
      return "★".repeat(level) + "☆".repeat(5 - level);
    },
    getImageUrl(path) {
      if (!path)
        return "/static/default-hotel.png";
      if (path.startsWith("http"))
        return path;
      if (path.startsWith("/file"))
        return "http://localhost:8080" + path;
      return path;
    },
    previewImage(url) {
      const fullUrl = this.getImageUrl(url);
      common_vendor.index.previewImage({
        urls: [fullUrl]
      });
    },
    loadHotels() {
      if (!this.merchantId) {
        common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:438", "未获取到商家ID");
        return;
      }
      this.loading = true;
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotels/merchant/${this.merchantId}`,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:447", "酒店列表返回:", res.data);
          if (res.data && res.data.code === 200) {
            this.hotelList = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            this.hotelList = res.data;
          } else {
            this.hotelList = [];
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:457", "获取酒店列表失败", err);
          this.hotelList = [];
        },
        complete: () => {
          this.loading = false;
        }
      });
    },
    // ========== 酒店状态管理 ==========
    toggleHotelStatus(hotel) {
      if (!this.checkMerchantStatus())
        return;
      const newStatus = hotel.status === "营业中" ? "已停业" : "营业中";
      const confirmTitle = hotel.status === "营业中" ? "确认停业" : "确认恢复营业";
      const confirmContent = hotel.status === "营业中" ? `确定要将"${hotel.name}"设为停业吗？停业后用户将无法预订。` : `确定要将"${hotel.name}"恢复营业吗？`;
      common_vendor.index.showModal({
        title: confirmTitle,
        content: confirmContent,
        confirmColor: hotel.status === "营业中" ? "#ff6b6b" : "#52c41a",
        success: (res) => {
          if (res.confirm) {
            this.updateHotelStatus(hotel.id, newStatus);
          }
        }
      });
    },
    updateHotelStatus(hotelId, newStatus) {
      common_vendor.index.showLoading({ title: "更新中..." });
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotels/${hotelId}`,
        method: "GET",
        success: (getRes) => {
          let hotelData = getRes.data;
          if (getRes.data && getRes.data.code === 200) {
            hotelData = getRes.data.data;
          }
          if (!hotelData) {
            common_vendor.index.hideLoading();
            common_vendor.index.showToast({ title: "获取酒店信息失败", icon: "none" });
            return;
          }
          hotelData.status = newStatus;
          common_vendor.index.request({
            url: `http://localhost:8080/api/hotels/${hotelId}`,
            method: "PUT",
            header: { "Content-Type": "application/json" },
            data: hotelData,
            success: (res) => {
              common_vendor.index.hideLoading();
              if (res.data && res.data.code === 200) {
                common_vendor.index.showToast({
                  title: newStatus === "营业中" ? "已恢复营业" : "已设为停业",
                  icon: "success"
                });
                this.loadHotels();
              } else {
                common_vendor.index.showToast({ title: "操作失败", icon: "none" });
              }
            },
            fail: (err) => {
              common_vendor.index.hideLoading();
              common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:527", "更新状态失败", err);
              common_vendor.index.showToast({ title: "网络错误", icon: "none" });
            }
          });
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:534", "获取酒店信息失败", err);
          common_vendor.index.showToast({ title: "获取酒店信息失败", icon: "none" });
        }
      });
    },
    // ========== 房型管理 ==========
    manageRooms(hotel) {
      if (!this.checkMerchantStatus())
        return;
      this.currentHotel = hotel;
      this.showRoomModal = true;
      this.loadRooms(hotel.id);
    },
    loadRooms(hotelId) {
      common_vendor.index.request({
        url: "http://localhost:8080/api/room-types/hotel/" + hotelId,
        method: "GET",
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
          common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:563", "获取房型失败", err);
          this.roomList = [];
        }
      });
    },
    openAddRoomModal() {
      if (!this.checkMerchantStatus())
        return;
      this.isEditRoom = false;
      this.roomForm = {
        id: null,
        hotelId: this.currentHotel.id,
        typeName: "",
        price: "",
        totalCount: "",
        availableCount: "",
        size: "",
        bedType: "",
        windowStatus: "",
        breakfast: ""
      };
      this.showRoomFormModal = true;
    },
    openEditRoomModal(room) {
      if (!this.checkMerchantStatus())
        return;
      this.isEditRoom = true;
      this.roomForm = {
        id: room.id,
        hotelId: room.hotelId || this.currentHotel.id,
        typeName: room.typeName,
        price: room.price,
        totalCount: room.totalCount,
        availableCount: room.availableCount,
        size: room.size || "",
        bedType: room.bedType || "",
        windowStatus: room.windowStatus || "",
        breakfast: room.breakfast || ""
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
      if (!this.checkMerchantStatus(false))
        return;
      if (!this.roomForm.typeName) {
        common_vendor.index.showToast({ title: "请输入房型名称", icon: "none" });
        return;
      }
      if (!this.roomForm.price || this.roomForm.price <= 0) {
        common_vendor.index.showToast({ title: "请输入有效价格", icon: "none" });
        return;
      }
      if (!this.roomForm.totalCount || this.roomForm.totalCount <= 0) {
        common_vendor.index.showToast({ title: "请输入总数量", icon: "none" });
        return;
      }
      if (!this.roomForm.availableCount || this.roomForm.availableCount <= 0) {
        common_vendor.index.showToast({ title: "请输入可预订数量", icon: "none" });
        return;
      }
      const submitData = {
        typeName: this.roomForm.typeName,
        price: parseFloat(this.roomForm.price),
        totalCount: parseInt(this.roomForm.totalCount),
        availableCount: parseInt(this.roomForm.availableCount),
        size: this.roomForm.size || "",
        bedType: this.roomForm.bedType || "",
        windowStatus: this.roomForm.windowStatus || "",
        breakfast: this.roomForm.breakfast || "",
        hotel: {
          id: parseInt(this.roomForm.hotelId)
        }
      };
      let url = "";
      let method = "";
      if (this.isEditRoom) {
        url = "http://localhost:8080/api/room-types/" + this.roomForm.id;
        method = "PUT";
        submitData.id = this.roomForm.id;
      } else {
        url = "http://localhost:8080/api/room-types";
        method = "POST";
      }
      common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:664", "提交的房型数据：", submitData);
      common_vendor.index.showLoading({ title: "保存中..." });
      common_vendor.index.request({
        url,
        method,
        header: { "Content-Type": "application/json" },
        data: submitData,
        success: (res) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:675", "房型提交结果：", res);
          if (res.statusCode === 200 && res.data && res.data.code === 200) {
            common_vendor.index.showToast({ title: this.isEditRoom ? "修改成功" : "添加成功", icon: "success" });
            this.closeRoomFormModal();
            this.loadRooms(this.currentHotel.id);
          } else {
            common_vendor.index.showToast({ title: res.data && res.data.message || "操作失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:687", "提交失败", err);
          common_vendor.index.showToast({ title: "网络错误，请重试", icon: "none" });
        }
      });
    },
    deleteRoom(roomId) {
      if (!this.checkMerchantStatus())
        return;
      common_vendor.index.showModal({
        title: "确认删除",
        content: "确定要删除该房型吗？",
        confirmColor: "#ff6b6b",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.showLoading({ title: "删除中..." });
            common_vendor.index.request({
              url: "http://localhost:8080/api/room-types/" + roomId,
              method: "DELETE",
              success: () => {
                common_vendor.index.showToast({ title: "删除成功", icon: "success" });
                this.loadRooms(this.currentHotel.id);
              },
              fail: (err) => {
                common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:711", "删除失败", err);
                common_vendor.index.showToast({ title: "删除失败", icon: "none" });
              },
              complete: () => {
                common_vendor.index.hideLoading();
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
      if (!this.checkMerchantStatus())
        return;
      this.isEdit = false;
      this.form = {
        id: null,
        name: "",
        category: "",
        address: "",
        starLevel: 3,
        price: "",
        totalRooms: "",
        coverImage: "",
        detailImages: [],
        latitude: null,
        longitude: null
      };
      this.showModal = true;
    },
    openEditModal(hotel) {
      if (!this.checkMerchantStatus())
        return;
      this.isEdit = true;
      let detailImages = [];
      if (hotel.detailImages && hotel.detailImages.length > 0) {
        detailImages = hotel.detailImages.map((img) => {
          if (typeof img === "object" && img.imageUrl) {
            return { imageUrl: img.imageUrl };
          } else if (typeof img === "string") {
            return { imageUrl: img };
          } else {
            return { imageUrl: img };
          }
        });
      }
      this.form = {
        id: hotel.id,
        name: hotel.name,
        category: hotel.category || "",
        address: hotel.address || "",
        starLevel: hotel.starLevel || 3,
        price: hotel.price,
        totalRooms: hotel.totalRooms || "",
        coverImage: hotel.coverImage || "",
        detailImages,
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
      if (!this.checkMerchantStatus(false))
        return;
      this.showMapPicker = true;
      this.searchKeyword = "";
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
          title: this.form.address || "酒店位置",
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
      common_vendor.index.request({
        url: "http://localhost:8080/api/map/search",
        method: "GET",
        data: {
          keyword,
          region: "福州"
          // 关键：限制搜索区域为福州
        },
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:855", "搜索建议返回:", res.data);
          if (res.statusCode === 200 && res.data && res.data.success) {
            this.suggestList = res.data.data || [];
          } else {
            this.suggestList = [];
          }
          this.showSuggest = this.suggestList.length > 0;
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
        common_vendor.index.showToast({ title: "请输入搜索关键词", icon: "none" });
        return;
      }
      this.suggestList = [];
      this.searchLocationDirect(this.searchKeyword);
    },
    searchLocationDirect(address) {
      common_vendor.index.showLoading({ title: "搜索中..." });
      const encodedAddress = encodeURIComponent(address);
      common_vendor.index.request({
        url: `http://localhost:8080/api/map/geocode?address=${encodedAddress}`,
        method: "GET",
        success: (res) => {
          var _a;
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:917", "地理编码返回:", res.data);
          if (res.statusCode === 200 && res.data && res.data.success) {
            const lat = res.data.latitude;
            const lng = res.data.longitude;
            const addr = res.data.address || address;
            this.mapCenter = { latitude: lat, longitude: lng };
            this.markers = [{
              id: Date.now(),
              latitude: lat,
              longitude: lng,
              title: addr,
              width: 30,
              height: 30
            }];
            this.selectedLocation = { address: addr, lat, lng };
            this.getNearbyAddresses(lat, lng);
          } else {
            common_vendor.index.showToast({ title: ((_a = res.data) == null ? void 0 : _a.message) || "未找到该地点", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:941", "地理编码失败:", err);
          common_vendor.index.showToast({ title: "搜索失败，请重试", icon: "none" });
        }
      });
    },
    onMapTap(e) {
      const { latitude, longitude } = e.detail;
      if (!latitude || !longitude)
        return;
      this.markers = [{
        id: Date.now(),
        latitude,
        longitude,
        title: "选中的位置",
        width: 30,
        height: 30
      }];
      this.getNearbyAddresses(latitude, longitude);
    },
    getNearbyAddresses(lat, lng) {
      common_vendor.index.showLoading({ title: "获取地址列表..." });
      common_vendor.index.request({
        url: `http://localhost:8080/api/map/reverse-geocode?lat=${lat}&lng=${lng}`,
        method: "GET",
        success: (res) => {
          var _a;
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:971", "逆地理编码返回:", res.data);
          if (res.statusCode === 200 && res.data && res.data.success) {
            this.nearbyPois = res.data.nearbyPois || [];
            if (res.data.address) {
              this.selectedLocation = {
                address: res.data.address,
                lat,
                lng
              };
            }
            if (this.nearbyPois.length === 0) {
              common_vendor.index.showToast({ title: "该位置无附近地址，可手动输入", icon: "none" });
            }
          } else {
            common_vendor.index.showToast({ title: ((_a = res.data) == null ? void 0 : _a.message) || "获取地址失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:991", "逆地理编码失败:", err);
          common_vendor.index.showToast({ title: "网络错误", icon: "none" });
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
      common_vendor.index.showToast({ title: "已选择：" + poi.name, icon: "success" });
    },
    confirmLocation() {
      if (this.selectedLocation && this.selectedLocation.address) {
        this.form.address = this.selectedLocation.address;
        this.form.latitude = this.selectedLocation.lat;
        this.form.longitude = this.selectedLocation.lng;
        common_vendor.index.showToast({ title: "地址已选择", icon: "success" });
        this.closeMapPicker();
      } else {
        common_vendor.index.showToast({ title: "请先在地图上选择位置", icon: "none" });
      }
    },
    uploadCover() {
      if (!this.checkMerchantStatus(false))
        return;
      common_vendor.index.chooseImage({
        count: 1,
        success: (res) => {
          const tempFile = res.tempFilePaths[0];
          common_vendor.index.showLoading({ title: "上传中..." });
          this.uploadImageToServer(tempFile, (url) => {
            if (url) {
              this.form.coverImage = url;
              common_vendor.index.showToast({ title: "封面上传成功", icon: "success" });
              common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1031", "封面上传成功，URL:", url);
            } else {
              common_vendor.index.showToast({ title: "封面上传失败", icon: "none" });
            }
            common_vendor.index.hideLoading();
          });
        }
      });
    },
    uploadDetailImage() {
      if (!this.checkMerchantStatus(false))
        return;
      common_vendor.index.chooseImage({
        count: 9 - this.form.detailImages.length,
        success: (res) => {
          if (this.form.detailImages.length + res.tempFilePaths.length > 9) {
            common_vendor.index.showToast({ title: "最多只能上传9张图片", icon: "none" });
            return;
          }
          common_vendor.index.showLoading({ title: "上传中..." });
          const tempFiles = res.tempFilePaths;
          let completed = 0;
          let uploadedUrls = [];
          if (tempFiles.length === 0) {
            common_vendor.index.hideLoading();
            return;
          }
          tempFiles.forEach((file) => {
            this.uploadImageToServer(file, (url) => {
              if (url) {
                uploadedUrls.push({ imageUrl: url });
                common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1066", "图片上传成功，当前已上传数量:", uploadedUrls.length);
              } else {
                common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:1068", "图片上传失败:", file);
              }
              completed++;
              if (completed === tempFiles.length) {
                if (uploadedUrls.length > 0) {
                  this.form.detailImages = [...this.form.detailImages, ...uploadedUrls];
                  common_vendor.index.showToast({
                    title: `成功上传 ${uploadedUrls.length} 张图片`,
                    icon: "success"
                  });
                  common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1078", "更新后的详情图列表:", JSON.stringify(this.form.detailImages));
                } else {
                  common_vendor.index.showToast({ title: "上传失败，请重试", icon: "none" });
                }
                common_vendor.index.hideLoading();
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
      common_vendor.index.uploadFile({
        url: "http://localhost:8080/api/upload",
        filePath,
        name: "file",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1100", "上传原始响应:", res.data);
          let imageUrl = null;
          try {
            const data = JSON.parse(res.data);
            imageUrl = data.url || data.data || data;
            common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1107", "JSON解析成功");
          } catch (e) {
            common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1109", "不是JSON格式，直接使用字符串");
            imageUrl = res.data;
            if (imageUrl.startsWith('"') && imageUrl.endsWith('"')) {
              imageUrl = imageUrl.slice(1, -1);
            }
          }
          if (imageUrl) {
            if (!imageUrl.startsWith("http") && !imageUrl.startsWith("/file")) {
              imageUrl = "/file/" + imageUrl;
            }
            common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1120", "图片上传成功，最终URL:", imageUrl);
            callback(imageUrl);
          } else {
            common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:1123", "获取图片URL失败");
            callback(null);
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:1128", "上传失败", err);
          callback(null);
        }
      });
    },
    submitHotel() {
      if (!this.checkMerchantStatus(false))
        return;
      const userInfo = common_vendor.index.getStorageSync("userInfo");
      const merchantId = userInfo && userInfo.id ? userInfo.id : null;
      common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1140", "=== 提交酒店 ===");
      common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1141", "商家ID:", merchantId);
      common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1142", "详情图原始数据:", this.form.detailImages);
      if (!this.form.name || this.form.name.trim() === "") {
        common_vendor.index.showToast({ title: "请输入酒店名称", icon: "none" });
        return;
      }
      if (!this.form.category) {
        common_vendor.index.showToast({ title: "请选择酒店分类", icon: "none" });
        return;
      }
      if (!this.form.address || this.form.address.trim() === "") {
        common_vendor.index.showToast({ title: "请输入酒店地址", icon: "none" });
        return;
      }
      if (!this.form.price || this.form.price <= 0) {
        common_vendor.index.showToast({ title: "请输入有效价格", icon: "none" });
        return;
      }
      if (!merchantId) {
        common_vendor.index.showToast({ title: "未获取到商家信息，请重新登录", icon: "none" });
        return;
      }
      let detailImagesData = [];
      if (this.form.detailImages && this.form.detailImages.length > 0) {
        detailImagesData = this.form.detailImages.map((img) => {
          if (typeof img === "object" && img.imageUrl) {
            return { imageUrl: img.imageUrl };
          } else if (typeof img === "string") {
            return { imageUrl: img };
          } else {
            return { imageUrl: img };
          }
        });
      }
      const submitData = {
        name: this.form.name,
        category: this.form.category,
        address: this.form.address,
        starLevel: parseInt(this.form.starLevel),
        price: parseFloat(this.form.price),
        totalRooms: this.form.totalRooms ? parseInt(this.form.totalRooms) : 0,
        coverImage: this.form.coverImage || "",
        detailImages: detailImagesData,
        latitude: this.form.latitude,
        longitude: this.form.longitude,
        merchantId,
        status: "营业中"
      };
      common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1196", "提交的详情图数据:", JSON.stringify(detailImagesData));
      common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1197", "完整提交数据:", JSON.stringify(submitData));
      let url = "";
      let method = "";
      if (this.isEdit) {
        url = "http://localhost:8080/api/hotels/" + this.form.id;
        method = "PUT";
      } else {
        url = "http://localhost:8080/api/hotels";
        method = "POST";
      }
      common_vendor.index.showLoading({ title: "保存中..." });
      common_vendor.index.request({
        url,
        method,
        header: { "Content-Type": "application/json" },
        data: submitData,
        success: (res) => {
          var _a;
          common_vendor.index.__f__("log", "at pages/merchant/hotel-list.vue:1217", "提交结果:", res.data);
          common_vendor.index.hideLoading();
          if (res.data && res.data.code === 200) {
            common_vendor.index.showToast({ title: this.isEdit ? "修改成功" : "添加成功", icon: "success" });
            this.closeModal();
            this.loadHotels();
          } else {
            common_vendor.index.showToast({ title: ((_a = res.data) == null ? void 0 : _a.message) || "操作失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:1230", "提交失败:", err);
          common_vendor.index.showToast({ title: "网络错误", icon: "none" });
        }
      });
    },
    deleteHotel(id) {
      if (!this.checkMerchantStatus())
        return;
      if (!id) {
        common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:1240", "删除失败：酒店ID为空");
        common_vendor.index.showToast({ title: "删除失败", icon: "none" });
        return;
      }
      common_vendor.index.showModal({
        title: "确认删除",
        content: "确定要删除该酒店吗？删除后不可恢复。",
        confirmColor: "#ff6b6b",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.showLoading({ title: "删除中..." });
            common_vendor.index.request({
              url: "http://localhost:8080/api/hotels/" + id,
              method: "DELETE",
              success: () => {
                common_vendor.index.showToast({ title: "删除成功", icon: "success" });
                this.loadHotels();
              },
              fail: (err) => {
                common_vendor.index.__f__("error", "at pages/merchant/hotel-list.vue:1260", "删除失败", err);
                common_vendor.index.showToast({ title: "删除失败", icon: "none" });
              },
              complete: () => {
                common_vendor.index.hideLoading();
              }
            });
          }
        }
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.openAddModal && $options.openAddModal(...args)),
    b: common_vendor.f($data.hotelList, (hotel, k0, i0) => {
      return common_vendor.e({
        a: $options.getImageUrl(hotel.coverImage),
        b: common_vendor.t(hotel.name),
        c: common_vendor.t(hotel.address || "暂无地址"),
        d: common_vendor.t($options.getStarText(hotel.starLevel)),
        e: common_vendor.t(hotel.price),
        f: common_vendor.t(hotel.status || "营业中"),
        g: common_vendor.n(hotel.status === "营业中" ? "status-open" : "status-closed"),
        h: common_vendor.t(hotel.status === "营业中" ? "设为停业" : "设为营业"),
        i: common_vendor.o(($event) => $options.toggleHotelStatus(hotel), hotel.id),
        j: hotel.category
      }, hotel.category ? {
        k: common_vendor.t(hotel.category)
      } : {}, {
        l: common_vendor.o(($event) => $options.openEditModal(hotel), hotel.id),
        m: common_vendor.o(($event) => $options.manageRooms(hotel), hotel.id),
        n: common_vendor.o(($event) => $options.deleteHotel(hotel.id), hotel.id),
        o: hotel.id
      });
    }),
    c: $data.hotelList.length === 0 && !$data.loading
  }, $data.hotelList.length === 0 && !$data.loading ? {} : {}, {
    d: $data.loading
  }, $data.loading ? {} : {}, {
    e: $data.showModal
  }, $data.showModal ? common_vendor.e({
    f: common_vendor.t($data.isEdit ? "编辑酒店" : "添加酒店"),
    g: common_vendor.o((...args) => $options.closeModal && $options.closeModal(...args)),
    h: $data.form.name,
    i: common_vendor.o(($event) => $data.form.name = $event.detail.value),
    j: common_vendor.t($data.form.category || "请选择"),
    k: $data.categoryOptions,
    l: common_vendor.o((...args) => $options.onCategoryChange && $options.onCategoryChange(...args)),
    m: $data.form.address,
    n: common_vendor.o(($event) => $data.form.address = $event.detail.value),
    o: common_vendor.o((...args) => $options.openMapPicker && $options.openMapPicker(...args)),
    p: common_vendor.f(5, (i, k0, i0) => {
      return {
        a: i,
        b: $data.form.starLevel >= i ? 1 : "",
        c: common_vendor.o(($event) => $data.form.starLevel = i, i)
      };
    }),
    q: $data.form.price,
    r: common_vendor.o(($event) => $data.form.price = $event.detail.value),
    s: $data.form.totalRooms,
    t: common_vendor.o(($event) => $data.form.totalRooms = $event.detail.value),
    v: $data.form.coverImage
  }, $data.form.coverImage ? {
    w: $options.getImageUrl($data.form.coverImage)
  } : {}, {
    x: common_vendor.o((...args) => $options.uploadCover && $options.uploadCover(...args)),
    y: common_vendor.f($data.form.detailImages, (img, idx, i0) => {
      return {
        a: $options.getImageUrl(img.imageUrl || img),
        b: common_vendor.o(($event) => $options.removeDetailImage(idx), idx),
        c: idx,
        d: common_vendor.o(($event) => $options.previewImage(img.imageUrl || img), idx)
      };
    }),
    z: common_vendor.o((...args) => $options.uploadDetailImage && $options.uploadDetailImage(...args)),
    A: $data.form.detailImages.length === 0
  }, $data.form.detailImages.length === 0 ? {} : {}, {
    B: common_vendor.o((...args) => $options.closeModal && $options.closeModal(...args)),
    C: common_vendor.o((...args) => $options.submitHotel && $options.submitHotel(...args)),
    D: common_vendor.o(() => {
    }),
    E: common_vendor.o((...args) => $options.closeModal && $options.closeModal(...args))
  }) : {}, {
    F: $data.showRoomModal
  }, $data.showRoomModal ? common_vendor.e({
    G: common_vendor.t($data.currentHotel ? $data.currentHotel.name : ""),
    H: common_vendor.o((...args) => $options.openAddRoomModal && $options.openAddRoomModal(...args)),
    I: common_vendor.o((...args) => $options.closeRoomModal && $options.closeRoomModal(...args)),
    J: common_vendor.f($data.roomList, (room, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(room.typeName),
        b: common_vendor.t(room.size),
        c: common_vendor.t(room.bedType),
        d: common_vendor.t(room.windowStatus),
        e: room.breakfast
      }, room.breakfast ? {
        f: common_vendor.t(room.breakfast)
      } : {}, {
        g: common_vendor.t(room.price),
        h: common_vendor.t(room.availableCount),
        i: common_vendor.t(room.totalCount),
        j: common_vendor.o(($event) => $options.openEditRoomModal(room), room.id),
        k: common_vendor.o(($event) => $options.deleteRoom(room.id), room.id),
        l: room.id
      });
    }),
    K: $data.roomList.length === 0
  }, $data.roomList.length === 0 ? {} : {}, {
    L: common_vendor.o((...args) => $options.closeRoomModal && $options.closeRoomModal(...args)),
    M: common_vendor.o(() => {
    }),
    N: common_vendor.o((...args) => $options.closeRoomModal && $options.closeRoomModal(...args))
  }) : {}, {
    O: $data.showRoomFormModal
  }, $data.showRoomFormModal ? {
    P: common_vendor.t($data.isEditRoom ? "编辑房型" : "添加房型"),
    Q: common_vendor.o((...args) => $options.closeRoomFormModal && $options.closeRoomFormModal(...args)),
    R: $data.roomForm.typeName,
    S: common_vendor.o(($event) => $data.roomForm.typeName = $event.detail.value),
    T: $data.roomForm.price,
    U: common_vendor.o(($event) => $data.roomForm.price = $event.detail.value),
    V: $data.roomForm.totalCount,
    W: common_vendor.o(($event) => $data.roomForm.totalCount = $event.detail.value),
    X: $data.roomForm.availableCount,
    Y: common_vendor.o(($event) => $data.roomForm.availableCount = $event.detail.value),
    Z: $data.roomForm.size,
    aa: common_vendor.o(($event) => $data.roomForm.size = $event.detail.value),
    ab: common_vendor.t($data.roomForm.bedType || "请选择床型"),
    ac: $data.bedOptions,
    ad: common_vendor.o((...args) => $options.onBedChange && $options.onBedChange(...args)),
    ae: common_vendor.t($data.roomForm.windowStatus || "请选择"),
    af: $data.windowOptions,
    ag: common_vendor.o((...args) => $options.onWindowChange && $options.onWindowChange(...args)),
    ah: common_vendor.t($data.roomForm.breakfast || "请选择"),
    ai: $data.breakfastOptions,
    aj: common_vendor.o((...args) => $options.onBreakfastChange && $options.onBreakfastChange(...args)),
    ak: common_vendor.o((...args) => $options.closeRoomFormModal && $options.closeRoomFormModal(...args)),
    al: common_vendor.o((...args) => $options.submitRoom && $options.submitRoom(...args)),
    am: common_vendor.o(() => {
    }),
    an: common_vendor.o((...args) => $options.closeRoomFormModal && $options.closeRoomFormModal(...args))
  } : {}, {
    ao: $data.showMapPicker
  }, $data.showMapPicker ? common_vendor.e({
    ap: common_vendor.o((...args) => $options.closeMapPicker && $options.closeMapPicker(...args)),
    aq: $data.suggestList.length > 0
  }, $data.suggestList.length > 0 ? {
    ar: common_vendor.f($data.suggestList, (item, index, i0) => {
      return {
        a: common_vendor.t(item.name),
        b: common_vendor.t(item.address),
        c: index,
        d: common_vendor.o(($event) => $options.selectSuggest(item), index)
      };
    })
  } : {}, {
    as: $data.mapCenter.latitude,
    at: $data.mapCenter.longitude,
    av: $data.markers,
    aw: common_vendor.o((...args) => $options.onMapTap && $options.onMapTap(...args)),
    ax: $data.nearbyPois.length > 0
  }, $data.nearbyPois.length > 0 ? {
    ay: common_vendor.o(($event) => $data.nearbyPois = []),
    az: common_vendor.f($data.nearbyPois, (poi, index, i0) => {
      return {
        a: common_vendor.t(poi.name),
        b: common_vendor.t(poi.address),
        c: index,
        d: common_vendor.o(($event) => $options.selectPoi(poi), index)
      };
    })
  } : {}, {
    aA: $data.selectedLocation
  }, $data.selectedLocation ? {
    aB: common_vendor.t($data.selectedLocation.address)
  } : {}, {
    aC: common_vendor.o((...args) => $options.closeMapPicker && $options.closeMapPicker(...args)),
    aD: common_vendor.o((...args) => $options.confirmLocation && $options.confirmLocation(...args)),
    aE: common_vendor.o(() => {
    }),
    aF: common_vendor.o((...args) => $options.closeMapPicker && $options.closeMapPicker(...args))
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/merchant/hotel-list.js.map
