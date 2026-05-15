package com.micultura.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "eventos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fecha;

    private LocalTime hora;

    @Column(nullable = false, length = 255)
    private String ubicacion;

    private Double latitud;
    private Double longitud;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(length = 500)
    private String imagenUrl;

    @Column(nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal precio = BigDecimal.ZERO;

    @Column(length = 500)
    private String enlaceCompra;

    /** Soft-delete flag — false = logically deleted */
    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
