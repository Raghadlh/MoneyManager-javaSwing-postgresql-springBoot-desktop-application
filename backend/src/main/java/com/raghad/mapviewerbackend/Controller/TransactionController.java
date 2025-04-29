package com.raghad.mapviewerbackend.Controller;
import com.raghad.mapviewerbackend.Model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private MyAppUserService userService;

    @Autowired
    private SavingGoalRepository savingGoalRepository;

    @Autowired
    private SavingGoalService savingGoalService;


    @Autowired
    private  TransactionRepository transactionRepository;

    @PostMapping
    public Transaction addTransaction(@RequestBody Transaction transaction, Principal principal) {
        MyAppUser user = userService.getUserByUsername(principal.getName());
        transaction.setUser(user);

        Transaction savedTransaction = transactionService.save(transaction);

        if (transaction.isSaving()) {
            savingGoalService.distributeSavingAmount(user, transaction.getAmount());
        }

        return savedTransaction;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id, Principal principal) {
        MyAppUser user = userService.getUserByUsername(principal.getName());
        Transaction transaction = transactionRepository.findById(id).orElse(null);

        if (transaction != null) {
            if (transaction.isSaving()) {
                savingGoalService.reverseDistributeSavingAmount(user, transaction.getAmount());
            }
            transactionService.deleteById(id);
        }

        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}")
    public Transaction updateTransaction(
            @PathVariable Long id,
            @RequestBody Transaction updatedData,
            Principal principal) {

        MyAppUser user = userService.getUserByUsername(principal.getName());
        Transaction existing = transactionRepository.findById(id).orElseThrow();

        boolean wasSaving = existing.isSaving();
        boolean isSavingNow = updatedData.isSaving();
        double oldAmount = existing.getAmount();
        double newAmount = updatedData.getAmount();

        existing.setType(updatedData.getType());
        existing.setCategory(updatedData.getCategory());
        existing.setAmount(newAmount);
        existing.setDate(updatedData.getDate());
        existing.setNote(updatedData.getNote());
        existing.setSaving(isSavingNow);

        Transaction saved = transactionRepository.save(existing);

        if (wasSaving) {
            savingGoalService.reverseDistributeSavingAmount(user, oldAmount);
        }
        if (isSavingNow) {
            savingGoalService.distributeSavingAmount(user, newAmount);
        }

        return saved;
    }



    @GetMapping
    public List<Transaction> getUserTransactions(Principal principal) {
        MyAppUser user = userService.getUserByUsername(principal.getName());
        return transactionService.getUserTransactions(user.getId());
    }


}
