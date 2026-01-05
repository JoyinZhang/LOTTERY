// pages/my/my.js
Page({
  data: {
    userInfo: null,
    hasUserInfo: false
  },

  onLoad() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.setData({
        userInfo,
        hasUserInfo: true
      });
    }
  }
})
