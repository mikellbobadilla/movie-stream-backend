package ar.mikellbobadilla.app.episode;

public record EpisodeDetailResponse(
        Long id,
        String title,
        String description,
        Integer episodeNumber,
        String video
) {
}
