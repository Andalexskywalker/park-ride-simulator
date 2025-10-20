package pt.portfolio.prr.parques.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Parque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cidade;
    private Integer capacidadeTotal;
    private Integer ocupacaoAtual;

    @Enumerated(EnumType.STRING)
    private EstadoParque estado = EstadoParque.ABERTO;

    private BigDecimal precoHora;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public Integer getCapacidadeTotal() { return capacidadeTotal; }
    public void setCapacidadeTotal(Integer capacidadeTotal) { this.capacidadeTotal = capacidadeTotal; }

    public Integer getOcupacaoAtual() { return ocupacaoAtual; }
    public void setOcupacaoAtual(Integer ocupacaoAtual) { this.ocupacaoAtual = ocupacaoAtual; }

    public EstadoParque getEstado() { return estado; }
    public void setEstado(EstadoParque estado) { this.estado = estado; }

    public BigDecimal getPrecoHora() { return precoHora; }
    public void setPrecoHora(BigDecimal precoHora) { this.precoHora = precoHora; }
}