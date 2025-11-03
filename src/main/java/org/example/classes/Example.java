package org.example.classes;

import org.example.annotations.Generatable;

@Generatable
public class Example {
    int i;

    public Example(int i) {
        this.i = i;
    }

    @Override
    public String toString() {
        return "Example(" + i + ")";
    }
}
