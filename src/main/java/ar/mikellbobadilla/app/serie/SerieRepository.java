package ar.mikellbobadilla.app.serie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Long> {
    @Query("select s from Serie s where s.id = ?1")
    Optional<SeriePathsOnly> getLocationPaths(Long serieId);

    @Query("select s.posterPath from Serie s where s.id = ?1")
    Optional<String> getPosterPathById(Long serieId);

    @Query("select s.path from Serie s where s.id = ?1")
    Optional<String> getSeriePathById(Long serieId);

    @Query("update Serie s set s.posterPath = ?1 where s.id = ?2")
    @Modifying
    void setPosterPathById(String posterPath, Long serieId);
}
