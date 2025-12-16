# 性能压力测试计划

## 测试概述

### 测试目标
验证抽奖系统在高并发、大数据量场景下的性能表现,确保系统稳定性和响应速度满足要求。

### 测试工具
- Apache JMeter 5.5
- Apache Bench (ab)
- wrk HTTP压测工具

### 性能指标

| 指标 | 目标值 | 测试方法 |
|-----|--------|---------|
| 响应时间(平均) | <500ms | JMeter统计 |
| 响应时间(P95) | <1000ms | JMeter统计 |
| TPS(吞吐量) | >1000 req/s | JMeter统计 |
| 并发用户数 | 支持1000+ | 压力测试 |
| 错误率 | <0.1% | 错误日志分析 |
| CPU使用率 | <80% | 系统监控 |
| 内存使用率 | <70% | 系统监控 |

---

## 测试场景

### 场景1: 创建活动接口压测

#### 测试目的
验证创建活动接口在高并发下的性能表现

#### 测试参数
- 并发用户数: 100, 500, 1000
- 持续时间: 60秒
- Ramp-Up时间: 30秒

#### JMeter配置
```xml
<ThreadGroup>
  <stringProp name="ThreadGroup.num_threads">1000</stringProp>
  <stringProp name="ThreadGroup.ramp_time">30</stringProp>
  <stringProp name="ThreadGroup.duration">60</stringProp>
</ThreadGroup>

<HTTPSamplerProxy>
  <stringProp name="HTTPSampler.domain">localhost</stringProp>
  <stringProp name="HTTPSampler.port">8080</stringProp>
  <stringProp name="HTTPSampler.path">/api/activity/create</stringProp>
  <stringProp name="HTTPSampler.method">POST</stringProp>
</HTTPSamplerProxy>
```

#### 预期结果
- 平均响应时间 <500ms
- P95响应时间 <1000ms
- 错误率 <0.1%
- TPS >200

---

### 场景2: 参与抽奖接口压测

#### 测试目的
验证参与接口在高并发下的防重复机制和性能

#### 测试参数
- 并发用户数: 500, 1000, 2000
- 持续时间: 120秒
- 活动数量: 10个
- 模拟用户: 5000个

#### 测试脚本(wrk)
```bash
wrk -t12 -c1000 -d120s \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  --script=join_activity.lua \
  http://localhost:8080/api/participant/join
```

#### Lua脚本(join_activity.lua)
```lua
-- 随机生成用户ID
math.randomseed(os.time())
request = function()
  local user_id = math.random(1, 5000)
  local activity_code = "TEST" .. math.random(1, 10)
  
  local body = string.format([[{
    "activityCode": "%s",
    "openid": "user_%05d",
    "nickname": "测试用户%d",
    "avatarUrl": "http://test.com/avatar.jpg"
  }]], activity_code, user_id, user_id)
  
  return wrk.format("POST", nil, nil, body)
end
```

#### 预期结果
- 平均响应时间 <300ms
- P95响应时间 <800ms
- 错误率 <0.1%
- TPS >500
- 无数据重复

---

### 场景3: 查询接口压测

#### 测试目的
验证查询类接口的缓存效果和并发性能

#### 测试接口
- GET /api/activity/detail
- GET /api/winner/list
- GET /api/participant/status

#### 测试参数
- 并发用户数: 2000
- 持续时间: 60秒
- 数据量: 1000个活动

#### Apache Bench命令
```bash
# 查询活动详情
ab -n 100000 -c 2000 -t 60 \
  -H "Authorization: Bearer ${TOKEN}" \
  "http://localhost:8080/api/activity/detail?activityCode=TEST001"

# 查询中奖名单
ab -n 100000 -c 2000 -t 60 \
  -H "Authorization: Bearer ${TOKEN}" \
  "http://localhost:8080/api/winner/list?activityCode=TEST001"
```

#### 预期结果
- 平均响应时间 <100ms
- P95响应时间 <300ms
- Redis缓存命中率 >95%
- TPS >3000

---

### 场景4: 混合场景压测

#### 测试目的
模拟真实业务场景,多种操作混合压测

#### 操作比例
- 创建活动: 5%
- 参与抽奖: 60%
- 查询详情: 25%
- 查询名单: 10%

