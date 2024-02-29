package com.statuses.statussaver.model;


import java.io.Serializable;

public class ImageModel implements Serializable {
    private String path;
    private boolean isSelected = false;

    public ImageModel(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


    public boolean isSelected() {
        return isSelected;
    }
}
