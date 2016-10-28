package ch.eth.infsec.services;


import ch.eth.infsec.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsX509ServiceImpl implements UserDetailsService {

    @Autowired
    UserService userService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userService.findByUid(s);
        if (user == null) {
            throw new UsernameNotFoundException("User " + s + " not found!");
        }
        return new ch.eth.infsec.model.UserDetails(user);
    }



}
