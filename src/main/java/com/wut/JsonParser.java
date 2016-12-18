package com.wut;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Created by rafal on 12/14/16.
 */
public class JsonParser {
    String json = "{\"total\":3,\"_links\":{\"self\":{\"href\":\"https://api.goldenline.pl/users/43549/contacts\"},\"owner\":{\"href\":\"https://api.goldenline.pl/users/43549\",\"title\":\"Contacts owner\"}},\"_embedded\":{\"contacts\":[{\"id\":100,\"name\":\"Pomoc\",\"surname\":\"GoldenLine\",\"photo\":\"https://static.goldenline.pl/user_photo/100/user_100_d5c5f5_huge.jpg\",\"_links\":{\"self\":{\"href\":\"https://api.goldenline.pl/users/100\"},\"www\":{\"href\":\"https://www.goldenline.pl/goldenline-pomoc/\",\"title\":\"User profile WWW address\",\"type\":\"text/html\"}}},{\"id\":23558,\"name\":\"Grażyna\",\"surname\":\"Szydłowska Ex Janiak\",\"photo\":\"https://static.goldenline.pl/user_photo/006/user_23558_18fc2b_huge.jpg\",\"_links\":{\"self\":{\"href\":\"https://api.goldenline.pl/users/23558\"},\"www\":{\"href\":\"https://www.goldenline.pl/grazyna-janiak/\",\"title\":\"User profile WWW address\",\"type\":\"text/html\"}}},{\"id\":39874,\"name\":\"Mar\",\"surname\":\"Galt\",\"photo\":\"https://static.goldenline.pl/user_man_photo_huge.png\",\"_links\":{\"self\":{\"href\":\"https://api.goldenline.pl/users/39874\"},\"www\":{\"href\":\"https://www.goldenline.pl/39874/\",\"title\":\"User profile WWW address\",\"type\":\"text/html\"}}}]}}";
    private List<String> toVisitNeighbours = new LinkedList<String>();
    private Stack<String> visited = new Stack<String>();


    public void visitFriendsOnePage(Elements friendContactList, String userUrl) {
//        Elements friendContactList = getUrlContactsFromOnePage(userUrl + pageContactNumber);

        for (Element friendRow : friendContactList) {
            String hrefToFriend = friendRow.attr("href");
//            for (String linkvisited : visited) {
//                if (!linkvisited.contains(hrefToFriend)) {
                    toVisitNeighbours.add(hrefToFriend);
                    visited.add(hrefToFriend);
//                }
                System.out.println("friend " + hrefToFriend);

                saveToFile(userUrl + " , " + hrefToFriend);
//            }

        }
    }

    public void visitFriendsInAllPages(String userUrl) {
        try {
            Document doc = Jsoup.connect(userUrl+"kontakty/s/1").get();
            Elements pagerElement = doc.select("ul:not(#contactLetters).pager").select("li");
            int numberOfPages = 0;
            if (pagerElement.size() > 1) {
                Element lastPage = pagerElement.get(pagerElement.size() - 2);
                numberOfPages =  Integer.parseInt(lastPage.select("a").html());
            }
            Elements friendHrefList = getUrlContactsFromOnePage(doc);
            visitFriendsOnePage(friendHrefList, userUrl);
            for(int i = 0; i < numberOfPages;i++) {
                try {
                    Thread.sleep(10*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                doc = Jsoup.connect(userUrl + "kontakty/s/"+i).get();
                friendHrefList = getUrlContactsFromOnePage(doc);
                visitFriendsOnePage(friendHrefList, userUrl);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Elements getUrlContactsFromOnePage(Document doc) {
        Elements friendHrefList = null;

        Elements friendList = doc.select("table.contactList");
        Elements photosList = friendList.select("td.inside.photo");
        friendHrefList = photosList.select("a[href]");

        return friendHrefList;
    }


    //przeszukiwanie grafu wszerz
    public void searchGraph(String startUrl) {
//        toVisitNeighbours.add(startUrl);
        visited.add(startUrl);
        visitFriendsInAllPages(startUrl);
        while (!toVisitNeighbours.isEmpty()) {
            String nextVisit = toVisitNeighbours.remove(0);
            System.out.println(nextVisit);
            visitFriendsInAllPages(nextVisit);
        }

    }

    private void saveToFile(String line) {
        System.out.println(line);
        PrintWriter output = null;
        try {
            output = new PrintWriter(new FileWriter("out.txt", true));
            output.println(line);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
