package org.poo.bank.commands.account_commands;

import org.poo.bank.account.Account;
import org.poo.bank.transaction.Transaction;
import org.poo.fileio.CommandInput;
import org.poo.bank.users.User;

import java.util.List;
import java.util.Map;

public class ChangeInterestRate {

    public List<Map<String, Object>> execute(CommandInput command, List<User> users) {
        String targetIBAN = command.getAccount();
        double newInterestRate = command.getInterestRate();
        int currentTimestamp = command.getTimestamp();

        Account targetAccount = null;
        User targetUser = null;  // Vom ține referința la utilizator
        for (User user : users) {
            targetAccount = user.getAccountByIBAN(targetIBAN);
            if (targetAccount != null) {
                targetUser = user;  // Salvăm utilizatorul care deține contul
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

        // Creăm tranzacția pentru schimbarea ratei dobânzii
        Transaction newInterestRateTransaction = new Transaction(
                currentTimestamp, // Incrementăm timestamp-ul pentru actualizarea cardului
                "Interest rate of the account changed to " + newInterestRate,
                null,
                null, // Nicio destinație pentru cardul nou
                0, // Nu se face transfer
                null,
                "other",
                null,
                null,
                null, // Nu există comerciant
                null,
                null,
                "changeInterestRate"
        );

        // Adăugăm tranzacția în lista de tranzacții a utilizatorului
        if (targetUser != null) {
            targetUser.addTransaction(newInterestRateTransaction); // Adăugăm tranzacția utilizatorului
        }

        // Nu returnăm nimic în caz de succes (nu se adaugă niciun output)
        return List.of();  // Listă goală în caz de succes
    }

}
