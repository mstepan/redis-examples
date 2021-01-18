package com.max.app.redis.movie;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MovieService implements AutoCloseable {

    private static final String KEY_FORMAT = "movies:%s:info";

    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;

    private final Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);

    public void store(Movie movie) {
        // As per Redis 4.0.0, HMSET is considered deprecated. Please prefer HSET in new code.
        jedis.hset(key(movie.getId()), toMap(movie));
    }

    public Movie findById(String id) {
        return toMovie(jedis.hgetAll(key(id)));
    }

    public void delete(Movie movie) {
        jedis.del(key(movie.getId()));
    }

    public void upVote(Movie movie) {
        jedis.hincrBy(key(movie.getId()), "votes", 1);
    }

    public void downVote(Movie movie) {
        jedis.hincrBy(key(movie.getId()), "votes", -1);
    }

    @Override
    public void close() {
        jedis.close();
    }

    private static String key(String id) {
        return String.format(KEY_FORMAT, id);
    }

    private static Map<String, String> toMap(Movie movie) {
        Map<String, String> map = new HashMap<>();
        map.put("id", movie.getId());
        map.put("title", movie.getTitle());
        map.put("description", movie.getDescription());
        map.put("year", String.valueOf(movie.getYear()));
        map.put("votes", String.valueOf(movie.getVotes()));
        return map;
    }

    private static Movie toMovie(Map<String, String> data) {
        return new Movie(data.get("id"),
                         data.get("title"),
                         data.get("description"),
                         Integer.parseInt(data.get("year")),
                         Integer.parseInt(data.get("votes")));
    }

}
