package com.max.app.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.SslJedisSentinelPool;

import java.util.Set;

public final class TlsSentinelMain {

    private static final int REDIS_DB_INDEX = 0;

    private static final String SENTINEL_USERNAME = "max";
    private static final String SENTINEL_PASSWORD = "sentinel-pazzword!";

    // identical for master & slaves
    private static final String REDIS_PASSWORD = "pazzword!";

    private static final String SENTINEL_MASTER_NAME = "mymaster";

    /**
     * Connect to Sentinel using TLS.
     */
    public static void main(String[] args) throws Exception {

//        System.setProperty("javax.net.ssl.keyStore",
//                           "/Users/mstepan/repo/redis-examples/docker/certs/keystore.jks");
//        System.setProperty("javax.net.ssl.keyStorePassword", "611191");
//        System.setProperty("javax.net.ssl.keyStoreType", "JKS");


        System.setProperty("javax.net.ssl.trustStore",
                           "/Users/mstepan/repo/redis-examples/docker/certs-client/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "611191");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        System.setProperty("https.protocols", "TLSv1.3");

        try (SslJedisSentinelPool pool = new SslJedisSentinelPool(SENTINEL_MASTER_NAME,
                                                                  Set.of("localhost:26379",
                                                                         "localhost:26380",
                                                                         "localhost:26381"))) {

            try (Jedis jedis = pool.getResource()) {
                jedis.auth(REDIS_PASSWORD);
                jedis.select(REDIS_DB_INDEX);

                jedis.set("key0", "some value for key 0");

                System.out.printf("key0: %s%n", jedis.get("key0"));
            }
        }

        System.out.printf("RedisMain completed. java version: %s%n", System.getProperty("java.version"));
    }
}
