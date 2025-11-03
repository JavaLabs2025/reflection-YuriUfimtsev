package org.example.classes;

import org.example.annotations.Generatable;

@Generatable
public class DeepClass {
    private DeeperClass deeper;

    public DeeperClass getDeeper() { return deeper; }
    public void setDeeper(DeeperClass deeper) { this.deeper = deeper; }
}