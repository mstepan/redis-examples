package com.max.app.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.Set;

public final class SentinelMain {

    private static final int REDIS_DB_INDEX = 0;

    // identical for master & slaves
    private static final String REDIS_PASSWORD = "pazzword!";

    private static final String SENTINEL_MASTER_NAME = "mymaster";

    public static void main(String[] args) throws Exception {

        try (JedisSentinelPool pool = new JedisSentinelPool(SENTINEL_MASTER_NAME,
                                                            Set.of("localhost:26379",
                                                                   "localhost:26380",
                                                                   "localhost:26381"))) {
            try (Jedis jedis = pool.getResource()) {
                jedis.auth(REDIS_PASSWORD);
                jedis.select(REDIS_DB_INDEX);

                for(int i =0; i < 3; ++i) {
                    jedis.set( String.format("key%d", i), String.format("value-%d", i));
                }

                System.out.println(jedis.get("key0"));
                System.out.println(jedis.get("key1"));
                System.out.println(jedis.get("key2"));
            }
        }

        System.out.printf("RedisMain completed. java version: %s%n", System.getProperty("java.version"));
    }
}
