package com.max.app.redis.movie;

public class Movie {

    private final String id;

    private final String title;
    private final String description;

    private final int year;

    private final int votes;

    public Movie(String id, String title, String description, int year, int votes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.year = year;
        this.votes = votes;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getYear() {
        return year;
    }

    public int getVotes() {
        return votes;
    }

    @Override
    public String toString() {
        return "id: " + id +
                ", title: " + title +
                ", description: " + description +
                ", year: " + year +
                ", votes: " + votes;
    }
}
