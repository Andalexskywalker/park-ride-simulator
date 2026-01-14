package pt.portfolio.prr.tarifas.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.portfolio.prr.tarifas.domain.Fatura;

import java.util.List;

public interface FaturaRepository extends JpaRepository<Fatura, Long> {
    List<Fatura> findByMatricula(String matricula);
}
