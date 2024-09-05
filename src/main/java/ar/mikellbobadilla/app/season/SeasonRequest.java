package ar.mikellbobadilla.app.season;

public record SeasonRequest(
        String title,
        String description,
        Integer seasonNumber,
        Long serieId
) {
}
