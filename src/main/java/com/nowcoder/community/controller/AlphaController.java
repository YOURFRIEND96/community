package com.nowcoder.community.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;

@Controller
public class AlphaController {

    @RequestMapping("/hello")
    @ResponseBody
    public String sayhello() {
        return "hello";
    }


}


