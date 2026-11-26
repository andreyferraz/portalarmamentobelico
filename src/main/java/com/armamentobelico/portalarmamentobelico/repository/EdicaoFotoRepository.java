package com.armamentobelico.portalarmamentobelico.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.armamentobelico.portalarmamentobelico.model.EdicaoFoto;

@Repository
public interface EdicaoFotoRepository extends ListCrudRepository<EdicaoFoto, UUID> {

    List<EdicaoFoto> findByEdicaoIdOrderByOrdemAsc(UUID edicaoId);

    @Modifying
    @Query("DELETE FROM edicao_foto WHERE edicao_id = :edicaoId")
    void deleteByEdicaoId(@Param("edicaoId") UUID edicaoId);
}
