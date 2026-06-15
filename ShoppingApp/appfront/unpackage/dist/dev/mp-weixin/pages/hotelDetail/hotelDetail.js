"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      id: null,
      hotel: {},
      images: [],
      roomTypes: [],
      commentList: [],
      isCollected: false,
      username: "",
      avgScore: 0
    };
  },
  onLoad(options) {
    this.id = options.id;
    this.username = common_vendor.index.getStorageSync("loginUsername") || "";
    const userInfo = common_vendor.index.getStorageSync("userInfo");
    if (userInfo && userInfo.username) {
      this.username = userInfo.username;
    }
    common_vendor.index.__f__("log", "at pages/hotelDetail/hotelDetail.vue:128", "酒店ID：", this.id);
    common_vendor.index.__f__("log", "at pages/hotelDetail/hotelDetail.vue:129", "当前用户：", this.username);
    this.getHotel();
    this.getRoomTypes();
    this.getComments();
    this.checkCollect();
  },
  methods: {
    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr)
        return "刚刚";
      try {
        const date = new Date(timeStr);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
      } catch (e) {
        return timeStr;
      }
    },
    // 计算酒店平均分
    calcAvgScore() {
      if (this.commentList.length === 0) {
        this.avgScore = 0;
        return;
      }
      let total = 0;
      this.commentList.forEach((item) => {
        const score = item.score || item.rating || 5;
        total += score;
      });
      let avg = total / this.commentList.length;
      this.avgScore = avg.toFixed(1);
    },
    // 获取酒店详情
    getHotel() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotels/" + this.id,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/hotelDetail/hotelDetail.vue:173", "酒店详情返回：", res.data);
          let hotelData = res.data;
          if (res.data && res.data.code === 200) {
            hotelData = res.data.data;
          }
          this.hotel = hotelData || {};
          common_vendor.index.__f__("log", "at pages/hotelDetail/hotelDetail.vue:181", "酒店merchantId：", this.hotel.merchantId);
          if (this.hotel.detailImages && this.hotel.detailImages.length > 0) {
            this.images = this.hotel.detailImages.map((img) => {
              if (img.imageUrl) {
                return "http://localhost:8080" + img.imageUrl;
              }
              return img;
            });
          } else if (this.hotel.image) {
            this.images = ["http://localhost:8080" + this.hotel.image];
          } else {
            this.images = ["/static/default-hotel.png"];
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/hotelDetail/hotelDetail.vue:197", "获取酒店详情失败：", err);
          common_vendor.index.showToast({ title: "加载失败", icon: "none" });
        }
      });
    },
    // 获取房型列表
    getRoomTypes() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/room-types/hotel/" + this.id,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/hotelDetail/hotelDetail.vue:209", "房型列表返回：", res.data);
          let rooms = [];
          if (res.data && res.data.code === 200) {
            rooms = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            rooms = res.data;
          } else {
            rooms = [];
          }
          this.roomTypes = rooms;
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/hotelDetail/hotelDetail.vue:223", "获取房型失败：", err);
        }
      });
    },
    // 获取评价（包含商家回复）
    getComments() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotel-comments/hotel/" + this.id,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/hotelDetail/hotelDetail.vue:234", "评价列表返回：", res.data);
          let comments = [];
          if (res.data && res.data.code === 200) {
            comments = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            comments = res.data;
          } else {
            comments = [];
          }
          comments.forEach((comment) => {
            common_vendor.index.__f__("log", "at pages/hotelDetail/hotelDetail.vue:247", `评价ID: ${comment.id}, 回复: ${comment.reply || "无回复"}`);
          });
          this.commentList = comments;
          this.calcAvgScore();
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/hotelDetail/hotelDetail.vue:254", "获取评价失败：", err);
          this.commentList = [];
        }
      });
    },
    // 选择房型
    selectRoom(room) {
      common_vendor.index.__f__("log", "at pages/hotelDetail/hotelDetail.vue:262", "选择的房型：", room);
      this.goToBookWithRoom(room);
    },
    // 跳转预订
    goToBook() {
      if (this.roomTypes.length > 0) {
        common_vendor.index.showToast({
          title: "请先选择房型",
          icon: "none"
        });
      } else {
        common_vendor.index.navigateTo({
          url: "/pages/booking/booking?id=" + this.id + "&name=" + encodeURIComponent(this.hotel.name || "酒店") + "&price=" + (this.hotel.minPrice || this.hotel.price || 0) + "&username=" + this.username + "&merchantId=" + (this.hotel.merchantId || "")
        });
      }
    },
    // 带房型预订
    goToBookWithRoom(room) {
      common_vendor.index.navigateTo({
        url: "/pages/booking/booking?id=" + this.id + "&name=" + encodeURIComponent(this.hotel.name || "酒店") + "&price=" + room.price + "&roomTypeId=" + room.id + "&roomTypeName=" + encodeURIComponent(room.typeName) + "&username=" + this.username + "&merchantId=" + (this.hotel.merchantId || "")
      });
    },
    // 检查收藏
    checkCollect() {
      if (!this.username)
        return;
      const key = "myCollection_" + this.username;
      let data = common_vendor.index.getStorageSync(key) || [];
      this.isCollected = data.some((item) => item.id == this.id);
    },
    // 切换收藏
    toggleCollect() {
      if (!this.username) {
        common_vendor.index.showToast({ title: "请先登录", icon: "none" });
        setTimeout(() => {
          common_vendor.index.navigateTo({ url: "/pages/login-register/login-register" });
        }, 1500);
        return;
      }
      const key = "myCollection_" + this.username;
      let data = common_vendor.index.getStorageSync(key) || [];
      const index = data.findIndex((item) => item.id == this.id);
      if (index > -1) {
        data.splice(index, 1);
        this.isCollected = false;
        common_vendor.index.showToast({ title: "取消收藏", icon: "none" });
      } else {
        data.push(this.hotel);
        this.isCollected = true;
        common_vendor.index.showToast({ title: "收藏成功", icon: "success" });
      }
      common_vendor.index.setStorageSync(key, data);
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.f($data.images, (img, idx, i0) => {
      return {
        a: img,
        b: idx
      };
    }),
    b: common_vendor.t($data.isCollected ? "★" : "☆"),
    c: common_vendor.o((...args) => $options.toggleCollect && $options.toggleCollect(...args)),
    d: common_vendor.t($data.hotel.name || "酒店名称"),
    e: common_vendor.t($data.hotel.star || 5),
    f: common_vendor.t($data.avgScore),
    g: common_vendor.t($data.hotel.minPrice || $data.hotel.price || 0),
    h: common_vendor.t($data.hotel.address || "暂无地址"),
    i: $data.hotel.facilities
  }, $data.hotel.facilities ? {
    j: common_vendor.t($data.hotel.facilities)
  } : {}, {
    k: $data.roomTypes.length > 0
  }, $data.roomTypes.length > 0 ? {
    l: common_vendor.f($data.roomTypes, (room, k0, i0) => {
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
    })
  } : {}, {
    m: $data.commentList.length > 0
  }, $data.commentList.length > 0 ? {
    n: common_vendor.t($data.commentList.length)
  } : {}, {
    o: common_vendor.f($data.commentList, (item, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(item.username || "匿名用户"),
        b: common_vendor.t(item.score || item.rating),
        c: common_vendor.t($options.formatTime(item.createTime)),
        d: common_vendor.t(item.content || item.comment),
        e: item.reply && item.reply.trim()
      }, item.reply && item.reply.trim() ? {
        f: common_vendor.t(item.reply)
      } : {}, {
        g: item.id
      });
    }),
    p: $data.commentList.length === 0
  }, $data.commentList.length === 0 ? {} : {}, {
    q: common_vendor.t($data.hotel.minPrice || $data.hotel.price || 0),
    r: common_vendor.o((...args) => $options.goToBook && $options.goToBook(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-d2073f73"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/hotelDetail/hotelDetail.js.map
