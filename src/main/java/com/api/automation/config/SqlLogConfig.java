package com.api.automation.config;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * SQL日志配置类
 * 用于自定义SQL日志输出格式，使其更加醒目
 */
@Configuration
public class SqlLogConfig {

    @PostConstruct
    public void init() {
        // 设置自定义的SQL日志实现
        LogFactory.useCustomLogging(CustomSqlLogger.class);
    }

    /**
     * 自定义SQL日志实现
     */
    public static class CustomSqlLogger implements Log {
        
        private static final String SQL_PREFIX = "\n" +
                "╔══════════════════════════════════════════════════════════════════════════════════════════════════════╗\n" +
                "║                                    SQL EXECUTION LOG                                                 ║\n" +
                "╠══════════════════════════════════════════════════════════════════════════════════════════════════════╣";
        
        private static final String SQL_SUFFIX = "\n" +
                "╚══════════════════════════════════════════════════════════════════════════════════════════════════════╝\n";
        
        private static final String PREPARE_PREFIX = "║ 🚀 PREPARING: ";
        private static final String PARAM_PREFIX = "║ 📝 PARAMETERS: ";
        private static final String RESULT_PREFIX = "║ ✅ RESULT: ";
        private static final String TIME_PREFIX = "║ ⏱️  EXECUTION TIME: ";
        private static final String ERROR_PREFIX = "║ ❌ ERROR: ";

        // 添加必要的构造函数
        public CustomSqlLogger(String clazz) {
            // 构造函数，MyBatis会传入类名
        }

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public boolean isTraceEnabled() {
            return true;
        }

        @Override
        public void error(String s, Throwable e) {
            System.err.println(SQL_PREFIX);
            System.err.println(ERROR_PREFIX + s);
            if (e != null) {
                e.printStackTrace();
            }
            System.err.println(SQL_SUFFIX);
        }

        @Override
        public void error(String s) {
            System.err.println(SQL_PREFIX);
            System.err.println(ERROR_PREFIX + s);
            System.err.println(SQL_SUFFIX);
        }

        @Override
        public void debug(String s) {
            if (s.startsWith("==>  Preparing:")) {
                System.out.println(SQL_PREFIX);
                System.out.println(PREPARE_PREFIX + s.substring(15));
            } else if (s.startsWith("==> Parameters:")) {
                System.out.println(PARAM_PREFIX + s.substring(15));
            } else if (s.startsWith("<==    Columns:")) {
                System.out.println(RESULT_PREFIX + "Columns: " + s.substring(15));
            } else if (s.startsWith("<==        Row:")) {
                System.out.println(RESULT_PREFIX + "Row: " + s.substring(15));
            } else if (s.startsWith("<==      Total:")) {
                System.out.println(RESULT_PREFIX + "Total: " + s.substring(15));
                System.out.println(TIME_PREFIX + System.currentTimeMillis() + "ms");
                System.out.println(SQL_SUFFIX);
            } else if (s.startsWith("Closing non transactional SqlSession")) {
                // 忽略关闭会话的日志
            } else if (s.startsWith("Creating a new SqlSession")) {
                // 忽略创建会话的日志
            } else if (s.startsWith("SqlSession") && s.contains("was not registered")) {
                // 忽略会话注册日志
            } else if (s.startsWith("JDBC Connection") && s.contains("will not be managed")) {
                // 忽略连接管理日志
            } else {
                // 其他SQL相关日志
                System.out.println("║ ℹ️  " + s);
            }
        }

        @Override
        public void trace(String s) {
            debug(s);
        }

        @Override
        public void warn(String s) {
            System.out.println("║ ⚠️  WARNING: " + s);
        }
    }
} 