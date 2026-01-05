// pages/activity/activity.js
const app = getApp();
const api = require('../../utils/api.js');
const util = require('../../utils/util.js');

Page({
  data: {
    activityCode: '',
    activity: null,
    hasParticipated: false,
    isWinner: false,
    userInfo: null
  },

  onLoad(options) {
    if (options.code) {
      this.setData({ activityCode: options.code });
      this.loadActivity();
      this.checkParticipantStatus();
    }
  },

  async loadActivity() {
    try {
      const activity = await api.getActivityDetail(this.data.activityCode);
      this.setData({ activity });
    } catch (err) {
      console.error('加载活动失败', err);
    }
  },

  async checkParticipantStatus() {
    const openid = app.globalData.openid;
    if (!openid) return;

    try {
      const status = await api.getParticipantStatus(this.data.activityCode, openid);
      this.setData({
        hasParticipated: status.hasParticipated,
        isWinner: status.isWinner
      });
    } catch (err) {
      console.error('查询参与状态失败', err);
    }
  },

  async joinActivity() {
    if (!app.globalData.userInfo) {
      const userInfo = await app.getUserInfo();
      this.setData({ userInfo });
    }

    const { activityCode } = this.data;
    const { openid } = app.globalData;
    const { nickName, avatarUrl } = app.globalData.userInfo;

    wx.showLoading({ title: '参与中...' });

    try {
      const result = await api.joinActivity({
        activityCode,
        openid,
        nickname: nickName,
        avatarUrl
      });

      wx.hideLoading();
      
      this.setData({
        hasParticipated: true,
        isWinner: result.isWinner
      });

      wx.showModal({
        title: result.isWinner ? '恭喜中奖！' : '参与成功',
        content: result.message,
        showCancel: false
      });

      this.loadActivity();
    } catch (err) {
      wx.hideLoading();
      console.error('参与抽奖失败', err);
    }
  },

  goToWinners() {
    wx.navigateTo({
      url: `/pages/winners/winners?code=${this.data.activityCode}`
    });
  },

  shareActivity() {
    return util.shareActivity(this.data.activityCode, this.data.activity.title);
  },

  onShareAppMessage() {
    return this.shareActivity();
  }
})
