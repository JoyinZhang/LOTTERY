// pages/create/create.js
const app = getApp();
const api = require('../../utils/api.js');

Page({
  data: {
    title: '',
    description: '',
    prizeCount: 1,
    drawMode: 1, // 1-定时 2-人数 3-即抽即中
    drawModes: [
      { value: 1, label: '定时开奖' },
      { value: 2, label: '人数开奖' },
      { value: 3, label: '即抽即中' }
    ],
    drawTime: '',
    targetCount: 10
  },

  onTitleInput(e) {
    this.setData({ title: e.detail.value });
  },

  onDescInput(e) {
    this.setData({ description: e.detail.value });
  },

  onPrizeCountChange(e) {
    this.setData({ prizeCount: parseInt(e.detail.value) });
  },

  onDrawModeChange(e) {
    this.setData({ drawMode: parseInt(e.detail.value) });
  },

  onDrawTimeChange(e) {
    this.setData({ drawTime: e.detail.value });
  },

  onTargetCountChange(e) {
    this.setData({ targetCount: parseInt(e.detail.value) });
  },

  async createActivity() {
    const { title, description, prizeCount, drawMode, drawTime, targetCount } = this.data;

    // 校验
    if (!title) {
      wx.showToast({ title: '请输入活动标题', icon: 'none' });
      return;
    }

    if (prizeCount < 1) {
      wx.showToast({ title: '奖品数量至少为1', icon: 'none' });
      return;
    }

    if (drawMode === 1 && !drawTime) {
      wx.showToast({ title: '请选择开奖时间', icon: 'none' });
      return;
    }

    if (drawMode === 2 && targetCount < prizeCount) {
      wx.showToast({ title: '目标人数不能小于奖品数量', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '创建中...' });

    try {
      const data = {
        creatorOpenid: app.globalData.openid,
        title,
        description,
        prizeCount,
        drawMode
      };

      if (drawMode === 1) {
        data.drawTime = drawTime.replace('T', ' ') + ':00';
      } else if (drawMode === 2) {
        data.targetParticipantCount = targetCount;
      }

      const result = await api.createActivity(data);

      wx.hideLoading();
      wx.showToast({ title: '创建成功', icon: 'success' });

      setTimeout(() => {
        wx.redirectTo({
          url: `/pages/activity/activity?code=${result.activityCode}`
        });
      }, 1500);

    } catch (err) {
      wx.hideLoading();
      console.error('创建活动失败', err);
    }
  }
})
