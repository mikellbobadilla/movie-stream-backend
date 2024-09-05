package ar.mikellbobadilla.app.episode;

import org.springframework.web.multipart.MultipartFile;

public record EpisodeRequest(
        String title,
        String description,
        Integer episodeNumber,
        MultipartFile episodeFile,
        Long serieId,
        Integer seasonNumber
) {
}
