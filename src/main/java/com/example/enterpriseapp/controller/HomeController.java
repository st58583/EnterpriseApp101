package com.example.enterpriseapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Vítej v EnterpriseApp!");
        return "index"; // načte šablonu src/main/resources/templates/index.html
    }
}
