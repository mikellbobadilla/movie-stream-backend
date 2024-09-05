package ar.mikellbobadilla.app.episode;

public record EpisodeResponse(
        Long id,
        String title,
        String description,
        Integer episodeNumber,
        String video
) {
}
