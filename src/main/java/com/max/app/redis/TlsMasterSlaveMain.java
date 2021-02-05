package com.max.app.redis;

import redis.clients.jedis.Jedis;

import java.util.concurrent.ThreadLocalRandom;

public final class TlsMasterSlaveMain {

    private static final int REDIS_DB_INDEX = 0;

    // identical for master & slaves
    private static final String REDIS_PASSWORD = "pazzword!";

    private static final ThreadLocalRandom RAND = ThreadLocalRandom.current();

    private enum RedisInstance {
        MASTER("rediss://localhost:6379"),
        SLAVE1("rediss://localhost:6380"),
        SLAVE2("rediss://localhost:6381");

        private final String url;

        RedisInstance(String url) {
            this.url = url;
        }
    }

    public static void main(String[] args) throws Exception {

        System.setProperty("javax.net.ssl.trustStore",
                           "/Users/mstepan/repo/redis-examples/docker/certs-client/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "611191");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
//        System.setProperty("javax.net.debug", "all");
        System.setProperty("https.protocols", "TLSv1.3");

        writeValue(RedisInstance.MASTER, "key1", "value for key1 is " + RAND.nextInt(100));

        System.out.printf("master value: %s%n", readValue(RedisInstance.MASTER, "key1"));
        System.out.printf("slave1 value: %s%n", readValue(RedisInstance.SLAVE1, "key1"));
        System.out.printf("slave2 value: %s%n", readValue(RedisInstance.SLAVE2, "key1"));

        System.out.printf("RedisMain completed. java version: %s%n", System.getProperty("java.version"));
    }

    private static void writeValue(RedisInstance instance, String key, String value) {
        try (Jedis jedis = new Jedis(instance.url)) {
            jedis.auth(REDIS_PASSWORD);
            jedis.select(REDIS_DB_INDEX);
            jedis.set(key, value);
        }
    }

    private static String readValue(RedisInstance instance, String key) {
        try (Jedis jedis = new Jedis(instance.url)) {
            jedis.auth(REDIS_PASSWORD);
            jedis.select(REDIS_DB_INDEX);
            return jedis.get(key);
        }
    }


}
