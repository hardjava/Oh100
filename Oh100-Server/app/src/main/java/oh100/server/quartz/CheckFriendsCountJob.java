package oh100.server.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import oh100.firebase.CloudMessaging;

public class CheckFriendsCountJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        CloudMessaging.checkFriends();
    }
}
