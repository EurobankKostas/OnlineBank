package newbank.server;

import newbank.server.exception.accountname.AccountNameCharacterException;
import newbank.server.exception.accountname.AccountNameException;
import newbank.server.exception.accountname.AccountNameExistsException;
import newbank.server.exception.accountname.AccountNameLengthException;
import newbank.server.exception.transfer.InsufficientFundsException;
import newbank.server.exception.username.UsernameCharacterException;
import newbank.server.exception.username.UsernameException;
import newbank.server.exception.username.UsernameExistsException;
import newbank.server.exception.username.UsernameLengthException;
import newbank.server.microloan.MicroloanManagement;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class NewBank {
	private static final NewBank bank = new NewBank();
	public Customer loggedInCustomer;
	private HashMap<Integer, Customer> customers;
	private HashMap<Integer, Account> accounts;
	private DatabaseHandler databaseHandler;
	private MicroloanManagement microloanManagement;

	public NewBank() {

		databaseHandler = new DatabaseHandler();
		customers = databaseHandler.getCustomers();
		accounts = databaseHandler.getAccounts();
		microloanManagement = new MicroloanManagement();
		linkCustomersToAccounts();
		microloanManagement.setMicroLoanRequestList(databaseHandler.microLoanRequestList());
		if(microloanManagement.getMicroLoanRequestList() == null || microloanManagement.getMicroLoanRequestList().isEmpty()) {
			microloanManagement.setMicroLoanRequestList(new ArrayList<>());
		}
		microloanManagement.setMicroloanList(databaseHandler.microloans());
		if(microloanManagement.getMicroloanList() == null || microloanManagement.getMicroloanList().isEmpty()) {
			microloanManagement.setMicroloanList(new ArrayList<>());
		}
		loggedInCustomer = null;
	}

	public void linkCustomersToAccounts() {
		for (Customer customer : customers.values()) {
			for (Account account : accounts.values()) {
				if (customer.getId() == account.getCustomerId()) {
					customer.addAccount(account);
				}
			}
		}
	}
	
	public static NewBank getBank() {
		return bank;
	}

	public void setLoggedInCustomer(int customerId) {
		loggedInCustomer = customers.get(customerId);
	}

	public Integer usernameExists(String username) {
		for (Customer customer : customers.values()) {
			if (customer.getUsername().equals(username)) {
				return customer.getId();
			}
		}
		return null;
	}

	public Integer accountNameExists(ArrayList<Account> accounts, String accountName) {
		for (Account account : accounts) {
			if (account.getName().equals(accountName)) {
				return account.getId();
			}
		}
		return null;
	}

	public synchronized boolean checkPassword(int customerId, String password) throws NoSuchAlgorithmException {
		Customer customer = customers.get(customerId);
		return customer.getPassword().equals(hashPassword(password));
	}

	/** Boolean method to check if new customer username is valid */
	public boolean isUsernameValid(String username) throws UsernameException {
		// Username must be between 5 and 25 characters in length
		if (username.length() < 5 || username.length() > 25) {
			throw new UsernameLengthException();
		}
		for (char c : username.toCharArray()) {
			// Username must consist only of alphabetic characters or numeric digits
			if (!(Character.isAlphabetic(c) || Character.isDigit(c))) {
				throw new UsernameCharacterException();
			}
		}
		// Username must not already exist
		if (usernameExists(username) != null) {
			throw new UsernameExistsException();
		}
		return true;
	}
	
	public boolean isPasswordValid(String password) {
		// https://www.geeksforgeeks.org/how-to-validate-a-password-using-regular-expressions-in-java
		// Regex to recognise a password that represents a digit, uppercase letter, special character appearing at least
		// once, and at least 8 characters and at most 20.
		return Pattern
				.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!?])(?=\\S+$).{8,25}$")
				.matcher(password)
				.matches();
	}

	public boolean isAccountNameValid(String accountName) throws AccountNameException {
		// Username must be between 3 and 15 characters in length
		if (accountName.length() < 3 || accountName.length() > 15) {
			throw new AccountNameLengthException();
		}
		for (char c : accountName.toCharArray()) {
			// Username must consist only of alphabetic characters or numeric digits
			if (!(Character.isAlphabetic(c) || Character.isDigit(c))) {
				throw new AccountNameCharacterException();
			}
		}
		// Account name must not already exist
		ArrayList<Account> accounts = loggedInCustomer.getAccounts();
		if (accountNameExists(accounts, accountName) != null) {
			throw new AccountNameExistsException();
		}
		return true;
	}

    public boolean customerAccountExists(String accountName) {
		for (Account account : loggedInCustomer.getAccounts()) {
			if (account.getName().equals(accountName)) {
				return true;
			}
		}
		return false;
	}

	public void addCustomer(String username, String password, double initialDeposit) throws NoSuchAlgorithmException {
		// Create Customer
		int customerId = customers.size() > 0 ? Collections.max(customers.keySet()) + 1 : 1;
		Customer customer = new Customer(customerId);
		customer.setUsername(username);
		customer.setPassword(hashPassword(password));
		customers.put(customerId, customer);

		// Create main account for Customer
		int accountId = accounts.size() > 0 ? Collections.max(accounts.keySet()) + 1 : 1;
		Account account = new Account(accountId);
		account.setCustomerId(customerId);
		account.setName("main");
		account.setBalance(initialDeposit);
		accounts.put(accountId, account);

		customer.addAccount(account);
		loggedInCustomer = customer;
	}

	public void addAccount(String name, double deposit) {
		int accountId = accounts.size() > 0 ? Collections.max(accounts.keySet()) + 1 : 1;
		Account account = new Account(accountId);
		account.setCustomerId(loggedInCustomer.getId());
		account.setName(name);
		account.setBalance(deposit);
		accounts.put(accountId, account);
		loggedInCustomer.addAccount(account);
	}

	public void deleteAccount(String name) {
		Account account = loggedInCustomer.getAccountByName(name);
		accounts.remove(account.getId());
		loggedInCustomer.getAccounts().removeIf(x -> x.getId() == account.getId());
	}

	public void internalTransfer(String fromAccountName, String toAccountName, double amount) throws InsufficientFundsException {
		Account fromAccount = loggedInCustomer.getAccountByName(fromAccountName);
		Account toAccount = loggedInCustomer.getAccountByName(toAccountName);
		if (fromAccount.getBalance() < amount) {
			throw new InsufficientFundsException();
		}
		fromAccount.setBalance(fromAccount.getBalance() - amount);
		toAccount.setBalance(toAccount.getBalance() + amount);
	}

	public void externalTransfer(String toCustomerName, String fromAccountName, double amount) throws InsufficientFundsException {
		Account fromAccount = loggedInCustomer.getAccountByName(fromAccountName);
		Customer toCustomer = customers.values().stream().filter(x -> x.getUsername().equals(toCustomerName)).toList().get(0);
		Account toAccount = toCustomer.getAccountByName("main");
		if (fromAccount.getBalance() < amount) {
			throw new InsufficientFundsException();
		}
		fromAccount.setBalance(fromAccount.getBalance() - amount);
		toAccount.setBalance(toAccount.getBalance() + amount);
	}

	private String hashPassword(String passwordInput) throws NoSuchAlgorithmException {
		byte[] stringBytes = passwordInput.getBytes(StandardCharsets.UTF_8);
		MessageDigest md = MessageDigest.getInstance("MD5");
		BigInteger bigInt = new BigInteger(md.digest(stringBytes));
		return bigInt.toString(16);
	}

	public List<String> getCustomerNames() {
		return customers.values().stream().map(Customer::getUsername).toList();
	}

	public void persistData() {
		databaseHandler.persistCustomers(customers);
		databaseHandler.persistAccounts(accounts);
	}

	public Customer getLoggedInCustomer() {
		return loggedInCustomer;
	}

	public void setLoggedInCustomer(Customer loggedInCustomer) {
		this.loggedInCustomer = loggedInCustomer;
	}

	public HashMap<Integer, Customer> getCustomers() {
		return customers;
	}

	public void setCustomers(HashMap<Integer, Customer> customers) {
		this.customers = customers;
	}

	public HashMap<Integer, Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(HashMap<Integer, Account> accounts) {
		this.accounts = accounts;
	}

	public DatabaseHandler getDatabaseHandler() {
		return databaseHandler;
	}

	public void setDatabaseHandler(DatabaseHandler databaseHandler) {
		this.databaseHandler = databaseHandler;
	}

	public MicroloanManagement getMicroloanManagement() {
		return microloanManagement;
	}

	public void setMicroloanManagement(MicroloanManagement microloanManagement) {
		this.microloanManagement = microloanManagement;
	}
}
