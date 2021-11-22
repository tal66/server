package mt.handlers;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.GeneralSecurityException;

public class ErrorHandler extends RequestHandler {

    public ErrorHandler() {
    }

    @Override
    public void handle(Socket connection) throws IOException, GeneralSecurityException {
        try (OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream())){
            out.write("bad request");
        }
    }
}
