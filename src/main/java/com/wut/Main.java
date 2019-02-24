package com.wut;

public class Main {

    public static void main(String[] args) {
	// write your code here
        FriendsParser friendsParser = new FriendsParser();
        InfoParser infoParser = new InfoParser();
        infoParser.connectToDatabase();
//
        infoParser.loadDone();
        infoParser.getInfoAboutPerson();

    }
}
