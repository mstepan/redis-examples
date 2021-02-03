package com.max.app.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.SslJedisSentinelPool;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Set;

public final class SslSentinelMain {

    private static final String SENTINEL_MASTER_NAME = "dss-master";

    public static void main(String[] args) throws Exception {

        SSLSocketFactory sslFactory = createSslSocketFactory();
        SSLParameters sslParameters = new SSLParameters();
        HostnameVerifier hostnameVerifier = (hostname, session) -> true;

        try (SslJedisSentinelPool pool = new SslJedisSentinelPool(SENTINEL_MASTER_NAME,
                                                                  Set.of("0.0.0.0:26379"),
                                                                  sslFactory, sslParameters, hostnameVerifier)) {

            try (Jedis jedis = pool.getResource()) {
                jedis.select(0); //0 - stage, 1 - prod
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
