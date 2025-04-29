package com.raghad.mapviewerbackend.Controller;

import com.raghad.mapviewerbackend.Model.MyAppUser;
import com.raghad.mapviewerbackend.Model.MyAppUserService;
import com.raghad.mapviewerbackend.Model.SavingGoal;
import com.raghad.mapviewerbackend.Model.SavingGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/saving-goals")
public class SavingGoalController {

    @Autowired
    private SavingGoalService goalService;

    @Autowired
    private MyAppUserService userService;

    @PostMapping
    public SavingGoal addGoal(@RequestBody SavingGoal goal, Principal principal) {
        MyAppUser user = userService.getUserByUsername(principal.getName());
        goal.setUser(user);
        return goalService.save(goal);
    }

    @GetMapping
    public List<SavingGoal> getGoals(Principal principal) {
        MyAppUser user = userService.getUserByUsername(principal.getName());
        return goalService.getGoalsForUser(user.getId());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable Long id, @RequestBody SavingGoal updated, Principal principal) {
        return goalService.updateGoal(id, updated, principal.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id, Principal principal) {
        return goalService.deleteGoal(id, principal.getName());
    }
}
