package com.max.app.redis.point;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointService implements AutoCloseable {

    private static final String KEY = "points:%s:data";

    private final Jedis jedis = new Jedis("localhost");

    public XYPoint getById(String id) {
        List<String> values = jedis.hmget(String.format(KEY, id), "x", "y");
        return new XYPoint(id, Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)));
    }

    public void save(XYPoint point) {
        Map<String, String> values = new HashMap<>();
        values.put("x", String.valueOf(point.x()));
        values.put("y", String.valueOf(point.y()));

        jedis.hset(String.format(KEY, point.id()), values);
    }

    public void movePoint(String id, int x, int y) {
        Map<String, String> values = new HashMap<>();
        values.put("x", String.valueOf(x));
        values.put("y", String.valueOf(y));

        jedis.hset(String.format(KEY, id), values);
    }

    @Override
    public void close() throws Exception {
        jedis.close();
    }
}
