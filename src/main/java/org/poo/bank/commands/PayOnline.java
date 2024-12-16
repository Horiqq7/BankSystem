package org.poo.bank.commands;

import org.poo.bank.*;
import org.poo.fileio.CommandInput;
import org.poo.operations.ExchangeRateManager;
import org.poo.users.User;
import org.poo.utils.Utils;

import java.util.*;

public class PayOnline {
    private List<User> users;

    public PayOnline(List<User> users) {
        this.users = users;
    }

    public List<Map<String, Object>> payOnline(CommandInput command, Bank bank) {
        List<Map<String, Object>> output = new ArrayList<>();

        User user = findUserByEmail(command.getEmail());
        if (user == null) {
            Map<String, Object> errorNode = new HashMap<>();
            errorNode.put("description", "User not found");
            Map<String, Object> response = new HashMap<>();
            response.put("command", "payOnline");
            response.put("output", errorNode);
            output.add(response);
            return output;
        }

        Account account = null;
        Card card = null;

        System.out.println("Checking accounts for user: " + user.getEmail());
        for (Account acc : user.getAccounts()) {
            System.out.println("Checking account with IBAN: " + acc.getIBAN());
            card = acc.getCardByNumber(command.getCardNumber());

            if (card != null) {
                System.out.println("Card found: " + card.getCardNumber() + " with status: " + card.getStatus());
            }

            if (card != null) {
                account = acc;
                break;
            }
        }

        if (card == null) {
            Map<String, Object> errorNode = new HashMap<>();
            errorNode.put("description", "Card not found");

            Map<String, Object> response = new HashMap<>();
            response.put("command", "payOnline");
            response.put("output", errorNode);
            output.add(response);

            return output;
        }

        if (account == null) {
            System.out.println("Account is null for user: " + user.getEmail());
            return output;
        } else {
            System.out.println("Account is valid: " + account.getIBAN());
        }

        double amount = command.getAmount();
        String currency = command.getCurrency();
        double availableBalance = account.getBalance();

        // Conversia valutei, dacă este necesar
        if (!currency.equalsIgnoreCase(account.getCurrency())) {
            try {
                amount = ExchangeRateManager.getInstance()
                        .convertCurrency(currency, account.getCurrency(), amount, command.getTimestamp());
            } catch (IllegalArgumentException e) {
                Map<String, Object> errorNode = new HashMap<>();
                errorNode.put("description", "Exchange rates not available");

                Map<String, Object> response = new HashMap<>();
                response.put("command", "payOnline");
                response.put("output", errorNode);
                output.add(response);

                return output;
            }
        }

        // Verificăm dacă sunt suficiente fonduri
        if (availableBalance < amount && card.getStatus().equals("active")) {
            Transaction transaction1 = new Transaction(
                    command.getTimestamp(),
                    "Insufficient funds",
                    null, // Sender IBAN
                    null, // Receiver IBAN (comerciantul)
                    0,
                    account.getCurrency(),
                    "payment", // Transfer Type
                    command.getCardNumber(), // Card Number
                    user.getEmail(), null, // Card Holder
                    null,
                    null,
                    "payOnlineInsufficentFunds" // Transaction Type
            );
            user.addTransaction(transaction1);
            return output;
        }

        if (amount > availableBalance - account.getMinimumBalance()) {
            card.setStatus("frozen");
            System.out.println("Card " + card.getCardNumber() + " has been frozen.");
        }

        if (card.getStatus().equals("frozen")) {
            // Înregistrăm tranzacția că cardul este blocat
            Transaction transaction = new Transaction(
                    command.getTimestamp(),
                    "The card is frozen",
                    account.getIBAN(), // Sender IBAN
                    command.getCommerciant(), // Receiver IBAN (comerciantul)
                    0, // Nu se face transfer
                    account.getCurrency(),
                    "payment", // Transfer Type
                    command.getCardNumber(), // Card Number
                    user.getEmail(), // Card Holder
                    command.getCommerciant(), // Commerciant
                    null,
                    null,
                    "payOnlineCardIsFrozen" // Transaction Type
            );

            // Adăugăm tranzacția utilizatorului
            user.addTransaction(transaction);

            // Nu mai procesăm plata
            return output;
        }

        // Creăm tranzacția folosind clasa Transaction
        account.withdrawFunds(amount);
        Transaction transaction = new Transaction(
                command.getTimestamp(),
                "Card payment",
                account.getIBAN(),
                command.getCommerciant(),
                amount,
                account.getCurrency(),
                "payment",
                command.getCardNumber(),
                user.getEmail(),
                command.getCommerciant(),
                null,
                null,
                "payOnline"
        );

        user.addTransaction(transaction);
        account.addTransaction(transaction);

        if (card instanceof OneTimeCard) {
            // Tranzacție pentru distrugerea cardului OneTimeCard
            Transaction destroyCardTransaction = new Transaction(
                    command.getTimestamp(),
                    "The card has been destroyed",
                    account.getIBAN(),
                    null, // Nicio destinație pentru card distrus
                    0,
                    account.getCurrency(),
                    "other",
                    card.getCardNumber(),
                    user.getEmail(),
                    null, // Nu există comerciant
                    null,
                    null,
                    "destroyOneTimeCard"
            );
            user.addTransaction(destroyCardTransaction);
            account.addTransaction(destroyCardTransaction);

            // Creăm un nou card
            String newCardNumber = Utils.generateCardNumber();
            card.setCardNumber(newCardNumber); // Schimbă numărul cardului existent
            card.setStatus("active"); // Setăm cardul înapoi la statusul "active"

            // Tranzacție pentru crearea unui nou card
            Transaction newCardTransaction = new Transaction(
                    command.getTimestamp(), // Incrementăm timestamp-ul pentru actualizarea cardului
                    "New card created",
                    account.getIBAN(),
                    null, // Nicio destinație pentru cardul nou
                    0, // Nu se face transfer
                    account.getCurrency(),
                    "other",
                    newCardNumber,
                    user.getEmail(),
                    null, // Nu există comerciant
                    null,
                    null,
                    "newOneTimeCard"
            );
            user.addTransaction(newCardTransaction);
            account.addTransaction(newCardTransaction);
        }

        // Nu adăugăm nimic în output în cazul unui succes
        return Collections.emptyList(); // succes
    }

    // Metoda care caută utilizatorul în lista de utilizatori pe baza email-ului
    public User findUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);  // Returnează null dacă utilizatorul nu există
    }
}
