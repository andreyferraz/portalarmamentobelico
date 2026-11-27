package com.armamentobelico.portalarmamentobelico.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.armamentobelico.portalarmamentobelico.model.EstadoBrasil;
import com.armamentobelico.portalarmamentobelico.model.PapelPublicador;
import com.armamentobelico.portalarmamentobelico.model.StatusModeracaoEvento;

import lombok.Data;

@Data
public class EventoAdminFormDTO {

    private UUID id;
    private String titulo;
    private MultipartFile flyer;
    private String flyerPathAtual;
    private String cidade;
    private EstadoBrasil estado;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataEvento;

    private String horario;
    private String contatoOrganizador;
    private String whatsappPublicador;
    private PapelPublicador papelPublicador;
    private StatusModeracaoEvento statusModeracao;
    private String motivoRecusa;
}
