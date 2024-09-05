package ar.mikellbobadilla.app.season;

import ar.mikellbobadilla.app.episode.Episode;
import ar.mikellbobadilla.app.episode.EpisodeDetailResponse;
import ar.mikellbobadilla.app.episode.EpisodeRepository;
import ar.mikellbobadilla.app.exceptions.ResourceNotFoundException;
import ar.mikellbobadilla.app.serie.Serie;
import ar.mikellbobadilla.app.serie.SerieRepository;
import ar.mikellbobadilla.app.utils.StorageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeasonService {
    private final SeasonRepository seasonRepository;
    private final SerieRepository serieRepository;
    private final EpisodeRepository episodeRepository;

    @Value("${storage.root.path}")
    private String rootPath;
    @Value("${server.host.url}")
    private String hostUrl;

    public List<SeasonSummaryResponse> getSeasonsBySerieId(Long serieId) {
        Sort sort = Sort.by("seasonNumber").ascending();
        return buildSummaryResponse(seasonRepository.findAllBySerieId(serieId, sort));
    }

    public SeasonDetailResponse getSeason(Long seasonId) {
        return seasonRepository.findById(seasonId)
                .map(this::buildResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No such season"));
    }

    @Transactional(rollbackFor = {})
    public SeasonSummaryResponse createSeason(SeasonRequest request) {
        Serie serie = serieRepository.findById(request.serieId())
                .orElseThrow(() -> new ResourceNotFoundException("No such serie, cannot create season"));
        Season seasonSaved = seasonRepository.save(Season.builder()
                .title(request.title())
                .description(request.description())
                .seasonNumber(request.seasonNumber())
                .serie(serie)
                .createAt(new Date())
                .build()
        );
        String template = "season_%s";
        Path seasonPath = Paths.get("seasons", String.format(template, seasonSaved.getId()));
        StorageUtils.createDir(Paths.get(rootPath).resolve(seasonPath));
        seasonSaved.setPath(seasonPath.toString().replace("\\", "/"));
        return buildSummaryResponse(seasonRepository.save(seasonSaved));
    }

    public void updateSeason(Long seasonId, SeasonRequest request) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("No such season"));
        Serie serie = serieRepository.findById(request.serieId())
                .orElseThrow(() -> new ResourceNotFoundException("No such serie, cannot create season"));
        season.setTitle(request.title());
        season.setDescription(request.description());
        season.setSerie(serie);
        season.setSeasonNumber(request.seasonNumber());
        seasonRepository.save(season);
    }

    public void deleteSeason(Long seasonId) {
        String seasonPath = seasonRepository.getSeasonPathById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("No such season"));
        seasonRepository.deleteById(seasonId);
        Path target = Paths.get(rootPath, seasonPath);
        StorageUtils.forceDeleteDir(target.toFile());
    }

    /* ################################ Builders ################################ */
    private SeasonSummaryResponse buildSummaryResponse(Season season) {
        return new SeasonSummaryResponse(
                season.getId(),
                season.getTitle(),
                season.getSeasonNumber()
        );
    }

    private List<SeasonSummaryResponse> buildSummaryResponse(List<Season> seasons) {
        return seasons.stream().map(this::buildSummaryResponse).toList();
    }

    private SeasonDetailResponse buildResponse(Season season) {
        Integer episodeCount = episodeRepository.countBySeasonId(season.getId());
        return new SeasonDetailResponse(
            season.getId(),
            season.getTitle(),
            season.getDescription(),
            season.getSeasonNumber(),
            episodeCount
        );
    }
}
