package id.ac.ui.cs.advprog.jsonbackend.wallet.service;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.*;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.*;
import id.ac.ui.cs.advprog.jsonbackend.wallet.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.*;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletServiceImpl(WalletRepository walletRepository,
                             TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    private Wallet getWallet(java.util.UUID userId){
        return walletRepository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);
    }

    @Override
    @Transactional
    public WalletResponse topUp(TopUpRequest request){

        Wallet wallet = getWallet(request.getUserId());

        wallet.credit(request.getAmount());
        walletRepository.save(wallet);

        Transaction trx = new Transaction();
        trx.setUserId(request.getUserId());
        trx.setAmount(request.getAmount());
        trx.setType(TransactionType.TOP_UP);

        transactionRepository.save(trx);

        return new WalletResponse(wallet.getUserId(), wallet.getBalance());
    }

    @Override
    @Transactional
    public WalletResponse withdraw(WithdrawRequest request){

        Wallet wallet = getWallet(request.getUserId());

        wallet.debit(request.getAmount());
        walletRepository.save(wallet);

        Transaction trx = new Transaction();
        trx.setUserId(request.getUserId());
        trx.setAmount(request.getAmount());
        trx.setType(TransactionType.WITHDRAWAL);

        transactionRepository.save(trx);

        return new WalletResponse(wallet.getUserId(), wallet.getBalance());
    }

    @Override
    @Transactional
    public WalletResponse payment(PaymentRequest request) {
        Wallet wallet = getWallet(request.getUserId());

        wallet.debit(request.getAmount());
        walletRepository.save(wallet);

        Transaction trx = new Transaction();
        trx.setUserId(request.getUserId());
        trx.setAmount(request.getAmount());
        trx.setType(TransactionType.PAYMENT);
        transactionRepository.save(trx);

        return new WalletResponse(wallet.getUserId(), wallet.getBalance());
    }

    @Override
    @Transactional
    public WalletResponse refund(RefundRequest request) {
        Wallet wallet = getWallet(request.getUserId());

        wallet.credit(request.getAmount());
        walletRepository.save(wallet);

        Transaction trx = new Transaction();
        trx.setUserId(request.getUserId());
        trx.setAmount(request.getAmount());
        trx.setType(TransactionType.REFUND);
        transactionRepository.save(trx);

        return new WalletResponse(wallet.getUserId(), wallet.getBalance());
    }
}
