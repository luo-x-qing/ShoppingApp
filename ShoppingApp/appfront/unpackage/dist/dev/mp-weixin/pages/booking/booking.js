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
      showRoomPicker: false,
      // 日期限制
      minCheckInDate: "",
      minCheckOutDate: ""
    };
  },
  onLoad(options) {
    common_vendor.index.__f__("log", "at pages/booking/booking.vue:127", "预订页面接收参数：", options);
    const today = /* @__PURE__ */ new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, "0");
    const day = String(today.getDate()).padStart(2, "0");
    const todayStr = `${year}-${month}-${day}`;
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tYear = tomorrow.getFullYear();
    const tMonth = String(tomorrow.getMonth() + 1).padStart(2, "0");
    const tDay = String(tomorrow.getDate()).padStart(2, "0");
    const tomorrowStr = `${tYear}-${tMonth}-${tDay}`;
    const dayAfterTomorrow = new Date(tomorrow);
    dayAfterTomorrow.setDate(dayAfterTomorrow.getDate() + 1);
    const dYear = dayAfterTomorrow.getFullYear();
    const dMonth = String(dayAfterTomorrow.getMonth() + 1).padStart(2, "0");
    const dDay = String(dayAfterTomorrow.getDate()).padStart(2, "0");
    const dayAfterTomorrowStr = `${dYear}-${dMonth}-${dDay}`;
    this.id = options.id;
    this.hotelName = decodeURIComponent(options.name || "酒店");
    this.price = Number(options.price) || 0;
    if (options.roomTypeId) {
      this.roomTypeId = options.roomTypeId;
      this.roomTypeName = decodeURIComponent(options.roomTypeName || "");
      this.selectedRoom = { id: this.roomTypeId, typeName: this.roomTypeName, price: this.price };
    }
    this.minCheckInDate = todayStr;
    this.checkInDate = tomorrowStr;
    this.minCheckOutDate = tomorrowStr;
    this.checkOutDate = dayAfterTomorrowStr;
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
          common_vendor.index.__f__("log", "at pages/booking/booking.vue:191", "房型列表返回：", res.data);
          let rooms = [];
          if (res.data && res.data.code === 200) {
            rooms = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            rooms = res.data;
          }
          this.roomTypes = rooms;
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/booking/booking.vue:203", "获取房型失败：", err);
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
    // 入住日期变化
    bindCheckInChange(e) {
      const newCheckIn = e.detail.value;
      const today = /* @__PURE__ */ new Date();
      today.setHours(0, 0, 0, 0);
      const selectedDate = new Date(newCheckIn);
      selectedDate.setHours(0, 0, 0, 0);
      if (selectedDate < today) {
        common_vendor.index.showToast({
          title: "不能选择今天之前的日期",
          icon: "none"
        });
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);
        this.checkInDate = this.formatDateStr(tomorrow);
        return;
      }
      this.checkInDate = newCheckIn;
      const checkInDateObj = new Date(newCheckIn);
      const minOutDate = new Date(checkInDateObj);
      minOutDate.setDate(minOutDate.getDate() + 1);
      this.minCheckOutDate = this.formatDateStr(minOutDate);
      if (this.checkOutDate < this.minCheckOutDate) {
        this.checkOutDate = this.minCheckOutDate;
      }
      this.calcDays();
    },
    // 退房日期变化
    bindCheckOutChange(e) {
      const newCheckOut = e.detail.value;
      if (newCheckOut <= this.checkInDate) {
        common_vendor.index.showToast({
          title: "退房日期必须晚于入住日期",
          icon: "none"
        });
        const checkInDateObj = new Date(this.checkInDate);
        const minOutDate = new Date(checkInDateObj);
        minOutDate.setDate(minOutDate.getDate() + 1);
        this.checkOutDate = this.formatDateStr(minOutDate);
        return;
      }
      this.checkOutDate = newCheckOut;
      this.calcDays();
    },
    // 格式化日期
    formatDateStr(date) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
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
      const today = /* @__PURE__ */ new Date();
      today.setHours(0, 0, 0, 0);
      const checkIn = new Date(this.checkInDate);
      checkIn.setHours(0, 0, 0, 0);
      if (checkIn < today) {
        common_vendor.index.showToast({ title: "入住日期不能是今天之前", icon: "none" });
        return;
      }
      if (this.checkOutDate <= this.checkInDate) {
        common_vendor.index.showToast({ title: "退房日期必须晚于入住日期", icon: "none" });
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
      common_vendor.index.__f__("log", "at pages/booking/booking.vue:359", "提交订单数据：", submitData);
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
          common_vendor.index.__f__("log", "at pages/booking/booking.vue:372", "订单提交结果：", res.data);
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
          common_vendor.index.__f__("error", "at pages/booking/booking.vue:388", "提交订单失败：", err);
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
