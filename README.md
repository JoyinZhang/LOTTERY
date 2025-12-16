# 微信群抽奖系统 - 项目说明

## 项目概述

本项目是"微信抽奖助手"小程序的完整复刻,实现了微信群抽奖的核心功能,包括活动创建、用户参与、自动开奖和中奖管理等完整闭环流程。

## 技术栈

### 后端
- **框架**: Spring Boot 3.0.12
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.0+
- **ORM**: MyBatis 3.0.3
- **构建工具**: Maven 3.8+
- **JDK版本**: Java 17

### 前端
- **框架**: 微信小程序原生框架
- **开发语言**: JavaScript
- **UI组件**: 微信小程序官方组件

### 部署
- **容器化**: Docker + Docker Compose
- **反向代理**: Nginx (可选)

## 项目结构

```
LOTTERY/
├── PRD/
│   └── PRD.md                          # 产品需求文档
├── lottery-backend/                     # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lottery/
│   │   │   │   ├── LotteryApplication.java
│   │   │   │   ├── common/             # 通用组件
│   │   │   │   │   ├── constant/       # 常量定义
│   │   │   │   │   ├── exception/      # 异常处理
│   │   │   │   │   ├── response/       # 响应封装
│   │   │   │   │   └── util/           # 工具类
│   │   │   │   ├── config/             # 配置类
│   │   │   │   ├── controller/         # 控制器
│   │   │   │   ├── dto/                # 数据传输对象
│   │   │   │   ├── entity/             # 实体类
│   │   │   │   ├── mapper/             # Mapper接口
│   │   │   │   ├── service/            # 服务接口
│   │   │   │   └── scheduler/          # 定时任务
│   │   │   └── resources/
│   │   │       ├── mapper/             # MyBatis XML
│   │   │       ├── sql/                # 数据库脚本
│   │   │       ├── application.yml     # 配置文件
│   │   │       ├── application-dev.yml
│   │   │       └── application-prod.yml
│   │   └── test/                       # 测试代码
│   ├── Dockerfile
│   └── pom.xml
├── lottery-miniprogram/                 # 微信小程序(待开发)
├── docker-compose.yml                   # Docker编排文件
├── .qoder/
│   ├── quests/                         # 技术方案设计
│   └── AI_RULES.md                     # AI开发规则
└── README.md
```

## 核心功能

### MVP版本功能
1. **活动创建**
   - 单奖项抽奖活动
   - 三种开奖模式：定时开奖、人数开奖、即抽即中
   - 活动信息配置

2. **用户参与**
   - 微信授权登录
   - 参与抽奖
   - 防重复参与

3. **自动开奖**
   - 定时开奖：定时任务扫描
   - 人数开奖：人数达标触发
   - 即抽即中：实时抽奖
   - Fisher-Yates洗牌算法

4. **中奖管理**
   - 中奖名单查询
   - 中奖状态展示

5. **防刷机制**
   - OpenID去重
   - 接口限流
   - 分布式锁

## 数据库设计

### 核心表
1. **lottery_activity**: 活动表
2. **lottery_participant**: 参与记录表
3. **lottery_winner**: 中奖记录表

详细建表SQL见: `lottery-backend/src/main/resources/sql/schema.sql`

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Docker & Docker Compose (可选)

### 本地开发

#### 1. 数据库初始化
```bash
# 连接MySQL
mysql -u root -p

# 执行建表脚本
source lottery-backend/src/main/resources/sql/schema.sql
```

#### 2. 配置文件
修改 `lottery-backend/src/main/resources/application-dev.yml`:
```yaml
wechat:
  appid: your_appid_here        # 替换为你的小程序AppID
  secret: your_secret_here      # 替换为你的小程序AppSecret
```

#### 3. 启动后端服务
```bash
cd lottery-backend
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

访问: http://localhost:8080

### Docker部署

#### 1. 配置环境变量
创建 `.env` 文件:
```env
WECHAT_APPID=your_appid_here
WECHAT_SECRET=your_secret_here
```

#### 2. 构建并启动
```bash
# 构建后端jar包
cd lottery-backend
mvn clean package -DskipTests

# 启动所有服务
cd ..
docker-compose up -d
```

#### 3. 查看日志
```bash
docker-compose logs -f lottery-backend
```

#### 4. 停止服务
```bash
docker-compose down
```

## API接口

### 认证接口
- `POST /api/auth/login` - 微信登录

### 活动接口
- `POST /api/activity/create` - 创建活动
- `GET /api/activity/detail` - 获取活动详情

### 参与接口
- `POST /api/participant/join` - 参与抽奖
- `GET /api/participant/status` - 查询参与状态

### 中奖接口
- `GET /api/winner/list` - 查询中奖名单

详细接口文档见: `.qoder/quests/wechat-lottery-module-replication.md`

## 开发规范

详见: `.qoder/AI_RULES.md`

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 统一异常处理
- 统一响应格式

### 提交规范
```
[类型] 简短描述

类型: feat/fix/docs/style/refactor/test/chore
```

## 测试

### 单元测试
```bash
cd lottery-backend
mvn test
```

### 接口测试
使用Postman或curl测试接口

示例:
```bash
# 创建活动
curl -X POST http://localhost:8080/api/activity/create \
  -H "Content-Type: application/json" \
  -d '{
    "creatorOpenid": "test_openid",
    "title": "测试抽奖",
    "drawMode": 3,
    "prizeCount": 5
  }'
```

## 项目状态

### 已完成
- ✅ PRD文档完善
- ✅ 技术方案设计
- ✅ AI开发规则编写
- ✅ 数据库设计和建表SQL
- ✅ Spring Boot项目初始化
- ✅ 实体类和Mapper
- ✅ 通用组件(异常、响应、工具类)
- ✅ 微信授权模块
- ✅ 活动管理模块(核心)
- ✅ Docker部署配置

### 待完成
- ⏳ 参与模块完整实现
- ⏳ 抽奖引擎完整实现
- ⏳ 定时调度器实现
- ⏳ 中奖管理模块实现
- ⏳ 单元测试编写
- ⏳ 微信小程序前端开发
- ⏳ 前后端联调测试
- ⏳ 性能优化

## 后续迭代计划

### 第二期功能
- 多奖项支持
- 奖品图片上传
- 收货地址收集
- 中奖名单导出
- 数据统计看板

### 第三期功能
- Web管理后台
- 消息推送
- 活动分享海报
- 虚拟奖品发放

## 常见问题

### Q: 如何配置微信小程序?
A: 在 `application-dev.yml` 中配置 `wechat.appid` 和 `wechat.secret`

### Q: Redis连接失败?
A: 检查Redis服务是否启动: `redis-cli ping`

### Q: 数据库连接失败?
A: 检查MySQL配置和权限，确保数据库已创建

### Q: 如何开启SQL日志?
A: 在 `application.yml` 中设置: `logging.level.com.lottery: DEBUG`

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m '[feat] 添加某某功能'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 许可证

本项目仅供学习交流使用

## 联系方式

如有问题，请提交Issue

---

**注意**: 本项目为MVP版本，核心架构和基础功能已完成，部分业务模块需继续开发完善。
