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
        const response = await fetch(url);
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`请求失败: ${response.status} - ${errorText}`);
        }
        return await response.json();
    } catch (error) {
        handleError(error);
        return null;
    }
}

// 通用POST请求
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
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`请求失败: ${response.status} - ${errorText}`);
        }

        return await response.json();
    } catch (error) {
        handleError(error);
        return null;
    }
}

// 通用DELETE请求
async function deleteData(url) {
    try {
        const response = await fetch(url, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`请求失败: ${response.status} - ${errorText}`);
        }

        return await response.json();
    } catch (error) {
        handleError(error);
        return null;
    }
}
