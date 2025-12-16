// utils/util.js

/**
 * 格式化时间
 */
function formatTime(date) {
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hour = date.getHours();
  const minute = date.getMinutes();
  const second = date.getSeconds();

  return `${[year, month, day].map(formatNumber).join('-')} ${[hour, minute, second].map(formatNumber).join(':')}`;
}

function formatNumber(n) {
  n = n.toString();
  return n[1] ? n : `0${n}`;
}

/**
 * 获取活动状态文本
 */
function getActivityStatusText(status) {
  const statusMap = {
    1: '进行中',
    2: '已开奖',
    3: '已取消'
  };
  return statusMap[status] || '未知';
}

/**
 * 获取开奖模式文本
 */
function getDrawModeText(mode) {
  const modeMap = {
    1: '定时开奖',
    2: '人数开奖',
    3: '即抽即中'
  };
  return modeMap[mode] || '未知';
}

/**
 * 复制到剪贴板
 */
function copyToClipboard(text) {
  return new Promise((resolve, reject) => {
    wx.setClipboardData({
      data: text,
      success: () => {
        wx.showToast({
          title: '复制成功',
          icon: 'success'
        });
        resolve();
      },
      fail: reject
    });
  });
}

/**
 * 分享活动
 */
function shareActivity(activityCode, title) {
  return {
    title: title || '快来参加抽奖吧！',
    path: `/pages/activity/activity?code=${activityCode}`,
    imageUrl: ''
  };
}

module.exports = {
  formatTime,
  getActivityStatusText,
  getDrawModeText,
  copyToClipboard,
  shareActivity
};
