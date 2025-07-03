// 全局变量
let stompClient = null;
let currentTestId = null;
let tests = [];
let filteredTests = [];
let currentViewMode = 'card'; // 'card' 或 'table'

// 分页相关变量
let currentPage = 1;
let pageSize = 12;
let totalElements = 0;
let totalPages = 0;
let currentSearch = '';
let currentStatus = '';
let currentModule = '';
let currentSortBy = 'name';
let currentSortOrder = 'asc';

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
    loadTestsByPage();
}

// 加载分页数据
function loadTestsByPage() {
    const params = new URLSearchParams({
        page: currentPage,
        size: pageSize,
        sortBy: currentSortBy,
        sortOrder: currentSortOrder
    });

    if (currentSearch) {
        params.append('search', currentSearch);
    }

    if (currentStatus) {
        params.append('status', currentStatus);
    }

    if (currentModule) {
        params.append('module', currentModule);
    }

    fetch(`/api/tests/page?${params}`)
        .then(response => response.json())
        .then(data => {
            tests = data.content || [];
            totalElements = data.totalElements || 0;
            totalPages = data.totalPages || 0;
            currentPage = data.currentPage || 1;

            renderTestList();
            renderPagination();
            updateStatistics();
        })
        .catch(error => {
            console.error('获取测试数据失败:', error);
            showToast('获取数据失败', 'error');
        });
}

// 渲染测试列表-
function renderTestList() {
    const testList = document.getElementById('testList');
    testList.innerHTML = '';

    if (tests.length === 0) {
        testList.innerHTML = '<div class="text-center text-muted py-4">暂无测试数据</div>';
        return;
    }

    if (currentViewMode === 'table') {
        renderTableView(testList, tests);
    } else {
        renderCardView(testList, tests);
    }
}

// 渲染卡片视图
function renderCardView(container, testsToRender) {
    container.className = 'row';
    testsToRender.forEach(test => {
        const testItem = createTestItem(test);
        container.appendChild(testItem);
    });
}

// 渲染表格视图
function renderTableView(container, testsToRender) {
    container.className = '';
    container.innerHTML = `
        <div class="table-responsive">
            <table class="table table-hover">
                <thead class="table-light">
                    <tr>
                        <th width="50">
                            <input type="checkbox" class="form-check-input" id="tableSelectAll" onchange="toggleTableSelectAll()">
                        </th>
                        <th>测试名称</th>
                        <th>模块</th>
                        <th>状态</th>
                        <th>描述</th>
                        <th>执行时间</th>
                        <th>最后执行</th>
                        <th width="200">操作</th>
                    </tr>
                </thead>
                <tbody id="testTableBody">
                </tbody>
            </table>
        </div>
    `;

    const tbody = document.getElementById('testTableBody');
    testsToRender.forEach(test => {
        const row = createTableRow(test);
        tbody.appendChild(row);
    });
}

