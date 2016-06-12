package com.haidangkf.photoquiz;

public class Category {
    private String category;
    private boolean isSelected;

    public Category() {
    }

    public Category(String category) {
        this.setCategory(category);
        this.setSelected(false);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
