"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      commentList: [],
      hotelList: [],
      hotelNames: [],
      selectedHotelId: null,
      selectedHotelName: "全部酒店",
      selectedRating: null,
      // all, 1,2,3,4,5
      totalCount: 0,
      ratingCounts: {},
      merchantId: null,
      // 回复相关
      replyContents: {},
      submittingId: null,
      // 正在提交回复的评论ID
      // 编辑回复弹窗
      showEditDialog: false,
      editCommentId: null,
      editReplyContent: "",
      updatingReply: false
    };
  },
  onShow() {
    this.loadMerchantInfo();
  },
  methods: {
    // 加载商家信息
    loadMerchantInfo() {
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:165", "商家信息：", userInfo);
        if (userInfo && userInfo.id) {
          this.merchantId = userInfo.id;
          this.loadMerchantHotels();
        } else {
          common_vendor.index.showToast({ title: "请先登录", icon: "none" });
          setTimeout(() => {
            common_vendor.index.reLaunch({ url: "/pages/login-register/login-register" });
          }, 1500);
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/merchant/comment-list.vue:176", "读取商家信息失败", e);
      }
    },
    // 获取商家酒店列表
    loadMerchantHotels() {
      common_vendor.index.request({
        url: `http://localhost:8080/api/merchant/hotels?merchantId=${this.merchantId}`,
        method: "GET",
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:186", "酒店列表返回：", res.data);
          if (res.data && res.data.code === 200) {
            this.hotelList = res.data.data || [];
            this.hotelNames = ["全部酒店", ...this.hotelList.map((h) => h.name)];
            this.hotelMap = {};
            this.hotelList.forEach((h) => {
              this.hotelMap[h.id] = h.name;
            });
            this.loadComments();
          } else {
            common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:196", "获取酒店列表失败或没有酒店");
            this.loadComments();
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/merchant/comment-list.vue:201", "获取酒店列表失败", err);
          this.loadComments();
        }
      });
    },
    // 获取酒店名称
    getHotelName(hotelId) {
      return this.hotelMap ? this.hotelMap[hotelId] || "未知酒店" : "酒店";
    },
    // 加载评价列表
    loadComments() {
      if (!this.merchantId) {
        common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:215", "商家ID为空");
        return;
      }
      common_vendor.index.showLoading({ title: "加载中..." });
      let url = `http://localhost:8080/api/hotel-comments/merchant?merchantId=${this.merchantId}`;
      if (this.selectedHotelId) {
        url += `&hotelId=${this.selectedHotelId}`;
      }
      if (this.selectedRating && this.selectedRating !== "all") {
        url += `&rating=${this.selectedRating}`;
      }
      common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:231", "请求URL：", url);
      common_vendor.index.request({
        url,
        method: "GET",
        success: (res) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:238", "评价列表返回：", res.data);
          if (res.data && res.data.code === 200) {
            let comments = res.data.data || [];
            common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:242", "评价数量：", comments.length);
            if (comments.length > 0) {
              common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:245", "第一条评价数据：", comments[0]);
            }
            this.commentList = comments;
            this.calculateStats();
          } else {
            common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:251", "返回code不是200：", res.data);
            this.commentList = [];
            this.totalCount = 0;
            this.ratingCounts = {};
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/merchant/comment-list.vue:259", "获取评价失败：", err);
          common_vendor.index.showToast({ title: "获取评价失败", icon: "none" });
          this.commentList = [];
        }
      });
    },
    // 计算统计数据
    calculateStats() {
      this.totalCount = this.commentList.length;
      this.ratingCounts = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
      this.commentList.forEach((comment) => {
        const rating = comment.rating || comment.score;
        if (rating >= 1 && rating <= 5) {
          this.ratingCounts[rating]++;
        }
      });
      common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:278", "统计结果：", this.ratingCounts);
    },
    // 按评分筛选
    filterByRating(rating) {
      common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:283", "筛选评分：", rating);
      this.selectedRating = rating;
      this.loadComments();
    },
    // 切换酒店
    onHotelChange(e) {
      const index = e.detail.value;
      common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:291", "选择酒店索引：", index);
      if (index === 0) {
        this.selectedHotelId = null;
        this.selectedHotelName = "全部酒店";
      } else {
        const hotel = this.hotelList[index - 1];
        this.selectedHotelId = hotel.id;
        this.selectedHotelName = hotel.name;
      }
      common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:300", "选中酒店ID：", this.selectedHotelId);
      this.loadComments();
    },
    // 提交回复
    submitReply(comment) {
      const commentId = comment.id || comment.commentId;
      if (!commentId) {
        common_vendor.index.__f__("error", "at pages/merchant/comment-list.vue:309", "评论ID不存在：", comment);
        common_vendor.index.showToast({ title: "评论ID无效", icon: "none" });
        return;
      }
      const replyContent = this.replyContents[commentId];
      if (!replyContent || !replyContent.trim()) {
        common_vendor.index.showToast({ title: "请输入回复内容", icon: "none" });
        return;
      }
      this.submittingId = commentId;
      common_vendor.index.showLoading({ title: "提交中..." });
      common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:323", `回复评论 ${commentId}，内容：`, replyContent.trim());
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotel-comments/${commentId}/reply`,
        method: "POST",
        header: {
          "Content-Type": "application/json"
        },
        data: {
          reply: replyContent.trim()
        },
        success: (res) => {
          var _a, _b;
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:336", "回复结果：", res.data);
          if (res.data && (res.data.code === 200 || res.data.success)) {
            common_vendor.index.showToast({ title: "回复成功", icon: "success" });
            comment.reply = replyContent.trim();
            this.$set(this.replyContents, commentId, "");
            setTimeout(() => {
              this.loadComments();
            }, 500);
          } else {
            const errorMsg = ((_a = res.data) == null ? void 0 : _a.message) || ((_b = res.data) == null ? void 0 : _b.msg) || "回复失败";
            common_vendor.index.showToast({ title: errorMsg, icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/merchant/comment-list.vue:356", "回复失败：", err);
          common_vendor.index.showToast({ title: "网络错误，请稍后重试", icon: "none" });
        },
        complete: () => {
          this.submittingId = null;
        }
      });
    },
    // 显示编辑回复弹窗
    showEditReply(comment) {
      const commentId = comment.id || comment.commentId;
      this.editCommentId = commentId;
      this.editReplyContent = comment.reply || "";
      this.showEditDialog = true;
    },
    // 关闭编辑弹窗
    closeEditDialog() {
      this.showEditDialog = false;
      this.editCommentId = null;
      this.editReplyContent = "";
    },
    // 更新回复
    updateReply() {
      if (!this.editReplyContent.trim()) {
        common_vendor.index.showToast({ title: "请输入回复内容", icon: "none" });
        return;
      }
      this.updatingReply = true;
      common_vendor.index.showLoading({ title: "更新中..." });
      common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:390", `更新回复 ${this.editCommentId}，内容：`, this.editReplyContent.trim());
      common_vendor.index.request({
        url: `http://localhost:8080/api/hotel-comments/${this.editCommentId}/reply`,
        method: "PUT",
        header: {
          "Content-Type": "application/json"
        },
        data: {
          reply: this.editReplyContent.trim()
        },
        success: (res) => {
          var _a, _b;
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("log", "at pages/merchant/comment-list.vue:403", "更新回复结果：", res.data);
          if (res.data && (res.data.code === 200 || res.data.success)) {
            common_vendor.index.showToast({ title: "修改成功", icon: "success" });
            const comment = this.commentList.find((c) => (c.id || c.commentId) === this.editCommentId);
            if (comment) {
              comment.reply = this.editReplyContent.trim();
            }
            this.closeEditDialog();
          } else {
            const errorMsg = ((_a = res.data) == null ? void 0 : _a.message) || ((_b = res.data) == null ? void 0 : _b.msg) || "修改失败";
            common_vendor.index.showToast({ title: errorMsg, icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/merchant/comment-list.vue:421", "修改回复失败：", err);
          common_vendor.index.showToast({ title: "网络错误，请稍后重试", icon: "none" });
        },
        complete: () => {
          this.updatingReply = false;
        }
      });
    },
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr)
        return "--";
      try {
        const date = new Date(dateStr);
        if (isNaN(date.getTime()))
          return dateStr;
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        const hour = String(date.getHours()).padStart(2, "0");
        const minute = String(date.getMinutes()).padStart(2, "0");
        return `${year}-${month}-${day} ${hour}:${minute}`;
      } catch (e) {
        return dateStr;
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.t($data.totalCount),
    b: common_vendor.o(($event) => $options.filterByRating("all")),
    c: common_vendor.t($data.ratingCounts[5] || 0),
    d: common_vendor.o(($event) => $options.filterByRating(5)),
    e: common_vendor.t($data.ratingCounts[4] || 0),
    f: common_vendor.o(($event) => $options.filterByRating(4)),
    g: common_vendor.t($data.ratingCounts[3] || 0),
    h: common_vendor.o(($event) => $options.filterByRating(3)),
    i: common_vendor.t($data.ratingCounts[2] || 0),
    j: common_vendor.o(($event) => $options.filterByRating(2)),
    k: common_vendor.t($data.ratingCounts[1] || 0),
    l: common_vendor.o(($event) => $options.filterByRating(1)),
    m: $data.hotelList.length > 0
  }, $data.hotelList.length > 0 ? {
    n: common_vendor.t($data.selectedHotelName || "全部酒店"),
    o: $data.hotelNames,
    p: common_vendor.o((...args) => $options.onHotelChange && $options.onHotelChange(...args))
  } : {}, {
    q: $data.commentList.length > 0
  }, $data.commentList.length > 0 ? {
    r: common_vendor.f($data.commentList, (comment, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(comment.hotelName || $options.getHotelName(comment.hotelId)),
        b: common_vendor.t($options.formatDate(comment.createTime)),
        c: common_vendor.f(5, (n, k1, i1) => {
          return {
            a: n,
            b: n <= (comment.rating || comment.score || 0) ? 1 : ""
          };
        }),
        d: common_vendor.t(comment.rating || comment.score || 0),
        e: common_vendor.t(comment.content || comment.comment || "用户没有填写评价内容"),
        f: common_vendor.t(comment.username || "匿名用户"),
        g: common_vendor.t(comment.orderId),
        h: comment.reply && comment.reply.trim()
      }, comment.reply && comment.reply.trim() ? {
        i: common_vendor.t(comment.reply),
        j: common_vendor.o(($event) => $options.showEditReply(comment), comment.id)
      } : {
        k: $data.replyContents[comment.id],
        l: common_vendor.o((e) => {
          $data.replyContents[comment.id] = e.detail.value;
        }, comment.id),
        m: common_vendor.t($data.submittingId === comment.id ? "提交中..." : "回复"),
        n: common_vendor.o(($event) => $options.submitReply(comment), comment.id),
        o: !$data.replyContents[comment.id] || $data.submittingId === comment.id
      }, {
        p: comment.id
      });
    })
  } : {}, {
    s: $data.showEditDialog
  }, $data.showEditDialog ? {
    t: common_vendor.o((...args) => $options.closeEditDialog && $options.closeEditDialog(...args)),
    v: $data.editReplyContent,
    w: common_vendor.o(($event) => $data.editReplyContent = $event.detail.value),
    x: common_vendor.o((...args) => $options.updateReply && $options.updateReply(...args)),
    y: $data.updatingReply,
    z: common_vendor.o(() => {
    }),
    A: common_vendor.o((...args) => $options.closeEditDialog && $options.closeEditDialog(...args))
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-ea970549"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/merchant/comment-list.js.map
