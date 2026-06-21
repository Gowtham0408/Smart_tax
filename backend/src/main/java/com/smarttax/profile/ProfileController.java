package com.smarttax.profile;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final UserProfileRepository repository;

    public ProfileController(UserProfileRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    UserProfile save(@RequestBody UserProfile profile) {
        return repository.save(profile);
    }

    @GetMapping("/{id}")
    UserProfile get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow();
    }
}
