package org.poo.bank.commands.account_commands;

import org.poo.bank.account.Account;
import org.poo.fileio.CommandInput;
import org.poo.bank.users.User;

import java.util.List;

public class SetAlias {
    private List<User> users;

    public SetAlias(List<User> users) {
        this.users = users;
    }

    public void setAlias(CommandInput command) {
        String email = command.getEmail();   // Email-ul utilizatorului
        String alias = command.getAlias();   // Alias-ul care va fi setat
        String iban = command.getAccount();  // IBAN-ul care va fi asociat alias-ului

        // Căutăm utilizatorul pe baza email-ului
        User user = findUserByEmail(email);
        if (user == null) {
            return; // Utilizatorul nu există, ieșim fără a face nimic
        }

        // Căutăm contul asociat IBAN-ului
        Account account = user.getAccountByIBAN(iban);
        if (account == null) {
            return; // Contul nu există, ieșim fără a face nimic
        }

        // Asociem alias-ul cu IBAN-ul
        user.setAlias(alias, iban);
    }

    private User findUserByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }
}

