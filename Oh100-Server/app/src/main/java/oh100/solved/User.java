package oh100.solved;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

public class User {
    private JSONObject user = null;

    public User(String handle)
    {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://solved.ac/api/v3/user/show?handle=" + handle))
                .header("x-solvedac-language", "")
                .header("Accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if(!response.body().equals("Not Found"))
                user = new JSONObject(response.body());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getHandle()
    {
        return user.getString("handle");
    }

    public String getProfileImageUrl()
    {
        return user.getString("profileImageUrl");
    }

    public int getSolvedCount()
    {
        return user.getInt("solvedCount");
    }

    public int getTier()
    {
        return user.getInt("tier");
    }
}
