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
    private List<Transaction> transactions = new ArrayList<>();

    public Account(String IBAN, String currency, String type) {
        this.IBAN = IBAN;
        this.currency = currency;
        this.type = type;
        this.balance = 0;
        this.minimumBalance = 0;
    }

    public void addTransaction(String description, double amount, String senderIBAN, String receiverIBAN,
                               String transferType, String card, String cardHolder, String transactionType, String commerciant) {
        int timestamp = (int) (System.currentTimeMillis() / 1000); // Timestampul tranzacției
        Transaction transaction = new Transaction(timestamp, description, senderIBAN, receiverIBAN,
                amount, currency, transferType, card, cardHolder, commerciant, transactionType); // Adăugăm field-ul "commerciant"
        transactions.add(transaction); // Adăugăm tranzacția în lista contului
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
            addTransaction("Funds added", amount, null, IBAN, "other", null, null, "addFunds", null); // Modifică pentru a include "commerciant"
        }
    }

    public void addCard(Card card) {
        cards.add(card);
        addTransaction("Card added", 0, null, IBAN, "other", card.getCardNumber(), null, "addCard", null); // Modifică pentru a include "commerciant"
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
            addTransaction("Funds withdrawn", -amount, IBAN, null, "sent", null, null, "withdrawFunds", null); // Modifică pentru a include "commerciant"
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
        addTransaction("Card removed", 0, null, IBAN, "other", card.getCardNumber(), null, "removeCard", null); // Modifică pentru a include "commerciant"
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

