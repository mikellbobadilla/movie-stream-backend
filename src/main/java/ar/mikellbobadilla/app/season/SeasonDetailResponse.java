package ar.mikellbobadilla.app.season;

public record SeasonDetailResponse(
    Long id,
    String title,
    String description,
    Integer seasonNumber,
    Integer episodesNumber
) {
}
