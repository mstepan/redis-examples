package redis.clients.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;

import javax.net.ssl.SSLSocketFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class SslJedisSentinelPool extends JedisPoolAbstract {
    protected Logger log;
    protected final GenericObjectPoolConfig poolConfig;
    protected final int connectionTimeout;
    protected final int soTimeout;
    protected final int infiniteSoTimeout;
    protected final String user;
    protected final String password;
    protected final int database;
    protected final String clientName;
    protected int sentinelConnectionTimeout;
    protected int sentinelSoTimeout;
    protected String sentinelUser;
    protected String sentinelPassword;
    protected String sentinelClientName;
    protected final Set<SslJedisSentinelPool.MasterListener> masterListeners;
    private volatile JedisFactory factory;
    private volatile HostAndPort currentHostMaster;
    private final Object initPoolLock;

    //TODO: add custom SSL socket factory using constructor
    private SSLSocketFactory sslSocketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();

    public SslJedisSentinelPool(String masterName, Set<String> sentinels) {
        this(masterName, sentinels, new GenericObjectPoolConfig(), 20000, 20000, 20000, null, null,
             Protocol.DEFAULT_DATABASE, null, 20000, 20000, null, null, null);
    }

    public SslJedisSentinelPool(String masterName, Set<String> sentinels, GenericObjectPoolConfig poolConfig,
                                int connectionTimeout, int soTimeout, int infiniteSoTimeout, String user, String password,
                                int database, String clientName, int sentinelConnectionTimeout, int sentinelSoTimeout,
                                String sentinelUser, String sentinelPassword, String sentinelClientName) {
        this.log = LoggerFactory.getLogger(this.getClass().getName());
        this.masterListeners = new HashSet<>();
        this.initPoolLock = new Object();
        this.poolConfig = poolConfig;
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.infiniteSoTimeout = infiniteSoTimeout;
        this.user = user;
        this.password = password;
        this.database = database;
        this.clientName = clientName;
        this.sentinelConnectionTimeout = sentinelConnectionTimeout;
        this.sentinelSoTimeout = sentinelSoTimeout;
        this.sentinelUser = sentinelUser;
        this.sentinelPassword = sentinelPassword;
        this.sentinelClientName = sentinelClientName;
        HostAndPort master = this.initSentinels(sentinels, masterName);
        this.initPool(master);
    }

    public void destroy() {
        Iterator var1 = this.masterListeners.iterator();

        while(var1.hasNext()) {
            SslJedisSentinelPool.MasterListener m = (SslJedisSentinelPool.MasterListener)var1.next();
            m.shutdown();
        }

        super.destroy();
    }

    private void initPool(HostAndPort master) {
        synchronized(this.initPoolLock) {
            if (!master.equals(this.currentHostMaster)) {
                this.currentHostMaster = master;
                if (this.factory == null) {

                    // TODO: use TLS for Jedis here
                    this.factory = new JedisFactory(master.getHost(), master.getPort(),
                                                    this.connectionTimeout, this.soTimeout,
                                                     this.password, this.database,
                                                    this.clientName, true, this.sslSocketFactory, null,
                                                    null);
                    this.initPool(this.poolConfig, this.factory);
                } else {
                    this.factory.setHostAndPort(this.currentHostMaster);
                    this.internalPool.clear();
                }

                this.log.info("Created JedisPool to master at {}", master);
            }

        }
    }

    private HostAndPort initSentinels(Set<String> sentinels, String masterName) {
        HostAndPort master = null;
        boolean sentinelAvailable = false;
        this.log.info("Trying to find master from available Sentinels...");
        Iterator var5 = sentinels.iterator();

        String sentinel;
        HostAndPort hap;
        while(var5.hasNext()) {
            sentinel = (String)var5.next();
            hap = HostAndPort.parseString(sentinel);
            this.log.debug("Connecting to Sentinel {}", hap);
            Jedis jedis = null;

            try {

                // TODO: use TLS for Jedis here
                jedis = new Jedis(hap.getHost(), hap.getPort(), this.sentinelConnectionTimeout, this.sentinelSoTimeout,
                                  true, this.sslSocketFactory, null, null);

                if (this.sentinelUser != null) {
                    jedis.auth(this.sentinelUser, this.sentinelPassword);
                } else if (this.sentinelPassword != null) {
                    jedis.auth(this.sentinelPassword);
                }

                if (this.sentinelClientName != null) {
                    jedis.clientSetname(this.sentinelClientName);
                }

                List<String> masterAddr = jedis.sentinelGetMasterAddrByName(masterName);
                sentinelAvailable = true;
                if (masterAddr != null && masterAddr.size() == 2) {
                    master = this.toHostAndPort(masterAddr);
                    this.log.debug("Found Redis master at {}", master);
                    break;
                }

                this.log.warn("Can not get master addr, master name: {}. Sentinel: {}", masterName, hap);
            } catch (JedisException var13) {
                this.log.warn("Cannot get master address from sentinel running @ {}. Reason: {}. Trying next one.", hap, var13);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }

            }
        }

        if (master == null) {
            if (sentinelAvailable) {
                throw new JedisException("Can connect to sentinel, but " + masterName + " seems to be not monitored...");
            } else {
                throw new JedisConnectionException("All sentinels down, cannot determine where is " + masterName + " master is running...");
            }
        } else {
            this.log.info("Redis master running at {}, starting Sentinel listeners...", master);
            var5 = sentinels.iterator();

            while(var5.hasNext()) {
                sentinel = (String)var5.next();
                hap = HostAndPort.parseString(sentinel);
                SslJedisSentinelPool.MasterListener masterListener = new SslJedisSentinelPool.MasterListener(masterName, hap.getHost(), hap.getPort());
                masterListener.setDaemon(true);
                this.masterListeners.add(masterListener);
                masterListener.start();
            }

            return master;
        }
    }

    private HostAndPort toHostAndPort(List<String> getMasterAddrByNameResult) {
        String host = (String)getMasterAddrByNameResult.get(0);
        int port = Integer.parseInt((String)getMasterAddrByNameResult.get(1));
        return new HostAndPort(host, port);
    }

    public Jedis getResource() {
        while(true) {
            Jedis jedis = (Jedis)super.getResource();
            jedis.setDataSource(this);
            HostAndPort master = this.currentHostMaster;
            HostAndPort connection = new HostAndPort(jedis.getClient().getHost(), jedis.getClient().getPort());
            if (master.equals(connection)) {
                return jedis;
            }

            this.returnBrokenResource(jedis);
        }
    }

    protected void returnBrokenResource(Jedis resource) {
        if (resource != null) {
            this.returnBrokenResourceObject(resource);
        }

    }

    protected void returnResource(Jedis resource) {
        if (resource != null) {
            try {
                resource.resetState();
                this.returnResourceObject(resource);
            } catch (Exception var3) {
                this.returnBrokenResource(resource);
                throw new JedisException("Resource is returned to the pool as broken", var3);
            }
        }

    }

    protected class MasterListener extends Thread {
        protected String masterName;
        protected String host;
        protected int port;
        protected long subscribeRetryWaitTimeMillis;
        protected volatile Jedis j;
        protected AtomicBoolean running;

        public MasterListener(String masterName, String host, int port) {
            super(String.format("MasterListener-%s-[%s:%d]", masterName, host, port));
            this.subscribeRetryWaitTimeMillis = 5000L;
            this.running = new AtomicBoolean(false);
            this.masterName = masterName;
            this.host = host;
            this.port = port;
        }

        public void run() {
            this.running.set(true);

            while(this.running.get()) {
                try {
                    if (!this.running.get()) {
                        break;
                    }

                    // TODO: use TLS for Jedis here
                    this.j = new Jedis(this.host, this.port, SslJedisSentinelPool.this.sentinelConnectionTimeout,
                                       SslJedisSentinelPool.this.sentinelSoTimeout, true);

                    if (SslJedisSentinelPool.this.sentinelUser != null) {
                        this.j.auth(SslJedisSentinelPool.this.sentinelUser, SslJedisSentinelPool.this.sentinelPassword);
                    } else if (SslJedisSentinelPool.this.sentinelPassword != null) {
                        this.j.auth(SslJedisSentinelPool.this.sentinelPassword);
                    }

                    if (SslJedisSentinelPool.this.sentinelClientName != null) {
                        this.j.clientSetname(SslJedisSentinelPool.this.sentinelClientName);
                    }

                    List<String> masterAddr = this.j.sentinelGetMasterAddrByName(this.masterName);
                    if (masterAddr != null && masterAddr.size() == 2) {
                        SslJedisSentinelPool.this.initPool(SslJedisSentinelPool.this.toHostAndPort(masterAddr));
                    } else {
                        SslJedisSentinelPool.this.log.warn("Can not get master addr, master name: {}. Sentinel: {}:{}.", new Object[]{this.masterName, this.host, this.port});
                    }

                    this.j.subscribe(new JedisPubSub() {
                        public void onMessage(String channel, String message) {
                            SslJedisSentinelPool.this.log.debug("Sentinel {}:{} published: {}.", new Object[]{SslJedisSentinelPool.MasterListener.this.host, SslJedisSentinelPool.MasterListener.this.port, message});
                            String[] switchMasterMsg = message.split(" ");
                            if (switchMasterMsg.length > 3) {
                                if (SslJedisSentinelPool.MasterListener.this.masterName.equals(switchMasterMsg[0])) {
                                    SslJedisSentinelPool.this.initPool(SslJedisSentinelPool.this.toHostAndPort(Arrays.asList(switchMasterMsg[3], switchMasterMsg[4])));
                                } else {
                                    SslJedisSentinelPool.this.log.debug("Ignoring message on +switch-master for master name {}, our master name is {}", switchMasterMsg[0], SslJedisSentinelPool.MasterListener.this.masterName);
                                }
                            } else {
                                SslJedisSentinelPool.this.log.error(
                                        "Invalid message received on Sentinel {}:{} on channel +switch-master: {}", new Object[]{SslJedisSentinelPool.MasterListener.this.host, SslJedisSentinelPool.MasterListener.this.port, message});
                            }

                        }
                    }, new String[]{"+switch-master"});
                } catch (JedisException var8) {
                    if (this.running.get()) {
                        SslJedisSentinelPool.this.log.error("Lost connection to Sentinel at {}:{}. Sleeping 5000ms and retrying.", new Object[]{this.host, this.port, var8});

                        try {
                            Thread.sleep(this.subscribeRetryWaitTimeMillis);
                        } catch (InterruptedException var7) {
                            SslJedisSentinelPool.this.log.error("Sleep interrupted: ", var7);
                        }
                    } else {
                        SslJedisSentinelPool.this.log.debug("Unsubscribing from Sentinel at {}:{}", this.host, this.port);
                    }
                } finally {
                    if (this.j != null) {
                        this.j.close();
                    }

                }
            }

        }

        public void shutdown() {
            try {
                SslJedisSentinelPool.this.log.debug("Shutting down listener on {}:{}", this.host, this.port);
                this.running.set(false);
                if (this.j != null) {
                    this.j.disconnect();
                }
            } catch (Exception var2) {
                SslJedisSentinelPool.this.log.error("Caught exception while shutting down: ", var2);
            }

        }
    }
}
