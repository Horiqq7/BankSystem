package org.poo.operations;

import org.poo.fileio.ExchangeInput;

import java.util.*;

public class ExchangeRateManager {
    private static ExchangeRateManager instance;
    private final List<ExchangeRate> exchangeRates = new ArrayList<>();
    private final Map<String, Map<String, Double>> exchangeGraph = new HashMap<>();

    private ExchangeRateManager() {}

    public static ExchangeRateManager getInstance() {
        if (instance == null) {
            instance = new ExchangeRateManager();
        }
        return instance;
    }

    // Încarcă ratele și construiește graful
    public void loadExchangeRates(List<ExchangeInput> exchangeInputs) {
        exchangeRates.clear();
        exchangeGraph.clear();
        for (ExchangeInput input : exchangeInputs) {
            ExchangeRate rate = new ExchangeRate(input);
            exchangeRates.add(rate);
            addToGraph(rate.getFrom(), rate.getTo(), rate.getRate());
        }
    }

    private void addToGraph(String from, String to, double rate) {
        exchangeGraph.putIfAbsent(from, new HashMap<>());
        exchangeGraph.putIfAbsent(to, new HashMap<>());
        exchangeGraph.get(from).put(to, rate);
        exchangeGraph.get(to).put(from, 1.0 / rate); // Adăugăm și rata inversă
    }

    public double convertCurrency(String from, String to, double amount, int timestamp) {
        double rate = getExchangeRate(from, to, timestamp);
        return amount * rate;
    }

    public double getExchangeRate(String from, String to, int timestamp) {
        if (from.equalsIgnoreCase(to)) {
            return 1.0;
        }

        // BFS pentru a găsi rata indirectă
        Queue<String> queue = new LinkedList<>();
        Map<String, Double> visited = new HashMap<>();
        queue.add(from);
        visited.put(from, 1.0);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            double currentRate = visited.get(current);

            Map<String, Double> neighbors = exchangeGraph.getOrDefault(current, Collections.emptyMap());
            for (Map.Entry<String, Double> entry : neighbors.entrySet()) {
                String neighbor = entry.getKey();
                double rate = entry.getValue();

                if (!visited.containsKey(neighbor)) {
                    double newRate = currentRate * rate;
                    visited.put(neighbor, newRate);

                    if (neighbor.equalsIgnoreCase(to)) {
                        return newRate;
                    }
                    queue.add(neighbor);
                }
            }
        }

        // Poți adăuga o excepție mai informativă sau un mesaj de eroare
        throw new IllegalArgumentException("Exchange rate not found for " + from + " to " + to);
    }

}
