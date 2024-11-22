import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class DHGen {  // DestinationHashGenerator -> DHGen
    private static String dVal = null;  // destinationValue -> dVal

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DHGen.jar <rollNo> <jsonPath>");
            System.exit(1);
        }

        String rNo = args[0];    // rollNumber -> rNo
        String jPath = args[1];  // jsonFilePath -> jPath

        try {
            ObjectMapper m = new ObjectMapper();  // mapper -> m
            JsonNode root = m.readTree(new File(jPath));

            scanJson(root);  // traverseJson -> scanJson

            if (dVal == null) {
                System.out.println("No 'destination' key found.");
                System.exit(1);
            }

            String rStr = genRandom(8);  // randomString -> rStr, generateRandomString -> genRandom
            String concat = rNo + dVal + rStr;
            String hash = genMD5(concat);  // generateMD5Hash -> genMD5

            System.out.println(hash + ";" + rStr);

        } catch (IOException e) {
            System.out.println("Error reading JSON: " + e.getMessage());
            System.exit(1);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error generating MD5: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void scanJson(JsonNode n) {  // node -> n
        if (dVal != null) return;

        if (n.isObject()) {
            n.fields().forEachRemaining(e -> {  // entry -> e
                if ("destination".equals(e.getKey()) && dVal == null) {
                    dVal = e.getValue().asText();
                } else if (dVal == null) {
                    scanJson(e.getValue());
                }
            });
        } else if (n.isArray()) {
            n.elements().forEachRemaining(el -> {  // element -> el
                if (dVal == null) {
                    scanJson(el);
                }
            });
        }
    }

    private static String genRandom(int len) {  // length -> len
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder(len);
        
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        
        return sb.toString();
    }

    private static String genMD5(String in) throws NoSuchAlgorithmException {  // input -> in
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] msgDig = md.digest(in.getBytes());  // messageDigest -> msgDig
        
        StringBuilder hex = new StringBuilder();  // hexString -> hex
        for (byte b : msgDig) {
            String h = Integer.toHexString(0xff & b);  // hex -> h
            if (h.length() == 1) hex.append('0');
            hex.append(h);
        }
        
        return hex.toString();
    }
}