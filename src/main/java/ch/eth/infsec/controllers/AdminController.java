package ch.eth.infsec.controllers;

import ch.eth.infsec.services.AdminService;
import ch.eth.infsec.services.PKIServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Fabian on 07.10.16.
 */
@Controller
@RequestMapping(path = "/admin")
public class AdminController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String welcome(ModelMap model) throws IOException {

        String issuedPath = PKIServiceImpl.cryptoPath + "/config.properties";

        BufferedReader reader = new BufferedReader(new FileReader(issuedPath));


        StringBuilder sb = new StringBuilder();
        String line = reader.readLine();

        while (line != null) {
            sb.append(line);
            // sb.append(System.lineSeparator());
            line = reader.readLine();
        }

        String content = sb.toString();

        String pattern = "serialNumber=(\\d+)";

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);
        m.find();

        model.addAttribute("counter", m.group(0));

        return "admin";
    }


}
