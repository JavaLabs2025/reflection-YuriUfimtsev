package org.example.classes;

import org.example.annotations.Generatable;

import java.util.List;

@Generatable
public class TestClass {
    private String name;
    private int value;
    private List<String> items;

    public TestClass() {}

    public TestClass(String name, int value) {
        this.name = name;
        this.value = value;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
}
