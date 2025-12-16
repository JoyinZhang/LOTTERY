# AI开发规则 - 微信群抽奖系统

## 一、项目概述

### 1.1 项目背景
本项目为微信群抽奖模块复刻项目，实现完整的抽奖闭环流程，包括活动创建、用户参与、自动开奖、中奖管理等核心功能。

### 1.2 技术栈
- **前端**: 微信小程序原生框架
- **后端**: Java 17 + Spring Boot 3.0+
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.0+
- **构建工具**: Maven 3.8+
- **部署**: Docker + Docker Compose

### 1.3 MVP功能范围
- 单奖项抽奖活动创建
- 三种开奖模式（定时开奖、人数开奖、即抽即中）
- 用户参与抽奖（微信授权）
- 自动开奖执行
- 中奖名单查看
- 基础防刷机制（OpenID去重）

## 二、后端开发规范

### 2.1 项目结构规范

```
lottery-backend/
├── src/main/java/com/lottery/
│   ├── LotteryApplication.java          # 启动类
│   ├── common/                           # 通用组件
│   │   ├── constant/                     # 常量定义
│   │   │   ├── DrawMode.java            # 开奖模式枚举
│   │   │   ├── ActivityStatus.java      # 活动状态枚举
│   │   │   └── ResultCode.java          # 响应码枚举
│   │   ├── exception/                    # 异常定义
│   │   │   ├── BusinessException.java   # 业务异常
│   │   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   │   ├── response/                     # 响应封装
│   │   │   └── Result.java              # 统一响应对象
│   │   └── util/                         # 工具类
│   │       ├── RedisUtil.java            # Redis工具类
│   │       └── UUIDUtil.java             # UUID生成工具
│   ├── config/                           # 配置类
│   │   ├── RedisConfig.java              # Redis配置
│   │   ├── MyBatisConfig.java            # MyBatis配置
│   │   └── WebConfig.java                # Web配置
│   ├── controller/                       # 控制器层
│   │   ├── ActivityController.java       # 活动管理
│   │   ├── ParticipantController.java    # 用户参与
│   │   ├── WinnerController.java         # 中奖管理
│   │   └── AuthController.java           # 微信授权
│   ├── service/                          # 业务层接口
│   │   ├── ActivityService.java
│   │   ├── ParticipantService.java
│   │   ├── LotteryService.java           # 抽奖核心服务
│   │   ├── WinnerService.java
│   │   └── WechatAuthService.java
│   ├── service/impl/                     # 业务层实现
│   │   ├── ActivityServiceImpl.java
│   │   ├── ParticipantServiceImpl.java
│   │   ├── LotteryServiceImpl.java
│   │   ├── WinnerServiceImpl.java
│   │   └── WechatAuthServiceImpl.java
│   ├── mapper/                           # MyBatis Mapper接口
│   │   ├── ActivityMapper.java
│   │   ├── ParticipantMapper.java
│   │   └── WinnerMapper.java
│   ├── entity/                           # 实体类
│   │   ├── Activity.java                 # 活动实体
│   │   ├── Participant.java              # 参与记录实体
│   │   └── Winner.java                   # 中奖记录实体
│   ├── dto/                              # 数据传输对象
│   │   ├── request/                      # 请求DTO
│   │   │   ├── CreateActivityRequest.java
│   │   │   └── JoinActivityRequest.java
│   │   └── response/                     # 响应DTO
│   │       ├── ActivityDetailResponse.java
│   │       └── WinnerListResponse.java
│   └── scheduler/                        # 定时任务
│       └── DrawScheduler.java            # 开奖调度器
├── src/main/resources/
│   ├── application.yml                   # 通用配置
│   ├── application-dev.yml               # 开发环境配置
│   ├── application-prod.yml              # 生产环境配置
│   └── mapper/                           # MyBatis XML映射文件
│       ├── ActivityMapper.xml
│       ├── ParticipantMapper.xml
│       └── WinnerMapper.xml
└── src/test/java/                        # 测试代码
```

