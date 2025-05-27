# 教务管理系统启动指南

## 环境要求

- JDK 8+
- MySQL 8.0+
- Maven 3.6+

## 启动步骤

### 1. 数据库初始化

1. 登录MySQL数据库
2. 执行项目根目录下的`script.sql`脚本以创建数据库和初始数据：
```bash
mysql -u your_username -p < script.sql
```

### 2. 配置数据库连接

修改 `src/main/java/utils/strategy/AbstractSqlStrategy.java` 文件中的数据库连接信息：

```java
protected static String jdbcUrl = "jdbc:mysql://localhost:3306/mybatis_db";
protected static String username = "your_username";  // 修改为你的MySQL用户名
protected static String password = "your_password";  // 修改为你的MySQL密码
```

### 3. 编译项目

在项目根目录下执行：

```bash
mvn clean compile
```

### 4. 启动应用

1. 运行 `src/main/java/Application.java` 中的 main 方法
2. 或在项目根目录下执行：
```bash
mvn exec:java -Dexec.mainClass="Application"
```

应用将在 http://localhost:8080 启动

### 5. 访问系统

启动成功后，可以通过以下URL访问系统：

- 首页：http://localhost:8080/index.html
- 学生管理：http://localhost:8080/students.html
- 教师管理：http://localhost:8080/teachers.html
- 课程管理：http://localhost:8080/courses.html
- 选课管理：http://localhost:8080/student-courses.html

## 初始数据说明

系统初始化后包含以下测试数据：

### 教师数据
- 张教授（教授）
- 李副教授（副教授）
- 王讲师（讲师）

### 课程数据
- 数据库原理（4学分）
- Java程序设计（3学分）
- 电路分析（3学分）

### 学生数据
- 张三（男，20岁）
- 李四（女，19岁）
- 王五（男，21岁）
- 赵六（女，20岁）

## 常见问题

1. 如果遇到端口占用问题，可以修改 `Application.java` 中的 `PORT` 常量更改端口号
2. 如果遇到数据库连接问题，请检查：
   - MySQL服务是否启动
   - 数据库用户名密码是否正确
   - 数据库是否成功创建 