-- 微信群抽奖系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS lottery DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lottery;

-- ========================================
-- 活动表 (lottery_activity)
-- ========================================
DROP TABLE IF EXISTS `lottery_activity`;

CREATE TABLE `lottery_activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `activity_code` VARCHAR(32) NOT NULL COMMENT '活动唯一编码',
  `creator_openid` VARCHAR(64) NOT NULL COMMENT '创建者OpenID',
  `title` VARCHAR(100) NOT NULL COMMENT '活动标题',
  `description` TEXT COMMENT '抽奖说明',
  `draw_mode` TINYINT NOT NULL COMMENT '开奖模式：1-定时，2-人数，3-即抽即中',
  `draw_time` DATETIME DEFAULT NULL COMMENT '定时开奖时间',
  `target_participant_count` INT DEFAULT NULL COMMENT '目标参与人数',
  `prize_count` INT NOT NULL COMMENT '奖品份数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '活动状态：1-进行中，2-已开奖，3-已取消',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_code` (`activity_code`),
  KEY `idx_creator_openid` (`creator_openid`),
  KEY `idx_status` (`status`),
  KEY `idx_draw_time` (`draw_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抽奖活动表';

-- ========================================
-- 参与记录表 (lottery_participant)
-- ========================================
DROP TABLE IF EXISTS `lottery_participant`;

CREATE TABLE `lottery_participant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '用户OpenID',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '用户头像',
  `participate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参与时间',
  `is_winner` TINYINT NOT NULL DEFAULT 0 COMMENT '是否中奖：0-未中奖，1-已中奖',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_openid` (`activity_id`, `openid`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抽奖参与记录表';

-- ========================================
-- 中奖记录表 (lottery_winner)
-- ========================================
DROP TABLE IF EXISTS `lottery_winner`;

CREATE TABLE `lottery_winner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `participant_id` BIGINT NOT NULL COMMENT '参与记录ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '中奖者OpenID',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '中奖者昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '中奖者头像',
  `win_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '中奖时间',
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_participant_id` (`participant_id`),
  KEY `idx_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='中奖记录表';

-- ========================================
-- 初始化测试数据（开发环境使用）
-- ========================================

-- 插入测试活动（定时开奖）
INSERT INTO `lottery_activity` 
(`activity_code`, `creator_openid`, `title`, `description`, `draw_mode`, `draw_time`, `target_participant_count`, `prize_count`, `status`) 
VALUES 
('TEST001', 'test_openid_001', '新年大抽奖', '参与即有机会获得精美礼品', 1, DATE_ADD(NOW(), INTERVAL 2 MINUTE), NULL, 5, 1);

-- 插入测试活动（人数开奖）
INSERT INTO `lottery_activity` 
(`activity_code`, `creator_openid`, `title`, `description`, `draw_mode`, `draw_time`, `target_participant_count`, `prize_count`, `status`) 
VALUES 
('TEST002', 'test_openid_001', '限时抢红包', '满10人自动开奖', 2, NULL, 10, 3, 1);

-- 插入测试活动（即抽即中）
INSERT INTO `lottery_activity` 
(`activity_code`, `creator_openid`, `title`, `description`, `draw_mode`, `draw_time`, `target_participant_count`, `prize_count`, `status`) 
VALUES 
('TEST003', 'test_openid_001', '幸运大转盘', '参与即可知道结果', 3, NULL, NULL, 10, 1);

-- ========================================
-- 数据库性能优化建议
-- ========================================

-- 1. 定期清理已结束的活动数据（保留最近3个月）
-- DELETE FROM lottery_activity WHERE status = 2 AND create_time < DATE_SUB(NOW(), INTERVAL 3 MONTH);

-- 2. 定期分析表以优化查询性能
-- ANALYZE TABLE lottery_activity;
-- ANALYZE TABLE lottery_participant;
-- ANALYZE TABLE lottery_winner;

-- 3. 监控慢查询
-- SET GLOBAL slow_query_log = 'ON';
-- SET GLOBAL long_query_time = 1;

-- ========================================
-- 数据字典说明
-- ========================================

/*
开奖模式 (draw_mode):
  1 - 定时开奖：到达设定时间自动开奖
  2 - 人数开奖：参与人数达到目标值自动开奖
  3 - 即抽即中：用户参与时立即返回中奖结果

活动状态 (status):
  1 - 进行中：活动正在进行，用户可以参与
  2 - 已开奖：活动已开奖，不可再参与
  3 - 已取消：活动已取消（MVP版本暂不支持）

是否中奖 (is_winner):
  0 - 未中奖：用户参与但未中奖
  1 - 已中奖：用户已中奖
*/
