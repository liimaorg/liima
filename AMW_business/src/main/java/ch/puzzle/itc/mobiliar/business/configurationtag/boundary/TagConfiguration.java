package ch.puzzle.itc.mobiliar.business.configurationtag.boundary;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@AllArgsConstructor
@Getter
public class TagConfiguration {
    private int resourceId;
    private String tag;
    private Date date;
}