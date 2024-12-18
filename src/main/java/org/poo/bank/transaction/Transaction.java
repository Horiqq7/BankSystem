package org.poo.bank.transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transaction {
    private final int timestamp;
    private final String description;
    private final String senderIBAN;
    private final String receiverIBAN;
    private final double amount;
    private final String currency;
    private final String transferType;
    private final String card; // Numărul cardului
    private final String cardHolder; // Deținătorul cardului
    private final String transactionType;
    private final String commerciant;
    private final List<String> involvedAccounts;
    private final String error;

    public Transaction(int timestamp, String description, String senderIBAN, String receiverIBAN,
                       double amount, String currency, String transferType, String card, String cardHolder, String commerciant,
                       List<String> involvedAccounts, String error, String transactionType) {
        this.timestamp = timestamp;
        this.description = description;
        this.senderIBAN = senderIBAN;
        this.receiverIBAN = receiverIBAN;
        this.amount = amount;
        this.currency = currency;
        this.transferType = transferType;
        this.card = card;
        this.cardHolder = cardHolder;
        this.commerciant = commerciant;
        this.involvedAccounts = involvedAccounts;
        this.transactionType = transactionType;
        this.error = error;
    }

    public List<String> getInvolvedAccounts() {
        return involvedAccounts;
    }

    public String getCommerciant() {
        return commerciant;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();


        switch (transactionType) {
            case "createCard":
                map.put("timestamp", timestamp);
                map.put("description", description);
                map.put("card", card);
                map.put("cardHolder", cardHolder);
                map.put("account", receiverIBAN);
                break;
            case "addAccount":
                map.put("timestamp", timestamp);
                map.put("description", description);
                break;
            case "deleteAccountError": // Noua eroare pentru ștergerea contului
                map.put("timestamp", timestamp);
                map.put("description", description); // Adăugăm mesajul de eroare
                break;
            case "sendMoney":
                map.put("timestamp", timestamp);
                map.put("description", description);
                map.put("senderIBAN", senderIBAN);
                map.put("receiverIBAN", receiverIBAN);
                map.put("amount", amount + " " + currency);
                map.put("transferType", transferType);
                break;
            case "sendMoneyInsufficientFunds":
                map.put("timestamp", timestamp);
                map.put("description", description);
                break;
            case "payOnlineInsufficentFunds":
                map.put("timestamp", timestamp);
                map.put("description", description);
                break;
            case "payOnline":
                map.put("timestamp", timestamp);
                map.put("description", description);
                map.put("commerciant", commerciant);
                map.put("amount", amount);
                break;
            case "payOnlineCardIsFrozen":
                map.put("timestamp", timestamp);
                map.put("description", description);
                break;
            case "deleteCard":
                map.put("timestamp", timestamp);
                map.put("card", card);
                map.put("account", senderIBAN);
                map.put("cardHolder", cardHolder);
                map.put("description", description);
                break;
            case "checkCardStatusFrozen":
                map.put("timestamp", timestamp);
                map.put("description", description);
                break;
            case "splitPayment":
                map.put("timestamp", timestamp);
                map.put("description", description);
                map.put("currency", currency);
                map.put("involvedAccounts", involvedAccounts);  // IBAN-uri de destinație
                map.put("amount", amount); // Aici, trebuie să păstrăm suma ca un număr (fără ghilimele)
                break;
            case "splitPaymentError":
                map.put("timestamp", timestamp);
                map.put("description", description);
                map.put("currency", currency);
                map.put("involvedAccounts", involvedAccounts);
                map.put("amount", amount); // Asigură-te că e suma totală
                map.put("error", error);  // Mesajul de eroare
                break;
            case "destroyOneTimeCard":
                map.put("timestamp", timestamp);
                map.put("description", description);
                map.put("card", card);
                map.put("cardHolder", cardHolder);
                map.put("account", senderIBAN);
                break;
            case "newOneTimeCard":
                map.put("timestamp", timestamp);
                map.put("description", description);
                map.put("card", card);
                map.put("cardHolder", cardHolder);
                map.put("account", senderIBAN);
                break;
            case "changeInterestRate":
                map.put("timestamp", timestamp);
                map.put("description", description);
                break;
            case "spendingsReportError":  // Noua eroare pentru raportul de cheltuieli
                map.put("timestamp", timestamp);
                map.put("description", description);  // Adăugăm mesajul de eroare
                map.put("error", error);  // Mesajul de eroare
                break;
            default:
                break;
        }
        return map;
    }


    public String getCard() {
        return card;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getSenderIBAN() {
        return senderIBAN;
    }

    public String getReceiverIBAN() {
        return receiverIBAN;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTransferType() {
        return transferType;
    }
}