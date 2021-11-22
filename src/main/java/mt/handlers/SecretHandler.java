package mt.handlers;

import mt.Request;
import org.bouncycastle.util.io.pem.PemReader;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class SecretHandler extends RequestHandler {

    private static String publicKeyFile = "public.pem";
    private static String privateKeyFile = "key.pem";
    private Request request;

    public SecretHandler(Request request) {
        this.request = request;
    }

    @Override
    public void handle(Socket connection) throws IOException, GeneralSecurityException {
        switch (this.request){
            case PUBLIC_KEY:
                sendPublicKey(connection);
                break;
            case SECRET:
                decrypt(connection);
                break;
        }
    }

    private static void decrypt(Socket connection) throws IOException, GeneralSecurityException {
        System.out.println("server: received secret");
        try (InputStream in = connection.getInputStream()){
            byte[] bytes = in.readAllBytes();
            String str = decrypt(bytes);
            System.out.println("server: secret is " + str);
        }
    }

    private static String decrypt(byte[] bytes) throws IOException, GeneralSecurityException {
        try {
            PrivateKey key = getKey();
            Cipher cipher = Cipher.getInstance("RSA", "BC");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(bytes);
            String str = new String(decrypted, StandardCharsets.UTF_8);
            return str;
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | NoSuchProviderException e) {
            throw new GeneralSecurityException(e);
        }
    }

    private static PrivateKey getKey() throws IOException, GeneralSecurityException {
        File file = getFile(privateKeyFile);
        try (PemReader pemReader = new PemReader(new FileReader(file))) {
            KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
            byte[] content = pemReader.readPemObject().getContent();
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(content);
            return factory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new GeneralSecurityException(e);
        }
    }

    private static void sendPublicKey(Socket connection) throws IOException {
        System.out.println("server: sending public key");
        OutputStream out = connection.getOutputStream();
        out.write(readPublicKey());
        out.flush();
    }

    private static byte[] readPublicKey() throws IOException {
        File file = getFile(publicKeyFile);
        try (PemReader pemReader = new PemReader(new FileReader(file))) {
            byte[] content = pemReader.readPemObject().getContent();
            return content;
        }
    }
}
