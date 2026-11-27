package com.armamentobelico.portalarmamentobelico.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("evento")
public class Evento implements Persistable<UUID> {

    private static final DateTimeFormatter DATA_BR_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Id
    @Column("id")
    private UUID id;

    @Column("titulo")
    private String titulo;

    @Column("flyer_path")
    private String flyerPath;

    @Column("cidade")
    private String cidade;

    @Column("estado")
    private EstadoBrasil estado;

    @Column("data_evento")
    private LocalDate dataEvento;

    @Column("horario")
    private String horario;

    @Column("contato_organizador")
    private String contatoOrganizador;

    @Column("whatsapp_publicador")
    private String whatsappPublicador;

    @Column("papel_publicador")
    private PapelPublicador papelPublicador;

    @Column("aceitou_regras")
    private Boolean aceitouRegras;

    @Column("status_moderacao")
    private StatusModeracaoEvento statusModeracao;

    @Column("motivo_recusa")
    private String motivoRecusa;

    @Column("moderado_em")
    private Instant moderadoEm;

    @Column("moderado_por")
    private String moderadoPor;

    @Column("criado_em")
    private Instant criadoEm;

    @Column("atualizado_em")
    private Instant atualizadoEm;

    @Transient
    private boolean isNew;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew || id == null;
    }

    public boolean isPassado(LocalDate hoje) {
        return dataEvento != null && hoje != null && dataEvento.isBefore(hoje);
    }

    public String getDataFormatada() {
        return dataEvento == null ? "" : dataEvento.format(DATA_BR_FORMATTER);
    }

    public String getWhatsappPublicadorSomenteDigitos() {
        if (whatsappPublicador == null) {
            return "";
        }
        return whatsappPublicador.replaceAll("\\D", "");
    }

    public String getWhatsappPublicadorUrl() {
        String digitos = getWhatsappPublicadorSomenteDigitos();
        if (digitos.isEmpty()) {
            return "";
        }
        if (digitos.length() <= 11 && !digitos.startsWith("55")) {
            return "https://wa.me/55" + digitos;
        }
        return "https://wa.me/" + digitos;
    }
}
