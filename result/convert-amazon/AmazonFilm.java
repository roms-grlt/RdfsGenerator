import org.example.model.csv.Column;

import java.util.List;

public class AmazonFilm {
    private Integer id;
    private String title;
    @Column(name = "movie_rating")
    private Double rating;
    @Column(name = "no_of_ratings")
    private Integer numberOfRating;
    private String format;
    @Column(name = "releaseyear")
    private String releaseYear;
    @Column(name = "mpaa_rating")
    private String mpaaRating;
    @Column(name = "directed_by")
    private List<String> directedBy;
    @Column(name = "starring")
    private List<String> starring;
    @Column(name = "price")
    private Double price;
}
