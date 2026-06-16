"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
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
      showAboutModal: false,
      userInfo: { ...defaultUser },
      editForm: { name: "", desc: "" },
      orderCount: 0,
      collectionCount: 0,
      unreadCount: 0
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
    this.loadStats();
    this.loadUnreadCount();
  },
  methods: {
    loadUserProfile(username) {
      const key = `userProfile_${username}`;
      const saved = common_vendor.index.getStorageSync(key);
      if (saved) {
        this.userInfo = saved;
      } else {
        this.userInfo = { ...defaultUser };
      }
    },
    loadStats() {
      const username = common_vendor.index.getStorageSync("loginUsername");
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotel-orders/user?username=${username}`,
        method: "GET",
        success: (res) => {
          let orders = [];
          if (res.data && res.data.code === 200) {
            orders = res.data.data || [];
          } else if (Array.isArray(res.data)) {
            orders = res.data;
          }
          this.orderCount = orders.length;
        }
      });
      const key = `myCollection_${username}`;
      const collections = common_vendor.index.getStorageSync(key) || [];
      this.collectionCount = collections.length;
    },
    loadUnreadCount() {
      const token = common_vendor.index.getStorageSync("token");
      if (!token)
        return;
      common_vendor.index.request({
        url: "http://localhost:8080/api/user/notifications/unread-count",
        method: "GET",
        header: {
          "Authorization": "Bearer " + token
        },
        success: (res) => {
          if (res.data && res.data.code === 200) {
            this.unreadCount = res.data.data.count || 0;
          }
        },
        fail: () => {
          common_vendor.index.__f__("log", "at pages/profile/profile.vue:205", "获取未读数量失败");
        }
      });
    },
    goToOrders() {
      common_vendor.index.navigateTo({ url: "/pages/my-orders/my-orders" });
    },
    goToCollection() {
      common_vendor.index.navigateTo({ url: "/pages/collection/collection" });
    },
    goToSetting() {
      common_vendor.index.navigateTo({ url: "/pages/profile/setting" });
    },
    goToNotifications() {
      common_vendor.index.navigateTo({ url: "/pages/profile/notifications" });
    },
    showAbout() {
      this.showAboutModal = true;
    },
    showFeedback() {
      common_vendor.index.showModal({
        title: "意见反馈",
        content: "请将您的意见发送至：service@example.com",
        showCancel: false,
        confirmText: "我知道了"
      });
    },
    logout() {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定退出登录吗？",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.clearStorageSync();
            common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
          }
        }
      });
    },
    saveInfo() {
      const username = common_vendor.index.getStorageSync("loginUsername");
      if (!username)
        return;
      if (this.editForm.name) {
        this.userInfo.name = this.editForm.name;
      }
      if (this.editForm.desc) {
        this.userInfo.desc = this.editForm.desc;
      }
      const key = `userProfile_${username}`;
      common_vendor.index.setStorageSync(key, this.userInfo);
      this.showEditModal = false;
      this.editForm = { name: "", desc: "" };
      common_vendor.index.showToast({ title: "保存成功", icon: "success" });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.unreadCount > 0
  }, $data.unreadCount > 0 ? {
    b: common_vendor.t($data.unreadCount > 99 ? "99+" : $data.unreadCount)
  } : {}, {
    c: common_vendor.o((...args) => $options.goToNotifications && $options.goToNotifications(...args)),
    d: $data.userInfo.avatar,
    e: common_vendor.t($data.userInfo.name),
    f: common_vendor.t($data.userInfo.desc),
    g: common_vendor.o(($event) => $data.showEditModal = true),
    h: common_vendor.t($data.orderCount),
    i: common_vendor.o((...args) => $options.goToOrders && $options.goToOrders(...args)),
    j: common_vendor.t($data.collectionCount),
    k: common_vendor.o((...args) => $options.goToCollection && $options.goToCollection(...args)),
    l: common_vendor.o((...args) => $options.goToOrders && $options.goToOrders(...args)),
    m: common_vendor.o((...args) => $options.goToCollection && $options.goToCollection(...args)),
    n: common_vendor.o((...args) => $options.goToSetting && $options.goToSetting(...args)),
    o: common_vendor.o((...args) => $options.showAbout && $options.showAbout(...args)),
    p: common_vendor.o((...args) => $options.showFeedback && $options.showFeedback(...args)),
    q: common_vendor.o((...args) => $options.logout && $options.logout(...args)),
    r: $data.showEditModal
  }, $data.showEditModal ? {
    s: common_vendor.o(($event) => $data.showEditModal = false),
    t: $data.editForm.name,
    v: common_vendor.o(($event) => $data.editForm.name = $event.detail.value),
    w: $data.editForm.desc,
    x: common_vendor.o(($event) => $data.editForm.desc = $event.detail.value),
    y: common_vendor.o((...args) => $options.saveInfo && $options.saveInfo(...args)),
    z: common_vendor.o(() => {
    }),
    A: common_vendor.o(($event) => $data.showEditModal = false)
  } : {}, {
    B: $data.showAboutModal
  }, $data.showAboutModal ? {
    C: common_assets._imports_0,
    D: common_vendor.o(($event) => $data.showAboutModal = false),
    E: common_vendor.o(() => {
    }),
    F: common_vendor.o(($event) => $data.showAboutModal = false)
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-dd383ca2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/profile.js.map
