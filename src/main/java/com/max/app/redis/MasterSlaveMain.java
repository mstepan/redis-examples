package com.max.app.redis;

import redis.clients.jedis.Jedis;

public final class MasterSlaveMain {

    private static final int REDIS_DB_INDEX = 0;

    // identical for master & slaves
    private static final String REDIS_PASSWORD = "pazzword!";

    enum Role {
        MASTER("localhost", 6379),
        SLAVE1("localhost", 6380),
        SLAVE2("localhost", 6381);

        private final String host;
        private final int port;

        Role(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    public static void main(String[] args) throws Exception {

        for (int i = 0; i < 10; ++i) {
            write(Role.MASTER, "dss:uap:async:key" + i, "value-" + i);
        }

        final String key = "dss:uap:async:key1";
        System.out.printf("master value: %s%n", read(Role.MASTER, key));
        System.out.printf("slave1 value: %s%n", read(Role.SLAVE1, key));
        System.out.printf("slave2 value: %s%n", read(Role.SLAVE2, key));

        System.out.printf("RedisMain completed. java version: %s%n", System.getProperty("java.version"));
    }

    private static void write(Role role, String key, String value) {

        try (Jedis jedis = new Jedis(role.host, role.port)) {
            jedis.auth(REDIS_PASSWORD);
            jedis.select(REDIS_DB_INDEX);

            jedis.set(key, value);
        }
    }

    private static String read(Role role, String key) {
        try (Jedis jedis = new Jedis(role.host, role.port)) {
            jedis.auth(REDIS_PASSWORD);
            jedis.select(REDIS_DB_INDEX);
            return jedis.get(key);
        }
    }

}
