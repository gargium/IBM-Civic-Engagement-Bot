# IBM Civic Engagement Bot

A friendlier way to get informed. Inspired by dwnewsbot tutorial on IBM's website. 

## Getting Started 

### File(s) of Interest 

/src/main/java/com/ringfulhealth/bots/facebook/NewsServlet.java : Use this when trying to respond to any of the user's inputs. 

More will be added as we touch those files. 

### Building and Running

Prerequisites: 
* CloudFoundry CLI
* BlueMix CLI
* JDK + JVM
* Maven

To build and run:


1.  `$mvn clean package` from the root project directory
2. `$cf api "https://api.ng.bluemix.net"` You'll find the data it asks you for on BlueMix. 
3. `$cf login`
4. `$cf push`

At this point you're done. You might see it repeating "0 of 1 started...", you can go ahead and terminate that after the first one or two times it does it. 

5. OPTIONAL: `$cf apps` to check out if the instance is running or not. 

Now switch over to BlueMix. Make sure that the instance is running. Click the Endpoint URL underneath the instance name and if a page loads, you're good. 

On Facebook, you can message the page. Be sure to wait up to 5 seconds for a response upon initializing the chat. 

### NOTE

Do NOT push code up to CloudFoundry unless you are certain that others aren't working on it. I don't want to deal with merge issues on a stack I know nothing about. 

Pushing to Github whenever is fine. Create branches for your own features and make pull reequests back into master so we can test everything on Mondays/Tuesdays before Sprint Meetings. 