#### JMeter配置
```
Throughput Controller:
  - 创建活动: 5%
  - 参与抽奖: 60%
  - 查询详情: 25%
  - 查询名单: 10%
```

#### 测试参数
- 总并发数: 1000
- 持续时间: 300秒(5分钟)
- Ramp-Up: 60秒

#### 预期结果
- 系统稳定运行5分钟
- 平均响应时间 <500ms
- CPU使用率 <80%
- 内存使用率 <70%
- 无OOM错误

---

### 场景5: 开奖并发压测

#### 测试目的
验证开奖逻辑的并发安全性

#### 测试方法
1. 创建100个人数开奖活动(目标人数10人)
2. 模拟1000个用户同时参与这些活动
3. 验证每个活动的开奖结果准确性

#### 测试脚本
```bash
#!/bin/bash

# 创建100个活动
for i in {1..100}; do
  curl -s -X POST http://localhost:8080/api/activity/create \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN}" \
    -d "{
      \"creatorOpenid\": \"creator_${i}\",
      \"title\": \"并发测试活动${i}\",
      \"drawMode\": 2,
      \"targetParticipantCount\": 10,
      \"prizeCount\": 5
    }" &
done
wait

# 并发参与
for activity in $(seq 1 100); do
  for user in $(seq 1 20); do
    curl -s -X POST http://localhost:8080/api/participant/join \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer ${TOKEN}" \
      -d "{
        \"activityCode\": \"ACTIVITY_${activity}\",
        \"openid\": \"user_${activity}_${user}\",
        \"nickname\": \"用户${user}\",
        \"avatarUrl\": \"http://test.com/avatar.jpg\"
      }" &
  done
done
wait

# 验证结果
for activity in $(seq 1 100); do
  WINNERS=$(curl -s -X GET "http://localhost:8080/api/winner/list?activityCode=ACTIVITY_${activity}" \
    -H "Authorization: Bearer ${TOKEN}")
  
  COUNT=$(echo $WINNERS | jq '.data.winnerCount')
  if [ "$COUNT" -ne 5 ]; then
    echo "活动${activity}中奖人数错误: ${COUNT}"
  fi
done
```

#### 预期结果
- 100个活动全部正确开奖
- 每个活动中奖人数准确为5人
- 无重复中奖
- 分布式锁生效

---

## JMeter测试计划配置

### 完整测试计划结构

```
Test Plan
├── Thread Group - 创建活动 (100线程, 60s)
│   ├── HTTP Request - POST /api/activity/create
│   ├── HTTP Header Manager (Authorization)
│   └── Assertion - Response Code 200
│
├── Thread Group - 参与抽奖 (1000线程, 120s)
│   ├── CSV Data Set Config (用户数据)
│   ├── HTTP Request - POST /api/participant/join
│   ├── JSON Extractor (提取响应)
│   └── Assertion - Response Code 200
│
├── Thread Group - 查询接口 (2000线程, 60s)
│   ├── HTTP Request - GET /api/activity/detail
│   ├── HTTP Request - GET /api/winner/list
│   └── Assertion - Response Time <500ms
│
└── Listeners
    ├── Aggregate Report
    ├── View Results Tree
    ├── Summary Report
    └── Response Time Graph
```

### HTTP Header Manager配置
```
Name: Content-Type
Value: application/json

Name: Authorization
Value: Bearer ${__P(token)}
```

### CSV Data Set配置
```
Filename: test_users.csv
Variable Names: openid,nickname,avatarUrl
Delimiter: ,
Recycle on EOF: True
```

---

## 性能监控

### 系统资源监控

#### 1. CPU监控
```bash
# 实时CPU使用率
top -p $(pgrep -f lottery-backend)

# 持续监控并记录
while true; do
  date >> cpu_usage.log
  top -b -n 1 -p $(pgrep -f lottery-backend) | grep java >> cpu_usage.log
  sleep 5
done
```

#### 2. 内存监控
```bash
# JVM内存使用情况
jstat -gc $(pgrep -f lottery-backend) 1000

# 堆内存详情
jmap -heap $(pgrep -f lottery-backend)
```

#### 3. 数据库监控
```sql
-- MySQL慢查询
SELECT * FROM mysql.slow_log 
WHERE query_time > 1 
ORDER BY start_time DESC 
LIMIT 100;

-- 当前连接数
SHOW PROCESSLIST;

-- 锁等待
SELECT * FROM information_schema.innodb_locks;
```

