package com.hengtiansoft.strategy.entrypoint;

import org.springframework.stereotype.Component;

@Component
public class EntryPoint {

    public String matchTest(String a, String b)
    {
        return a + b;
    }
}
