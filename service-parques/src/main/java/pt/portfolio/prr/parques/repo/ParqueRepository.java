package pt.portfolio.prr.parques.repo;

import pt.portfolio.prr.parques.domain.Parque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParqueRepository extends JpaRepository<Parque, Long> {
    List<Parque> findByCidadeIgnoreCase(String cidade);
}