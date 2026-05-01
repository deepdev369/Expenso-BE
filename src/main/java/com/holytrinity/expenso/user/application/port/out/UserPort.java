package com.holytrinity.expenso.user.application.port.out;

import com.holytrinity.expenso.user.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserPort {
    Optional<User> loadUser(String userId);

    List<User> loadAllUsers();

    boolean existsByEmail(String email);

    User saveUser(User user);

    void deleteUser(User user);

    Optional<User> loadUserByEmail(String email);
}
