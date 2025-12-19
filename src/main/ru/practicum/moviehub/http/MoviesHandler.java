package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore store;
    private final Gson gson;

    public MoviesHandler(MoviesStore store, Gson gson) {
        this.store = store;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    handleGet(ex);
                    break;
                case "POST":
                    handlePost(ex);
                    break;
                case "DELETE":
                    handleDelete(ex);
                    break;
                default:
                    ex.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ex.sendResponseHeaders(500, -1);
        }
    }

    private void handleGet(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();
        String[] pathParts = path.split("/");

        if (pathParts.length == 2) {
            if (query != null) {
                String[] queryParts = query.split("=");

                if (queryParts.length == 2 && "year".equals(queryParts[0])) {
                    try {
                        int year = Integer.parseInt(queryParts[1]);
                        List<Movie> movies = store.getByYear(year);
                        sendJson(ex, 200, gson.toJson(movies));
                        return;
                    } catch (NumberFormatException e) {
                        sendJson(ex, 400, gson.toJson(new ErrorResponse("Bad Request",
                                List.of("Некорректный параметр запроса"))));
                        return;
                    }
                }
            }

            List<Movie> movies = store.getAll();
            String jsonResponse = gson.toJson(movies);
            sendJson(ex, 200, jsonResponse);
            return;
        }

        if (pathParts.length == 3) {
            long id;

            try {
                id = Long.parseLong(pathParts[2]);
            } catch (NumberFormatException e) {
                sendJson(ex, 400, gson.toJson(new ErrorResponse("Bad Request",
                        List.of("Некорректный ID: " + pathParts[2]))));
                return;
            }

            Optional<Movie> movieOptional = store.getById(id);

            if (movieOptional.isPresent()) {
                sendJson(ex, 200, gson.toJson(movieOptional.get()));
            } else {
                sendJson(ex, 404, gson.toJson(new ErrorResponse("Not Found",
                        List.of("Фильм не найден, ID: " + id))));
            }
            return;
        }

        ex.sendResponseHeaders(404, -1);
    }

    private void handlePost(HttpExchange ex) throws IOException {
        String contentType = ex.getRequestHeaders().getFirst("Content-Type");

        if (contentType == null || !contentType.contains("application/json")) {
            sendJson(ex, 415, gson.toJson(new ErrorResponse("Unsupported Media Type",
                    List.of("Content-Type должен быть application/json"))));
            return;
        }

        String body = readRequestBody(ex);
        Movie movie = gson.fromJson(body, Movie.class);
        List<String> validationErrors = validate(movie);

        if (!validationErrors.isEmpty()) {
            sendJson(ex, 422, gson.toJson(new ErrorResponse("Ошибка валидации", validationErrors)));
            return;
        }

        store.add(movie);
        sendJson(ex, 201, gson.toJson(movie));
    }

    private void handleDelete(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length != 3) {
            ex.sendResponseHeaders(405, -1);
            return;
        }

        long id;

        try {
            id = Long.parseLong(pathParts[2]);
        } catch (NumberFormatException e) {
            sendJson(ex, 400, gson.toJson(new ErrorResponse("Bad Request", List.of("Некорректный ID"))));
            return;
        }

        if (store.delete(id)) {
            sendNoContent(ex);
        } else {
            sendJson(ex, 404, gson.toJson(new ErrorResponse("Not Found",
                    List.of("Фильм не найден, ID: " + id))));
        }
    }

    private List<String> validate(Movie movie) {
        List<String> errors = new ArrayList<>();

        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            errors.add("название не должно быть пустым");
        } else if (movie.getTitle().length() > 100) {
            errors.add("название не должно превышать 100 символов");
        }

        int currentYear = LocalDate.now().getYear();

        if (movie.getYear() < 1888 || movie.getYear() > currentYear + 1) {
            errors.add("год должен быть между 1888 и " + (currentYear + 1));
        }

        return errors;
    }
}
