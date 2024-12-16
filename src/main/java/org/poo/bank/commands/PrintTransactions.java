package org.poo.bank.commands;

import org.poo.bank.Transaction;
import org.poo.fileio.CommandInput;
import org.poo.users.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrintTransactions {

    private List<User> users;

    public PrintTransactions(List<User> users) {
        this.users = users;
    }

    public List<Map<String, Object>> printTransactions(CommandInput command) {
        String email = command.getEmail();
        User user = findUserByEmail(email); // Asigură-te că această metodă există
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        List<Transaction> transactions = user.getTransactions();
        List<Map<String, Object>> outputTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            outputTransactions.add(transaction.toMap()); // Asigură-te că Transaction are metoda toMap
        }
        return outputTransactions;
    }

    private User findUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
}
