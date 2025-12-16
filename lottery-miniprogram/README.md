# 抽奖小程序 - 项目说明

## 项目概述

本项目实现了微信群抽奖功能的完整MVP版本,包括后端服务和微信小程序前端。

## 已完成模块

### ✅ 后端模块
1. **微信授权模块** - 微信登录、JWT token生成
2. **活动管理模块** - 创建活动、查询活动详情
3. **参与模块** - 用户参与抽奖、防重复参与、Redis缓存
4. **抽奖引擎** - Fisher-Yates洗牌算法、即抽即中逻辑、人数/定时开奖
5. **定时调度器** - 定时扫描待开奖活动
6. **中奖管理模块** - 查询中奖名单
7. **单元测试** - 核心业务逻辑测试覆盖

### ✅ 前端模块
1. **首页** - 创建抽奖入口、扫码参与
2. **创建页** - 支持三种开奖模式(定时/人数/即抽即中)
3. **活动详情页** - 参与抽奖、查看状态、分享
4. **中奖名单页** - 展示中奖用户
5. **我的页面** - 用户中心

### ✅ 部署配置
- Docker + Docker Compose 一键部署
- MySQL + Redis 容器化

## 技术栈

### 后端
- Java 17
- Spring Boot 3.0.12
- MyBatis 3.0.3
- MySQL 8.0
- Redis 6.0+
- JWT 认证

### 前端
- 微信小程序原生框架
- JavaScript

## 快速开始

### 1. 后端启动

#### 方式一:Docker部署(推荐)
```bash
cd /data/workspace/LOTTERY
docker-compose up -d
```

#### 方式二:本地开发
```bash
# 1. 启动MySQL和Redis
docker-compose up -d mysql redis

# 2. 导入数据库
mysql -h127.0.0.1 -uroot -proot123456 lottery < lottery-backend/src/main/resources/sql/schema.sql

# 3. 启动后端
cd lottery-backend
./mvnw spring-boot:run
```

### 2. 前端启动

1. 使用微信开发者工具打开 `lottery-miniprogram` 目录
2. 修改 `app.js` 中的 `apiUrl` 为后端服务地址
3. 修改 `project.config.json` 中的 `appid`
4. 点击编译运行

## 核心功能

### 1. 三种开奖模式

#### 定时开奖
- 设置具体的开奖时间
- 系统定时扫描并自动开奖
- 使用Fisher-Yates算法随机抽取

#### 人数开奖
- 设置目标参与人数
- 达到人数后自动触发开奖
- Redis计数器实时统计

#### 即抽即中
- 参与即开奖
- 基于剩余奖品数动态计算中奖概率
- Redis库存控制防止超发

### 2. 防刷机制
- Redis缓存防重复参与
- 数据库唯一索引兜底
- 分布式锁防并发

### 3. 性能优化
- Redis缓存活动数据
- 批量插入中奖记录
- 异步触发开奖

## API接口文档

### 认证接口
- `POST /api/auth/login` - 微信登录

### 活动接口
- `POST /api/activity/create` - 创建活动
- `GET /api/activity/detail` - 查询活动详情

### 参与接口
- `POST /api/participant/join` - 参与抽奖
- `GET /api/participant/status` - 查询参与状态

### 中奖接口
- `GET /api/winner/list` - 查询中奖名单

## 数据库表结构

### lottery_activity - 活动表
- 活动基本信息
- 开奖模式配置
- 活动状态

### lottery_participant - 参与记录表
- 参与用户信息
- 参与时间
- 中奖状态

### lottery_winner - 中奖记录表
- 中奖用户信息
- 中奖时间

## 测试

```bash
cd lottery-backend
./mvnw test
```

主要测试类:
- `LotteryServiceTest` - 抽奖引擎测试
- `ParticipantServiceTest` - 参与服务测试
- `ActivityServiceTest` - 活动服务测试

## 配置说明

### 后端配置
- `application-dev.yml` - 开发环境配置
- `application-prod.yml` - 生产环境配置(使用环境变量)

### 小程序配置
- 需要配置微信小程序AppID和AppSecret
- 修改后端服务地址

## 注意事项

1. **微信开发者平台配置**
   - 需要在微信公众平台申请小程序
   - 配置服务器域名白名单
   - 获取AppID和AppSecret

2. **生产环境部署**
   - 修改数据库密码
   - 配置JWT密钥
   - 使用HTTPS协议

3. **性能建议**
   - Redis设置合理的过期时间
   - 定期清理过期数据
   - 数据库索引优化

## 后续优化方向

1. 增加活动管理功能(取消、编辑)
2. 支持多奖品等级
3. 添加抽奖动画效果
4. 实现消息通知功能
5. 数据统计与分析

## 技术亮点

1. **Fisher-Yates洗牌算法** - O(n)时间复杂度,保证公平性
2. **Redis分布式锁** - 防止并发开奖
3. **即抽即中动态概率** - 根据库存实时计算
4. **双重防刷机制** - Redis+数据库唯一索引
5. **事务一致性** - @Transactional保证数据完整性

## 联系方式

如有问题,请提交Issue或联系开发团队。

## 许可证

MIT License
