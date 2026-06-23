"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      userInfo: {},
      unreadCount: 0,
      // 通知未读数
      messageUnreadCount: 0,
      // 消息未读数
      avatarColor: "#f0e68c"
    };
  },
  computed: {
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
    statusTag() {
      const status = this.userInfo.status;
      if (status === "PENDING")
        return "待审核中";
      if (status === "REJECTED")
        return "审核未通过";
      if (status === "BANNED")
        return "已禁用";
      return "暂不可用";
    },
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
    statusDesc() {
      const status = this.userInfo.status;
      if (status === "PENDING")
        return "您的商家账号正在审核中，请等待管理员审核通过后使用全部功能。";
      if (status === "REJECTED")
        return "您的商家账号审核未通过，请联系管理员了解详情。";
      if (status === "BANNED")
        return "您的商家账号已被禁用，请联系管理员申诉。";
      return "";
    },
    avatarLetter() {
      const shopName = this.userInfo.shopName || this.userInfo.name || "商";
      const firstChar = shopName.charAt(0);
      return /[a-zA-Z]/.test(firstChar) ? firstChar.toUpperCase() : firstChar;
    }
  },
  onShow() {
    this.loadUserInfo();
    this.checkMerchantStatus();
    this.loadUnreadCount();
    this.loadMessageUnreadCount();
  },
  methods: {
    loadUserInfo() {
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        this.userInfo = userInfo || {};
        this.generateAvatarColor();
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/merchant/home.vue:180", "读取用户信息失败", e);
      }
    },
    generateAvatarColor() {
      const shopName = this.userInfo.shopName || this.userInfo.name || "商家";
      const colors = [
        "#f0e68c",
        "#ff6b6b",
        "#4ecdc4",
        "#45b7d1",
        "#96ceb4",
        "#ffeaa7",
        "#dfe6e9",
        "#74b9ff",
        "#a29bfe",
        "#fd79a8",
        "#00cec9",
        "#e17055",
        "#d63031",
        "#6c5ce7",
        "#a8e6cf"
      ];
      let sum = 0;
      for (let i = 0; i < shopName.length; i++) {
        sum += shopName.charCodeAt(i);
      }
      const index = sum % colors.length;
      this.avatarColor = colors[index];
    },
    // 加载通知未读数
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
          common_vendor.index.__f__("log", "at pages/merchant/home.vue:216", "获取未读数量失败");
        }
      });
    },
    // 加载消息未读数 - 直接从后端数据库读取
    loadMessageUnreadCount() {
      const merchantId = this.userInfo.id;
      if (!merchantId) {
        common_vendor.index.__f__("log", "at pages/merchant/home.vue:225", "商家ID不存在，无法获取消息未读数");
        this.messageUnreadCount = 0;
        return;
      }
      common_vendor.index.__f__("log", "at pages/merchant/home.vue:230", "=== 从后端获取商家总未读数 ===");
      common_vendor.index.__f__("log", "at pages/merchant/home.vue:231", "商家ID:", merchantId);
      common_vendor.index.request({
        url: `http://localhost:8080/api/messages/merchant/total-unread?merchantId=${merchantId}`,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/home.vue:237", "后端返回完整数据:", res.data);
          if (res.data && res.data.code === 200) {
            this.messageUnreadCount = res.data.data || 0;
            common_vendor.index.__f__("log", "at pages/merchant/home.vue:240", "✅ 主页显示未读数:", this.messageUnreadCount);
          } else {
            common_vendor.index.__f__("log", "at pages/merchant/home.vue:242", "❌ 后端返回异常:", res.data);
            this.messageUnreadCount = 0;
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/merchant/home.vue:247", "❌ 获取消息未读数失败:", err);
          this.messageUnreadCount = 0;
        }
      });
    },
    // 检查商家状态
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
          content = "您的商家账号已被禁用，请联系管理员申诉。";
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
    // 核心校验方法：检查商家状态是否允许操作
    checkStatusAndAlert() {
      const status = this.userInfo.status;
      if (status !== "NORMAL") {
        let message = "";
        if (status === "PENDING") {
          message = "账号正在审核中，暂无法使用此功能";
        } else if (status === "REJECTED") {
          message = "账号审核未通过，暂无法使用此功能";
        } else if (status === "BANNED") {
          message = "商家已被禁用，暂无法使用此功能";
        } else {
          message = "账号状态异常，暂无法使用此功能";
        }
        common_vendor.index.showToast({
          title: message,
          icon: "none",
          duration: 2e3
        });
        return false;
      }
      return true;
    },
    // 导航跳转 - 统一校验
    navigateTo(url) {
      if (this.checkStatusAndAlert()) {
        common_vendor.index.navigateTo({ url });
      }
    },
    goToNotifications() {
      if (this.checkStatusAndAlert()) {
        common_vendor.index.navigateTo({ url: "/pages/merchant/notifications" });
      }
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
    a: common_vendor.t($options.avatarLetter),
    b: $data.avatarColor,
    c: common_vendor.t($data.userInfo.shopName || "商家名称"),
    d: common_vendor.t($data.userInfo.name),
    e: $data.userInfo.status && $data.userInfo.status !== "NORMAL"
  }, $data.userInfo.status && $data.userInfo.status !== "NORMAL" ? {
    f: common_vendor.t($options.statusText),
    g: common_vendor.n($options.statusClass)
  } : {}, {
    h: $data.unreadCount > 0
  }, $data.unreadCount > 0 ? {
    i: common_vendor.t($data.unreadCount > 99 ? "99+" : $data.unreadCount)
  } : {}, {
    j: common_vendor.o((...args) => $options.goToNotifications && $options.goToNotifications(...args)),
    k: $data.userInfo.status && $data.userInfo.status !== "NORMAL"
  }, $data.userInfo.status && $data.userInfo.status !== "NORMAL" ? {
    l: common_vendor.t($options.statusIcon),
    m: common_vendor.t($options.statusTitle),
    n: common_vendor.t($options.statusDesc)
  } : {}, {
    o: $data.userInfo.status && $data.userInfo.status !== "NORMAL"
  }, $data.userInfo.status && $data.userInfo.status !== "NORMAL" ? {
    p: common_vendor.o((...args) => $options.goToAppeal && $options.goToAppeal(...args))
  } : {}, {
    q: $data.userInfo.status === "NORMAL"
  }, $data.userInfo.status === "NORMAL" ? common_vendor.e({
    r: common_vendor.o(($event) => $options.navigateTo("/pages/merchant/hotel-list")),
    s: common_vendor.o(($event) => $options.navigateTo("/pages/merchant/order-list")),
    t: common_vendor.o(($event) => $options.navigateTo("/pages/merchant/comment-list")),
    v: $data.messageUnreadCount > 0
  }, $data.messageUnreadCount > 0 ? {
    w: common_vendor.t($data.messageUnreadCount > 99 ? "99+" : $data.messageUnreadCount)
  } : {}, {
    x: common_vendor.o(($event) => $options.navigateTo("/pages/merchant/messages")),
    y: common_vendor.o(($event) => $options.navigateTo("/pages/merchant/analysis")),
    z: common_vendor.o(($event) => $options.navigateTo("/pages/merchant/setting"))
  }) : {
    A: common_vendor.t($options.statusTag),
    B: common_vendor.t($options.statusTag),
    C: common_vendor.t($options.statusTag),
    D: common_vendor.t($options.statusTag),
    E: common_vendor.t($options.statusTag)
  }, {
    F: common_vendor.o((...args) => $options.logout && $options.logout(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-e5d2a991"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/merchant/home.js.map
