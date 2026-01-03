package org.example.dto;

import org.example.model.Remove;
import org.example.model.Replace;

import java.util.List;

public class ImdbFilmRdfsModel {
    @Replace("_")
    private String name;
    private Integer date;
    private Double rate;
    private Integer votes;
    private List<String> genre;
    private Integer duration;
    private String type;
    private String certificate;
    private String episodes;
    private String nudity;
    private String violence;
    private String profanity;
    private String alcohol;
    private String frightening;
}
