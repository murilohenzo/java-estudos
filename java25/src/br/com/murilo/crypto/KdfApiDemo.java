package br.com.murilo.crypto;

import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.HKDFParameterSpec;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HexFormat;

/**
 * Demonstração da <b>Key Derivation Function API</b> — JEP 510 (GA no Java 25).
 *
 * <p>Baseado no exemplo oficial da JEP: HKDF-SHA256 extrai/expande material
 * inicial em uma chave AES de 32 bytes.
 */
public class KdfApiDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== KDF API (HKDF) — Java 25 / JEP 510 ===\n");

        byte[] ikm = new byte[32];
        byte[] salt = new byte[16];
        byte[] info = "java25-session-key".getBytes();
        new SecureRandom().nextBytes(ikm);
        new SecureRandom().nextBytes(salt);

        KDF hkdf = KDF.getInstance("HKDF-SHA256");

        AlgorithmParameterSpec params = HKDFParameterSpec.ofExtract()
                .addIKM(ikm)
                .addSalt(salt)
                .thenExpand(info, 32);

        SecretKey aesKey = hkdf.deriveKey("AES", params);

        System.out.println("algorithm = " + aesKey.getAlgorithm());
        System.out.println("format    = " + aesKey.getFormat());
        System.out.println("key hex   = " + HexFormat.of().formatHex(aesKey.getEncoded()));
        System.out.println("\nCaso de uso: derivar chave de sessão a partir de IKM+salt compartilhados.");
    }
}
