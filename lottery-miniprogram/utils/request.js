// utils/request.js
const app = getApp();

/**
 * 封装HTTP请求
 */
function request(options) {
  return new Promise((resolve, reject) => {
    const { url, method = 'GET', data = {}, needAuth = true } = options;

    const header = {
      'Content-Type': 'application/json'
    };

    // 添加认证token
    if (needAuth && app.globalData.token) {
      header['Authorization'] = `Bearer ${app.globalData.token}`;
    }

    wx.request({
      url: `${app.globalData.apiUrl}${url}`,
      method,
      data,
      header,
      success(res) {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data);
          } else {
            wx.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            });
            reject(res.data);
          }
        } else {
          wx.showToast({
            title: '网络请求失败',
            icon: 'none'
          });
          reject(res);
        }
      },
      fail(err) {
        wx.showToast({
          title: '网络连接失败',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
}

module.exports = {
  get: (url, data, needAuth = true) => request({ url, method: 'GET', data, needAuth }),
  post: (url, data, needAuth = true) => request({ url, method: 'POST', data, needAuth }),
  put: (url, data, needAuth = true) => request({ url, method: 'PUT', data, needAuth }),
  delete: (url, data, needAuth = true) => request({ url, method: 'DELETE', data, needAuth })
};
