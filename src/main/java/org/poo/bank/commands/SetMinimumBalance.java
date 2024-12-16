package org.poo.bank.commands;

import org.poo.bank.Account;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

import java.util.List;

public class SetMinimumBalance {
    private final List<User> users;

    public SetMinimumBalance(List<User> users) {
        this.users = users;
    }

    public void setMinimumBalance(CommandInput command) {
        String iban = command.getAccount(); // IBAN-ul contului
        double minimumBalance = command.getAmount(); // Noua balanță minimă

        // Căutăm contul în lista utilizatorilor
        for (User user : users) {
            Account account = user.getAccountByIBAN(iban); // Găsim contul pe baza IBAN-ului
            if (account != null) {
                // Setăm balanța minimă
                account.setMinimumBalance(minimumBalance);
                return;
            }
        }

        // Dacă nu găsim contul, aruncăm o excepție
        throw new IllegalArgumentException("Invalid account or user does not own the account.");
    }
}