"use strict";
const common_vendor = require("../../common/vendor.js");
const defaultUser = {
  name: "点击编辑",
  desc: "这家伙很懒，什么都没留下",
  avatar: "/static/img/5.jpg"
};
const _sfc_main = {
  name: "profile",
  data() {
    return {
      showEditModal: false,
      userInfo: { ...defaultUser },
      editForm: { name: "", desc: "" }
    };
  },
  onShow() {
    const token = common_vendor.index.getStorageSync("token");
    const username = common_vendor.index.getStorageSync("loginUsername");
    if (!token || !username) {
      common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
      return;
    }
    this.loadUserProfile(username);
  },
  methods: {
    // 加载对应用户的资料
    loadUserProfile(username) {
      const key = `userProfile_${username}`;
      const saved = common_vendor.index.getStorageSync(key);
      if (saved) {
        this.userInfo = saved;
      } else {
        this.userInfo = { ...defaultUser };
      }
    },
    goToOrders() {
      common_vendor.index.navigateTo({ url: "/pages/my-orders/my-orders" });
    },
    goToCart() {
      common_vendor.index.navigateTo({ url: "/pages/cart/cart" });
    },
    goToCollection() {
      common_vendor.index.navigateTo({ url: "/pages/collection/collection" });
    },
    // 退出登录
    logout() {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定退出登录吗？",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.removeStorageSync("token");
            common_vendor.index.removeStorageSync("loginUsername");
            common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
          }
        }
      });
    },
    // 保存【当前用户】自己的资料
    saveInfo() {
      const username = common_vendor.index.getStorageSync("loginUsername");
      if (!username)
        return;
      this.userInfo.name = this.editForm.name || this.userInfo.name;
      this.userInfo.desc = this.editForm.desc || this.userInfo.desc;
      const key = `userProfile_${username}`;
      common_vendor.index.setStorageSync(key, this.userInfo);
      this.showEditModal = false;
      common_vendor.index.showToast({ title: "保存成功", icon: "success" });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.userInfo.avatar,
    b: common_vendor.t($data.userInfo.name),
    c: common_vendor.t($data.userInfo.desc),
    d: common_vendor.o(($event) => $data.showEditModal = true),
    e: common_vendor.o((...args) => $options.goToOrders && $options.goToOrders(...args)),
    f: common_vendor.o((...args) => $options.goToCart && $options.goToCart(...args)),
    g: common_vendor.o((...args) => $options.goToCollection && $options.goToCollection(...args)),
    h: common_vendor.o((...args) => $options.logout && $options.logout(...args)),
    i: $data.showEditModal
  }, $data.showEditModal ? {
    j: $data.editForm.name,
    k: common_vendor.o(($event) => $data.editForm.name = $event.detail.value),
    l: $data.editForm.desc,
    m: common_vendor.o(($event) => $data.editForm.desc = $event.detail.value),
    n: common_vendor.o((...args) => $options.saveInfo && $options.saveInfo(...args)),
    o: common_vendor.o(() => {
    }),
    p: common_vendor.o(($event) => $data.showEditModal = false)
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-dd383ca2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/profile.js.map
