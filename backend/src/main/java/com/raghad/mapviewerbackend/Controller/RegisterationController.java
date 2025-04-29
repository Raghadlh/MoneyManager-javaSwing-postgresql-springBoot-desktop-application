package com.raghad.mapviewerbackend.Controller;
import com.raghad.mapviewerbackend.Model.MyAppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.raghad.mapviewerbackend.Model.MyAppUser;

@RestController
public class RegisterationController {
    @Autowired
    private MyAppUserRepository myAppUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping(value= "/req/signup", consumes ="application/json")
    public ResponseEntity<?> createUser(@RequestBody MyAppUser user) {
        if (myAppUserRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        myAppUserRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

