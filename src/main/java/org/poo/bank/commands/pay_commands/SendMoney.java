package org.poo.bank.commands.pay_commands;

import org.poo.bank.account.Account;
import org.poo.bank.transaction.Transaction;
import org.poo.fileio.CommandInput;
import org.poo.bank.exchange_rates.ExchangeRateManager;
import org.poo.bank.users.User;

import java.util.*;

public class SendMoney {
    private final List<User> users;

    public SendMoney(List<User> users) {
        this.users = users;
    }

    public List<Map<String, Object>> sendMoney(CommandInput command) {
        List<Map<String, Object>> output = new ArrayList<>();
        String senderIBAN = command.getAccount();
        String receiverIBAN = command.getReceiver();
        double amount = command.getAmount();
        String description = command.getDescription();
        int timestamp = command.getTimestamp();

        // Verificăm suma transferată
        if (amount <= 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("description", "Invalid amount");
            output.add(error);
            return output;
        }

        // Căutăm expeditorul
        Account senderAccount = null;
        User senderUser = null;
        for (User user : users) {
            senderAccount = user.getAccountByIBAN(senderIBAN);
            if (senderAccount != null) {
                senderUser = user;
                break;
            }
        }

        if (senderAccount == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("description", "Sender account not found");
            output.add(error);
            return output;
        }

        // Căutăm destinatarul
        Account receiverAccount = null;
        User receiverUser = null;
        for (User user : users) {
            receiverAccount = user.getAccountByIBAN(receiverIBAN);
            if (receiverAccount != null) {
                receiverUser = user;
                break;
            }
        }

        if (receiverAccount == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("description", "Receiver account not found");
            output.add(error);
            return output;
        }

        // Verificăm dacă expeditorul are suficiente fonduri
        if (senderAccount.getBalance() < amount) {
            // Adăugăm tranzacția de eroare la utilizator
            Transaction insufficientFundsTransaction = new Transaction(
                    timestamp,
                    "Insufficient funds",
                    senderIBAN,
                    receiverIBAN,
                    amount,
                    senderAccount.getCurrency(),
                    "error",
                    null, null, null,
                    null,
                    null,
                    "sendMoneyInsufficientFunds"
            );
            senderUser.addTransaction(insufficientFundsTransaction);
            senderAccount.addTransaction(insufficientFundsTransaction);

            Map<String, Object> error = new HashMap<>();
            error.put("description", "Insufficient funds in sender account");
            output.add(error);
            return output;
        }

        double convertedAmount = amount;
        if (!senderAccount.getCurrency().equalsIgnoreCase(receiverAccount.getCurrency())) {
            try {
                convertedAmount = ExchangeRateManager.getInstance().convertCurrency(
                        senderAccount.getCurrency(),
                        receiverAccount.getCurrency(),
                        amount,
                        command.getTimestamp()
                );
            } catch (IllegalArgumentException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("description", "Exchange rates not available");
                output.add(error);
                return output;
            }
        }

        // Tranzacție atomică
        senderAccount.withdrawFunds(amount);
        receiverAccount.addFunds(convertedAmount);

        Transaction senderTransaction = new Transaction(
                timestamp,
                description,
                senderIBAN,
                receiverIBAN,
                Double.parseDouble(String.format("%.14f", amount)), // Asigurăm precizia pentru expeditor
                senderAccount.getCurrency(),
                "sent", null, null, null, null, null, "sendMoney"
        );

        Transaction receiverTransaction = new Transaction(
                timestamp,
                description,
                senderIBAN,
                receiverIBAN,
                Double.parseDouble(String.format("%.14f", convertedAmount)), // Asigurăm precizia pentru destinatar
                receiverAccount.getCurrency(),
                "received", null, null, null, null, null, "sendMoney"
        );

        senderUser.addTransaction(senderTransaction);
        senderAccount.addTransaction(senderTransaction);

        receiverUser.addTransaction(receiverTransaction);
        receiverAccount.addTransaction(receiverTransaction);

        // Întoarcem o listă goală pentru succes
        return Collections.emptyList();
    }
}