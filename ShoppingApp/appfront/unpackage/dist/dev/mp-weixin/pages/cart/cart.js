"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      cart: null,
      items: []
    };
  },
  computed: {
    totalPrice() {
      return this.items.reduce((sum, item) => sum + (item.ticketPrice || 0) * item.quantity, 0).toFixed(2);
    }
  },
  onShow() {
    this.loadCart();
  },
  methods: {
    getUsername() {
      return common_vendor.index.getStorageSync("loginUsername") || "";
    },
    loadCart() {
      const username = this.getUsername();
      if (!username) {
        this.items = [];
        return;
      }
      common_vendor.index.request({
        url: "http://localhost:8080/api/cart?username=" + encodeURIComponent(username),
        method: "GET",
        success: (res) => {
          this.cart = res.data;
          this.items = res.data && res.data.items || [];
        },
        fail: () => {
          this.items = [];
        }
      });
    },
    increaseQty(item) {
      const qty = (item.quantity || 0) + 1;
      this.updateQty(item.id, qty);
    },
    decreaseQty(item) {
      const qty = (item.quantity || 0) - 1;
      if (qty <= 0) {
        this.removeItem(item);
        return;
      }
      this.updateQty(item.id, qty);
    },
    updateQty(itemId, quantity) {
      const username = this.getUsername();
      common_vendor.index.request({
        url: `http://localhost:8080/api/cart/item/${itemId}?username=${encodeURIComponent(username)}`,
        method: "PUT",
        data: { quantity },
        success: () => {
          this.loadCart();
        }
      });
    },
    removeItem(item) {
      const username = this.getUsername();
      common_vendor.index.showModal({
        title: "提示",
        content: "确定删除此商品？",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.request({
              url: `http://localhost:8080/api/cart/item/${item.id}?username=${encodeURIComponent(username)}`,
              method: "DELETE",
              success: () => {
                this.loadCart();
                common_vendor.index.showToast({ title: "已删除" });
              }
            });
          }
        }
      });
    },
    clearCart() {
      const username = this.getUsername();
      common_vendor.index.showModal({
        title: "提示",
        content: "确定清空购物车？",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.request({
              url: `http://localhost:8080/api/cart/clear?username=${encodeURIComponent(username)}`,
              method: "DELETE",
              success: () => {
                this.loadCart();
                common_vendor.index.showToast({ title: "已清空" });
              }
            });
          }
        }
      });
    },
    checkout() {
      common_vendor.index.showToast({ title: "结算功能开发中", icon: "none" });
    }
  }
};
if (!Array) {
  const _component_RouteFloat = common_vendor.resolveComponent("RouteFloat");
  _component_RouteFloat();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.items.length > 0
  }, $data.items.length > 0 ? {
    b: common_vendor.o((...args) => $options.clearCart && $options.clearCart(...args))
  } : {}, {
    c: $data.items.length === 0
  }, $data.items.length === 0 ? {} : {
    d: common_vendor.f($data.items, (item, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(item.scenicName),
        b: common_vendor.t(item.ticketPrice || 0),
        c: item.routeId
      }, item.routeId ? {} : {}, {
        d: common_vendor.o(($event) => $options.decreaseQty(item), item.id),
        e: common_vendor.t(item.quantity),
        f: common_vendor.o(($event) => $options.increaseQty(item), item.id),
        g: common_vendor.t(((item.ticketPrice || 0) * item.quantity).toFixed(2)),
        h: common_vendor.o(($event) => $options.removeItem(item), item.id),
        i: item.id
      });
    })
  }, {
    e: $data.items.length > 0
  }, $data.items.length > 0 ? {
    f: common_vendor.t($options.totalPrice),
    g: common_vendor.o((...args) => $options.checkout && $options.checkout(...args))
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-c91e7611"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/cart/cart.js.map
