#!/bin/bash

echo "=== AutoTestAI API自动化测试平台启动脚本 ==="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装Java 8或更高版本"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请先安装Maven 3.6或更高版本"
    exit 1
fi

# 检查curl环境
if ! command -v curl &> /dev/null; then
    echo "警告: 未找到curl命令，可能影响测试执行"
fi

echo "环境检查完成"

# 编译项目
echo "正在编译项目..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "编译失败，请检查错误信息"
    exit 1
fi

echo "编译成功"

# 启动应用
echo "正在启动应用..."
echo "访问地址: http://localhost:8080"
echo "按 Ctrl+C 停止应用"

java -jar target/autoTestAi-1.0.0.jar 