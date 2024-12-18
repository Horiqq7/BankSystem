package org.poo.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.commands.print_commands.PrintTransactions;
import org.poo.bank.commands.account_commands.card_commands.CheckCardStatus;
import org.poo.checker.Checker;
import org.poo.checker.CheckerConstants;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;
import org.poo.bank.Bank;
import org.poo.bank.exchange_rates.ExchangeRateManager;
import org.poo.bank.users.User;
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
                case "addAccount", "createCard", "addFunds", "createOneTimeCard", "deleteCard", "setMinimumBalance", "setAlias" -> {
                    bank.processCommand(command);
                }
                case "deleteAccount" -> {
                    Map<String, Object> deleteAccountResponse = bank.processCommand(command).get(0); // Obține rezultatul
                    var responseNode = objectMapper.createObjectNode();
                    responseNode.put("command", command.getCommand());
                    responseNode.put("timestamp", command.getTimestamp());

                    if (deleteAccountResponse.containsKey("error")) {
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("error", deleteAccountResponse.get("error").toString());
                        errorNode.put("timestamp", command.getTimestamp());
                        responseNode.set("output", errorNode);
                    } else {
                        var successNode = objectMapper.createObjectNode();
                        successNode.put("success", "Account deleted");
                        successNode.put("timestamp", command.getTimestamp());
                        responseNode.set("output", successNode);
                    }

                    output.add(responseNode);
                }

                case "payOnline" -> {
                    List<Map<String, Object>> response = bank.processCommand(command);
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
                        bank.processCommand(command);
                    } catch (IllegalArgumentException e) {
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("description", e.getMessage());
                        errorNode.put("timestamp", command.getTimestamp());
                        objectNode.set("output", errorNode);
                        output.add(objectNode);
                    }
                }


                case "printTransactions" -> {
                    // Extrage lista de utilizatori din obiectul Bank
                    List<User> users = bank.getUsers();

                    // Creăm instanța de PrintTransactions cu lista de utilizatori
                    PrintTransactions printTransactionsProcessor = new PrintTransactions(users);

                    // Apoi apelăm metoda printTransactions
                    var transactions = printTransactionsProcessor.printTransactions(command);

                    // Setează doar tranzacțiile ca output, fără a adăuga structuri suplimentare
                    objectNode.set("output", objectMapper.valueToTree(transactions));
                    output.add(objectNode);
                }


                case "checkCardStatus" -> {
                    CheckCardStatus checkCardStatus = new CheckCardStatus();
                    Map<String, Object> checkCardStatusResponse = checkCardStatus.execute(command, bank.getUsers());

                    if (!checkCardStatusResponse.isEmpty()) {
                        ObjectNode responseNode = objectMapper.createObjectNode();
                        responseNode.put("command", checkCardStatusResponse.get("command").toString());
                        responseNode.set("output", objectMapper.valueToTree(checkCardStatusResponse.get("output")));
                        responseNode.put("timestamp", Integer.parseInt(checkCardStatusResponse.get("timestamp").toString()));

                        output.add(responseNode); // Adăugăm răspunsul în output
                    }
                }


                case "changeInterestRate" -> {
                    List<Map<String, Object>> response = bank.processCommand(command); // Apelăm processCommand pentru a obține rezultatul

                    // Procesăm doar erorile, fără să adăugăm nimic la output în caz de succes
                    if (!response.isEmpty()) {
                        for (Map<String, Object> line : response) {
                            ObjectNode responseNode = objectMapper.createObjectNode();
                            responseNode.put("command", line.get("command").toString());
                            responseNode.set("output", objectMapper.valueToTree(line.get("output")));
                            responseNode.put("timestamp", Integer.parseInt(line.get("timestamp").toString()));

                            output.add(responseNode); // Adăugăm răspunsul în output
                        }
                    }
                }

                case "splitPayment" -> {
                    bank.processCommand(command);
                }

                case "report", "spendingsReport" -> {
                    // Procesăm comanda prin `Bank`, care folosește AbstractReportCommand
                    List<Map<String, Object>> response = bank.processCommand(command);

                    for (Map<String, Object> line : response) {
                        ObjectNode responseNode = objectMapper.createObjectNode();

                        // Adăugăm comanda în răspuns
                        responseNode.put("command", line.get("command").toString());

                        // Adăugăm output-ul (dacă există)
                        if (line.containsKey("output")) {
                            responseNode.set("output", objectMapper.valueToTree(line.get("output")));
                        }

                        // Adăugăm timestamp-ul
                        responseNode.put("timestamp", Integer.parseInt(line.get("timestamp").toString()));

                        // Adăugăm nodul răspuns în lista finală de output
                        output.add(responseNode);
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