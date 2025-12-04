package oh100.server;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import oh100.firebase.*;
import oh100.server.quartz.*;

public class App {
    private final static String VERSION = "240605";

    public static OutputStreamWriter osw = null;
    public static BufferedWriter bw = null;
    public static Scanner sc = null;

    private final static int MIDNIGHT_HOUR = 0;
    private final static int MIDNIGHT_MINUTE = 0;
    private final static int EVENING_HOUR = 20;
    private final static int EVENING_MINUTE = 0;
    private final static int CHECKING_INTERVAL = 1;

    public static void main(String[] args) throws Exception {
        osw = new OutputStreamWriter(System.out);
        bw = new BufferedWriter(osw);
        sc = new Scanner(System.in);

        bw.write("==============================\n");
        bw.write("Oh100 Mini Server v" + VERSION + '\n');
        bw.write("==============================\n");
        
        bw.write("Firebase server connecting...\n\n");

        bw.flush();

        Firebase.init();
        
        bw.write("\nConnecting success!\n");

        bw.write("\nServer job start\n");
        
        bw.write("==============================\n");

        Scheduler scheduler = null;

        try {
            JobDetail updateCountJob = JobBuilder.newJob(UpdateCountJob.class)
                    .withIdentity("updateCountJob", "dailyGroup")
                    .build();

            JobDetail checkUserCountJob = JobBuilder.newJob(CheckUserCountJob.class)
                    .withIdentity("checkUserCountJob", "dailyGroup")
                    .build();

            JobDetail checkFriendsCountJob = JobBuilder.newJob(CheckFriendsCountJob.class)
                    .withIdentity("checkFriendsCountJob", "hourlyGroup")
                    .build();

            Trigger midnight_trigger = TriggerBuilder.newTrigger()
                    .withIdentity("midnightTrigger", "dailyGroup")
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(MIDNIGHT_HOUR, MIDNIGHT_MINUTE))
                    .build();
                
            Trigger evening_trigger = TriggerBuilder.newTrigger()
                    .withIdentity("eveningTrigger", "dailyGroup")
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(EVENING_HOUR, EVENING_MINUTE))
                    .build();

            Trigger hourly_trigger = TriggerBuilder.newTrigger()
                    .withIdentity("hourlyTrigger", "hourlyGroup")
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInHours(CHECKING_INTERVAL)
                    .repeatForever()) 
                    .build();

            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(updateCountJob, midnight_trigger);
            scheduler.scheduleJob(checkUserCountJob, evening_trigger);
            scheduler.scheduleJob(checkFriendsCountJob, hourly_trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }

        while(true) {
            int menu = getMenu();
            int flag = 0;

            switch(menu) {
            case 1 :
                debug();
                
                break;
            case 2:
                flag = 1;
                
                break;
            }

            if(flag == 1)
                break;
        }

        try {
            scheduler.shutdown();
        } catch (SchedulerException se) {
            se.printStackTrace();
        }

        bw.write("Server terminated\n");
        bw.flush();

        bw.close();
        osw.close();
    }

    private static int getMenu() throws Exception
    {
        int menu = 0;

        while (true) {
                try {
                bw.write("1. Debug mode\n");
                bw.write("2. Terminate server\n");
                bw.write("> ");
                bw.flush();

                menu = sc.nextInt();

                if(menu > 2 || menu < 1)
                    throw new InputMismatchException();
                else
                    break;
            } catch(InputMismatchException e) {
                bw.write("Wrong number...\n");
                bw.write("==============================\n");
                bw.flush();

                sc.nextLine();
            }
        }

        bw.write("==============================\n");
        bw.flush();

        return menu;
    }

    private static void debug() throws Exception
    {
        int menu = 0;

        while (true) {
            try {
                bw.write("1. Force Friends count\n");
                bw.write("2. Force User Message\n");
                bw.write("3. Force Friends Message\n");
                bw.write("> ");
                bw.flush();

                menu = sc.nextInt();

                if(menu > 3 || menu < 1)
                    throw new InputMismatchException();
                else
                    break;
            } catch(InputMismatchException e) {
                bw.write("Wrong number...\n");
                bw.write("==============================\n");
                bw.flush();

                sc.nextLine();
            }
        }

        bw.write("==============================\n");
        bw.write("Input users handle > ");
        bw.flush();

        String user_handle = sc.nextLine();
        
        if(user_handle.equals(""))
            user_handle = sc.nextLine();

        switch(menu) {
        case 1:
            Debugger.forceFriendsCount(user_handle);

            break;
        case 2:
            Debugger.forceUserMessage(user_handle);

            break;  
        case 3:
            Debugger.forceFriendsMessage(user_handle);

            break;
        }

        bw.write("\nSuccess!\n");
        bw.write("==============================\n");
        bw.flush();
    }
}
