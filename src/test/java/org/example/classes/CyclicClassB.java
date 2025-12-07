package org.example.classes;

import org.example.annotations.Generatable;

@Generatable
public class CyclicClassB {
    private CyclicClassA a;

    public CyclicClassA getA() { return a; }
    public void setA(CyclicClassA a) { this.a = a; }
}
