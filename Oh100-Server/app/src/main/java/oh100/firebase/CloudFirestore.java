package oh100.firebase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteBatch;
import com.google.firebase.cloud.FirestoreClient;

import oh100.solved.User;

public class CloudFirestore {
    private static Firestore db = null;

    static void init()
    {
        if(Firebase.isInitialized && db == null)
            db = FirestoreClient.getFirestore();
    }

    public static Firestore getDB()
    {
        return db;
    }

    public static HashSet<String> getAllUserHandle()
    {
        HashSet<String> allUserHandle = new HashSet<String>();

        if(db != null) {
            try {
                ApiFuture<QuerySnapshot> future = db.collection("users").get();
                
                List<QueryDocumentSnapshot> users = future.get().getDocuments();
                
                for (DocumentSnapshot user : users) {
                    allUserHandle.add(user.getId());
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        return allUserHandle;
    }

    public static long getUserCount(String userHandle)
    {
        long userCount = 0;

        if(db != null) {
            try {
                DocumentReference docRef = db.collection("users").document(userHandle);
                
                ApiFuture<DocumentSnapshot> future = docRef.get();
                
                DocumentSnapshot document = future.get();
                
                userCount = (long) document.getData().get("solved_count");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        return userCount;
    }

    public static String getUserToken(String userHandle)
    {
        String token = null;

        if(db != null) {
            try {
                DocumentReference docRef = db.collection("users").document(userHandle);
                
                ApiFuture<DocumentSnapshot> future = docRef.get();
                
                DocumentSnapshot document = future.get();
                
                token = (String) document.getData().get("token");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        return token;
    }

    public static Set<String> getUserFriendsHandle(String userHandle)
    {
        Set<String> allUserFriendsHandle = null;

        if(db != null) {
            try {
                DocumentReference docRef = db.collection("friends_count").document(userHandle);
                
                ApiFuture<DocumentSnapshot> future = docRef.get();
                
                DocumentSnapshot document = future.get();
                
                allUserFriendsHandle = document.getData().keySet();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        return allUserFriendsHandle;
    }

    public static Map<String, Object> getUserFriends(String userHandle)
    {
        Map<String, Object> allUserFriends = null;

        if(db != null) {
            try {
                DocumentReference docRef = db.collection("friends_count").document(userHandle);
                
                ApiFuture<DocumentSnapshot> future = docRef.get();
                
                DocumentSnapshot document = future.get();
                
                allUserFriends = document.getData();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        return allUserFriends;
    }

    public static void addUser(String userHandle)
    {
        User user = new User(userHandle);

        Map<String, Object> data = new HashMap<>();
        data.put("solved_count", user.getSolvedCount());

        Map<String, Object> tempData = new HashMap<>();
        tempData.put("temp_user", 0);

        try {
            db.collection("users").document(userHandle).set(data).get();
            db.collection("friends_count").document(userHandle).set(tempData).get();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void addUserFriend(String userHandle, String friendHandle)
    {
        User friend = new User(friendHandle);

        Map<String, Object> updates = new HashMap<>();
        updates.put(friendHandle, friend.getSolvedCount());

        try {
            db.collection("friends_count").document(userHandle).set(updates, SetOptions.merge()).get();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteUser(String userHandle)
    {
        try {
            db.collection("users").document(userHandle).delete();
            db.collection("friends_count").document(userHandle).delete().get();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteUserFriend(String userHandle, String friendHandle)
    {
        DocumentReference docRef = db.collection("friends_count").document(userHandle);

        Map<String, Object> updates = new HashMap<>();
        updates.put(friendHandle, FieldValue.delete());
        
        try {
            docRef.update(updates).get();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateAllFriendsCount()
    {
        WriteBatch batch = db.batch();
        
        try {
            ApiFuture<QuerySnapshot> future = db.collection("friends_count").get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                Set<String> friendsHandle = document.getData().keySet();

                for(String handle : friendsHandle) {
                    User friend = new User(handle);

                    Map<String, Object> data = new HashMap<>();
                    data.put(handle, friend.getSolvedCount());

                    batch.update(document.getReference(), data);
                }
            }

            future = db.collection("users").get();
            
            documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                User user = new User(document.getId());

                Map<String, Object> data = new HashMap<>();
                data.put("solved_count", user.getSolvedCount());

                batch.update(document.getReference(), data);
            }

            batch.commit().get();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void debugUserFriend(String userHandle, String friendHandle, int count)
    {
        try {
            DocumentReference docRef = db.collection("friends_count").document(userHandle);

            ApiFuture<DocumentSnapshot> future = docRef.get();
                    
            DocumentSnapshot document = future.get();

            Map<String, Object> updates = new HashMap<>();
            updates.put(friendHandle, (long) document.getData().get(friendHandle) + count);
            
            docRef.update(updates);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
