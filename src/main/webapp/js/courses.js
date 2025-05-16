// 课程管理模块
document.addEventListener('DOMContentLoaded', () => {
    // DOM元素
    const courseForm = document.getElementById('courseForm');
    const courseIdInput = document.getElementById('courseId');
    const courseNameInput = document.getElementById('courseName');
    const creditInput = document.getElementById('credit');
    const teacherIdInput = document.getElementById('teacherId');
    const saveCourseBtn = document.getElementById('saveCourse');
    const updateCourseBtn = document.getElementById('updateCourse');
    const resetCourseFormBtn = document.getElementById('resetCourseForm');
    const fetchCourseByIdBtn = document.getElementById('fetchCourseById');
    const queryCourseIdInput = document.getElementById('queryCourseId');
    const fetchCoursesByTeacherBtn = document.getElementById('fetchCoursesByTeacher');
    const queryTeacherIdInput = document.getElementById('queryTeacherId');
    const courseTableBody = document.getElementById('courseTableBody');

    // API端点
    const COURSES_API = `${API_BASE_URL}/courses`;

    // 调试信息
    console.log(`使用API端点: ${COURSES_API}`);

    // 测试API连接
    testApiConnection();

    // 初始化页面
    initPage();

    // 阻止表单默认提交行为
    courseForm.addEventListener('submit', (event) => {
        event.preventDefault();
    });

    // 初始化页面时禁用更新按钮
    updateCourseBtn.disabled = true;

    async function testApiConnection() {
        try {
            const response = await fetch(COURSES_API);
            console.log('API 响应状态:', response.status);
            if (!response.ok) {
                const errorText = await response.text();
                console.error('API 响应错误:', errorText);
                showError(`API 连接测试失败: ${response.status} - 请检查服务器配置`);
            } else {
                console.log('API 连接测试成功');
            }
        } catch (error) {
            console.error('API 连接测试错误:', error);
            showError(`API 连接测试错误: ${error.message}`);
        }
    }

    // 保存课程
    saveCourseBtn.addEventListener('click', async (event) => {
        event.preventDefault();

        if (!validateCourseForm()) return;

        const courseData = {
            courseName: courseNameInput.value.trim(),
            credit: parseInt(creditInput.value),
            teacherId: teacherIdInput.value ? parseInt(teacherIdInput.value) : null
        };

        try {
            console.log('发送课程数据:', courseData);
            const response = await fetch(COURSES_API, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(courseData)
            });

            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(errorData || '创建课程失败');
            }

            const result = await response.json();
            showSuccess('课程创建成功!');
            resetCourseForm();
            await loadAllCourses();
        } catch (error) {
            showError(`创建课程失败: ${error.message}`);
        }
    });

    // 按ID查询课程
    fetchCourseByIdBtn.addEventListener('click', async () => {
        const id = queryCourseIdInput.value;
        if (!id) {
            showError('请输入课程ID');
            return;
        }

        try {
            console.log(`查询ID为${id}的课程`);
            const response = await fetch(`${COURSES_API}/${id}`);
            
            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(errorData || '查询课程失败');
            }

            const course = await response.json();
            populateCourseTable([course]);
        } catch (error) {
            showError(`查询课程失败: ${error.message}`);
        }
    });

    // 按教师查询课程
    fetchCoursesByTeacherBtn.addEventListener('click', async () => {
        const teacherId = queryTeacherIdInput.value;
        if (!teacherId) {
            showError('请输入教师ID');
            return;
        }

        try {
            console.log(`查询教师ID为${teacherId}的课程`);
            const response = await fetch(`${COURSES_API}?teacherId=${teacherId}`);
            
            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(errorData || '查询课程失败');
            }

            const courses = await response.json();
            populateCourseTable(courses);
        } catch (error) {
            showError(`查询课程失败: ${error.message}`);
        }
    });

    // 重置表单
    resetCourseFormBtn.addEventListener('click', resetCourseForm);

    // 初始化页面
    async function initPage() {
        await loadAllCourses();
    }

    // 加载所有课程
    async function loadAllCourses() {
        try {
            console.log('加载所有课程');
            const response = await fetch(COURSES_API);
            
            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(errorData || '加载课程失败');
            }

            const courses = await response.json();
            console.log('获取到的课程数据:', courses); // 添加调试日志
            populateCourseTable(courses);
        } catch (error) {
            showError(`加载课程失败: ${error.message}`);
        }
    }

    // 验证表单
    function validateCourseForm() {
        const courseName = courseNameInput.value.trim();
        const credit = parseInt(creditInput.value);

        if (!courseName) {
            showError('请输入课程名称');
            courseNameInput.focus();
            return false;
        }

        if (isNaN(credit) || credit < 1 || credit > 10) {
            showError('课程学分必须在1-10之间');
            creditInput.focus();
            return false;
        }

        return true;
    }

    // 重置课程表单
    function resetCourseForm() {
        courseForm.reset();
        courseIdInput.value = '';
        courseIdInput.disabled = false;
        saveCourseBtn.disabled = false;
        updateCourseBtn.disabled = true;
    }

    // 填充课程表格
    function populateCourseTable(courses) {
        courseTableBody.innerHTML = '';

        if (!courses || courses.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = '<td colspan="5" class="text-center">没有找到课程</td>';
            courseTableBody.appendChild(row);
            return;
        }

        console.log('准备渲染的课程数据:', courses);

        courses.forEach(course => {
            console.log('处理课程数据:', course);
            
            if (!course) {
                console.error('无效的课程数据:', course);
                return;
            }

            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${course.courseId || ''}</td>
                <td>${course.courseName ? escapeHtml(course.courseName) : ''}</td>
                <td>${course.credit || ''}</td>
                <td>${course.teacherId || ''}</td>
                <td>
                    <button class="btn btn-sm btn-warning edit-course" data-id="${course.courseId}">编辑</button>
                    <button class="btn btn-sm btn-danger delete-course" data-id="${course.courseId}">删除</button>
                </td>
            `;
            courseTableBody.appendChild(row);
        });

        addTableButtonHandlers();
    }

    // 向表格按钮添加事件处理
    function addTableButtonHandlers() {
        // 编辑按钮事件
        document.querySelectorAll('.edit-course').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                if (!id) {
                    showError('无效的课程ID');
                    return;
                }

                try {
                    console.log(`编辑ID为${id}的课程`);
                    const response = await fetch(`${COURSES_API}/${id}`);
                    
                    if (!response.ok) {
                        const errorData = await response.text();
                        throw new Error(errorData || '获取课程信息失败');
                    }

                    const course = await response.json();
                    console.log('获取到的课程详情:', course);

                    if (!course) {
                        throw new Error('获取到的课程数据无效');
                    }

                    // 填充表单
                    courseIdInput.value = course.courseId || '';
                    courseNameInput.value = course.courseName || '';
                    creditInput.value = course.credit || '';
                    teacherIdInput.value = course.teacherId || '';

                    courseIdInput.disabled = true;
                    saveCourseBtn.disabled = true;
                    updateCourseBtn.disabled = false;
                } catch (error) {
                    showError(`获取课程信息失败: ${error.message}`);
                }
            });
        });

        // 删除按钮事件
        document.querySelectorAll('.delete-course').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                if (!id) {
                    showError('无效的课程ID');
                    return;
                }

                if (confirm(`确定要删除ID为${id}的课程吗?`)) {
                    try {
                        console.log(`删除ID为${id}的课程`);
                        const response = await fetch(`${COURSES_API}/${id}`, {
                            method: 'DELETE'
                        });

                        if (!response.ok) {
                            const errorData = await response.text();
                            throw new Error(errorData || '删除课程失败');
                        }

                        showSuccess('课程删除成功!');
                        await loadAllCourses();
                    } catch (error) {
                        showError(`删除课程失败: ${error.message}`);
                    }
                }
            });
        });
    }

    // 更新课程
    updateCourseBtn.addEventListener('click', async (event) => {
        event.preventDefault();

        if (!validateCourseForm()) return;

        const courseId = courseIdInput.value;
        if (!courseId) {
            showError('请先选择要更新的课程');
            return;
        }

        const courseData = {
            courseName: courseNameInput.value.trim(),
            credit: parseInt(creditInput.value),
            teacherId: teacherIdInput.value ? parseInt(teacherIdInput.value) : null
        };

        try {
            console.log(`更新ID为${courseId}的课程数据:`, courseData);
            const response = await fetch(`${COURSES_API}/${courseId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(courseData)
            });

            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(errorData || '更新课程失败');
            }

            showSuccess('课程更新成功!');
            resetCourseForm();
            await loadAllCourses();
        } catch (error) {
            showError(`更新课程失败: ${error.message}`);
        }
    });

    // 工具函数：显示错误消息
    function showError(message) {
        alert(message);
    }

    // 工具函数：显示成功消息
    function showSuccess(message) {
        alert(message);
    }

    // 工具函数：HTML转义
    function escapeHtml(unsafe) {
        if (!unsafe) return '';
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
}); 