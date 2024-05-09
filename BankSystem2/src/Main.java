import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class Bank {
    protected String bankName;
    protected Map<String, Account> accounts;
    protected List<Transaction> transactions;
    protected double totalTransactionFeeAmount;
    protected double totalTransferAmount;
    protected double transactionFlatFeeAmount;
    protected double transactionPercentFeeValue;

    public Bank(String bankName, double transactionFlatFeeAmount, double transactionPercentFeeValue) {
        this.bankName = bankName;
        this.accounts = new HashMap<>();
        this.transactions = new ArrayList<>();
        this.totalTransactionFeeAmount = 0.0;
        this.totalTransferAmount = 0.0;
        this.transactionFlatFeeAmount = transactionFlatFeeAmount;
        this.transactionPercentFeeValue = transactionPercentFeeValue;
    }

    public abstract void addAccount(Account account);

    public abstract void performTransaction(Account fromAccount, Account toAccount, double amount, String reason) throws InsufficientFundsException,AccountNotFoundException;

    protected double calculateTransactionFee(double amount) {
        return (transactionPercentFeeValue / 100) * amount + transactionFlatFeeAmount;
    }

    public List<Transaction> getTransactions(String accountId) {
        List<Transaction> accountTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getOriginatingAccountId().equals(accountId) || transaction.getResultingAccountId().equals(accountId)) {
                accountTransactions.add(transaction);
            }
        }
        return accountTransactions;
    }

    public double getAccountBalance(String accountId) throws AccountNotFoundException {
        Account account = accounts.get(accountId);
        if (account == null) {
            throw new AccountNotFoundException("Account not found");
        }
        return account.getBalance();
    }

    public void displayAccounts() {
        System.out.println("Accounts in " + bankName + ":");
        for (Account account : accounts.values()) {
            System.out.println("Account ID: " + account.getAccountId() + ", User Name: " + account.getUserName() + ", Balance: $" + account.getBalance());
        }
    }

    public double getTotalTransactionFeeAmount() {
        return totalTransactionFeeAmount;
    }

    public double getTotalTransferAmount() {
        return totalTransferAmount;
    }
}

class BasicBank extends Bank {
    public BasicBank(String bankName, double transactionFlatFeeAmount, double transactionPercentFeeValue) {
        super(bankName, transactionFlatFeeAmount, transactionPercentFeeValue);
    }

    @Override
    public void addAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    @Override
    public void performTransaction(Account fromAccount, Account toAccount, double amount, String reason) throws InsufficientFundsException, AccountNotFoundException {
        double transactionFee = calculateTransactionFee(amount);
        if (fromAccount.withdraw(amount + transactionFee)) {
            toAccount.deposit(amount);
            transactions.add(new Transaction(amount, fromAccount.getAccountId(), toAccount.getAccountId(), reason));
            totalTransactionFeeAmount += transactionFee;
            totalTransferAmount += amount;
            System.out.println("Transaction Successful");
        } else {
            throw new InsufficientFundsException("Insufficient funds");
        }
    }
}

class Account {
    private String accountId;
    private String userName;
    private double balance;

    public Account(String accountId, String userName, double balance) {
        this.accountId = accountId;
        this.userName = userName;
        this.balance = balance;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getUserName() {
        return userName;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public boolean withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
}

class Transaction {
    private double amount;
    private String originatingAccountId;
    private String resultingAccountId;
    private String transactionReason;

    public Transaction(double amount, String originatingAccountId, String resultingAccountId, String transactionReason) {
        this.amount = amount;
        this.originatingAccountId = originatingAccountId;
        this.resultingAccountId = resultingAccountId;
        this.transactionReason = transactionReason;
    }

    public double getAmount() {
        return amount;
    }

    public String getOriginatingAccountId() {
        return originatingAccountId;
    }

    public String getResultingAccountId() {
        return resultingAccountId;
    }

    public String getTransactionReason() {
        return transactionReason;
    }
}

class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String message) {
        super(message);
    }
}

class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

public class Main {
    public static void main(String[] args) {
        Bank bank = new BasicBank("MyBank", 10.0, 2.5);

        Account account1 = new Account("123", "John Doe", 1000.00);
        Account account2 = new Account("456", "Jane Smith", 5000.00);

        bank.addAccount(account1);
        bank.addAccount(account2);

        try {

            bank.displayAccounts();
            System.out.println("Transactions: ");
            System.out.println("From Account :" + account1.getAccountId() + " in name of : "+ account1.getUserName() + " to " + account2.getAccountId() + " in name of : "+ account2.getUserName());
            bank.performTransaction(account1, account2, 100.0, "National Payment");
            System.out.println("From Account :" + account2.getAccountId() + " in name of : "+ account2.getUserName() + " to " + account1.getAccountId() + " in name of : "+ account1.getUserName());
            bank.performTransaction(account2, account1, 50.0, "Pay Debt");



            System.out.println("John's transactions:");
            List<Transaction> johnTransactions = bank.getTransactions(account1.getAccountId());
            for (Transaction transaction : johnTransactions) {
                System.out.println("Amount: $" + transaction.getAmount() + ", Reason: " + transaction.getTransactionReason());
            }

            System.out.println("Total transaction fee amount: $" + bank.getTotalTransactionFeeAmount());
            System.out.println("Total transfer amount: $" + bank.getTotalTransferAmount());

            //System.out.println("Jane's balance: $" + bank.getAccountBalance(account2.getAccountId()));
        } catch (AccountNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
