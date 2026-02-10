package pt.portfolio.prr.parques.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import pt.portfolio.prr.parques.domain.Parque;
import pt.portfolio.prr.parques.repo.ParqueRepository;

@ExtendWith(MockitoExtension.class)
class ParquesServiceTest {

    @Mock
    private ParqueRepository repo;

    @InjectMocks
    private ParquesService service;

    private Parque p;

    @BeforeEach
    void setUp() {
        p = new Parque();
        p.setId(1L);
        p.setNome("Parque Teste");
        p.setCapacidadeTotal(10);
        p.setOcupacaoAtual(5);
    }

    @Test
    void checkin_success() {
        // Arrange (Preparar)
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.save(any(Parque.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act (Executar)
        Parque result = service.checkin(1L);

        // Assert (Verificar)
        assertNotNull(result);
        assertEquals(6, result.getOcupacaoAtual()); // 5 + 1 = 6
        verify(repo).save(p); // Garante que guardou na BD
    }

    @Test
    void checkin_full_throwsError() {
        // Arrange
        p.setOcupacaoAtual(10); // Parque cheio
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            service.checkin(1L);
        });

        // Garante que NÃO guardou nada
        verify(repo, never()).save(any());
    }

    @Test
    void checkout_success() {
        // Arrange
        p.setOcupacaoAtual(5);
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.save(any(Parque.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Parque result = service.checkout(1L);

        // Assert
        assertEquals(4, result.getOcupacaoAtual()); // 5 - 1 = 4
    }

    @Test
    void checkout_empty_throwsError() {
        // Arrange
        p.setOcupacaoAtual(0); // Parque vazio
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.checkout(1L));
    }
}
