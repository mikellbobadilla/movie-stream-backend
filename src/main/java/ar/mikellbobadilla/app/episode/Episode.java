package ar.mikellbobadilla.app.episode;

import ar.mikellbobadilla.app.season.Season;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "episodes")

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @Builder
@ToString(exclude = "season")
@EqualsAndHashCode(exclude = "season")
public class Episode {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String description;
    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;
    @Column(name = "file_path")
    private String filePath;
    @ManyToOne
    @JoinColumn(name = "season_id")
    private Season season;
    @Column(name = "create_at")
    private Date createAt;
}
