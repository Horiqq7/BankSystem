package org.poo.bank.commands;

import org.poo.bank.Account;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

import java.util.List;
import java.util.Map;

public class ChangeInterestRate {

    public List<Map<String, Object>> execute(CommandInput command, List<User> users) {
        String targetIBAN = command.getAccount();
        double newInterestRate = command.getInterestRate();
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
                    "command", "changeInterestRate",
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
                    "command", "changeInterestRate",
                    "output", Map.of(
                            "description", "This is not a savings account",
                            "timestamp", currentTimestamp
                    ),
                    "timestamp", currentTimestamp
            ));
        }

        // Schimbăm rata dobânzii pentru contul de economii
        targetAccount.setInterestRate(newInterestRate);

        // Nu returnăm nimic în caz de succes (nu se adaugă niciun output)
        return List.of();  // Listă goală în caz de succes
    }
}
