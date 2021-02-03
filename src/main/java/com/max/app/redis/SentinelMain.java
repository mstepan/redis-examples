package com.max.app.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.Set;

public final class SentinelMain {

    private static final String PASSWORD = "pazzword!";
    private static final int DB_INDEX = 0;

    private static final String SENTINEL_MASTER_NAME = "mymaster";

    public static void main(String[] args) throws Exception {

        try (JedisSentinelPool pool = new JedisSentinelPool(SENTINEL_MASTER_NAME,
                                                            Set.of("0.0.0.0:26379",
                                                                   "0.0.0.0:26380",
                                                                   "0.0.0.0:26381"))) {
            try (Jedis jedis = pool.getResource()) {
                jedis.auth(PASSWORD);
                jedis.select(DB_INDEX);

                jedis.set("key0", "value-0");

                System.out.printf("key0: %s%n", jedis.get("key0"));
            }
        }

        System.out.printf("SentinelMain completed. java version: %s%n", System.getProperty("java.version"));
    }

}
