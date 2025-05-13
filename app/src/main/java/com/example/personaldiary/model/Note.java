package com.example.personaldiary.model;

public class Note {
    public int note_id;
    public String title;
    public String text;
    public String datetime;
    public String photoPath;

    public Note(int note_id, String title, String text, String datetime, String photoPath) {
        this.note_id = note_id;
        this.title = title;
        this.text = text;
        this.datetime = datetime;
        this.photoPath = photoPath;
    }
}
