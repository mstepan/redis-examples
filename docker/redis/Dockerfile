FROM redis:6.0

ARG REDIS_CONFIG

COPY ${REDIS_CONFIG} /usr/local/etc/redis/redis.conf

CMD [ "redis-server", "/usr/local/etc/redis/redis.conf" ]
