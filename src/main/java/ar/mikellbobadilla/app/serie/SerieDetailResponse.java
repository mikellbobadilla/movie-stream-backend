package ar.mikellbobadilla.app.serie;

import ar.mikellbobadilla.app.genre.Genre;

import java.util.Set;

public record SerieDetailResponse(
        Long id,
        String title,
        String description,
        String poster,
        Set<Genre> genres
) {
}
