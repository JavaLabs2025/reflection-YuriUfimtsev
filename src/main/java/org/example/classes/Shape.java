package org.example.classes;

import org.example.annotations.Generatable;

@Generatable
public interface Shape {
    double getArea();
    double getPerimeter();
}