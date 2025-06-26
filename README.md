# AutoTestAI

一个基于Spring Boot的AI驱动API自动化测试平台，支持批量执行curl命令并实时查看测试结果。

## 功能特性

- ✅ **批量执行curl命令** - 支持单个或批量执行API测试
- ✅ **实时看板** - WebSocket实时推送测试状态更新
- ✅ **智能判断** - 自动判断返回结果中code=0表示成功
- ✅ **统计监控** - 实时统计成功/失败/执行中的测试数量
- ✅ **测试管理** - 添加、编辑、删除API测试用例
- ✅ **详细报告** - 查看测试执行详情和错误信息
- ✅ **响应式UI** - 现代化的Bootstrap界面

## 技术栈

- **后端**: Spring Boot 2.7.14
- **前端**: Bootstrap 5 + JavaScript
- **实时通信**: WebSocket (STOMP)
- **构建工具**: Maven

## 快速开始

### 环境要求

- Java 8+
- Maven 3.6+
- curl命令可用

### 安装运行

1. **克隆项目**
```bash
git clone <repository-url>
cd autoTestAi
```

2. **编译运行**
```bash
mvn clean package
java -jar target/autoTestAi-1.0.0.jar
```

3. **访问应用**
```
http://localhost:8080
```

### 使用说明

1. **初始化示例数据**
   - 点击"初始化示例数据"按钮创建示例测试用例

2. **添加API测试**
   - 点击"添加测试"按钮
   - 填写测试名称、curl命令和描述
   - 保存测试用例

3. **执行测试**
   - 单个执行：点击测试项右侧的播放按钮
   - 批量执行：点击"执行所有测试"按钮

4. **查看结果**
   - 实时查看测试状态变化
   - 点击眼睛图标查看详细结果
   - 查看统计信息

## API接口

### 测试管理

- `GET /api/tests` - 获取所有测试
- `GET /api/tests/{id}` - 获取单个测试
- `POST /api/tests` - 创建测试
- `PUT /api/tests/{id}` - 更新测试
- `DELETE /api/tests/{id}` - 删除测试

### 测试执行

- `POST /api/tests/{id}/execute` - 执行单个测试
- `POST /api/tests/batch-execute` - 批量执行测试
- `POST /api/tests/execute-all` - 执行所有启用的测试

### 统计信息

- `GET /api/tests/statistics` - 获取统计信息
- `POST /api/tests/init-sample-data` - 初始化示例数据

## WebSocket事件

- `/topic/api-test-update` - API测试状态更新
- `/topic/statistics-update` - 统计信息更新
- `/topic/batch-execution` - 批量执行状态通知

## 配置说明

### application.yml

```yaml
server:
  port: 8080

api:
  automation:
    timeout: 30000        # 默认超时时间(毫秒)
    max-concurrent: 10    # 最大并发数
```

## 测试用例示例

### 基本GET请求
```bash
curl -X GET http://localhost:8080/api/health
```

### POST请求带JSON数据
```bash
curl -X POST http://localhost:8080/api/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"test","password":"123456"}'
```

### 带认证的请求
```bash
curl -X GET http://localhost:8080/api/user/info \
  -H 'Authorization: Bearer token123'
```

## 成功判断逻辑

系统通过以下条件判断测试是否成功：

1. curl命令执行成功（退出码为0）
2. 响应内容包含 `"code":0`

```json
{
  "code": 0,
  "message": "success",
  "data": {...}
}
```

## 项目结构

```
src/
├── main/
│   ├── java/com/api/automation/
│   │   ├── AutoTestAiApplication.java       # 启动类
│   │   ├── controller/
│   │   │   └── ApiTestController.java       # REST API控制器
│   │   ├── service/
│   │   │   ├── ApiTestService.java          # 测试管理服务
│   │   │   ├── CurlExecutorService.java     # curl执行服务
│   │   │   └── WebSocketService.java        # WebSocket服务
│   │   ├── model/
│   │   │   └── ApiTest.java                 # 测试实体类
│   │   └── config/
│   │       └── WebSocketConfig.java         # WebSocket配置
│   └── resources/
│       ├── static/
│       │   ├── index.html                   # 主页面
│       │   └── js/
│       │       └── dashboard.js             # 前端逻辑
│       └── application.yml                  # 配置文件
```

## 扩展功能

### 定时执行
可以添加定时任务功能，定期执行测试用例：

```java
@Scheduled(fixedRate = 300000) // 每5分钟执行一次
public void scheduledExecution() {
    apiTestService.executeAllEnabledApiTests();
}
```

### 数据库存储
当前使用内存存储，生产环境建议使用数据库：

```java
@Repository
public interface ApiTestRepository extends JpaRepository<ApiTest, String> {
    List<ApiTest> findByEnabledTrue();
}
```

### 邮件通知
可以添加邮件通知功能，在测试失败时发送通知。

## 故障排除

### 常见问题

1. **curl命令执行失败**
   - 检查curl是否已安装
   - 确认命令语法正确
   - 检查网络连接

2. **WebSocket连接失败**
   - 检查防火墙设置
   - 确认端口8080未被占用

3. **测试结果显示异常**
   - 检查API接口是否可访问
   - 确认返回格式符合预期

### 日志查看

```bash
# 查看应用日志
tail -f logs/application.log

# 查看错误日志
grep ERROR logs/application.log
```

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交Issue或联系开发团队。 