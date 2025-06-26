-- 修改 api_test 表的 result 字段为 TEXT 类型
ALTER TABLE api_test MODIFY COLUMN result TEXT;

-- 修改 api_test_record 表的 result 字段为 TEXT 类型
ALTER TABLE api_test_record MODIFY COLUMN result TEXT;

-- 显示修改后的表结构
DESCRIBE api_test;
DESCRIBE api_test_record; 