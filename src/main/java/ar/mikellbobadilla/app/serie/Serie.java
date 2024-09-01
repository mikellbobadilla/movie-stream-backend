package ar.mikellbobadilla.app.serie;

import ar.mikellbobadilla.app.genre.Genre;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity @Table(name = "series")

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @Builder
@ToString(exclude = "genres")
@EqualsAndHashCode(exclude = "genres")
public class Serie {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String description;
    @Column(name = "poster_path")
    private String posterPath;
    private String path;
    @Column(name = "create_at")
    private Date createAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "serie_genres",
            joinColumns = @JoinColumn(name = "serie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres;
}
