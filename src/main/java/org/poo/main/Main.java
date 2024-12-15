package org.poo.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Account;
import org.poo.bank.Card;
import org.poo.checker.Checker;
import org.poo.checker.CheckerConstants;
import org.poo.commerciant.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;
import org.poo.bank.Bank;
import org.poo.operations.ExchangeRate;
import org.poo.operations.ExchangeRateManager;
import org.poo.users.User;
import org.poo.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The entry point to this homework. It runs the checker that tests your implementation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        var sortedFiles = Arrays.stream(Objects.requireNonNull(directory.listFiles())).
                sorted(Comparator.comparingInt(Main::fileConsumer))
                .toList();

        for (File file : sortedFiles) {
            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
                              final String filePath2) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(CheckerConstants.TESTS_PATH + filePath1);
        ObjectInput inputData = objectMapper.readValue(file, ObjectInput.class);

        ExchangeRateManager exchangeRateManager = ExchangeRateManager.getInstance();
        exchangeRateManager.loadExchangeRates(List.of(inputData.getExchangeRates()));

        ArrayNode output = objectMapper.createArrayNode();


        /*
         * TODO Implement your function here
         *
         * How to add output to the output array?
         * There are multiple ways to do this, here is one example:
         *
         * ObjectMapper mapper = new ObjectMapper();
         *
         * ObjectNode objectNode = mapper.createObjectNode();
         * objectNode.put("field_name", "field_value");
         *
         * ArrayNode arrayNode = mapper.createArrayNode();
         * arrayNode.add(objectNode);
         *
         * output.add(arrayNode);
         * output.add(objectNode);
         *
         */
        Bank bank = new Bank(inputData);

        for (CommandInput command : inputData.getCommands()) {
            var objectNode = objectMapper.createObjectNode();
            objectNode.put("command", command.getCommand());
            objectNode.put("timestamp", command.getTimestamp());


            switch (command.getCommand()) {
                case "printUsers" -> {
                    var usersOutput = objectMapper.createArrayNode();
                    for (Map<String, Object> user : bank.processCommand(command)) {
                        usersOutput.add(objectMapper.valueToTree(user));
                    }
                    objectNode.set("output", usersOutput);
                    output.add(objectNode);
                }
                case "addAccount", "createCard", "addFunds" -> {
                    bank.processCommand(command); // Procesăm comanda dar nu adăugăm nimic în output
                }
                case "deleteAccount" -> {
                    // Procesăm comanda de ștergere a contului
                    Map<String, Object> deleteAccountResponse = bank.deleteAccount(command);

                    // Creăm nodul principal pentru răspuns
                    var responseNode = objectMapper.createObjectNode();
                    responseNode.put("command", command.getCommand());
                    responseNode.put("timestamp", command.getTimestamp());

                    // Dacă există un mesaj de eroare, îl adăugăm în răspuns
                    if (deleteAccountResponse.containsKey("error")) {
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("error", deleteAccountResponse.get("error").toString());
                        errorNode.put("timestamp", command.getTimestamp());

                        responseNode.set("output", errorNode);
                    } else {
                        // Dacă ștergerea a fost un succes, adăugăm răspunsul de succes
                        var successNode = objectMapper.createObjectNode();
                        successNode.put("success", "Account deleted");
                        successNode.put("timestamp", command.getTimestamp());

                        responseNode.set("output", successNode);
                    }

                    // Adăugăm obiectul complet în output
                    output.add(responseNode);
                }


                case "createOneTimeCard" -> {
                    bank.processCommand(command);
                }

                case "deleteCard" -> {
                    bank.processCommand(command); // Procesăm comanda, dar nu adăugăm nimic în output
                }

                case "setMinimumBalance" -> {
                    bank.processCommand(command);
                }

                case "payOnline" -> {
                    List<Map<String, Object>> response = bank.payOnline(command);
                    if (!response.isEmpty()) { // Adăugăm doar dacă există erori sau mesaje de problemă
                        // Iterăm prin fiecare eroare și adăugăm direct în nodul "output"
                        for (Map<String, Object> line : response) {
                            var responseNode = objectMapper.createObjectNode();
                            responseNode.put("description", "Card not found");
                            responseNode.put("timestamp", command.getTimestamp());

                            objectNode.set("output", responseNode);
                            objectNode.put("timestamp", command.getTimestamp());

                            output.add(objectNode);
                        }
                    }
                }

                case "sendMoney" -> {
                    try {
                        bank.sendMoney(command);
                    } catch (IllegalArgumentException e) {
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("description", e.getMessage());
                        errorNode.put("timestamp", command.getTimestamp());
                        objectNode.set("output", errorNode);
                        output.add(objectNode);
                    }
                }

                case "setAlias" -> {
                    bank.processCommand(command);
                }

                case "printTransactions" -> {
                    var transactions = bank.printTransactions(command);
                    // Setează doar tranzacțiile ca output, fără a adăuga structuri suplimentare
                    objectNode.set("output", objectMapper.valueToTree(transactions));
                    output.add(objectNode);
                }


                case "checkCardStatus" -> {
                    Map<String, Object> response = bank.checkCardStatus(command);

                    if (response.containsKey("output")) {
                        ObjectNode objectNodeCheckCardStatus = objectMapper.createObjectNode();

                        objectNodeCheckCardStatus .put("command", response.get("command").toString());
                        objectNodeCheckCardStatus .set("output", objectMapper.valueToTree(response.get("output")));
                        objectNodeCheckCardStatus .put("timestamp", Integer.parseInt(response.get("timestamp").toString()));

                        output.add(objectNodeCheckCardStatus);
                    }
                }

                case "changeInterestRate" -> {
                    List<Map<String, Object>> response = bank.changeInterestRate(command);

                    // Procesăm doar erorile, fără să adăugăm nimic la output în caz de succes
                    if (!response.isEmpty()) {
                        for (Map<String, Object> line : response) {
                            ObjectNode responseNode = objectMapper.createObjectNode();
                            responseNode.put("command", line.get("command").toString());
                            responseNode.set("output", objectMapper.valueToTree(line.get("output")));
                            responseNode.put("timestamp", Integer.parseInt(line.get("timestamp").toString()));

                            output.add(responseNode);
                        }
                    }
                }


                case "splitPayment" -> {
                    bank.processCommand(command);
                }

                case "report" -> {
                    Map<String, Object> response = bank.generateReport(command);
                    var objectNodeReport = objectMapper.createObjectNode();
                    objectNodeReport.put("command", response.get("command").toString());
                    objectNodeReport.put("timestamp", Integer.parseInt(response.get("timestamp").toString()));

                    if (response.containsKey("output")) {
                        objectNodeReport.set("output", objectMapper.valueToTree(response.get("output")));
                    } else {
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("description", "Account not found");
                        errorNode.put("timestamp", command.getTimestamp());
                        objectNodeReport.set("output", errorNode);
                    }

                    output.add(objectNodeReport);
                }


                case "spendingsReport" -> {
                    List<Map<String, Object>> response = bank.processCommand(command);

                    if (!response.isEmpty()) {
                        for (Map<String, Object> line : response) {
                            ObjectNode responseNode = objectMapper.createObjectNode();
                            responseNode.put("command", line.get("command").toString());

                            // Procesăm ieșirea "output"
                            if (line.get("output") != null) {
                                responseNode.set("output", objectMapper.valueToTree(line.get("output")));
                            }

                            responseNode.put("timestamp", Integer.parseInt(line.get("timestamp").toString()));
                            output.add(responseNode);
                        }
                    }
                }


                case "addInterest" -> { // Cazul pentru addInterest
                    List<Map<String, Object>> interestResponse = bank.processCommand(command);
                    if (!interestResponse.isEmpty()) {
                        for (Map<String, Object> line : interestResponse) {
                            ObjectNode responseNode = objectMapper.createObjectNode();
                            responseNode.put("command", line.get("command").toString());
                            responseNode.set("output", objectMapper.valueToTree(line.get("output")));
                            responseNode.put("timestamp", Integer.parseInt(line.get("timestamp").toString()));
                            output.add(responseNode);
                        }
                    }
                }




                default -> {
                    objectNode.put("type", "error");
                    objectNode.put("message", "Unknown command: " + command.getCommand());
                    output.add(objectNode);
                }
            }
        }

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), output);
        Utils.resetRandom();
    }

    /**
     * Method used for extracting the test number from the file name.
     *
     * @param file the input file
     * @return the extracted numbers
     */
    public static int fileConsumer(final File file) {
        String fileName = file.getName()
                .replaceAll(CheckerConstants.DIGIT_REGEX, CheckerConstants.EMPTY_STR);
        return Integer.parseInt(fileName.substring(0, 2));
    }
}