package ar.mikellbobadilla.app.serie;

import ar.mikellbobadilla.app.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SerieController {
    private final SerieService service;

    @GetMapping
    @ResponseStatus(OK)
    List<SerieResponse> getSeries() {
        return service.getSeries();
    }

    @GetMapping("/{serieId}")
    @ResponseStatus(OK)
    SerieResponse getSerie(@PathVariable Long serieId) {
        return service.getSerie(serieId);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    SerieResponse postSerie(@ModelAttribute SerieRequest request) {
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

    @DeleteMapping("/{serieId}")
    @ResponseStatus(NO_CONTENT)
    void deleteSerie(@PathVariable Long serieId) {
        service.deleteSerie(serieId);
    }
}
