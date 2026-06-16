"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      activeTab: 1,
      orderList: [],
      flightOrders: [],
      username: ""
    };
  },
  onShow() {
    this.username = common_vendor.index.getStorageSync("loginUsername") || "";
    this.loadOrderData();
  },
  methods: {
    switchTab(type) {
      this.activeTab = type;
      this.loadOrderData();
    },
    loadOrderData() {
      if (this.activeTab === 1) {
        this.getHotelOrders();
      } else {
        this.getFlightOrders();
      }
    },
    // 酒店订单
    getHotelOrders() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotel-orders/user?username=" + this.username,
        method: "GET",
        success: (res) => {
          this.orderList = (res.data || []).map((item) => {
            item.content = "";
            item.score = 5;
            item.commented = false;
            return item;
          });
          this.checkAllCommentedOrders();
        }
      });
    },
    // 🔥 修复：获取所有评价，标记已评价订单
    checkAllCommentedOrders() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotel-comments/all",
        success: (res) => {
          const commentedOrderIds = res.data.map((c) => c.orderId);
          this.orderList.forEach((item) => {
            if (commentedOrderIds.includes(item.id)) {
              item.commented = true;
            }
          });
        }
      });
    },
    // 提交评价
    submitComment(item) {
      if (!item.content)
        return common_vendor.index.showToast({ title: "请输入评价内容", icon: "none" });
      if (item.commented)
        return common_vendor.index.showToast({ title: "已评价", icon: "none" });
      common_vendor.index.request({
        url: "http://localhost:8080/api/hotel-comments",
        method: "POST",
        data: {
          hotelId: item.hotelId,
          orderId: item.id,
          content: item.content,
          score: item.score
        },
        success: (res) => {
          if (res.data === "已评价") {
            common_vendor.index.showToast({ title: "该订单已评价", icon: "none" });
            item.commented = true;
          } else {
            common_vendor.index.showToast({ title: "评价成功" });
            item.commented = true;
          }
        }
      });
    },
    // 机票订单
    getFlightOrders() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/flight-orders",
        method: "GET",
        success: (res) => {
          let all = res.data || [];
          this.flightOrders = all.filter((item) => item.loginUsername === this.username);
        }
      });
    }
  }
};
if (!Array) {
  const _component_RouteFloat = common_vendor.resolveComponent("RouteFloat");
  _component_RouteFloat();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.n($data.activeTab === 1 ? "active" : ""),
    b: common_vendor.o(($event) => $options.switchTab(1)),
    c: common_vendor.n($data.activeTab === 2 ? "active" : ""),
    d: common_vendor.o(($event) => $options.switchTab(2)),
    e: $data.activeTab === 1
  }, $data.activeTab === 1 ? common_vendor.e({
    f: $data.orderList.length === 0
  }, $data.orderList.length === 0 ? {} : {}, {
    g: common_vendor.f($data.orderList, (item, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(item.price),
        b: common_vendor.t(item.name),
        c: common_vendor.t(item.checkIn),
        d: common_vendor.t(item.checkOut),
        e: !item.commented
      }, !item.commented ? {
        f: common_vendor.f(5, (n, k1, i1) => {
          return {
            a: n,
            b: n <= item.score ? "#ffbc36" : "#ccc",
            c: common_vendor.o(($event) => item.score = n, n)
          };
        }),
        g: item.content,
        h: common_vendor.o(($event) => item.content = $event.detail.value, item.id),
        i: common_vendor.o(($event) => $options.submitComment(item), item.id)
      } : {}, {
        j: item.id
      });
    })
  }) : {}, {
    h: $data.activeTab === 2
  }, $data.activeTab === 2 ? common_vendor.e({
    i: $data.flightOrders.length === 0
  }, $data.flightOrders.length === 0 ? {} : {}, {
    j: common_vendor.f($data.flightOrders, (item, k0, i0) => {
      return {
        a: common_vendor.t(item.price),
        b: common_vendor.t(item.flightNumber),
        c: common_vendor.t(item.departCity),
        d: common_vendor.t(item.arriveCity),
        e: common_vendor.t(item.departTime),
        f: common_vendor.t(item.userName),
        g: item.id
      };
    })
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-5fe9fe45"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/my-orders/my-orders.js.map
