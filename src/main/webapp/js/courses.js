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
            console.log('API 响应头:', response.headers);
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

    // 保存课程
    saveCourseBtn.addEventListener('click', async (event) => {
        event.preventDefault();

        if (!validateCourseForm()) return;

        const courseData = {
            courseName: courseNameInput.value,
            credit: parseInt(creditInput.value),
            teacherId: teacherIdInput.value ? parseInt(teacherIdInput.value) : null
        };

        // 如果提供了课程ID，则添加到请求数据中
        if (courseIdInput.value) {
            courseData.courseId = parseInt(courseIdInput.value);
        }

        console.log('发送课程数据:', courseData);
        const result = await postData(COURSES_API, courseData);
        if (result) {
            alert('课程创建成功!');
            resetCourseForm();
            await loadAllCourses();
        }
    });

    // 按ID查询课程
    fetchCourseByIdBtn.addEventListener('click', async () => {
        const id = queryCourseIdInput.value;
        if (!id) {
            alert('请输入课程ID');
            return;
        }

        console.log(`查询ID为${id}的课程`);
        const course = await fetchData(`${COURSES_API}/${id}`);
        if (course) {
            populateCourseTable([course]);
        }
    });

    // 按教师查询课程
    fetchCoursesByTeacherBtn.addEventListener('click', async () => {
        const teacherId = queryTeacherIdInput.value;
        if (!teacherId) {
            alert('请输入教师ID');
            return;
        }

        console.log(`查询教师ID为${teacherId}的课程`);
        const courses = await fetchData(`${COURSES_API}?teacherId=${teacherId}`);
        if (courses) {
            populateCourseTable(courses);
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
        console.log('加载所有课程');
        const courses = await fetchData(COURSES_API);
        if (courses) {
            populateCourseTable(courses);
        }
    }

    // 验证表单
    function validateCourseForm() {
        if (!courseNameInput.value) {
            alert('请输入课程名称');
            return false;
        }
        if (!creditInput.value) {
            alert('请输入课程学分');
            return false;
        }
        const credit = parseInt(creditInput.value);
        if (credit < 1 || credit > 10) {
            alert('学分必须在1-10之间');
            return false;
        }
        return true;
    }

    // 重置课程表单
    function resetCourseForm() {
        courseForm.reset();
        courseIdInput.disabled = false; // 重置时启用课程ID输入
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

        courses.forEach(course => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${course.courseId || ''}</td>
                <td>${course.courseName || ''}</td>
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
                console.log(`编辑ID为${id}的课程`);
                const course = await fetchData(`${COURSES_API}/${id}`);
                if (course) {
                    courseIdInput.value = course.courseId;
                    courseIdInput.disabled = true; // 编辑时禁用课程ID输入
                    courseNameInput.value = course.courseName;
                    creditInput.value = course.credit;
                    teacherIdInput.value = course.teacherId || '';

                    saveCourseBtn.disabled = true;
                    updateCourseBtn.disabled = false;
                }
            });
        });

        // 删除按钮事件
        document.querySelectorAll('.delete-course').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                if (confirm(`确定要删除ID为${id}的课程吗?`)) {
                    console.log(`删除ID为${id}的课程`);
                    const result = await deleteData(`${COURSES_API}/${id}`);
                    if (result) {
                        alert('课程删除成功!');
                        await loadAllCourses();
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
            alert('请先选择要更新的课程');
            return;
        }

        const courseData = {
            courseName: courseNameInput.value,
            credit: parseInt(creditInput.value),
            teacherId: teacherIdInput.value ? parseInt(teacherIdInput.value) : null
        };

        console.log(`更新ID为${courseId}的课程数据:`, courseData);
        const result = await updateData(`${COURSES_API}/${courseId}`, courseData);
        if (result) {
            alert('课程更新成功!');
            resetCourseForm();
            await loadAllCourses();
        }
    });
}); 