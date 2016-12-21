package com.wut;

public class Main {

    public static void main(String[] args) {
	// write your code here
        JsonParser jsonParser = new JsonParser();
        jsonParser.loadDone();
        jsonParser.searchGraph("http://www.goldenline.pl/anna-kowalska172/");
//        jsonParser.add();

    }
}
