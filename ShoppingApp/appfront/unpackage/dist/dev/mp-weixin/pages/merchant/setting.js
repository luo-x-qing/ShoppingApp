"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      userInfo: {},
      token: "",
      // 编辑弹窗
      showEditModal: false,
      editType: "",
      editTitle: "",
      editValue: "",
      editFieldName: ""
    };
  },
  onShow() {
    this.loadUserInfo();
    this.token = common_vendor.index.getStorageSync("token");
  },
  methods: {
    loadUserInfo() {
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        this.userInfo = userInfo || {};
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/merchant/setting.vue:123", "读取用户信息失败", e);
      }
    },
    editField(field) {
      this.editType = field;
      this.editFieldName = field;
      switch (field) {
        case "shopName":
          this.editTitle = "商家名称";
          this.editValue = this.userInfo.shopName || "";
          break;
        case "phone":
          this.editTitle = "联系电话";
          this.editValue = this.userInfo.phone || "";
          break;
        case "email":
          this.editTitle = "电子邮箱";
          this.editValue = this.userInfo.email || "";
          break;
        case "description":
          this.editTitle = "商家简介";
          this.editValue = this.userInfo.shopDescription || "";
          break;
        case "password":
          this.editTitle = "修改密码";
          this.editValue = "";
          break;
        default:
          return;
      }
      this.showEditModal = true;
    },
    closeEditModal() {
      this.showEditModal = false;
      this.editValue = "";
    },
    submitEdit() {
      if (this.editType === "password") {
        if (!this.editValue || this.editValue.length < 6) {
          common_vendor.index.showToast({ title: "密码至少6位", icon: "none" });
          return;
        }
        this.updatePassword();
      } else {
        if (!this.editValue.trim()) {
          common_vendor.index.showToast({ title: "请填写" + this.editTitle, icon: "none" });
          return;
        }
        this.updateProfile();
      }
    },
    updateProfile() {
      const updateData = {};
      switch (this.editType) {
        case "shopName":
          updateData.shopName = this.editValue.trim();
          break;
        case "phone":
          updateData.phone = this.editValue.trim();
          break;
        case "email":
          updateData.email = this.editValue.trim();
          break;
        case "description":
          updateData.shopDescription = this.editValue.trim();
          break;
      }
      common_vendor.index.request({
        url: "http://localhost:8080/api/users/merchant/info",
        method: "PUT",
        header: {
          "Authorization": "Bearer " + this.token,
          "Content-Type": "application/json"
        },
        data: updateData,
        success: (res) => {
          var _a;
          if (res.statusCode === 200 && res.data) {
            const updatedUserInfo = { ...this.userInfo, ...updateData };
            common_vendor.index.setStorageSync("userInfo", updatedUserInfo);
            this.userInfo = updatedUserInfo;
            common_vendor.index.showToast({ title: "修改成功", icon: "success" });
            this.closeEditModal();
          } else {
            common_vendor.index.showToast({ title: ((_a = res.data) == null ? void 0 : _a.message) || "修改失败", icon: "none" });
          }
        },
        fail: () => {
          common_vendor.index.showToast({ title: "网络错误", icon: "none" });
        }
      });
    },
    updatePassword() {
      common_vendor.index.request({
        url: "http://localhost:8080/api/users/profile",
        method: "PUT",
        header: {
          "Authorization": "Bearer " + this.token,
          "Content-Type": "application/json"
        },
        data: {
          password: this.editValue
        },
        success: (res) => {
          var _a;
          if (res.statusCode === 200 && res.data) {
            common_vendor.index.showToast({ title: "密码修改成功，请重新登录", icon: "success" });
            this.closeEditModal();
            setTimeout(() => {
              common_vendor.index.clearStorageSync();
              common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
            }, 1500);
          } else {
            common_vendor.index.showToast({ title: ((_a = res.data) == null ? void 0 : _a.message) || "修改失败", icon: "none" });
          }
        },
        fail: () => {
          common_vendor.index.showToast({ title: "网络错误", icon: "none" });
        }
      });
    },
    showLogoutConfirm() {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要退出登录吗？",
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
    a: common_vendor.t($data.userInfo.shopName || "未设置"),
    b: common_vendor.o(($event) => $options.editField("shopName")),
    c: common_vendor.t($data.userInfo.phone || "未设置"),
    d: common_vendor.o(($event) => $options.editField("phone")),
    e: common_vendor.t($data.userInfo.email || "未设置"),
    f: common_vendor.o(($event) => $options.editField("email")),
    g: common_vendor.t($data.userInfo.shopDescription || "暂无简介"),
    h: common_vendor.o(($event) => $options.editField("description")),
    i: common_vendor.o(($event) => $options.editField("password")),
    j: common_vendor.o((...args) => $options.showLogoutConfirm && $options.showLogoutConfirm(...args)),
    k: $data.showEditModal
  }, $data.showEditModal ? common_vendor.e({
    l: common_vendor.t($data.editTitle),
    m: common_vendor.o((...args) => $options.closeEditModal && $options.closeEditModal(...args)),
    n: "请输入" + $data.editTitle,
    o: $data.editType === "phone" ? "number" : "text",
    p: $data.editType === "password",
    q: $data.editValue,
    r: common_vendor.o(($event) => $data.editValue = $event.detail.value),
    s: $data.editType === "description"
  }, $data.editType === "description" ? {
    t: $data.editValue,
    v: common_vendor.o(($event) => $data.editValue = $event.detail.value)
  } : {}, {
    w: common_vendor.o((...args) => $options.closeEditModal && $options.closeEditModal(...args)),
    x: common_vendor.o((...args) => $options.submitEdit && $options.submitEdit(...args)),
    y: common_vendor.o(() => {
    }),
    z: common_vendor.o((...args) => $options.closeEditModal && $options.closeEditModal(...args))
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-794c7c1a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/merchant/setting.js.map
