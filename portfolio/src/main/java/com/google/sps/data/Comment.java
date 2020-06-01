package com.google.sps.data; 

public final class Comment {

    private final long id;
    private final String name;
    private final long timestamp;
    private final String text;

    public Comment(long id, String name, long timestamp, String text) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.text = text;
    }
}