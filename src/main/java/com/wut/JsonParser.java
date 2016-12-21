package com.wut;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

/**
 * Created by rafal on 12/14/16.
 */
public class JsonParser {
    private Stack<String> toDo = new Stack<String>();
    private Set<String> done = new HashSet<String>();

    private int nrAddedRows = 0;
    private boolean lastPage;

    public void visitFriendsOnePage(Elements friendContactList, String userUrl) {
//        Elements friendContactList = getUrlContactsFromOnePage(userUrl + pageContactNumber);

        for (Element friendRow : friendContactList) {
            String hrefToFriend = friendRow.attr("href");
            done.add(userUrl);
            for (String linkvisited : done) {
                if (!linkvisited.contains(hrefToFriend)) {
                    toDo.add(hrefToFriend);
                }

            }

            saveToFile(userUrl + " , " + hrefToFriend);
            nrAddedRows++;

        }
        if (nrAddedRows > 800 && lastPage) {
            try {
                Thread.sleep(3* 60 * 1000);
                System.out.println("I'm sleeping for 3 minuts");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void visitFriendsInAllPages(String userUrl) {
        try {
            Document doc = Jsoup.connect(userUrl + "kontakty/s/1").get();
            Elements pagerElement = doc.select("ul:not(#contactLetters).pager").select("li");
            int numberOfPages = 0;
            if (pagerElement.size() > 1) {
                Element lastPage = pagerElement.get(pagerElement.size() - 2);
                numberOfPages = Integer.parseInt(lastPage.select("a").html());
            }
            Elements friendHrefList = getUrlContactsFromOnePage(doc);
            visitFriendsOnePage(friendHrefList, userUrl);
            lastPage = false;

            for (int i = 2; i <= numberOfPages; i++) {
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                doc = Jsoup.connect(userUrl + "kontakty/s/" + i).get();
                friendHrefList = getUrlContactsFromOnePage(doc);
                visitFriendsOnePage(friendHrefList, userUrl);
                if (i == numberOfPages) {
                    lastPage = true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
//        }

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
//        toDo.add(startUrl);
//        done.add(startUrl);
//        visitFriendsInAllPages(startUrl);
        while (!toDo.isEmpty()) {
            String nextVisit = toDo.remove(0);
            System.out.println(nextVisit);
            visitFriendsInAllPages(nextVisit);

        }

    }

    public void loadDone() {
        BufferedReader br = null;
        FileReader fr = null;

        try {
            br = new BufferedReader(new FileReader("out.txt"));
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                String visited = currentLine.split(",")[0].trim();
                String toVisit = currentLine.split(",")[1].trim();
                this.done.add(visited);
                boolean addToDo = true;
                this.toDo.add(toVisit);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        // remove the same elements in To doList
        Set<String> hs = new LinkedHashSet<String>();
        hs.addAll(toDo);
        toDo.clear();
        toDo.addAll(hs);
        // remove element which is done
        toDo.removeAll(done);
        System.out.println(done);
        System.out.println(toDo);


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