### 2.2 编码规范

#### 2.2.1 命名规范

**类命名**
- 实体类: 名词，大驼峰，如 `Activity`, `Participant`
- 控制器: 以Controller结尾，如 `ActivityController`
- 服务接口: 以Service结尾，如 `ActivityService`
- 服务实现: 以ServiceImpl结尾，如 `ActivityServiceImpl`
- Mapper接口: 以Mapper结尾，如 `ActivityMapper`
- DTO: 以Request/Response结尾，如 `CreateActivityRequest`
- 常量类: 大驼峰名词，如 `DrawMode`, `ResultCode`
- 工具类: 以Util结尾，如 `RedisUtil`

**方法命名**
- 查询方法: get/find/query开头，如 `getActivityByCode`
- 新增方法: create/add/insert开头，如 `createActivity`
- 修改方法: update/modify开头，如 `updateActivityStatus`
- 删除方法: delete/remove开头，如 `deleteActivity`
- 布尔方法: is/has/can开头，如 `isActivityFinished`

**变量命名**
- 局部变量和参数: 小驼峰，如 `activityCode`, `prizeCount`
- 常量: 全大写，下划线分隔，如 `MAX_PRIZE_COUNT`
- 集合变量: 复数形式，如 `participants`, `winners`

#### 2.2.2 注释规范

**类注释**
```java
/**
 * 活动管理服务
 * 
 * @author lottery
 * @since 2024-12-16
 */
public class ActivityService {
}
```

**方法注释**
```java
/**
 * 创建抽奖活动
 *
 * @param request 创建活动请求参数
 * @return 活动详情
 * @throws BusinessException 参数校验失败或业务异常
 */
public ActivityDetailResponse createActivity(CreateActivityRequest request);
```

**复杂业务逻辑注释**
- 关键算法必须添加注释说明
- 复杂条件判断需要注释说明业务场景
- 重要配置项必须注释说明用途

### 2.3 分层架构规范

#### 2.3.1 Controller层规范

**职责**
- 接收HTTP请求参数
- 调用Service层业务逻辑
- 封装响应结果
- **不得包含业务逻辑代码**

**规范要求**
```java
@RestController
@RequestMapping("/api/activity")
public class ActivityController {
    
    @Autowired
    private ActivityService activityService;
    
    /**
     * 创建活动
     */
    @PostMapping("/create")
    public Result<ActivityDetailResponse> create(@RequestBody @Valid CreateActivityRequest request) {
        // 仅调用Service，不包含业务逻辑
        ActivityDetailResponse response = activityService.createActivity(request);
        return Result.success(response);
    }
}
```

**关键点**
- 所有接口必须返回统一的 `Result<T>` 对象
- 使用 `@Valid` 注解启用参数校验
- 不在Controller中处理异常，由全局异常处理器统一处理
- 使用 `@RestController` 而不是 `@Controller`

#### 2.3.2 Service层规范

**职责**
- 实现核心业务逻辑
- 调用Mapper层进行数据操作
- 处理事务边界
- 调用外部服务（如微信API）

**规范要求**
```java
@Service
public class ActivityServiceImpl implements ActivityService {
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityDetailResponse createActivity(CreateActivityRequest request) {
        // 1. 参数校验
        validateCreateRequest(request);
        
        // 2. 生成活动编码
        String activityCode = UUIDUtil.generateActivityCode();
        
        // 3. 构建实体对象
        Activity activity = buildActivity(request, activityCode);
        
        // 4. 保存到数据库
        activityMapper.insert(activity);
        
        // 5. 缓存活动信息
        cacheActivity(activity);
        
        // 6. 返回响应
        return buildResponse(activity);
    }
    
    // 私有方法实现具体逻辑
    private void validateCreateRequest(CreateActivityRequest request) {
        // 验证逻辑
    }
}
```

