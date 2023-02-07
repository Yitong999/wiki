package cpen221.mp3.server;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Assertions;

import java.io.*;
import java.net.Socket;

/** Adopted from FibonacciClient
 * WikiMediator Client is a client that sends requests to the WikiMediator Server
 * and interprets its replies
 * A new WikiMediator is "open" until the close() is called,
 * at which point it is "closed" and may not be used further
 */
public class WikiMediatorClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Make a WikiMediatorClient and connect it to a server running on
     * hostname at the specific port
     *
     * @throws IOException if cannot connect
     */
    public WikiMediatorClient(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    /**
     * Send a request to the server. Requires this is "open"
     * @param request: request to get result from Wiki Api in json string format
     * @throws IOException if network or server failure
     */
    public void sendRequest(String request) throws IOException{
        out.print(request);
        out.flush();
    }

    /**
     * Get a response from the next request that was submitted
     * Requires this is "open"
     * @return the json response from the server
     * @throws IOException
     */
    public String getResponse() throws IOException{
        return in.readLine();
    }

    /**
     * Closes the client's connection to the server
     * This client is now "closed". Requires this is "open"
     * @throws IOException if close fails
     */
    public void close() throws IOException{
        in.close();
        out.close();
        socket.close();
    }
}
