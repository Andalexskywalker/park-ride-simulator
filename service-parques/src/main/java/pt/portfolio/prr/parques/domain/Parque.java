package pt.portfolio.prr.parques.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Entity
public class Parque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @NotBlank
    private String cidade;

    @NotNull
    @Positive
    private Integer capacidadeTotal;

    @jakarta.persistence.Column(nullable = false)
    private Integer ocupacaoAtual = 0;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @jakarta.persistence.Column(nullable = false)
    private EstadoParque estado = EstadoParque.ABERTO;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
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

    // ...

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (ocupacaoAtual == null) ocupacaoAtual = 0;
        if (estado == null) estado = EstadoParque.ABERTO;
    }

}