"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const _sfc_main = {
  name: "profile",
  data() {
    return {
      showAboutModal: false,
      userInfo: {},
      orderCount: 0,
      collectionCount: 0,
      unreadCount: 0,
      token: ""
    };
  },
  computed: {
    avatarUrl() {
      if (this.userInfo.avatar) {
        if (this.userInfo.avatar.startsWith("http")) {
          return this.userInfo.avatar;
        }
        if (this.userInfo.avatar.startsWith("/file")) {
          return "http://localhost:8080" + this.userInfo.avatar;
        }
        return this.userInfo.avatar;
      }
      return "/static/default-avatar.png";
    }
  },
  onShow() {
    const token = common_vendor.index.getStorageSync("token");
    const username = common_vendor.index.getStorageSync("loginUsername");
    if (!token || !username) {
      common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
      return;
    }
    this.token = token;
    this.loadUserProfile();
    this.loadStats();
    this.loadUnreadCount();
  },
  methods: {
    loadUserProfile() {
      const storedUserInfo = common_vendor.index.getStorageSync("userInfo");
      if (storedUserInfo && Object.keys(storedUserInfo).length > 0) {
        this.userInfo = {
          id: storedUserInfo.id,
          username: storedUserInfo.username || storedUserInfo.name,
          bio: storedUserInfo.bio || storedUserInfo.desc,
          avatar: storedUserInfo.avatar,
          phone: storedUserInfo.phone,
          email: storedUserInfo.email,
          role: storedUserInfo.role
        };
      } else {
        this.fetchUserInfoFromServer();
      }
    },
    fetchUserInfoFromServer() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/users/userinfo",
        method: "GET",
        header: {
          "Authorization": "Bearer " + this.token
        },
        success: (res) => {
          if (res.statusCode === 200 && res.data) {
            const userData = res.data;
            this.userInfo = {
              id: userData.id,
              username: userData.username,
              bio: userData.bio || "",
              avatar: userData.avatar || "",
              phone: userData.phone || "",
              email: userData.email || "",
              role: userData.role
            };
            common_vendor.index.setStorageSync("userInfo", this.userInfo);
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/profile/profile.vue:187", "获取用户信息失败", err);
        }
      });
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
        },
        fail: () => {
          this.orderCount = 0;
        }
      });
      const key = `myCollection_${username}`;
      const collections = common_vendor.index.getStorageSync(key) || [];
      this.collectionCount = collections.length;
    },
    loadUnreadCount() {
      if (!this.token)
        return;
      common_vendor.index.request({
        url: "http://localhost:8080/api/user/notifications/unread-count",
        method: "GET",
        header: {
          "Authorization": "Bearer " + this.token
        },
        success: (res) => {
          if (res.data && res.data.code === 200) {
            this.unreadCount = res.data.data.count || 0;
          }
        },
        fail: () => {
          common_vendor.index.__f__("log", "at pages/profile/profile.vue:233", "获取未读数量失败");
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
    d: $options.avatarUrl,
    e: common_vendor.t($data.userInfo.username || $data.userInfo.name || "用户"),
    f: common_vendor.t($data.userInfo.bio || $data.userInfo.desc || "这家伙很懒，什么都没留下"),
    g: common_vendor.t($data.orderCount),
    h: common_vendor.o((...args) => $options.goToOrders && $options.goToOrders(...args)),
    i: common_vendor.t($data.collectionCount),
    j: common_vendor.o((...args) => $options.goToCollection && $options.goToCollection(...args)),
    k: common_vendor.o((...args) => $options.goToOrders && $options.goToOrders(...args)),
    l: common_vendor.o((...args) => $options.goToCollection && $options.goToCollection(...args)),
    m: common_vendor.o((...args) => $options.goToSetting && $options.goToSetting(...args)),
    n: common_vendor.o((...args) => $options.showAbout && $options.showAbout(...args)),
    o: common_vendor.o((...args) => $options.showFeedback && $options.showFeedback(...args)),
    p: common_vendor.o((...args) => $options.logout && $options.logout(...args)),
    q: $data.showAboutModal
  }, $data.showAboutModal ? {
    r: common_assets._imports_0,
    s: common_vendor.o(($event) => $data.showAboutModal = false),
    t: common_vendor.o(() => {
    }),
    v: common_vendor.o(($event) => $data.showAboutModal = false)
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-dd383ca2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/profile.js.map
