/*
 * Copyright (c) 2024 Daniel I. Tikamori. All rights reserved.
 */

package api.dto.out;

import domain.Candidate;

import java.util.List;

public record Election(String id, List<String> candidates) {
    public static Election fromDomain(domain.Election election) {
        return new Election(election.id(), election.candidates().stream().map(Candidate::id).toList());
    }
}