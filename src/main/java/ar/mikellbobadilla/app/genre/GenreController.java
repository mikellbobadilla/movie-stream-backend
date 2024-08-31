package ar.mikellbobadilla.app.genre;

import ar.mikellbobadilla.app.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService service;

    @GetMapping
    @ResponseStatus(OK)
    List<Genre> getGenres() {
        return service.getGenres();
    }

    @GetMapping("/{genreId}")
    @ResponseStatus(OK)
    Genre getGenre(@PathVariable Long genreId) {
        return service.getGenre(genreId);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    Genre postGenre(@RequestBody GenreRequest request) {
        return service.createGenre(request);
    }

    @PutMapping("/{genreId}")
    ResponseEntity<?> putGenre(@PathVariable Long genreId, @RequestBody GenreRequest request) {
        try {
            service.updateGenre(genreId, request);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException ignore) {
            var response = service.createGenre(request);
            return new ResponseEntity<>(response, CREATED);
        }
    }

    @DeleteMapping("/{genreId}")
    @ResponseStatus(NO_CONTENT)
    void deleteGenre(@PathVariable Long genreId) {
        service.deleteGenre(genreId);
    }
}
