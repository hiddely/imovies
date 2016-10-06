package ch.eth.infsec.controllers;

import ch.eth.infsec.model.User;
import ch.eth.infsec.model.UserDetails;
import ch.eth.infsec.model.UserForm;
import ch.eth.infsec.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController {

    @Autowired
    UserService userService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String welcome() {
        return "home";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public String account(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserForm.Builder formBuilder = new UserForm.Builder(userDetails.getUser());
        model.addAttribute("account", formBuilder.build());
        return "account";
    }

    @RequestMapping(value = "/account", method = RequestMethod.POST)
    public String saveAccount(Model model, UserForm userForm) {
        User user = userService.updateUser(userForm);
        model.addAttribute("account", new UserForm.Builder(user).build());
        return "account";
    }

    public String greeting() {
        return "greeting";
    }

}
