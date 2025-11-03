package org.example.classes;

import org.example.annotations.Generatable;

@Generatable
public class CyclicClassA {
    private CyclicClassB b;

    public CyclicClassB getB() { return b; }
    public void setB(CyclicClassB b) { this.b = b; }
}
