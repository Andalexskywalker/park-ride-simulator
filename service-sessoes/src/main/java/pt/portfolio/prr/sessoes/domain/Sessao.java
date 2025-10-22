package pt.portfolio.prr.sessoes.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Sessao {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  private Long parqueId;
  private String matricula;
  private Instant inicio;
  private Instant fim;
  private Integer minutos;
  @Enumerated(EnumType.STRING) private Estado estado = Estado.ATIVA;
  public enum Estado { ATIVA, TERMINADA }

  // getters/setters ...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }    
    public Long getParqueId() { return parqueId; }
    public void setParqueId(Long parqueId) { this.parqueId = parqueId; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }  
    public Instant getInicio() { return inicio; }
    public void setInicio(Instant inicio) { this.inicio = inicio; }
    public Instant getFim() { return fim; }
    public void setFim(Instant fim) { this.fim = fim; }
    public Integer getMinutos() { return minutos; }
    public void setMinutos(Integer minutos) { this.minutos = minutos; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
}