**关键点**
- 接口与实现分离，定义Service接口
- 复杂业务逻辑拆分为私有方法
- 涉及数据修改的方法必须添加 `@Transactional` 注解
- 事务注解必须指定 `rollbackFor = Exception.class`
- 异常抛出使用 `BusinessException`

#### 2.3.3 Mapper层规范

**职责**
- 定义SQL映射接口
- 执行数据库CRUD操作
- **不得包含业务逻辑**

**规范要求**
```java
@Mapper
public interface ActivityMapper {
    
    /**
     * 插入活动记录
     */
    int insert(Activity activity);
    
    /**
     * 根据活动编码查询
     */
    Activity selectByCode(@Param("activityCode") String activityCode);
    
    /**
     * 更新活动状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 查询待开奖的定时活动
     */
    List<Activity> selectPendingTimedActivities(@Param("currentTime") LocalDateTime currentTime);
}
```

**XML规范**
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lottery.mapper.ActivityMapper">
    
    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="com.lottery.entity.Activity">
        <id column="id" property="id" />
        <result column="activity_code" property="activityCode" />
        <result column="creator_openid" property="creatorOpenid" />
        <!-- 其他字段映射 -->
    </resultMap>
    
    <!-- 插入活动 -->
    <insert id="insert" parameterType="com.lottery.entity.Activity" 
            useGeneratedKeys="true" keyProperty="id">
        INSERT INTO lottery_activity (
            activity_code, creator_openid, title, description,
            draw_mode, draw_time, target_participant_count, prize_count,
            status, create_time, update_time
        ) VALUES (
            #{activityCode}, #{creatorOpenid}, #{title}, #{description},
            #{drawMode}, #{drawTime}, #{targetParticipantCount}, #{prizeCount},
            #{status}, #{createTime}, #{updateTime}
        )
    </insert>
    
</mapper>
```

### 2.4 异常处理规范

#### 2.4.1 异常定义

**业务异常类**
```java
public class BusinessException extends RuntimeException {
    
    private Integer code;
    private String message;
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
}
```

**全局异常处理器**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }
    
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ResultCode.SYSTEM_ERROR);
    }
}
```

#### 2.4.2 异常使用规范

- 业务异常统一使用 `BusinessException`
- 异常信息使用 `ResultCode` 枚举定义
- 不要捕获异常后不处理（空catch块）
- Service层抛出异常，Controller层不捕获
- 记录异常日志时必须包含堆栈信息

### 2.5 数据库操作规范

#### 2.5.1 事务管理

**规范**
- 所有涉及数据修改的方法必须添加 `@Transactional`
- 事务注解放在Service实现类的方法上
- 必须指定 `rollbackFor = Exception.class`
- 避免大事务，合理拆分事务边界
- 不要在事务中调用外部接口

**示例**
```java
@Transactional(rollbackFor = Exception.class)
public void executeDrawing(Long activityId) {
    // 1. 查询参与用户
    List<Participant> participants = participantMapper.selectByActivityId(activityId);
    
    // 2. 执行抽奖算法
    List<Participant> winners = lotteryAlgorithm(participants);
    
    // 3. 保存中奖记录
    winnerMapper.batchInsert(winners);
    
    // 4. 更新活动状态
    activityMapper.updateStatus(activityId, ActivityStatus.FINISHED.getValue());
}
```

#### 2.5.2 SQL编写规范

- 所有SQL语句写在XML文件中，不使用注解SQL
- 使用 `#{}` 参数占位符，避免SQL注入
- 查询字段明确列出，不使用 `SELECT *`
- 批量插入使用 `<foreach>` 标签
- 复杂查询使用 `<where>` 和 `<if>` 动态SQL
- 必须添加适当的索引

### 2.6 Redis使用规范

#### 2.6.1 Key命名规范

**格式**: `业务模块:功能:业务标识`

