package cpen221.mp3;
import cpen221.mp3.fsftbuffer.Bufferable;
import org.jetbrains.annotations.NotNull;

import java.nio.Buffer;

public class BufferableInteger implements Bufferable, Comparable{
    private String id;
    private int val;

    public BufferableInteger(int val, String id){
        this.val = val;
        this.id = id;
    }
    public String id(){ return id; }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof BufferableInteger) {
            BufferableInteger that = (BufferableInteger) obj;
            return this.val == that.val;
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if(o instanceof BufferableInteger){
            BufferableInteger other = (BufferableInteger) o;
            return this.val - other.val;
        }
        return Integer.MAX_VALUE;
    }
}