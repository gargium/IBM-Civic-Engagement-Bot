package com.ringfulhealth.bots.facebook;

import com.ringfulhealth.bots.Constants;
import com.ringfulhealth.bots.DataManager;
import com.ringfulhealth.bots.NewsItem;
import com.ringfulhealth.bots.User;
import com.ringfulhealth.bots.UserQuery;
import com.ringfulhealth.chatbotbook.facebook.BaseServlet;
// bluemix imports
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.EntityManagerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;

import javax.net.ssl.HttpsURLConnection;

import org.json.*;



// import org.apache.http.*;
// import org.apache.http.client.methods.HttpGet;
// import org.apache.http.impl.client.DefaultHttpClient;
// import org.apache.http.util.EntityUtils;


public class NewsServlet extends BaseServlet {

    public static final String HELP_MESSAGE = "I'm here to help you get involved in local politics. Here are some sample queries you can make:\n Where is my polling location?\n Who are my representatives?\n How do I contact my representatives?\n I want to know about recent bills.\n Tell me about a candidate or political figure.";
    public static final String IRRELEVANT_MESSAGE = "Sorry, I could not understand that question. Please type \"help\" to find out what you can ask me!";
    public static final String LOCATION_UPDATED_MESSAGE = "Your location was updated!";
    public static final String ASK_FOR_LOCATION_MESSAGE = "Please enter your address first, and retry your query.";
    public static final String CLEAR_LOCATION_MESSAGE = "Your location was cleared!";


    // context maps an integer (senderId -- each user has its own id) to a Map<String, Object>
    // representing that user's context.
    private Map<String,Object> context;
    private String google_api_key;

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

