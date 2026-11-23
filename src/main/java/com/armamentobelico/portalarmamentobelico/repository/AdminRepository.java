package com.armamentobelico.portalarmamentobelico.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.armamentobelico.portalarmamentobelico.model.Admin;

@Repository
public interface AdminRepository extends CrudRepository<Admin, UUID> {
    
    Optional<Admin> findByUsername(String username);

}
