package ar.mikellbobadilla.app.season;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
    @Query("select s.path from Season s where s.id = ?1")
    Optional<String> getSeasonPathById(Long seasonId);
    List<Season> findAllBySerieId(Long serieId, Sort sort);
    Optional<Season> findBySerieIdAndSeasonNumber(Long serieId, Integer seasonNumber);
}
