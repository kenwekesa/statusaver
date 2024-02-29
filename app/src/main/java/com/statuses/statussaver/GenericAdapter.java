package com.statuses.statussaver;

public class GenericAdapter<T> {
    protected T value;

    public GenericAdapter(T value) {
        this.value = value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}