package org.poo.bank.commands;

import org.poo.users.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PrintUsers {
    private final List<User> users;

    public PrintUsers(List<User> users) {
        this.users = users;
    }

    public List<Map<String, Object>> execute() {
        return users.stream()
                .map(User::toMap)
                .collect(Collectors.toList());
    }
}