/**
 * AutoTestAI Left Menu JavaScript
 * 左侧菜单功能模块
 */

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 默认展开用例管理菜单
    const testManagementMenu = document.getElementById('test-management');
    const testManagementLink = document.querySelector('[data-target="test-management"]');
    if (testManagementMenu && testManagementLink) {
        testManagementMenu.classList.add('show');
        testManagementLink.classList.add('expanded');
    }
    
    // 更新测试用例数量
    updateTestCount();
});

/**
 * 切换侧边栏折叠状态
 */
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    const collapseIcon = document.getElementById('collapseIcon');
    
    sidebar.classList.toggle('collapsed');
    mainContent.classList.toggle('expanded');
    
    if (sidebar.classList.contains('collapsed')) {
        collapseIcon.className = 'bi bi-chevron-right';
    } else {
        collapseIcon.className = 'bi bi-chevron-left';
    }
}

/**
 * 切换移动端侧边栏
 */
function toggleMobileSidebar() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('show');
}

/**
 * 切换子菜单
 * @param {HTMLElement} element - 点击的菜单元素
 */
function toggleSubMenu(element) {
    const targetId = element.getAttribute('data-target');
    const subMenu = document.getElementById(targetId);
    const arrow = element.querySelector('.nav-arrow');
    
    // 关闭其他子菜单
    document.querySelectorAll('.sub-menu.show').forEach(menu => {
        if (menu.id !== targetId) {
            menu.classList.remove('show');
            const parentLink = document.querySelector(`[data-target="${menu.id}"]`);
            if (parentLink) {
                parentLink.classList.remove('expanded');
            }
        }
    });
    
    // 切换当前子菜单
    if (subMenu) {
        subMenu.classList.toggle('show');
        element.classList.toggle('expanded');
    }
}

/**
 * 加载页面
 * @param {string} url - 页面URL
 * @param {string} category - 分类名称
 * @param {string} pageName - 页面名称
 * @param {HTMLElement} element - 点击的菜单元素
 */
function loadPage(url, category, pageName, element) {
    // 更新面包屑
    const breadcrumbCategory = document.getElementById('breadcrumb-category');
    const breadcrumbPage = document.getElementById('breadcrumb-page');
    
    if (breadcrumbCategory) {
        breadcrumbCategory.textContent = category;
    }
    
    if (breadcrumbPage) {
        breadcrumbPage.textContent = pageName;
    }
    
    // 更新iframe源
    const contentFrame = document.getElementById('contentFrame');
    if (contentFrame) {
        contentFrame.src = url;
    }
    
    // 更新菜单激活状态
    document.querySelectorAll('.sub-nav-link').forEach(link => {
        link.classList.remove('active');
    });
    
    if (element) {
        element.classList.add('active');
    }
    
    // 移动端自动关闭菜单
    if (window.innerWidth <= 768) {
        const sidebar = document.getElementById('sidebar');
        if (sidebar) {
            sidebar.classList.remove('show');
        }
    }
}

/**
 * 更新测试用例数量
 */
function updateTestCount() {
    fetch('/api/tests/statistics')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            const totalTests = data.total || data.totalTests || 0;
            const badge = document.getElementById('test-count');
            if (badge) {
                badge.textContent = totalTests;
            }
        })
        .catch(error => {
            console.error('获取测试统计失败:', error);
            // 设置默认值
            const badge = document.getElementById('test-count');
            if (badge) {
                badge.textContent = '0';
            }
        });
}

/**
 * 退出登录
 */
function logout() {
    if (confirm('确定要退出登录吗？')) {
        // 这里可以添加退出登录的逻辑
        alert('退出登录功能待实现');
        // 实际项目中可以这样实现：
        // window.location.href = '/logout';
    }
}

/**
 * 监听窗口大小变化
 */
window.addEventListener('resize', function() {
    if (window.innerWidth > 768) {
        const sidebar = document.getElementById('sidebar');
        if (sidebar) {
            sidebar.classList.remove('show');
        }
    }
});

/**
 * 定期更新测试数量
 */
setInterval(updateTestCount, 30000); // 每30秒更新一次

/**
 * 监听iframe加载完成事件
 */
document.addEventListener('DOMContentLoaded', function() {
    const contentFrame = document.getElementById('contentFrame');
    if (contentFrame) {
        contentFrame.addEventListener('load', function() {
            console.log('页面加载完成:', this.src);
        });
        
        contentFrame.addEventListener('error', function() {
            console.error('页面加载失败:', this.src);
        });
    }
});

/**
 * 工具函数：显示加载状态
 */
function showLoading() {
    // 可以在这里添加加载动画
    console.log('正在加载...');
}

/**
 * 工具函数：隐藏加载状态
 */
function hideLoading() {
    // 可以在这里隐藏加载动画
    console.log('加载完成');
}

/**
 * 工具函数：显示错误信息
 * @param {string} message - 错误信息
 */
function showError(message) {
    console.error('错误:', message);
    // 可以在这里添加错误提示UI
}

/**
 * 工具函数：显示成功信息
 * @param {string} message - 成功信息
 */
function showSuccess(message) {
    console.log('成功:', message);
    // 可以在这里添加成功提示UI
}

// 导出函数供全局使用
window.leftMenuModule = {
    toggleSidebar,
    toggleMobileSidebar,
    toggleSubMenu,
    loadPage,
    updateTestCount,
    logout,
    showLoading,
    hideLoading,
    showError,
    showSuccess
}; 