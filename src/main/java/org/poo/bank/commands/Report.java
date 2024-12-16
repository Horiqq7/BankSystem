package org.poo.bank.commands;

import org.poo.bank.Account;
import org.poo.bank.Transaction;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Report {

    private List<User> users;

    public Report(List<User> users) {
        this.users = users; // Lista de utilizatori va fi transmisă ca parametru
    }

    public Map<String, Object> report(CommandInput command) {
        // Obținem datele din input
        String accountIBAN = command.getAccount();
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        int timestamp = command.getTimestamp(); // Folosim timpul curent dacă nu e oferit

        // Căutăm contul corespunzător
        Account account = null;
        for (User user : users) {
            account = user.getAccountByIBAN(accountIBAN);
            if (account != null) {
                break;
            }
        }

        if (account == null) {
            // Dacă contul nu există, returnăm un mesaj de eroare
            return Map.of(
                    "command", "report",
                    "timestamp", timestamp,
                    "output", Map.of(
                            "description", "Account not found",
                            "timestamp", timestamp
                    )
            );
        }

        // Filtrăm tranzacțiile pe baza intervalului și, dacă e cazul, tipului de cont
        List<Transaction> transactions = account.getTransactions();
        List<Map<String, Object>> filteredTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            boolean isInTimeRange = transaction.getTimestamp() >= startTimestamp && transaction.getTimestamp() <= endTimestamp;
            boolean isValidTransaction = true;

            // Dacă e un cont de economii, filtrăm doar tranzacțiile relevante
            if ("savings".equals(account.getType())) {
                isValidTransaction = "interest".equals(transaction.getTransactionType());
            }

            if (isInTimeRange && isValidTransaction) {
                filteredTransactions.add(transaction.toMap());
            }
        }

        // Creăm structura raportului
        Map<String, Object> reportDetails = new HashMap<>();
        reportDetails.put("IBAN", accountIBAN);
        reportDetails.put("balance", account.getBalance());
        reportDetails.put("currency", account.getCurrency());
        reportDetails.put("transactions", filteredTransactions); // Adăugăm tranzacțiile filtrate

        // Output-ul final
        return Map.of(
                "command", "report",
                "timestamp", timestamp,
                "output", reportDetails
        );
    }
}