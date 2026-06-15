"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      notificationList: [],
      unreadCount: 0
    };
  },
  onShow() {
    this.loadNotifications();
  },
  methods: {
    loadNotifications() {
      const token = common_vendor.index.getStorageSync("token");
      if (!token)
        return;
      common_vendor.index.showLoading({ title: "加载中..." });
      common_vendor.index.request({
        url: "http://localhost:8080/api/user/notifications",
        method: "GET",
        header: {
          "Authorization": "Bearer " + token
        },
        success: (res) => {
          common_vendor.index.hideLoading();
          if (res.data && res.data.code === 200) {
            this.notificationList = res.data.data || [];
            this.unreadCount = this.notificationList.filter((n) => n.status === "UNREAD").length;
          }
        },
        fail: () => {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({ title: "加载失败", icon: "none" });
        }
      });
    },
    markAllAsRead() {
      const token = common_vendor.index.getStorageSync("token");
      if (!token)
        return;
      common_vendor.index.request({
        url: "http://localhost:8080/api/user/notifications/read-all",
        method: "PUT",
        header: {
          "Authorization": "Bearer " + token
        },
        success: (res) => {
          if (res.data && res.data.code === 200) {
            this.notificationList.forEach((n) => {
              n.status = "READ";
            });
            this.unreadCount = 0;
            common_vendor.index.showToast({ title: "已全部标记为已读", icon: "success" });
          }
        }
      });
    },
    markAsRead(id) {
      const token = common_vendor.index.getStorageSync("token");
      if (!token)
        return;
      common_vendor.index.request({
        url: `http://localhost:8080/api/user/notifications/${id}/read`,
        method: "PUT",
        header: {
          "Authorization": "Bearer " + token
        }
      });
    },
    viewDetail(item) {
      if (item.status === "UNREAD") {
        this.markAsRead(item.id);
        item.status = "READ";
        this.unreadCount--;
      }
      common_vendor.index.showModal({
        title: item.title,
        content: item.content,
        showCancel: false,
        confirmText: "我知道了"
      });
    },
    getIcon(type) {
      if (type === "USER_BANNED")
        return "🔇";
      if (type === "USER_UNBANNED")
        return "✅";
      return "📢";
    },
    getIconClass(type) {
      if (type === "USER_BANNED")
        return "icon-danger";
      if (type === "USER_UNBANNED")
        return "icon-success";
      return "";
    },
    formatTime(timeStr) {
      if (!timeStr)
        return "";
      try {
        const date = new Date(timeStr);
        const now = /* @__PURE__ */ new Date();
        const diff = now - date;
        const minutes = Math.floor(diff / (1e3 * 60));
        const hours = Math.floor(diff / (1e3 * 60 * 60));
        const days = Math.floor(diff / (1e3 * 60 * 60 * 24));
        if (minutes < 1)
          return "刚刚";
        if (minutes < 60)
          return `${minutes}分钟前`;
        if (hours < 24)
          return `${hours}小时前`;
        if (days < 7)
          return `${days}天前`;
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${month}-${day}`;
      } catch (e) {
        return timeStr;
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.unreadCount > 0
  }, $data.unreadCount > 0 ? {
    b: common_vendor.o((...args) => $options.markAllAsRead && $options.markAllAsRead(...args))
  } : {}, {
    c: $data.unreadCount > 0
  }, $data.unreadCount > 0 ? {
    d: common_vendor.t($data.unreadCount)
  } : {}, {
    e: $data.notificationList.length > 0
  }, $data.notificationList.length > 0 ? {
    f: common_vendor.f($data.notificationList, (item, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t($options.getIcon(item.type)),
        b: common_vendor.n($options.getIconClass(item.type)),
        c: common_vendor.t(item.title),
        d: common_vendor.t($options.formatTime(item.createTime)),
        e: common_vendor.t(item.content),
        f: item.status === "UNREAD"
      }, item.status === "UNREAD" ? {} : {}, {
        g: item.id,
        h: item.status === "UNREAD" ? 1 : "",
        i: common_vendor.o(($event) => $options.viewDetail(item), item.id)
      });
    })
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-b7870fea"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/notifications.js.map
