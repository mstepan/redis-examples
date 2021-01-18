package com.max.app.redis.point;

public record XYPoint(String id, int x, int y) {


    public boolean isValid(){
        return x == y;
    }
}
