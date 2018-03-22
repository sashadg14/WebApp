package web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Control {
    @RequestMapping("/agent")
    public String index() {
     //   System.out.println("asdsad");
        return "agent/user.html";
    }

    @RequestMapping("/user")
    public String usersPage2() {
        return "alpha/user.html";
    }
}
