package pt.portfolio.prr.sessoes.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import pt.portfolio.prr.sessoes.client.BillingClient;
import pt.portfolio.prr.sessoes.client.ParquesClient;
import pt.portfolio.prr.sessoes.client.UtilizadoresClient;
import pt.portfolio.prr.sessoes.domain.Sessao;
import pt.portfolio.prr.sessoes.repo.SessaoRepository;
import pt.portfolio.prr.sessoes.web.SessoesController.StartReq;

@ExtendWith(MockitoExtension.class)
class SessoesControllerTest {

    @Mock
    private SessaoRepository repo;
    @Mock
    private ParquesClient parquesClient;
    @Mock
    private UtilizadoresClient utilizadoresClient;
    @Mock
    private BillingClient billingClient;

    @InjectMocks
    private SessoesController controller;

    private final String MATRICULA = "AA-00-XX";
    private final Long PARQUE_ID = 1L;

    @BeforeEach
    void setUp() {
    }

    @Test
    void start_success_createsSession() {
        // 1. Mock: Viatura existe
        UtilizadoresClient.ViaturaDTO viatura = new UtilizadoresClient.ViaturaDTO(10L, MATRICULA, "Audi", "A4");
        when(utilizadoresClient.getViatura(MATRICULA)).thenReturn(viatura);

        // 2. Mock: Não tem sessão ativa
        when(repo.findFirstByMatriculaAndEstado(MATRICULA, Sessao.Estado.ATIVA)).thenReturn(Optional.empty());

        // 3. Mock: Parque existe e está ABERTO
        ParquesClient.ParqueDTO parqueInfo = new ParquesClient.ParqueDTO(
                PARQUE_ID, "Parque Central", "Lisboa", 100, 50, "ABERTO", BigDecimal.ONE);
        when(parquesClient.get(PARQUE_ID)).thenReturn(parqueInfo);

        // 4. Mock: Checkin corre bem
        when(parquesClient.checkin(PARQUE_ID)).thenReturn(new ParquesClient.OcupacaoDTO(PARQUE_ID, 51, 100));

        // 5. Mock: Save Sessão
        when(repo.save(any(Sessao.class))).thenAnswer(inv -> {
            Sessao s = inv.getArgument(0);
            s.setId(999L); // Simula ID gerado pela BD
            return s;
        });

        // ACTION
        Map<String, Object> response = controller.start(new StartReq(MATRICULA, PARQUE_ID));

        // ASSERT
        assertNotNull(response);
        assertEquals(999L, response.get("sessaoId"));
        assertEquals(MATRICULA, response.get("matricula"));

        // Verifica se chamou todos os serviços externos
        verify(utilizadoresClient).getViatura(MATRICULA);
        verify(parquesClient).checkin(PARQUE_ID);
        verify(repo).save(any(Sessao.class));
    }

    @Test
    void start_viaturaNaoExiste_throws404() {
        // Simular erro do client Feign (quando a viatura n existe)
        when(utilizadoresClient.getViatura(MATRICULA)).thenThrow(new RuntimeException("Not Found"));

        // Assert que o controller lança exceção
        assertThrows(ResponseStatusException.class, () -> {
            controller.start(new StartReq(MATRICULA, PARQUE_ID));
        });

        // Garante que NUNCA tentou fazer checkin no parque nem guardar sessão
        verify(parquesClient, never()).checkin(any());
        verify(repo, never()).save(any());
    }

    @Test
    void start_parqueCheio_throwsConflict() {
        // 1. Viatura OK
        when(utilizadoresClient.getViatura(MATRICULA))
                .thenReturn(new UtilizadoresClient.ViaturaDTO(1L, MATRICULA, "Ford", "Fiesta"));

        // 2. Sessão OK (não existe)
        when(repo.findFirstByMatriculaAndEstado(MATRICULA, Sessao.Estado.ATIVA)).thenReturn(Optional.empty());

        // 3. Parque existe e ABERTO
        when(parquesClient.get(PARQUE_ID))
                .thenReturn(new ParquesClient.ParqueDTO(PARQUE_ID, "P1", "Lx", 10, 10, "ABERTO", BigDecimal.ONE));

        // 4. Checkin CHECKIN FALHA (Ex: Parque cheio lança erro no outro microsserviço)
        doThrow(new RuntimeException("Parque cheio")).when(parquesClient).checkin(PARQUE_ID);

        // Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            controller.start(new StartReq(MATRICULA, PARQUE_ID));
        });

        assertEquals(org.springframework.http.HttpStatus.CONFLICT, ex.getStatusCode());

        // Garante que não guardou a sessão
        verify(repo, never()).save(any());
    }
}
