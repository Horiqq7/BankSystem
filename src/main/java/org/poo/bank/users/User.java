package org.poo.bank.users;

import org.poo.bank.account.Account;
import org.poo.bank.transaction.Transaction;
import org.poo.fileio.UserInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class User {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final List<Account> accounts = new ArrayList<>();
    private final Map<String, String> aliases = new HashMap<>(); // Map pentru alias-uri: alias -> IBAN
    private final List<Transaction> transactions = new ArrayList<>(); // Listă de tranzacții ale utilizatorului

    // Constructor pentru inițializare
    public User(UserInput input) {
        this.firstName = input.getFirstName();
        this.lastName = input.getLastName();
        this.email = input.getEmail();
    }

    // Setăm alias-ul pentru un IBAN specific
    public void setAlias(String alias, String iban) {
        aliases.put(alias, iban); // Asociem alias-ul cu IBAN-ul
    }

    // Obținem IBAN-ul asociat unui alias
    public String getIBANByAlias(String alias) {
        return aliases.get(alias); // Returnăm IBAN-ul asociat alias-ului
    }

    // Returnăm toate tranzacțiile utilizatorului
    public List<Transaction> getTransactions() {
        return transactions;
    }

    // Adăugăm o tranzacție în lista utilizatorului
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    // Adăugăm un cont utilizatorului
    public void addAccount(Account account) {
        accounts.add(account); // Adăugăm contul la utilizator
    }

    // Eliminăm un cont din lista utilizatorului
    public void removeAccount(Account account) {
        accounts.remove(account); // Ștergem contul din lista de conturi
    }

    // Căutăm un cont după IBAN
    public Account getAccountByIBAN(String iban) {
        return accounts.stream()
                .filter(account -> account.getIBAN().equals(iban)) // Verificăm IBAN-ul
                .findFirst()
                .orElse(null); // Returnăm null dacă nu am găsit contul
    }


    // Transformăm obiectul utilizator în hartă pentru afișare
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("email", email);
        map.put("accounts", accounts.stream().map(Account::toMap).collect(Collectors.toList()));
        return map;
    }

    // Getteri pentru proprietăți individuale
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}
