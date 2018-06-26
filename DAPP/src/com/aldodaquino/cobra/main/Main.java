package com.aldodaquino.cobra.main;

public class Main {
    public static void main(String[] args) {
        Catalog catalog;

        if (args.length > 0)
            catalog = new Catalog(args[0]);
        else catalog = new Catalog();
    }
}
