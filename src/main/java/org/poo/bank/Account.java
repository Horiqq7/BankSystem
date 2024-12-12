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