package cpen221.mp3.wikimediator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.fastily.jwiki.core.Wiki;
import cpen221.mp3.fsftbuffer.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;


public class WikiMediator {

    //RI: requestHis time is in ascending order

    // AF: represent a wiki API to fetch and store info in cache with a limited size
    // that also stores data and performs zeitgeist, trending, windowpeakload, etc. tasks

    private FSFTBuffer <BufferableString> storage;

    private List<Integer> requestHis = new ArrayList<>();
    //String: searched key word   Integer: searched key word count
    private Map<String, Integer> searchOccurrences = new HashMap<>();

    //String: searched key word   List: search occurred time
    private Map<String, List<Integer>> searchHistoryTime = new HashMap<>();

    Gson gson = new Gson();
    private static FileWriter file;

    /**
     * Determine whether the invariant holds
     * @throws AssertionError: indicating the invariant is broken
     */
    private synchronized void checkRep(boolean run) throws AssertionError {
        if (run) {
            if (requestHis == null) {
                throw new AssertionError("invariant broken");
            } else {
                for (int i = 0; i < requestHis.size() - 1; i++) {
                    if (requestHis.get(i + 1) < requestHis.get(i)) {
                        throw new AssertionError("invariant broken");
                    }
                }
            }
        }
    }

    /**
     * Create new wiki api
     * @param capacity: number of objects WikiMediator can hold in memory
     * @param stalenessInterval: time before objects in memory expire
     */

    public WikiMediator(int capacity, int stalenessInterval){
        try {
            File file = new File("local/lastState.txt");
            Scanner sc = new Scanner(file);

            String jsonString = sc.nextLine();

            HashMap<String, String> allData = gson.fromJson(jsonString, HashMap.class);
            for(String key: allData.keySet()) {
                switch (key) {
                    case "seachHistoryTime":
                        Type gsonMapType1 = new TypeToken<HashMap<String, List<Integer>>>() {
                        }.getType();
                        searchHistoryTime = gson.fromJson(allData.get(key), gsonMapType1);
                        break;

                    case "requestHis":
                        requestHis = gson.fromJson(allData.get(key), List.class);
                        break;

                    case "searchOccurences":
                        Type gsonMapType2 = new TypeToken<HashMap<String, Integer>>() {
                        }.getType();
                        searchOccurrences = gson.fromJson(allData.get(key), gsonMapType2);
                        break;

                    default:
                        break;
                }
            }

            this.storage = new FSFTBuffer(capacity, stalenessInterval);

        } catch (Exception e){
            this.storage = new FSFTBuffer(capacity, stalenessInterval);
        }
    }

    Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();


    /** Given a query, return up to limit page titles that match the query string
    *(per Wikipedia's search service).
    * @param query is a query string to search on wikipedia
    * @param limit the max number of page titles that match the query string to return
    * return titles s.t. titles contains limit numbers of title of query
    */

    public synchronized List<String> search(String query, int limit){
        callTime();
        storeSearch(query);

        List<String> titles = wiki.search(query, limit);

        for (String title : titles) {
            storeData(title);
        }

        checkRep(false);
        return titles;
    }

    /** Given a pageTitle, return the text associated
    * with the Wikipedia page that matches pageTitle.
    * Returns empty text if the page is non-existent/something went wrong
    * @param pageTitle a String to search
    * return the text of pageTitle
    */

    public synchronized String getPage (String pageTitle) {
        callTime();
        String content;
        try{
            content = storage.get(pageTitle).getText();
        } catch (NoSuchElementException e) {
            storeData(pageTitle);
            content = storage.get(pageTitle).getText();
        }
        storeSearch(pageTitle);
        checkRep(false);
        return content;
    }

    /** Return the most common Strings used in search and getPage requests,
     * with items being sorted in non-increasing count order.
     * When many requests have been made, return only limit items.
     * @param limit the size of search history
     * return zeitList s.t. zeitList contains limit numbers of search history
     */

    public synchronized List<String> zeitgeist(int limit) {
        callTime();
        return cmRequest(searchOccurrences, limit);
    }

