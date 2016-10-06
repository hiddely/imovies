package ch.eth.infsec.controllers;

import ch.eth.infsec.model.UserDetails;
import ch.eth.infsec.services.PKIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/certificate")
public class CertificateController {

    @Autowired
    PKIService pkiService;

    @RequestMapping(path = "/issue")
    public String issue() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        pkiService.issueCertificate(userDetails.getUser());

        return "issue";
    }


}
