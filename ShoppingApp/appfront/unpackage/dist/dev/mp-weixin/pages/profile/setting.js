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
      // 密码修改相关
      currentPassword: "",
      verifyPhone: "",
      newPassword: "",
      confirmPassword: "",
      isPasswordVerified: false,
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
    // 获取完整图片URL
    getFullImageUrl(path) {
      if (!path)
        return "/static/default-avatar.png";
      if (path.startsWith("http"))
        return path;
      if (path.startsWith("/file")) {
        return "http://localhost:8080" + path;
      }
      return path;
    },
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
        common_vendor.index.__f__("log", "at pages/profile/setting.vue:266", "用户信息:", this.userInfo);
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/profile/setting.vue:268", "读取用户信息失败", e);
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
      this.currentPassword = "";
      this.verifyPhone = "";
      this.newPassword = "";
      this.confirmPassword = "";
      this.isPasswordVerified = false;
      switch (field) {
        case "nickname":
          this.editTitle = "昵称";
          this.editValue = this.userInfo.nickname || "";
          this.showEditModal = true;
          break;
        case "phone":
          this.editTitle = "手机号";
          this.editValue = this.userInfo.phone || "";
          this.showEditModal = true;
          break;
        case "email":
          this.editTitle = "邮箱";
          this.editValue = this.userInfo.email || "";
          this.showEditModal = true;
          break;
        case "bio":
          this.editTitle = "个人简介";
          this.editValue = this.userInfo.bio || "";
          this.editType = "textarea";
          this.showEditModal = true;
          break;
        case "password":
          this.editTitle = "修改密码";
          this.showEditModal = true;
          break;
        case "avatar":
          this.chooseAvatar();
          return;
        default:
          return;
      }
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
          if (avatarUrl && avatarUrl.startsWith("/file")) {
            avatarUrl = "http://localhost:8080" + avatarUrl;
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
      this.currentPassword = "";
      this.verifyPhone = "";
      this.newPassword = "";
      this.confirmPassword = "";
      this.isPasswordVerified = false;
    },
    submitEdit() {
      if (this.editType === "password") {
        if (!this.isPasswordVerified) {
          this.verifyIdentity();
        } else {
          this.updatePassword();
        }
      } else {
        if (!this.editValue.trim() && this.editType !== "bio") {
          common_vendor.index.showToast({ title: "请填写" + this.editTitle, icon: "none" });
          return;
        }
        const updateData = {};
        switch (this.editType) {
          case "nickname":
            updateData.username = this.editValue.trim();
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
    // 验证身份：使用登录接口验证密码，本地验证手机号
    verifyIdentity() {
      if (!this.currentPassword) {
        common_vendor.index.showToast({ title: "请输入当前密码", icon: "none" });
        return;
      }
      if (!this.verifyPhone) {
        common_vendor.index.showToast({ title: "请输入绑定的手机号", icon: "none" });
        return;
      }
      if (this.userInfo.phone !== this.verifyPhone) {
        common_vendor.index.showToast({ title: "手机号不匹配", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "验证中..." });
      common_vendor.index.request({
        url: "http://localhost:8080/api/users/login",
        method: "POST",
        header: {
          "Content-Type": "application/json"
        },
        data: {
          username: this.userInfo.username,
          password: this.currentPassword
        },
        success: (res) => {
          common_vendor.index.hideLoading();
          if (res.statusCode === 200 && res.data && res.data.id) {
            this.isPasswordVerified = true;
            common_vendor.index.showToast({ title: "验证成功", icon: "success" });
          } else {
            common_vendor.index.showToast({ title: "当前密码错误", icon: "none" });
          }
        },
        fail: () => {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({ title: "网络错误", icon: "none" });
        }
      });
    },
    // 修改密码：使用profile接口更新
    updatePassword() {
      if (!this.newPassword || this.newPassword.length < 6) {
        common_vendor.index.showToast({ title: "新密码至少6位", icon: "none" });
        return;
      }
      if (this.newPassword !== this.confirmPassword) {
        common_vendor.index.showToast({ title: "两次输入的密码不一致", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "修改中..." });
      common_vendor.index.request({
        url: "http://localhost:8080/api/users/profile",
        method: "PUT",
        header: {
          "Authorization": "Bearer " + this.token,
          "Content-Type": "application/json"
        },
        data: {
          password: this.newPassword
        },
        success: (res) => {
          common_vendor.index.hideLoading();
          if (res.statusCode === 200 && res.data && res.data.id) {
            common_vendor.index.showToast({ title: "密码修改成功，请重新登录", icon: "success" });
            this.closeEditModal();
            setTimeout(() => {
              common_vendor.index.clearStorageSync();
              common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
            }, 1500);
          } else {
            common_vendor.index.showToast({ title: "修改失败，请稍后重试", icon: "none" });
          }
        },
        fail: () => {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({ title: "网络错误", icon: "none" });
        }
      });
    },
    updateProfile(updateData) {
      common_vendor.index.showLoading({ title: "保存中..." });
      common_vendor.index.request({
        url: "http://localhost:8080/api/users/profile",
        method: "PUT",
        header: {
          "Authorization": "Bearer " + this.token,
          "Content-Type": "application/json"
        },
        data: updateData,
        success: (res) => {
          common_vendor.index.hideLoading();
          if (res.statusCode === 200 && res.data && res.data.id) {
            const updatedUserInfo = { ...this.userInfo, ...updateData };
            common_vendor.index.setStorageSync("userInfo", updatedUserInfo);
            this.userInfo = updatedUserInfo;
            common_vendor.index.showToast({ title: "修改成功", icon: "success" });
            this.closeEditModal();
          } else {
            common_vendor.index.showToast({ title: "修改失败", icon: "none" });
          }
        },
        fail: () => {
          common_vendor.index.hideLoading();
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
    a: $options.getFullImageUrl($data.userInfo.avatar),
    b: common_vendor.t($data.userInfo.username || "用户"),
    c: common_vendor.t($data.userInfo.role === "MERCHANT" ? "商家账号" : "普通用户"),
    d: common_vendor.o(($event) => $options.editField("password")),
    e: common_vendor.t($data.userInfo.phone || "未绑定"),
    f: common_vendor.o(($event) => $options.editField("phone")),
    g: common_vendor.t($data.userInfo.email || "未绑定"),
    h: common_vendor.o(($event) => $options.editField("email")),
    i: $options.getFullImageUrl($data.userInfo.avatar),
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
    x: $data.editType === "password" && !$data.isPasswordVerified
  }, $data.editType === "password" && !$data.isPasswordVerified ? {
    y: $data.currentPassword,
    z: common_vendor.o(($event) => $data.currentPassword = $event.detail.value),
    A: $data.verifyPhone,
    B: common_vendor.o(($event) => $data.verifyPhone = $event.detail.value)
  } : $data.editType === "password" && $data.isPasswordVerified ? {
    D: $data.newPassword,
    E: common_vendor.o(($event) => $data.newPassword = $event.detail.value),
    F: $data.confirmPassword,
    G: common_vendor.o(($event) => $data.confirmPassword = $event.detail.value)
  } : common_vendor.e({
    H: $data.editType !== "textarea"
  }, $data.editType !== "textarea" ? {
    I: "请输入" + $data.editTitle,
    J: $data.editType === "phone" ? "number" : "text",
    K: $data.editValue,
    L: common_vendor.o(($event) => $data.editValue = $event.detail.value)
  } : {
    M: "请输入" + $data.editTitle,
    N: $data.editValue,
    O: common_vendor.o(($event) => $data.editValue = $event.detail.value)
  }), {
    C: $data.editType === "password" && $data.isPasswordVerified,
    P: common_vendor.o((...args) => $options.closeEditModal && $options.closeEditModal(...args)),
    Q: common_vendor.o((...args) => $options.submitEdit && $options.submitEdit(...args)),
    R: common_vendor.o(() => {
    }),
    S: common_vendor.o((...args) => $options.closeEditModal && $options.closeEditModal(...args))
  }) : {}, {
    T: $data.showAboutModal
  }, $data.showAboutModal ? {
    U: common_vendor.o(($event) => $data.showAboutModal = false),
    V: common_assets._imports_0,
    W: common_vendor.o(() => {
    }),
    X: common_vendor.o(($event) => $data.showAboutModal = false)
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-46031339"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/setting.js.map
