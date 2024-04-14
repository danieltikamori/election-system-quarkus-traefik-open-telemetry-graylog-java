/*
 * Copyright (c) 2024 Daniel I. Tikamori. All rights reserved.
 */

package domain;

import domain.annotations.SQL;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import java.util.List;

@ApplicationScoped
public class ElectionService {
    private final ElectionRepository repository;
    private final Instance<ElectionRepository> repositories;
    private final CandidateService candidateService;

    public ElectionService(@SQL ElectionRepository repository, @Any Instance<ElectionRepository> repositories, CandidateService candidateService) {
        this.repository = repository;
        this.repositories = repositories;
        this.candidateService = candidateService;
    }

    public void submit() {
        Election election = Election.create(candidateService.findAll());
        repositories.forEach(repository -> repository.submit(election));
    }

    public List<Election> findAll() {
        return repository.findAll();
    }
}