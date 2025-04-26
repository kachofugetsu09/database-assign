// 学生管理模块
document.addEventListener('DOMContentLoaded', () => {
    // DOM元素
    const studentForm = document.getElementById('studentForm');
    const studentIdInput = document.getElementById('studentId');
    const studentNameInput = document.getElementById('studentName');
    const studentGenderSelect = document.getElementById('studentGender');
    const studentAgeInput = document.getElementById('studentAge');
    const enrollmentDateInput = document.getElementById('enrollmentDate');
    const saveStudentBtn = document.getElementById('saveStudent');
    const updateStudentBtn = document.getElementById('updateStudent');
    const resetStudentFormBtn = document.getElementById('resetStudentForm');
    const fetchStudentByIdBtn = document.getElementById('fetchStudentById');
    const queryStudentIdInput = document.getElementById('queryStudentId');
    const fetchStudentsByAgeRangeBtn = document.getElementById('fetchStudentsByAgeRange');
    const minAgeInput = document.getElementById('minAge');
    const maxAgeInput = document.getElementById('maxAge');
    const studentTableBody = document.getElementById('studentTableBody');

    // API端点
    const STUDENTS_API = `${API_BASE_URL}/students`;

    // 调试信息
    console.log(`使用API端点: ${STUDENTS_API}`);

    // 测试API连接
    testApiConnection();

    // 初始化页面
    initPage();

    // 阻止表单默认提交行为，防止重复请求
    studentForm.addEventListener('submit', (event) => {
        event.preventDefault();
    });

    async function testApiConnection() {
        try {
            const response = await fetch(STUDENTS_API);
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

    // 保存学生
    saveStudentBtn.addEventListener('click', async (event) => {
        // 阻止可能的默认行为
        event.preventDefault();

        if (!validateStudentForm()) return;

        // 创建新学生时不发送 studentId 字段，让数据库自动生成
        const studentData = {
            name: studentNameInput.value,
            gender: studentGenderSelect.value,
            age: parseInt(studentAgeInput.value),
            enrollmentDate: enrollmentDateInput.value
            // 不包含 studentId
        };

        console.log('发送学生数据:', studentData);
        const result = await postData(STUDENTS_API, studentData);
        if (result) {
            // 检查返回的结果是否包含必要的数据
            if (result.studentId === null) {
                console.warn('服务器返回的学生ID为null，可能会影响后续操作');
            }

            alert('学生创建成功!');
            resetStudentForm();
            await fetchStudentsByAgeRange();
        }
    });

    // 按ID查询学生
    fetchStudentByIdBtn.addEventListener('click', async () => {
        const id = queryStudentIdInput.value;
        if (!id) {
            alert('请输入学生ID');
            return;
        }

        console.log(`查询ID为${id}的学生`);
        const student = await fetchData(`${STUDENTS_API}/${id}`);
        if (student) {
            populateStudentTable([student]);
        }
    });

    // 按年龄范围查询学生
    fetchStudentsByAgeRangeBtn.addEventListener('click', fetchStudentsByAgeRange);

    // 重置表单
    resetStudentFormBtn.addEventListener('click', resetStudentForm);

    // 初始化页面
    async function initPage() {
        // 设置默认查询年龄范围
        minAgeInput.value = 18;
        maxAgeInput.value = 25;

        // 加载学生列表
        await fetchStudentsByAgeRange();
    }

    // 按年龄范围获取学生
    async function fetchStudentsByAgeRange() {
        const minAge = minAgeInput.value || 0;
        const maxAge = maxAgeInput.value || 100;

        console.log(`查询年龄范围 ${minAge}-${maxAge} 的学生`);
        const students = await fetchData(`${STUDENTS_API}?minAge=${minAge}&maxAge=${maxAge}`);
        if (students) {
            populateStudentTable(students);
        }
    }

    // 验证表单
    function validateStudentForm() {
        if (!studentNameInput.value) {
            alert('请输入学生姓名');
            return false;
        }
        if (!studentAgeInput.value) {
            alert('请输入学生年龄');
            return false;
        }
        if (!enrollmentDateInput.value) {
            alert('请选择入学日期');
            return false;
        }
        return true;
    }

    // 重置学生表单
    function resetStudentForm() {
        studentIdInput.value = '';
        studentForm.reset();
        // 切换按钮状态
        saveStudentBtn.disabled = false;
        updateStudentBtn.disabled = false;
    }

    // 填充学生表格
    function populateStudentTable(students) {
        studentTableBody.innerHTML = '';

        if (!students || students.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = '<td colspan="6" class="text-center">没有找到学生</td>';
            studentTableBody.appendChild(row);
            return;
        }

        students.forEach(student => {
            // 使用 studentId 而不是 id
            const studentId = student.studentId || '未知';
            const enrollmentDateStr = student.enrollmentDate
                ? formatDate(student.enrollmentDate)
                : '';

            const row = document.createElement('tr');
            row.innerHTML = `
            <td>${studentId}</td>
            <td>${student.name || ''}</td>
            <td>${student.gender || ''}</td>
            <td>${student.age || ''}</td>
            <td>${enrollmentDateStr}</td>
            <td>
                <button class="btn btn-sm btn-warning edit-student" data-id="${studentId}">编辑</button>
                <button class="btn btn-sm btn-danger delete-student" data-id="${studentId}">删除</button>
            </td>
        `;
            studentTableBody.appendChild(row);
        });

        addTableButtonHandlers();
    }

    // 向表格按钮添加事件处理
    function addTableButtonHandlers() {
        // 编辑按钮事件
        document.querySelectorAll('.edit-student').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');

                if (!id || id === '未知') {
                    alert('无法编辑：学生ID无效');
                    return;
                }

                console.log(`编辑ID为${id}的学生`);
                const student = await fetchData(`${STUDENTS_API}/${id}`);
                if (student) {
                    // 使用 studentId 而不是 id
                    studentIdInput.value = student.studentId || '';
                    studentNameInput.value = student.name || '';
                    studentGenderSelect.value = student.gender || '男';
                    studentAgeInput.value = student.age || '';

                    // 处理日期
                    if (student.enrollmentDate) {
                        enrollmentDateInput.value = formatDateForInput(student.enrollmentDate);
                    } else {
                        enrollmentDateInput.value = '';
                    }
                }
            });
        });

        // 删除按钮事件
        document.querySelectorAll('.delete-student').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');

                if (!id || id === '未知') {
                    alert('无法删除：学生ID无效');
                    return;
                }

                if (confirm(`确定要删除ID为${id}的学生吗?`)) {
                    console.log(`删除ID为${id}的学生`);
                    const result = await deleteData(`${STUDENTS_API}/${id}`);
                    if (result) {
                        alert('学生删除成功!');
                        await fetchStudentsByAgeRange();
                    }
                }
            });
        });
    }

    // 修改更新学生函数
    updateStudentBtn.addEventListener('click', async (event) => {
        // 阻止可能的默认行为
        event.preventDefault();

        if (!validateStudentForm()) return;

        const studentId = studentIdInput.value;
        if (!studentId) {
            alert('请先选择要更新的学生');
            return;
        }

        const studentData = {
            studentId: parseInt(studentId),  // 使用 studentId 而不是 id
            name: studentNameInput.value,
            gender: studentGenderSelect.value,
            age: parseInt(studentAgeInput.value),
            enrollmentDate: enrollmentDateInput.value
        };

        console.log(`更新ID为${studentId}的学生数据:`, studentData);
        const result = await updateData(`${STUDENTS_API}/${studentId}`, studentData);
        if (result) {
            alert('学生更新成功!');
            resetStudentForm();
            await fetchStudentsByAgeRange();
        }
    });

    // 改进日期格式化函数
    function formatDate(dateString) {
        if (!dateString) return '';

        // 尝试解析日期字符串
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return dateString;

        return date.toLocaleDateString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });
    }

    // 格式化日期为HTML input元素所需的格式 (YYYY-MM-DD)
    function formatDateForInput(dateString) {
        if (!dateString) return '';

        const date = new Date(dateString);
        if (isNaN(date.getTime())) return '';

        return date.toISOString().split('T')[0];
    }
});

