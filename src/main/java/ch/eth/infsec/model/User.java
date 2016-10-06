package ch.eth.infsec.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    private String uid;

    @Getter @Setter
    private String lastname;

    @Getter @Setter
    private String firstname;

    @Getter @Setter
    private String email;

    @Getter @Setter
    private String pwd;
}
