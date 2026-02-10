package pt.portfolio.prr.parques.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pt.portfolio.prr.parques.domain.Parque;
import pt.portfolio.prr.parques.repo.ParqueRepository;

@Service
public class ParquesService {

    private final ParqueRepository repo;

    public ParquesService(ParqueRepository repo) {
        this.repo = repo;
    }

    @Transactional
    @SuppressWarnings("null")
    public Parque checkin(Long id) {
        Parque p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parque não encontrado"));
        if (p.getOcupacaoAtual() == null)
            p.setOcupacaoAtual(0);
        if (p.getOcupacaoAtual() >= p.getCapacidadeTotal()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Parque cheio");
        }
        p.setOcupacaoAtual(p.getOcupacaoAtual() + 1);
        return repo.save(p);
    }

    @Transactional
    @SuppressWarnings("null")
    public Parque checkout(Long id) {
        Parque p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parque não encontrado"));
        if (p.getOcupacaoAtual() == null)
            p.setOcupacaoAtual(0);
        if (p.getOcupacaoAtual() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Parque vazio");
        }
        p.setOcupacaoAtual(p.getOcupacaoAtual() - 1);
        return repo.save(p);
    }
}
