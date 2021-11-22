package mt.handlers;

import mt.Request;
import org.bouncycastle.util.encoders.Hex;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class FileHandler extends RequestHandler {

    private static String filename = "file.txt";
    private Request request;

    public FileHandler(Request request) {
        this.request = request;
    }

    @Override
    public void handle(Socket connection) throws IOException, GeneralSecurityException {
        switch (this.request){
            case FILE:
                sendFile(connection);
                break;
            case DIGEST:
                sendDigest(connection);
                break;
        }
    }

    private static void sendFile(Socket connection) throws IOException {
        System.out.println("server: sending file");
        File file = getFile(filename);

        try (OutputStream out = connection.getOutputStream();
             Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             InputStreamReader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(in))
        {
            String line = reader.readLine();
            while (line != null){
                writer.write(line+"\n");
                line = reader.readLine();
            }
            writer.flush();
        }
    }

    private static void sendDigest(Socket connection) throws IOException, GeneralSecurityException {
        System.out.println("server: sending digest");
        try (OutputStream out = connection.getOutputStream();
            Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);){
            writer.write(calculateFileDigest()+"\n");
            writer.flush();
        }
    }

    private static String calculateFileDigest() throws GeneralSecurityException, IOException {
        File file = getFile(filename);
        MessageDigest messageDigest = getMessageDigest();
        digest(file, messageDigest);
        byte[] bytes = messageDigest.digest();
        String hex = new String(Hex.encode(bytes), StandardCharsets.UTF_8);
        return hex;
    }

    private static void digest(File file, MessageDigest messageDigest) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file);
             DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest)){
            byte[] buffer = new byte[1024];
            while (digestInputStream.read(buffer) != -1){;}
        }
    }

    private static MessageDigest getMessageDigest() throws GeneralSecurityException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256", "BC");
            return messageDigest;
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            throw new GeneralSecurityException(e.getMessage());
        }
    }
}
