package com.hengtiansoft.strategy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class RegisterController {

    @GetMapping("register")
    public void register(String strategyId, String codeId, String userId, String[] accounts)
    {
        System.out.println("strategyId:"+strategyId+",codeId:"+codeId+",userId:"+userId+",accounts:"+ Arrays.toString(accounts));
    }

    @GetMapping("unregister")
    public void unregister(String strategyId)
    {
        System.out.println("strategyId:"+strategyId);
    }
}
