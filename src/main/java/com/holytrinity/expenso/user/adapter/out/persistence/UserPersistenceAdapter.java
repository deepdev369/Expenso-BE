package com.holytrinity.expenso.user.adapter.out.persistence;

import com.holytrinity.expenso.user.application.port.out.UserPort;
import com.holytrinity.expenso.user.domain.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPort {

    private final SpringDataUserRepository userRepository;

    @Override
    public Optional<User> loadUser(String userId) {
        return userRepository.findById(userId);
    }

    @Override
    public java.util.List<User> loadAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    public Optional<User> loadUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
