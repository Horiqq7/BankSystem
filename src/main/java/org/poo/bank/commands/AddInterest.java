package org.poo.bank.commands;

import org.poo.bank.Account;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

import java.util.List;
import java.util.Map;

public class AddInterest {
    private final List<User> users;

    public AddInterest(List<User> users) {
        this.users = users;
    }

    public List<Map<String, Object>> addInterest(CommandInput command) {
        String targetIBAN = command.getAccount();
        int currentTimestamp = command.getTimestamp();

        // Căutăm contul țintă
        Account targetAccount = null;
        for (User user : users) {
            targetAccount = user.getAccountByIBAN(targetIBAN);
            if (targetAccount != null) {
                break;
            }
        }

        if (targetAccount == null) {
            // Returnăm eroarea dacă contul nu este găsit
            return List.of(Map.of(
                    "command", "addInterest",
                    "output", Map.of(
                            "description", "Account not found",
                            "timestamp", currentTimestamp
                    ),
                    "timestamp", currentTimestamp
            ));
        }

        // Verificăm dacă este un cont de tip "savings"
        if (!"savings".equalsIgnoreCase(targetAccount.getType())) {
            // Returnăm eroarea dacă contul nu este de tip "savings"
            return List.of(Map.of(
                    "command", "addInterest",
                    "output", Map.of(
                            "description", "This is not a savings account",
                            "timestamp", currentTimestamp
                    ),
                    "timestamp", currentTimestamp
            ));
        }

        // Calculăm dobânda și o adăugăm contului
        double interestAmount = targetAccount.getBalance() * targetAccount.getInterestRate() / 100;
        targetAccount.addFunds(interestAmount);

        // Nu returnăm nimic în caz de succes (nu se adaugă niciun output)
        return List.of();  // Listă goală în caz de succes
    }
}