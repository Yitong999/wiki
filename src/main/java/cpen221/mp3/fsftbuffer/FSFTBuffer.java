package cpen221.mp3.fsftbuffer;

import java.util.*;

public class FSFTBuffer<T extends Bufferable> {

    //RI:
    //1. bufferList.size() <= capacity
    //2. Node in bufferList does not equal to null
    //3. time of Node (time node was added) should always be > currentTime - timeout
    // after removing all expired objects

    //AF: represents a buffer that can only store number of objects = capacity
    //for finite time = timeout.

    /* the default buffer size is 32 objects */
    public static final int DSIZE = 32;

    /* the default timeout value is 3600s */
    public static final int DTIMEOUT = 3600;

    private Map<String, Node> idNodeMap;
    private LinkedList<Node> bufferList;
    private int timeout;
    private int capacity;

    /**
     * Create a buffer with a fixed capacity and a timeout value.
     * Objects in the buffer that have not been refreshed within the
     * timeout period are removed from the cache.
     *
     * Requires unique objects have different ids
     * i.e., two objects with same name, different data will still be treated as the same object
     * Campuswire #2148: Items are distinguished only by their ids.
     * So two items that share an id are different versions of the same item.
     *
     * @param capacity the number of objects the buffer can hold
     * @param timeout  the duration, in seconds, an object should
     *                 be in the buffer before it times out
     */

    public FSFTBuffer (int capacity, int timeout) {
        this.idNodeMap = new HashMap<String, Node>();
        this.bufferList = new LinkedList<Node>();
        this.capacity = capacity;
        this.timeout = timeout;
    }

    /**
     * Create a buffer with default capacity and timeout values.
     */
    public FSFTBuffer() {
        this(DSIZE, DTIMEOUT);
    }

    private void checkRep(boolean run) throws AssertionError {
        if (run) {
            int currentTime = (int) (System.currentTimeMillis() / 1000);
            if (bufferList.size() > capacity) {
                throw new AssertionError("RI broken1");
            }
            for (Node each : bufferList) {
                if (each.getTime() <= currentTime - timeout) {
                    //throw new AssertionError("RI broken2");
                } else if (each == null) {
                    throw new AssertionError("RI broken");
                }
            }
        }
    }

    /**
     * Add a value to the buffer.
     * If the buffer is full then remove the least recently accessed
     * object to make room for the new object.
     *
     * First put affects access time (position in LRU) campuswire #2148
     * Repeated puts do not affect access time nor expiry time
     *
     * @param t value to put into buffer
     * Returns whether insertion was successful
     * Returns false if {@code t} = null
     */
    public synchronized boolean put(T t) {

        if (t == null) {
            return false;
        }

        removeAllExpired();

        //if this object has not already been put in cache
        if (!idNodeMap.keySet().contains(t.id())) {

            //if list is full, pop first object from list (accessed not recently)
            if (bufferList.size() == capacity) {
                Node toRemove = bufferList.removeFirst(); //remove least recently used from linked list
                idNodeMap.remove(toRemove.getID()); //remove from hashmap
            }

            int timeStamp = (int) (System.currentTimeMillis() / 1000);
            Node newData = new Node(timeStamp, t);
            bufferList.add(newData); //add new node
            idNodeMap.put(t.id(), newData);
        }

        checkRep(false);
        return true;
    }

    /**
     * Removes all objects that have expired from the buffer
     */
    private void removeAllExpired() {

        //all objects added before this time would have timed out
        int minTime = (int)(System.currentTimeMillis()/1000) - timeout;

        Iterator iter = bufferList.iterator();
        while (iter.hasNext()) {
            Node bufferObj = (Node) iter.next();
            if (bufferObj.getTime() < minTime) {
                idNodeMap.remove(bufferObj.getID()); //remove from the hashmap
                iter.remove(); //remove from the linkedlist
            } else { //all other objects in the buffer have not expired
                break;
            }
        }
    }

    /**
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the buffer
     * Object is moved to front of LRU when get is called (higher priority, recently accessed)
     * @throws NoSuchElementException if object does not exist in the buffer
     * (does not exist = has never been added or has already expired)
     */

    public synchronized T get(String id) throws NoSuchElementException {

        if (!idNodeMap.containsKey(id) || hasExpired(id)) {
            throw new NoSuchElementException();
        } else {
            //object exists and has not expired, move to front of cache but don't update time
            Node currentNode = idNodeMap.get(id);
            bufferList.remove(currentNode);
            bufferList.add(currentNode);

            checkRep(false);
            return (T) idNodeMap.get(id).getData();
        }
    }

    /**
     * Checks if object in buffer has expired
     * @requires object with id exists in the buffer
     * (was previously added to the buffer and was never removed)
     * @param id id of object to check
     * @return true if object has timed out, false otherwise
     */
    private boolean hasExpired(String id){

        int currentTime = (int)(System.currentTimeMillis()/1000);

        if (idNodeMap.get(id).getTime() < (currentTime - timeout)) {
            bufferList.remove(idNodeMap.get(id)); //remove from linkedlist
            idNodeMap.remove(id); //remove from the hashmap
            return true;
        } else {
            return false;
        }
    }

    /**
     * Update the last refresh time for the object with the provided id.
     *
     * This method is used to mark an object as "not stale" so that its
     * timeout is delayed.
     * This method does not affect the access time (LRU position) of the object
     *
     * @param id the identifier of the object to "touch"
     * @return true if successful and false otherwise
     * unsuccessful if object with id does not exist in buffer
     */
    public synchronized boolean touch(String id) {

        if (!idNodeMap.containsKey(id)) {
            checkRep(false);
            return false;
        } else {
            int currentTime = (int)(System.currentTimeMillis()/1000);

            if (hasExpired(id)) { //object timed out not in list
                checkRep(false);
                return false;

            } else { //update time but don't update position in LRU
                Node currentNode = idNodeMap.get(id);
                currentNode.setTime(currentTime);
                checkRep(false);
            }
        }
        return true;
    }

    /**
     * Update an object in the buffer.
     * This method updates an object and acts like a "touch" to
     * renew the object in the cache.
     *
     * @param t the object to update
     * @return true if successful and false otherwise
     */
    public synchronized boolean update(T t) {
        if (touch(t.id())) { //updates timeout
            idNodeMap.get(t.id()).setData(t);
            checkRep(true);
            return true;
        } else { //object does not exist in cache
            checkRep(false);
            return false;
        }
    }
}
