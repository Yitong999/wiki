package cpen221.mp3.fsftbuffer;

public class BufferableString implements Bufferable {

    //RI: id and text are non-null
    //AF: Represents an entry to the FSFTbuffer which has id = title, data (content) = text

    private String title;
    private String text;

    public BufferableString(String id, String text){
        this.title = id;
        this.text = text;
    }

    public String getText(){
        return text;
    }

    public String id(){
        return title;
    }
}
