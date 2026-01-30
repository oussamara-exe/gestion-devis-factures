package com.entreprise.gestion.repository;

import com.entreprise.gestion.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    Optional<Client> findByEmail(String email);
    
    @Query("SELECT c FROM Client c WHERE c.nom LIKE %:search% OR c.email LIKE %:search%")
    List<Client> searchClients(@Param("search") String search);
}

