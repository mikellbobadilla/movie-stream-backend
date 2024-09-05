package ar.mikellbobadilla.app.episode;

import ar.mikellbobadilla.app.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/episodes")
@RequiredArgsConstructor
public class EpisodeController {
    private final EpisodeService service;

    @GetMapping("/season/{seasonId}")
    @ResponseStatus(OK)
    List<Episode> getAllBySeasonId(@PathVariable Long seasonId) {
        return service.findAllBySeasonId(seasonId);
    }

    @GetMapping("/{episodeId}")
    @ResponseStatus(OK)
    EpisodeResponse getEpisode(@PathVariable Long episodeId) {
        return service.findEpisode(episodeId);
    }

    @GetMapping("/{episodeId}/stream")
    ResponseEntity<Resource> getVideoFile(@PathVariable Long episodeId) {
        Resource videoFile = service.getVideoFile(episodeId);
        var mediaType = MediaTypeFactory.getMediaType(videoFile).orElse(MediaType.APPLICATION_OCTET_STREAM);
        if (videoFile.exists() && videoFile.isReadable())
            return ResponseEntity.ok().contentType(mediaType).body(videoFile);
        else
            return ResponseEntity.notFound().build();
    }

    @PostMapping
    @ResponseStatus(CREATED)
    EpisodeResponse postEpisode(@ModelAttribute EpisodeRequest request) {
        return service.createEpisode(request);
    }

    @PutMapping("/{episodeId}")
    ResponseEntity<?> putEpisode(@PathVariable Long episodeId, @ModelAttribute EpisodeRequest request) {
        try {
            service.updateEpisode(episodeId, request);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException ignore) {
            var response = service.createEpisode(request);
            return new ResponseEntity<>(response, CREATED);
        }
    }

    @PatchMapping("/{episodeId}/episode")
    @ResponseStatus(NO_CONTENT)
    void updateVideoFile(@PathVariable Long episodeId, @ModelAttribute MultipartFile videoFile) {
        service.updateEpisodeFile(episodeId, videoFile);
    }

    @DeleteMapping("/{episodeId}")
    @ResponseStatus(NO_CONTENT)
    void deleteEpisode(@PathVariable Long episodeId) {
        service.deleteEpisode(episodeId);
    }
}
