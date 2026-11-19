package com.example.smartdagligvare.Controllers;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home(Principal principal) {

        if (principal == null) {
            return "redirect:/welcome.html";
        }

        return "redirect:/index.html";
    }
}