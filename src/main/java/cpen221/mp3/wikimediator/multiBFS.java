//package cpen221.mp3.wikimediator;
//
//import java.sql.Time;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.TimeoutException;
//
//
//public class multiBFS {
//    static private Integer NUMTHREADS = 15;
//    static private String finalPage;
//    static private Integer TIMEOUT;
//    static volatile private Integer maxPathLength = Integer.MAX_VALUE;
//    static volatile private Boolean done = false;
//
//    static private ArrayList<LinkedList<String>> possiblePaths = new ArrayList<>();
//    static volatile private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
//    static volatile private ConcurrentLinkedQueue<LinkedList<String>> queue = new ConcurrentLinkedQueue<>();
//    static volatile private Set<String> visitedPages = map.newKeySet();
//    static private WikiMediator wiki = new WikiMediator(5, 5);
//
//
//    private static class BFSThread extends Thread {
//
//        private LinkedList<String> currentPath;
//
//        public BFSThread(LinkedList<String> currentPath) {
//            this.currentPath = currentPath;
//        }
//
//        public void run() {
//            String currentPage = currentPath.getLast();
//            visitedPages.add(currentPage);
//            int pathLength = currentPath.size();
//            int currentTime = (int)(System.currentTimeMillis()/1000);
//
//            if (currentTime < TIMEOUT){
//                if (pathLength < maxPathLength) {
//                    List<String> relatedPages = wiki.getLinks(currentPage);
//
//                    for (String page : relatedPages) {
//                        if (page.equals(finalPage)) {
//                            LinkedList<String> newPath = (LinkedList<String>) currentPath.clone();
//                            newPath.add(page);
//                            possiblePaths.add(newPath);
//
//                            synchronized (maxPathLength) { //update max path length
//                                if (pathLength < maxPathLength) {
//                                    maxPathLength = pathLength;
//                                }
//                            }
//                            synchronized (done) {
//                                done = true;
//                            }
//
//                        } else if (!visitedPages.contains(page)) {
//                            LinkedList<String> newPath = (LinkedList<String>) currentPath.clone();
//                            newPath.add(page);
//                            queue.add(newPath);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    public LinkedList<String> search(String startPage, String finalPage, int timeout) throws InterruptedException, TimeoutException{
//
//        this.finalPage = finalPage;
//        int currentTime = (int)(System.currentTimeMillis()/1000);
//        this.TIMEOUT = currentTime + timeout;
//
//        if(currentTime < TIMEOUT) {
//            if (!wiki.exists(startPage) || !wiki.exists(finalPage)) {
//                return new LinkedList<String>();
//            } else if (startPage.equals(finalPage)) {
//                return new LinkedList<String>(List.of(startPage));
//            }
//
//            BFSThread newThread = new BFSThread(new LinkedList<String>(List.of(startPage)));
//            newThread.start();
//            newThread.join();
//        }
//
//        BFSThread[] ts = new BFSThread[NUMTHREADS];
//
//        while(!queue.isEmpty() && currentTime < TIMEOUT) {
//            int numQueued = queue.size();
//            for (int i = 0; i < Math.min(NUMTHREADS, numQueued); i++) {// do parallel computations
//                LinkedList<String> next = queue.poll();
//                BFSThread bfs = new BFSThread(next);
//                ts[i] = bfs;
//                ts[i].start();
//            }
//            for (int i = 0; i < Math.min(NUMTHREADS, numQueued); i++) {
//                ts[i].join();
//            }
//
//            currentTime = (int)(System.currentTimeMillis()/1000);
//        }
//        if(done){
//            System.out.println(possiblePaths);
//            return possiblePaths.get(0);
//        } else {
//            throw new TimeoutException();
//        }
//    }
//}