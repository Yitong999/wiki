package cpen221.mp3.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import cpen221.mp3.wikimediator.WikiMediator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;

/** Adopted from FibonacciServer
 * WikiMediatorServer is a server that finds the n^th Fibonacci number given n. It
 * accepts requests of the form: Request ::= Number "\n" Number ::= [0-9]+ and
 * for each request, returns a reply of the form: Reply ::= (Number | "err")
 * "\n" where a Number is the requested Fibonacci number, or "err" is used to
 * indicate a misformatted request. FibonacciServer can handle only one client
 * at a time.
 */

public class WikiMediatorServer {

    public static final int WIKIMEDIATOR_PORT = 9090; // default port where server listen for connection

    private ServerSocket serverSocket;
    private int maxConcurrentRequests;
    private WikiMediator wikiMediator;

    /** Adopted from FibonacciServer
     * Start a server at a given port number, with the ability to process
     * upto n requests concurrently.
     *
     * @param port the port number to bind the server to, 9000 <= {@code port} <= 9999
     * @param n the number of concurrent requests the server can handle, 0 < {@code n} <= 32
     * @param wikiMediator the WikiMediator instance to use for the server, {@code wikiMediator} is not {@code null}
     *
     */
    public WikiMediatorServer(int port, int n, WikiMediator wikiMediator) throws IOException {
        serverSocket = new ServerSocket(port);
        maxConcurrentRequests = n;
        this.wikiMediator = wikiMediator;
    }

    /**
     * Run the server, listening for connections and handling time
     * @throws IOException if the main server socket is broken
     */
    public void serve() throws RuntimeException {
        ConcurrentLinkedQueue<Thread> socketThreads = new ConcurrentLinkedQueue<>();

        while (true) {
            // block until a client connects
            final Socket socket;

            try {
                socket = serverSocket.accept();
            } catch (IOException ioe) {
                throw new RuntimeException();
            }

            Thread handler = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        try {
                            handle(socket);
                        } finally {
                            socket.close();
                        }
                    } catch(IOException ioe) {
                        ioe.printStackTrace();
                        throw new RuntimeException();
                    }
                }
            });
            handler.start();
        }
    }

    /**
     * Handle one client connection. Returns when client disconnects
     * @param socket: socket where client is connected
     * @throws IOException: if the connection encounters an error
     */
    private void handle(Socket socket) throws IOException {
        /*
         * get the socket's input stream and convert it
         * from a byte stream to a character stream
         */
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        /*
         * wrap character stream to byte stream and wrap a PrintWriter around so
         * we have more convenient ways to write java primitive types
         */
        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        try {
            for (String inputJson = in.readLine(); inputJson != null; inputJson = in.readLine()) {
                Gson gson = new Gson();
                WikiRequest request = gson.fromJson(inputJson, WikiRequest.class);
                JsonObject response = processRequest(request);
                out.println(gson.toJson(response));
                out.flush();
            }
        } finally {
            out.close();
            in.close();
        }
    }

    /**
     * process a WikiRequest and return a WikiResponse Object
     * @param request: the request sent from the client to the WikiServer
     * @return the server response according to the parameters of the request
     */
    public JsonObject processRequest(WikiRequest request) {
        String requestType = request.getType();
        String id = request.getId();
        JsonObject response = new JsonObject();

        switch (requestType) {
            case "search":
                String query = request.getQuery();
                Integer limit = request.getLimit();
                if (query != null && limit != null) {
                    List<String> reply = wikiMediator.search(query, limit);
                    response.addProperty("id", id);
                    response.addProperty("status", "success");
                    response.addProperty("response", reply.toString());
                } else {
                    response.addProperty("id", id);
                    response.addProperty("status", "failed");
                    response.addProperty("response", "missing required fields");
                }
                break;
            case "getPage":
                String pageTitle = request.getPageTitle();
                if (pageTitle != null) {
                    String reply = wikiMediator.getPage(pageTitle);
                    response.addProperty("id", id);
                    response.addProperty("status", "success");
                    response.addProperty("response", reply);
                } else {
                    response.addProperty("id", id);
                    response.addProperty("status", "failed");
                    response.addProperty("response", "missing required fields");
                }
                break;
            case "zeitgeist":
                Integer limitZ = request.getLimit();
                if (limitZ != null) {
                    List<String> reply = wikiMediator.zeitgeist(limitZ);
                    response.addProperty("id", id);
                    response.addProperty("status", "success");
                    response.addProperty("response", reply.toString());
                } else {
                    response.addProperty("id", id);
                    response.addProperty("status", "failed");
                    response.addProperty("response", "missing required fields");
                }
                break;
            case "trending":
                Integer timeLimitInSeconds = request.getTimeLimitInSeconds();
                Integer maxItems = request.getMaxItems();
                if (timeLimitInSeconds != null && maxItems != null) {
                    List<String> reply = wikiMediator.trending(timeLimitInSeconds, maxItems);
                    response.addProperty("id", id);
                    response.addProperty("status", "success");
                    response.addProperty("response", reply.toString());
                } else {
                    response.addProperty("id", id);
                    response.addProperty("status", "failed");
                    response.addProperty("response", "missing required fields");
                }
                break;
            case "windowedPeakLoad":
                Integer timeWindowInSeconds = request.getTimeWindowInSeconds();
                if (timeWindowInSeconds != null) {
                    int reply = wikiMediator.windowedPeakLoad(timeWindowInSeconds);
                    response.addProperty("id", id);
                    response.addProperty("status", "success");
                    response.addProperty("response", reply);
                } else {
                    int reply = wikiMediator.windowedPeakLoad();
                    response.addProperty("id", id);
                    response.addProperty("status", "success");
                    response.addProperty("response", reply);
                }
                break;
            case "stop":
                wikiMediator.storeState();
                response.addProperty("id", id);
                response.addProperty("response", "bye");
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "shortestPath":
                Integer timeout = request.getTimeOut();
                String pageTitle1 = request.getPageTitle1();
                String pageTitle2 = request.getPageTitle2();
                if (timeout != null && pageTitle1 != null && pageTitle2 != null) {
                    try {
                        List<String> reply = wikiMediator.shortestPath(pageTitle1, pageTitle2, timeout);
                        response.addProperty("id", id);
                        response.addProperty("status", "success");
                        response.addProperty("response", reply.toString());
                    } catch (TimeoutException te) {
                        response.addProperty("id", id);
                        response.addProperty("status", "failed");
                        response.addProperty("response", "Operation timed out");
                    }
                } else {
                    response.addProperty("id", id);
                    response.addProperty("status", "failed");
                    response.addProperty("response", "missing required fields");
                }
                break;
            default:
                response.addProperty("id", id);
                response.addProperty("status", "failed");
                response.addProperty("response", "invalid command");
                break;
        }
        return response;
    }

    public static void main(String[] args) {
        try {
            WikiMediatorServer server = new WikiMediatorServer(WIKIMEDIATOR_PORT, 5, new WikiMediator(5,3));
            server.serve();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
