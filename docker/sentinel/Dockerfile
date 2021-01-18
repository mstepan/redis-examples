FROM redis:6.0

COPY sentinel.conf /usr/local/etc/redis/sentinel.conf

CMD [ "redis-sentinel", "/usr/local/etc/redis/sentinel.conf" ]
