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

        console.log('发送课程数据:', courseData);
        const result = await postData(COURSES_API, courseData);
        if (result) {
            if (result.courseId === null) {
                console.warn('服务器返回的课程ID为null，可能会影响后续操作');
            }

            alert('课程创建成功!');
            resetCourseForm();
            await fetchCoursesByTeacher();
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
        // 加载课程列表
        await fetchCoursesByTeacher();
    }

    // 按教师获取课程
    async function fetchCoursesByTeacher() {
        const teacherId = queryTeacherIdInput.value;
        if (teacherId) {
            console.log(`查询教师ID为${teacherId}的课程`);
            const courses = await fetchData(`${COURSES_API}?teacherId=${teacherId}`);
            if (courses) {
                populateCourseTable(courses);
            }
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
        return true;
    }

    // 重置课程表单
    function resetCourseForm() {
        courseIdInput.value = '';
        courseForm.reset();
        saveCourseBtn.disabled = false;
        updateCourseBtn.disabled = false;
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
            const courseId = course.courseId || '未知';
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${courseId}</td>
                <td>${course.courseName || ''}</td>
                <td>${course.credit || ''}</td>
                <td>${course.teacherId || ''}</td>
                <td>
                    <button class="btn btn-sm btn-warning edit-course" data-id="${courseId}">编辑</button>
                    <button class="btn btn-sm btn-danger delete-course" data-id="${courseId}">删除</button>
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

                if (!id || id === '未知') {
                    alert('无法编辑：课程ID无效');
                    return;
                }

                console.log(`编辑ID为${id}的课程`);
                const course = await fetchData(`${COURSES_API}/${id}`);
                if (course) {
                    courseIdInput.value = course.courseId || '';
                    courseNameInput.value = course.courseName || '';
                    creditInput.value = course.credit || '';
                    teacherIdInput.value = course.teacherId || '';
                }
            });
        });

        // 删除按钮事件
        document.querySelectorAll('.delete-course').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');

                if (!id || id === '未知') {
                    alert('无法删除：课程ID无效');
                    return;
                }

                if (confirm(`确定要删除ID为${id}的课程吗?`)) {
                    console.log(`删除ID为${id}的课程`);
                    const result = await deleteData(`${COURSES_API}/${id}`);
                    if (result) {
                        alert('课程删除成功!');
                        await fetchCoursesByTeacher();
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
            courseId: parseInt(courseId),
            courseName: courseNameInput.value,
            credit: parseInt(creditInput.value),
            teacherId: teacherIdInput.value ? parseInt(teacherIdInput.value) : null
        };

        console.log(`更新ID为${courseId}的课程数据:`, courseData);
        const result = await updateData(`${COURSES_API}/${courseId}`, courseData);
        if (result) {
            alert('课程更新成功!');
            resetCourseForm();
            await fetchCoursesByTeacher();
        }
    });
}); 