**示例**
```
lottery:lock:123456                    # 分布式锁
lottery:activity:cache:123456          # 活动信息缓存
lottery:participant:count:123456       # 参与人数计数器
lottery:user:participated:openid123    # 用户已参与活动集合
```

#### 2.6.2 使用规范

**设置过期时间**
- 所有缓存必须设置过期时间
- 分布式锁: 30秒
- 活动信息缓存: 1小时
- 参与记录缓存: 7天
- 计数器: 活动结束后7天

**分布式锁**
```java
public void executeWithLock(Long activityId) {
    String lockKey = "lottery:lock:" + activityId;
    boolean locked = redisUtil.tryLock(lockKey, 30);
    
    if (!locked) {
        throw new BusinessException(ResultCode.SYSTEM_BUSY);
    }
    
    try {
        // 执行业务逻辑
        doBusinessLogic(activityId);
    } finally {
        // 释放锁
        redisUtil.unlock(lockKey);
    }
}
```

**原子操作**
- 计数器使用 `INCR`/`DECR` 命令
- 库存扣减使用 `DECR` 命令
- 避免先查询再修改的非原子操作

### 2.7 核心业务实现规范

#### 2.7.1 Fisher-Yates抽奖算法

**算法实现**
```java
/**
 * Fisher-Yates洗牌算法实现抽奖
 *
 * @param participants 参与用户列表
 * @param prizeCount 奖品数量
 * @return 中奖用户列表
 */
public List<Participant> fisherYatesLottery(List<Participant> participants, int prizeCount) {
    if (participants.size() <= prizeCount) {
        return participants;
    }
    
    // 复制列表避免修改原列表
    List<Participant> shuffled = new ArrayList<>(participants);
    Random random = new Random();
    
    // Fisher-Yates洗牌
    for (int i = shuffled.size() - 1; i > 0; i--) {
        int j = random.nextInt(i + 1);
        // 交换元素
        Participant temp = shuffled.get(i);
        shuffled.set(i, shuffled.get(j));
        shuffled.set(j, temp);
    }
    
    // 取前N个作为中奖者
    return shuffled.subList(0, prizeCount);
}
```

#### 2.7.2 即抽即中算法

```java
/**
 * 即抽即中模式抽奖
 *
 * @param activityId 活动ID
 * @param prizeCount 总奖品数
 * @return 是否中奖
 */
public boolean instantDrawing(Long activityId, int prizeCount) {
    String stockKey = "lottery:prize:stock:" + activityId;
    
    // 使用Redis DECR原子扣减库存
    Long remainStock = redisUtil.decrement(stockKey);
    
    if (remainStock < 0) {
        // 库存不足，未中奖
        redisUtil.increment(stockKey); // 回滚
        return false;
    }
    
    // 中奖概率计算
    Random random = new Random();
    double winRate = calculateWinRate(remainStock, prizeCount);
    
    return random.nextDouble() < winRate;
}

/**
 * 计算中奖概率
 */
private double calculateWinRate(long remainStock, int prizeCount) {
    // 预期中奖率30%
    double expectedRate = 0.3;
    double maxParticipants = prizeCount / expectedRate;
    return remainStock / maxParticipants;
}
```

#### 2.7.3 定时开奖调度器

```java
@Component
public class DrawScheduler {
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Autowired
    private LotteryService lotteryService;
    
    /**
     * 每分钟扫描一次待开奖活动
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scanPendingDraws() {
        LocalDateTime now = LocalDateTime.now();
        
        // 查询当前分钟内应该开奖的活动
        List<Activity> activities = activityMapper.selectPendingTimedActivities(now);
        
        for (Activity activity : activities) {
            try {
                // 执行开奖
                lotteryService.executeDrawing(activity.getId());
            } catch (Exception e) {
                log.error("开奖失败，活动ID: {}", activity.getId(), e);
            }
        }
    }
}
```

### 2.8 日志规范

#### 2.8.1 日志级别

