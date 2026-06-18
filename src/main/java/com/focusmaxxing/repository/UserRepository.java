package com.focusmaxxing.repository;

import com.focusmaxxing.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(int id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    void delete(int id);
}
