package com.ringfulhealth.bots.facebook;

import com.ringfulhealth.bots.Constants;
import com.ringfulhealth.bots.DataManager;
import com.ringfulhealth.bots.NewsItem;
import com.ringfulhealth.bots.User;
import com.ringfulhealth.chatbotbook.facebook.BaseServlet;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.EntityManagerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.*;
import java.net.*;

import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;

import org.json.*;



// import org.apache.http.*;
// import org.apache.http.client.methods.HttpGet;
// import org.apache.http.impl.client.DefaultHttpClient;
// import org.apache.http.util.EntityUtils;


public class NewsServlet extends BaseServlet {

    public NewsServlet () {
        page_access_token = Constants.fb_page_access_token;

        zencoder_apikey = Constants.zencoder_apikey;
        audio_bluemix_username = Constants.audio_bluemix_username;
        audio_bluemix_password = Constants.audio_bluemix_password;

        nlp_bluemix_username = Constants.nlp_bluemix_username;
        nlp_bluemix_password = Constants.nlp_bluemix_password;
        nlp_bluemix_id = Constants.nlp_bluemix_id;

        conv_bluemix_username = Constants.conv_bluemix_username;
        conv_bluemix_password = Constants.conv_bluemix_password;
        conv_bluemix_id = Constants.conv_bluemix_id;
    }

    private EntityManagerFactory emf;

