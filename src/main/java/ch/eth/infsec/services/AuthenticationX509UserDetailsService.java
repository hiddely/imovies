package ch.eth.infsec.services;

import ch.eth.infsec.model.User;
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

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        X509Certificate certificate = (X509Certificate)token.getCredentials();

        System.out.println("Certificate: " + certificate.getSerialNumber().toString());

        String subjectDN = certificate.getSubjectDN().toString();
        final String patternCN = "CN=(.*?),";
        final String patternOU = "OU=(.*?),";

        String cn = extractFromDN(subjectDN, patternCN);
        User user = userService.findByUid(cn);
        if (user == null) {
            throw new UsernameNotFoundException("User with UID " + cn + " was not found.");
        }
        ch.eth.infsec.model.UserDetails userDetails = new ch.eth.infsec.model.UserDetails(user);

        // check if user is admin
        String userType = extractFromDN(subjectDN, patternOU);
        if (userType.equals("Admin")) {
            System.out.println("User is admin!");

            userDetails.setAuthorities(AuthorityUtils
                    .commaSeparatedStringToAuthorityList("ROLE_USER,ROLE_ADMIN"));
        } else if (userType.equals("Personal")) {
            userDetails.setAuthorities(AuthorityUtils
                    .commaSeparatedStringToAuthorityList("ROLE_USER"));
        } else {
            throw new InvalidCertificateException("User is of invalid type");
        }

        return userDetails;

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
