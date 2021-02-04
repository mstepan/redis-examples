package com.max.app.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Set;

public final class PlainSentinelTlsToMasterMain {

    private static final int REDIS_DB_INDEX = 0;

    // identical for master & slaves
    private static final String REDIS_PASSWORD = "pazzword!";

    private static final String SENTINEL_PASSWORD = "sentinel-pazzword!";

    private static final String SENTINEL_MASTER_NAME = "mymaster";

    /**
     * Connect to Sentinel using plain port without TLS (but with password).
     * Connect to master using TLS.
     */
    public static void main(String[] args) throws Exception {

        System.setProperty("javax.net.ssl.trustStore",
                           "/Users/mstepan/repo/redis-examples/docker/certs/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "611191");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("https.protocols", "TLSv1.3");

        try (JedisSentinelPool pool = new JedisSentinelPool(SENTINEL_MASTER_NAME,
                                                            Set.of("localhost:26000",
                                                                   "localhost:26001",
                                                                   "localhost:26002"),
                                                            SENTINEL_PASSWORD)) {

            HostAndPort masterHostAndPort = pool.getCurrentHostMaster();

            System.out.printf("master host & port: %s:%d%n", masterHostAndPort.getHost(), masterHostAndPort.getPort());

            try (Jedis jedis = new Jedis(masterHostAndPort.getHost(), masterHostAndPort.getPort(), true)) {
                jedis.auth(REDIS_PASSWORD);
                jedis.select(REDIS_DB_INDEX);

                jedis.set("key0", "some value for key 0");

                System.out.printf("key0: %s%n", jedis.get("key0"));
            }
        }

        System.out.printf("RedisMain completed. java version: %s%n", System.getProperty("java.version"));
    }

    private static SSLSocketFactory createSslSocketFactory() throws Exception {

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in = Files.newInputStream(Path.of("/Users/mstepan/opower/dss/redis-ms/certs/redis.crt"));
        Certificate ca = cf.generateCertificate(in);

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(null, tmf.getTrustManagers(), null);
        return context.getSocketFactory();
    }
}
