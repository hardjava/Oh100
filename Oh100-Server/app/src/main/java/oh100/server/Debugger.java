package oh100.server;

import java.util.Set;

import oh100.firebase.CloudFirestore;
import oh100.firebase.CloudMessaging;
import oh100.util.StringLoader;

public class Debugger {
    public static void forceFriendsCount(String user_handle) {
        Set<String> friends = CloudFirestore.getUserFriendsHandle(user_handle);

        for(String friend : friends)
            CloudFirestore.debugUserFriend(user_handle, friend, -1);
    }

    public static void forceUserMessage(String user_handle) {
        StringLoader stringLoader = new StringLoader();
        
        String token = CloudFirestore.getUserToken(user_handle);

        CloudMessaging.sendMessage(token, stringLoader.getProperty("user_message_title"), stringLoader.getProperty("user_message_body"));
    }

    public static void forceFriendsMessage(String user_handle) {
        StringLoader stringLoader = new StringLoader();
        
        String token = CloudFirestore.getUserToken(user_handle);

        CloudMessaging.sendMessage(token, stringLoader.getProperty("friend_message_title"), stringLoader.getProperty("friend_message_body"));
    }
}
