import org.example.model.csv.Ignore;
import org.example.model.csv.Remove;
import org.example.model.rdfs.Replace;

import java.util.List;

public class ImdbFilm {

    @Ignore("No Rate")
    @Replace("_")
    private String name;
    @Ignore("No Rate")
    private Integer date;
    @Ignore("No Rate")
    private Double rate;
    @Remove(regex = ",")
    @Ignore("No Votes")
    private Integer votes;
    @Ignore("No Rate")
    private List<String> genre;
    @Ignore("None")
    private Integer duration;
    @Ignore("No Rate")
    private String type;
    @Ignore("No Rate")
    private String certificate;
    @Ignore("No Rate,-")
    private Integer episodes;
    @Ignore("No Rate")
    private String nudity;
    @Ignore("No Rate")
    private String violence;
    @Ignore("No Rate")
    private String profanity;
    @Ignore("No Rate")
    private String alcohol;
    @Ignore("No Rate")
    private String frightening;
}
