package org.poo.bank.commands;

import org.poo.bank.Account;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

import java.util.List;
import java.util.Map;

public class SpendingsReport {

    public List<Map<String, Object>> generateSpendingsReport(CommandInput command, List<User> users) {
        String accountIBAN = command.getAccount();
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        int currentTimestamp = command.getTimestamp();

        // Căutăm contul corespunzător
        Account account = null;
        for (User user : users) {
            account = user.getAccountByIBAN(accountIBAN);
            if (account != null) {
                break;
            }
        }

        if (account == null) {
            // Dacă contul nu există, returnăm eroarea cerută
            return List.of(Map.of(
                    "command", "spendingsReport",
                    "output", Map.of(
                            "description", "Account not found",
                            "timestamp", currentTimestamp
                    ),
                    "timestamp", currentTimestamp
            ));
        }

        // Generăm raportul pentru cheltuieli
        Map<String, Object> report = account.generateSpendingsReport(startTimestamp, endTimestamp);

        // Ordonați comercianții alfabetic, punând literele mari înaintea celor mici
        List<Map<String, Object>> commerciants = (List<Map<String, Object>>) report.get("commerciants");
        if (commerciants != null) {
            // Sortare personalizată: litere mari înainte de cele mici
            commerciants.sort((a, b) -> {
                String firstCommerciant = (String) a.get("commerciant");
                String secondCommerciant = (String) b.get("commerciant");

                // Comparăm literele mari (A-Z) înainte de cele mici (a-z)
                return firstCommerciant.compareTo(secondCommerciant);
            });
        }

        // Returnăm rezultatul în formatul cerut
        return List.of(Map.of(
                "command", "spendingsReport",
                "output", report,
                "timestamp", currentTimestamp
        ));
    }
}