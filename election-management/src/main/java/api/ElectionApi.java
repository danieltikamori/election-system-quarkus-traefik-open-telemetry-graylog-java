/*
 * Copyright (c) 2024 Daniel I. Tikamori. All rights reserved.
 */

package api;

import domain.ElectionService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ElectionApi {
    private final ElectionService service;

    public ElectionApi(ElectionService service) {
        this.service = service;
    }

    public void submit() {
        service.submit();
    }
}