package ru.func.museum.element;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Element {
    private int parentId;
    private int id;
    private boolean locked;
    private double increase;
}
