package org.poo.bank;

public class OneTimeCard extends Card {
    private boolean used = false;  // Flag care indică dacă cardul a fost folosit

    public OneTimeCard(String cardNumber, String status) {
        super(cardNumber, status);
    }

    public boolean isUsed() {
        return used;
    }

    public void useCard() {
        if (!used) {
            used = true;
            this.setStatus("used");  // Schimbăm statusul cardului
        }
    }
}
