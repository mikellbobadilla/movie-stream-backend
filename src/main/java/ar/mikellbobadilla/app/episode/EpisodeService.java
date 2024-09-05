package ar.mikellbobadilla.app.episode;

import ar.mikellbobadilla.app.exceptions.ResourceNotFoundException;
import ar.mikellbobadilla.app.season.Season;
import ar.mikellbobadilla.app.season.SeasonRepository;
import ar.mikellbobadilla.app.utils.StorageUtils;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Stream;
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
public class EpisodeService {
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository repository;

    @Value("${storage.root.path}")
    private String rootPath;
    @Value("${server.host.url}")
    private String hostUrl;

    public List<Episode> findAllBySeasonId(Long seasonId) {
        return repository.findAllBySeasonId(seasonId, Sort.by("episodeNumber").ascending());
    }

    public EpisodeResponse findEpisode(Long episodeId) {
        return repository.findById(episodeId)
                .map(this::buildResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No such episode"));
    }

    public Resource getVideoFile(Long episodeId) {
        String posterPath = repository.getFilePathById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("No such episode"));
        /* /{rootPath}/seasons/season_{seasonId}/video.mkv */
        Path location = Paths.get(rootPath, posterPath);

        try {
            return new UrlResource(location.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(rollbackFor = {})
    public EpisodeResponse createEpisode(EpisodeRequest request) {
        Season season = seasonRepository.findBySerieIdAndSeasonNumber(request.serieId(), request.seasonNumber())
                .orElseThrow(() -> new ResourceNotFoundException("No such season, try again"));

        Episode episode = repository.save(Episode.builder()
                        .title(request.title())
                        .description(request.description())
                        .episodeNumber(request.episodeNumber())
                        .season(season)
                        .createAt(new Date())
                        .build()
        );
        String filePath = saveVideoFile(Paths.get(season.getPath()), request.episodeFile());
        episode.setFilePath(filePath);
        return buildResponse(repository.save(episode));
    }

    @Transactional(rollbackFor = {})
    public void updateEpisode(Long episodeId, EpisodeRequest request) {
        Episode episode = repository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("No such episode"));
        Season season = seasonRepository.findBySerieIdAndSeasonNumber(request.serieId(), request.seasonNumber())
                .orElseThrow(() -> new ResourceNotFoundException("No such season, try again"));
        episode.setTitle(request.title());
        episode.setDescription(request.description());
        episode.setEpisodeNumber(request.episodeNumber());
        episode.setSeason(season);
        String oldFilePath = episode.getFilePath();
        String newFilePath = saveVideoFile(Paths.get(season.getPath()), request.episodeFile());
        episode.setFilePath(newFilePath);
        repository.save(episode);
        removeResource(oldFilePath);
    }

    @Transactional(rollbackFor = {})
    public void updateEpisodeFile(Long episodeId, MultipartFile file) {
        Episode episode = repository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("No such episode"));
        String oldFilePath = episode.getFilePath();
        String seasonDir = episode.getSeason().getPath();
        String newFilePath = saveVideoFile(Paths.get(seasonDir), file);
        repository.setFilePathById(newFilePath, episodeId);
        removeResource(oldFilePath);
    }

    @Transactional(rollbackFor = {})
    public void deleteEpisode(Long episodeId) {
        String filePath = repository.getFilePathById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("No such episode"));
        repository.deleteById(episodeId);
        removeResource(filePath);
    }

    /* ################################ Private Methods ################################ */
    private String saveVideoFile(Path seasonDir, MultipartFile file) {
        Path locationDir = Paths.get(rootPath);
        StorageUtils.createDir(locationDir.resolve(seasonDir));
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        /* /seasons/season_{seasonId}/{filename} */
        Path filePath = seasonDir.resolve(filename);
        StorageUtils.saveResource(locationDir.resolve(filePath), file);
        return filePath.toString().replace("\\", "/");
    }

    private void removeResource(String filePath) {
        Path targetFile = Paths.get(rootPath, filePath);
        StorageUtils.deleteResource(targetFile);
    }

    /* ################################ Builders ################################ */
    public EpisodeResponse buildResponse(Episode episode) {
        /* http://localhost:8080/api/episodes/{episodeId}/stream */
        String videoUrl = hostUrl + "/episodes/" + episode.getId() + "/stream";
        return new EpisodeResponse(
                episode.getId(),
                episode.getTitle(),
                episode.getDescription(),
                episode.getEpisodeNumber(),
                videoUrl
        );
    }

    public List<EpisodeResponse> buildResponse(List<Episode> episodes) {
        return episodes.stream().map(this::buildResponse).toList();
    }
}