    /** Return the most common Strings used in hist and getPage requests,
     * with items being sorted in non-increasing count order.
     * When many requests have been made, return only limit items.
     * @param hist the history of search
     * @param limit the size of search history
     * return zeitList s.t. zeitList contains limit numbers of search history
     */
    private synchronized List<String> cmRequest(Map<String, Integer> hist, int limit) {
        List<String> sortedByNumRequests = hist.entrySet().stream()
                .sorted(Map.Entry.<String, Integer> comparingByValue()
                        .reversed()).limit(limit).map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return sortedByNumRequests;
    }

    /** Similar to zeitgeist(), but returns the most frequent requests made
    * in the last timeLimitInSeconds seconds. This method should report at most
    * maxItems of the most frequent requests.
    * @param timeLimitInSeconds the limit of time
    * @param maxItems max size of returned List
    */
    public synchronized List<String> trending(int timeLimitInSeconds, int maxItems) {
        callTime();
        int currentTime = (int)(System.currentTimeMillis()/1000);
        Map<String, Integer> his = new HashMap<>(searchOccurrences);
        Map<String, List<Integer>> hisTime = new HashMap<>(searchHistoryTime);

        for (String search : hisTime.keySet()) {
            int searchCount = his.get(search);

            for (int time : hisTime.get(search)) {
                if (currentTime - time > timeLimitInSeconds) {
                    his.put(search, --searchCount);
                }
                if (his.get(search) == 0) {
                    his.remove(search);
                }
            }
        }
        int size = his.size();
        if (size <= maxItems) {
            checkRep(false);
            return cmRequest(his, size);
        } else {
            checkRep(false);
            return cmRequest(his, maxItems);
        }
    }

    /** Returns maximum number of requests seen in any time window of a given length
    * Includes all requests made using the public API of WikiMediator
    * (counts all five basic page request methods)
    * @param timeWindowInSeconds time windows in seconds
     */
    public synchronized int windowedPeakLoad(int timeWindowInSeconds){
        callTime();
        int maxCount = 1; //after implement windowedPeakLoad, requestHis.size() = 1
        for(int i = 0; i < requestHis.size() - 1; i++){
            int count = 1;
            for(int j = i + 1; j < requestHis.size(); j++){
                if(requestHis.get(j) - requestHis.get(i) <= timeWindowInSeconds){
                    count++;
                }else{
                    break;
                }
            }
            if(count > maxCount){
                maxCount = count;
            }
        }
        checkRep(true);
        return maxCount;
    }

    /** Returns maximum number of requests seen in 30s
     * Includes all requests made using the public API of WikiMediator????
     */

    public synchronized int windowedPeakLoad(){
        return windowedPeakLoad(30);
    }

    /**
     * store Data to the cache
     * @param title the title of the info
     */
    private synchronized void storeData(String title){
        storage.put(new BufferableString(title, wiki.getPageText(title)));
    }

    /**
     * store search to searchHistory
     * store search time to searchHistoryTime
     * @param trace the query or title the user search
     */
    private synchronized void storeSearch(String trace){
        int currentTime = (int)(System.currentTimeMillis()/1000);

        if (searchHistoryTime.containsKey(trace)) {
            List<Integer> tmp = new ArrayList<>(searchHistoryTime.get(trace));
            tmp.add(currentTime);
            searchHistoryTime.put(trace, tmp);
        } else {
            List<Integer> tmp = Arrays.asList(currentTime);
            searchHistoryTime.put(trace, tmp);
        }

        if (searchOccurrences.containsKey(trace)) {
            int timesSearched = searchOccurrences.get(trace);
            searchOccurrences.put(trace, ++timesSearched);
        } else {
            searchOccurrences.put(trace, 1);
        }
    }

    /**
     * store the current time when any of the
     * 5 main methods are called
     */
    private void callTime() {
        int currentTime = (int)(System.currentTimeMillis()/1000);
        requestHis.add(currentTime);
    }