        context = new HashMap<String, Object>();
        google_api_key = "AIzaSyBOsJffpfrjizZFced5a4CuwSjXc2NRgAc";
    }

    private EntityManagerFactory emf;

    public void initMyContext(String senderId) {
      Map<String, Object> myContextMap = new HashMap<String, Object>();
      myContextMap.put("location", "");
      myContextMap.put("lastAction", "");
      context.put(senderId, myContextMap);
    }

    public String handleMultipartAction(String lastAction, String userInput) {
      String out = "";
      switch (lastAction) {
        case "/polling-location":
          out = getPollingLocation(userInput);
          break;
        case "/representatives":
          break;
        case "/contact-reps":
          break;
        default:
          break;
      }
      return out;
    }

    public String getRepresentatives(String location) {
      String address = location;
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
      return "Sorry, no representative information could be found for your location.";
    }

    public String getRepsContactInfo(String location) {
      String address = location;
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
              JSONArray channels = (JSONArray) firstRep.getJSONArray("channels");
              String channelString = "";
              for(int j = 0; j < channels.length(); j++){
                  JSONObject firstChannel = (JSONObject) channels.get(j);
                  String nextPunc = ", ";
                  if(j == (channels.length() - 1)){
                      nextPunc = "\n";
                  }
                  channelString += firstChannel.getString("type") + " - " + firstChannel.getString("id") + nextPunc;
              }
              String nextPunc = "\n";
              if(i == (representativesInfo.length() - 1)){
                  nextPunc = "";
              }
              profile += firstRep.getString("name") + ": \nWebsite - " + firstRep.getString("candidateUrl") + ", " + channelString + nextPunc;
          }

          String responseToHuman = profile;

          return responseToHuman;
      }
      catch (Exception e) {
          e.printStackTrace();
      }
      //print result
      // System.out.println(response.toString());
      return "Sorry, no representatives' contact information could be found for your location.";

    }

    public String getPollingLocation(String location) {
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


      String address = location;
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
          System.out.println("got the pollinglocations object");
          

          //pollingAddress
          JSONObject addressInfoObj = (JSONObject) pollingLocations.get(0);
          System.out.println("got the addressinfoobj object");

          JSONObject addressInfo = (JSONObject) addressInfoObj.getJSONObject("address");
          System.out.println("got the addressinfo object");

          String locationName = "";
          if (addressInfo.has("locationName"))
            locationName = addressInfo.getString("locationName");
        
          String line1 = addressInfo.getString("line1");
          String city = addressInfo.getString("city");
          String state = addressInfo.getString("state");
          String zip = addressInfo.getString("zip");

          //String name = representativesInfo.get(0).getString("name");
          //JSONObject firstRep = (JSONObject) representativesInfo.get(0);

          System.out.println("name:" + locationName);

          //polling notes:
          // JSONObject pollingNotesObj = (JSONObject) addressInfoObj.getJSONObject("notes");
          String pollingNotes = "";
          if (addressInfoObj.isNull("notes"))
            pollingNotes = addressInfoObj.getString("notes");
          
          System.out.println("polling notes:" + pollingNotes);

          //polling hours:
          // JSONObject pollingHoursObj = (JSONObject) addressInfoObj.getJSONObject("pollingHours");
          String pollingHours = "";
          if (addressInfoObj.isNull("pollingHours"))
            pollingHours = addressInfoObj.getString("pollingHours");

          System.out.println("polling hours: " + pollingHours);

          if (locationName != "") {
            locationName += ", located on";
          }

          if (pollingNotes != "") {
            pollingNotes = "Note: " + pollingNotes + ".";
          }

          if (pollingHours != "") {
            pollingHours = "The hours are " + pollingHours + ".";            
          }

          String addressName = locationName + ",";
          String responseToHuman = "Your polling location is " + locationName + line1 + ", " + city + ", " + state + " " + zip + ". " + pollingHours + pollingNotes;

          return responseToHuman;
      }
      catch (Exception e) {
          e.printStackTrace();
      }
      //print result
      // System.out.println(response.toString());
      return "Sorry, no polling location was found near your address.";
    }

    public String getBillsForIssue(String issue) {
       String encodedUrl = null;

       try {
           encodedUrl = URLEncoder.encode(issue, "UTF-8");
       } catch (UnsupportedEncodingException ignored) {
           // Can be safely ignored because UTF-8 is always supported
       }

       //https://congress.api.sunlightfoundation.com/bills/search?query=%22health%20care%22&history.active=true&order=last_action_at
       //send it to civic information api
       String urlString = "https://congress.api.sunlightfoundation.com/bills/search?query=" + issue + "&history.active=true&order=last_action_at";
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

           JSONArray billResults = obj.getJSONArray("results");
           int arraySize = billResults.length();
           int max = (5 < billResults.length()) ? 5 : billResults.length();

           //billInfo
           ArrayList<String> bills = new ArrayList<String>();
           for (int i = 0; i < max; i++) {
               JSONObject billInfoObj = (JSONObject) billResults.get(i);
               String title = billInfoObj.getString("short_title");
               if (title == null || title == "null") {
                   title = billInfoObj.getString("popular_title");
                   if (title == null || title == "null") {
                       title = billInfoObj.getString("official_title");
                   }
               }
               StringBuilder sb = new StringBuilder(title);
               StringBuilder formattedTitle = sb.insert(0, "Title " + (i+1) + ": ");
               bills.add(formattedTitle.toString());
           }

           String responseToHuman = "";
           for (String b : bills) {
               responseToHuman += (b + "\n\n");
           }
           responseToHuman += "\n\nIf you would like to know more about any of these, please type in name of the bill as follows: 'Tell me about name_of_bill'";

           return responseToHuman;
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         return "";
    }

    public String getInfoAbout(String query) {
      String [] splitQuery = query.split(" on ");
      if (splitQuery.length < 2) {
        splitQuery = query.split(" about ");
      }

      for (String s: splitQuery) {
        System.out.print(s);
      }

      String candidate = splitQuery[1];
      candidate = candidate.replace(" ", "%20");

      String encodedUrl = null;

      try {
        encodedUrl = URLEncoder.encode(candidate, "UTF-8");
      } catch (UnsupportedEncodingException ignored) {
        // Can be safely ignored because UTF-8 is always supported
      }

      String urlString = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&format=json&exintro=&titles=" + candidate + "&redirects=1";

      System.out.print(urlString);
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
          System.out.println(inputLine);
        }
        in.close();

        JSONTokener tokener = new JSONTokener(response.toString());
        JSONObject obj = new JSONObject(tokener);

        JSONObject result = obj.getJSONObject("query").getJSONObject("pages");

        String key = "";
        for(Iterator iter = result.keys(); iter.hasNext();) {
          key = (String) iter.next();
        }

        String extract = result.getJSONObject(key).getString("extract");
        String noHTMLString = extract.replaceAll("\\<.*?>","");

        String responseToHuman = noHTMLString.substring(0, Math.min(noHTMLString.length(), 637));

        return responseToHuman + "...";
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      return "Sorry, I couldn't find any information on that person.";
    }

    public Object converse (String human, ConcurrentHashMap<String, Object> myContext) {
        System.out.println("IBMdWServlet converse: " + human);
        System.out.println("myContext: " + myContext.toString());

        String senderId = myContext.get("sender_id").toString();

        // init user's context if it doesn't exist
        if (!context.containsKey(senderId)) {
          initMyContext(senderId);
        }

        // bluemix nlp
        ConversationService service = new ConversationService("2017-05-26");
        // username & password from workspace
        service.setUsernameAndPassword("705e632d-341d-45be-9363-f98e30207ed3", "3Ozmm3P4SrjG");

        // send input string to Conversation API and receive response
        MessageRequest newMessage = new MessageRequest.Builder().inputText(human).context(context).build();
        MessageResponse mResponse = service.message("05801e3a-dbd2-4f03-98d9-a2303b02ab55", newMessage).execute();
        List<String> responses = mResponse.getText();
        String message = responses.get(0);
        context = mResponse.getContext();
        System.out.println("context from msg: " + context.toString());
        System.out.println("responses: " + responses.toString());
        System.out.println("message: " + message);

        Map<String, Object> conversationContext = (Map<String, Object>) context.get(senderId);

        String conversationOutput = "";
        switch(message) {
          case "/help":
            conversationOutput = HELP_MESSAGE;
            conversationContext.put("location","");
            conversationContext.put("lastAction", "/help");
            break;
          case "/polling-location":
            // if location has been saved, use that to get polling location. Otherwise, output generic message
            // asking user for location.
            if (conversationContext.get("location").toString().length() == 0) {
              conversationOutput = ASK_FOR_LOCATION_MESSAGE;
            }
            else {
              conversationOutput = getPollingLocation(conversationContext.get("location").toString());
            }
            conversationContext.put("lastAction", "/polling-location");
            break;
          case "/representatives":
            if (conversationContext.get("location").toString().length() == 0) {
              conversationOutput = ASK_FOR_LOCATION_MESSAGE;
            }
            else {
              conversationOutput = getRepresentatives(conversationContext.get("location").toString());
            }
            conversationContext.put("lastAction", "/representatives");
            break;
          case "/contact-reps":
          case "/donate-reps":
            if (conversationContext.get("location").toString().length() == 0) {
              conversationOutput = ASK_FOR_LOCATION_MESSAGE;
            }
            else {
              conversationOutput = getRepsContactInfo(conversationContext.get("location").toString());
            }
            conversationContext.put("lastAction", "/contact-reps");
            break;
          case "/bills":
            conversationOutput = message;
            break;
          case "/bill-issue":
            String billIssue = human;
            conversationOutput = getBillsForIssue(billIssue);
            break;
          case "/location": // request interpreted to be a location
            String lastAction = "";
            if (conversationContext.containsKey("lastAction"))
              lastAction = conversationContext.get("lastAction").toString();
            conversationContext.put("location", human);
            if (conversationOutput.length() == 0) {
              conversationOutput = LOCATION_UPDATED_MESSAGE;
            }
            break;
          case "/clear-location":
            conversationContext.put("location", "");
            conversationContext.put("lastAction", "/clear-location");
            conversationOutput = CLEAR_LOCATION_MESSAGE;
            break;
          case "/information":
            if (human.toLowerCase().contains("tell me about") || human.contains("more information") ||
                              human.contains("information about") || human.contains("information on") ||
                              human.contains("info about") || human.contains("info on")) {
              conversationOutput = getInfoAbout(human);
            }
            else {
              // don't set conversationOutput here, so it'll be handled by the Whoops case.
            }
            break;
          case "/irrelevant":
            conversationOutput = IRRELEVANT_MESSAGE;
            break;
          default:
            conversationOutput = message;
            break;
        }
        if (conversationOutput.length() > 0) {
          return conversationOutput;
        }
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
