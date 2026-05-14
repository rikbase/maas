package com.maas.user.config;

import com.maas.dify.entity.OAuthClient;
import com.maas.dify.repository.OAuthClientRepository;
import com.maas.user.entity.User;
import com.maas.user.entity.UserRole;
import com.maas.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final UserRepository userRepository;
    private final OAuthClientRepository oAuthClientRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      OAuthClientRepository oAuthClientRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.oAuthClientRepository = oAuthClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User("admin", passwordEncoder.encode("admin123"), "Administrator", UserRole.admin);
            userRepository.save(admin);
            log.info("Default admin user created (username: admin, password: admin123)");
        }

        if (oAuthClientRepository.findByClientId("dify").isEmpty()) {
            OAuthClient client = new OAuthClient();
            client.setClientId("dify");
            client.setClientSecret(passwordEncoder.encode("maas_dify_secret"));
            client.setClientName("Dify Console");
            client.setRedirectUris("http://localhost:8080/api/oauth2/callback");
            client.setGrantTypes("authorization_code");
            client.setScopes("openid profile");
            oAuthClientRepository.save(client);
            log.info("Default OAuth client 'dify' created");
        }
    }
}
