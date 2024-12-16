package org.poo.bank.commands;

import org.poo.bank.Account;
import org.poo.bank.OneTimeCard;
import org.poo.bank.Transaction;
import org.poo.fileio.CommandInput;
import org.poo.users.User;
import org.poo.utils.Utils;

import java.util.List;

public class CreateOneTimeCard {
    private final List<User> users;

    public CreateOneTimeCard(List<User> users) {
        this.users = users;
    }

    public void createOneTimeCard(CommandInput command) {
        System.out.println("am intrat aici " + command.getTimestamp());

        // Găsim utilizatorul pe baza email-ului
        User user = findUserByEmail(command.getEmail());
        if (user == null) {
            return; // Ieșim dacă utilizatorul nu există
        }

        // Găsim contul asociat utilizatorului
        Account account = user.getAccountByIBAN(command.getAccount());
        if (account == null) {
            return; // Ieșim dacă contul nu există
        }

        // Generăm detaliile cardului
        String cardNumber = Utils.generateCardNumber();
        // Creează un card de tip OneTimeCard
        OneTimeCard card = new OneTimeCard(cardNumber, "active");
        account.addCard(card); // Adaugă cardul în cont
        String description = "New card created";

        System.out.println("generez cardul " + card.getCardNumber());

        // Creăm tranzacția asociată adăugării cardului
        Transaction transaction = new Transaction(
                command.getTimestamp(),
                description,
                null, // Sender IBAN (nu există)
                account.getIBAN(), // Receiver IBAN (contul asociat)
                0, // Suma tranzacției (0 pentru crearea cardului)
                account.getCurrency(),
                "other", // Tipul transferului
                cardNumber, // Numărul cardului
                user.getEmail(), // Deținătorul cardului
                null, // Comerciant (nu este aplicabil)
                null, // Conturi implicate
                null,
                "createCard" // Tipul tranzacției
        );
        account.addTransaction(transaction);
        // Adăugăm tranzacția la utilizator
        user.addTransaction(transaction);
    }

    private User findUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
}
