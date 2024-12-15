package io.github.classroomorg.team27;
import schedulerAlertApp.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ){
        SchedulerInterface scheduler = new SchedulerInterface(new Register(), new Scheduler());
    }
}
