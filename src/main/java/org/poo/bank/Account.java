package org.poo.bank;

import java.util.*;
import java.util.stream.Collectors;

public class Account {
    private final String IBAN;
    private double balance;
    private double minimumBalance;
    private final String currency;
    private final String type;
    private final List<Card> cards = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private double interestRate;
    private final List<String> involvedAccounts;

    public Account(String IBAN, String currency, String type) {
        this.IBAN = IBAN;
        this.currency = currency;
        this.type = type;
        this.balance = 0;
        this.minimumBalance = 0;
        this.interestRate = 0;
        this.involvedAccounts = null;
    }

    public Map<String, Double> getSpendingByCommerciants(int startTimestamp, int endTimestamp) {
        Map<String, Double> spendingByCommerciants = new HashMap<>();

        for (Transaction transaction : transactions) {
            if (transaction.getTimestamp() >= startTimestamp
                    && transaction.getTimestamp() <= endTimestamp
                    && "payOnline".equals(transaction.getTransactionType())) {

                String commerciant = transaction.getCommerciant();
                spendingByCommerciants.put(commerciant,
                        spendingByCommerciants.getOrDefault(commerciant, 0.0) + transaction.getAmount());
            }
        }

        return spendingByCommerciants;
    }


    // Adăugăm o tranzacție la lista contului
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
}