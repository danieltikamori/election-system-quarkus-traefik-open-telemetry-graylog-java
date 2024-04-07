/*
 * Copyright (c) 2024 Daniel I. Tikamori. All rights reserved.
 */

package domain;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ElectionService {
    private final ElectionRepository repository;

    public ElectionService(ElectionRepository repository) {
        this.repository = repository;
    }

    public List<Election> findAll() {
        return repository.findAll();
    }

//    With DDOS attack / brute force avoidance. No return.
    public void vote(String electionId, String candidateId) {
        repository.findById(electionId)
                  .candidates()
                  .stream()
                  .filter(candidate -> candidate.id().equals(candidateId))
                  .findFirst()
                  .ifPresent(candidate -> repository.vote(electionId, candidate));
    }
}