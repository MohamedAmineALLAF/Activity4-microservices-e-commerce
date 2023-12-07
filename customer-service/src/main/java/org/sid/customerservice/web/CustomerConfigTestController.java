package org.sid.customerservice.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RefreshScope
public class CustomerConfigTestController {
    @Value("${global.params.p1}")
    private String p1;
    @Value("${global.params.p2}")
    private String p2;
    @Value("${customer.params.a}")
    private String a;
    @Value("${customer.params.b}")
    private String b;
    @GetMapping("/params")
    public Map<String, String> params(){
        return Map.of("p1",p1,"p2",p2,"a",a,"b",b);
    }
}
