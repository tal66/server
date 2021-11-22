package mt.handlers;

import mt.Request;

public class RequestHandlerFactory {
    public static RequestHandler newHandler(String reqInput){
        if (reqInput.equalsIgnoreCase(Request.FILE.toString())){
            return new FileHandler(Request.FILE);
        } else if (reqInput.equalsIgnoreCase(Request.DIGEST.toString())){
            return new FileHandler(Request.DIGEST);
        } else if (reqInput.equalsIgnoreCase(Request.PUBLIC_KEY.toString())){
            return new SecretHandler(Request.PUBLIC_KEY);
        } else if (reqInput.equalsIgnoreCase(Request.SECRET.toString())){
            return new SecretHandler(Request.SECRET);
        } else {
            return new ErrorHandler();
        }
    }
}
