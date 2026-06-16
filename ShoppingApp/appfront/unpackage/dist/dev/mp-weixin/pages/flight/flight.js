"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  onLoad() {
    common_vendor.index.redirectTo({
      url: "/pages/flight/search-flight",
      fail: (err) => {
        common_vendor.index.__f__("error", "at pages/flight/flight.vue:14", "跳转失败：", err);
        common_vendor.index.showToast({
          title: "页面跳转失败",
          icon: "none"
        });
      }
    });
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {};
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-aa7f39c0"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/flight/flight.js.map
