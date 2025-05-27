// 学生选课管理模块
document.addEventListener('DOMContentLoaded', () => {
    // DOM元素
    const studentCourseForm = document.getElementById('studentCourseForm');
    const studentCourseIdInput = document.getElementById('studentCourseId');
    const studentIdInput = document.getElementById('studentId');
    const courseIdInput = document.getElementById('courseId');
    const scoreInput = document.getElementById('score');
    const semesterInput = document.getElementById('semester');
    const saveStudentCourseBtn = document.getElementById('saveStudentCourse');
    const updateStudentCourseBtn = document.getElementById('updateStudentCourse');
    const resetStudentCourseFormBtn = document.getElementById('resetStudentCourseForm');
    const fetchByStudentIdBtn = document.getElementById('fetchByStudentId');
    const queryStudentIdInput = document.getElementById('queryStudentId');
    const fetchByCourseIdBtn = document.getElementById('fetchByCourseId');
    const queryCourseIdInput = document.getElementById('queryCourseId');
    const studentCourseTableBody = document.getElementById('studentCourseTableBody');

    // API端点
    const STUDENT_COURSES_API = `${API_BASE_URL}/student-courses`;

    // 调试信息
    console.log(`使用API端点: ${STUDENT_COURSES_API}`);

    // 测试API连接
    testApiConnection();

    // 初始化页面
    initPage();

    // 阻止表单默认提交行为
    studentCourseForm.addEventListener('submit', (event) => {
        event.preventDefault();
    });

    async function testApiConnection() {
        try {
            const response = await fetch(STUDENT_COURSES_API);
            console.log('API 响应状态:', response.status);
            if (!response.ok) {
                const errorText = await response.text();
                console.error('API 响应错误:', errorText);
                alert(`API 连接测试失败: ${response.status} - 请检查服务器配置`);
            } else {
                console.log('API 连接测试成功');
            }
        } catch (error) {
            console.error('API 连接测试错误:', error);
            alert(`API 连接测试错误: ${error.message}`);
        }
    }

    // 保存选课记录
    saveStudentCourseBtn.addEventListener('click', async (event) => {
        event.preventDefault();

        if (!validateStudentCourseForm()) return;

        const studentCourseData = {
            studentId: parseInt(studentIdInput.value),
            courseId: parseInt(courseIdInput.value),
            score: scoreInput.value ? parseFloat(scoreInput.value) : null,
            semester: semesterInput.value
        };

        console.log('发送选课记录数据:', studentCourseData);
        const result = await postData(STUDENT_COURSES_API, studentCourseData);
        if (result) {
            alert('选课记录创建成功!');
            resetStudentCourseForm();
            await fetchByStudentId();
        }
    });

    // 按学生ID查询选课记录
    fetchByStudentIdBtn.addEventListener('click', async () => {
        const studentId = queryStudentIdInput.value;
        if (!studentId) {
            alert('请输入学生ID');
            return;
        }

        console.log(`查询学生ID为${studentId}的选课记录`);
        const studentCourses = await fetchData(`${STUDENT_COURSES_API}?studentId=${studentId}`);
        if (studentCourses) {
            populateStudentCourseTable(studentCourses);
        }
    });

    // 按课程ID查询选课记录
    fetchByCourseIdBtn.addEventListener('click', async () => {
        const courseId = queryCourseIdInput.value;
        if (!courseId) {
            alert('请输入课程ID');
            return;
        }

        console.log(`查询课程ID为${courseId}的选课记录`);
        const studentCourses = await fetchData(`${STUDENT_COURSES_API}?courseId=${courseId}`);
        if (studentCourses) {
            populateStudentCourseTable(studentCourses);
        }
    });

    // 重置表单
    resetStudentCourseFormBtn.addEventListener('click', resetStudentCourseForm);

    // 初始化页面
    async function initPage() {
        // 默认加载所有选课记录
        const studentCourses = await fetchData(STUDENT_COURSES_API);
        if (studentCourses) {
            populateStudentCourseTable(studentCourses);
        }
    }

    // 验证表单
    function validateStudentCourseForm() {
        if (!studentIdInput.value) {
            alert('请输入学生ID');
            return false;
        }
        if (!courseIdInput.value) {
            alert('请输入课程ID');
            return false;
        }
        if (!semesterInput.value) {
            alert('请输入学期');
            return false;
        }
        return true;
    }

    // 重置选课记录表单
    function resetStudentCourseForm() {
        studentCourseIdInput.value = '';
        studentCourseForm.reset();
        // 初始状态下禁用更新按钮，启用保存按钮
        saveStudentCourseBtn.disabled = false;
        updateStudentCourseBtn.disabled = true;
    }

    // 填充选课记录表格
    function populateStudentCourseTable(studentCourses) {
        studentCourseTableBody.innerHTML = '';

        if (!studentCourses || studentCourses.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = '<td colspan="6" class="text-center">没有找到选课记录</td>';
            studentCourseTableBody.appendChild(row);
            return;
        }

        studentCourses.forEach(studentCourse => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${studentCourse.id || ''}</td>
                <td>${studentCourse.studentId || ''}</td>
                <td>${studentCourse.courseId || ''}</td>
                <td>${studentCourse.score || ''}</td>
                <td>${studentCourse.semester || ''}</td>
                <td>
                    <button class="btn btn-sm btn-warning edit-student-course" data-id="${studentCourse.id}">编辑</button>
                    <button class="btn btn-sm btn-danger delete-student-course" data-id="${studentCourse.id}">删除</button>
                </td>
            `;
            studentCourseTableBody.appendChild(row);
        });

        addTableButtonHandlers();
    }

    // 向表格按钮添加事件处理
    function addTableButtonHandlers() {
        // 编辑按钮事件
        document.querySelectorAll('.edit-student-course').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                if (!id) {
                    alert('无法编辑：选课记录ID无效');
                    return;
                }

                console.log(`编辑ID为${id}的选课记录`);
                const studentCourse = await fetchData(`${STUDENT_COURSES_API}/${id}`);
                if (studentCourse) {
                    studentCourseIdInput.value = studentCourse.id || '';
                    studentIdInput.value = studentCourse.studentId || '';
                    courseIdInput.value = studentCourse.courseId || '';
                    scoreInput.value = studentCourse.score || '';
                    semesterInput.value = studentCourse.semester || '';
                    
                    // 切换到更新模式
                    saveStudentCourseBtn.disabled = true;
                    updateStudentCourseBtn.disabled = false;
                }
            });
        });

        // 删除按钮事件
        document.querySelectorAll('.delete-student-course').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                if (!id) {
                    alert('无法删除：选课记录ID无效');
                    return;
                }

                if (confirm(`确定要删除ID为${id}的选课记录吗?`)) {
                    console.log(`删除ID为${id}的选课记录`);
                    const result = await deleteData(`${STUDENT_COURSES_API}/${id}`);
                    if (result) {
                        alert('选课记录删除成功!');
                        await initPage();
                    }
                }
            });
        });
    }

    // 更新选课记录
    updateStudentCourseBtn.addEventListener('click', async (event) => {
        event.preventDefault();
        console.log('点击更新按钮');

        if (!validateStudentCourseForm()) {
            console.log('表单验证失败');
            return;
        }

        const id = studentCourseIdInput.value;
        if (!id) {
            alert('请先选择要更新的选课记录');
            return;
        }

        const studentCourseData = {
            studentId: parseInt(studentIdInput.value),
            courseId: parseInt(courseIdInput.value),
            score: scoreInput.value ? parseFloat(scoreInput.value) : null,
            semester: semesterInput.value
        };

        console.log(`更新ID为${id}的选课记录:`, studentCourseData);
        try {
            const result = await updateData(`${STUDENT_COURSES_API}/${id}`, studentCourseData);
            if (result) {
                alert('选课记录更新成功!');
                resetStudentCourseForm();
                await initPage();
            }
        } catch (error) {
            console.error('更新选课记录失败:', error);
            alert('更新选课记录失败: ' + error.message);
        }
    });
}); 