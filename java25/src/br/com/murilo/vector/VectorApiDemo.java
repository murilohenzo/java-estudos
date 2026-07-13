package br.com.murilo.vector;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

/**
 * Demonstração da <b>Vector API</b> — JEP 508 (10º Incubator no Java 25).
 *
 * <pre>
 * javac --add-modules jdk.incubator.vector ...
 * java  --add-modules jdk.incubator.vector -cp . br.com.murilo.vector.VectorApiDemo
 * </pre>
 */
public class VectorApiDemo {

    static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    public static void main(String[] args) {
        System.out.println("=== Vector API — Java 25 / JEP 508 (Incubator) ===\n");
        System.out.println("preferred species length = " + SPECIES.length());

        float[] left = {1, 2, 3, 4, 5, 6, 7, 8};
        float[] right = {10, 20, 30, 40, 50, 60, 70, 80};
        float[] result = new float[left.length];

        int i = 0;
        for (; i < SPECIES.loopBound(left.length); i += SPECIES.length()) {
            FloatVector a = FloatVector.fromArray(SPECIES, left, i);
            FloatVector b = FloatVector.fromArray(SPECIES, right, i);
            a.add(b).intoArray(result, i);
        }
        for (; i < left.length; i++) {
            result[i] = left[i] + right[i];
        }

        System.out.print("result = [");
        for (int j = 0; j < result.length; j++) {
            if (j > 0) System.out.print(", ");
            System.out.print(result[j]);
        }
        System.out.println("]");
        System.out.println("\nCaso de uso: hot loops numéricos com SIMD (ML, codecs, image).");
    }
}
