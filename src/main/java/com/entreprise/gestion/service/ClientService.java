package com.entreprise.gestion.service;

import com.entreprise.gestion.model.Client;
import com.entreprise.gestion.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {
    
    private final ClientRepository clientRepository;
    
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }
    
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }
    
    public Client createClient(Client client) {
        if (client.getEmail() != null && clientRepository.findByEmail(client.getEmail()).isPresent()) {
            throw new RuntimeException("Un client avec cet email existe déjà");
        }
        return clientRepository.save(client);
    }
    
    public Client updateClient(Long id, Client clientDetails) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'id: " + id));
        
        if (clientDetails.getEmail() != null && !clientDetails.getEmail().equals(client.getEmail())) {
            if (clientRepository.findByEmail(clientDetails.getEmail()).isPresent()) {
                throw new RuntimeException("Un client avec cet email existe déjà");
            }
        }
        
        client.setNom(clientDetails.getNom());
        client.setEmail(clientDetails.getEmail());
        client.setTelephone(clientDetails.getTelephone());
        client.setAdresse(clientDetails.getAdresse());
        client.setVille(clientDetails.getVille());
        client.setCodePostal(clientDetails.getCodePostal());
        
        return clientRepository.save(client);
    }
    
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'id: " + id));
        clientRepository.delete(client);
    }
    
    public List<Client> searchClients(String search) {
        return clientRepository.searchClients(search);
    }
}

