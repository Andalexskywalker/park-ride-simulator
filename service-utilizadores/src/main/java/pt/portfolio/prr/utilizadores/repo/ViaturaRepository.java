package pt.portfolio.prr.utilizadores.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.portfolio.prr.utilizadores.domain.Viatura;
import java.util.Optional;

public interface ViaturaRepository extends JpaRepository<Viatura, Long> {
    Optional<Viatura> findByMatricula(String matricula);
}
