package com.ruozedata.ruozeplatform.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    //localhost:9527/platform/test/hello
    @GetMapping("/test/hello")
    public String hello(){
        return "若泽数据,你好.......";
    }
}
