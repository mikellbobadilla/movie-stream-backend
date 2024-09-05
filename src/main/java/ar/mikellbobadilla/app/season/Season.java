package ar.mikellbobadilla.app.season;

import ar.mikellbobadilla.app.serie.Serie;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "seasons")

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @Builder
@ToString(exclude = "serie")
@EqualsAndHashCode(exclude = "serie")
public class Season {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String title;
    private String description;
    @Column(nullable = false, name = "season_number")
    private Integer seasonNumber;
    private String path;
    @ManyToOne
    @JoinColumn(name = "serie_id")
    private Serie serie;
    @Column(name = "create_at")
    private Date createAt;
}
