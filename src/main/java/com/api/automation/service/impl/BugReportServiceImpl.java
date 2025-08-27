package com.api.automation.service.impl;

import com.api.automation.service.BugReportService;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;

import java.util.Map;

/**
 * @author liang
 * @className BugReportServiceImpl
 * @description TODO
 * @date 2025/8/27 21:47
 */
@Service
public class BugReportServiceImpl implements BugReportService {
    @Override
    public Map<String, Object> createData() {
        String curl = "curl --location 'https://itwork-test.yonghui.cn/api/test-workbench/v1/bug/list_by_options' \\\n" +
                "--header 'sec-ch-ua-platform: \"macOS\"' \\\n" +
                "--header 'Authorization: bearer f713a92d-d3aa-4257-9023-87f36338e79a' \\\n" +
                "--header 'Referer: https://itwork-test.yonghui.cn/bug-manage' \\\n" +
                "--header 'sec-ch-ua: \"Not;A=Brand\";v=\"99\", \"Google Chrome\";v=\"139\", \"Chromium\";v=\"139\"' \\\n" +
                "--header 'sec-ch-ua-mobile: ?0' \\\n" +
                "--header 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36' \\\n" +
                "--header 'Accept: application/json, text/plain, */*' \\\n" +
                "--header 'Content-Type: application/json' \\\n" +
                "--data '{\"bugUserList\":[{\"userType\":\"Test\",\"iamUserId\":24306},{\"userType\":\"Test\",\"iamUserId\":21199},{\"userType\":\"Test\",\"iamUserId\":25710},{\"userType\":\"Test\",\"iamUserId\":23435},{\"userType\":\"Test\",\"iamUserId\":19326},{\"userType\":\"Test\",\"iamUserId\":18954},{\"userType\":\"Test\",\"iamUserId\":18901},{\"userType\":\"Test\",\"iamUserId\":23120}],\"startTime\":\"2025-08-01 00:00:00\",\"endTime\":\"2025-08-31 23:59:59\",\"pageNo\":0,\"pageSize\":1000}'";


        return null;
    }
}