    public Object converse (String human, ConcurrentHashMap<String, Object> context) {
        String google_api_key = "AIzaSyBOsJffpfrjizZFced5a4CuwSjXc2NRgAc";

        System.out.println("IBMdWServlet converse: " + human);

        // if (emf == null) {
        //     // This is for Tomcat
        //     emf = (EntityManagerFactory) getServletContext().getAttribute("emf");
        // }
        // DataManager dm = new DataManager (emf);

        // boolean new_user = false;
        // User user = dm.getFbUser((String) context.get("sender_id"));
        // if (user == null) {
        //     user = new User ();
        //     user.setFbId((String) context.get("sender_id"));

        //     HashMap profile = getUserProfile(user.getFbId());
        //     if (profile != null && !profile.isEmpty()) {
        //         user.setFirst_name((String) profile.get("first_name"));
        //         user.setLast_name((String) profile.get("last_name"));
        //         user.setProfile_pic((String) profile.get("profile_pic"));
        //         user.setLocale((String) profile.get("locale"));
        //         user.setGender((String) profile.get("gender"));
        //         try {
        //             user.setTimezone((Integer) profile.get("timezone"));
        //         } catch (Exception e) {
        //             // This one does not exist
        //             user.setTimezone(0);
        //         }
        //     }

        //     dm.saveUser(user);
        //     new_user = true;
        // }
        // List <String> faves = user.getFavesList();

        // if (human.equalsIgnoreCase("stop")) {
        //     user.setStopped(1);
        //     dm.saveUser(user);
        //     context.remove("search");
        //     return "I have stopped your news delivery. You can still get dW news by initiating a conversation with me.";
        // }

        // if (human.equalsIgnoreCase("resume")) {
        //     user.setStopped(0);
        //     dm.saveUser(user);
        //     context.remove("search");
        //     return "I have resumed your news delivery.";
        // }

        // if (human.equalsIgnoreCase("RANDOM-TOPICS")) {
        //     faves = new ArrayList<String>();
        //     List<String> allfaves = Arrays.asList("big data", "bluemix", "bpm", "commerce", "cognitive", "iot", "java", "linux", "mobile", "open source", "security", "SOA", "web", "XML", "cloud");
        //     Random rand = new Random ();
        //     for (int i = 0; i < 3; i++) {
        //         int index = rand.nextInt(allfaves.size());
        //         faves.add(allfaves.get(index));
        //         allfaves.remove(index);
        //     }

        //     user.setFaves(String.join(",", faves));
        //     dm.saveUser(user);

        //     try {
        //         JSONObject payload = new JSONObject();
        //         payload.put("template_type", "button");
        //         payload.put("text", "Great, I selected the following topics for you: \"" + user.getFaves() + "\".");
        //         String[] button_titles = {"I want to change", "Great, show me!"};
        //         String[] button_payloads = {"TOPICS", "NEWS"};
        //         payload.put("buttons", createButtons(button_titles, button_payloads));
        //         return payload;
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        // }

        // if (context.get("search") != null) {
        //     List <NewsItem> items = dm.searchNewsItems(human);
        //     context.put("items", items);
        //     context.remove("search");

        //     List replies = new ArrayList();
        //     replies.add ("Search results for: " + human);
        //     replies.add(dm.replyFbItems(items, true));
        //     return replies;
        // }

        // // Pattern ptn = Pattern.compile("((change)|(update)|(modify)).*topic", Pattern.CASE_INSENSITIVE);
        // // Matcher match = ptn.matcher(human);
        // // boolean change_faves = match.find();
        // boolean change_faves = human.equalsIgnoreCase("topics");

        // if (faves == null || faves.isEmpty() || change_faves || new_user) {
        //     if (new_user) {
        //         // New user
        //         List replies = new ArrayList();
        //         replies.add("Hello " + user.getFirst_name() + "! This bot delivers developer news and articles from IBM developerWorks to you!");
        //         replies.add("To start, please reply with your interests in technical topics. Example:");
        //         replies.add("big data, bluemix, bpm, commerce, cognitive, IBM i, iot, java, linux, mobile, open source, security, service management, SOA, web, XML, cloud, industries");
        //         return replies;

        //     } else if (change_faves) {
        //         // The human input asks for changing faves
        //         user.setFaves("");
        //         dm.saveUser(user);

        //         List replies = new ArrayList();
        //         replies.add("Pls reply with your interests in technical topics. Example:");
        //         replies.add("big data, bluemix, bpm, commerce, cognitive, IBM i, iot, java, linux, mobile, open source, security, service management, SOA, web, XML, cloud, industries");
        //         return replies;

        //     } else {
        //         // The human is no longer asking for changing faves. He is giving his faves
        //         Set <String> faveset = new HashSet <String> ();
        //         Iterator it = dm.feeds.entrySet().iterator();
        //         while (it.hasNext()) {
        //             Map.Entry <String, String[]> pair = (Map.Entry)it.next();
        //             System.out.println(pair.getKey() + " = " + pair.getValue());

        //             Pattern ptn2 = Pattern.compile(pair.getValue()[0], Pattern.CASE_INSENSITIVE);
        //             Matcher match2 = ptn2.matcher(human);
        //             if (match2.find()) {
        //                 faveset.add(pair.getKey());
        //             }
        //         }
        //         /*
        //         String[] elements = human.split(",|;");
        //         for (String e : elements) {
        //             Iterator it = dm.feeds.entrySet().iterator();
        //             while (it.hasNext()) {
        //                 Map.Entry <String, String[]> pair = (Map.Entry)it.next();
        //                 System.out.println(pair.getKey() + " = " + pair.getValue());

        //                 Pattern ptn2 = Pattern.compile(pair.getValue()[0], Pattern.CASE_INSENSITIVE);
        //                 Matcher match2 = ptn2.matcher(e);
        //                 if (match2.find()) {
        //                     faveset.add(pair.getKey());
        //                     break;
        //                 }
        //             }
        //         }
        //         */
        //         faves = new ArrayList <String> (faveset);

        //         if (faves.isEmpty()) {
        //             List replies = new ArrayList();
        //             replies.add("Sorry, I do not see any available technical topic.");
        //             replies.add("Pls reply with your interests in technical topics. Example:");
        //             replies.add("big data, bluemix, bpm, commerce, cognitive, IBM i, iot, java, linux, mobile, open source, security, service management, SOA, web, XML, cloud, industries");
        //             try {
        //                 replies.add(createButtons(
        //                         "Or, alternatively, you can let me randomly pick 3 topics for you!",
        //                         new HashMap<String, String>(){{
        //                             put("Choose random", "RANDOM-TOPICS");
        //                         }}
        //                         // new String[] {"Choose random"},
        //                         // new String[] {"RANDOM-TOPICS"}
        //                 ));
        //             } catch (Exception e) {
        //                 e.printStackTrace();
        //             }
        //             return replies;

        //         } else {
        //             user.setFaves(String.join(",", faves));
        //             dm.saveUser(user);

        //             try {
        //                 return createQuickReplies(
        //                         "Great, your topics are \"" + user.getFaves() + "\".",
        //                         new HashMap<String, String>() {{
        //                             put("Oh no", "TOPICS");
        //                             put("Okay!", "NEWS");
        //                         }}
        //                         // new String[] {"I made a mistake", "This is correct!"},
        //                         // new String[] {"TOPICS", "NEWS"}
        //                 );
        //             } catch (Exception e) {
        //                 e.printStackTrace();
        //             }
        //         }
        //     }
        // }

        // System.out.println("CHECK GET TOPIC");
        // if (human.startsWith("GET-TOPIC-")) {
        //     String topic = human.substring(10);
        //     List <NewsItem> items = dm.getNewsItems(topic);
        //     context.put("items", items);

        //     List replies = new ArrayList();
        //     replies.add("Latest articles from " + topic);
        //     replies.add(dm.replyFbItems(items, false));
        //     return replies;
        // }

        // System.out.println("CHECK GET SUMMARY");
        // if (human.startsWith("GET-SUMMARY-")) {
        //     long sid = Long.parseLong(human.substring(12));
        //     final NewsItem ni = dm.getNewsItem(sid); // Needed this for access from inner class for the HashMap init

        //     List replies = new ArrayList();
        //     try {
        //         List <String> ss = splitByWord(ni.getSubtitle(), 315);
        //         for (String s : ss) {
        //             replies.add(s);
        //         }

        //         replies.add(createButtons(
        //                 "What's next?",
        //                 new HashMap<String, String>(){{
        //                     put("Read article", ni.getArticleUrl());
        //                     put(ni.getTopic(), "GET-TOPIC-" + ni.getTopic());
        //                     put("All my topics", "NEWS");
        //                 }}
        //                 // new String[] {"Read article", ni.getTopic(), "All my topics"},
        //                 // new String[] {ni.getArticleUrl(), "GET-TOPIC-" + ni.getTopic(), "NEWS"}
        //         ));
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }

