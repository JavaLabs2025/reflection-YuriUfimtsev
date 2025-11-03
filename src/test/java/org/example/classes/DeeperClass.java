package org.example.classes;

import org.example.annotations.Generatable;

@Generatable
public class DeeperClass {
    private String value;

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
