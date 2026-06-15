"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      id: "",
      hotelName: "",
      price: 0,
      checkInDate: "",
      checkOutDate: "",
      days: 1,
      totalPrice: 0,
      roomCount: 1,
      contactPhone: "",
      username: "",
      // 房型相关
      roomTypes: [],
      selectedRoom: null,
      roomTypeId: null,
      roomTypeName: "",
      showRoomPicker: false
    };
  },
  onLoad(options) {
    common_vendor.index.__f__("log", "at pages/booking/booking.vue:123", "预订页面接收参数：", options);
    const today = /* @__PURE__ */ new Date();
    const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1e3);
    const formatDate = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
    this.id = options.id;
    this.hotelName = decodeURIComponent(options.name || "酒店");
    this.price = Number(options.price) || 0;
    if (options.roomTypeId) {
      this.roomTypeId = options.roomTypeId;
      this.roomTypeName = decodeURIComponent(options.roomTypeName || "");
      this.selectedRoom = { id: this.roomTypeId, typeName: this.roomTypeName, price: this.price };
    }
    this.checkInDate = formatDate(tomorrow);
    this.checkOutDate = formatDate(new Date(tomorrow.getTime() + 24 * 60 * 60 * 1e3));
    this.days = 1;
    this.totalPrice = this.price;
    this.roomCount = 1;
    this.contactPhone = "";
    this.username = options.username || common_vendor.index.getStorageSync("loginUsername") || "";
    this.getRoomTypes();
  },
  methods: {
    // 获取房型列表
    getRoomTypes() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/room-types/hotel/" + this.id,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/booking/booking.vue:161", "房型列表返回：", res.data);
          let rooms = [];
          if (res.data && res.data.code === 200) {
            rooms = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            rooms = res.data;
          }
          this.roomTypes = rooms;
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/booking/booking.vue:173", "获取房型失败：", err);
        }
      });
    },
    // 选择房型
    selectRoom(room) {
      this.selectedRoom = room;
      this.roomTypeId = room.id;
      this.roomTypeName = room.typeName;
      this.price = room.price;
      this.showRoomPicker = false;
      this.calcDays();
      common_vendor.index.showToast({ title: `已选择${room.typeName}`, icon: "success" });
    },
    bindCheckInChange(e) {
      this.checkInDate = e.detail.value;
      this.calcDays();
    },
    bindCheckOutChange(e) {
      this.checkOutDate = e.detail.value;
      this.calcDays();
    },
    decreaseCount() {
      if (this.roomCount > 1) {
        this.roomCount--;
        this.calcDays();
      }
    },
    increaseCount() {
      if (this.selectedRoom && this.roomCount >= this.selectedRoom.availableCount) {
        common_vendor.index.showToast({ title: `最多可订${this.selectedRoom.availableCount}间`, icon: "none" });
        return;
      }
      this.roomCount++;
      this.calcDays();
    },
    calcDays() {
      const inDate = new Date(this.checkInDate);
      const outDate = new Date(this.checkOutDate);
      const diff = (outDate - inDate) / (1e3 * 60 * 60 * 24);
      const days = diff > 0 ? diff : 1;
      this.days = days;
      this.totalPrice = days * this.price * this.roomCount;
    },
    submitOrder() {
      if (!this.roomTypeId) {
        common_vendor.index.showToast({ title: "请选择房型", icon: "none" });
        return;
      }
      if (!this.contactPhone) {
        common_vendor.index.showToast({ title: "请输入联系电话", icon: "none" });
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(this.contactPhone)) {
        common_vendor.index.showToast({ title: "手机号格式不正确", icon: "none" });
        return;
      }
      const submitData = {
        hotelId: parseInt(this.id),
        name: this.hotelName,
        roomTypeId: parseInt(this.roomTypeId),
        roomTypeName: this.roomTypeName,
        roomCount: this.roomCount,
        price: this.totalPrice,
        checkIn: this.checkInDate,
        checkOut: this.checkOutDate,
        username: this.username,
        contactPhone: this.contactPhone,
        status: "待支付"
      };
      common_vendor.index.__f__("log", "at pages/booking/booking.vue:255", "提交订单数据：", submitData);
      common_vendor.index.showLoading({ title: "提交中..." });
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotel-orders",
        method: "POST",
        header: {
          "Content-Type": "application/json"
        },
        data: submitData,
        success: (res) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("log", "at pages/booking/booking.vue:268", "订单提交结果：", res.data);
          if (res.data && res.data.code === 200) {
            common_vendor.index.showToast({ title: "预订成功！", icon: "success" });
            setTimeout(() => {
              common_vendor.index.switchTab({ url: "/pages/profile/profile" });
            }, 1500);
          } else {
            common_vendor.index.showToast({
              title: res.data.message || "预订失败",
              icon: "none"
            });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/booking/booking.vue:284", "提交订单失败：", err);
          common_vendor.index.showToast({ title: "网络错误，请重试", icon: "none" });
        }
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.t($data.hotelName),
    b: common_vendor.t($data.price),
    c: common_vendor.t($data.roomTypeName || "请选择房型"),
    d: common_vendor.n($data.roomTypeName ? "selected" : "placeholder"),
    e: common_vendor.o(($event) => $data.showRoomPicker = true),
    f: $data.showRoomPicker
  }, $data.showRoomPicker ? {
    g: common_vendor.o(($event) => $data.showRoomPicker = false),
    h: common_vendor.f($data.roomTypes, (room, k0, i0) => {
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
        i: room.id,
        j: common_vendor.o(($event) => $options.selectRoom(room), room.id)
      });
    }),
    i: common_vendor.o(() => {
    }),
    j: common_vendor.o(($event) => $data.showRoomPicker = false)
  } : {}, {
    k: common_vendor.t($data.checkInDate),
    l: $data.checkInDate,
    m: common_vendor.o((...args) => $options.bindCheckInChange && $options.bindCheckInChange(...args)),
    n: common_vendor.t($data.checkOutDate),
    o: $data.checkOutDate,
    p: common_vendor.o((...args) => $options.bindCheckOutChange && $options.bindCheckOutChange(...args)),
    q: common_vendor.o((...args) => $options.decreaseCount && $options.decreaseCount(...args)),
    r: common_vendor.t($data.roomCount),
    s: common_vendor.o((...args) => $options.increaseCount && $options.increaseCount(...args)),
    t: common_vendor.t($data.days),
    v: $data.contactPhone,
    w: common_vendor.o(($event) => $data.contactPhone = $event.detail.value),
    x: common_vendor.t($data.totalPrice),
    y: common_vendor.o((...args) => $options.submitOrder && $options.submitOrder(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-d331dabb"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/booking/booking.js.map
