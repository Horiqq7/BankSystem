package org.poo.bank;

import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;
import org.poo.operations.ExchangeRateManager;
import org.poo.users.User;
import org.poo.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;


public class Bank {
    private final List<User> users = new ArrayList<>();


    public Bank(final ObjectInput objectInput) {
        if (objectInput.getUsers() != null) {
            for (var userInput : objectInput.getUsers()) {
                User user = new User(userInput);
                users.add(user);
            }
        }
    }


    public List<Map<String, Object>> processCommand(CommandInput command) {
        switch (command.getCommand()) {
            case "printUsers":
                return printUsers();
            case "addAccount":
                addAccount(command);
                return Collections.emptyList();
            case "createCard":
                createCard(command);
                return Collections.emptyList();
            case "addFunds":
                addFunds(command);
                return Collections.emptyList();
            case "deleteAccount":  // Cazul pentru deleteAccount
                deleteAccount(command); // Apelează metoda deleteAccount
                return Collections.emptyList();
            case "deleteCard":  // Cazul pentru deleteCard
                deleteCard(command); // Apelează metoda deleteCard
                return Collections.emptyList();
            case "setMinimumBalance":
                setMinimumBalance(command);
                return Collections.emptyList();
            case "payOnline":
                payOnline(command);
                return Collections.emptyList();
            case "sendMoney": // Adăugăm cazul pentru sendMoney
                return sendMoney(command);
            case "setAlias":
                setAlias(command);
                return Collections.emptyList();
            case "printTransactions": // Adăugăm comanda aici
                return printTransactions(command);
            default:
                throw new IllegalArgumentException("Unknown command: " + command.getCommand());
        }
    }

    private List<Map<String, Object>> printUsers() {
        return users.stream()
                .map(User::toMap)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> printTransactions(CommandInput command) {
        String email = command.getEmail();
        User user = findUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        List<Transaction> transactions = user.getTransactions();
        List<Map<String, Object>> outputTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            outputTransactions.add(transaction.toMap());
        }
        return outputTransactions;
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

        // Creăm obiectele Transaction cu format corect
        Transaction senderTransaction = new Transaction(
                timestamp,
                description,
                senderIBAN,
                receiverIBAN,
                amount,
                senderAccount.getCurrency(),
                "sent", null, null, null,"sendMoney"
        );

        Transaction receiverTransaction = new Transaction(
                timestamp,
                description,
                senderIBAN,
                receiverIBAN,
                convertedAmount,
                receiverAccount.getCurrency(),
                "received", null, null, null, "sendMoney"
        );

        // Adăugăm tranzacțiile la utilizatori
        senderUser.addTransaction(senderTransaction);
        receiverUser.addTransaction(receiverTransaction);

        // Întoarcem o listă goală pentru succes
        return Collections.emptyList();
    }


