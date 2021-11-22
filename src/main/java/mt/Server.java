package mt;

import mt.handlers.RequestHandler;
import mt.handlers.RequestHandlerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;


public class Server {
    static Runnable tasks(ServerSocket server){
        return () -> {
            while (true){
                try (Socket connection = server.accept();
                     InputStream in = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
                {
                    connection.setSoTimeout(15000);
                    System.out.println("server: new connection " + connection.getInetAddress());
                    String method = reader.readLine();
                    System.out.println("server: received req. - " + method);
                    RequestHandler handler = RequestHandlerFactory.newHandler(method);
                    handler.handle(connection);
                }
                catch (IOException | GeneralSecurityException e){
                    e.printStackTrace();
                }
                System.out.println("server: bye");
            }
        };
    }

}
