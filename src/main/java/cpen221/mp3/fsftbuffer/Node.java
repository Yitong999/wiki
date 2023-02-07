package cpen221.mp3.fsftbuffer;

public class Node<T extends Bufferable> {

    //RI: data not null, timeStamp is an integer value >= 0
    //AF: represents a node in a doubly linked list
    // that stores the data to be retrieved from cache
    // and the time this node was added to the list (last time it was accessed)

    private int timeStamp;
    private T data;

    public Node(int timeStamp, T data) {
        this.timeStamp = timeStamp;
        this.data = data;
    }

    //return the id of this object
    public String getID() {
        return this.data.id();
    }

    //returns the time this node was last accessed from the cache
    public int getTime() {
        return this.timeStamp;
    }

    //update the time (used for get/touch methods)
    public void setTime(int time) {
        this.timeStamp = time;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T newData) {
        this.data = newData;
    }
}
