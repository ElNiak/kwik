package net.luminis.quic.proxy.io;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SecretExtractor {

    public static Map<String, String> extractSecrets(String filePath) throws IOException {
        Map<String, String> secrets = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    String secretType = parts[0];
                    String secretValue = parts[2];
                    System.out.println("Secret type: " + secretType + ", secret value: " + secretValue);
                    secrets.put(secretType, secretValue);
                }
            }
        }
        return secrets;
    }
}
