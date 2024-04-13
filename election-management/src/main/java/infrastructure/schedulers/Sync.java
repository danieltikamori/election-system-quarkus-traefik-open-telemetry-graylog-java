/*
 * Copyright (c) 2024 Daniel I. Tikamori. All rights reserved.
 */

package infrastructure.schedulers;

import infrastructure.repositories.RedisElectionRepository;
import infrastructure.repositories.SQLElectionRepository;
import io.quarkus.scheduler.Scheduled;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Sync {
    private final SQLElectionRepository sqlRepository;
    private final RedisElectionRepository redisRepository;

    public Sync(SQLElectionRepository sqlRepository, RedisElectionRepository redisRepository) {
        this.sqlRepository = sqlRepository;
        this.redisRepository = redisRepository;
    }

    @Scheduled(cron = "*/5 * * * * ?")
    void sync() {

        sqlRepository.findAll().forEach(election -> sqlRepository.sync(redisRepository.sync(election)));
    }
}
