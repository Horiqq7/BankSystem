package org.poo.bank;

import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;
import org.poo.operations.Command;
import org.poo.operations.ExchangeRateManager;
import org.poo.users.User;
import org.poo.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;


public class Bank {
    private final List<User> users = new ArrayList<>();
    private Map<String, Account> accounts = new HashMap<>();


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
            case "createOneTimeCard":
                createOneTimeCard(command);
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
            case "checkCardStatus":
                Map<String, Object> response = checkCardStatus(command);

                // Dacă răspunsul conține "output", adăugăm în formatul final
                if (response.containsKey("output")) {
                    return Collections.singletonList(response);
                } else {
                    return Collections.emptyList(); // Niciun răspuns dacă nu există output
                }
            case "changeInterestRate":
                changeInterestRate(command);
                return Collections.emptyList();
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


    public void changeInterestRate(CommandInput command) {
        String targetIBAN = command.getAccount();
        double newInterestRate = command.getInterestRate();

        // Căutăm contul țintă
        Account targetAccount = null;
        for (User user : users) {
            targetAccount = user.getAccountByIBAN(targetIBAN);
            if (targetAccount != null) {
                break;
            }
        }

        if (targetAccount == null) {
            throw new IllegalArgumentException("Account not found");
        }

        // Schimbăm rata dobânzii
        targetAccount.setInterestRate(newInterestRate);
    }


    public Map<String, Object> checkCardStatus(CommandInput command) {
        String cardNumber = command.getCardNumber();
        int timestamp = command.getTimestamp();

        // Găsim utilizatorul pe baza numărului de card
        User user = findUserByCardNumber(cardNumber);
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
                            "checkCardStatusFrozen" // Transaction type este "checkCardStatusFrozen"
                    );
                    user.addTransaction(transaction);
                }
            }
            // Nu adăugăm niciun răspuns dacă cardul există
        }
        return response;
    }



    private Account findAccountByCardNumber(User user, String cardNumber) {
        return user.getAccounts().stream()
                .filter(account -> account.getCardByNumber(cardNumber) != null)
                .findFirst()
                .orElse(null);
    }


    private User findUserByCardNumber(String cardNumber) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getCardByNumber(cardNumber) != null) {
                    return user;
                }
            }
        }
        return null;
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
                    "sendMoneyInsufficientFunds"
            );
            senderUser.addTransaction(insufficientFundsTransaction);

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

        System.out.println("User found: " + user.getEmail());

        // Căutăm contul și cardul activ
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

        // Dacă cardul nu a fost găsit sau nu este activ
        if (card == null) {
            Map<String, Object> errorNode = new HashMap<>();
            errorNode.put("description", "Card not found");

            Map<String, Object> response = new HashMap<>();
            response.put("command", "payOnline");
            response.put("output", errorNode);
            output.add(response);

            return output;
        }

        // Verificăm dacă account este null
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
                    "payOnlineCardIsFrozen" // Transaction Type
            );

            // Adăugăm tranzacția utilizatorului
            user.addTransaction(transaction);

            // Nu mai procesăm plata
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

    public void createOneTimeCard(CommandInput command) {
        User  user = findUserByEmail(command.getEmail());
        if (user == null) {
            return;
        }

        Account account = user.getAccountByIBAN(command.getAccount());
        if (account == null) {
            return;
        }

        String description;
        String cardNumber;

        cardNumber = Utils.generateCardNumber();
        Card card = new Card(cardNumber, "active");
        account.addCard(card);
        description = "New card created";

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

        user.addTransaction(transaction);
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

         // if ("createCard".equals(command.getCommand())) {
            cardNumber = Utils.generateCardNumber();
            Card card = new Card(cardNumber, "active");
            account.addCard(card);
            description = "New card created";
//        } else if ("createOneTimeCard".equals(command.getCommand())) {
//            cardNumber = Utils.generateCardNumber();
//            OneTimeCard oneTimeCard = new OneTimeCard(cardNumber, "active");
//            account.addCard(oneTimeCard);
//            description = "New one-time card created";
//        } else {
//            return;
//        }

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

    public void addAccount(Account account) {
        accounts.put(account.getIBAN(), account);
    }

    // Metodă pentru a obține un cont pe baza IBAN-ului
    public Account getAccount(String iban) {
        return accounts.get(iban);
    }

    public User findUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);  // Returnează null dacă utilizatorul nu există
    }
}