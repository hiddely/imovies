package ch.eth.infsec.controllers;

import ch.eth.infsec.model.CADetails;
import ch.eth.infsec.services.pki.PKIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.cert.X509Certificate;

@Controller
@RequestMapping(path = "/admin")
public class AdminController {

    @Autowired
    PKIService pkiService;

    @RequestMapping(value = { "/", ""}, method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String welcome(Model model) {

        model.addAttribute("details",
                new CADetails(
                        pkiService.numberOfCertificates(),
                        pkiService.numberOfCRL(),
                        pkiService.currentSerialNumber()
                )
        );

        model.addAttribute("certificates",
                pkiService.getAllCertificates()
        );

        return "admin";
    }

}
