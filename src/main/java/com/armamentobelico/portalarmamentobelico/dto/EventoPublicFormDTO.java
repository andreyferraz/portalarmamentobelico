package com.armamentobelico.portalarmamentobelico.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class EventoPublicFormDTO {

    private String title;
    private MultipartFile flyer;
    private String city;
    private String state;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    private String time;
    private String organizerContact;
    private String publisherWhatsapp;
    private String publisherRole;
    private boolean rulesAgree;
    private String website; // Honeypot anti-bot

    // Compatibilidade com nomes snake_case caso enviados por clientes ou testes
    public void setOrganizer_contact(String organizerContact) {
        this.organizerContact = organizerContact;
    }

    public void setPublisher_whatsapp(String publisherWhatsapp) {
        this.publisherWhatsapp = publisherWhatsapp;
    }

    public void setPublisher_role(String publisherRole) {
        this.publisherRole = publisherRole;
    }

    public void setRules_agree(boolean rulesAgree) {
        this.rulesAgree = rulesAgree;
    }
}
