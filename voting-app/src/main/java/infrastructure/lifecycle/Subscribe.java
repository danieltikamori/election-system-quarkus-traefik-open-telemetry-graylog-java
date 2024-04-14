/*
 * Copyright (c) 2024 Daniel I. Tikamori. All rights reserved.
 */

package infrastructure.lifecycle;

import infrastructure.repositories.RedisElectionRepository;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Startup
@ApplicationScoped
public class Subscribe {
    private static final Logger LOGGER = Logger.getLogger(Subscribe.class);

    private final ReactiveRedisDataSource dataSource;
    private final RedisElectionRepository repository;

    @Inject
    public Subscribe(ReactiveRedisDataSource dataSource,
                     RedisElectionRepository repository) {
        this.dataSource = dataSource;
        this.repository = repository;

        LOGGER.info("Startup: Subscribe");

        dataSource.pubsub(String.class)
                .subscribe("elections")
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .subscribe()
                .with(id -> {
                    LOGGER.info("Election " + id + " received from subscription");
                    LOGGER.info("Election " + repository.findById(id) + " starting");
                });
    }
}