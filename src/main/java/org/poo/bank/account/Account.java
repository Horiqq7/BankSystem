package org.poo.bank.account;

import org.poo.bank.cards.Card;
import org.poo.bank.transaction.Transaction;

import java.util.*;
import java.util.stream.Collectors;

public class Account {
    private final String IBAN;
    private double balance;
    private double minimumBalance;
    private final String currency;
    private final String type;
    private List<Card> cards;
    private List<Transaction> transactions;
    private double interestRate;

    private Account(AccountBuilder builder) {
        this.IBAN = builder.IBAN;
        this.balance = builder.balance;
        this.minimumBalance = builder.minimumBalance;
        this.currency = builder.currency;
        this.type = builder.type;
        this.cards = builder.cards != null ? builder.cards : new ArrayList<>();
        this.transactions = builder.transactions != null ? builder.transactions : new ArrayList<>();
        this.interestRate = builder.interestRate;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void setInterestRate(double newInterestRate) {
        if (newInterestRate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        this.interestRate = newInterestRate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public String getIBAN() {
        return IBAN;
    }

    public void removeAllCards() {
        this.cards.clear();
    }

    public Map<String, Object> generateSpendingsReport(int startTimestamp, int endTimestamp) {
        // Filtrăm tranzacțiile care aparțin intervalului și sunt de tip "payment"
        List<Transaction> filteredTransactions = transactions.stream()
                .filter(t -> t.getTimestamp() >= startTimestamp
                        && t.getTimestamp() <= endTimestamp
                        && "payOnline".equals(t.getTransactionType()))
                .collect(Collectors.toList());

        // Grupăm tranzacțiile după commerciant și calculăm totalurile
        Map<String, Double> commerciantsTotals = filteredTransactions.stream()
                .filter(t -> t.getCommerciant() != null)
                .collect(Collectors.groupingBy(
                        Transaction::getCommerciant,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        // Generăm o listă de comercianți cu totalurile corespunzătoare
        List<Map<String, Object>> commerciantsList = commerciantsTotals.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("commerciant", entry.getKey());
                    map.put("total", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        // Conversia tranzacțiilor filtrate într-o listă de hărți
        List<Map<String, Object>> transactionsList = filteredTransactions.stream()
                .map(Transaction::toMap)
                .collect(Collectors.toList());

        // Creăm raportul final
        Map<String, Object> report = new HashMap<>();
        report.put("IBAN", IBAN);
        report.put("balance", balance);
        report.put("currency", currency);
        report.put("transactions", transactionsList); // Lista tranzacțiilor
        report.put("commerciants", commerciantsList); // Lista comercianților

        return report;
    }


    public void addFunds(double amount) {
        if (amount > 0) {
            this.balance += amount;
            Transaction transaction = new Transaction(
                    (int) (System.currentTimeMillis() / 1000),
                    "Funds added",
                    null,
                    IBAN,
                    amount,
                    currency,
                    "addFunds",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "addFunds"
            );
            addTransaction(transaction);
        }
    }

    public void addCard(Card card) {
        cards.add(card);
        Transaction transaction = new Transaction(
                (int) (System.currentTimeMillis() / 1000),
                "Card added",
                null,
                IBAN,
                0,
                currency,
                "addCard",
                card.getCardNumber(),
                null,
                null,
                null,
                null,
                "addCard"
        );
        addTransaction(transaction);
    }

    public Card getCardByNumber(String cardNumber) {
        return cards.stream()
                .filter(card -> card.getCardNumber().equals(cardNumber))
                .findFirst()
                .orElse(null);
    }

    public void withdrawFunds(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            Transaction transaction = new Transaction(
                    (int) (System.currentTimeMillis() / 1000),
                    "Funds withdrawn",
                    IBAN,
                    null,
                    -amount,
                    currency,
                    "withdrawFunds",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "withdrawFunds"
            );
            addTransaction(transaction);
        } else {
            throw new IllegalArgumentException("Insufficient funds");
        }
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(double minimumBalance) {
        this.minimumBalance = minimumBalance;
    }

    public void removeCard(Card card) {
        cards.remove(card);
        Transaction transaction = new Transaction(
                (int) (System.currentTimeMillis() / 1000),
                "Card removed",
                null,
                IBAN,
                0,
                currency,
                "removeCard",
                card.getCardNumber(),
                null,
                null,
                null,
                null,
                "removeCard"
        );
        addTransaction(transaction);
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getType() {
        return type;
    }

    public List<Card> getCards() {
        return cards;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("IBAN", IBAN);
        map.put("balance", balance);
        map.put("currency", currency);
        map.put("type", type);
        map.put("cards", cards.stream().map(Card::toMap).collect(Collectors.toList()));
        return map;
    }

    public static class AccountBuilder {
        private String IBAN;
        private double balance = 0;
        private double minimumBalance = 0;
        private String currency;
        private String type;
        private List<Card> cards;
        private List<Transaction> transactions;
        private double interestRate = 0;
        private List<String> involvedAccounts;

        public AccountBuilder(String IBAN, String currency, String type) {
            this.IBAN = IBAN;
            this.currency = currency;
            this.type = type;
        }

        public AccountBuilder balance(double balance) {
            this.balance = balance;
            return this;
        }

        public AccountBuilder minimumBalance(double minimumBalance) {
            this.minimumBalance = minimumBalance;
            return this;
        }

        public AccountBuilder interestRate(double interestRate) {
            this.interestRate = interestRate;
            return this;
        }

        public AccountBuilder cards(List<Card> cards) {
            this.cards = cards;
            return this;
        }

        public AccountBuilder transactions(List<Transaction> transactions) {
            this.transactions = transactions;
            return this;
        }

        public AccountBuilder involvedAccounts(List<String> involvedAccounts) {
            this.involvedAccounts = involvedAccounts;
            return this;
        }

        public Account build() {
            return new Account(this);
        }
    }

}