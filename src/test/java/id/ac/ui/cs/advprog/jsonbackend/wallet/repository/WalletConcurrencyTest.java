package id.ac.ui.cs.advprog.jsonbackend.wallet.repository;

import id.ac.ui.cs.advprog.jsonbackend.wallet.model.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class WalletConcurrencyTest {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void testOptimisticLockingPreventsLostUpdate() {
        Wallet wallet = new Wallet();
        wallet.setUserId(UUID.randomUUID());
        wallet.setBalance(new BigDecimal("100000"));


        wallet = walletRepository.saveAndFlush(wallet);

        Wallet walletRequest1 = walletRepository.findById(wallet.getId()).get();

        Wallet walletRequest2 = walletRepository.findById(wallet.getId()).get();

        walletRequest1.debit(new BigDecimal("50000"));
        walletRepository.saveAndFlush(walletRequest1);

        walletRequest2.debit(new BigDecimal("30000"));

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            walletRepository.saveAndFlush(walletRequest2);
        });
    }
}