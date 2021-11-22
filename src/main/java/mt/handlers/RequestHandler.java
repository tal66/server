package mt.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;

public abstract class RequestHandler {
    public abstract void handle(Socket connection) throws IOException, GeneralSecurityException;

    protected static File getFile(String file) throws FileNotFoundException {
        try {
            return new File(getFileURI(file));
        } catch (IllegalArgumentException e){
            throw new FileNotFoundException(e.getMessage());
        }
    }

    protected static URI getFileURI(String file) throws FileNotFoundException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(file);
        try {
            if (url != null) return url.toURI();
        } catch (URISyntaxException e) {
            throw new FileNotFoundException(e.getMessage());
        }
        throw new FileNotFoundException(file);
    }
}
