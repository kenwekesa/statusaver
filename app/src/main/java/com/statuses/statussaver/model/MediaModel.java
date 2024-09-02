package com.statuses.statussaver.model;

public class MediaModel {
    private String path;
    private boolean isVideo;

    public MediaModel(String path, boolean isVideo) {
        this.path = path;
        this.isVideo = isVideo;
    }

    // Getter for path
    public String getPath() {
        return path;
    }

    // Setter for path
    public void setPath(String path) {
        this.path = path;
    }

    // Getter for isVideo
    public boolean isVideo() {
        return isVideo;
    }

    // Setter for isVideo
    public void setVideo(boolean video) {
        isVideo = video;
    }
}