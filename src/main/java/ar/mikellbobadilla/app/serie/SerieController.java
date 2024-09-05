package ar.mikellbobadilla.app.serie;

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

@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SerieController {
    private final SerieService service;

    @GetMapping
    @ResponseStatus(OK)
    List<SerieSummaryResponse> getAll() {
        return service.getSeries();
    }

    @GetMapping("/{serieId}")
    @ResponseStatus(OK)
    SerieDetailResponse getSerie(@PathVariable Long serieId) {
        return service.getSerie(serieId);
    }

    @GetMapping("/{serieId}/poster")
    ResponseEntity<Resource> getPosterFile(@PathVariable Long serieId) {
        Resource posterFile = service.getPosterFile(serieId);
        var mediaType = MediaTypeFactory.getMediaType(posterFile).orElse(MediaType.IMAGE_PNG);

        if (posterFile.exists() && posterFile.isReadable())
            return ResponseEntity.ok().contentType(mediaType).body(posterFile);
        else
            return ResponseEntity.notFound().build();
    }

    @PostMapping
    @ResponseStatus(CREATED)
    SerieDetailResponse postSerie(@ModelAttribute SerieRequest request) {
        return service.createSerie(request);
    }

    @PutMapping("/{serieId}")
    ResponseEntity<?> putSerie(@PathVariable Long serieId, @ModelAttribute SerieRequest request) {
        try {
            service.updateSerie(serieId, request);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException ignore) {
            var response = service.createSerie(request);
            return new ResponseEntity<>(response, CREATED);
        }
    }

    @PatchMapping("/{serieId}/poster")
    @ResponseStatus(NO_CONTENT)
    void patchSeriePoster(@PathVariable Long serieId, @ModelAttribute MultipartFile posterFile) {
        service.updateSeriePoster(serieId, posterFile);
    }

    @DeleteMapping("/{serieId}")
    @ResponseStatus(NO_CONTENT)
    void deleteSerie(@PathVariable Long serieId) {
        service.deleteSerie(serieId);
    }
}
