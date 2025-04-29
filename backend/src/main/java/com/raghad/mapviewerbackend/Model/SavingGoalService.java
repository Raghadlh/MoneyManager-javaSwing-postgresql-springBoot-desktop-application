package com.raghad.mapviewerbackend.Model;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SavingGoalService {

    @Autowired
    private SavingGoalRepository savingGoalRepository;

    @Autowired
    private MyAppUserRepository userRepository;

    public SavingGoal save(SavingGoal goal) {
        return savingGoalRepository.save(goal);
    }

    public List<SavingGoal> getGoalsForUser(Long userId) {
        return savingGoalRepository.findByUserId(userId);
    }

    public ResponseEntity<?> updateGoal(Long id, SavingGoal updated, String username) {
        Optional<SavingGoal> optionalGoal = savingGoalRepository.findById(id);
        if (optionalGoal.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SavingGoal goal = optionalGoal.get();
        if (!goal.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        goal.setName(updated.getName());
        goal.setTargetAmount(updated.getTargetAmount());
        goal.setDeadline(updated.getDeadline());
        savingGoalRepository.save(goal);

        return ResponseEntity.ok(goal);
    }

    public ResponseEntity<?> deleteGoal(Long id, String username) {
        Optional<SavingGoal> optionalGoal = savingGoalRepository.findById(id);
        if (optionalGoal.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SavingGoal goal = optionalGoal.get();
        if (!goal.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        savingGoalRepository.delete(goal);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    public void distributeSavingAmount(MyAppUser user, double amountToSave) {
        List<SavingGoal> goals = savingGoalRepository.findByUserId(user.getId());
        goals.sort((g1, g2) -> g1.getId().compareTo(g2.getId()));

        double remainingAmount = amountToSave;

        for (SavingGoal goal : goals) {
            double needed = goal.getTargetAmount() - goal.getSavedAmount();

            if (needed <= 0) {
                continue;
            }

            if (remainingAmount <= 0) {
                break; // no more money
            }

            double toAdd = Math.min(needed, remainingAmount);

            goal.setSavedAmount(goal.getSavedAmount() + toAdd);
            remainingAmount -= toAdd;
        }

        savingGoalRepository.saveAll(goals);
    }

    @Transactional
    public void reverseDistributeSavingAmount(MyAppUser user, double amountToRemove) {
        List<SavingGoal> goals = savingGoalRepository.findByUserId(user.getId());
        goals.sort((g1, g2) -> g1.getId().compareTo(g2.getId())); // same order

        double remainingAmount = amountToRemove;

        for (SavingGoal goal : goals) {
            double saved = goal.getSavedAmount();
            if (saved > 0) {
                if (saved >= remainingAmount) {
                    goal.setSavedAmount(saved - remainingAmount);
                    remainingAmount = 0;
                    break;
                } else {
                    goal.setSavedAmount(0);
                    remainingAmount -= saved;
                }
            }
        }

        savingGoalRepository.saveAll(goals);
    }

}
