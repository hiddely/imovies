package ch.eth.infsec.controllers;

import ch.eth.infsec.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Fabian on 07.10.16.
 */
@Controller
@RequestMapping(path = "/admin")
public class AdminController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String welcome() {
        return "admin";
    }


}
