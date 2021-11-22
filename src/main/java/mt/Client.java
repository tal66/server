package mt;

import org.bouncycastle.util.encoders.Hex;
import javax.crypto.Cipher;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class Client {

    private PublicKey serverPublicKey;
    private String host;
    private int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect(Request request) {
        connect(request, "");
    }

    public void connect(Request request, String data) {
        try(Socket client = new Socket(host, port);
            OutputStream out = client.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out);
            InputStream in = client.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
        {
            if (request == Request.FILE){
                requestFile(writer, reader);
            } else if (request == Request.DIGEST){
                reqDigest(writer, reader);
            } else if (request == Request.PUBLIC_KEY){
                requestPublicKey(writer, in);
            } else if (request == Request.SECRET){
                requestSecret(out, data);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void reqDigest(OutputStreamWriter writer, BufferedReader reader) throws IOException {
        System.out.println("client: req. digest");
        writer.write(Request.DIGEST+"\n");
        writer.flush();
        String line = reader.readLine();
        System.out.println("client: received\t" + line);
    }

    private void requestFile(OutputStreamWriter writer, BufferedReader reader) throws IOException, GeneralSecurityException {
        System.out.println("client: req. file");
        writer.write(Request.FILE+"\n");
        writer.flush();

        MessageDigest md = MessageDigest.getInstance("SHA-256", "BC");
        System.out.println("client: received text");
        reader.lines().forEach(line -> {
            md.update((line + "\r\n").getBytes(StandardCharsets.UTF_8));
            System.out.println("\t" + line);
        });

        String hex = new String(Hex.encode(md.digest()), StandardCharsets.UTF_8);
        System.out.println("client: calculated\t" + hex);
    }

    private PublicKey requestPublicKey(OutputStreamWriter writer, InputStream in) throws IOException, GeneralSecurityException {
        System.out.println("client: req. public key");
        writer.write(Request.PUBLIC_KEY+"\n");
        writer.flush();

        KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
        byte[] allBytes = in.readAllBytes();
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(allBytes);
        PublicKey publicKey = factory.generatePublic(pubKeySpec);

        this.serverPublicKey = publicKey;
        return publicKey;
    }

    private void requestSecret(OutputStream out, String data) throws GeneralSecurityException, IOException {
        System.out.println("client: req. secret");
        if (serverPublicKey == null){
            System.out.println("public key undefined");
        }

        Cipher cipher = Cipher.getInstance("RSA", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
        byte[] bytes = data.getBytes();
        byte[] encrypted = cipher.doFinal(bytes);

        out.write((Request.SECRET+"\n").getBytes(StandardCharsets.UTF_8));
        out.write(encrypted);
        out.flush();
    }
}