- **ERROR**: 系统异常、业务错误
- **WARN**: 警告信息、降级处理
- **INFO**: 关键业务流程、接口调用
- **DEBUG**: 调试信息、详细参数

#### 2.8.2 日志格式

```java
// 关键业务日志
log.info("创建活动成功，活动编码: {}, 创建者: {}", activityCode, creatorOpenid);

// 异常日志
log.error("开奖执行失败，活动ID: {}", activityId, e);

// 调试日志
log.debug("参与人数检查，活动ID: {}, 当前人数: {}, 目标人数: {}", 
    activityId, currentCount, targetCount);
```

## 三、前端开发规范

### 3.1 项目结构规范

```
lottery-miniprogram/
├── pages/                              # 页面目录
│   ├── create/                         # 活动创建页
│   │   ├── create.js
│   │   ├── create.json
│   │   ├── create.wxml
│   │   └── create.wxss
│   ├── activity/                       # 活动详情页
│   │   ├── activity.js
│   │   ├── activity.json
│   │   ├── activity.wxml
│   │   └── activity.wxss
│   ├── winners/                        # 中奖名单页
│   │   ├── winners.js
│   │   ├── winners.json
│   │   ├── winners.wxml
│   │   └── winners.wxss
│   └── my-activities/                  # 我的活动页
│       ├── my-activities.js
│       ├── my-activities.json
│       ├── my-activities.wxml
│       └── my-activities.wxss
├── components/                         # 组件目录
│   ├── countdown/                      # 倒计时组件
│   │   ├── countdown.js
│   │   ├── countdown.json
│   │   ├── countdown.wxml
│   │   └── countdown.wxss
│   └── loading/                        # 加载组件
│       ├── loading.js
│       ├── loading.json
│       ├── loading.wxml
│       └── loading.wxss
├── utils/                              # 工具类
│   ├── request.js                      # 网络请求封装
│   ├── auth.js                         # 授权相关
│   ├── storage.js                      # 本地存储封装
│   └── util.js                         # 通用工具
├── api/                                # API接口定义
│   ├── activity.js                     # 活动相关接口
│   ├── participant.js                  # 参与相关接口
│   └── winner.js                       # 中奖相关接口
├── config/                             # 配置文件
│   └── config.js                       # 全局配置
├── app.js                              # 小程序主入口
├── app.json                            # 小程序全局配置
├── app.wxss                            # 全局样式
└── project.config.json                 # 项目配置
```

### 3.2 编码规范

#### 3.2.1 命名规范

**文件命名**
- 页面/组件: 小写字母，中划线分隔，如 `my-activities`
- JS文件: 小驼峰，如 `request.js`, `auth.js`

**变量命名**
- 普通变量: 小驼峰，如 `activityCode`, `prizeCount`
- 常量: 全大写，下划线分隔，如 `API_BASE_URL`
- 私有变量: 下划线开头，如 `_timer`
- 布尔变量: is/has/can开头，如 `isLoading`, `hasAuth`

**函数命名**
- 事件处理函数: on开头，如 `onSubmit`, `onShareAppMessage`
- 普通函数: 小驼峰，动词开头，如 `loadData`, `validateForm`
- 生命周期函数: 使用小程序规定的名称，如 `onLoad`, `onShow`

#### 3.2.2 代码风格

**缩进**: 2空格

**Page结构顺序**
```javascript
Page({
  // 1. 页面数据
  data: {
    activityCode: '',
    activityInfo: {},
    isLoading: false
  },
  
  // 2. 生命周期函数
  onLoad(options) {
    this.loadActivityDetail(options.code);
  },
  
  onShow() {
    // 页面显示逻辑
  },
  
  onShareAppMessage() {
    // 分享配置
  },
  
  // 3. 事件处理函数
  onParticipateClick() {
    // 参与按钮点击
  },
  
  onSubmit() {
    // 表单提交
  },
  
  // 4. 自定义函数
  loadActivityDetail(code) {
    // 加载活动详情
  },
  
  validateForm() {
    // 表单验证
  }
});
```

