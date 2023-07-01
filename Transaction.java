package assignment;

import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Transaction {
    public enum Type {DEPOSIT, WITHDRAW, TRANSFER} // Enum to represent transaction types
    private Type type;
    private double amount;
    private String accountNumber;
    private String time;

    public Transaction(Type type, double amount, String accountNumber, String time) {
        this.type = type;
        this.amount = amount;
        this.accountNumber = accountNumber;
        this.time = time;
    }

    // Getters and setters for transaction properties
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

abstract class Account {
    private String accountNumber;
    private double balance;
    private String accountHolder;
    private AccountStatus accountStatus;
    private List<Transaction> transactions;

    public Account(String accountNumber, String accountHolder) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = 0.0;
        this.accountStatus = AccountStatus.ACTIVE;
        this.transactions = new ArrayList<>();
    }


    public void deposit(double amount) {
        if (accountStatus != AccountStatus.ACTIVE) {
            throw new AccountInactiveException();
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        balance += amount;
        recordTransaction(Transaction.Type.DEPOSIT, amount);
    }

    public void withdraw(double amount) {
        if (accountStatus != AccountStatus.ACTIVE) {
            throw new AccountInactiveException();
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        if (amount > balance) {
            throw new InsufficientFundsException();
        }

        balance -= amount;
        recordTransaction(Transaction.Type.WITHDRAW, amount);
    }

    public void transfer(double amount, Account recipient) {
        if (accountStatus != AccountStatus.ACTIVE) {
            throw new AccountInactiveException();
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (amount > balance) {
            throw new InsufficientFundsException();
        }

        balance -= amount;
        recipient.deposit(amount);

        recordTransaction(Transaction.Type.TRANSFER, amount);
        recipient.recordTransaction(Transaction.Type.DEPOSIT, amount);
    }

    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    private void recordTransaction(Transaction.Type type, double amount) {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = currentTime.format(formatter);

        Transaction transaction = new Transaction(type, amount, accountNumber, formattedTime);
        transactions.add(transaction);
    }
    public enum AccountStatus {
        ACTIVE,
        INACTIVE,
        CLOSED
    }
}

class CheckingAccount extends Account {
    private Map<Integer, Double> uncashedChecks;

    public CheckingAccount(String accountNumber, String accountHolder) {
        super(accountNumber, accountHolder);
        this.uncashedChecks = new HashMap<>();
    }

    public void writeCheck(int checkNumber, double amount) {
        if (getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException();
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Check amount must be positive");
        }

        uncashedChecks.put(checkNumber, amount);
        withdraw(amount);
    }

    public void cashCheck(int checkNumber, Account recipient) {
        if (getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException();
        }

        if (!uncashedChecks.containsKey(checkNumber)) {
            throw new CheckNotFoundException();
        }

        double checkAmount = uncashedChecks.get(checkNumber);
        if (checkAmount > getBalance()) {
            throw new InsufficientFundsException();
        }

        uncashedChecks.remove(checkNumber);
        recipient.deposit(checkAmount);
    }
}

class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(String accountNumber, String accountHolder, double interestRate) {
        super(accountNumber, accountHolder);
        this.interestRate = interestRate;
    }

    public void applyInterest() {
        if (getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException();
        }

        double interestAmount = getBalance() * interestRate;
        deposit(interestAmount);
    }
}


class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Insufficient funds in the account");
    }
}

class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException() {
        super("Account not found");
    }
}

class AccountInactiveException extends RuntimeException {
    public AccountInactiveException() {
        super("Account is inactive");
    }
}

class AccountClosedException extends RuntimeException {
    public AccountClosedException() {
        super("Account is closed");
    }
}

class CheckNotFoundException extends RuntimeException {
    public CheckNotFoundException() {
        super("Check not found");
    }
}

class Bank {
    private String name;
    private Map<String, Account> accounts;

    public Bank(String name) {
        this.name = name;
        this.accounts = new HashMap<>();
    }

    public void addAccount(Account account) {
        accounts.put(account.getAccountNumber(), account);
    }

    public void removeAccount(Account account) {
        accounts.remove(account.getAccountNumber());
    }

    public double getTotalAssets() {
        double totalAssets = 0.0;
        for (Account account : accounts.values()) {
            totalAssets += account.getBalance();
        }
        return totalAssets;
    }

    public List<Transaction> getTransactionHistory() {
        List<Transaction> transactionHistory = new ArrayList<>();
        for (Account account : accounts.values()) {
            transactionHistory.addAll(account.getTransactions());
        }
        return transactionHistory;
    }
    public List<Account> getAccounts() {
        return new ArrayList<>(accounts.values());
    }
    public String getName() {
        return name;
    }
}

 class Main {
    public static void main(String[] args) {
        Bank bank = new Bank("MyBank");

        CheckingAccount checkingAccount = new CheckingAccount("C1001", "John Doe");
        checkingAccount.deposit(1000);
        checkingAccount.writeCheck(1001, 200);

        SavingsAccount savingsAccount = new SavingsAccount("S2001", "Jane Smith", 0.05);
        savingsAccount.deposit(5000);
        savingsAccount.applyInterest();

        bank.addAccount(checkingAccount);
        bank.addAccount(savingsAccount);

        System.out.println("Bank name: " + bank.getName());
        System.out.println("Total assets in " + bank.getName() + ": $" + bank.getTotalAssets());
        System.out.println("Total assets in the bank: $" + bank.getTotalAssets());
        System.out.println("Transaction history:");
        List<Transaction> transactionHistory = bank.getTransactionHistory();
        for (Transaction transaction : transactionHistory) {
            System.out.println(transaction.getType() + " - Amount: $" + transaction.getAmount() +
                    ", Account: " + transaction.getAccountNumber() +
                    ", Time: " + transaction.getTime()
            );
        }
    }
}
