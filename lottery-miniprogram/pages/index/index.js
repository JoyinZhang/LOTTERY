// pages/index/index.js
const app = getApp();

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
  },

  // 获取用户信息
  async getUserProfile() {
    try {
      const userInfo = await app.getUserInfo();
      this.setData({
        userInfo,
        hasUserInfo: true
      });
    } catch (err) {
      console.error('获取用户信息失败', err);
    }
  },

  // 创建抽奖
  goToCreate() {
    if (!this.data.hasUserInfo) {
      this.getUserProfile();
      return;
    }
    wx.navigateTo({
      url: '/pages/create/create'
    });
  },

  // 扫码参与
  scanCode() {
    wx.scanCode({
      onlyFromCamera: true,
      success: (res) => {
        // 解析二维码内容，跳转到活动页
        const activityCode = this.parseActivityCode(res.result);
        if (activityCode) {
          wx.navigateTo({
            url: `/pages/activity/activity?code=${activityCode}`
          });
        }
      }
    });
  },

  // 解析活动码
  parseActivityCode(text) {
    // 如果是完整URL，提取code参数
    if (text.includes('code=')) {
      const match = text.match(/code=([^&]+)/);
      return match ? match[1] : null;
    }
    // 否则直接返回
    return text;
  },

  onShareAppMessage() {
    return {
      title: '一起来抽奖吧！',
      path: '/pages/index/index'
    };
  }
})
