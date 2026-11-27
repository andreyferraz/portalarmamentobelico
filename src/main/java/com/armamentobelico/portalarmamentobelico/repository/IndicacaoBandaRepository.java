package com.armamentobelico.portalarmamentobelico.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import com.armamentobelico.portalarmamentobelico.model.IndicacaoBanda;
import com.armamentobelico.portalarmamentobelico.model.StatusIndicacaoBanda;

@Repository
public interface IndicacaoBandaRepository extends ListCrudRepository<IndicacaoBanda, UUID> {

    List<IndicacaoBanda> findByStatusInOrderByCriadoEmDesc(Collection<StatusIndicacaoBanda> statuses);

    List<IndicacaoBanda> findAllByOrderByCriadoEmDesc();

    List<IndicacaoBanda> findByStatusOrderByCriadoEmDesc(StatusIndicacaoBanda status);

    List<IndicacaoBanda> findByNomeBandaNormalizadoAndPaisNormalizadoAndStatusIn(
            String nomeBandaNormalizado,
            String paisNormalizado,
            Collection<StatusIndicacaoBanda> statuses);

    List<IndicacaoBanda> findByEdicaoPublicadaId(UUID edicaoPublicadaId);

    long countByStatus(StatusIndicacaoBanda status);
}
