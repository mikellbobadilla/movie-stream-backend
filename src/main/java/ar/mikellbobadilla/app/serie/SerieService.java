package ar.mikellbobadilla.app.serie;

import ar.mikellbobadilla.app.exceptions.ResourceNotFoundException;
import ar.mikellbobadilla.app.genre.GenreRepository;
import ar.mikellbobadilla.app.utils.StorageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SerieService {
    public static final String NOT_FOUND = "No such serie";
    private final GenreRepository genreRepository;
    private final SerieRepository serieRepository;

    @Value("${storage.root.path}")
    private String rootPath;
    @Value("${server.host.url}")
    private String hostUrl;

    public List<SerieSummaryResponse> getSeries() {
        Sort sort = Sort.by("title").ascending();
        return buildSummaryResponse(serieRepository.findAll(sort));
    }

    public SerieDetailResponse getSerie(Long serieId) {
        return serieRepository.findById(serieId)
                .map(this::buildResponse)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
    }

    public Resource getPosterFile(Long serieId) {
        String posterPath = serieRepository.getPosterPathById(serieId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
        /* /{rootPath}/series/poster.jpg */
        Path location = Paths.get(rootPath, posterPath);
        try {
            return new UrlResource(location.toUri());
        } catch (MalformedURLException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Transactional(rollbackFor = {})
    public SerieDetailResponse createSerie(SerieRequest request) {
        var genres = genreRepository.findAllByNameIgnoreCaseIn(request.genres());
        Serie savedSerie = serieRepository.save(Serie.builder()
                .title(request.title())
                .description(request.description())
                .createAt(new Date())
                .genres(genres)
                .build()
        );
        /* /series/{id} */
        Path seriePath = Paths.get("series", savedSerie.getId().toString());
        String posterPath = savePoster(seriePath, request.posterFile());
        savedSerie.setPath(seriePath.toString().replace("\\", "/"));
        savedSerie.setPosterPath(posterPath);
        return buildResponse(serieRepository.save(savedSerie));
    }

    @Transactional(rollbackFor = {})
    public void updateSerie(Long serieId, SerieRequest request) {
        Serie serie = serieRepository.findById(serieId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
        var genres = genreRepository.findAllByNameIgnoreCaseIn(request.genres());
        serie.setTitle(request.title());
        serie.setDescription(request.description());
        serie.setGenres(genres);
        String oldPosterPath = serie.getPosterPath();
        String newPosterPath = savePoster(Paths.get(serie.getPath()), request.posterFile());
        serie.setPosterPath(newPosterPath);
        serieRepository.save(serie);
        removeResource(oldPosterPath);
    }

    @Transactional(rollbackFor = {})
    public void deleteSerie(Long serieId) {
        String seriePath = serieRepository.getSeriePathById(serieId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
        Path targetPath = Paths.get(rootPath, seriePath);
        serieRepository.deleteById(serieId);
        StorageUtils.forceDeleteDir(targetPath.toFile());
    }

    @Transactional(rollbackFor = {})
    public void updateSeriePoster(Long serieId, MultipartFile posterFile) {
        var seriePaths = serieRepository.getLocationPaths(serieId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
        String oldPosterPath = seriePaths.getPosterPath();
        String newPosterPath = savePoster(Paths.get(seriePaths.getPath()), posterFile);
        serieRepository.setPosterPathById(newPosterPath, serieId);
        removeResource(oldPosterPath);
    }

    /* ################################ Private Methods ################################ */
    private void removeResource(String posterPath) {
        Path locationDir = Paths.get(rootPath, posterPath);
        StorageUtils.deleteResource(locationDir);
    }

    private String savePoster(Path seriePath, MultipartFile file) {
        /* /{rootPath} */
        Path locationDir = Paths.get(rootPath);
        StorageUtils.createDir(locationDir.resolve(seriePath));
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        /* /series/{serieId}/{fileName} */
        Path posterPath = seriePath.resolve(filename);
        StorageUtils.saveResource(locationDir.resolve(posterPath), file);
        return posterPath.toString().replace("\\", "/");
    }

    /* ################################ Builders ################################ */
    private List<SerieSummaryResponse> buildSummaryResponse(List<Serie> series) {
        return series.stream().map(this::buildSummaryResponse).toList();
    }

    private SerieSummaryResponse buildSummaryResponse(Serie serie) {
        /* /{hostUrl}/series/{serieId}/poster */
        String posterUrl = hostUrl + "/" + serie.getPath() + "/poster";
        return new SerieSummaryResponse(serie.getId(), serie.getTitle(), posterUrl);
    }

    private List<SerieDetailResponse> buildResponse(List<Serie> series) {
        return series.stream().map(this::buildResponse).toList();
    }

    private SerieDetailResponse buildResponse(Serie serie) {
        /* /{hostUrl}/series/{serieId}/poster */
        String posterUrl = hostUrl + "/" + serie.getPath() + "/poster";
        return new SerieDetailResponse(
                serie.getId(), serie.getTitle(), serie.getDescription(),
                posterUrl, serie.getGenres()
        );
    }
}
