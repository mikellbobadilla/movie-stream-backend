package ar.mikellbobadilla.app.serie;

import ar.mikellbobadilla.app.exceptions.ResourceNotFoundException;
import ar.mikellbobadilla.app.genre.GenreRepository;
import ar.mikellbobadilla.app.utils.StorageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SerieService {
    private final GenreRepository genreRepository;
    private final SerieRepository serieRepository;

    @Value("${storage.root.path}")
    private String rootPath;
    @Value("${server.host.url}")
    private String hostUrl;

    public List<SerieResponse> getSeries() {
        return buildResponse(serieRepository.findAll(Sort.by("title").ascending()));
    }

    public SerieResponse getSerie(Long serieId) {
        return serieRepository.findById(serieId)
                .map(this::buildResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No such serie"));
    }

    @Transactional(rollbackFor = {})
    public SerieResponse createSerie(SerieRequest request) {
        var genres = genreRepository.findAllByNameIgnoreCaseIn(request.genres());
        Serie savedSerie = serieRepository.save(Serie.builder()
                .title(request.title())
                .description(request.description())
                .createAt(new Date())
                .genres(genres)
                .build()
        );
        /* /series/{id} */
        Path seriePath = Paths.get("series", String.valueOf(savedSerie.getId()));
        String posterPath = savePoster(seriePath, request.posterFile());
        savedSerie.setPath(seriePath.toString().replace("\\", "/"));
        savedSerie.setPosterPath(posterPath);
        return buildResponse(serieRepository.save(savedSerie));
    }

    private void removeResource(String posterPath) {
        Path locationDir = Paths.get(rootPath, posterPath);
        StorageUtils.deleteResource(locationDir);
    }

    private String savePoster(Path seriePath, MultipartFile file) {
        /* /{rootPath} */
        Path locationDir = Paths.get(rootPath);
        StorageUtils.createDir(locationDir.resolve(seriePath));
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        /* /series/{serieId}/{fileName} */
        Path posterPath = seriePath.resolve(fileName);
        StorageUtils.saveResource(locationDir.resolve(posterPath), file);
        return posterPath.toString().replace("\\", "/");
    }

    @Transactional(rollbackFor = {})
    public void updateSerie(Long serieId, SerieRequest request) {
        var genres = genreRepository.findAllByNameIgnoreCaseIn(request.genres());
        Serie serie = serieRepository.findById(serieId)
                .orElseThrow(() -> new ResourceNotFoundException("No such serie"));
        serie.setTitle(request.title());
        serie.setDescription(request.description());
        serie.setGenres(genres);
        removeResource(serie.getPosterPath());
        String posterPath = savePoster(Paths.get(serie.getPath()), request.posterFile());
        serie.setPosterPath(posterPath);
        serieRepository.save(serie);
    }

    @Transactional(rollbackFor = {})
    public void deleteSerie(Long serieId) {
        Serie serie = serieRepository.findById(serieId)
                .orElseThrow(() -> new ResourceNotFoundException("No such serie"));
        Path targetPath = Paths.get(rootPath, serie.getPath());
        StorageUtils.forceDeleteDir(targetPath.toFile());
        serieRepository.deleteById(serieId);
    }

    private List<SerieResponse> buildResponse(List<Serie> series) {
        return series.stream().map(this::buildResponse).toList();
    }

    private SerieResponse buildResponse(Serie serie) {
        String posterUrl = hostUrl + "/" + serie.getPosterPath();
        return new SerieResponse(
            serie.getId(),
            serie.getTitle(),
            serie.getDescription(),
            posterUrl,
            serie.getGenres()
        );
    }
}
