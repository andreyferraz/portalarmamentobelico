package com.armamentobelico.portalarmamentobelico.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.armamentobelico.portalarmamentobelico.model.EdicaoConteudoExtra;

@Repository
public interface EdicaoConteudoExtraRepository extends ListCrudRepository<EdicaoConteudoExtra, UUID> {

    List<EdicaoConteudoExtra> findByEdicaoIdOrderByOrdemAsc(UUID edicaoId);

    @Modifying
    @Query("DELETE FROM edicao_conteudo_extra WHERE edicao_id = :edicaoId")
    void deleteByEdicaoId(@Param("edicaoId") UUID edicaoId);
}
