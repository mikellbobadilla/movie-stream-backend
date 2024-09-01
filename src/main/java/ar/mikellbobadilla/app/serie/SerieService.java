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

    public Resource getPosterFile(Long serieId) {
        String posterPath = serieRepository.getPosterPathById(serieId)
                .orElseThrow(() -> new ResourceNotFoundException("No such serie"));
        /* /{rootPath}/series/poster.jpg */
        Path location = Paths.get(rootPath, posterPath);
        try {
            return new UrlResource(location.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
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

    @Transactional(rollbackFor = {})
    public void updateSerie(Long serieId, SerieRequest request) {
        Serie serie = serieRepository.findById(serieId)
                .orElseThrow(() -> new ResourceNotFoundException("No such serie"));
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
                .orElseThrow(() -> new ResourceNotFoundException("No such serie"));
        Path targetPath = Paths.get(rootPath, seriePath);
        serieRepository.deleteById(serieId);
        StorageUtils.forceDeleteDir(targetPath.toFile());
    }

    @Transactional(rollbackFor = {})
    public void updateSeriePoster(Long serieId, MultipartFile posterFile) {
        var seriePaths = serieRepository.getLocationPaths(serieId)
                .orElseThrow(() -> new ResourceNotFoundException("No such serie"));
        String oldPosterPath = seriePaths.getPosterPath();
        String newPosterPath = savePoster(Paths.get(seriePaths.getPath()), posterFile);
        serieRepository.setPosterPathById(newPosterPath, serieId);
        removeResource(oldPosterPath);
    }

    private List<SerieResponse> buildResponse(List<Serie> series) {
        return series.stream().map(this::buildResponse).toList();
    }

    private SerieResponse buildResponse(Serie serie) {
        String posterUrl = hostUrl + "/" + serie.getPath() + "/poster";
        return new SerieResponse(
            serie.getId(),
            serie.getTitle(),
            serie.getDescription(),
            posterUrl,
            serie.getGenres()
        );
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

}
