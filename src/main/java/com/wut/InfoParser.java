package com.wut;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by rafal on 12/22/16.
 */
public class InfoParser {
    public static final String[] USER_AGENT_ARRAY = {"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Acoo Browser; InfoPath.2; .NET CLR 2.0.50727; Alexa Toolbar)",
           "Mozilla/4.0 (compatible; MSIE 7.0; America Online Browser 1.1; rev1.5; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)","Mozilla/4.0 (compatible; MSIE 8.0; AOL 9.6; AOLBuild 4340.168; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; MS-RTC LM 8)",
    "Mozilla/4.0 (compatible; MSIE 6.0; AOL 8.0; Windows NT 5.1; SV1)","Mozilla/4.0 (compatible; MSIE 6.0; AOL 7.0; Windows NT 5.1)",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.0 Safari/537.36","Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.67 Safari/537.36","Mozilla/5.0 (X11; OpenBSD i386) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1944.0 Safari/537.36","Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.3319.102 Safari/537.36","Mozilla/5.0 (X11; U; Linux x86_64; fr; rv:1.9.0.11) Gecko/2009060309 Ubuntu/9.04 (jaunty) Firefox/3.0.11",
    "Mozilla/5.0 (compatible; MSIE 10.6; Windows NT 6.1; Trident/5.0; InfoPath.2; SLCC1; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 2.0.50727) 3gpp-gba UNTRUSTED/1.0","Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/4.0; GTB6; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.5.30729; OfficeLiveConnector.1.4; OfficeLivePatch.1.3; .NET CLR 3.0.30729; Lunascape 5.1.3.4)",
            "Mozilla/5.0 (Windows; U; ; cs-CZ) AppleWebKit/532+ (KHTML, like Gecko, Safari/532.0) Lunascape/5.1.2.3"};
    public Connection connection = null;
    Set<LinkPair> links = new LinkedHashSet<LinkPair>();
    private int doneCounter = 0;
    public void loadDone() {
        BufferedReader br = null;
        BufferedReader br1 = null;
        FileReader fr = null;

        try {
            br = new BufferedReader(new FileReader("out2.txt"));
            br1 = new BufferedReader(new FileReader("out3.txt"));
            String currentLine;
            LineNumberReader lnr = new LineNumberReader(br1);
            lnr.skip(Long.MAX_VALUE);
            int i = 0;
            while ((currentLine = br.readLine()) != null) {
//                String visited = currentLine.split(",")[0].trim();
                if (i > lnr.getLineNumber() - 1) {
                    String toVisit = currentLine.split(",")[1].trim();
                    String user = currentLine.split(",")[0].trim();
                    this.links.add(new LinkPair(user,toVisit));
//                    System.out.println(toVisit);

                }
                i++;
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

    }

    private void addSkillToDatabase(String skill,String userLink) {
        try {
            PreparedStatement prepStmt = connection.prepareStatement(
                    "insert Into skills (skill_name, description) values  (?,?)");
            String sql =
                    "INSERT INTO users_skills (user_id, skill_id) VALUES " +
                            "    (( SELECT user_id from users where link Like ?)," +
                            "    ( SELECT skill_id from skills WHERE skill_name = ?));";
            PreparedStatement prepStmt1 = connection.prepareStatement(sql);
            if (!skillExistInDatabase(skill)) {
                prepStmt.setString(1, skill);
                prepStmt.setString(2, "");
                prepStmt.executeUpdate();

            }
            prepStmt1.setString(1,"%"+userLink+"%");
            prepStmt1.setString(2,skill);
            prepStmt1.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    private void addUserToDatabase(String name,String city,String district,String userLink,String friendLink) {
        try {
            PreparedStatement prepStmt = connection.prepareStatement(
                    "insert Into users (name,city,district,link) values  (?,?,?,?)");
            prepStmt.setString(1,name);
            prepStmt.setString(2,city);
            prepStmt.setString(3,district);
            prepStmt.setString(4,friendLink);
            if(!userExistInDatabase(friendLink)) {
                if (prepStmt.executeUpdate() == 1) {
                   addUserFriendToDatabase(userLink, friendLink);
                }
            }else{
                addUserFriendToDatabase(userLink, friendLink);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

    }
    public void addUserFriendToDatabase(String user,String friend){
        try {
            System.out.println(user+ "," + friend);
            PreparedStatement prepStmt = connection.prepareStatement(
                    "insert Into user_friends (user_id, friend_id) values  (" +
                            "(Select user_id from users where link Like ? Limit 1)," +
                            "(Select user_id from users where link like ? Limit 1))");
            prepStmt.setString(1,"%"+user+"%");
            prepStmt.setString(2,"%" + friend+"%");

            prepStmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private boolean skillExistInDatabase(String skill) throws SQLException {
        boolean skillExist = false;
        PreparedStatement stmt1 = connection.prepareStatement("Select skill_name from skills where skill_name = ?");
        stmt1.setString(1, skill);
        if (stmt1.executeQuery().next()) {
            skillExist = true;
        }
        return skillExist;
    }
    private boolean userExistInDatabase(String userLink) throws SQLException {
        boolean userExist = false;
        PreparedStatement stmt1 = connection.prepareStatement("Select name from users where link Like ?");
        stmt1.setString(1, "%"+userLink+"%");
        if (stmt1.executeQuery().next()) {
            userExist = true;
        }
        return userExist;
    }

    public void connectToDatabase() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;

        }

        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/social_connection", "rafal",
                    "rafal");

        } catch (SQLException e) {

            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;

        }

        if (connection != null) {
            System.out.println("You made it, take control your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }
    }

    public void getInfoAboutPerson() {
        try {
            for (LinkPair personLink : links) {
                if(!userExistInDatabase(personLink.friendLink)) {
                    Document doc = Jsoup.connect(personLink.friendLink).timeout(1000 * 10).userAgent(USER_AGENT_ARRAY[randomizeNumber(0, USER_AGENT_ARRAY.length)]).get();
                    downloadPage(doc, getFileName(personLink.friendLink) + ".html");
                    String name = doc.select("h1.user-name:first-child").text();
                    String locality = doc.select("span.locality").text();
                    String region = doc.select("span.region").text();

                    addUserToDatabase(name, locality, region, personLink.userLink, personLink.friendLink);


                    Elements skillsList = doc.select("ul.skills-list").select("li.badge");
                    List<String> skills = new LinkedList<String>();
                    for (Element skill : skillsList) {
                        skills.add(skill.attr("title"));
                        addSkillToDatabase(skill.attr("title").trim(), personLink.friendLink);
                    }
                    String line = "";
                    if (skills.isEmpty()) {
                        line = name + ";" + locality + ";" + region + ";";
                    } else {
                        line = name + ";" + locality + ";" + region + ";" + skills;
                    }
                    saveToFile(line);
                    doneCounter++;

                    Thread.sleep(randomizeNumber(12, 16) * 1000);
                    if (doneCounter > 40) {
                        System.out.println("DZemke se utne");
                        Thread.sleep(randomizeNumber(15, 1 * 60) * 1000);
                        doneCounter = 0;
                    }
                }else{
                    System.out.println("user istnieje w bazie");

                    saveToFile(";;;");
                    addUserFriendToDatabase(personLink.userLink,personLink.friendLink);
                }
            }
        } catch(HttpStatusException e){
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception " + e.getMessage());
        }
    }

    private void saveToFile(String line) {
        System.out.println(line);
        PrintWriter output = null;
        try {
            output = new PrintWriter(new FileWriter("out3.txt", true));
            output.println(line);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private int randomizeNumber(int min,int max){
        Random rand = new Random();
        int randomInt = min + rand.nextInt(max);
        return randomInt;
    }
    private String getFileName(String url){
        String splitedUrl[] = url.split("/");
        return splitedUrl[splitedUrl.length-1];

    }
    private void downloadPage(Document doc,String htmlName) throws Exception {
        String html = doc.html();
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("/home/rafal/RAFAL/Programowanie/bigData/goldenlineFiles/"+htmlName), "utf-8"));
            writer.write(html);
        } catch (IOException ex) {
            // report
        } finally {
            try {writer.close();} catch (Exception ex) {/*ignore*/}
        }
    }
    private class LinkPair{
        private String userLink;
        private String friendLink;

        private LinkPair(String userLink, String friendLink) {
            this.friendLink = friendLink;
            this.userLink = userLink;
        }
    }

}
