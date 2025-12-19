package ru.practicum.moviehub.model;

import java.util.Objects;

public class Movie {
    private long id;
    private final String title;
    private final int year;

    public Movie(int year, String title, long id) {
        this.year = year;
        this.title = title;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return getId() == movie.getId() && getYear() == movie.getYear() && Objects.equals(getTitle(), movie.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getYear());
    }
}