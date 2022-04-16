package newbank.server;

import newbank.server.exception.accountname.AccountNameException;
import newbank.server.exception.transfer.InsufficientFundsException;
import newbank.server.exception.username.UsernameException;
import newbank.server.exception.exit.ExitProcessException;
import newbank.server.exception.exit.LogoutException;
import newbank.server.microloan.MicroLoanRequest;
import newbank.server.microloan.Microloan;
import newbank.server.microloan.MicroloanManagement;
import newbank.server.microloan.MicroloanOffer;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class NewBankClientHandler extends Thread {
    private NewBank bank;
    private BufferedReader in;
    private PrintWriter out;

    private final static String pathMicroloan = "./newbank/microloan.csv";
    private final static String pathMicroloanOffer ="./newbank/microloan_offer.csv";
    private final static String pathMicroloanAccount ="./newbank/account.csv";
    private final static String pathMicroloanReq ="./newbank/microloan_request.csv";

    public NewBankClientHandler(Socket s) throws IOException {

        bank = NewBank.getBank();
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream(), true);
    }

    public String requestUserInput() throws ExitProcessException, IOException {
        String userInput = in.readLine();
        if (userInput.equals("x")) {
            throw new ExitProcessException();
        } else {
            return userInput;
        }
    }

    public void run() {
        mainMenu();
    }

    public void mainMenu() {
        // Keep getting requests from the client and processing them
        try {
            while (true) {
                try {
                    // Ask for username
                    out.println("Welcome to NewBank, please choose from the following options to get started: ");
                    out.println("[1] - Sign in");
                    out.println("[2] - Create new NewBank user");
                    String userInput = in.readLine();
                    switch (userInput) {
                        case "1" -> login();
                        case "2" -> registerCustomer();
                    }
                } catch (ExitProcessException ignored) {

                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Method that proceeds with identifying existing accounts
     */
    private void login() throws ExitProcessException {
        try {
            Integer customerId;
            while (true) {
                out.println("Please enter your username. Press \"x\" to exit");
                String username = requestUserInput();
                customerId = bank.usernameExists(username);
                if (customerId == null) {
                    out.println(String.format("No customer account with the username \"%s\" exists", username));
                    continue;
                }
                break;
            }
            while (true) {
                out.println("Please enter your password. Press \"x\" to exit");
                String password = requestUserInput();
                boolean passwordCorrect = bank.checkPassword(customerId, password);
                if (!passwordCorrect) {
                    out.println("Password incorrect");
                    continue;
                }
                break;
            }
            bank.setLoggedInCustomer(customerId);
            loggedInUserLoop();
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (LogoutException ignored) {
        }
    }

    /**
     * Method that registers new Customer, and once account registered it will loop to existingAccount Method to prevent termination.
     */
    private void registerCustomer() throws ExitProcessException {
        try {
            // Get username
            boolean usernameValid = false;
            String username = null;
            while (!usernameValid) {
                out.println("Please enter a new username. Press \"x\" to exit");
                username = requestUserInput();
                try {
                    usernameValid = bank.isUsernameValid(username);
                } catch (UsernameException ex) {
                    out.println(ex.getMessage());
                }
            }
            // Get password
            boolean passwordValid = false;
            String password = null;
            while (!passwordValid) {
                out.println("Please enter a password. Press \"x\" to exit");
                out.println("GUIDANCE: password must be between 8-25 characters long and must include at least one uppercase letter, one lowercase letter, one number and one special character");
                password = requestUserInput();
                passwordValid = bank.isPasswordValid(password);
            }
            // Get initial deposit
            double initialDeposit;
            while (true) {
                out.println("How much would you like to deposit in your main account? Press \"x\" to exit");
                String depositInput = requestUserInput();
                try {
                    initialDeposit = Double.parseDouble(depositInput);
                    break;
                } catch (NumberFormatException ex) {
                    out.println("Please enter a valid number");
                }
            }
            bank.addCustomer(username, password, initialDeposit);
            out.println(String.format("Customer account %s created, with an initial deposit of £%.2f", username, initialDeposit));
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

    private void createBankAccount() throws ExitProcessException {
        try {
            // Get account name
            boolean accountNameValid = false;
            String accountName = null;
            while (!accountNameValid) {
                out.println("What would you like to call the account? Press \"x\" to exit");
                accountName = requestUserInput();
                try {
                    accountNameValid = bank.isAccountNameValid(accountName);
                } catch (AccountNameException ex) {
                    out.println(ex.getMessage());
                }
            }
            // Get deposit
            double deposit;
            while (true) {
                out.println(String.format("How much would you like to deposit in the account \"%s\"? Press \"x\" to exit", accountName));
                String depositInput = requestUserInput();
                try {
                    deposit = Double.parseDouble(depositInput);
                    break;
                } catch (NumberFormatException ex) {
                    out.println("Please enter a valid number");
                }
            }
            bank.addAccount(accountName, deposit);
            out.println(String.format("Account \"%s\" successfully created!", accountName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void deleteBankAccount() throws ExitProcessException {
        try {
            // Get account name
            String accountName;
            ArrayList<Account> accounts = bank.loggedInCustomer.getAccounts();
            List<String> accountNames = accounts.stream().map(Account::getName).toList();
            String accountNameString = accounts.stream().map(Account::getName).collect(Collectors.joining(", "));
            out.println(String.format("The names of your accounts are as follows: %s", accountNameString));
            while (true) {
                out.println("Enter the name of the account you would like to delete. Press \"x\" to exit");
                accountName = requestUserInput();
                boolean accountNameExists = accountNames.contains(accountName);
                if (accountName.equals("main")) {
                    out.println("Cannot delete main account");
                } else if (!accountNameExists) {
                    out.println(String.format("No account with the name \"%s\" exists", accountName));
                } else {
                    break;
                }
            }
            bank.deleteAccount(accountName);
            out.println(String.format("Account \"%s\" successfully deleted", accountName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void internalTransfer() throws ExitProcessException {
        try {
            while (true) {
                ArrayList<Account> accounts = bank.loggedInCustomer.getAccounts();
                if (accounts.size() < 2) {
                    out.println("You need at least two accounts to make an internal transfer");
                    return;
                }
                List<String> accountNames = accounts.stream().map(Account::getName).toList();
                String accountNameString = accounts.stream().map(Account::getName).collect(Collectors.joining(", "));
                out.println(String.format("The names of your accounts are as follows: %s", accountNameString));
                String fromAccountName;
                while (true) {
                    out.println("Enter the name of the account you would like to transfer money from. Press \"x\" to exit");
                    fromAccountName = requestUserInput();
                    boolean accountNameExists = accountNames.contains(fromAccountName);
                    if (!accountNameExists) {
                        out.println(String.format("No account with the name \"%s\" exists", fromAccountName));
                        continue;
                    }
                    break;
                }
                String toAccountName;
                while (true) {
                    out.println("Enter the name of the account you would like to transfer money to. Press \"x\" to exit");
                    toAccountName = requestUserInput();
                    boolean accountNameExists = accountNames.contains(toAccountName);
                    if (!accountNameExists) {
                        out.println(String.format("No account with the name \"%s\" exists", toAccountName));
                        continue;
                    }
                    if (toAccountName.equals(fromAccountName)) {
                        out.println("Recipient account cannot be the same as donor account");
                        continue;
                    }
                    break;
                }
                double amount;
                while (true) {
                    out.println(String.format("How much would like to transfer from \"%s\" to \"%s\"? Press \"x\" to exit", fromAccountName, toAccountName));
                    String amountInput = requestUserInput();
                    try {
                        amount = Double.parseDouble(amountInput);
                        break;
                    } catch (NumberFormatException ex) {
                        out.println("Please enter a valid number");
                    }
                }
                try {
                    bank.internalTransfer(fromAccountName, toAccountName, amount);
                } catch (InsufficientFundsException ex) {
                    out.println(ex.getMessage());
                    continue;
                }
                out.println(String.format(
                        "£%.2f successfully transferred from account \"%s\" to account \"%s\"",
                        amount,
                        fromAccountName,
                        toAccountName
                ));
                break;
            }

            //out.println(String.format("Account \"%s\" successfully deleted!", accountName));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void externalTransfer() throws ExitProcessException {
        try {
            while (true) {
                // Get customer to transfer to
                List<String> customerNames = bank.getCustomerNames();
                String toCustomerName;
                while (true) {
                    out.println("Enter the username of the customer you would like to transfer money to. Press \"x\" to exit");
                    toCustomerName = requestUserInput();
                    boolean customerNameExists = customerNames.contains(toCustomerName);
                    if (!customerNameExists) {
                        out.println(String.format("No customer with the name \"%s\" exists", toCustomerName));
                    } else if (toCustomerName.equals(bank.loggedInCustomer.getUsername())) {
                        out.println("If you want to transfer money to yourself, please select \"Transfer money between my accounts\" from the main menu");
                    } else {
                        break;
                    }
                }
                // Get bank account to transfer from
                ArrayList<Account> accounts = bank.loggedInCustomer.getAccounts();
                List<String> accountNames = accounts.stream().map(Account::getName).toList();
                String accountNameString = accounts.stream().map(Account::getName).collect(Collectors.joining(", "));
                out.println(String.format("The names of your accounts are as follows: %s", accountNameString));
                String fromAccountName;
                while (true) {
                    out.println("Enter the name of the account you would like to transfer money from. Press \"x\" to exit");
                    fromAccountName = requestUserInput();
                    boolean accountNameExists = accountNames.contains(fromAccountName);
                    if (!accountNameExists) {
                        out.println(String.format("No account with the name \"%s\" exists", fromAccountName));
                        continue;
                    }
                    break;
                }
                // Get amount to transfer
                Account fromAccount = bank.loggedInCustomer.getAccountByName(fromAccountName);
                double balance = fromAccount.getBalance();
                double amount;
                while (true) {
                    out.println(String.format("There is £%.2f in the account \"%s\". How much would like to transfer to \"%s\"? Press \"x\" to exit", balance, fromAccountName, toCustomerName));
                    String amountInput = requestUserInput();
                    try {
                        amount = Double.parseDouble(amountInput);
                        break;
                    } catch (NumberFormatException ex) {
                        out.println("Please enter a valid number");
                    }
                }
                // Do transfer
                try {
                    bank.externalTransfer(toCustomerName, fromAccountName, amount);
                } catch (InsufficientFundsException ex) {
                    out.println(ex.getMessage());
                    continue;
                }
                out.println(String.format(
                        "£%.2f successfully transferred from account \"%s\" to customer \"%s\"",
                        amount,
                        fromAccountName,
                        toCustomerName
                ));
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print available bank menu options, to be updated should future features be introduced.
     */
    private void displayLoggedInUserOptions() {
        out.println(
                "Welcome to NewBank, what would you like to do? \r" +
                        "[1] - Show my bank accounts\r" +
                        "[2] - Create bank account\r" +
                        "[3] - Delete bank account\r" +
                        "[4] - Transfer money between my accounts\r" +
                        "[5] - Transfer money to another customer\r" +
                        "[6] - Choose and purchase a microloan offer \r" +
                        "[7] - Request a microloan \r" +
                        "[8] - Accept a microloan \r" +
                        "[9] - Display your active microloans\r" +
                        "[10] - Log out"
        );
    }

    private void chooseOffer() throws IOException, InterruptedException {
        out.println("Below are the available offers  -- Please choose the desired offer by its id");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(pathMicroloanOffer))) {
            String readLine;
            AtomicReference<HashMap<String, String>> attributesMap = new AtomicReference<>(new HashMap<>());
            List<MicroloanOffer> microloanOfferList = new ArrayList<>();
            while ((readLine = bufferedReader.readLine()) != null) {
                Arrays.stream(readLine.split(",")).map(String::trim).toList().forEach(e ->
                        {
                            String[] pair = e.split(":");
                            attributesMap.get().put(pair[0], pair[1]);
                        }
                );
                MicroloanOffer microloanOffer = new MicroloanOffer();
                microloanOffer.setCustomerId(Integer.parseInt(attributesMap.get().get("customerId")));
                microloanOffer.setAmount(Float.parseFloat(attributesMap.get().get("amount")));
                microloanOffer.setInterestRate(Float.parseFloat(attributesMap.get().get("interestRate")));
                microloanOffer.setId(Integer.parseInt(attributesMap.get().get("id")));
                microloanOfferList.add(microloanOffer);
                attributesMap.set(new HashMap<>());
            }
            List<MicroloanOffer> microloanOffers = microloanOfferList.stream().filter(e -> e.getCustomerId() != bank.loggedInCustomer.getId()).collect(Collectors.toList());
            if(microloanOffers.isEmpty()){
                out.println("Empty offers");
                Thread.sleep(2000);
                return;
            }
            microloanOffers.forEach(e -> out.println(e));
            String choice = in.readLine();
            Optional<MicroloanOffer> offer = microloanOfferList.stream().filter(e -> e.getId() == Integer.parseInt(choice)).findFirst();
            if (offer.isEmpty()) {
                out.println("Error occurred in offers");
                Thread.sleep(2000);
                return;
            }
            if(bank.getCustomers().get(offer.get().getCustomerId()) == null){
                out.println("No existing customer in specific offer");
                Thread.sleep(2000);
                return;
            }
            Account account = bank.getCustomers().get(offer.get().getCustomerId()).getAccountByName("main");
            if (account.getBalance() < offer.get().getAmount()) {
                out.println("There are not enough funds");
                Thread.sleep(2000);
                return;
            }
            out.println("Proceed with transfer..");
            Double previousLoanerAmount = bank.getCustomers().get(offer.get().getCustomerId()).getAccountByName("main").getBalance();
            Double previousTakerAmount = bank.getCustomers().get(bank.loggedInCustomer.getId()).getAccountByName("main").getBalance();
            bank.getCustomers().get(offer.get().getCustomerId()).getAccountByName("main").setBalance(account.getBalance() - offer.get().getAmount());
            bank.getCustomers().get(bank.loggedInCustomer.getId()).getAccountByName("main").setBalance(previousTakerAmount + offer.get().getAmount());
            replaceBalanceInFiles(previousLoanerAmount, previousTakerAmount, bank.getCustomers().get(offer.get().getCustomerId()), bank.getCustomers().get(bank.loggedInCustomer.getId()));
            Microloan microloan = new Microloan();
            microloan.setAmount(offer.get().getAmount());
            microloan.setInterest(offer.get().getInterestRate());
            microloan.setRepaid(false);
            microloan.setLoaner(bank.getCustomers().get(offer.get().getCustomerId()));
            microloan.setTaker(bank.getCustomers().get(bank.loggedInCustomer.getId()));
            microloan.setDateOfExpiry(LocalDateTime.now().plusWeeks(1).toString());
            storeMicroLoan(microloan);
            removeLine( offer.get().getId() , pathMicroloanOffer);
            bank.getMicroloanManagement().getMicroloanList().add(microloan);
            out.println("Transaction Completed -- Redirecting to Main menu");
            Thread.sleep(2000);
        }
    }

    private void storeMicroLoan(Microloan microloan) throws IOException {
        try (FileWriter fw = new FileWriter(pathMicroloan, true); BufferedWriter bw = new BufferedWriter(fw)) {
            StringBuilder sb = new StringBuilder();
            sb.append("fromCustomerId:" + microloan.getLoaner().getId() + ",");
            sb.append("toCustomerId:" + microloan.getTaker().getId() + ",");
            sb.append("amount:" + microloan.getAmount() + ",");
            sb.append("interestRate:" + microloan.getInterest() + ",");
            sb.append("expiryDate:" + microloan.getDateOfExpiry() + ",");
            sb.append("rePaid:" + microloan.getRepaid() + ",");
            sb.append("\n");
            bw.write(sb.toString());
        }
    }

    private void replaceBalanceInFiles(Double previousLoanerAmount, Double previousTakerAmount, Customer loaner, Customer taker) throws IOException {

        HashMap<String, String> oldToNewLine = new HashMap<>();
        List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(pathMicroloanAccount), StandardCharsets.UTF_8));
        Optional<String> lineToChangeLoaner = fileContent.stream().filter(e -> e.contains("id:" + loaner.getAccountByName("main").getId())).findFirst();
        if (lineToChangeLoaner.isEmpty()) {
            out.println("Loaner funds insufficient");
            return;
        }
        oldToNewLine.put(lineToChangeLoaner.get(), lineToChangeLoaner.get().replace("balance:" + previousLoanerAmount, "balance:" + loaner.getAccountByName("main").getBalance()));
        Optional<String> lineToChangeTaker = fileContent.stream().filter(e -> e.contains("id:" + taker.getAccountByName("main").getId())).findFirst();
        if (lineToChangeTaker.isEmpty()) {
            out.println("Taker funds insufficient");
            return;
        }
        oldToNewLine.put(lineToChangeTaker.get(), lineToChangeTaker.get().replace("balance:" + previousTakerAmount, "balance:" + (taker.getAccountByName("main").getBalance())));
        fileContent.forEach(e ->
                {
                    oldToNewLine.forEach((key, value) -> {
                        if (e.equals(key)) {
                            fileContent.set(fileContent.indexOf(e), value);
                        }
                    });
                }
        );

        Files.write(Paths.get(pathMicroloanAccount), fileContent, StandardCharsets.UTF_8);
    }

    private void requestMicroLoan() {

        try {
            out.println("Please submit the desired amount");
            String amount = in.readLine();
            out.println("Please submit the desired interest rate");
            String interestRate = in.readLine();
            if (Float.parseFloat(interestRate) > 0.5) {
                out.println("Not a valid interestRate --> Redirecting to main menu");
                Thread.sleep(2000);
                return;
            }
            MicroLoanRequest microLoanRequest = new MicroLoanRequest();
            microLoanRequest.setAmount(Float.parseFloat(amount));
            microLoanRequest.setCustomer(bank.getLoggedInCustomer());
            microLoanRequest.setInterestRate(Float.parseFloat(interestRate));
            bank.getMicroloanManagement().getMicroLoanRequestList().add(microLoanRequest);
            persistMicroloanReq(microLoanRequest);
        } catch (Exception e) {
            out.println("Please check again your input");
        }
    }

    private void persistMicroloanReq(MicroLoanRequest microLoanRequest) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(pathMicroloanReq));
        String line;
        String last = "";
        int id = 1;
        while ((line = input.readLine()) != null) {
            last = line;
        }
        input.close();
        if (!last.isEmpty()) {
            last = last.substring(0, last.indexOf(",")).trim();
            String[] pair = last.split(":");
            id = Integer.parseInt(pair[1]) + 1;
        }

        try {
            FileWriter fw = new FileWriter(pathMicroloanReq, true);
            BufferedWriter bw = new BufferedWriter(fw);
            StringBuilder sb = new StringBuilder();
            sb.append("id:" + id + ",");
            sb.append("customerId:" + microLoanRequest.getCustomer().getId() + ",");
            sb.append("amount:" + microLoanRequest.getAmount() + ",");
            sb.append("interestRate:" + microLoanRequest.getInterestRate());
            sb.append("\n");
            bw.write(sb.toString());
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void acceptMicroloan() throws IOException {
        try {
            out.println("Below are the available microloan requests -- Please choose the desired request by its id");
            List<MicroLoanRequest> microLoanRequestList = bank.getMicroloanManagement().getMicroLoanRequestList();
            microLoanRequestList= microLoanRequestList.stream().filter(e->e.getCustomerId() != bank.loggedInCustomer.getId()).collect(Collectors.toList());
            if(microLoanRequestList.isEmpty()){
                out.println("Empty requests");
                Thread.sleep(2000);
                return;
            }
            microLoanRequestList.forEach(e -> out.println(e));
            String userChoice = in.readLine();
            Optional<MicroLoanRequest> microLoanRequest = microLoanRequestList.stream().filter(e -> e.getId() == Integer.parseInt(userChoice)).findFirst();
            if (microLoanRequest.isEmpty()) {
                out.println("no request matches");
                Thread.sleep(2000);
                return;
            }
            Account account = bank.getCustomers().get(bank.loggedInCustomer.getId()).getAccountByName("main");
            if (account.getBalance() < microLoanRequest.get().getAmount()) {
                Thread.sleep(2000);
                out.println("There are not enough funds");
                return;
            }
            if(bank.getCustomers().get(microLoanRequest.get().getCustomerId()) == null){
                out.println("No existing customer in specific request");
                Thread.sleep(2000);
                return;
            }
            out.println("Proceed with transfer..");
            Double previousTakerAmount = bank.getCustomers().get(microLoanRequest.get().getCustomerId()).getAccountByName("main").getBalance();
            Double previousLoanerAmount = bank.getCustomers().get(bank.loggedInCustomer.getId()).getAccountByName("main").getBalance();
            bank.getCustomers().get(microLoanRequest.get().getCustomerId()).getAccountByName("main").setBalance(previousTakerAmount + microLoanRequest.get().getAmount());
            bank.getCustomers().get(bank.loggedInCustomer.getId()).getAccountByName("main").setBalance(previousLoanerAmount - microLoanRequest.get().getAmount());
            replaceBalanceInFiles(previousLoanerAmount, previousTakerAmount, bank.getCustomers().get(bank.loggedInCustomer.getId()), bank.getCustomers().get(microLoanRequest.get().getCustomerId()));
            Microloan microloan = new Microloan();
            microloan.setAmount(microLoanRequest.get().getAmount());
            microloan.setInterest(microLoanRequest.get().getInterestRate());
            microloan.setRepaid(false);
            microloan.setLoaner(bank.getCustomers().get(bank.loggedInCustomer.getId()));
            microloan.setTaker(bank.getCustomers().get(microLoanRequest.get().getCustomerId()));
            microloan.setDateOfExpiry(LocalDateTime.now().plusWeeks(1).toString());
            storeMicroLoan(microloan);
            removeLine(microLoanRequest.get().getId(),pathMicroloanReq);
            bank.getMicroloanManagement().getMicroLoanRequestList().remove(microLoanRequest.get());
            bank.getMicroloanManagement().getMicroloanList().add(microloan);
            out.println("Transaction Completed -- Redirecting to Main menu");
            Thread.sleep(3000);
        } catch (Exception e) {
            out.println("Please check again your input");
        }

    }
    public void removeLine(Integer id,String path) throws IOException
    {
        File file = new File(path);
        List<String> out = Files.lines(file.toPath())
                .filter(line -> !line.contains("id:"+id))
                .collect(Collectors.toList());
        Files.write(file.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void displayActiveMicroloans() throws IOException {
        List<Microloan> microLoanRequestList = bank.getMicroloanManagement().getMicroloanList();

        out.println("Active requests");
        List<Microloan> microloansReq = microLoanRequestList.stream().filter(e -> e.getTaker().getId() == bank.loggedInCustomer.getId()).collect(Collectors.toList());
        microloansReq.forEach(e -> out.println(e));
        List<Microloan> microloansAcc = microLoanRequestList.stream().filter(e -> e.getLoaner().getId() == bank.loggedInCustomer.getId()).collect(Collectors.toList());
        out.println("Active accepted requests");
        microloansAcc.forEach(e -> out.println(e));
        while (true) {
            out.println("If you want to exit type exit");
            String exit = in.readLine();
            if(exit.equals("exit")){
                break;
            }
        }
    }

    /**
     * Process logged-in user requests in an interminable loop
     */
    public void loggedInUserLoop() throws IOException, LogoutException {
        while (true) {
            try {
                bank.persistData();
                displayLoggedInUserOptions();
                String request = in.readLine();
                switch (request) {
                    case "1" -> out.println(bank.loggedInCustomer.accountsToString());
                    case "2" -> createBankAccount();
                    case "3" -> deleteBankAccount();
                    case "4" -> internalTransfer();
                    case "5" -> externalTransfer();
                    case "6" -> chooseOffer();
                    case "7" -> requestMicroLoan();
                    case "8" -> acceptMicroloan();
                    case "9" -> displayActiveMicroloans();
                    case "10" -> throw new LogoutException();

                }
            } catch (ExitProcessException | InterruptedException ignored) {
            }
        }
    }
}