    public List<Map<String, Object>> payOnline(CommandInput command) {
        List<Map<String, Object>> output = new ArrayList<>();

        // Căutăm utilizatorul pe baza email-ului
        User user = findUserByEmail(command.getEmail());
        if (user == null) {
            return output; // Nu facem nimic dacă utilizatorul nu există
        }

        // Căutăm contul și cardul activ
        Account account = null;
        Card card = null;
        for (Account acc : user.getAccounts()) {
            card = acc.getCardByNumber(command.getCardNumber());
            if (card != null && card.getStatus().equals("active")) {
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
        if (availableBalance < amount) {
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
                    "payOnlineInsufficentFunds" // Transaction Type
            );
            user.addTransaction(transaction1);
            return output;
        }

        // Creăm tranzacția folosind clasa Transaction
        Transaction transaction = new Transaction(
                command.getTimestamp(),
                "Card payment",
                account.getIBAN(), // Sender IBAN
                command.getCommerciant(), // Receiver IBAN (comerciantul)
                amount,
                account.getCurrency(),
                "payment", // Transfer Type
                command.getCardNumber(), // Card Number
                user.getEmail(), command.getCommerciant(), // Card Holder
                "payOnline" // Transaction Type
        );

        // Adăugăm tranzacția utilizatorului
        user.addTransaction(transaction);

        // Reducem balanța contului
        account.withdrawFunds(amount);

        // Nu adăugăm nimic în output în cazul unui succes
        return Collections.emptyList(); // succes
    }


    public void setMinimumBalance(CommandInput command) {
        String iban = command.getAccount();
        double minimumBalance = command.getAmount();

        // Căutăm contul
        for (User user : users) {
            Account account = user.getAccountByIBAN(iban);
            if (account != null) {
                // Setăm balanța minimă
                account.setMinimumBalance(minimumBalance);
                return;
            }
        }

        throw new IllegalArgumentException("Invalid account or user does not own the account.");
    }

    private void addAccount(CommandInput command) {
        // Căutăm utilizatorul pe baza email-ului
        User user = findUserByEmail(command.getEmail());
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
        String description = "New account created"; // Descrierea tranzacției
        String transferType = "other"; // Tipul tranzacției pentru crearea unui cont
        Transaction transaction = new Transaction(command.getTimestamp(), description, null, null,
                0, command.getCurrency(), transferType, null, null, null, "addAccount");

        // Adăugăm tranzacția la utilizator
        user.addTransaction(transaction);
    }

    public Map<String, Object> deleteAccount(CommandInput command) {
        String iban = command.getAccount();
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", timestamp);

        // Căutăm utilizatorul după email
        User user = findUserByEmail(email);
        if (user == null) {
            response.put("error", "User not found");
            return response;
        }

        // Căutăm contul după IBAN
        Account account = user.getAccountByIBAN(iban);
        if (account == null) {
            response.put("error", "Account not found");
            return response;
        }

        // Verificăm dacă balanța contului este 0
        if (account.getBalance() != 0) {
            response.put("error", "Account couldn't be deleted - see org.poo.transactions for details");
            return response;
        }

        // Ștergem cardurile asociate contului
        account.removeAllCards();

        // Ștergem contul din lista utilizatorului
        user.removeAccount(account);

        // Răspuns de succes
        response.put("success", true);
        return response;
    }

    public void addFunds(CommandInput command) {
        String iban = command.getAccount();  // IBAN-ul contului la care se adaugă fonduri
        double amount = command.getAmount();  // Suma care se adaugă

        // Parcurgem toți utilizatorii pentru a găsi contul cu IBAN-ul respectiv
        for (User user : users) {
            Account account = user.getAccountByIBAN(iban);  // Căutăm contul în cadrul fiecărui utilizator

            if (account != null) {
                // Adăugăm suma la balance-ul contului găsit
                account.addFunds(amount);
                return;  // Oprirea căutării odată ce am găsit contul
            }
        }

    }

    public void createCard(CommandInput command) {
        User user = findUserByEmail(command.getEmail());
        if (user == null) {
            return;
        }

        Account account = user.getAccountByIBAN(command.getAccount());
        if (account == null) {
            return;
        }

        String description;
        String cardNumber;

        if ("createCard".equals(command.getCommand())) {
            cardNumber = Utils.generateCardNumber();
            Card card = new Card(cardNumber, "active");
            account.addCard(card);
            description = "New card created";
        } else if ("createOneTimeCard".equals(command.getCommand())) {
            cardNumber = Utils.generateCardNumber();
            OneTimeCard oneTimeCard = new OneTimeCard(cardNumber, "active");
            account.addCard(oneTimeCard);
            description = "New one-time card created";
        } else {
            return;
        }

        // Creăm tranzacția
        Transaction transaction = new Transaction(
                command.getTimestamp(),
                description,
                null, // Sender IBAN
                account.getIBAN(), // Receiver IBAN
                0, // Amount
                account.getCurrency(),
                "other", cardNumber,
                user.getEmail(), null,"createCard"
        );

        // Adăugăm tranzacția utilizatorului
        user.addTransaction(transaction);
    }


    public void deleteCard(CommandInput command) {
        User user = findUserByEmail(command.getEmail());
        if (user == null) {
            return;
        }

        for (Account account : user.getAccounts()) {
            Card card = account.getCardByNumber(command.getCardNumber());
            if (card != null) {
                account.removeCard(card);

                // Creăm tranzacția
                Transaction transaction = new Transaction(
                        command.getTimestamp(),
                        "The card has been destroyed",
                        account.getIBAN(), // Sender IBAN
                        null, // Receiver IBAN
                        0, // Amount
                        account.getCurrency(),
                        "other",
                        card.getCardNumber(),
                        user.getEmail(),
                        null,
                        "deleteCard"
                );

                // Adăugăm tranzacția utilizatorului
                user.addTransaction(transaction);
                return;
            }
        }
    }

    public User findUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);  // Returnează null dacă utilizatorul nu există
    }
}