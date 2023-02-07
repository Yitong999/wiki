package cpen221.mp3;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import cpen221.mp3.server.WikiMediatorClient;
import cpen221.mp3.server.WikiMediatorServer;
import cpen221.mp3.wikimediator.WikiMediator;
import org.fastily.jwiki.core.Wiki;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static cpen221.mp3.server.WikiMediatorServer.WIKIMEDIATOR_PORT;

public class Task4Test {

    /* reminder: these tests will only pass if you restart the server */

    private Gson gson = new Gson();

    private void runTest(String inputJson, String outputJson){
        try {
            WikiMediatorClient client = new WikiMediatorClient("127.0.0.1", WIKIMEDIATOR_PORT);
            client.sendRequest(inputJson);
            /* this output is exactly the same as outputJson, starting and ending quotation marks inclusive */
            // this version should be convertible to normal json
            String output = client.getResponse();
            client.close();
            Assertions.assertEquals(outputJson, output);
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

//    private void runTestClient2(String inputJson, String outputJson){
//        try {
//            WikiMediatorClient client = new WikiMediatorClient("", WIKIMEDIATOR_PORT);
//            client.sendRequest(inputJson);
//            /* this output is exactly the same as outputJson, starting and ending quotation marks inclusive */
//            // this version should be convertible to normal json
//            String output = client.getResponse();
//            client.close();
//            Assertions.assertEquals(outputJson, output);
//        } catch(IOException ioe){
//            ioe.printStackTrace();
//        }
//    }

    private String setOutput(String id, String status, String resp){
        JsonObject response = new JsonObject();
        response.addProperty("id", id);
        response.addProperty("status", status);
        response.addProperty("response", resp);
        return gson.toJson(response);
    }

    private String setIntOutput(String id, String status, int resp){
        JsonObject response = new JsonObject();
        response.addProperty("id", id);
        response.addProperty("status", status);
        response.addProperty("response", resp);
        return gson.toJson(response);
    }

    private String setSearch(String id, String query, int limit){
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", "search");
        request.addProperty("query", query);
        request.addProperty("limit", limit);
        return gson.toJson(request) + "\n";
    }

    private String setSearchWithTimeout(String id, String query, int limit, int timeout){
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", "search");
        request.addProperty("query", query);
        request.addProperty("limit", limit);
        request.addProperty("timeout", timeout);
        return gson.toJson(request) + "\n";
    }

    private String setZeitgeist(String id, int limit){
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", "zeitgeist");
        request.addProperty("limit", limit);
        return gson.toJson(request) + "\n";
    }

    private String setTrending(String id, int timeLimitInSeconds, int maxItems){
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", "trending");
        request.addProperty("timeLimitInSeconds", timeLimitInSeconds);
        request.addProperty("maxItems", maxItems);
        return gson.toJson(request) + "\n";
    }

    private String setWindowedPeakLoad(String id){
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", "windowedPeakLoad");
        return gson.toJson(request) + "\n";
    }

    private String setWindowedPeakLoadWithParams(String id, int timeWindowInSeconds){
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", "windowedPeakLoad");
        request.addProperty("timeWindowInSeconds", timeWindowInSeconds);
        return gson.toJson(request) + "\n";
    }

    private String setStop(String id){
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", "stop");
        return gson.toJson(request) + "\n";
    }

    private String setStopOutput(String id){
        JsonObject response = new JsonObject();
        response.addProperty("id", id);
        response.addProperty("response", "bye");
        return gson.toJson(response) + "\n";
    }

    private String setShortestPath(String id, String pageTitle1, String pageTitle2, int timeout){
        JsonObject response = new JsonObject();
        response.addProperty("id", id);
        response.addProperty("type", "shortestPath");
        response.addProperty("pageTitle1", pageTitle1);
        response.addProperty("pageTitle2", pageTitle2);
        response.addProperty("timeout", timeout);
        return gson.toJson(response) + "\n";
    }


    @Test
    public void simpleSearchTest() {
        String inputJson = setSearch("1", "Barack Obama", 6);
        String outputJson = setOutput("1", "success", "[Barack Obama, Barack Obama in comics, Barack Obama Sr., Timeline of the Barack Obama presidency, Speeches of Barack Obama, Bibliography of Barack Obama]");
        runTest(inputJson, outputJson);
        //runTestClient2(inputJson, outputJson);
    }

    @Test
    public void simpleSearch2Test() {
        String inputJson = setSearch("2", "Barack Obama in comics", 1);
        String outputJson = setOutput("2", "success", "[Barack Obama in comics]");
        runTest(inputJson, outputJson);
    }

    @Test
    public void simpleZeitgeistTest() {
        String inputJson = setZeitgeist("3", 3);
        String outputJson = setOutput("3", "success", "[Barack Obama in comics]");
         runTest(inputJson, outputJson);
    }

    @Test
    public void simpleTrendingTest(){
        String inputJson = setTrending("4", 5, 3);
        String outputJson = setOutput("4", "success", "[Barack Obama, Barack Obama in comics]");
        runTest(inputJson, outputJson);
    }

    @Test
    public void windowPeakLoadWithParamsTest(){
        String inputJson = setWindowedPeakLoadWithParams("5", 5);
        String outputJson = setIntOutput("5", "success", 5);
        runTest(inputJson, outputJson);
    }

    @Test
    public void windowPeakLoadTest() {
        String inputJson = setWindowedPeakLoad("6");
        String outputJson = setIntOutput("6", "success", 6);
        runTest(inputJson, outputJson);
    }

    @Test
    public void shortestPathTest() {
        String inputJson = setShortestPath("7", "Philosophy", "Barack Obama", 10);
        String outputJson = setOutput("7", "success", "[Philosophy, Academic bias, Barack Obama]");
        runTest(inputJson, outputJson);
    }

    @Test
    public void shortestPathTest2() {
        String inputJson = setShortestPath("8", "Philosophy", "Barack Obama", 1);
        String outputJson = setOutput("8", "failed", "Operation timed out");
        runTest(inputJson, outputJson);
    }

    @Test
    public void simpleSearchSecondClient() {
        String inputJson = setSearch("7", "Barack Obama", 6);
        String outputJson = setOutput("7", "success", "[Barack Obama, Barack Obama in comics, Barack Obama Sr., Timeline of the Barack Obama presidency, Speeches of Barack Obama, Bibliography of Barack Obama]");
        //runTestClient2(inputJson, outputJson);
    }
}
