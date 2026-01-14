package pt.portfolio.prr.utilizadores.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.portfolio.prr.utilizadores.domain.Utilizador;
import java.util.Optional;

public interface UtilizadorRepository extends JpaRepository<Utilizador, Long> {
    Optional<Utilizador> findByEmail(String email);

    Optional<Utilizador> findByNif(String nif);
}
