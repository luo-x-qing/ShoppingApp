"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      username: "",
      password: "",
      confirmPassword: "",
      isLogin: true
    };
  },
  methods: {
    toggleForm() {
      this.isLogin = !this.isLogin;
      this.username = "";
      this.password = "";
      this.confirmPassword = "";
    },
    login() {
      if (!this.username || !this.password) {
        common_vendor.index.showToast({ title: "请输入账号密码", icon: "none" });
        return;
      }
      common_vendor.index.request({
        url: "http://localhost:8080/api/users/login",
        method: "POST",
        data: {
          username: this.username,
          password: this.password
        },
        success: (res) => {
          var _a;
          if (res.statusCode === 200 && res.data) {
            common_vendor.index.setStorageSync("token", res.data.token || "login");
            common_vendor.index.setStorageSync("loginUsername", this.username);
            common_vendor.index.setStorageSync("userInfo", {
              name: this.username,
              id: "",
              desc: "用户"
            });
            common_vendor.index.showToast({
              title: "登录成功",
              icon: "success",
              complete: () => {
                setTimeout(() => {
                  common_vendor.index.switchTab({ url: "/pages/home/home" });
                }, 500);
              }
            });
          } else {
            common_vendor.index.showToast({
              title: ((_a = res.data) == null ? void 0 : _a.message) || "登录失败",
              icon: "none"
            });
          }
        },
        fail: () => {
          common_vendor.index.showToast({ title: "网络异常", icon: "none" });
        }
      });
    },
    register() {
      if (!this.username || !this.password) {
        common_vendor.index.showToast({ title: "请完善信息", icon: "none" });
        return;
      }
      if (this.password !== this.confirmPassword) {
        common_vendor.index.showToast({ title: "两次密码不一致", icon: "none" });
        return;
      }
      common_vendor.index.request({
        url: "http://localhost:8080/api/users/register",
        method: "POST",
        data: {
          username: this.username,
          password: this.password
        },
        success: (res) => {
          var _a;
          if (res.statusCode === 200) {
            common_vendor.index.showToast({ title: "注册成功", icon: "success" });
            this.toggleForm();
          } else {
            common_vendor.index.showToast({ title: ((_a = res.data) == null ? void 0 : _a.message) || "注册失败", icon: "none" });
          }
        },
        fail: () => {
          common_vendor.index.showToast({ title: "网络异常", icon: "none" });
        }
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.isLogin
  }, $data.isLogin ? {
    b: $data.username,
    c: common_vendor.o(($event) => $data.username = $event.detail.value),
    d: $data.password,
    e: common_vendor.o(($event) => $data.password = $event.detail.value),
    f: common_vendor.o((...args) => $options.login && $options.login(...args)),
    g: common_vendor.o((...args) => $options.toggleForm && $options.toggleForm(...args))
  } : {
    h: $data.username,
    i: common_vendor.o(($event) => $data.username = $event.detail.value),
    j: $data.password,
    k: common_vendor.o(($event) => $data.password = $event.detail.value),
    l: $data.confirmPassword,
    m: common_vendor.o(($event) => $data.confirmPassword = $event.detail.value),
    n: common_vendor.o((...args) => $options.register && $options.register(...args)),
    o: common_vendor.o((...args) => $options.toggleForm && $options.toggleForm(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login-register/login-register.js.map
