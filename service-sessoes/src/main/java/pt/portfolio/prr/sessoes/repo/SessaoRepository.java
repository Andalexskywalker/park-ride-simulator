package pt.portfolio.prr.sessoes.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.portfolio.prr.sessoes.domain.Sessao;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {
    Optional<Sessao> findFirstByMatriculaAndEstado(String matricula, Sessao.Estado estado);

    java.util.List<Sessao> findByParqueIdAndEstado(Long parqueId, Sessao.Estado estado);
}
