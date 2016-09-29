package ch.eth.infsec.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    private String uid;

    private String lastname;

    private String firstname;

    private String email;

    private String pwd;
}
