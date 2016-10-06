package ch.eth.infsec.model;

import lombok.Getter;
import lombok.Setter;

public class UserForm {

    @Getter @Setter
    public String email;

    @Getter @Setter
    public String firstname;

    @Getter @Setter
    public String lastname;

    @Getter @Setter
    public String password;

    public static class Builder {
        private User user;
        public Builder(User user) {
            this.user = user;
        }

        public UserForm build() {
            UserForm form = new UserForm();
            form.setEmail(user.getEmail());
            form.setFirstname(user.getFirstname());
            form.setLastname(user.getLastname());
            form.setPassword(null);
            return form;
        }
    }

}
