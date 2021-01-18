package com.max.app.redis.movie;

public final class MovieMain {

    public static void main(String[] args) throws Exception {

        try (MovieService service = new MovieService()) {

            Movie alien = new Movie("id-123", "Alien", "Cool old movie", 1976, 0);
            service.store(alien);

            for (int i = 0; i < 10; ++i) {
                service.upVote(alien);
            }
            service.downVote(alien);

            System.out.println(service.findById(alien.getId()));
        }


        System.out.printf("Main completed. java version: %s%n", System.getProperty("java.version"));
    }

}
