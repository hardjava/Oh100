package oh100.firebase;

import java.util.HashSet;
import java.util.Map;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import oh100.solved.User;
import oh100.util.StringLoader;

public class CloudMessaging {
    public static void sendMessage(String token, String title, String body)
    {
        Message message = Message.builder()
            .putData("title", title)
            .putData("body", body)
            .setToken(token)
            .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkCounts()
    {
        StringLoader stringLoader = new StringLoader();

        HashSet<String> user_handles = CloudFirestore.getAllUserHandle();

        for(String handle : user_handles) {
            long prev_count = CloudFirestore.getUserCount(handle);
            String user_token = CloudFirestore.getUserToken(handle);

            User user = new User(handle);

            long cur_count = user.getSolvedCount();

            if(prev_count == cur_count)
                sendMessage(user_token, stringLoader.getProperty("user_message_title"), stringLoader.getProperty("user_message_body"));
        }
    }
    
    public static void checkFriends()
    {
        StringLoader stringLoader = new StringLoader();

        HashSet<String> user_handles = CloudFirestore.getAllUserHandle();

        for(String handle : user_handles) {
            String user_token = CloudFirestore.getUserToken(handle);
            Map<String, Object> friends = CloudFirestore.getUserFriends(handle);

            int flag = 0;

            for(Map.Entry<String, Object> friend : friends.entrySet()) {
                User user = new User(friend.getKey());

                long prev_count = (long) friend.getValue();
                long cur_count = user.getSolvedCount();

                // 이전 count보다 현재 count가 작은 경우는 없다 가정.
                if(prev_count != cur_count) {
                    sendMessage(user_token, stringLoader.getProperty("friend_message_title"), stringLoader.getProperty("friend_message_body"));

                    flag = 1;

                    break;
                }
            }

            if(flag == 1)
                break;
        }
    }
}
