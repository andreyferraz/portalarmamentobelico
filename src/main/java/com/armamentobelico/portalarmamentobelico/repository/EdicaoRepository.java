package com.armamentobelico.portalarmamentobelico.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;

@Repository
public interface EdicaoRepository extends ListCrudRepository<Edicao, UUID> {

    List<Edicao> findAllByOrderByNumeroEdicaoDescIdiomaAsc();

    List<Edicao> findByIdiomaOrderByNumeroEdicaoDesc(IdiomaEdicao idioma);

    List<Edicao> findByPublicadoTrueOrderByNumeroEdicaoDesc();

    Optional<Edicao> findByNumeroEdicaoAndIdioma(Integer numeroEdicao, IdiomaEdicao idioma);

    Optional<Edicao> findByNumeroEdicaoAndIdiomaAndPublicadoTrue(Integer numeroEdicao, IdiomaEdicao idioma);

    List<Edicao> findByNumeroEdicaoAndPublicadoTrue(Integer numeroEdicao);

    boolean existsByNumeroEdicaoAndIdioma(Integer numeroEdicao, IdiomaEdicao idioma);

    boolean existsByNumeroEdicaoAndIdiomaAndIdNot(Integer numeroEdicao, IdiomaEdicao idioma, UUID id);

    @Query("SELECT * FROM edicao WHERE publicado = 1 AND idioma = :idioma AND numero_edicao < :numero ORDER BY numero_edicao DESC LIMIT 1")
    Optional<Edicao> findEdicaoAnteriorPublicada(@Param("idioma") String idioma, @Param("numero") Integer numero);

    @Query("SELECT * FROM edicao WHERE publicado = 1 AND idioma = :idioma AND numero_edicao > :numero ORDER BY numero_edicao ASC LIMIT 1")
    Optional<Edicao> findProximaEdicaoPublicada(@Param("idioma") String idioma, @Param("numero") Integer numero);
}
