package pt.portfolio.prr.utilizadores.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pt.portfolio.prr.utilizadores.domain.Role;
import pt.portfolio.prr.utilizadores.domain.Utilizador;
import pt.portfolio.prr.utilizadores.repo.UtilizadorRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UtilizadorRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UtilizadorRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (repository.findByEmail("admin@prr.pt").isEmpty()) {
            Utilizador admin = new Utilizador();
            admin.setNome("Administrador PRR");
            admin.setEmail("admin@prr.pt");
            admin.setTelemovel("912345678");
            admin.setNif("999999999");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            repository.save(admin);
            System.out.println(">>> Admin account created: admin@prr.pt / admin123");
        }

        if (repository.findByEmail("op@prr.pt").isEmpty()) {
            Utilizador op = new Utilizador();
            op.setNome("Operador Parque 1");
            op.setEmail("op@prr.pt");
            op.setTelemovel("966666666");
            op.setNif("888888888");
            op.setPassword(passwordEncoder.encode("Op123!45"));
            op.setRole(Role.OPERADOR);
            repository.save(op);
            System.out.println(">>> Operator account created: op@prr.pt / Op123!45");
        }
    }
}
