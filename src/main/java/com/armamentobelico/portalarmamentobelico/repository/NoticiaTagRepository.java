package com.armamentobelico.portalarmamentobelico.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.armamentobelico.portalarmamentobelico.model.NoticiaTag;

@Repository
public interface NoticiaTagRepository extends ListCrudRepository<NoticiaTag, UUID> {

    List<NoticiaTag> findByNoticiaIdOrderByOrdemAsc(UUID noticiaId);

    List<NoticiaTag> findByNoticiaIdInOrderByOrdemAsc(Collection<UUID> noticiaIds);

    @Modifying
    @Query("DELETE FROM noticia_tag WHERE noticia_id = :noticiaId")
    void deleteByNoticiaId(@Param("noticiaId") UUID noticiaId);
}
