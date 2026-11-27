package com.armamentobelico.portalarmamentobelico.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.armamentobelico.portalarmamentobelico.model.Evento;
import com.armamentobelico.portalarmamentobelico.model.StatusModeracaoEvento;

@Repository
public interface EventoRepository extends ListCrudRepository<Evento, UUID> {

    @Query("""
            SELECT * FROM evento
            WHERE status_moderacao = 'APROVADO'
              AND (:estado IS NULL OR :estado = '' OR :estado = 'all' OR estado = :estado)
              AND (
                  :statusTemporal IS NULL
                  OR :statusTemporal = ''
                  OR :statusTemporal = 'all'
                  OR (:statusTemporal = 'upcoming' AND data_evento >= :hojeIso)
                  OR (:statusTemporal = 'past' AND data_evento < :hojeIso)
              )
            ORDER BY
              CASE WHEN data_evento >= :hojeIso THEN 0 ELSE 1 END ASC,
              CASE WHEN data_evento >= :hojeIso THEN data_evento END ASC,
              CASE WHEN data_evento >= :hojeIso THEN horario END ASC,
              CASE WHEN data_evento <  :hojeIso THEN data_evento END DESC,
              CASE WHEN data_evento <  :hojeIso THEN horario END DESC
            """)
    List<Evento> findAprovadosOrdenados(
            @Param("hojeIso") String hojeIso,
            @Param("estado") String estado,
            @Param("statusTemporal") String statusTemporal);

    @Query("""
            SELECT * FROM evento
            WHERE (:statusModeracao IS NULL OR :statusModeracao = '' OR :statusModeracao = 'TODOS' OR :statusModeracao = 'ALL' OR status_moderacao = :statusModeracao)
              AND (:estado IS NULL OR :estado = '' OR :estado = 'all' OR :estado = 'TODOS' OR estado = :estado)
            ORDER BY
              CASE WHEN status_moderacao = 'PENDENTE' THEN 0 ELSE 1 END ASC,
              CASE WHEN status_moderacao = 'PENDENTE' THEN criado_em END ASC,
              CASE WHEN status_moderacao <> 'PENDENTE' THEN criado_em END DESC
            """)
    List<Evento> findParaAdmin(
            @Param("statusModeracao") String statusModeracao,
            @Param("estado") String estado);

    long countByStatusModeracao(StatusModeracaoEvento statusModeracao);
}
