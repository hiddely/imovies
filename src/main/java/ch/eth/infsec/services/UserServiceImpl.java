package ch.eth.infsec.services;


import ch.eth.infsec.model.User;
import ch.eth.infsec.model.UserDetails;
import ch.eth.infsec.model.UserForm;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User updateUser(UserForm userForm) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User loggedIn = userDetails.getUser();
        loggedIn.setEmail(userForm.getEmail());
        loggedIn.setFirstname(userForm.getFirstname());
        loggedIn.setLastname(userForm.getLastname());
        if (userForm.getPassword() != null && userForm.getPassword().length() > 0) {
            Sha1PasswordEncoder encoder = new Sha1PasswordEncoder();
            loggedIn.setPwd(encoder.encode(userForm.getPassword()));
        }
        userRepository.save(loggedIn);
        return loggedIn;
    }


}
