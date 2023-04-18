package web.instaweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping("/")
    public String home() {
        return "home";
    }


    @RequestMapping("/pages/jq")
    public String jquery() {
        return "pages/testSort";
    }
}
