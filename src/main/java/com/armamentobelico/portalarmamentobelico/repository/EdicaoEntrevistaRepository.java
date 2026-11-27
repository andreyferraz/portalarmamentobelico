package com.armamentobelico.portalarmamentobelico.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.armamentobelico.portalarmamentobelico.model.EdicaoEntrevista;

@Repository
public interface EdicaoEntrevistaRepository extends ListCrudRepository<EdicaoEntrevista, UUID> {

    List<EdicaoEntrevista> findByEdicaoIdOrderByOrdemAsc(UUID edicaoId);

    List<EdicaoEntrevista> findAllByOrderByEdicaoIdAscOrdemAsc();

    @Modifying
    @Query("DELETE FROM edicao_entrevista WHERE edicao_id = :edicaoId")
    void deleteByEdicaoId(@Param("edicaoId") UUID edicaoId);

    @Modifying
    @Query("UPDATE edicao_entrevista SET indicacao_banda_id = NULL WHERE indicacao_banda_id = :indicacaoId")
    void clearIndicacaoBandaId(@Param("indicacaoId") UUID indicacaoId);

    @Query("""
            SELECT ee.edicao_id AS edicao_id,
                   e.numero_edicao AS numero_edicao,
                   e.titulo AS titulo,
                   e.idioma AS idioma,
                   ee.banda AS banda,
                   ee.pais AS pais,
                   ee.indicacao_banda_id AS indicacao_banda_id
            FROM edicao_entrevista ee
            INNER JOIN edicao e ON e.id = ee.edicao_id
            WHERE e.publicado = 1
            ORDER BY e.numero_edicao DESC, ee.ordem ASC
            """)
    List<EntrevistaPublicadaRow> findEntrevistasDeEdicoesPublicadas();

    record EntrevistaPublicadaRow(
            UUID edicaoId,
            Integer numeroEdicao,
            String titulo,
            String idioma,
            String banda,
            String pais,
            UUID indicacaoBandaId) {
    }
}
