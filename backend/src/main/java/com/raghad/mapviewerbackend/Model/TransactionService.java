package com.raghad.mapviewerbackend.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepo;

    @Autowired
    private SavingGoalRepository savingGoalRepository;

    @Autowired
    private MyAppUserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    public Transaction save(Transaction transaction) {
        Transaction saved = transactionRepo.save(transaction);
        return saved;
    }

    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepo.findByUserId(userId);
    }

    public void deleteById(Long id) {
        transactionRepo.deleteById(id);
    }


}
