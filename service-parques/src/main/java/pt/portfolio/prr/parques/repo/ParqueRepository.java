package pt.portfolio.prr.parques.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pt.portfolio.prr.parques.domain.Parque;

public interface ParqueRepository extends JpaRepository<Parque, Long> {

    List<Parque> findByCidadeIgnoreCase(String cidade);              // (opcional)
    Page<Parque> findByCidadeIgnoreCase(String cidade, Pageable pg);

  @Modifying @Transactional
  @Query("""
      update Parque p
         set p.ocupacaoAtual = p.ocupacaoAtual + :delta
       where p.id = :id
         and p.ocupacaoAtual + :delta between 0 and p.capacidadeTotal
      """)
  int applyDelta(@Param("id") Long id, @Param("delta") int delta);
}