// 创建表格行
function createTableRow(test) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td>
            <input type="checkbox" class="form-check-input table-test-checkbox" 
                   value="${test.id}" onchange="updateSelectedCount()" 
                   ${test.enabled ? '' : 'disabled'}>
        </td>
        <td>
            <div class="fw-bold">${test.name}</div>
            <small class="text-muted">ID: ${test.id}</small>
        </td>
        <td>
            ${test.module ? `<small class="text-muted"><i class="bi bi-tags"></i> ${test.module}</small>` : ''}
        </td>
        <td>
            <span class="badge ${getStatusBadgeClass(test.status)}">${getStatusText(test.status)}</span>
        </td>
        <td>
            <div class="text-truncate" style="max-width: 200px;" title="${test.description || '无描述'}">
                ${test.description || '无描述'}
            </div>
        </td>
        <td>
            ${test.executionTime ? `<span class="text-muted">${test.executionTime}ms</span>` : '-'}
        </td>
        <td>
            ${test.lastRunTime ? `<small class="text-muted">${formatDateTime(test.lastRunTime)}</small>` : '-'}
        </td>
        <td>
            <div class="btn-group btn-group-sm" role="group">
                <button class="btn btn-outline-primary" onclick="viewTestDetail('${test.id}')" title="查看详情">
                    <i class="bi bi-eye"></i>
                </button>
                <button class="btn btn-outline-success" onclick="executeTest('${test.id}')" title="执行测试">
                    <i class="bi bi-play"></i>
                </button>
                <button class="btn btn-outline-danger" onclick="deleteTest('${test.id}')" title="删除测试">
                    <i class="bi bi-trash"></i>
                </button>
            </div>
        </td>
    `;
    return tr;
}

// 创建测试项目元素 - 卡片形式横向排列
function createTestItem(test) {
    const div = document.createElement('div');
    div.className = `col-lg-4 col-md-6 col-sm-12 mb-3`;
    div.innerHTML = `
        <div class="card h-100 test-item ${getStatusClass(test.status)}">
            <div class="card-header d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center">
                    <input type="checkbox" class="form-check-input me-2 test-checkbox" 
                           value="${test.id}" onchange="updateSelectedCount()" 
                           ${test.enabled ? '' : 'disabled'}>
                    <div>
                        <h6 class="mb-0 text-truncate" title="${test.name}">${test.name}</h6>
                        ${test.module ? `<small class="text-muted"><i class="bi bi-tags"></i> ${test.module}</small>` : ''}
                    </div>
                </div>
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
    // 从后端获取最新的统计数据
    fetch('/api/tests/statistics')
        .then(response => response.json())
        .then(stats => {
            updateStatisticsDisplay(stats);
        })
        .catch(error => {
            console.error('获取统计数据失败:', error);
            // 如果获取失败，使用本地数据作为备选
            const localStats = {
                total: tests.length,
                success: tests.filter(t => t.status === 'SUCCESS').length,
                failed: tests.filter(t => t.status === 'FAILED').length,
                running: tests.filter(t => t.status === 'RUNNING').length
            };
            localStats.successRate = localStats.total > 0 ? Math.round((localStats.success / localStats.total) * 100) : 0;
            updateStatisticsDisplay(localStats);
        });
}

