package newbank.server;

import newbank.server.microloan.MicroLoanRequest;
import newbank.server.microloan.Microloan;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseHandler  {
    private File customerFile = new File("newbank/customer.csv");
    private File accountFile = new File("newbank/account.csv");

    public HashMap<Integer, Customer> getCustomers() {
        HashMap<Integer, Customer> customers = new HashMap<>();
        ArrayList<HashMap<String, String>> records = getRecords(customerFile);
        for (HashMap<String, String> record : records) {
            customers.put(Integer.parseInt(record.get("id")), new Customer(record));
        }
        return customers;
    }
    public List<Microloan> microloans (){
        List<Microloan> microLoanList = new ArrayList<>();
        ArrayList<HashMap<String, String>> records = getRecords(new File("./newbank/microloan.csv"));
        for (HashMap<String, String> record : records) {
            if(record.isEmpty()){
                continue;
            }
            Microloan microLoan= new Microloan();
            microLoan.setLoaner(getCustomers().get(Integer.parseInt(record.get("fromCustomerId"))));
            microLoan.setTaker(getCustomers().get(Integer.parseInt(record.get("toCustomerId"))));
            microLoan.setRepaid(Boolean.parseBoolean((record.get("rePaid"))));
            microLoan.setAmount(Float.parseFloat((record.get("amount"))));
            microLoan.setInterest(Float.parseFloat((record.get("interestRate"))));
            microLoan.setDateOfExpiry(record.get("expiryDate"));
            microLoanList.add(microLoan);
        }
        return microLoanList;
    }

    public List<MicroLoanRequest> microLoanRequestList (){
        List<MicroLoanRequest> microLoanRequestList = new ArrayList<>();
        ArrayList<HashMap<String, String>> records = getRecords(new File("./newbank/microloan_request.csv"));
        for (HashMap<String, String> record : records) {
            if(record.isEmpty()){
                continue;
            }
            MicroLoanRequest microLoanRequest = new MicroLoanRequest();
            microLoanRequest.setId(Integer.parseInt(record.get("id")));
            microLoanRequest.setCustomer(getCustomers().get(Integer.parseInt(record.get("customerId"))));
            microLoanRequest.setCustomerId(Integer.parseInt(record.get("customerId")));
            microLoanRequest.setInterestRate(Float.parseFloat((record.get("interestRate"))));
            microLoanRequest.setAmount(Float.parseFloat((record.get("amount"))));
            microLoanRequestList.add(microLoanRequest);
        }
        return microLoanRequestList;
    }

    public HashMap<Integer, Account> getAccounts() {
        HashMap<Integer, Account> accounts = new HashMap<>();
        ArrayList<HashMap<String, String>> records = getRecords(accountFile);
        for (HashMap<String, String> record : records) {
            accounts.put(Integer.parseInt(record.get("id")), new Account(record));
        }
        return accounts;
    }

    private ArrayList<HashMap<String, String>> getRecords(File file) {
        ArrayList<HashMap<String, String>> records = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                HashMap<String, String> record = new HashMap<>();
                for (String field : fields) {
                    String[] kvPair = field.split(":");
                    record.put(kvPair[0], kvPair[1]);
                }
                records.add(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    public void persistCustomers(HashMap<Integer, Customer> customers) {
        try {
            FileWriter fw = new FileWriter(customerFile, false);
            BufferedWriter bw = new BufferedWriter(fw);
            for (Customer customer : customers.values()) {
                StringBuilder sb = new StringBuilder();
                sb.append("id:" + customer.getId() + ",");
                sb.append("username:" + customer.getUsername() + ",");
                sb.append("password:" + customer.getPassword());
                sb.append("\n");
                bw.write(sb.toString());
            }
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void persistAccounts(HashMap<Integer, Account> accounts) {
        try {
            FileWriter fw = new FileWriter(accountFile, false);
            BufferedWriter bw = new BufferedWriter(fw);
            for (Account account : accounts.values()) {
                StringBuilder sb = new StringBuilder();
                sb.append("id:" + account.getId() + ",");
                sb.append("customerId:" + account.getCustomerId() + ",");
                sb.append("name:" + account.getName() + ",");
                sb.append("balance:" + account.getBalance());
                sb.append("\n");
                bw.write(sb.toString());
            }
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
