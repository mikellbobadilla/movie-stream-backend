package ar.mikellbobadilla.app.genre;

import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity @Table(name = "genres")

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode
@Builder
public class Genre {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
}
