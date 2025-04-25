-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS mybatis_db;

-- 使用数据库
USE mybatis_db;

-- 删除表（如果存在）以避免冲突
DROP TABLE IF EXISTS user;

-- 创建user表
CREATE TABLE user (
                      id   INT AUTO_INCREMENT PRIMARY KEY,
                      name VARCHAR(255) NULL,
                      age  INT NULL
);

-- 插入初始数据
INSERT INTO user (id, name, age) VALUES (3, '新名字1', 25);
INSERT INTO user (id, name, age) VALUES (4, 'huashen1', 21);
INSERT INTO user (id, name, age) VALUES (7, 'test', 20);
INSERT INTO user (id, name, age) VALUES (9, 'hhh', 66);

-- 重置自增ID值（可选，确保下一个自动生成的ID正确）
ALTER TABLE user AUTO_INCREMENT = 10;
