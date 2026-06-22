"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      // 表单数据
      username: "",
      password: "",
      confirmPassword: "",
      shopName: "",
      phone: "",
      email: "",
      shopDescription: "",
      rememberPwd: false,
      // 界面状态
      isLogin: true,
      userRole: "user",
      // 申诉相关
      showAppealModal: false,
      appealUsername: "",
      appealShopName: "",
      appealStatus: "",
      appealType: "",
      appealContent: "",
      contactInfo: "",
      appealTypes: ["账号解封", "审核咨询", "信息修改", "投诉建议", "其他问题"]
    };
  },
  computed: {
    appealStatusText() {
      const status = this.appealStatus;
      if (status === "PENDING")
        return "待审核";
      if (status === "REJECTED")
        return "已拒绝";
      if (status === "BANNED")
        return "已禁用";
      return "未知状态";
    },
    appealStatusClass() {
      const status = this.appealStatus;
      if (status === "PENDING")
        return "status-pending";
      if (status === "REJECTED")
        return "status-rejected";
      if (status === "BANNED")
        return "status-banned";
      return "";
    }
  },
  onLoad() {
    const savedUsername = common_vendor.index.getStorageSync("savedUsername");
    const savedPassword = common_vendor.index.getStorageSync("savedPassword");
    if (savedUsername && savedPassword) {
      this.username = savedUsername;
      this.password = savedPassword;
      this.rememberPwd = true;
    }
  },
  methods: {
    switchRole(role) {
      this.userRole = role;
      this.clearForm();
    },
    toggleForm() {
      this.isLogin = !this.isLogin;
      this.clearForm();
    },
    clearForm() {
      this.username = "";
      this.password = "";
      this.confirmPassword = "";
      this.shopName = "";
      this.phone = "";
      this.email = "";
      this.shopDescription = "";
    },
    onAppealTypeChange(e) {
      this.appealType = this.appealTypes[e.detail.value];
    },
    openAppealModal(username, shopName, status) {
      this.appealUsername = username;
      this.appealShopName = shopName || "";
      this.appealStatus = status;
      this.appealType = "";
      this.appealContent = "";
      this.contactInfo = "";
      this.showAppealModal = true;
    },
    closeAppealModal() {
      this.showAppealModal = false;
    },
    submitAppeal() {
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
          username: this.appealUsername,
          shopName: this.appealShopName,
          status: this.appealStatus,
          type: this.appealType,
          content: this.appealContent,
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
            this.closeAppealModal();
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
          this.closeAppealModal();
        }
      });
    },
    // 登录
    login() {
      if (!this.username || !this.password) {
        common_vendor.index.showToast({ title: "请输入账号密码", icon: "none" });
        return;
      }
      if (this.rememberPwd) {
        common_vendor.index.setStorageSync("savedUsername", this.username);
        common_vendor.index.setStorageSync("savedPassword", this.password);
      } else {
        common_vendor.index.removeStorageSync("savedUsername");
        common_vendor.index.removeStorageSync("savedPassword");
      }
      const loginUrl = "http://localhost:8080/api/users/login";
      common_vendor.index.showLoading({ title: "登录中..." });
      common_vendor.index.request({
        url: loginUrl,
        method: "POST",
        data: {
          username: this.username,
          password: this.password
        },
        success: (res) => {
          var _a;
          common_vendor.index.hideLoading();
          if (res.statusCode === 200 && res.data) {
            const userData = res.data;
            if (this.userRole === "merchant" && userData.role !== "MERCHANT") {
              common_vendor.index.showToast({ title: "该账号不是商家账号", icon: "none" });
              return;
            }
            if (this.userRole === "user" && userData.role !== "USER") {
              common_vendor.index.showToast({ title: "该账号不是普通用户账号", icon: "none" });
              return;
            }
            if (this.userRole === "merchant") {
              const status = (userData.status || "NORMAL").toUpperCase();
              if (status === "PENDING") {
                common_vendor.index.showModal({
                  title: "账号待审核",
                  content: "您的商家账号正在审核中，请等待管理员审核。如需联系管理员，请点击申诉。",
                  confirmText: "申诉",
                  cancelText: "知道了",
                  success: (modalRes) => {
                    if (modalRes.confirm) {
                      this.openAppealModal(userData.username, userData.shopName, "PENDING");
                    }
                  }
                });
                return;
              } else if (status === "REJECTED") {
                common_vendor.index.showModal({
                  title: "审核未通过",
                  content: "您的商家账号审核未通过。如需申诉或了解详情，请联系管理员。",
                  confirmText: "申诉",
                  cancelText: "知道了",
                  success: (modalRes) => {
                    if (modalRes.confirm) {
                      this.openAppealModal(userData.username, userData.shopName, "REJECTED");
                    }
                  }
                });
                return;
              } else if (status === "BANNED") {
                common_vendor.index.showModal({
                  title: "账号已禁用",
                  content: "您的商家账号已被禁用。如需申诉，请联系管理员。",
                  confirmText: "申诉",
                  cancelText: "知道了",
                  success: (modalRes) => {
                    if (modalRes.confirm) {
                      this.openAppealModal(userData.username, userData.shopName, "BANNED");
                    }
                  }
                });
                return;
              } else if (status !== "NORMAL") {
                common_vendor.index.showToast({
                  title: "账号状态异常(" + status + ")，请联系管理员",
                  icon: "none"
                });
                return;
              }
            }
            common_vendor.index.setStorageSync("token", userData.token);
            common_vendor.index.setStorageSync("loginUsername", this.username);
            common_vendor.index.setStorageSync("userRole", userData.role);
            common_vendor.index.setStorageSync("userInfo", {
              id: userData.id,
              name: this.username,
              role: userData.role,
              status: userData.status || "NORMAL",
              shopName: userData.shopName || "",
              phone: userData.phone || "",
              email: userData.email || "",
              avatar: userData.avatar || ""
            });
            common_vendor.index.showToast({
              title: "登录成功",
              icon: "success",
              complete: () => {
                setTimeout(() => {
                  if (userData.role === "MERCHANT") {
                    common_vendor.index.reLaunch({ url: "/pages/merchant/home" });
                  } else {
                    common_vendor.index.switchTab({ url: "/pages/home/home" });
                  }
                }, 500);
              }
            });
          } else {
            common_vendor.index.showToast({
              title: ((_a = res.data) == null ? void 0 : _a.message) || "用户名或密码错误",
              icon: "none"
            });
          }
        },
        fail: () => {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({ title: "网络异常，请稍后重试", icon: "none" });
        }
      });
    },
    // 注册
    register() {
      if (!this.username || !this.password) {
        common_vendor.index.showToast({ title: "请完善信息", icon: "none" });
        return;
      }
      if (this.password !== this.confirmPassword) {
        common_vendor.index.showToast({ title: "两次密码不一致", icon: "none" });
        return;
      }
      if (this.password.length < 6) {
        common_vendor.index.showToast({ title: "密码至少6位", icon: "none" });
        return;
      }
      if (this.userRole === "merchant") {
        if (!this.shopName) {
          common_vendor.index.showToast({ title: "请填写商家名称", icon: "none" });
          return;
        }
        if (!this.phone) {
          common_vendor.index.showToast({ title: "请填写联系电话", icon: "none" });
          return;
        }
      }
      const registerUrl = this.userRole === "merchant" ? "http://localhost:8080/api/users/register-merchant" : "http://localhost:8080/api/users/register";
      const requestData = this.userRole === "merchant" ? {
        username: this.username,
        password: this.password,
        shopName: this.shopName,
        phone: this.phone,
        email: this.email,
        shopDescription: this.shopDescription
      } : {
        username: this.username,
        password: this.password
      };
      common_vendor.index.showLoading({ title: "注册中..." });
      common_vendor.index.request({
        url: registerUrl,
        method: "POST",
        data: requestData,
        success: (res) => {
          var _a;
          common_vendor.index.hideLoading();
          if (res.statusCode === 200 && res.data.success) {
            if (this.userRole === "merchant") {
              common_vendor.index.showModal({
                title: "注册成功",
                content: "商家注册成功，请等待管理员审核。审核通过后即可登录使用。",
                confirmText: "知道了",
                showCancel: false
              });
            } else {
              common_vendor.index.showToast({ title: "注册成功", icon: "success" });
            }
            this.isLogin = true;
            this.clearForm();
          } else {
            common_vendor.index.showToast({
              title: ((_a = res.data) == null ? void 0 : _a.message) || "注册失败",
              icon: "none"
            });
          }
        },
        fail: () => {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({ title: "网络异常", icon: "none" });
        }
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.userRole === "user" ? 1 : "",
    b: common_vendor.o(($event) => $options.switchRole("user")),
    c: $data.userRole === "merchant" ? 1 : "",
    d: common_vendor.o(($event) => $options.switchRole("merchant")),
    e: $data.isLogin
  }, $data.isLogin ? {
    f: $data.username,
    g: common_vendor.o(($event) => $data.username = $event.detail.value),
    h: $data.password,
    i: common_vendor.o(($event) => $data.password = $event.detail.value),
    j: $data.rememberPwd,
    k: common_vendor.o(($event) => $data.rememberPwd = !$data.rememberPwd),
    l: common_vendor.o((...args) => $options.login && $options.login(...args)),
    m: common_vendor.o((...args) => $options.toggleForm && $options.toggleForm(...args))
  } : common_vendor.e({
    n: common_vendor.t($data.userRole === "merchant" ? "加入商家平台" : "开启旅游之旅"),
    o: $data.username,
    p: common_vendor.o(($event) => $data.username = $event.detail.value),
    q: $data.password,
    r: common_vendor.o(($event) => $data.password = $event.detail.value),
    s: $data.confirmPassword,
    t: common_vendor.o(($event) => $data.confirmPassword = $event.detail.value),
    v: $data.userRole === "merchant"
  }, $data.userRole === "merchant" ? {
    w: $data.shopName,
    x: common_vendor.o(($event) => $data.shopName = $event.detail.value),
    y: $data.phone,
    z: common_vendor.o(($event) => $data.phone = $event.detail.value),
    A: $data.email,
    B: common_vendor.o(($event) => $data.email = $event.detail.value),
    C: $data.shopDescription,
    D: common_vendor.o(($event) => $data.shopDescription = $event.detail.value)
  } : {}, {
    E: common_vendor.o((...args) => $options.register && $options.register(...args)),
    F: common_vendor.o((...args) => $options.toggleForm && $options.toggleForm(...args))
  }), {
    G: $data.showAppealModal
  }, $data.showAppealModal ? common_vendor.e({
    H: common_vendor.o((...args) => $options.closeAppealModal && $options.closeAppealModal(...args)),
    I: common_vendor.t($options.appealStatusText),
    J: common_vendor.n($options.appealStatusClass),
    K: common_vendor.t($data.appealUsername),
    L: $data.appealShopName
  }, $data.appealShopName ? {
    M: common_vendor.t($data.appealShopName)
  } : {}, {
    N: common_vendor.t($data.appealType || "请选择申诉类型"),
    O: $data.appealTypes,
    P: common_vendor.o((...args) => $options.onAppealTypeChange && $options.onAppealTypeChange(...args)),
    Q: $data.appealContent,
    R: common_vendor.o(($event) => $data.appealContent = $event.detail.value),
    S: common_vendor.t($data.appealContent.length),
    T: $data.contactInfo,
    U: common_vendor.o(($event) => $data.contactInfo = $event.detail.value),
    V: common_vendor.o((...args) => $options.closeAppealModal && $options.closeAppealModal(...args)),
    W: common_vendor.o((...args) => $options.submitAppeal && $options.submitAppeal(...args)),
    X: common_vendor.o(() => {
    }),
    Y: common_vendor.o((...args) => $options.closeAppealModal && $options.closeAppealModal(...args))
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-64876f70"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login-register/login-register.js.map
