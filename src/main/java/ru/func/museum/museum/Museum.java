package ru.func.museum.museum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.func.museum.museum.collector.CollectorType;
import ru.func.museum.museum.space.Space;
import ru.func.museum.museum.template.MuseumTemplateType;

import java.util.List;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Museum implements AbstractMuseum {
    private List<Space> matrix;
    private String title;
    private MuseumTemplateType museumTemplateType;
    private CollectorType collectorType;
}
