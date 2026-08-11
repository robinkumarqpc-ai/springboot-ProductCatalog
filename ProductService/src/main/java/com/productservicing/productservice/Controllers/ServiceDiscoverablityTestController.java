package com.productservicing.productservice.Controllers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/service-discoverablity-test")
public class ServiceDiscoverablityTestController {

    private final RestTemplate loadBalancedRestTemplate;

    public ServiceDiscoverablityTestController(@Qualifier("loadBalancedRestTemplate") RestTemplate loadBalancedRestTemplate) {
        this.loadBalancedRestTemplate = loadBalancedRestTemplate;
    }

    @GetMapping("/call-user-auth")
    public String callUserAuth() {
        return loadBalancedRestTemplate.getForObject("http://USERAUTHSERVICE/service-discoverablity-test/ping", String.class);
    }
}