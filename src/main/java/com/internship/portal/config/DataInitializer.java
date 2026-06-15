package com.internship.portal.config;

import com.internship.portal.entity.Quote;
import com.internship.portal.entity.Role;
import com.internship.portal.entity.User;
import com.internship.portal.repository.QuoteRepository;
import com.internship.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final QuoteRepository quoteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@internship.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("System")
                    .lastName("Admin")
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
        }

        if (quoteRepository.count() == 0) {
            List<String> quotes = List.of(
                "Coffee first, coding later. ☕",
                "Intern today, CTO tomorrow. 🚀",
                "Every bug teaches a lesson. 🐛",
                "99 bugs in the code, fix one, 127 appear. 😅",
                "Coding is like magic, except the compiler judges you. 🧙",
                "It works on my machine. 🤷",
                "Stack Overflow is just another dependency. 📚",
                "Sleep is for those without deadlines. 💤",
                "First, solve the problem. Then, write the code. ✍️",
                "Attendance marked. Productivity unlocked. 🔓"
            );
            quotes.forEach(text -> quoteRepository.save(Quote.builder().text(text).active(true).build()));
            System.out.println("✅ Quotes loaded: " + quotes.size());
        }
    }
}
