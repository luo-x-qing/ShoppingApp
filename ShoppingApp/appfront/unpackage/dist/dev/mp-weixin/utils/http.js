"use strict";
const common_vendor = require("../common/vendor.js");
const http = common_vendor.axios.create({
  baseURL: "http://localhost:8080",
  // API 的 base_url
  timeout: 5e3
  // 请求超时时间
});
http.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
http.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    return Promise.reject(error);
  }
);
exports.http = http;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/http.js.map
