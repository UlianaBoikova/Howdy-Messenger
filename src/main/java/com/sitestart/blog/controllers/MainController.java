package com.sitestart.blog.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 The main controller that runs the site
 */
@Controller
public class MainController {

    /**
      Returns main page of the site
      @return "howdy-main.html"
     */
    @GetMapping("/")
    public String home() {
        return "howdy-main";
    }

}