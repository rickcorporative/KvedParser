package com.demo.data;

import com.demo.data.abstractClasses.AbstractPerson;
import com.demo.utils.MailReader;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Setter
@Getter
public class User extends AbstractPerson {
    public MailReader mailReader;
    public String password;

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = new String(Base64.getDecoder().decode(password), StandardCharsets.UTF_8);
    }

    public User(String email, String password, String appPassword) {
        this(email, password);
        this.appPassword = new String(Base64.getDecoder().decode(appPassword), StandardCharsets.UTF_8);
        mailReader = new MailReader(email, this.appPassword);
    }

    public void setPasswordBase64(String password) {
        this.password = new String(Base64.getDecoder().decode(password), StandardCharsets.UTF_8);
    }
}
