package org.poo.bank.commands;

import org.poo.bank.Account;
import org.poo.bank.Transaction;
import org.poo.fileio.CommandInput;
import org.poo.users.User;
import org.poo.utils.Utils;

import java.util.List;

public class AddAccount {
    private List<User> users;

    public AddAccount(List<User> users) {
        this.users = users;
    }

    public void addAccount(CommandInput command) {
        // Căutăm utilizatorul pe baza email-ului
        User user = users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(command.getEmail()))
                .findFirst()
                .orElse(null);

        if (user == null) {
            System.out.println("User not found: " + command.getEmail());
            return;
        }

        // Generăm un IBAN nou pentru contul utilizatorului
        String iban = Utils.generateIBAN();

        // Creăm contul cu detaliile din command
        Account account = new Account(iban, command.getCurrency(), command.getAccountType());
        user.addAccount(account);

        // Înregistrăm tranzacția de creare a contului
        Transaction transaction = new Transaction(
                command.getTimestamp(),
                "New account created",
                null,
                iban,
                0,
                command.getCurrency(),
                "other",
                null,
                null,
                null,
                null,
                null,
                "addAccount"
        );

        user.addTransaction(transaction);
        account.addTransaction(transaction);
    }
}
