package com.Session.EmploiTempsV1.emploi;


import com.Session.EmploiTempsV1.emploi.entities.Role;
import com.Session.EmploiTempsV1.emploi.entities.User;
import com.Session.EmploiTempsV1.emploi.repository.RoleRepository;
import com.Session.EmploiTempsV1.emploi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create roles if they don't exist
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role("ROLE_ADMIN"));
        }
        if (roleRepository.findByName("ROLE_CONSULTATION").isEmpty()) {
            roleRepository.save(new Role("ROLE_CONSULTATION"));
        }

        if (userRepository.findByUsername("newAdmin").isEmpty()) {
            User adminUser = new User("newAdmin", "newAdmin@gmail.com", passwordEncoder.encode("admin123"));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("Fpt");
            Set<Role> adminRoles = new HashSet<>();
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Error: Admin role not found for init."));
            adminRoles.add(adminRole);
            adminUser.setRoles(adminRoles);
            userRepository.save(adminUser);
            //System.out.println("Created admin user with username '****' and password '******'");
        }
    }
}