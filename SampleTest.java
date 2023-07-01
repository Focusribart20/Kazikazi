package assignment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleTests {
    Bank bank;
    CheckingAccount account1;
    SavingsAccount account2;

    @BeforeEach void setUp() {
        bank = new Bank("My Bank");
        account1 = new CheckingAccount("123", "John Doe", 100.0, Account.Status.ACTIVE);
        account2 = new SavingsAccount("456", "Jane Doe", 200.0, 0.05, Account.Status.ACTIVE);
        bank.addAccount(account1);
        bank.addAccount(account2);
    }

    @Test
    void testDeposit() throws AccountInactiveException, AccountClosedException {
        account1.deposit(50.0);
        assertEquals(150.0, account1.getBalance());
    }

    @Test void testAddAccount() {
        CheckingAccount account3 = new CheckingAccount("789", "Aron", 300.0, Account.Status.ACTIVE);
        bank.addAccount(account3);
        assertEquals(3, bank.getAccounts().size());
    }

    @Test public void testWriteCheck() {
        account1.writeCheck(300, 50.0);
        assertEquals(50.0, account1.getBalance());
    }

    @Test void applyInterest() {
        account2.applyInterest();
        assertEquals(105.0, account2.getBalance());
    }
}
