package com.armamentobelico.portalarmamentobelico.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.armamentobelico.portalarmamentobelico.model.Noticia;

@Repository
public interface NoticiaRepository extends ListCrudRepository<Noticia, UUID> {

    Optional<Noticia> findBySlug(String slug);

    Optional<Noticia> findBySlugAndPublicadoTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Noticia> findAllByOrderByDataPublicacaoDescCriadoEmDesc();

    List<Noticia> findByPublicadoTrueOrderByDataPublicacaoDescCriadoEmDesc();

    @Query("SELECT * FROM noticia WHERE publicado = 1 ORDER BY data_publicacao DESC, criado_em DESC LIMIT 2")
    List<Noticia> findUltimasDuasPublicadas();

    @Query("SELECT DISTINCT n.* FROM noticia n JOIN noticia_tag t ON t.noticia_id = n.id "
            + "WHERE n.publicado = 1 AND LOWER(t.nome) = LOWER(:tag) "
            + "ORDER BY n.data_publicacao DESC, n.criado_em DESC")
    List<Noticia> findPublicadasPorTag(@Param("tag") String tag);
}