        //     return replies;
        // }

        // System.out.println("CHECK NEWS");
        // if (human.equalsIgnoreCase("news")) {
        //     // SHOW FAVE TOPICS. We will always to blogs
        //     List <NewsItem> items = new ArrayList <NewsItem> ();
        //     if (!faves.contains("dW Blog")) {
        //         faves.add("dW Blog");
        //     }
        //     for (String fave : faves) {
        //         List <NewsItem> nis = dm.getNewsItems(fave);
        //         if (nis == null || nis.isEmpty()) {
        //             continue;
        //         }
        //         // Do NOT add duplicates. We will just add each article once -- based on the first topic we found it.
        //         for (NewsItem ni : nis) {
        //             boolean needToAdd = true;
        //             for (NewsItem item : items) {
        //                 if (ni.getTitle().equals(item.getTitle())) {
        //                     needToAdd = false;
        //                     break;
        //                 }
        //             }
        //             if (needToAdd) {
        //                 items.add(ni);
        //             }
        //         }
        //     }
        //     context.put("items", items);

        //     List replies = new ArrayList();
        //     replies.add ("Latest articles from topics you are interested in.");
        //     replies.add(dm.replyFbItems(items, true));
        //     return replies;
        //     // return showNews("Latest articles from topics you are interested in.", faves, dm);
        // }

        // System.out.println("CHECK NEXT ARTICLE");
        // if (human.equals("NEXT-ARTICLE")) {
        //     List <NewsItem> items = (List <NewsItem>) context.get("items");
        //     if (items == null || items.isEmpty()) {
        //         try {
        //             JSONObject payload = new JSONObject();
        //             payload.put("template_type", "button");
        //             payload.put("text", "Sorry, No more articles.");
        //             String[] button_titles = {"Articles for me", "Search"};
        //             String[] button_payloads = {"NEWS", "SEARCH"};
        //             payload.put("buttons", createButtons(button_titles, button_payloads));
        //             return payload;
        //         } catch (Exception e) {
        //             e.printStackTrace();
        //         }
        //         return "Sorry, No more articles";

        //     } else {
        //         int limit = items.size();
        //         if (limit > 8) {
        //             limit = 8;
        //         }
        //         for (int i = 0; i < limit; i++) {
        //             items.remove(0);
        //         }
        //         context.put("items", items);

        //         if (items.isEmpty()) {
        //             return "You have reached the end of the list!";
        //         } else {
        //             List replies = new ArrayList();
        //             replies.add("More articles:");
        //             replies.add(dm.replyFbItems(items, false));
        //             return replies;
        //         }
        //     }
        // }

        // System.out.println("CHECK NLP");
        // // String cstr = classifyText(human);
        // String cstr = "";
        // JSONObject json = watsonConversation(human);
        // if (json != null) {
        //     try {
        //         JSONArray intents = json.getJSONArray("intents");
        //         if (intents.getJSONObject(0).getDouble("confidence") > 0.5) {
        //             cstr = intents.getJSONObject(0).getString("intent");
        //         }
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        // }
        // System.out.println("DETECTED INTENT: " + cstr);

