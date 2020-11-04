package com.ruozedata.ruozesbspark.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/spark")
public class AccessController {



    @PostMapping(value = "/access")
    public void processAccess(String date){

    }
}
