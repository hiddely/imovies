package ch.eth.infsec.services;


import ch.eth.infsec.model.User;
import ch.eth.infsec.model.UserForm;

public interface UserService {

    void saveUser(User user);

    User findByEmail(String email);

    User findByUid(String uid);

    User updateUser(UserForm userForm);

}
