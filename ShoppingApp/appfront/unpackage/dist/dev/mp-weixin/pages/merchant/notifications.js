"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      notificationList: [],
      unreadCount: 0,
      userInfo: {},
      // 酒店申诉弹窗
      showHotelAppealModal: false,
      appealHotelId: null,
      appealHotelName: "",
      appealHotelReason: "",
      appealType: "",
      appealContent: "",
      contactInfo: "",
      appealTypes: ["酒店解封", "审核咨询", "信息修改", "投诉建议", "其他问题"],
      // 申诉回复弹窗
      showReplyModal: false,
      replyTitle: "",
      replyContent: "",
      replyMessage: ""
    };
  },
  onShow() {
    this.loadUserInfo();
    this.loadNotifications();
  },
  methods: {
    loadUserInfo() {
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        this.userInfo = userInfo || {};
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/merchant/notifications.vue:166", "读取用户信息失败", e);
      }
    },
    loadNotifications() {
      const token = common_vendor.index.getStorageSync("token");
      if (!token)
        return;
      common_vendor.index.showLoading({ title: "加载中..." });
      common_vendor.index.request({
        url: "http://localhost:8080/api/merchant/notifications",
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
        url: "http://localhost:8080/api/merchant/notifications/read-all",
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
        },
        fail: () => {
          common_vendor.index.showToast({ title: "操作失败", icon: "none" });
        }
      });
    },
    markAsRead(id) {
      const token = common_vendor.index.getStorageSync("token");
      if (!token)
        return;
      common_vendor.index.request({
        url: `http://localhost:8080/api/merchant/notifications/${id}/read`,
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
      if (item.type === "HOTEL_BANNED") {
        this.openHotelAppealModal(item.relatedId, item.relatedName, item.content);
      } else if (item.type === "HOTEL_UNBANNED") {
        common_vendor.index.showModal({
          title: item.title,
          content: item.content,
          showCancel: false,
          confirmText: "我知道了"
        });
      } else if (item.type === "APPEAL_REPLY") {
        this.showReplyModal = true;
        this.replyTitle = item.title;
        this.replyContent = item.content;
        this.replyMessage = item.reply || "暂无回复内容";
      } else if (item.type === "APPEAL_APPROVED") {
        common_vendor.index.showModal({
          title: item.title,
          content: item.content,
          showCancel: false,
          confirmText: "我知道了"
        });
      } else if (item.type === "APPEAL_REJECTED") {
        common_vendor.index.showModal({
          title: item.title,
          content: item.content,
          showCancel: false,
          confirmText: "我知道了"
        });
      } else {
        common_vendor.index.showModal({
          title: item.title,
          content: item.content,
          showCancel: false,
          confirmText: "我知道了"
        });
      }
    },
    openHotelAppealModal(hotelId, hotelName, content) {
      this.appealHotelId = hotelId;
      this.appealHotelName = hotelName;
      const reasonMatch = content.match(/原因：(.*?)。/);
      this.appealHotelReason = reasonMatch ? reasonMatch[1] : "违规经营";
      this.appealType = "";
      this.appealContent = "";
      this.contactInfo = "";
      this.showHotelAppealModal = true;
    },
    closeHotelAppealModal() {
      this.showHotelAppealModal = false;
    },
    onAppealTypeChange(e) {
      this.appealType = this.appealTypes[e.detail.value];
    },
    submitHotelAppeal() {
      if (!this.appealType) {
        common_vendor.index.showToast({ title: "请选择申诉类型", icon: "none" });
        return;
      }
      if (!this.appealContent.trim()) {
        common_vendor.index.showToast({ title: "请填写申诉内容", icon: "none" });
        return;
      }
      if (!this.contactInfo.trim()) {
        common_vendor.index.showToast({ title: "请填写联系方式", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "提交中..." });
      common_vendor.index.request({
        url: "http://localhost:8080/api/appeals/submit",
        method: "POST",
        data: {
          username: this.userInfo.name,
          shopName: this.userInfo.shopName,
          status: this.userInfo.status,
          type: this.appealType,
          content: `【酒店申诉】酒店ID:${this.appealHotelId}，酒店名称:${this.appealHotelName}
禁用原因:${this.appealHotelReason}

申诉内容:${this.appealContent}`,
          contact: this.contactInfo
        },
        success: (res) => {
          var _a;
          common_vendor.index.hideLoading();
          if (res.data && (res.data.code === 200 || res.data.success)) {
            common_vendor.index.showToast({
              title: "申诉已提交，管理员会尽快处理",
              icon: "success",
              duration: 2e3
            });
            this.closeHotelAppealModal();
          } else {
            common_vendor.index.showToast({
              title: ((_a = res.data) == null ? void 0 : _a.message) || "提交失败",
              icon: "none"
            });
          }
        },
        fail: () => {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({
            title: "申诉已提交，管理员会尽快处理",
            icon: "success",
            duration: 2e3
          });
          this.closeHotelAppealModal();
        }
      });
    },
    goToAccountAppeal() {
      common_vendor.index.navigateTo({
        url: "/pages/merchant/account-appeal"
      });
    },
    getIcon(type) {
      if (type === "MERCHANT_BANNED")
        return "🔒";
      if (type === "MERCHANT_UNBANNED")
        return "✅";
      if (type === "HOTEL_BANNED")
        return "🏨⚠️";
      if (type === "HOTEL_UNBANNED")
        return "🏨✅";
      if (type === "APPEAL_APPROVED")
        return "✅";
      if (type === "APPEAL_REJECTED")
        return "❌";
      if (type === "APPEAL_REPLY")
        return "📝";
      return "📢";
    },
    getIconClass(type) {
      if (type === "MERCHANT_BANNED")
        return "icon-danger";
      if (type === "HOTEL_BANNED")
        return "icon-warning";
      if (type === "APPEAL_REJECTED")
        return "icon-danger";
      if (type === "APPEAL_REPLY")
        return "icon-info";
      if (type === "MERCHANT_UNBANNED")
        return "icon-success";
      if (type === "HOTEL_UNBANNED")
        return "icon-success";
      if (type === "APPEAL_APPROVED")
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
        const days = Math.floor(diff / (1e3 * 60 * 60 * 24));
        const hours = Math.floor(diff / (1e3 * 60 * 60));
        const minutes = Math.floor(diff / (1e3 * 60));
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
        f: item.relatedName
      }, item.relatedName ? {
        g: common_vendor.t(item.relatedName)
      } : {}, {
        h: item.status === "UNREAD"
      }, item.status === "UNREAD" ? {} : {}, {
        i: item.id,
        j: item.status === "UNREAD" ? 1 : "",
        k: common_vendor.o(($event) => $options.viewDetail(item), item.id)
      });
    })
  } : {}, {
    g: $data.showHotelAppealModal
  }, $data.showHotelAppealModal ? {
    h: common_vendor.o((...args) => $options.closeHotelAppealModal && $options.closeHotelAppealModal(...args)),
    i: common_vendor.t($data.appealHotelName),
    j: common_vendor.t($data.appealHotelReason || "违规经营"),
    k: common_vendor.t($data.appealType || "请选择申诉类型"),
    l: $data.appealTypes,
    m: common_vendor.o((...args) => $options.onAppealTypeChange && $options.onAppealTypeChange(...args)),
    n: $data.appealContent,
    o: common_vendor.o(($event) => $data.appealContent = $event.detail.value),
    p: common_vendor.t($data.appealContent.length),
    q: $data.contactInfo,
    r: common_vendor.o(($event) => $data.contactInfo = $event.detail.value),
    s: common_vendor.o((...args) => $options.closeHotelAppealModal && $options.closeHotelAppealModal(...args)),
    t: common_vendor.o((...args) => $options.submitHotelAppeal && $options.submitHotelAppeal(...args)),
    v: common_vendor.o(() => {
    }),
    w: common_vendor.o((...args) => $options.closeHotelAppealModal && $options.closeHotelAppealModal(...args))
  } : {}, {
    x: $data.showReplyModal
  }, $data.showReplyModal ? common_vendor.e({
    y: common_vendor.t($data.replyTitle),
    z: common_vendor.o(($event) => $data.showReplyModal = false),
    A: common_vendor.t($data.replyContent),
    B: $data.replyMessage
  }, $data.replyMessage ? {
    C: common_vendor.t($data.replyMessage)
  } : {}, {
    D: common_vendor.o(($event) => $data.showReplyModal = false),
    E: common_vendor.o(() => {
    }),
    F: common_vendor.o(($event) => $data.showReplyModal = false)
  }) : {}, {
    G: $data.userInfo.status && $data.userInfo.status !== "NORMAL"
  }, $data.userInfo.status && $data.userInfo.status !== "NORMAL" ? {
    H: common_vendor.o((...args) => $options.goToAccountAppeal && $options.goToAccountAppeal(...args))
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-363e3f1a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/merchant/notifications.js.map
