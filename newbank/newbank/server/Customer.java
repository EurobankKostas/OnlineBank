package newbank.server;

import java.util.ArrayList;
import java.util.HashMap;

public class Customer {
	private int id;
	private String username;
	private String password;
	private ArrayList<Account> accounts;

	public Customer(int customerId) {
		id = customerId;
		accounts = new ArrayList<>();
	}
	
	public Customer(HashMap<String, String> record) {
		id = Integer.parseInt(record.get("id"));
		username = record.get("username");
		password = record.get("password");
		accounts = new ArrayList<>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String accountsToString() {
		StringBuilder s = new StringBuilder();
		for (Account a : accounts) {
			s.append(a.toString()).append("\r");
		}
		return s.toString();
	}

	public void addAccount(Account account) {
		accounts.add(account);		
	}

	public ArrayList<Account> getAccounts() {
		return accounts;
	}

	public Account getAccountByName(String name) {
		for (Account account : accounts) {
			if (account.getName().equals(name)) {
				return account;
			}
		}
		return null;
	}
}
