//PausableWorker.java:  Defines a background-worker thread with
//                      wait/notify/terminate functionality that
//                      may also be paused.
//
// 12/30/2016 -- [ET]
//

package com.etheli.util;

/**
 * Class PausableWorker defines a background-worker thread with
 * wait/notify/terminate functionality that may also be paused.
 * The executing method for the worker runs as a periodic task
 * at a set interval.
 */
public abstract class PausableWorker extends PausableThread
{
  protected final long periodicIntervalDelayMs;

  /**
   * Creates a background-worker thread.
   * @param threadNameStr thread name.
   * @param periodicDelayMs delay to be executed between intervals.
   */
  public PausableWorker(String threadNameStr, long periodicDelayMs)
  {
    super(threadNameStr);
    periodicIntervalDelayMs = periodicDelayMs;
  }

  /**
   * Task method to be invoked at each interval.
   * @return true if the worker should continue running;
   * false if the worker should terminate.
   */
  public abstract boolean doWorkerTask();

  /**
   * Executing method for worker thread.  (Called automatically.)
   */
  public void run()
  {
    while(!isTerminated())
    {  //loop if thread not terminated
      if(!doWorkerTask())         //run worker task
        break;               //if false returned then terminate
      waitForNotify(periodicIntervalDelayMs);    //do delay and/or pause
    }
  }


//Test code.
//  public static void main(String args [])
//  {
//    final PausableWorker pausableWorkerObj = new PausableWorker("workerTestThread",25)
//        {
//          protected int workerCounter = 0;
//          public boolean doWorkerTask()
//          {
//            if(++workerCounter % 10 == 0)
//              System.out.println("  worker-task-count=" + workerCounter);
//            final long timeMs = System.currentTimeMillis();
//            while(System.currentTimeMillis() < timeMs + 7);
//            return true;
//          }
//        };
//    System.out.println("Starting worker thread");
//    pausableWorkerObj.start();
//    try { Thread.sleep(2000); }
//    catch(InterruptedException ex) {}
//
//    System.out.println("Pausing worker thread");
//    if(!pausableWorkerObj.pauseThread(1000))
//      System.out.println("***Unable to pause worker thread***");
//    System.out.println("Waiting while worker thread paused");
//    try { Thread.sleep(2000); }
//    catch(InterruptedException ex) {}
//
//    System.out.println("Resuming worker thread");
//    pausableWorkerObj.resumeThread();
//    try { Thread.sleep(1000); }
//    catch(InterruptedException ex) {}
//
//    System.out.println("Starting doing lots of pauses and resumes");
//    for(int i=0; i<50; ++i)
//    {
//      try { Thread.sleep(37); }
//      catch(InterruptedException ex) {}
//      if(!pausableWorkerObj.pauseThread(1000))
//        System.out.println("***Unable to pause worker thread***");
//      try { Thread.sleep(51); }
//      catch(InterruptedException ex) {}
//      pausableWorkerObj.resumeThread();
//    }
//    System.out.println("Finished doing lots of pauses and resumes");
//
//    try { Thread.sleep(2000); }
//    catch(InterruptedException ex) {}
//    System.out.println("Terminating worker thread");
//    pausableWorkerObj.terminate();
//    System.out.println("pausableWorkerObj.isAlive()=" + pausableWorkerObj.isAlive());
//  }

}
