"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      userInfo: {},
      unreadCount: 0
    };
  },
  computed: {
    // 状态样式类
    statusClass() {
      const status = this.userInfo.status;
      if (status === "PENDING")
        return "status-pending";
      if (status === "REJECTED")
        return "status-rejected";
      if (status === "BANNED")
        return "status-banned";
      return "";
    },
    // 状态简短文字
    statusText() {
      const status = this.userInfo.status;
      if (status === "PENDING")
        return "待审核";
      if (status === "REJECTED")
        return "已拒绝";
      if (status === "BANNED")
        return "已禁用";
      return "";
    },
    // 状态图标
    statusIcon() {
      const status = this.userInfo.status;
      if (status === "PENDING")
        return "⏳";
      if (status === "REJECTED")
        return "❌";
      if (status === "BANNED")
        return "🔒";
      return "";
    },
    // 状态标题
    statusTitle() {
      const status = this.userInfo.status;
      if (status === "PENDING")
        return "账号待审核";
      if (status === "REJECTED")
        return "审核未通过";
      if (status === "BANNED")
        return "账号已禁用";
      return "";
    },
    // 状态描述
    statusDesc() {
      const status = this.userInfo.status;
      if (status === "PENDING")
        return "您的商家账号正在审核中，请等待管理员审核通过后使用全部功能。";
      if (status === "REJECTED")
        return "您的商家账号审核未通过，请联系管理员了解详情。";
      if (status === "BANNED")
        return "您的商家账号已被禁用，请联系管理员。";
      return "";
    }
  },
  onShow() {
    this.loadUserInfo();
    this.checkMerchantStatus();
    this.loadUnreadCount();
  },
  methods: {
    loadUserInfo() {
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        this.userInfo = userInfo || {};
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/merchant/home.vue:158", "读取用户信息失败", e);
      }
    },
    loadUnreadCount() {
      const token = common_vendor.index.getStorageSync("token");
      if (!token)
        return;
      common_vendor.index.request({
        url: "http://localhost:8080/api/merchant/notifications/unread-count",
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
          common_vendor.index.__f__("log", "at pages/merchant/home.vue:178", "获取未读数量失败");
        }
      });
    },
    checkMerchantStatus() {
      const status = this.userInfo.status;
      if (status && status !== "NORMAL") {
        let title = "";
        let content = "";
        if (status === "PENDING") {
          title = "账号待审核";
          content = "您的商家账号正在审核中，请等待管理员审核通过后使用全部功能。";
        } else if (status === "REJECTED") {
          title = "审核未通过";
          content = "您的商家账号审核未通过，请联系管理员了解详情。";
        } else if (status === "BANNED") {
          title = "账号已禁用";
          content = "您的商家账号已被禁用，请联系管理员。";
        }
        if (title) {
          common_vendor.index.showModal({
            title,
            content,
            showCancel: false,
            confirmText: "我知道了",
            success: (res) => {
              if (res.confirm) {
                if (status === "REJECTED" || status === "BANNED") {
                  setTimeout(() => {
                    common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
                  }, 2e3);
                }
              }
            }
          });
        }
      }
    },
    navigateTo(url) {
      if (this.userInfo.status !== "NORMAL") {
        common_vendor.index.showToast({
          title: this.statusText + "，无法使用此功能",
          icon: "none"
        });
        return;
      }
      common_vendor.index.navigateTo({ url });
    },
    goToNotifications() {
      common_vendor.index.navigateTo({
        url: "/pages/merchant/notifications"
      });
    },
    goToAppeal() {
      common_vendor.index.navigateTo({
        url: "/pages/merchant/appeal"
      });
    },
    logout() {
      common_vendor.index.clearStorageSync();
      common_vendor.index.reLaunch({
        url: "/pages/login-register/login-register"
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.userInfo.avatar || "/static/default-avatar.png",
    b: common_vendor.t($data.userInfo.shopName || "商家名称"),
    c: common_vendor.t($data.userInfo.name),
    d: $data.userInfo.status && $data.userInfo.status !== "NORMAL"
  }, $data.userInfo.status && $data.userInfo.status !== "NORMAL" ? {
    e: common_vendor.t($options.statusText),
    f: common_vendor.n($options.statusClass)
  } : {}, {
    g: $data.unreadCount > 0
  }, $data.unreadCount > 0 ? {
    h: common_vendor.t($data.unreadCount > 99 ? "99+" : $data.unreadCount)
  } : {}, {
    i: common_vendor.o((...args) => $options.goToNotifications && $options.goToNotifications(...args)),
    j: $data.userInfo.status && $data.userInfo.status !== "NORMAL"
  }, $data.userInfo.status && $data.userInfo.status !== "NORMAL" ? {
    k: common_vendor.t($options.statusIcon),
    l: common_vendor.t($options.statusTitle),
    m: common_vendor.t($options.statusDesc)
  } : {}, {
    n: $data.userInfo.status && $data.userInfo.status !== "NORMAL"
  }, $data.userInfo.status && $data.userInfo.status !== "NORMAL" ? {
    o: common_vendor.o((...args) => $options.goToAppeal && $options.goToAppeal(...args))
  } : {}, {
    p: $data.userInfo.status === "NORMAL"
  }, $data.userInfo.status === "NORMAL" ? {
    q: common_vendor.o(($event) => $options.navigateTo("/pages/merchant/hotel-list")),
    r: common_vendor.o(($event) => $options.navigateTo("/pages/merchant/order-list")),
    s: common_vendor.o(($event) => $options.navigateTo("/pages/merchant/comment-list")),
    t: common_vendor.o(($event) => $options.navigateTo("/pages/merchant/messages")),
    v: common_vendor.o(($event) => $options.navigateTo("/pages/merchant/setting"))
  } : {}, {
    w: common_vendor.o((...args) => $options.logout && $options.logout(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/merchant/home.js.map