        // if ("HELP".equalsIgnoreCase(cstr)) {
        //     return "Hello! Here are a couple of commands to get you started: NEWS to get latest articles. TOPICS to change your interested topics; STOP to stop news delivery; and RESUME to resume news delivery.";

        // } else if ("HELLO".equalsIgnoreCase(cstr)) {
        //     try {
        //         JSONObject payload = new JSONObject();
        //         payload.put("template_type", "button");
        //         payload.put("text", "Hello! It is great meeting you! Would you like to see");
        //         String[] button_titles = {"Articles for me", "Change my interests", "Search"};
        //         String[] button_payloads = {"NEWS", "TOPICS", "SEARCH"};
        //         payload.put("buttons", createButtons(button_titles, button_payloads));
        //         return payload;
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        //     // return showNews("Hello! It is great meeting you! Below are the latest articles based on your interests.", faves, dm);

        // } else if ("TOPIC".equalsIgnoreCase(cstr)) {
        //     try {
        //         JSONObject payload = new JSONObject();
        //         payload.put("template_type", "button");
        //         payload.put("text", "Are you sure you want to update your interested topics?");
        //         String[] button_titles = {"Yes"};
        //         String[] button_payloads = {"TOPICS"};
        //         payload.put("buttons", createButtons(button_titles, button_payloads));
        //         return payload;
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }

        // } else if ("STOP".equalsIgnoreCase(cstr)) {
        //     try {
        //         JSONObject payload = new JSONObject();
        //         payload.put("template_type", "button");
        //         payload.put("text", "Are you sure you want to stop developerWorks news delivery to you?");
        //         String[] button_titles = {"Yes"};
        //         String[] button_payloads = {"STOP"};
        //         payload.put("buttons", createButtons(button_titles, button_payloads));
        //         return payload;
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }

        // } else if ("RESUME".equalsIgnoreCase(cstr)) {
        //     try {
        //         JSONObject payload = new JSONObject();
        //         payload.put("template_type", "button");
        //         payload.put("text", "Are you sure you want to resume developerWorks news delivery to you?");
        //         String[] button_titles = {"Yes"};
        //         String[] button_payloads = {"RESUME"};
        //         payload.put("buttons", createButtons(button_titles, button_payloads));
        //         return payload;
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }

        // } else if ("SEARCH".equalsIgnoreCase(cstr)) {
        //     context.put("search", true);
        //     return "Please enter your search query here. For example, you can enter \"Java Stream\"";

        // } else if ("MORE".equalsIgnoreCase(cstr)) {
        //     try {
        //         JSONObject payload = new JSONObject();
        //         payload.put("template_type", "button");
        //         payload.put("text", "Do you want to see more articles from the previous list?");
        //         String[] button_titles = {"Yes", "Search", "Help"};
        //         String[] button_payloads = {"NEXT-ARTICLE", "SEARCH", "HELP"};
        //         payload.put("buttons", createButtons(button_titles, button_payloads));
        //         return payload;
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        //     /*
        //     String topic = (String) context.get("topic");
        //     if (topic == null) {
        //         return showNews("Latest articles from topics you are interested in.", faves, dm);
        //     } else {
        //         List replies = new ArrayList();
        //         replies.add("Latest articles from " + topic);
        //         replies.add(Util.replyItems(dm.getNewsItems(topic), false));
        //         return replies;
        //     }
        //     */
        // } else {
        //     try {
        //         JSONObject payload = new JSONObject();
        //         payload.put("template_type", "button");
        //         payload.put("text", "Sorry, I cannot understand you.");
        //         String[] button_titles = {"Help me", "Articles for me", "Search"};
        //         String[] button_payloads = {"HELP", "NEWS", "SEARCH"};
        //         payload.put("buttons", createButtons(button_titles, button_payloads));
        //         return payload;
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        //     // return showNews("Sorry, I cannot understand you. Try HELP to see a menu of options. Below are the latest articles ", faves, dm);
        // }

        if (human.equalsIgnoreCase("Help")) {
            return "I'm here to help you get involved in local politics.";
        } else if (human.equalsIgnoreCase("Hi")) {
            return "Hello, citizen.";
        } else if (human.equalsIgnoreCase("Where is my polling location?")) {
            return "Pls give me your address, prefaced by 'Address:'. Here's an example: 'Address: 1234 Westwood Blvd, Los Angeles, CA 90024'";
        } else if(human.equalsIgnoreCase("Who are my representatives?")) {
            return "Pls give me your address, prefaced by 'Location:'. Here's an example: 'Location: 1234 Westwood Blvd, Los Angeles, CA 90024'";
        } else if (human.startsWith("Address") || human.startsWith("address")) {
            //get rid of the word "address:" in the human query
            String [] splitHuman = human.split(":"); 
            
            //get rid of all punctuation
            // to look like this
            // 1234 Westwood Blvd
            // Los Angeles
            // CA 90024
            // String [] commaSplitHuman = splitHuman[1].split(",");

            //looks like this now 
            //1234 Westwood Blvc
            //Los Angeles
            //CA
            // for (String s : commaSplitHuman) {
            //     s = s.trim(); 
            // }


            // //replace space with %20
            // for (String s : commaSplitHuman) {
            //     s = s.replace(" ", "%20");
            // }

            // //smoosh string[] back into a string 
            // String address = "";
            // for (String s : commaSplitHuman) {
            //     address += (s + "%20");
            // }


            String address = splitHuman[1];
            String encodedUrl = null;

            try {
                encodedUrl = URLEncoder.encode(address, "UTF-8");
            } catch (UnsupportedEncodingException ignored) {
                // Can be safely ignored because UTF-8 is always supported
            }

            //send it to google 
            String urlString = "https://www.googleapis.com/civicinfo/v2/voterinfo?key=" + google_api_key + "&address=" + encodedUrl + "&electionId=2000";
            try {
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");

                //add request header
                con.setRequestProperty("User-Agent", "Mozilla/5.0");

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                    // System.out.println(inputLine);
                }
                in.close();


                JSONTokener tokener = new JSONTokener(response.toString());
                JSONObject obj = new JSONObject(tokener);

                // JSONObject obj = new JSONObject(response);
                System.out.println("got the root object");
                System.out.println(obj);

                JSONArray pollingLocations = obj.getJSONArray("pollingLocations");
                JSONArray representatives = obj.getJSONArray("contests");

                //pollingAddress
                JSONObject addressInfoObj = (JSONObject) pollingLocations.get(0);
            

                //respresentativesInfo
                JSONObject representativesInfoObj = (JSONObject) representatives.get(0);

                JSONObject addressInfo = (JSONObject) addressInfoObj.getJSONObject("address");
                JSONArray representativesInfo = (JSONArray) representativesInfoObj.getJSONArray("candidates");
                JSONObject firstRep = (JSONObject) representativesInfo.get(0);



                String locationName = addressInfo.getString("locationName");
                String line1 = addressInfo.getString("line1");
                String city = addressInfo.getString("city");
                String state = addressInfo.getString("state");
                String zip = addressInfo.getString("zip");

                //String name = representativesInfo.get(0).getString("name");
                //JSONObject firstRep = (JSONObject) representativesInfo.get(0);

                String name = firstRep.getString("name");

                System.out.println("name:" + locationName);

                //polling notes:
                // JSONObject pollingNotesObj = (JSONObject) addressInfoObj.getJSONObject("notes");
                String pollingNotes = addressInfoObj.getString("notes");
                
                System.out.println("polling notes:" + pollingNotes);
                
                //polling hours:
                // JSONObject pollingHoursObj = (JSONObject) addressInfoObj.getJSONObject("pollingHours");
                String pollingHours = addressInfoObj.getString("pollingHours");

                System.out.println("polling hours: " + pollingHours);

                String responseToHuman = "Your polling location is " + locationName + ", located on " + line1 + ", " + city + ", " + state + " " + zip + ". " + "The hours are " + pollingHours + ". Note: " + pollingNotes + ". Your representative is " + name; 

                return responseToHuman;
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
            //print result
            // System.out.println(response.toString());
            return "heehoo";

        } else if (human.startsWith("Location") || human.startsWith("location")) {
            //get rid of the word "address:" in the human query
            String [] splitHuman = human.split(":"); 
            
            //get rid of all punctuation
            // to look like this
            // 1234 Westwood Blvd
            // Los Angeles
            // CA 90024
            // String [] commaSplitHuman = splitHuman[1].split(",");

            //looks like this now 
            //1234 Westwood Blvc
            //Los Angeles
            //CA
            // for (String s : commaSplitHuman) {
            //     s = s.trim(); 
            // }


            // //replace space with %20
            // for (String s : commaSplitHuman) {
            //     s = s.replace(" ", "%20");
            // }

            // //smoosh string[] back into a string 
            // String address = "";
            // for (String s : commaSplitHuman) {
            //     address += (s + "%20");
            // }


            String address = splitHuman[1];
            String encodedUrl = null;

            try {
                encodedUrl = URLEncoder.encode(address, "UTF-8");
            } catch (UnsupportedEncodingException ignored) {
                // Can be safely ignored because UTF-8 is always supported
            }

            //send it to google 
            String urlString = "https://www.googleapis.com/civicinfo/v2/voterinfo?key=" + google_api_key + "&address=" + encodedUrl + "&electionId=2000";
            try {
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");

                //add request header
                con.setRequestProperty("User-Agent", "Mozilla/5.0");

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                    // System.out.println(inputLine);
                }
                in.close();


                JSONTokener tokener = new JSONTokener(response.toString());
                JSONObject obj = new JSONObject(tokener);



                
                JSONArray representatives = obj.getJSONArray("contests");

                //respresentativesInfo
                JSONObject representativesInfoObj = (JSONObject) representatives.get(0);

            
                JSONArray representativesInfo = (JSONArray) representativesInfoObj.getJSONArray("candidates");
                

                String profile = "";
                for(int i = 0; i < representativesInfo.length(); i++){
                    JSONObject firstRep = (JSONObject) representativesInfo.get(i);
                    if(representativesInfo.length() == 1){
                        profile = firstRep.getString("name") + " (" + firstRep.getString("party") + ").";
                    }
                    else if (representativesInfo.length() == 2 && i == 0){
                        profile += firstRep.getString("name") + " (" + firstRep.getString("party") + ") ";
                    }
                    else if(i == (representativesInfo.length() - 1)){
                        profile += "and " + firstRep.getString("name") + " (" + firstRep.getString("party") + ").";
                    }
                    else{
                        profile += firstRep.getString("name") + " (" + firstRep.getString("party") + "), ";
                    }
                }
                



            

                String responseToHuman = "Your representatives are " + profile; 

                return responseToHuman;
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
            //print result
            // System.out.println(response.toString());
            return "heehoo";

        } 




        //         DefaultHttpClient httpClient = new DefaultHttpClient();
        //         HttpGet getRequest = new HttpGet(urlString);
        //         getRequest.addHeader("accept", "application/json");

        //         HttpResponse response = httpClient.execute(getRequest);

        //         if (response.getStatusLine().getStatusCode() != 200) {
        //             throw new RuntimeException("Failed : HTTP error code : "
        //                + response.getStatusLine().getStatusCode());
        //         }

        //         BufferedReader br = new BufferedReader(
        //                          new InputStreamReader((response.getEntity().getContent())));

        //         String output;
        //         System.out.println("Output from Server .... \n");
        //         while ((output = br.readLine()) != null) {
        //             System.out.println(output);
        //         }
        //         return "worked!";

        //         httpClient.getConnectionManager().shutdown();

        //     } 
        //     catch (ClientProtocolException e) {
        //         e.printStackTrace();
        //     } 
        //     catch (IOException e) {
        //         e.printStackTrace();
        //     }

        // }





            //     System.out.println(url);
            //     conn.setRequestMethod("GET");

            //     if (conn.getResponseCode() != 200) {

            //         return "Failed : HTTP error code : " + conn.getResponseCode();
            //     }


            //     //get a response back
            //     StringBuilder sb = new StringBuilder();
            //     BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            //     String l = null; 
            //     while ((l=br.readLine()) != null) {
            //         System.out.println(l);
            //         sb.append(l);
            //     }
            //     br.close();

            //     format the response and send it back to user
            //     return sb;
            // } catch (IOException e) {
            //     System.out.println("hehe hoohoo u made a poopoo");
            //     System.out.println("Exception: ");
            //     System.out.println(e);
            // }
        // }
        
        return "Whoops! I don't know what you just said";

    }


    public List showNews (String greeting, List <String> faves, DataManager dm) {
        // SHOW FAVE TOPICS. We will always to blogs
        List <NewsItem> items = new ArrayList <NewsItem> ();
        if (!faves.contains("dW Blog")) {
            faves.add("dW Blog");
        }
        for (String fave : faves) {
            items.addAll(dm.getNewsItems(fave));
        }
        List replies = new ArrayList();
        if (greeting.isEmpty()) {
            // Nothing
        } else {
            replies.add (greeting);
        }
        replies.add(dm.replyFbItems(items, true));
        return replies;
    }

}