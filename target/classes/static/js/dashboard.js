// 全局变量
let stompClient = null;
let currentTestId = null;
let tests = [];

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    connectWebSocket();
    refreshData();
});

// 连接WebSocket
function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('WebSocket连接成功');
        
        // 订阅API测试更新
        stompClient.subscribe('/topic/api-test-update', function(message) {
            const test = JSON.parse(message.body);
            updateTestInList(test);
            updateStatistics();
        });
        
        // 订阅统计信息更新
        stompClient.subscribe('/topic/statistics-update', function(message) {
            const stats = JSON.parse(message.body);
            updateStatisticsDisplay(stats);
        });
        
        // 订阅批量执行通知
        stompClient.subscribe('/topic/batch-execution', function(message) {
            const data = JSON.parse(message.body);
            handleBatchExecutionMessage(data);
        });
    }, function(error) {
        console.error('WebSocket连接失败:', error);
        setTimeout(connectWebSocket, 5000); // 5秒后重连
    });
}

// 刷新数据
function refreshData() {
    fetch('/api/tests')
        .then(response => response.json())
        .then(data => {
            tests = data;
            renderTestList();
            updateStatistics();
        })
        .catch(error => {
            console.error('获取测试数据失败:', error);
            showToast('获取数据失败', 'error');
        });
}

// 渲染测试列表
function renderTestList() {
    const testList = document.getElementById('testList');
    testList.innerHTML = '';
    
    if (tests.length === 0) {
        testList.innerHTML = '<div class="text-center text-muted py-4">暂无测试数据</div>';
        return;
    }
    
    tests.forEach(test => {
        const testItem = createTestItem(test);
        testList.appendChild(testItem);
    });
}

// 创建测试项目元素 - 卡片形式横向排列
function createTestItem(test) {
    const div = document.createElement('div');
    div.className = `col-lg-4 col-md-6 col-sm-12 mb-3`;
    div.innerHTML = `
        <div class="card h-100 test-item ${getStatusClass(test.status)}">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h6 class="mb-0 text-truncate" title="${test.name}">${test.name}</h6>
                <span class="badge ${getStatusBadgeClass(test.status)}">${getStatusText(test.status)}</span>
            </div>
            <div class="card-body d-flex flex-column">
                <p class="test-description mb-3 flex-grow-1">${test.description || '无描述'}</p>
                <div class="test-meta mb-3">
                    ${test.executionTime ? `<div class="execution-time mb-1"><i class="bi bi-clock"></i> ${test.executionTime}ms</div>` : ''}
                    ${test.lastRunTime ? `<div class="text-muted small"><i class="bi bi-calendar"></i> ${formatDateTime(test.lastRunTime)}</div>` : ''}
                </div>
                <div class="test-actions d-flex gap-2">
                    <button class="btn btn-sm btn-outline-primary flex-fill" onclick="viewTestDetail('${test.id}')" title="查看详情">
                        <i class="bi bi-eye"></i> 详情
                    </button>
                    <button class="btn btn-sm btn-outline-success flex-fill" onclick="executeTest('${test.id}')" title="执行测试">
                        <i class="bi bi-play"></i> 执行
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteTest('${test.id}')" title="删除测试">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </div>
        </div>
    `;
    return div;
}

// 更新列表中的测试
function updateTestInList(updatedTest) {
    const index = tests.findIndex(t => t.id === updatedTest.id);
    if (index !== -1) {
        tests[index] = updatedTest;
        renderTestList();
    }
}

// 获取状态样式类
function getStatusClass(status) {
    switch (status) {
        case 'SUCCESS': return 'success';
        case 'FAILED': return 'failed';
        case 'RUNNING': return 'running';
        default: return '';
    }
}

// 获取状态徽章样式
function getStatusBadgeClass(status) {
    switch (status) {
        case 'SUCCESS': return 'bg-success';
        case 'FAILED': return 'bg-danger';
        case 'RUNNING': return 'bg-warning';
        default: return 'bg-secondary';
    }
}

// 获取状态文本
function getStatusText(status) {
    switch (status) {
        case 'SUCCESS': return '成功';
        case 'FAILED': return '失败';
        case 'RUNNING': return '执行中';
        default: return '未知';
    }
}

// 更新统计信息
function updateStatistics() {
    const stats = {
        total: tests.length,
        success: tests.filter(t => t.status === 'SUCCESS').length,
        failed: tests.filter(t => t.status === 'FAILED').length,
        running: tests.filter(t => t.status === 'RUNNING').length
    };
    
    stats.successRate = stats.total > 0 ? Math.round((stats.success / stats.total) * 100) : 0;
    
    updateStatisticsDisplay(stats);
}

// 更新统计信息显示
function updateStatisticsDisplay(stats) {
    document.getElementById('totalTests').textContent = stats.total || 0;
    document.getElementById('runningTests').textContent = stats.running || 0;
    document.getElementById('successTests').textContent = stats.success || 0;
    document.getElementById('failedTests').textContent = stats.failed || 0;
    document.getElementById('notExecutedTests').textContent = stats.notExecuted || 0;
    document.getElementById('successRate').textContent = (stats.successRate || 0).toFixed(1) + '%';
}

// 执行单个测试
function executeTest(testId) {
    const button = event.target.closest('button');
    const originalContent = button.innerHTML;
    
    // 显示加载状态
    button.innerHTML = '<span class="loading-spinner"></span>';
    button.disabled = true;
    
    fetch(`/api/tests/${testId}/execute`, { method: 'POST' })
        .then(response => response.json())
        .then(data => {
            showToast('测试执行已开始', 'success');
        })
        .catch(error => {
            console.error('执行测试失败:', error);
            showToast('执行测试失败', 'error');
        })
        .finally(() => {
            // 恢复按钮状态
            button.innerHTML = originalContent;
            button.disabled = false;
        });
}