    /**
     * Gets links on wikipedia page with title {@code pageTitle}
     * @param pageTitle: title of wikipedia page to get links from
     * @return list of related links
     */
    private synchronized List<String> getLinks(String pageTitle) {
        return wiki.getLinksOnPage(pageTitle);
    }

    /**
     * Checks if wikipedia page with title {@code pageTitle} exists
     * @param pageTitle
     * @return true if exists, false otherwise
     */
    private synchronized boolean exists(String pageTitle) {
        return wiki.exists(pageTitle);
    }

    /**
     * Returns path between page titles
     * If a path exists, a list of page titles (including the starting and ending pages) on the shortest path is returned
     * If there are >2 shortest paths, one with the lowest lexicographical value is returned
     * If no path exists between two pages, an empty List is returned
     * @param pageTitle: page to start search from
     * @param pageTitle2: page to end search, destination page
     * @param timeout: maximum time - in seconds- permitted for searching, stops search if time exceeded
     * @throws TimeoutException if search takes longer than {@code timeout}
     */

    public synchronized List<String> shortestPath(String pageTitle, String pageTitle2, int timeout) throws TimeoutException {

        LinkedList<String> firstPage = new LinkedList<String>();
        firstPage.add(pageTitle);

        int currentTime = (int)(System.currentTimeMillis()/1000);
        int maxTime = currentTime + timeout;

        if (currentTime < maxTime) {
            //One of the pages does not exist
            if (!exists(pageTitle) || !exists(pageTitle2)) {
                return new ArrayList<String>();
            } else if (pageTitle.equals(pageTitle2)) {
                return firstPage;
            }

        } else {
            throw new TimeoutException();
        }

        Set<String> visitedPages = new HashSet<>();
        //List of paths taken thus far
        LinkedList<LinkedList<String>> queue = new LinkedList<>();
        queue.add(firstPage);

        while (queue.size() > 0) {

            currentTime = (int)(System.currentTimeMillis()/1000);
            if (currentTime >= maxTime) {
                throw new TimeoutException();
            }

            LinkedList<String> path = queue.removeFirst();
            String mostRecentPage = path.getLast(); //get last element in path

            List<String> relatedPages = getLinks(mostRecentPage);
            visitedPages.add(mostRecentPage);

            for (String page : relatedPages) {
                if (page.equals(pageTitle2)) {
                    path.add(page);
                    //will be lexicographically lowest since titles are returned in alphabetic order
                    return path;
                } else if (!visitedPages.contains(page)) {
                    LinkedList<String> newPath = (LinkedList<String>) path.clone();
                    newPath.add(page);
                    queue.add(newPath);
                }
            }
        }
        return new ArrayList<String>();
    }

    /**
     * Stores current state of the wikimediator
     */
    public synchronized void storeState(){

        //nothing to store
        if(requestHis.size() == 0){
            return;
        }
        //Store searchOccurrences (Map<String, Integer>)
        Type gsonMapType = new TypeToken<HashMap<String, Integer>>(){}.getType();
        String occurrencesJson = gson.toJson(searchOccurrences,gsonMapType);

        //Store searchHistoryTime(Map<String, List<Integer>)
        Type gsonMapType2 = new TypeToken<HashMap<String, List<Integer>>>(){}.getType();
        String historyTimeJson = gson.toJson(searchHistoryTime,gsonMapType2);

        //Store requestHis (List<Integer>)
        Type gsonListType = new TypeToken<List>(){}.getType();
        String hisJson = gson.toJson(requestHis, gsonListType);

        HashMap<String, String> fullJson = new HashMap<>();
        fullJson.put("searchOccurences", occurrencesJson);
        fullJson.put("seachHistoryTime", historyTimeJson);
        fullJson.put("requestHis", hisJson);
        String finalJson = gson.toJson(fullJson, gsonMapType);

        try {
            file = new FileWriter("local/lastState.txt");
            file.write(finalJson);
        } catch (IOException io){ //ERROR WRITING TO FILE
        } finally {
            try {
                file.flush();
                file.close();
            } catch (IOException e){ //ERROR CLOSING
            }
        }
    }
}
