package newbank.server;

import java.util.HashMap;

public class Account {
	private int id;
	private int customerId;
	private String name;
	private double balance;

	public Account(int accountId) {
		id = accountId;
	}

	public Account(HashMap<String, String> record) {
		id = Integer.parseInt(record.get("id"));
		customerId = Integer.parseInt(record.get("customerId"));
		name = record.get("name");
		balance = Double.parseDouble(record.get("balance"));
	}

	public int getId() {
		return id;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "id: " + id + " - " +
			   "name: " + name + " - " +
			   "balance: " + String.format("£%.2f", balance);
	}
}