// 执行所有测试
function executeAll() {
    const button = event.target.closest('button');
    const originalContent = button.innerHTML;
    
    // 显示加载状态
    button.innerHTML = '<span class="loading-spinner"></span> 执行中...';
    button.disabled = true;
    
    fetch('/api/tests/execute-all', { method: 'POST' })
        .then(response => response.json())
        .then(data => {
            showToast(`开始执行 ${data.count} 个测试`, 'success');
        })
        .catch(error => {
            console.error('批量执行失败:', error);
            showToast('批量执行失败', 'error');
        })
        .finally(() => {
            // 恢复按钮状态
            button.innerHTML = originalContent;
            button.disabled = false;
        });
}

// 删除测试
function deleteTest(testId) {
    if (confirm('确定要删除这个测试吗？')) {
        fetch(`/api/tests/${testId}`, { method: 'DELETE' })
            .then(response => {
                if (response.ok) {
                    tests = tests.filter(t => t.id !== testId);
                    renderTestList();
                    updateStatistics();
                    showToast('测试已删除', 'success');
                }
            })
            .catch(error => {
                console.error('删除测试失败:', error);
                showToast('删除测试失败', 'error');
            });
    }
}

// 查看测试详情
function viewTestDetail(testId) {
    const test = tests.find(t => t.id === testId);
    if (!test) return;
    
    currentTestId = testId;
    
    document.getElementById('detailTitle').textContent = test.name;
    document.getElementById('detailName').textContent = test.name;
    document.getElementById('detailStatus').innerHTML = `<span class="badge ${getStatusBadgeClass(test.status)}">${getStatusText(test.status)}</span>`;
    document.getElementById('detailExecutionTime').textContent = test.executionTime ? test.executionTime + 'ms' : '未执行';
    document.getElementById('detailLastRun').textContent = test.lastRunTime ? formatDateTime(test.lastRunTime) : '未执行';
    document.getElementById('detailCurl').textContent = test.curlCommand;
    document.getElementById('detailResult').textContent = test.result || '无结果';
    
    const errorSection = document.getElementById('errorSection');
    const detailError = document.getElementById('detailError');
    
    if (test.errorMessage) {
        errorSection.style.display = 'block';
        detailError.textContent = test.errorMessage;
    } else {
        errorSection.style.display = 'none';
    }
    
    new bootstrap.Modal(document.getElementById('testDetailModal')).show();
}

// 执行单个测试（从详情页面）
function executeSingleTest() {
    if (currentTestId) {
        executeTest(currentTestId);
        bootstrap.Modal.getInstance(document.getElementById('testDetailModal')).hide();
    }
}

// 显示添加测试模态框
function showAddModal() {
    document.getElementById('addTestForm').reset();
    new bootstrap.Modal(document.getElementById('addTestModal')).show();
}

// 添加测试
function addTest() {
    const name = document.getElementById('testName').value.trim();
    const curlCommand = document.getElementById('curlCommand').value.trim();
    const description = document.getElementById('testDescription').value.trim();
    
    if (!name || !curlCommand) {
        showToast('请填写必填字段', 'error');
        return;
    }
    
    const test = {
        name: name,
        curlCommand: curlCommand,
        description: description
    };
    
    fetch('/api/tests', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(test)
    })
    .then(response => response.json())
    .then(data => {
        tests.push(data);
        renderTestList();
        updateStatistics();
        bootstrap.Modal.getInstance(document.getElementById('addTestModal')).hide();
        showToast('测试添加成功', 'success');
    })
    .catch(error => {
        console.error('添加测试失败:', error);
        showToast('添加测试失败', 'error');
    });
}

// 初始化示例数据
function initSampleData() {
    fetch('/api/tests/init-sample-data', { method: 'POST' })
        .then(response => response.json())
        .then(data => {
            refreshData();
            showToast(`示例数据初始化完成，共 ${data.count} 个测试`, 'success');
        })
        .catch(error => {
            console.error('初始化示例数据失败:', error);
            showToast('初始化示例数据失败', 'error');
        });
}

// 处理批量执行消息
function handleBatchExecutionMessage(data) {
    switch (data.type) {
        case 'BATCH_START':
            showToast(`开始批量执行 ${data.count} 个测试`, 'info');
            break;
        case 'BATCH_COMPLETE':
            showToast(`批量执行完成: 成功 ${data.successCount} 个，失败 ${data.failedCount} 个`, 'info');
            break;
    }
}

// 格式化日期时间
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '';
    const date = new Date(dateTimeStr);
    return date.toLocaleString('zh-CN');
}

// 显示提示消息
function showToast(message, type = 'info') {
    const toastContainer = document.querySelector('.toast-container');
    
    const alertClass = type === 'error' ? 'alert-danger' : 
                      type === 'success' ? 'alert-success' : 
                      type === 'warning' ? 'alert-warning' : 'alert-info';
    
    const iconClass = type === 'error' ? 'bi-exclamation-triangle' : 
                     type === 'success' ? 'bi-check-circle' : 
                     type === 'warning' ? 'bi-exclamation-circle' : 'bi-info-circle';
    
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert ${alertClass} alert-dismissible fade show`;
    alertDiv.style.cssText = 'min-width: 300px; max-width: 400px; margin-bottom: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);';
    alertDiv.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="bi ${iconClass} me-2" style="font-size: 1.2rem;"></i>
            <span class="flex-grow-1">${message}</span>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    toastContainer.appendChild(alertDiv);
    
    // 自动移除
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.classList.remove('show');
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.parentNode.removeChild(alertDiv);
                }
            }, 150);
        }
    }, 4000);
} 