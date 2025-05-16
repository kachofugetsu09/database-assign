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
            const response = await fetchData(COURSES_API);
            if (response === null) {
                showError('API 连接测试失败 - 请检查服务器配置');
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

        const courseId = courseIdInput.value;
        if (!courseId) {
            showError('请输入课程ID');
            courseIdInput.focus();
            return;
        }

        const courseData = {
            courseId: parseInt(courseId),
            courseName: courseNameInput.value.trim(),
            credit: parseInt(creditInput.value),
            teacherId: teacherIdInput.value ? parseInt(teacherIdInput.value) : null
        };

        showLoading();
        try {
            console.log('发送课程数据:', courseData);
            const result = await postData(COURSES_API, courseData);
            console.log('服务器返回的课程数据:', result);
            
            if (result) {
                showSuccess('课程创建成功!');
                // 立即刷新课程列表
                await loadAllCourses();
                resetCourseForm();
            } else {
                throw new Error('创建失败：服务器未返回有效数据');
            }
        } catch (error) {
            console.error('创建课程错误:', error);
            showError(`创建课程失败: ${error.message}`);
        } finally {
            hideLoading();
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
            const course = await fetchData(`${COURSES_API}/${id}`);
            if (course) {
                populateCourseTable([course]);
            }
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
            const courses = await fetchData(`${COURSES_API}?teacherId=${teacherId}`);
            if (courses) {
                populateCourseTable(courses);
            }
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
        showLoading();
        try {
            console.log('加载所有课程');
            const courses = await fetchData(COURSES_API);
            if (courses) {
                console.log('获取到的课程数据:', courses);
                populateCourseTable(courses);
            }
        } catch (error) {
            showError(`加载课程失败: ${error.message}`);
        } finally {
            hideLoading();
        }
    }

    // 验证表单
    function validateCourseForm() {
        const courseId = courseIdInput.value ? parseInt(courseIdInput.value) : null;
        const courseName = courseNameInput.value.trim();
        const credit = parseInt(creditInput.value);
        const teacherId = teacherIdInput.value ? parseInt(teacherIdInput.value) : null;

        if (!courseId || isNaN(courseId) || courseId < 1) {
            showError('请输入有效的课程ID（必须大于0）');
            courseIdInput.focus();
            return false;
        }

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

        if (teacherId !== null && (isNaN(teacherId) || teacherId < 1)) {
            showError('教师ID必须大于0');
            teacherIdInput.focus();
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
                    const course = await fetchData(`${COURSES_API}/${id}`);
                    if (course) {
                        console.log('获取到的课程详情:', course);

                        // 填充表单
                        courseIdInput.value = course.courseId || '';
                        courseNameInput.value = course.courseName || '';
                        creditInput.value = course.credit || '';
                        teacherIdInput.value = course.teacherId || '';

                        // 编辑模式下禁用ID输入
                        courseIdInput.disabled = true;
                        saveCourseBtn.disabled = true;
                        updateCourseBtn.disabled = false;
                    }
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
                    showLoading();
                    try {
                        console.log(`删除ID为${id}的课程`);
                        const result = await deleteData(`${COURSES_API}/${id}`);
                        if (result) {
                            showSuccess('课程删除成功!');
                            await loadAllCourses();
                        }
                    } catch (error) {
                        showError(`删除课程失败: ${error.message}`);
                    } finally {
                        hideLoading();
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
            courseId: parseInt(courseId),
            courseName: courseNameInput.value.trim(),
            credit: parseInt(creditInput.value),
            teacherId: teacherIdInput.value ? parseInt(teacherIdInput.value) : null
        };

        // 打印发送的数据用于调试
        console.log('准备发送的更新数据:', courseData);

        showLoading();
        try {
            console.log(`更新ID为${courseId}的课程数据:`, courseData);
            const result = await updateData(`${COURSES_API}/${courseId}`, courseData);
            console.log('服务器返回的更新结果:', result);
            
            if (result) {
                showSuccess('课程更新成功!');
                // 直接使用返回的课程数据更新表格
                const courses = await fetchData(COURSES_API);
                if (courses) {
                    populateCourseTable(courses);
                }
                resetCourseForm();
            } else {
                throw new Error('更新失败：服务器未返回有效数据');
            }
        } catch (error) {
            console.error('更新课程错误:', error);
            showError(`更新课程失败: ${error.message}`);
        } finally {
            hideLoading();
        }
    });

    // 工具函数：显示错误消息
    function showError(message) {
        const toast = document.createElement('div');
        toast.className = 'toast align-items-center text-white bg-danger border-0 position-fixed top-0 end-0 m-3';
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'assertive');
        toast.setAttribute('aria-atomic', 'true');
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        document.body.appendChild(toast);
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
        setTimeout(() => toast.remove(), 5000);
    }

    // 工具函数：显示成功消息
    function showSuccess(message) {
        const toast = document.createElement('div');
        toast.className = 'toast align-items-center text-white bg-success border-0 position-fixed top-0 end-0 m-3';
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'assertive');
        toast.setAttribute('aria-atomic', 'true');
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        document.body.appendChild(toast);
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
        setTimeout(() => {
            toast.remove();
            // 在消息消失后刷新课程列表
            loadAllCourses();
        }, 2000);
    }

    // 工具函数：显示加载状态
    function showLoading() {
        const loadingDiv = document.createElement('div');
        loadingDiv.id = 'loadingOverlay';
        loadingDiv.className = 'position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center bg-dark bg-opacity-50';
        loadingDiv.style.zIndex = '9999';
        loadingDiv.innerHTML = `
            <div class="spinner-border text-light" role="status">
                <span class="visually-hidden">加载中...</span>
            </div>
        `;
        document.body.appendChild(loadingDiv);
    }

    // 工具函数：隐藏加载状态
    function hideLoading() {
        const loadingDiv = document.getElementById('loadingOverlay');
        if (loadingDiv) {
            loadingDiv.remove();
        }
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