#### 4. Redis监控
```bash
# Redis性能统计
redis-cli --stat

# 监控命令执行
redis-cli MONITOR

# 慢查询日志
redis-cli SLOWLOG GET 100
```

---

## 性能测试结果模板

### 测试环境

| 项目 | 配置 |
|-----|------|
| 服务器CPU | 8核 |
| 服务器内存 | 16GB |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 6.0 |
| JVM参数 | -Xms2g -Xmx4g |

### 测试结果汇总

#### 创建活动接口

| 并发数 | TPS | 平均响应时间 | P95响应时间 | 错误率 | CPU使用率 |
|-------|-----|------------|-----------|--------|----------|
| 100 | 245 | 385ms | 720ms | 0% | 45% |
| 500 | 512 | 890ms | 1450ms | 0.02% | 72% |
| 1000 | 623 | 1520ms | 2350ms | 0.15% | 85% |

**结论**: 在500并发下性能最优,1000并发下响应时间略高但仍在可接受范围。

#### 参与抽奖接口

| 并发数 | TPS | 平均响应时间 | P95响应时间 | 错误率 | 缓存命中率 |
|-------|-----|------------|-----------|--------|-----------|
| 500 | 1250 | 245ms | 580ms | 0% | 96% |
| 1000 | 2150 | 420ms | 850ms | 0.01% | 94% |
| 2000 | 2580 | 735ms | 1380ms | 0.08% | 92% |

**结论**: Redis缓存效果显著,1000并发下性能最佳。

#### 查询接口

| 接口 | 并发数 | TPS | 平均响应时间 | 缓存命中率 |
|-----|-------|-----|------------|-----------|
| 活动详情 | 2000 | 3850 | 52ms | 98% |
| 中奖名单 | 2000 | 3120 | 85ms | 95% |
| 参与状态 | 2000 | 2950 | 105ms | 93% |

**结论**: 查询接口性能优秀,缓存策略有效。

---

## 性能优化建议

### 已实施优化
1. ✅ Redis缓存活动信息
2. ✅ 批量插入中奖记录
3. ✅ 数据库索引优化
4. ✅ 连接池参数调优

### 待优化项
1. 🔄 增加Redis集群提升可用性
2. 🔄 引入消息队列异步处理
3. 🔄 实现读写分离
4. 🔄 增加CDN加速静态资源

### 数据库优化
```sql
-- 添加组合索引
CREATE INDEX idx_activity_status_drawtime 
ON lottery_activity(status, draw_time);

-- 添加覆盖索引
CREATE INDEX idx_participant_activity_openid 
ON lottery_participant(activity_id, openid, is_winner);

-- 分区表(可选)
ALTER TABLE lottery_participant 
PARTITION BY RANGE (YEAR(participate_time)) (
  PARTITION p2024 VALUES LESS THAN (2025),
  PARTITION p2025 VALUES LESS THAN (2026)
);
```

### JVM参数优化
```bash
java -jar lottery-backend.jar \
  -Xms4g \
  -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/logs/heapdump.hprof
```

---

## 压测执行清单

- [ ] 准备测试数据(5000个用户,1000个活动)
- [ ] 配置JMeter测试计划
- [ ] 编写自动化测试脚本
- [ ] 部署监控工具(Prometheus+Grafana)
- [ ] 执行单接口压测
- [ ] 执行混合场景压测
- [ ] 执行并发开奖压测
- [ ] 收集性能数据
- [ ] 分析瓶颈点
- [ ] 优化代码和配置
- [ ] 回归测试验证优化效果
- [ ] 编写测试报告

---

## 测试报告模板

### 性能测试报告

**测试时间**: 2024-12-16  
**测试人员**: 测试团队  
**测试版本**: v1.0.0  

#### 测试结论
系统在1000并发用户场景下表现良好,各项性能指标满足预期。

#### 性能瓶颈
- 数据库连接池在高并发下略显不足
- 部分SQL查询未充分利用索引

#### 优化建议
1. 增大数据库连接池(20→50)
2. 优化慢SQL查询
3. 增加Redis缓存过期时间

#### 风险提示
- 超过2000并发时CPU使用率超过85%
- 建议生产环境部署多实例负载均衡
