// 通用API处理函数
const API_BASE_URL = './api';

// 错误处理函数
function handleError(error) {
    console.error('Error:', error);
    alert(`发生错误: ${error.message || error}`);
}

// 表单重置函数
function resetForm(formId) {
    document.getElementById(formId).reset();
}

// 通用GET请求
async function fetchData(url) {
    try {
        console.log(`发送GET请求到 ${url}`);
        const response = await fetch(url);

        console.log(`收到响应:`, response);

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`GET请求错误内容:`, errorText);
            throw new Error(`请求失败: ${response.status} - ${errorText}`);
        }

        const responseData = await response.json();
        console.log(`响应数据:`, responseData);
        return responseData;
    } catch (error) {
        console.error(`GET请求发生错误:`, error);
        handleError(error);
        return null;
    }
}

// 改进的POST请求函数
async function postData(url, data) {
    try {
        console.log(`发送POST请求到 ${url}，数据:`, data);

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        console.log(`收到响应:`, response);

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`POST请求错误内容:`, errorText);
            throw new Error(`请求失败: ${response.status} - ${errorText}`);
        }

        const responseData = await response.json();
        console.log(`响应数据:`, responseData);

        // 验证响应数据的完整性
        if (responseData) {
            if (responseData.age === null && data.age) {
                console.warn('服务器未保存age字段值，请检查后端处理');
            }
            if (responseData.studentId === null) {
                console.warn('服务器未返回有效的studentId，请检查后端ID生成');
            }
        }

        return responseData;
    } catch (error) {
        console.error(`POST请求发生错误:`, error);
        handleError(error);
        return null;
    }
}

// 通用PUT请求
async function updateData(url, data) {
    try {
        console.log(`发送PUT请求到 ${url}，数据:`, data);

        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        console.log(`收到响应:`, response);

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`PUT请求错误内容:`, errorText);
            throw new Error(`请求失败: ${response.status} - ${errorText}`);
        }

        const responseData = await response.json();
        console.log(`响应数据:`, responseData);
        return responseData;
    } catch (error) {
        console.error(`PUT请求发生错误:`, error);
        handleError(error);
        return null;
    }
}

// 通用DELETE请求
async function deleteData(url) {
    try {
        console.log(`发送DELETE请求到 ${url}`);

        const response = await fetch(url, {
            method: 'DELETE'
        });

        console.log(`收到响应:`, response);

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`DELETE请求错误内容:`, errorText);
            throw new Error(`请求失败: ${response.status} - ${errorText}`);
        }

        const responseData = await response.json();
        console.log(`响应数据:`, responseData);
        return responseData;
    } catch (error) {
        console.error(`DELETE请求发生错误:`, error);
        handleError(error);
        return null;
    }
}

