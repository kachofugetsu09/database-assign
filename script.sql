CREATE DATABASE IF NOT EXISTS mybatis_db;
-- 使用数据库
USE mybatis_db;

-- 删除现有表（按依赖顺序）
DROP TABLE IF EXISTS student_course;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS teacher;

-- 创建教师表（作为基础表）
CREATE TABLE teacher (
                         teacher_id INT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         gender CHAR(1),
                         title VARCHAR(50)
);

-- 创建课程表（关联教师）
CREATE TABLE course (
                        course_id INT AUTO_INCREMENT PRIMARY KEY,
                        course_name VARCHAR(100) NOT NULL,
                        credit INT,
                        teacher_id INT,
                        FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id) ON DELETE SET NULL
);

-- 创建学生表
CREATE TABLE student (
                         student_id INT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         gender CHAR(1),
                         age INT,
                         enrollment_date DATE
);

-- 创建学生-课程关联表（多对多关系，关联学生和课程）
CREATE TABLE student_course (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                student_id INT,
                                course_id INT,
                                score DECIMAL(5,2),
                                semester VARCHAR(20),
                                FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE,
                                FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE,
                                UNIQUE KEY (student_id, course_id, semester)
);

-- 插入初始数据
-- 教师数据
INSERT INTO teacher (name, gender, title) VALUES
                                              ('张教授', 'M', '教授'),
                                              ('李副教授', 'F', '副教授'),
                                              ('王讲师', 'M', '讲师');

-- 课程数据
INSERT INTO course (course_name, credit, teacher_id) VALUES
                                                         ('数据库原理', 4, 1),
                                                         ('Java程序设计', 3, 2),
                                                         ('电路分析', 3, 3);

-- 学生数据
INSERT INTO student (name, gender, age, enrollment_date) VALUES
                                                             ('张三', 'M', 20, '2021-09-01'),
                                                             ('李四', 'F', 19, '2021-09-01'),
                                                             ('王五', 'M', 21, '2021-09-01'),
                                                             ('赵六', 'F', 20, '2021-09-01');

-- 选课数据
INSERT INTO student_course (student_id, course_id, score, semester) VALUES
                                                                        (1, 1, 85.5, '2022-2023-1'),
                                                                        (1, 2, 90.0, '2022-2023-1'),
                                                                        (2, 1, 78.0, '2022-2023-1'),
                                                                        (3, 2, 92.5, '2022-2023-1'),
                                                                        (4, 3, 88.0, '2022-2023-1');

ALTER TABLE teacher MODIFY gender VARCHAR(10);
ALTER TABLE student MODIFY gender VARCHAR(10);

UPDATE teacher SET gender = '男' WHERE gender = 'M';
UPDATE teacher SET gender = '女' WHERE gender = 'F';

UPDATE student SET gender = '男' WHERE gender = 'M';
UPDATE student SET gender = '女' WHERE gender = 'F';