### 3.3 网络请求规范

#### 3.3.1 请求封装

**request.js**
```javascript
const config = require('../config/config.js');

/**
 * 封装网络请求
 */
function request(url, method = 'GET', data = {}) {
  return new Promise((resolve, reject) => {
    wx.showLoading({ title: '加载中...', mask: true });
    
    wx.request({
      url: config.API_BASE_URL + url,
      method: method,
      data: data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': wx.getStorageSync('token') || ''
      },
      success(res) {
        wx.hideLoading();
        
        if (res.data.code === 0) {
          resolve(res.data.data);
        } else if (res.data.code === 2001) {
          // Token过期，重新授权
          wx.removeStorageSync('token');
          wx.showToast({ title: '请重新授权', icon: 'none' });
          reject(res.data);
        } else {
          wx.showToast({ title: res.data.message, icon: 'none' });
          reject(res.data);
        }
      },
      fail(err) {
        wx.hideLoading();
        wx.showToast({ title: '网络不给力，请稍后重试', icon: 'none' });
        reject(err);
      }
    });
  });
}

module.exports = {
  get: (url, data) => request(url, 'GET', data),
  post: (url, data) => request(url, 'POST', data)
};
```

#### 3.3.2 API定义

**api/activity.js**
```javascript
const request = require('../utils/request.js');

/**
 * 创建活动
 */
function createActivity(data) {
  return request.post('/api/activity/create', data);
}

/**
 * 获取活动详情
 */
function getActivityDetail(activityCode) {
  return request.get('/api/activity/detail', { activityCode });
}

module.exports = {
  createActivity,
  getActivityDetail
};
```

### 3.4 授权处理规范

**auth.js**
```javascript
/**
 * 微信登录授权
 */
function wxLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success(res) {
        if (res.code) {
          resolve(res.code);
        } else {
          reject('登录失败');
        }
      },
      fail(err) {
        reject(err);
      }
    });
  });
}

/**
 * 获取用户信息授权
 */
function getUserProfile() {
  return new Promise((resolve, reject) => {
    wx.getUserProfile({
      desc: '用于展示中奖信息',
      success(res) {
        resolve(res.userInfo);
      },
      fail(err) {
        reject(err);
      }
    });
  });
}

/**
 * 完整授权流程
 */
async function authorize() {
  try {
    // 1. 获取用户信息
    const userInfo = await getUserProfile();
    
    // 2. 微信登录获取code
    const code = await wxLogin();
    
    // 3. 发送到后端换取token
    const res = await request.post('/api/auth/login', {
      code: code,
      nickname: userInfo.nickName,
      avatarUrl: userInfo.avatarUrl
    });
    
    // 4. 保存token
    wx.setStorageSync('token', res.token);
    wx.setStorageSync('openid', res.openid);
    
    return res;
  } catch (err) {
    wx.showToast({ title: '授权失败', icon: 'none' });
    throw err;
  }
}

module.exports = {
  wxLogin,
  getUserProfile,
  authorize
};
```

### 3.5 表单验证规范

