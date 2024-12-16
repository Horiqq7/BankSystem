package org.poo.bank.commands;

import org.poo.bank.Account;
import org.poo.bank.Transaction;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

import java.util.List;

public class AddFunds {
    private final List<User> users;

    public AddFunds(List<User> users) {
        this.users = users;
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
                Transaction transaction = new Transaction(
                        (int) (System.currentTimeMillis() / 1000),  // Timpul tranzacției
                        "Funds added",  // Descrierea tranzacției
                        null,  // Sender IBAN
                        iban,  // Receiver IBAN
                        amount,  // Suma adăugată
                        account.getCurrency(),  // Moneda contului
                        "addFunds",  // Tipul tranzacției
                        null,  // Numărul cardului (neaplicabil)
                        null,  // Deținătorul cardului
                        null,  // Comerciant (neaplicabil)
                        null,  // Conturi implicate (neaplicabil)
                        null,  // Alte informații
                        "addFunds"  // Tipul tranzacției
                );
                account.addTransaction(transaction);  // Adăugăm tranzacția în istoricul contului
                break;  // Ieșim din buclă după ce găsim contul
            }
        }
    }
}