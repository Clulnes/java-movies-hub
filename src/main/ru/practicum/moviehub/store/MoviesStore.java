package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;
import java.util.stream.Collectors;

public class MoviesStore {
    private final Map<Long, Movie> movies = new HashMap<>();
    private long currentId = 1;

    public List<Movie> getAll() {
        return new ArrayList<>(movies.values());
    }

    public void add(Movie movie) {
        movie.setId(currentId++);
        movies.put(movie.getId(), movie);
    }

    public boolean delete(long id) {
        return movies.remove(id) != null;
    }

    public void clear() {
        movies.clear();
        currentId = 1;
    }

    public Optional<Movie> getById(long id) {
        return Optional.ofNullable(movies.get(id));
    }

    public List<Movie> getByYear(int year) {
        return movies.values().stream()
                .filter(movie -> movie.getYear() == year)
                .collect(Collectors.toList());
    }
}