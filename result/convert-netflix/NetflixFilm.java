import org.example.model.csv.Column;

import java.util.List;

public class NetflixFilm {
    private String title;
    private String genre;
    private String premiere;
    private String runtime;
    @Column(name = "imdb score")
    private String imdbScore;
    private String language;
}
