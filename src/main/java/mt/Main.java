package mt;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.net.*;
import java.security.Security;


public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        Security.addProvider(new BouncyCastleProvider());
        int port = 8000;
        String host = InetAddress.getLocalHost().getHostAddress();
        ServerSocket server = new ServerSocket(port);
        System.out.println("server: listening on port " + port);

        new Thread(Server.tasks(server)).start();
        Client client = new Client(host, port);
        client.connect(Request.FILE);
        client.connect(Request.DIGEST);
        client.connect(Request.PUBLIC_KEY);
        client.connect(Request.SECRET, "E=MC2");

        Thread.sleep(6000);
        System.exit(130);
    }

}

