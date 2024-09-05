package ar.mikellbobadilla.app.episode;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    List<Episode> findAllBySeasonId(Long seasonId, Sort sort);
    Integer countBySeasonId(Long seasonId);
    @Query("select e.filePath from Episode e where e.id = ?1")
    Optional<String> getFilePathById(Long episodeId);

    @Query("update Episode e set e.filePath = ?1 where e.id = ?2")
    @Modifying
    void setFilePathById(String filePath, Long episodeId);
}
