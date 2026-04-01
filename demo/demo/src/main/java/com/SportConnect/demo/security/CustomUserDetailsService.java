package com.SportConnect.demo.security;

import com.SportConnect.demo.model.User;
import com.SportConnect.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

private final UserRepository userRepository;

public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
}

public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email).
            orElseThrow(() -> new UsernameNotFoundException("Nu am gasit niciun cont cu email: " + email));

    return new CustomUserDetails(user);
}

}
