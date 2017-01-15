package com.wut;

public class Main {

    public static void main(String[] args) {
	// write your code here
        FriendsParser friendsParser = new FriendsParser();
//        friendsParser.loadDone();
        //last done http://www.goldenline.pl/monika-andrzejuk2/kontakty/s/88
//        friendsParser.searchGraph("http://www.goldenline.pl/katarzyna-cioczek/");
        InfoParser infoParser = new InfoParser();
        infoParser.connectToDatabase();
//
        infoParser.loadDone();
        infoParser.getInfoAboutPerson();

    }
}
