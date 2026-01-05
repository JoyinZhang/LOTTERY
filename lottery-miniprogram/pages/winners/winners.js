// pages/winners/winners.js
const api = require('../../utils/api.js');

Page({
  data: {
    activityCode: '',
    winnerList: null
  },

  onLoad(options) {
    if (options.code) {
      this.setData({ activityCode: options.code });
      this.loadWinners();
    }
  },

  async loadWinners() {
    try {
      const result = await api.getWinnerList(this.data.activityCode);
      this.setData({ winnerList: result });
    } catch (err) {
      console.error('加载中奖名单失败', err);
    }
  }
})
