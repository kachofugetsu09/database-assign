document.addEventListener('DOMContentLoaded', () => {
    // DOM元素
    const teacherForm = document.getElementById('teacherForm');
    const teacherIdInput = document.getElementById('teacherId');
    const teacherNameInput = document.getElementById('teacherName');
    const teacherGenderSelect = document.getElementById('teacherGender');
    const teacherTitleInput = document.getElementById('teacherTitle');
    const saveTeacherBtn = document.getElementById('saveTeacher');
    const updateTeacherBtn = document.getElementById('updateTeacher');
    const resetTeacherFormBtn = document.getElementById('resetTeacherForm');
    const fetchTeacherByIdBtn = document.getElementById('fetchTeacherById');
    const queryTeacherIdInput = document.getElementById('queryTeacherId');
    const teacherTableBody = document.getElementById('teacherTableBody');

    // API端点
    const TEACHERS_API = `${API_BASE_URL}/teachers`;

    // 调试信息
    console.log(`使用API端点: ${TEACHERS_API}`);

    // 初始化页面
    initPage();

    // 阻止表单默认提交行为，防止重复请求
    teacherForm.addEventListener('submit', (event) => {
        event.preventDefault();
    });

    // 初始化页面
    async function initPage() {
        // 加载教师列表
        await fetchTeachers();
    }

    // 获取教师列表
    async function fetchTeachers() {
        try {
            const response = await fetch(TEACHERS_API);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const teachers = await response.json();
            populateTeacherTable(teachers);
        } catch (error) {
            console.error('获取教师列表失败:', error);
            alert(`获取教师列表失败: ${error.message}`);
        }
    }

    // 填充教师表格
    function populateTeacherTable(teachers) {
        teacherTableBody.innerHTML = '';

        if (!teachers || teachers.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = '<td colspan="5" class="text-center">没有找到教师</td>';
            teacherTableBody.appendChild(row);
            return;
        }

        teachers.forEach(teacher => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${teacher.teacherId}</td>
                <td>${teacher.name || ''}</td>
                <td>${teacher.gender || ''}</td>
                <td>${teacher.title || ''}</td>
                <td>
                    <button class="btn btn-sm btn-warning edit-teacher" data-id="${teacher.teacherId}">编辑</button>
                    <button class="btn btn-sm btn-danger delete-teacher" data-id="${teacher.teacherId}">删除</button>
                </td>
            `;
            teacherTableBody.appendChild(row);
        });

        addTableButtonHandlers();
    }

    // 向表格按钮添加事件处理
    function addTableButtonHandlers() {
        // 编辑按钮事件
        document.querySelectorAll('.edit-teacher').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');

                if (!id) {
                    alert('无法编辑：教师ID无效');
                    return;
                }

                console.log(`编辑ID为${id}的教师`);
                const teacher = await fetchData(`${TEACHERS_API}/${id}`);
                if (teacher) {
                    teacherIdInput.value = teacher.teacherId || '';
                    teacherNameInput.value = teacher.name || '';
                    teacherGenderSelect.value = teacher.gender || '男';
                    teacherTitleInput.value = teacher.title || '';

                    // 切换按钮状态
                    saveTeacherBtn.disabled = true;
                    updateTeacherBtn.disabled = false;
                }
            });
        });

        // 删除按钮事件
        document.querySelectorAll('.delete-teacher').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');

                if (!id) {
                    alert('无法删除：教师ID无效');
                    return;
                }

                if (confirm(`确定要删除ID为${id}的教师吗?`)) {
                    console.log(`删除ID为${id}的教师`);
                    const result = await deleteData(`${TEACHERS_API}/${id}`);
                    if (result) {
                        alert('教师删除成功!');
                        await fetchTeachers();
                    }
                }
            });
        });
    }

    // 保存教师
    saveTeacherBtn.addEventListener('click', async (event) => {
        // 阻止可能的默认行为
        event.preventDefault();

        if (!validateTeacherForm()) return;

        // 创建新教师时不发送 teacherId 字段，让数据库自动生成
        const teacherData = {
            name: teacherNameInput.value,
            gender: teacherGenderSelect.value,
            title: teacherTitleInput.value
            // 不包含 teacherId
        };

        console.log('发送教师数据:', teacherData);
        const result = await postData(TEACHERS_API, teacherData);
        if (result) {
            // 检查返回的结果是否包含必要的数据
            if (result.teacherId === null) {
                console.warn('服务器返回的教师ID为null，可能会影响后续操作');
            }

            alert('教师创建成功!');
            resetTeacherForm();
            await fetchTeachers();
        }
    });

    // 按ID查询教师
    fetchTeacherByIdBtn.addEventListener('click', async () => {
        const id = queryTeacherIdInput.value;
        if (!id) {
            alert('请输入教师ID');
            return;
        }

        console.log(`查询ID为${id}的教师`);
        const teacher = await fetchData(`${TEACHERS_API}/${id}`);
        if (teacher) {
            populateTeacherTable([teacher]);
        }
    });

    // 重置表单
    resetTeacherFormBtn.addEventListener('click', resetTeacherForm);

    // 验证表单
    function validateTeacherForm() {
        if (!teacherNameInput.value) {
            alert('请输入教师姓名');
            return false;
        }
        if (!teacherTitleInput.value) {
            alert('请输入教师职称');
            return false;
        }
        return true;
    }

    // 重置教师表单
    function resetTeacherForm() {
        teacherIdInput.value = '';
        teacherForm.reset();
        // 切换按钮状态
        saveTeacherBtn.disabled = false;
        updateTeacherBtn.disabled = true;
    }

    // 修改更新教师函数
    updateTeacherBtn.addEventListener('click', async (event) => {
        // 阻止可能的默认行为
        event.preventDefault();

        if (!validateTeacherForm()) return;

        const teacherId = teacherIdInput.value;
        if (!teacherId) {
            alert('请先选择要更新的教师');
            return;
        }

        const teacherData = {
            teacherId: parseInt(teacherId),
            name: teacherNameInput.value,
            gender: teacherGenderSelect.value,
            title: teacherTitleInput.value
        };

        console.log(`更新ID为${teacherId}的教师数据:`, teacherData);
        try {
            const result = await updateData(`${TEACHERS_API}/${teacherId}`, teacherData);
            if (result) {
                console.log('更新成功，服务器返回数据:', result);
                // 直接使用返回的数据更新表格中的对应行
                const row = document.querySelector(`tr:has(button[data-id="${teacherId}"])`);
                if (row) {
                    row.innerHTML = `
                        <td>${result.teacherId}</td>
                        <td>${result.name || ''}</td>
                        <td>${result.gender || ''}</td>
                        <td>${result.title || ''}</td>
                        <td>
                            <button class="btn btn-sm btn-warning edit-teacher" data-id="${result.teacherId}">编辑</button>
                            <button class="btn btn-sm btn-danger delete-teacher" data-id="${result.teacherId}">删除</button>
                        </td>
                    `;
                    // 重新绑定按钮事件
                    addTableButtonHandlers();
                }
                alert('教师信息更新成功！');
                resetTeacherForm();
            }
        } catch (error) {
            console.error('更新教师信息失败:', error);
            alert(`更新失败: ${error.message}`);
        }
    });
});