**create.js示例**
```javascript
const activityApi = require('../../api/activity.js');

Page({
  data: {
    form: {
      title: '',
      description: '',
      drawMode: 1,
      drawTime: '',
      targetParticipantCount: '',
      prizeCount: ''
    }
  },
  
  /**
   * 表单提交
   */
  onSubmit() {
    // 表单验证
    if (!this.validateForm()) {
      return;
    }
    
    // 提交请求
    activityApi.createActivity(this.data.form)
      .then(res => {
        wx.showToast({ title: '创建成功', icon: 'success' });
        // 跳转到活动详情页
        wx.redirectTo({
          url: `/pages/activity/activity?code=${res.activityCode}`
        });
      })
      .catch(err => {
        console.error('创建失败', err);
      });
  },
  
  /**
   * 表单验证
   */
  validateForm() {
    const { title, drawMode, drawTime, targetParticipantCount, prizeCount } = this.data.form;
    
    // 标题验证
    if (!title || title.trim() === '') {
      wx.showToast({ title: '请输入活动标题', icon: 'none' });
      return false;
    }
    
    if (title.length > 24) {
      wx.showToast({ title: '活动标题不能超过24个字', icon: 'none' });
      return false;
    }
    
    // 奖品份数验证
    if (!prizeCount || prizeCount <= 0) {
      wx.showToast({ title: '请输入奖品份数', icon: 'none' });
      return false;
    }
    
    // 定时开奖验证
    if (drawMode === 1) {
      if (!drawTime) {
        wx.showToast({ title: '请选择开奖时间', icon: 'none' });
        return false;
      }
      
      const drawTimestamp = new Date(drawTime).getTime();
      const now = Date.now();
      
      if (drawTimestamp <= now + 5 * 60 * 1000) {
        wx.showToast({ title: '开奖时间必须晚于当前时间5分钟', icon: 'none' });
        return false;
      }
    }
    
    // 人数开奖验证
    if (drawMode === 2) {
      if (!targetParticipantCount || targetParticipantCount < prizeCount) {
        wx.showToast({ title: '目标人数必须大于等于奖品份数', icon: 'none' });
        return false;
      }
    }
    
    return true;
  }
});
```

### 3.6 分享配置规范

```javascript
Page({
  data: {
    activityInfo: {}
  },
  
  /**
   * 分享配置
   */
  onShareAppMessage() {
    const { activityCode, title, prizeCount } = this.data.activityInfo;
    
    return {
      title: `快来参加抽奖，${prizeCount}份奖品等你拿！`,
      path: `/pages/activity/activity?code=${activityCode}`,
      imageUrl: '' // 使用默认封面图
    };
  }
});
```

## 四、数据库设计规范

### 4.1 表设计规范

**字段命名**
- 使用小写字母和下划线
- 主键统一命名为 `id`
- 外键命名为 `关联表名_id`，如 `activity_id`
- 布尔字段使用 `is_` 前缀，如 `is_winner`
- 时间字段使用 `_time` 后缀，如 `create_time`

**字段类型**
- 主键: BIGINT，自增
- 状态/类型: TINYINT
- 字符串: VARCHAR，明确长度
- 文本: TEXT
- 时间: DATETIME
- 金额: DECIMAL(10,2)

**必备字段**
- 每张表必须包含: `id`, `create_time`, `update_time`
- 逻辑删除表需要: `is_deleted`

### 4.2 索引设计规范

**索引类型**
- 主键索引: 自动创建
- 唯一索引: 业务唯一字段，如 `activity_code`
- 普通索引: 查询条件字段，如 `creator_openid`, `status`
- 组合索引: 多条件查询，如 `(activity_id, openid)`

**索引命名**
- 普通索引: `idx_字段名`，如 `idx_status`
- 唯一索引: `uk_字段名`，如 `uk_activity_code`
- 组合索引: `idx_字段1_字段2`，如 `idx_activity_openid`

## 五、测试规范

### 5.1 单元测试规范

**测试类命名**: 被测试类名 + Test，如 `ActivityServiceTest`

**测试方法命名**: test + 方法名 + 场景，如 `testCreateActivity_Success`

**测试结构**: Given-When-Then

