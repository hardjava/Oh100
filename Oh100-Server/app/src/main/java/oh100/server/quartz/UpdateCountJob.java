package oh100.server.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import oh100.firebase.CloudFirestore;

public class UpdateCountJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        CloudFirestore.updateAllFriendsCount();
    }
}
