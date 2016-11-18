package ch.eth.infsec.services;

import ch.eth.infsec.model.User;
import ch.eth.infsec.services.pki.PKIService;
import org.bouncycastle.asn1.x500.X500Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthenticationX509UserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    @Autowired
    UserService userService;

    @Autowired
    PKIService pkiService;

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        X509Certificate certificate = (X509Certificate)token.getCredentials();

        if (!pkiService.isValid(certificate)) {
            //throw new InvalidCertificateException("Certificate is invalid.");
            throw new UsernameNotFoundException("Certificate is invalid.");
        }

        String subjectDN = certificate.getSubjectDN().toString();
        final String patternCN = "CN=(.*?),";
        final String patternOU = "OU=(.*?),";



        // check if user is admin
        String userType = extractFromDN(subjectDN, patternOU);
        if (userType.equals("Admin")) {
            System.out.println("User is admin!");

            User adminUser = new User();
            adminUser.setFirstname("iMovies");
            adminUser.setLastname("Admin");
            adminUser.setEmail("admin@imovies.ch");

            ch.eth.infsec.model.UserDetails userDetails = new ch.eth.infsec.model.UserDetails(adminUser);

            userDetails.setAuthorities(AuthorityUtils
                    .commaSeparatedStringToAuthorityList("ROLE_USER,ROLE_ADMIN"));

            return userDetails;
        } else if (userType.equals("Personal")) {

            String cn = extractFromDN(subjectDN, patternCN);
            User user = userService.findByUid(cn);
            if (user == null) {
                throw new UsernameNotFoundException("User with UID " + cn + " was not found.");
            }

            ch.eth.infsec.model.UserDetails userDetails = new ch.eth.infsec.model.UserDetails(user);

            userDetails.setAuthorities(AuthorityUtils
                    .commaSeparatedStringToAuthorityList("ROLE_USER"));

            return userDetails;
        } else {
            throw new InvalidCertificateException("User is of invalid type");
        }

    }

    private String extractFromDN(String dn, String patternString) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(dn);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new InvalidCertificateException("Error occurred while extracting" + patternString + " from " + dn);
    }
}
