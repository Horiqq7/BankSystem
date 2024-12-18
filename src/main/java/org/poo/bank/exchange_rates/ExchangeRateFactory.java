package org.poo.bank.exchange_rates;

import org.poo.fileio.ExchangeInput;

public class ExchangeRateFactory {

    public static ExchangeRate createExchangeRate(ExchangeInput exchangeInput) {
        return new ExchangeRate(exchangeInput);
    }
}
