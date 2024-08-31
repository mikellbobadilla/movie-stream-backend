package ar.mikellbobadilla.app.genre;

import ar.mikellbobadilla.app.exceptions.ResourceException;
import ar.mikellbobadilla.app.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository repository;

    public List<Genre> getGenres() {
        return repository.findAll(Sort.by("name").ascending());
    }

    public Genre getGenre(Long genreId) {
        return repository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("No such genre"));
    }

    public Genre createGenre(GenreRequest request) {
        if (repository.existsByNameIgnoreCase(request.name()))
            throw new ResourceException("Genre name exists, try again");

        Genre newGenre = Genre.builder().name(request.name()).build();
        return repository.save(newGenre);
    }

    public void updateGenre(Long genreId, GenreRequest request) {
        Genre genre = repository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("No such genre"));
        if (repository.existsByNameIgnoreCaseAndIdNot(request.name(), genreId))
            throw new ResourceException("Genre name exists, try again");
        genre.setName(request.name());
        repository.save(genre);
    }

    @Transactional
    public void deleteGenre(Long genreId) {
        repository.deleteById(genreId);
    }
}