// 更新统计信息显示
function updateStatisticsDisplay(stats) {
    document.getElementById('totalTests').textContent = stats.total || 0;
    document.getElementById('runningTests').textContent = stats.running || 0;
    document.getElementById('successTests').textContent = stats.success || 0;
    document.getElementById('failedTests').textContent = stats.failed || 0;
    document.getElementById('notExecutedTests').textContent = stats.notExecuted || 0;

    // 更新成功率显示
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
            // 重新获取统计数据
            updateStatistics();
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
            // 重新获取统计数据
            updateStatistics();
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
    document.getElementById('detailModule').textContent = test.module || '无模块';
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
    loadTestHistory(); // 保证每次打开详情都刷新历史
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
    const module = document.getElementById('testModule').value.trim();
    const description = document.getElementById('testDescription').value.trim();

    if (!name || !curlCommand || !module) {
        showToast('请填写必填字段', 'error');
        return;
    }

    const test = {
        name: name,
        curlCommand: curlCommand,
        module: module,
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

// 全选/取消全选
function toggleSelectAll() {
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const testCheckboxes = document.querySelectorAll('.test-checkbox:not(:disabled)');

    testCheckboxes.forEach(checkbox => {
        checkbox.checked = selectAllCheckbox.checked;
    });

    updateSelectedCount();
}

// 更新选中数量
function updateSelectedCount() {
    const selectedCheckboxes = document.querySelectorAll('.test-checkbox:checked, .table-test-checkbox:checked');
    const executeSelectedBtn = document.getElementById('executeSelectedBtn');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const tableSelectAll = document.getElementById('tableSelectAll');
    const batchOperationArea = document.getElementById('batchOperationArea');
    const selectedCount = document.getElementById('selectedCount');

    const selectedCountNum = selectedCheckboxes.length;

    // 更新选中数量显示
    selectedCount.textContent = selectedCountNum;

    // 显示/隐藏批量操作区域
    if (selectedCountNum > 0) {
        batchOperationArea.style.display = 'block';
    } else {
        batchOperationArea.style.display = 'none';
    }

    // 更新执行选中测试按钮状态
    executeSelectedBtn.disabled = selectedCountNum === 0;

    // 更新全选复选框状态
    const allCheckboxes = document.querySelectorAll('.test-checkbox:not(:disabled), .table-test-checkbox:not(:disabled)');
    const allChecked = allCheckboxes.length > 0 && selectedCountNum === allCheckboxes.length;

    if (selectAllCheckbox) {
        selectAllCheckbox.checked = allChecked;
        selectAllCheckbox.indeterminate = selectedCountNum > 0 && !allChecked;
    }

    if (tableSelectAll) {
        tableSelectAll.checked = allChecked;
        tableSelectAll.indeterminate = selectedCountNum > 0 && !allChecked;
    }
}

// 清空选择
function clearSelection() {
    const selectedCheckboxes = document.querySelectorAll('.test-checkbox:checked, .table-test-checkbox:checked');
    selectedCheckboxes.forEach(checkbox => checkbox.checked = false);
    updateSelectedCount();
    showToast('已清空选择', 'info');
}

// 执行选中的测试
function executeSelected() {
    const selectedCheckboxes = document.querySelectorAll('.test-checkbox:checked, .table-test-checkbox:checked');
    const selectedIds = Array.from(selectedCheckboxes).map(checkbox => checkbox.value);

    if (selectedIds.length === 0) {
        showToast('请先选择要执行的测试', 'warning');
        return;
    }

    const button = event.target.closest('button');
    const originalContent = button.innerHTML;

    // 显示加载状态
    button.innerHTML = '<span class="loading-spinner"></span> 执行中...';
    button.disabled = true;

    fetch('/api/tests/batch-execute', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(selectedIds)
    })
    .then(response => response.json())
    .then(data => {
        showToast(`开始执行 ${data.count} 个选中的测试`, 'success');
        // 清空选择
        selectedCheckboxes.forEach(checkbox => checkbox.checked = false);
        updateSelectedCount();
        // 重新获取统计数据
        updateStatistics();
    })
    .catch(error => {
        console.error('批量执行失败:', error);
        showToast('批量执行失败', 'error');
    })
    .finally(() => {
        // 恢复按钮状态
        button.innerHTML = originalContent;
        button.disabled = false; // 恢复按钮状态，让用户可以再次点击
    });
}

// 筛选测试
function filterTests() {
    const searchTerm = document.getElementById('searchInput').value;
    const statusFilter = document.getElementById('statusFilter').value;

    // 更新当前筛选条件
    currentSearch = searchTerm;
    currentStatus = statusFilter;
    currentPage = 1; // 重置到第一页

    // 重新加载数据
    loadTestsByPage();
}

// 执行搜索（点击搜索按钮或按回车键）
function performSearch() {
    const searchTerm = document.getElementById('searchInput').value;
    const statusFilter = document.getElementById('statusFilter').value;
    const moduleFilter = document.getElementById('moduleFilter').value;
    const sortBy = document.getElementById('sortBy').value;

    // 更新当前筛选和排序条件
    currentSearch = searchTerm;
    currentStatus = statusFilter;
    currentModule = moduleFilter;
    currentSortBy = sortBy;
    currentPage = 1; // 重置到第一页

    // 更新搜索状态指示器
    updateSearchStatus();

    // 重新加载数据
    loadTestsByPage();

    // 显示搜索提示
    if (searchTerm || statusFilter || moduleFilter) {
        const searchInfo = [];
        if (searchTerm) searchInfo.push(`关键词: "${searchTerm}"`);
        if (statusFilter) searchInfo.push(`状态: ${getStatusText(statusFilter)}`);
        if (moduleFilter) searchInfo.push(`模块: ${moduleFilter}`);
        showToast(`搜索条件: ${searchInfo.join(', ')}`, 'info');
    }
}

// 清空搜索
function clearSearch() {
    document.getElementById('searchInput').value = '';
    document.getElementById('statusFilter').value = '';
    document.getElementById('moduleFilter').value = '';
    document.getElementById('sortBy').value = 'name';

    // 重置搜索条件
    currentSearch = '';
    currentStatus = '';
    currentModule = '';
    currentSortBy = 'name';
    currentSortOrder = 'asc';
    currentPage = 1;

    // 更新搜索状态指示器
    updateSearchStatus();

    // 重新加载数据
    loadTestsByPage();
    showToast('搜索条件已清空', 'info');
}

// 更新搜索状态指示器
function updateSearchStatus() {
    const searchStatus = document.getElementById('searchStatus');
    const hasSearchCondition = currentSearch || currentStatus || currentModule;

    if (hasSearchCondition) {
        searchStatus.style.display = 'inline-block';
        let statusText = '已筛选';
        const conditions = [];
        if (currentSearch) conditions.push(`关键词: "${currentSearch}"`);
        if (currentStatus) conditions.push(`状态: ${getStatusText(currentStatus)}`);
        if (currentModule) conditions.push(`模块: ${currentModule}`);

        if (conditions.length > 0) {
            statusText = conditions.join(', ');
        }
        searchStatus.title = statusText;
    } else {
        searchStatus.style.display = 'none';
    }
}

// 处理搜索框键盘事件
function handleSearchKeyPress(event) {
    if (event.key === 'Enter') {
        performSearch();
    }
}

// 排序测试
function sortTests() {
    const sortBy = document.getElementById('sortBy').value;

    // 更新当前排序条件
    currentSortBy = sortBy;
    currentPage = 1; // 重置到第一页

    // 重新加载数据
    loadTestsByPage();
}

// 切换视图模式
function switchViewMode() {
    const cardView = document.getElementById('cardView');
    const tableView = document.getElementById('tableView');

    if (cardView.checked) {
        currentViewMode = 'card';
    } else if (tableView.checked) {
        currentViewMode = 'table';
    }

    renderTestList();
}

// 表格视图的全选功能
function toggleTableSelectAll() {
    const tableSelectAll = document.getElementById('tableSelectAll');
    const tableCheckboxes = document.querySelectorAll('.table-test-checkbox:not(:disabled)');

    tableCheckboxes.forEach(checkbox => {
        checkbox.checked = tableSelectAll.checked;
    });

    updateSelectedCount();
}

// 加载测试历史记录
function loadTestHistory() {
    if (!currentTestId) return;

    const historyContent = document.getElementById('historyContent');
    historyContent.innerHTML = `
        <div class="text-center py-4">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">加载中...</span>
            </div>
            <p class="mt-2">正在加载历史记录...</p>
        </div>
    `;

    fetch(`/api/tests/${currentTestId}/history`)
        .then(response => response.json())
        .then(history => {
            if (history.length === 0) {
                historyContent.innerHTML = `
                    <div class="text-center text-muted py-4">
                        <i class="bi bi-inbox fs-1"></i>
                        <p class="mt-2">暂无执行历史记录</p>
                    </div>
                `;
                return;
            }

            const historyHtml = `
                <div class="table-responsive">
                    <table class="table table-sm">
                        <thead class="table-light">
                            <tr>
                                <th>执行时间</th>
                                <th>状态</th>
                                <th>执行时长</th>
                                <th>响应码</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${history.map(record => `
                                <tr>
                                    <td>
                                        <small class="text-muted">${formatDateTime(record.runTime)}</small>
                                    </td>
                                    <td>
                                        <span class="badge ${getStatusBadgeClass(record.status)}">${getStatusText(record.status)}</span>
                                    </td>
                                    <td>
                                        ${record.executionTime ? `<span class="text-muted">${record.executionTime}ms</span>` : '-'}
                                    </td>
                                    <td>
                                        ${record.responseCode !== null ? record.responseCode : '-'}
                                    </td>
                                    <td>
                                        <button class="btn btn-sm btn-outline-info" onclick="viewHistoryDetail('${record.id}')" title="查看详情">
                                            <i class="bi bi-eye"></i>
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            `;

            historyContent.innerHTML = historyHtml;
        })
        .catch(error => {
            console.error('加载历史记录失败:', error);
            historyContent.innerHTML = `
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle"></i>
                    加载历史记录失败: ${error.message}
                </div>
            `;
        });
}

// 查看历史记录详情
function viewHistoryDetail(recordId) {
    // 这里可以打开一个新的模态框显示历史记录的详细信息
    showToast('历史记录详情功能开发中...', 'info');
}

// 渲染分页控件
function renderPagination() {
    const paginationContainer = document.getElementById('paginationContainer');
    const pagination = document.getElementById('pagination');
    const startIndex = document.getElementById('startIndex');
    const endIndex = document.getElementById('endIndex');
    const totalElementsSpan = document.getElementById('totalElements');

    if (totalPages <= 1) {
        paginationContainer.style.display = 'none';
        return;
    }

    paginationContainer.style.display = 'flex';

    // 更新显示信息
    const start = (currentPage - 1) * pageSize + 1;
    const end = Math.min(currentPage * pageSize, totalElements);
    startIndex.textContent = start;
    endIndex.textContent = end;
    totalElementsSpan.textContent = totalElements;

    // 生成分页按钮
    pagination.innerHTML = '';

    // 上一页按钮
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${currentPage === 1 ? 'disabled' : ''}`;
    prevLi.innerHTML = `<a class="page-link" href="#" onclick="goToPage(${currentPage - 1})">上一页</a>`;
    pagination.appendChild(prevLi);

    // 页码按钮
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);

    if (startPage > 1) {
        const firstLi = document.createElement('li');
        firstLi.className = 'page-item';
        firstLi.innerHTML = `<a class="page-link" href="#" onclick="goToPage(1)">1</a>`;
        pagination.appendChild(firstLi);

        if (startPage > 2) {
            const ellipsisLi = document.createElement('li');
            ellipsisLi.className = 'page-item disabled';
            ellipsisLi.innerHTML = '<span class="page-link">...</span>';
            pagination.appendChild(ellipsisLi);
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        const li = document.createElement('li');
        li.className = `page-item ${i === currentPage ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#" onclick="goToPage(${i})">${i}</a>`;
        pagination.appendChild(li);
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
            const ellipsisLi = document.createElement('li');
            ellipsisLi.className = 'page-item disabled';
            ellipsisLi.innerHTML = '<span class="page-link">...</span>';
            pagination.appendChild(ellipsisLi);
        }

        const lastLi = document.createElement('li');
        lastLi.className = 'page-item';
        lastLi.innerHTML = `<a class="page-link" href="#" onclick="goToPage(${totalPages})">${totalPages}</a>`;
        pagination.appendChild(lastLi);
    }

    // 下一页按钮
    const nextLi = document.createElement('li');
    nextLi.className = `page-item ${currentPage === totalPages ? 'disabled' : ''}`;
    nextLi.innerHTML = `<a class="page-link" href="#" onclick="goToPage(${currentPage + 1})">下一页</a>`;
    pagination.appendChild(nextLi);
}

// 跳转到指定页面
function goToPage(page) {
    if (page < 1 || page > totalPages || page === currentPage) {
        return;
    }

    currentPage = page;
    loadTestsByPage();
}

// 改变每页显示数量
function changePageSize() {
    const newPageSize = parseInt(document.getElementById('pageSizeSelect').value);
    if (newPageSize !== pageSize) {
        pageSize = newPageSize;
        currentPage = 1; // 重置到第一页
        loadTestsByPage();
    }
}
