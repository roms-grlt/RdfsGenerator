import org.example.model.csv.Column;

import java.util.List;

public class NetflixFilm {
    private String title;
    private String genre;
    private String premiere;
    @Column(name="runtime")
    private Integer duration;
    @Column(name = "imdb score")
    private Double rating;
    private String language;
}
