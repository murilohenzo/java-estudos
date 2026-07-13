package br.com.murilo.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PEMDecoder;
import java.security.PEMEncoder;
import java.security.PublicKey;

/**
 * Demonstração de <b>PEM Encodings</b> — JEP 470 (Preview no Java 25).
 *
 * <p>Encode/decode de objetos criptográficos no formato PEM (RFC 7468),
 * conforme a API {@code PEMEncoder} / {@code PEMDecoder} da JEP.
 *
 * <pre>
 * javac --enable-preview --release 25 ...
 * java  --enable-preview -cp . br.com.murilo.crypto.PemEncodingDemo
 * </pre>
 */
public class PemEncodingDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== PEM Encodings — Java 25 / JEP 470 (Preview) ===\n");

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        KeyPair pair = kpg.generateKeyPair();

        PEMEncoder encoder = PEMEncoder.of();
        String publicPem = encoder.encodeToString(pair.getPublic());
        String privatePem = encoder.encodeToString(pair.getPrivate());

        System.out.println("--- Public key (PEM) ---");
        System.out.println(publicPem);

        System.out.println("--- Private key (PEM, primeiros 2 blocos) ---");
        privatePem.lines().limit(4).forEach(System.out::println);
        System.out.println("...");

        PublicKey decoded = PEMDecoder.of().decode(publicPem, PublicKey.class);
        System.out.println("\ndecode ok? algorithm=" + decoded.getAlgorithm()
                + " equals=" + decoded.equals(pair.getPublic()));

        System.out.println("\nCaso de uso: interoperar com OpenSSL/CAs sem parsing manual de PEM.");
    }
}
