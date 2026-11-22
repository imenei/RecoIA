package dz.recoia.authservice.service;

import dz.recoia.authservice.model.Role;
import dz.recoia.authservice.model.User;
import dz.recoia.authservice.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Au premier demarrage (table vide) : cree 1 administrateur et 30 etudiants de test. */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final boolean seedOnEmpty;
    private final String adminPassword;
    private final String studentPassword;

    public DataSeeder(UserRepository users, PasswordEncoder encoder,
                      @Value("${app.seed-on-empty:true}") boolean seedOnEmpty,
                      @Value("${app.seed.admin-password:Admin123!}") String adminPassword,
                      @Value("${app.seed.student-password:Student123!}") String studentPassword) {
        this.users = users;
        this.encoder = encoder;
        this.seedOnEmpty = seedOnEmpty;
        this.adminPassword = adminPassword;
        this.studentPassword = studentPassword;
    }

    @Override
    public void run(String... args) {
        if (!seedOnEmpty || users.count() > 0) return;

        User admin = new User();
        admin.setEmail("admin@recoia.dz");
        admin.setFullName("Administrateur RecoIA");
        admin.setPasswordHash(encoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        users.save(admin);

        String hash = encoder.encode(studentPassword);   // meme mot de passe pour tous les etudiants de test
        for (SampleUsers.Spec spec : SampleUsers.students(30, 42)) {
            User u = new User();
            u.setEmail(spec.email());
            u.setFullName(spec.fullName());
            u.setPasswordHash(hash);
            u.setRole(Role.STUDENT);
            u.setInterests(spec.interests());
            users.save(u);
        }
        log.info("[auth] 1 administrateur et 30 étudiants de test créés");
    }
}
