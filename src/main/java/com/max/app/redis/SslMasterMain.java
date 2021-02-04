package com.max.app.redis;

import redis.clients.jedis.Jedis;

public final class SslMasterMain {

    private static final int REDIS_DB_INDEX = 0;

    // identical for master & slaves
    private static final String REDIS_PASSWORD = "pazzword!";

    public static void main(String[] args) throws Exception {

        System.setProperty("javax.net.ssl.trustStore",
                           "/Users/mstepan/repo/redis-examples/docker/certs/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "611191");
//        System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
//        System.setProperty("javax.net.debug", "all");
//        System.setProperty("https.protocols", "TLSv1.2");

        try (Jedis jedis = new Jedis("rediss://localhost:6379")) {
            jedis.auth(REDIS_PASSWORD);
            jedis.select(REDIS_DB_INDEX);

            jedis.set("key0", "some value 0");

            System.out.printf("key0: %s%n", jedis.get("key0"));
        }

        System.out.printf("RedisMain completed. java version: %s%n", System.getProperty("java.version"));
    }


}
