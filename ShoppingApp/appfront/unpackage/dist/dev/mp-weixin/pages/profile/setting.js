"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const _sfc_main = {
  data() {
    return {
      userInfo: {},
      token: "",
      cacheSize: "0KB",
      // 编辑弹窗
      showEditModal: false,
      editType: "",
      editTitle: "",
      editValue: "",
      editFieldName: "",
      // 关于弹窗
      showAboutModal: false
    };
  },
  onShow() {
    this.checkLogin();
    this.loadUserInfo();
    this.calculateCacheSize();
  },
  methods: {
    checkLogin() {
      const token = common_vendor.index.getStorageSync("token");
      const username = common_vendor.index.getStorageSync("loginUsername");
      if (!token || !username) {
        common_vendor.index.showToast({ title: "请先登录", icon: "none" });
        setTimeout(() => {
          common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
        }, 1500);
        return;
      }
      this.token = token;
    },
    loadUserInfo() {
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        this.userInfo = userInfo || {};
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/profile/setting.vue:206", "读取用户信息失败", e);
      }
    },
    calculateCacheSize() {
      try {
        let size = 0;
        const keys = common_vendor.index.getStorageInfoSync().keys;
        keys.forEach((key) => {
          const value = common_vendor.index.getStorageSync(key);
          const str = JSON.stringify(value);
          size += str.length;
        });
        if (size < 1024) {
          this.cacheSize = size + "B";
        } else if (size < 1024 * 1024) {
          this.cacheSize = (size / 1024).toFixed(1) + "KB";
        } else {
          this.cacheSize = (size / (1024 * 1024)).toFixed(1) + "MB";
        }
      } catch (e) {
        this.cacheSize = "0KB";
      }
    },
    editField(field) {
      this.editType = field;
      this.editFieldName = field;
      switch (field) {
        case "nickname":
          this.editTitle = "昵称";
          this.editValue = this.userInfo.nickname || "";
          break;
        case "phone":
          this.editTitle = "手机号";
          this.editValue = this.userInfo.phone || "";
          break;
        case "email":
          this.editTitle = "邮箱";
          this.editValue = this.userInfo.email || "";
          break;
        case "bio":
          this.editTitle = "个人简介";
          this.editValue = this.userInfo.bio || "";
          this.editType = "textarea";
          break;
        case "password":
          this.editTitle = "修改密码";
          this.editValue = "";
          break;
        case "avatar":
          this.editTitle = "更换头像";
          this.chooseAvatar();
          return;
        default:
          return;
      }
      this.showEditModal = true;
    },
    chooseAvatar() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0];
          this.uploadAvatar(tempFilePath);
        }
      });
    },
    uploadAvatar(filePath) {
      common_vendor.index.showLoading({ title: "上传中..." });
      common_vendor.index.uploadFile({
        url: "http://localhost:8080/api/upload",
        filePath,
        name: "file",
        success: (uploadRes) => {
          common_vendor.index.hideLoading();
          let avatarUrl = uploadRes.data;
          try {
            const data = JSON.parse(uploadRes.data);
            avatarUrl = data.url || data;
          } catch (e) {
          }
          this.updateProfile({ avatar: avatarUrl });
        },
        fail: () => {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({ title: "上传失败", icon: "none" });
        }
      });
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
        if (!this.editValue.trim() && this.editType !== "bio") {
          common_vendor.index.showToast({ title: "请填写" + this.editTitle, icon: "none" });
          return;
        }
        const updateData = {};
        switch (this.editType) {
          case "nickname":
            updateData.nickname = this.editValue.trim();
            break;
          case "phone":
            updateData.phone = this.editValue.trim();
            break;
          case "email":
            updateData.email = this.editValue.trim();
            break;
          case "bio":
            updateData.bio = this.editValue.trim();
            break;
        }
        this.updateProfile(updateData);
      }
    },
    updateProfile(updateData) {
      common_vendor.index.request({
        url: "http://localhost:8080/api/users/profile",
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
    clearCache() {
      common_vendor.index.showModal({
        title: "清除缓存",
        content: "确定要清除所有缓存吗？清除后需要重新登录。",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.clearStorageSync();
            common_vendor.index.showToast({ title: "缓存已清除", icon: "success" });
            setTimeout(() => {
              common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
            }, 1500);
          }
        }
      });
    },
    showAbout() {
      this.showAboutModal = true;
    },
    showPrivacy() {
      common_vendor.index.showModal({
        title: "隐私政策",
        content: "我们重视您的隐私保护。本应用收集的信息仅用于提供旅游服务，不会与第三方共享您的个人信息。",
        showCancel: false,
        confirmText: "我知道了"
      });
    },
    logout() {
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
    a: $data.userInfo.avatar || "/static/default-avatar.png",
    b: common_vendor.t($data.userInfo.username || "用户"),
    c: common_vendor.t($data.userInfo.role === "MERCHANT" ? "商家账号" : "普通用户"),
    d: common_vendor.o(($event) => $options.editField("password")),
    e: common_vendor.t($data.userInfo.phone || "未绑定"),
    f: common_vendor.o(($event) => $options.editField("phone")),
    g: common_vendor.t($data.userInfo.email || "未绑定"),
    h: common_vendor.o(($event) => $options.editField("email")),
    i: $data.userInfo.avatar || "/static/default-avatar.png",
    j: common_vendor.o(($event) => $options.editField("avatar")),
    k: common_vendor.t($data.userInfo.nickname || $data.userInfo.username || "未设置"),
    l: common_vendor.o(($event) => $options.editField("nickname")),
    m: common_vendor.t($data.userInfo.bio || "这家伙很懒，什么都没留下"),
    n: common_vendor.o(($event) => $options.editField("bio")),
    o: common_vendor.t($data.cacheSize),
    p: common_vendor.o((...args) => $options.clearCache && $options.clearCache(...args)),
    q: common_vendor.o((...args) => $options.showAbout && $options.showAbout(...args)),
    r: common_vendor.o((...args) => $options.showPrivacy && $options.showPrivacy(...args)),
    s: common_vendor.o((...args) => $options.logout && $options.logout(...args)),
    t: $data.showEditModal
  }, $data.showEditModal ? common_vendor.e({
    v: common_vendor.t($data.editTitle),
    w: common_vendor.o((...args) => $options.closeEditModal && $options.closeEditModal(...args)),
    x: $data.editType !== "textarea"
  }, $data.editType !== "textarea" ? {
    y: "请输入" + $data.editTitle,
    z: $data.editType === "phone" ? "number" : "text",
    A: $data.editType === "password",
    B: $data.editValue,
    C: common_vendor.o(($event) => $data.editValue = $event.detail.value)
  } : {
    D: "请输入" + $data.editTitle,
    E: $data.editValue,
    F: common_vendor.o(($event) => $data.editValue = $event.detail.value)
  }, {
    G: common_vendor.o((...args) => $options.closeEditModal && $options.closeEditModal(...args)),
    H: common_vendor.o((...args) => $options.submitEdit && $options.submitEdit(...args)),
    I: common_vendor.o(() => {
    }),
    J: common_vendor.o((...args) => $options.closeEditModal && $options.closeEditModal(...args))
  }) : {}, {
    K: $data.showAboutModal
  }, $data.showAboutModal ? {
    L: common_vendor.o(($event) => $data.showAboutModal = false),
    M: common_assets._imports_0,
    N: common_vendor.o(() => {
    }),
    O: common_vendor.o(($event) => $data.showAboutModal = false)
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-46031339"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/setting.js.map
