package com.demo.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GlobalContext {
    protected List<User> emailUsers;
    protected User createdUser1;

    public User getEmailUser(int index) {
        return emailUsers.get(index - 1);
    }
}
