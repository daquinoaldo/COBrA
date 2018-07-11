package com.aldodaquino.javautils;

public class Pair <T, U> {

    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return first.toString() + second.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair)) return false;
        Pair pairObj = (Pair) obj;
        return this.first.equals(pairObj.getFirst()) &&
                this.second.equals(pairObj.getSecond());
    }

}