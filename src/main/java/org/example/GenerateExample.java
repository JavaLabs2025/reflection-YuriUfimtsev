package org.example;


import org.example.classes.*;
import org.example.generator.Generator;

public class GenerateExample {
    public static void main(String[] args) {
        var gen = new Generator();

        try {
            Object generated = gen.generateValueOfType(BinaryTreeNode.class);
            System.out.println(generated);

            generated = gen.generateValueOfType(Cart.class);
            System.out.println(generated);

            generated = gen.generateValueOfType(Example.class);
            System.out.println(generated);

            generated = gen.generateValueOfType(Product.class);
            System.out.println(generated);

            generated = gen.generateValueOfType(Rectangle.class);
            System.out.println(generated);

            generated = gen.generateValueOfType(Shape.class);
            System.out.println(generated);

            generated = gen.generateValueOfType(Triangle.class);
            System.out.println(generated);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}