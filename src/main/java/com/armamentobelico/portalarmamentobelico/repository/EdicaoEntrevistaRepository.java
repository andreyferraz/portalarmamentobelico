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
}