```java
@SpringBootTest
public class ActivityServiceTest {
    
    @Autowired
    private ActivityService activityService;
    
    @Test
    public void testCreateActivity_Success() {
        // Given: 准备测试数据
        CreateActivityRequest request = new CreateActivityRequest();
        request.setTitle("测试活动");
        request.setDrawMode(1);
        request.setPrizeCount(10);
        
        // When: 执行测试
        ActivityDetailResponse response = activityService.createActivity(request);
        
        // Then: 验证结果
        assertNotNull(response);
        assertNotNull(response.getActivityCode());
        assertEquals("测试活动", response.getTitle());
    }
    
    @Test
    public void testCreateActivity_InvalidTitle() {
        // Given: 标题为空
        CreateActivityRequest request = new CreateActivityRequest();
        request.setTitle("");
        
        // When & Then: 预期抛出异常
        assertThrows(BusinessException.class, () -> {
            activityService.createActivity(request);
        });
    }
}
```

### 5.2 测试覆盖要求

- Service层核心业务逻辑必须编写单元测试
- 抽奖算法必须编写单元测试
- 测试覆盖率目标: 70%以上

## 六、部署配置规范

### 6.1 配置文件规范

**application.yml (通用配置)**
```yaml
spring:
  application:
    name: lottery-backend

server:
  port: 8080

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.lottery.entity
```

**application-dev.yml (开发环境)**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lottery?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  redis:
    host: localhost
    port: 6379
    database: 0

logging:
  level:
    root: INFO
    com.lottery: DEBUG
```

### 6.2 Docker配置规范

**Dockerfile**
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/lottery-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml**
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: lottery123
      MYSQL_DATABASE: lottery
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
  
  redis:
    image: redis:6.0-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
  
  lottery-backend:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_PROFILES_ACTIVE: prod
      MYSQL_HOST: mysql
      REDIS_HOST: redis

volumes:
  mysql-data:
  redis-data:
```

## 七、常见问题处理指引

### 7.1 并发问题

**问题**: 开奖时可能被多次触发
**解决**: 使用Redis分布式锁

**问题**: 人数开奖时计数不准确
**解决**: 使用Redis INCR原子操作

### 7.2 性能问题

**问题**: 活动详情页频繁查询数据库
**解决**: 使用Redis缓存活动信息

**问题**: 中奖名单查询慢
**解决**: 添加数据库索引，缓存中奖名单

### 7.3 数据一致性

**问题**: 开奖后中奖记录与参与记录不一致
**解决**: 使用事务保证原子性

**问题**: Redis缓存与数据库数据不一致
**解决**: 更新数据库后同步更新缓存，设置合理过期时间

## 八、开发流程

### 8.1 开发步骤

1. **需求分析**: 理解PRD和技术方案
2. **数据库设计**: 创建建表SQL
3. **实体类和Mapper**: 编写实体类和Mapper接口
4. **Service层**: 实现业务逻辑
5. **Controller层**: 实现接口
6. **单元测试**: 编写并运行单元测试
7. **集成测试**: 完整流程测试
8. **代码审查**: 检查代码规范
9. **部署上线**: Docker部署

### 8.2 代码提交规范

**提交信息格式**: `[类型] 简短描述`

**类型**
- feat: 新功能
- fix: Bug修复
- docs: 文档更新
- style: 代码格式调整
- refactor: 代码重构
- test: 测试代码
- chore: 构建/配置修改

**示例**
```
[feat] 实现活动创建接口
[fix] 修复定时开奖重复执行问题
[refactor] 优化抽奖算法性能
```

## 九、注意事项

### 9.1 安全注意事项

- 所有用户输入必须进行参数校验
- 使用参数化查询防止SQL注入
- 敏感信息不记录到日志
- Token必须设置过期时间
- 接口必须进行限流

### 9.2 性能注意事项

- 避免N+1查询问题
- 大数据量查询使用分页
- 合理使用缓存
- 避免在循环中调用数据库
- 定时任务避免重复执行

### 9.3 代码质量注意事项

- 遵循单一职责原则
- 避免过长方法（建议不超过50行）
- 避免过深嵌套（建议不超过3层）
- 及时清理无用代码
- 保持代码可读性

---

**本规则文档为AI开发过程中的强制性规范，所有代码生成必须严格遵守以上规则。**
