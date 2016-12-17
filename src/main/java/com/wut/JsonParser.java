package com.wut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by rafal on 12/14/16.
 */
public class JsonParser {
    String json = "{\"total\":3,\"_links\":{\"self\":{\"href\":\"https://api.goldenline.pl/users/43549/contacts\"},\"owner\":{\"href\":\"https://api.goldenline.pl/users/43549\",\"title\":\"Contacts owner\"}},\"_embedded\":{\"contacts\":[{\"id\":100,\"name\":\"Pomoc\",\"surname\":\"GoldenLine\",\"photo\":\"https://static.goldenline.pl/user_photo/100/user_100_d5c5f5_huge.jpg\",\"_links\":{\"self\":{\"href\":\"https://api.goldenline.pl/users/100\"},\"www\":{\"href\":\"https://www.goldenline.pl/goldenline-pomoc/\",\"title\":\"User profile WWW address\",\"type\":\"text/html\"}}},{\"id\":23558,\"name\":\"Grażyna\",\"surname\":\"Szydłowska Ex Janiak\",\"photo\":\"https://static.goldenline.pl/user_photo/006/user_23558_18fc2b_huge.jpg\",\"_links\":{\"self\":{\"href\":\"https://api.goldenline.pl/users/23558\"},\"www\":{\"href\":\"https://www.goldenline.pl/grazyna-janiak/\",\"title\":\"User profile WWW address\",\"type\":\"text/html\"}}},{\"id\":39874,\"name\":\"Mar\",\"surname\":\"Galt\",\"photo\":\"https://static.goldenline.pl/user_man_photo_huge.png\",\"_links\":{\"self\":{\"href\":\"https://api.goldenline.pl/users/39874\"},\"www\":{\"href\":\"https://www.goldenline.pl/39874/\",\"title\":\"User profile WWW address\",\"type\":\"text/html\"}}}]}}";
    private List<String> toVisitNeighbours = new LinkedList<String>();
    private Stack<String> visited = new Stack<String>();


    public void visitUsersContacts(String userUrl) {
        Elements linkContactList = getUrlContacts(userUrl);
        for (Element linkContact : linkContactList) {
            String linkToContact = linkContact.attr("href") + "kontakty";
            toVisitNeighbours.add(linkToContact);
            visited.add(linkToContact);
            System.out.println(linkToContact);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            saveToFile(userUrl + " , "  +linkToContact);

        }
    }

    private Elements getUrlContacts(String userUrl) {
        Elements linkContactList = null;
        try {
            Document doc = Jsoup.connect(userUrl).get();
            Elements contactList = doc.select("table.contactList");
            Elements photosList = contactList.select("td.inside.photo");
            linkContactList = photosList.select("a[href]");
        } catch (IOException je) {
            je.printStackTrace();
        }
        return linkContactList;
    }

    //przeszukiwanie grafu wszerz
    public void searchGraph(String startUrl) {
//        toVisitNeighbours.add(startUrl);
        visited.add(startUrl);
        visitUsersContacts(startUrl);
        while (!toVisitNeighbours.isEmpty()) {
            String nextVisit = toVisitNeighbours.remove(0);
            System.out.println(nextVisit);
            visitUsersContacts(nextVisit);
            System.out.println(visited.size());
        }

    }
    private void saveToFile(String line){
        System.out.println(line);
        PrintWriter output = null;
        try {
            output = new PrintWriter(new FileWriter("out.txt",true));
            output.println(line);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
