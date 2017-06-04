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

    public static final String HELP_MESSAGE = "I'm here to help you get involved in local politics. Here are some sample queries you can make:\n Where is my polling location?\n Who are my representatives?\n How do I contact my representatives?\n I want to know about recent bills.";
    private Map<String,Object> context;

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
        context.put("location", "");
    }

    private EntityManagerFactory emf;

    public String handleMultipartAction(String lastAction, String userInput) {
      String out = "";
      switch (lastAction) {
        case "/polling-location":
          context.put("location", userInput);
          out = getPollingLocation(userInput);
          break;
        case "/representatives":
          context.put("location", userInput);

          break;
        case "/contact-reps":
          context.put("location", userInput);
          break;
        default:
          break;
      }
      return out;
    }

    public String getPollingLocation(String location) {
      String google_api_key = "AIzaSyBOsJffpfrjizZFced5a4CuwSjXc2NRgAc";

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

          //pollingAddress
          JSONObject addressInfoObj = (JSONObject) pollingLocations.get(0);


          JSONObject addressInfo = (JSONObject) addressInfoObj.getJSONObject("address");




          String locationName = addressInfo.getString("locationName");
          String line1 = addressInfo.getString("line1");
          String city = addressInfo.getString("city");
          String state = addressInfo.getString("state");
          String zip = addressInfo.getString("zip");

          //String name = representativesInfo.get(0).getString("name");
          //JSONObject firstRep = (JSONObject) representativesInfo.get(0);

          System.out.println("name:" + locationName);

          //polling notes:
          // JSONObject pollingNotesObj = (JSONObject) addressInfoObj.getJSONObject("notes");
          String pollingNotes = addressInfoObj.getString("notes");

          System.out.println("polling notes:" + pollingNotes);

          //polling hours:
          // JSONObject pollingHoursObj = (JSONObject) addressInfoObj.getJSONObject("pollingHours");
          String pollingHours = addressInfoObj.getString("pollingHours");

          System.out.println("polling hours: " + pollingHours);

          String responseToHuman = "Your polling location is " + locationName + ", located on " + line1 + ", " + city + ", " + state + " " + zip + ". " + "The hours are " + pollingHours + ". Note: " + pollingNotes + ".";

          return responseToHuman;
      }
      catch (Exception e) {
          e.printStackTrace();
      }
      //print result
      // System.out.println(response.toString());
      return "Sorry! It appears that was an invalid request!";
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
           responseToHuman += "\n\nIf you would like to know more about any of these, please type in name of the bill as follows: 'Summary: name_of_bill'";

           return responseToHuman;
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         return "";
    }

    public Object converse (String human, ConcurrentHashMap<String, Object> myContext) {
        String google_api_key = "AIzaSyBOsJffpfrjizZFced5a4CuwSjXc2NRgAc";

        System.out.println("IBMdWServlet converse: " + human);
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



        // TODO: change println to return once all functionality is complete to transition to Conversation API
        String conversationOutput = "";
        switch(message) {
          case "/help":
            conversationOutput = HELP_MESSAGE;
            context.put("location","");
            context.put("lastAction", "/help");
            break;
          case "/polling-location":
            // if location has been saved, use that to get polling location. Otherwise, output generic message
            // asking user for location.
            if (context.get("location").toString().length() == 0) {
              conversationOutput = "Please enter your address.";
            }
            else {
              conversationOutput = getPollingLocation(context.get("location").toString());
            }
            context.put("lastAction", "/polling-location");
            break;
          case "representatives":
            break;
          case "contact-reps":
            break;
          case "/bills":
            conversationOutput = message;
            break;
          case "/bill-issue":
            String billIssue = human;
            conversationOutput = getBillsForIssue(billIssue);
            break;
          case "/location": // request interpreted to be a location
            String lastAction = context.get("lastAction").toString();
            conversationOutput = handleMultipartAction(lastAction, human);
            break;
          default:
            context.put("location","");
            conversationOutput = message;
            break;
        }
        if (conversationOutput.length() > 0) {
          return conversationOutput;
        }


        if (human.equalsIgnoreCase("Help")) {
            return "I'm here to help you get involved in local politics. Here are some sample queries you can make:\n Where is my polling location?\n Who are my representatives?\n How do I contact my representatives?\n I want to know about recent bills.";
        } else if (human.equalsIgnoreCase("Hi")) {
            // MessageRequest newMessage = new MessageRequest.Builder().inputText("Hi").build();
            // MessageResponse response = service.message("05801e3a-dbd2-4f03-98d9-a2303b02ab55", newMessage).execute();
            // System.out.println(response);
            // return response.getTextConcatenated("|");
            //return "Hello, citizen.";
        } else if (human.equalsIgnoreCase("Where is my polling location?")) {
//        	uq.setLastQueryText(human);
//        	uq.setLastQueryType(uq.POLLING_LOCATION);
//        	dm.saveUserQuery(uq);
            return "Please give me your address, prefaced by 'Polling Location:'. For example: 'Polling Location: 1234 Westwood Blvd, Los Angeles, CA 90024'";
        } else if(human.equalsIgnoreCase("Who are my representatives?")) {
            return "Please give me your address, prefaced by 'Reps:'. For example: 'Reps: 1234 Westwood Blvd, Los Angeles, CA 90024'";
        } else if(human.equalsIgnoreCase("How do I contact my representatives?")) {
            return "Please give me your address, prefaced by 'Contact Reps:'. For example: 'Contact Reps: 1234 Westwood Blvd, Los Angeles, CA 90024'";
        } else if(human.equalsIgnoreCase("I want to know about recent bills")) {
            return "Of course. Please type in the issue you want to know about, prefaced by 'Issue:'. For example: 'Issue: health care' or 'Issue: guns'";
        }
        else if (human.startsWith("Polling Location") || human.startsWith("polling location")) {
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

                //pollingAddress
                JSONObject addressInfoObj = (JSONObject) pollingLocations.get(0);


                JSONObject addressInfo = (JSONObject) addressInfoObj.getJSONObject("address");




                String locationName = addressInfo.getString("locationName");
                String line1 = addressInfo.getString("line1");
                String city = addressInfo.getString("city");
                String state = addressInfo.getString("state");
                String zip = addressInfo.getString("zip");

                //String name = representativesInfo.get(0).getString("name");
                //JSONObject firstRep = (JSONObject) representativesInfo.get(0);

                System.out.println("name:" + locationName);

                //polling notes:
                // JSONObject pollingNotesObj = (JSONObject) addressInfoObj.getJSONObject("notes");
                String pollingNotes = addressInfoObj.getString("notes");

                System.out.println("polling notes:" + pollingNotes);

                //polling hours:
                // JSONObject pollingHoursObj = (JSONObject) addressInfoObj.getJSONObject("pollingHours");
                String pollingHours = addressInfoObj.getString("pollingHours");

                System.out.println("polling hours: " + pollingHours);

                String responseToHuman = "Your polling location is " + locationName + ", located on " + line1 + ", " + city + ", " + state + " " + zip + ". " + "The hours are " + pollingHours + ". Note: " + pollingNotes + ".";

                return responseToHuman;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            //print result
            // System.out.println(response.toString());
            return "Sorry! It appears that was an invalid request!";

        } else if (human.startsWith("Reps") || human.startsWith("reps")) {

            String [] splitHuman = human.split(":");



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

        } else if (human.startsWith("Contact Reps") || human.startsWith("contact reps")) {

            String [] splitHuman = human.split(":");

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
            return "heehoo";

        }  else if (human.startsWith("Issue") || human.startsWith("issue")) {
           String [] splitHuman = human.split(":");

            String issue = splitHuman[1];
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
                responseToHuman += "\n\nIf you would like to know more about any of these, please type in name of the bill as follows: 'Summary: name_of_bill'";

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
