package org.poo.bank.commands;

import org.poo.bank.Account;
import org.poo.bank.Transaction;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// În DeleteAccount
public class DeleteAccount {
    private final List<User> users;

    public DeleteAccount(List<User> users) {
        this.users = users;
    }

    public Map<String, Object> deleteAccount(CommandInput command) {
        String iban = command.getAccount();
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", timestamp);

        User user = users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        if (user == null) {
            response.put("error", "User not found");
            return response;
        }

        Account account = user.getAccountByIBAN(iban);
        if (account == null) {
            response.put("error", "Account not found");
            return response;
        }

        if (account.getBalance() != 0) {
            Transaction transaction = new Transaction(
                    timestamp,
                    "Account couldn't be deleted - there are funds remaining",
                    null,
                    iban,
                    0,
                    command.getCurrency(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "deleteAccountError"
            );

            user.addTransaction(transaction);

            response.put("error", "Account couldn't be deleted - see org.poo.transactions for details");
            return response;
        }

        account.removeAllCards();
        user.removeAccount(account);

        response.put("success", true);
        return response;
    }
}


