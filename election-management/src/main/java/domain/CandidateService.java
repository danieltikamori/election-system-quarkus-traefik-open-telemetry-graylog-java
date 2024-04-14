/*
 * Copyright (c) 2024 Daniel I. Tikamori. All rights reserved.
 */

package domain;

import javax.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CandidateService {
    private final CandidateRepository repository;

    public CandidateService(CandidateRepository repository) {
        this.repository = repository;
    }

    public void save(Candidate domain) {
        repository.save(domain);
    }

    public List<Candidate> findAll() {
        return repository.findAll();
    }

    public Candidate findById(String id) {
        return repository.findById(id).orElseThrow();
    }

    public void delete(String id) {
        repository.delete(id);
    }

    public Candidate update(Candidate domain) {
        return repository.update(domain);
    }

    public List<Candidate> findAll(int page, int size) {
        return repository.findAll(page, size);
    }
}