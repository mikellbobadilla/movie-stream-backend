package ar.mikellbobadilla.app.serie;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record SerieRequest(
        @NotBlank(message = "El título de la serie no puede estar vacío")
        String title,
        String description,
        @NotNull(message = "El poster no puede estar vacío")
        MultipartFile posterFile,
        List<String> genres
) {
}
