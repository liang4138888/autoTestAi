# AutoTestAI CSS & JavaScript 文件结构

## 📁 文件组织结构

```
src/main/resources/static/
├── css/
│   ├── common.css          # 通用样式文件
│   └── left-menu.css       # 左侧菜单专用样式
├── js/
│   └── left-menu.js        # 左侧菜单功能脚本
└── *.html                  # HTML页面文件
```

## 🎨 CSS 文件说明

### 1. `css/common.css` - 通用样式文件
**用途**: 包含所有页面共用的样式
**内容**:
- 基础样式重置
- 状态颜色定义
- 卡片样式（统计卡片、分组卡片等）
- 按钮和表单样式
- 加载动画
- 工具提示
- 响应式设计

**适用页面**: 
- `index.html`
- `test-groups.html` 
- `menu-test.html`
- 其他子页面

### 2. `css/left-menu.css` - 左侧菜单样式
**用途**: 专门为左侧菜单设计的样式
**内容**:
- 侧边栏布局和动画
- 菜单项样式和交互效果
- 折叠/展开功能样式
- 用户信息区域样式
- 面包屑导航样式
- 移动端适配

**适用页面**: 
- `left-menu.html`

## 🔧 JavaScript 文件说明

### 1. `js/left-menu.js` - 左侧菜单功能脚本
**用途**: 左侧菜单的所有交互功能
**主要函数**:
- `toggleSidebar()` - 切换侧边栏折叠状态
- `toggleMobileSidebar()` - 移动端菜单切换
- `toggleSubMenu(element)` - 子菜单展开/折叠
- `loadPage(url, category, pageName, element)` - 页面加载
- `updateTestCount()` - 更新测试用例数量
- `logout()` - 退出登录

**适用页面**: 
- `left-menu.html`

## 📄 HTML 文件更新说明

### 1. `left-menu.html` - 主框架页面
**更新内容**:
- ✅ 移除所有内联CSS（约440行）
- ✅ 移除所有内联JavaScript（约120行）
- ✅ 引入外部CSS文件
- ✅ 引入外部JavaScript文件

### 2. `index.html` - 用例列表页面
**更新内容**:
- ✅ 移除内联CSS（约160行）
- ✅ 引入通用CSS文件
- ✅ 保持原有功能不变

### 3. `test-groups.html` - 用例分组页面
**更新内容**:
- ✅ 移除内联CSS（约30行）
- ✅ 引入通用CSS文件

### 4. `menu-test.html` - 菜单测试页面
**更新内容**:
- ✅ 部分移除内联CSS
- ✅ 引入通用CSS文件
- ✅ 保留页面特有样式

## 🚀 优势和好处

### 1. **代码复用**
- 通用样式只需维护一份
- 减少代码重复，提高开发效率

### 2. **维护性**
- 样式集中管理，修改更方便
- 功能模块化，便于调试和扩展

### 3. **性能优化**
- 浏览器可以缓存CSS和JS文件
- 减少HTML文件大小，加载更快

### 4. **团队协作**
- 前端开发者可以专注于样式文件
- 后端开发者可以专注于HTML结构

### 5. **扩展性**
- 新增页面时可以直接引用现有样式
- 便于添加新的功能模块

## 📋 使用指南

### 引入CSS文件
```html
<!-- 基础依赖 -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">

<!-- 项目样式 -->
<link href="css/common.css" rel="stylesheet">        <!-- 所有页面都需要 -->
<link href="css/left-menu.css" rel="stylesheet">     <!-- 仅主框架页面需要 -->
```

### 引入JavaScript文件
```html
<!-- 基础依赖 -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>

<!-- 项目脚本 -->
<script src="js/left-menu.js"></script>              <!-- 仅主框架页面需要 -->
```

### 创建新页面
1. 复制现有页面的HTML结构
2. 引入 `css/common.css`
3. 根据需要添加页面特有样式
4. 如需JavaScript功能，创建对应的JS文件

## 🔄 迁移前后对比

### 迁移前
- `left-menu.html`: 813行 → 现在: 253行 (减少69%)
- `index.html`: 506行 → 现在: 340行 (减少33%)
- 内联样式和脚本混杂在HTML中
- 代码重复，难以维护

### 迁移后
- HTML文件更简洁，结构清晰
- CSS和JS模块化管理
- 便于维护和扩展
- 提高了代码复用性

## 📝 注意事项

1. **文件路径**: 确保CSS和JS文件的路径正确
2. **加载顺序**: 先加载CSS，再加载JavaScript
3. **缓存策略**: 生产环境建议添加版本号防止缓存问题
4. **浏览器兼容**: 已考虑主流浏览器兼容性
5. **响应式设计**: 所有样式都支持移动端适配

---

*文档更新时间: 2024年1月* 