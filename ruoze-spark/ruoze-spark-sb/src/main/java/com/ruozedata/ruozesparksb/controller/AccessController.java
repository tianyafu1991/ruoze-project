package com.ruozedata.ruozesparksb.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * https://www.imooc.com/article/295920
 */
@RestController
@RequestMapping(value = "/access")
public class AccessController {

    @RequestMapping(value = "/{date}")
    public String processAccess(@PathVariable String date) {
        System.out.println(date);
        return date;
    }
}
