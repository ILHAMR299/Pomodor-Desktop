package com.focusmaxxing.service;

import com.focusmaxxing.model.Role;
import com.focusmaxxing.model.User;
import com.focusmaxxing.repository.UserRepository;
import com.focusmaxxing.repository.UserRepositoryImpl;
import com.focusmaxxing.util.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepositoryImpl();
    }

    public User register(String username, String plainPassword, Role role) {
        if (username == null || username.trim().isEmpty() || plainPassword == null || plainPassword.length() < 4) {
            throw new IllegalArgumentException("Nama pengguna tidak boleh kosong dan kata sandi minimal 4 karakter");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Nama pengguna sudah digunakan");
        }
        
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(hashedPassword);
        newUser.setRole(role != null ? role : Role.USER);
        
        return userRepository.save(newUser);
    }

    public boolean login(String username, String plainPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
                SessionManager.getInstance().loginUser(user);
                return true;
            }
        }
        return false;
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }

}
