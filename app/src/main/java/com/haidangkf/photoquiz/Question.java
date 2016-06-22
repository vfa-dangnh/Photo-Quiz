package com.haidangkf.photoquiz;

import java.io.Serializable;

@SuppressWarnings("serial") // With this annotation we are going to hide compiler warnings
public class Question implements Serializable {

    private int id;
    private String category;
    private String comment;
    private String photoPath;
    private String audioPath;

    public Question() {
    }

    public Question(int id, String category, String comment, String photoPath, String audioPath) {
        this.setId(id);
        this.setCategory(category);
        this.setComment(comment);
        this.setPhotoPath(photoPath);
        this.setAudioPath(audioPath);
    }

    public Question(String category, String comment, String photoPath, String audioPath) {
        this.setCategory(category);
        this.setComment(comment);
        this.setPhotoPath(photoPath);
        this.setAudioPath(audioPath);
    }

    @Override
    public String toString() {
        return "Category = " + category + ", Comment = " + comment
                + "\nPhotoPath = " + photoPath + "\nAudioPath = " + audioPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

}
