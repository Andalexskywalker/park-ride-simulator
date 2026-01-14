package pt.portfolio.prr.parques.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pt.portfolio.prr.parques.domain.EstadoParque;
import pt.portfolio.prr.parques.domain.Parque;
import pt.portfolio.prr.parques.repo.ParqueRepository;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ParqueRepository repository;

    public DataInitializer(ParqueRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            saveParque("Parque Oriente", "Lisboa", 150, new BigDecimal("1.20"));
            saveParque("Parque Casa da Música", "Porto", 200, new BigDecimal("1.50"));
            saveParque("Parque Coina", "Barreiro", 100, new BigDecimal("0.80"));
            saveParque("Parque Sete Rios", "Lisboa", 80, new BigDecimal("2.00"));

            System.out.println(">>> Sample parks created successfully.");
        }
    }

    private void saveParque(String nome, String cidade, Integer capacidade, BigDecimal preco) {
        Parque p = new Parque();
        p.setNome(nome);
        p.setCidade(cidade);
        p.setCapacidadeTotal(capacidade);
        p.setOcupacaoAtual(0);
        p.setEstado(EstadoParque.ABERTO);
        p.setPrecoHora(preco);
        repository.save(p);
    }
}
