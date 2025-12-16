// utils/api.js
const request = require('./request.js');

/**
 * API接口封装
 */
module.exports = {
  // 登录接口
  login(code) {
    return request.post('/auth/login', { code }, false);
  },

  // 创建活动
  createActivity(data) {
    return request.post('/activity/create', data);
  },

  // 获取活动详情
  getActivityDetail(activityCode) {
    return request.get('/activity/detail', { activityCode });
  },

  // 参与抽奖
  joinActivity(data) {
    return request.post('/participant/join', data);
  },

  // 查询参与状态
  getParticipantStatus(activityCode, openid) {
    return request.get('/participant/status', { activityCode, openid });
  },

  // 查询中奖名单
  getWinnerList(activityCode) {
    return request.get('/winner/list', { activityCode });
  },

  // 查询我的活动
  getMyActivities(creatorOpenid, page = 1, size = 10) {
    return request.get('/activity/my', { creatorOpenid, page, size });
  }
};
