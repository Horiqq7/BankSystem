package org.poo.bank.commands.account_commands.card_commands;

import org.poo.bank.account.Account;
import org.poo.bank.cards.Card;
import org.poo.bank.transaction.Transaction;
import org.poo.fileio.CommandInput;
import org.poo.bank.users.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckCardStatus {

    public Map<String, Object> execute(CommandInput command, List<User> users) {
        String cardNumber = command.getCardNumber();
        int timestamp = command.getTimestamp();

        // Găsim utilizatorul pe baza numărului de card
        User user = findUserByCardNumber(cardNumber, users);
        String description = "";

        Map<String, Object> response = new HashMap<>();

        // Verificăm dacă utilizatorul există
        if (user == null) {
            description = "Card not found";
            Map<String, Object> output = new HashMap<>();
            output.put("description", description);
            output.put("timestamp", timestamp);

            response.put("command", "checkCardStatus");
            response.put("output", output);
            response.put("timestamp", timestamp);
        } else {
            Account account = findAccountByCardNumber(user, cardNumber);
            if (account != null) {
                Card card = account.getCardByNumber(cardNumber);
                if (card != null && account.getBalance() <= account.getMinimumBalance()) {
                    // Cardul este înghețat, înregistrăm tranzacția
                    description = "You have reached the minimum amount of funds, the card will be frozen";
                    Transaction transaction = new Transaction(
                            timestamp,
                            description,
                            account.getIBAN(),
                            null, // ReceiverIBAN poate fi null
                            0, // Suma este 0 pentru acest tip de tranzacție
                            null, // Currency este null
                            null, // TransferType este null
                            cardNumber,
                            user.getEmail(),
                            null, // Commerciantul este null
                            null,
                            null,
                            "checkCardStatusFrozen" // Transaction type este "checkCardStatusFrozen"
                    );
                    user.addTransaction(transaction);
                }
            }
        }
        return response;
    }

    private Account findAccountByCardNumber(User user, String cardNumber) {
        return user.getAccounts().stream()
                .filter(account -> account.getCardByNumber(cardNumber) != null)
                .findFirst()
                .orElse(null);
    }

    private User findUserByCardNumber(String cardNumber, List<User> users) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getCardByNumber(cardNumber) != null) {
                    return user;
                }
            }
        }
        return null;
    }
}