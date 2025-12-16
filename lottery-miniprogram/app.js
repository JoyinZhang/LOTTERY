// app.js
App({
  globalData: {
    apiUrl: 'http://localhost:8080/api',
    userInfo: null,
    openid: null,
    token: null
  },

  onLaunch() {
    // 检查登录状态
    const token = wx.getStorageSync('token');
    const openid = wx.getStorageSync('openid');
    
    if (token && openid) {
      this.globalData.token = token;
      this.globalData.openid = openid;
    } else {
      // 自动登录
      this.login();
    }
  },

  // 微信登录
  login() {
    const that = this;
    wx.login({
      success(res) {
        if (res.code) {
          // 调用后端登录接口
          wx.request({
            url: `${that.globalData.apiUrl}/auth/login`,
            method: 'POST',
            data: {
              code: res.code
            },
            success(loginRes) {
              if (loginRes.data.code === 200) {
                const { token, openid } = loginRes.data.data;
                that.globalData.token = token;
                that.globalData.openid = openid;
                
                // 缓存登录信息
                wx.setStorageSync('token', token);
                wx.setStorageSync('openid', openid);
              } else {
                wx.showToast({
                  title: '登录失败',
                  icon: 'none'
                });
              }
            }
          });
        }
      }
    });
  },

  // 获取用户信息
  getUserInfo() {
    const that = this;
    return new Promise((resolve, reject) => {
      wx.getUserProfile({
        desc: '用于完善用户资料',
        success(res) {
          that.globalData.userInfo = res.userInfo;
          wx.setStorageSync('userInfo', res.userInfo);
          resolve(res.userInfo);
        },
        fail(err) {
          reject(err);
        }
      });
    });
  }
})
