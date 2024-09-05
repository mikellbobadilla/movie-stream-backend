package ar.mikellbobadilla.app.season;

import ar.mikellbobadilla.app.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/seasons")
@RequiredArgsConstructor
public class SeasonController {
    private final SeasonService service;

    @GetMapping("/serie/{serieId}")
    @ResponseStatus(OK)
    List<SeasonSummaryResponse> getSeasonsBySerieId(@PathVariable Long serieId) {
        return service.getSeasonsBySerieId(serieId);
    }

    @GetMapping("/{seasonId}")
    @ResponseStatus(OK)
    SeasonDetailResponse getSeason(@PathVariable Long seasonId) {
        return service.getSeason(seasonId);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    SeasonSummaryResponse postSeason(@RequestBody SeasonRequest request) {
        return service.createSeason(request);
    }

    @PutMapping("/{seasonId}")
    ResponseEntity<?> putSeason(@PathVariable Long seasonId, @RequestBody SeasonRequest request) {
        try {
            service.updateSeason(seasonId, request);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException ignore) {
            var response = service.createSeason(request);
            return new ResponseEntity<>(response, CREATED);
        }
    }

    @DeleteMapping("/{seasonId}")
    @ResponseStatus(NO_CONTENT)
    void deleteSeason(@PathVariable Long seasonId) {
        service.deleteSeason(seasonId);
    }
}
