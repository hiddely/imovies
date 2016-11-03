package ch.eth.infsec.controllers;

import ch.eth.infsec.model.UserDetails;
import ch.eth.infsec.services.pki.PKIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

@Controller
@RequestMapping(path = "/certificate")
public class CertificateController {

    @Autowired
    PKIService pkiService;

    @RequestMapping(path = "/issue", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<InputStreamResource> issue(
            @RequestParam(value="password", required=true) String password) throws FileNotFoundException {

        if (password.length() < 5) {
            // trhow error
        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String path = pkiService.issueCertificate(userDetails.getUser(), password, null);
        File file = new File(path);

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(new MediaType("application", "x-pkcs12"));
        respHeaders.setContentLength(file.length());
        respHeaders.setContentDispositionFormData("attachment", "authentication.p12");

        InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
        return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
    }

    @RequestMapping(path = "/revoke")
    public String revoke() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (pkiService.revokeCertificate(userDetails.getUser())) {
            return "redirect:/account?q=Your certificate has been revoked";
        }
        return "redirect:/account?q=Your certificate was not revoked, because you don't have any";


    }


}
