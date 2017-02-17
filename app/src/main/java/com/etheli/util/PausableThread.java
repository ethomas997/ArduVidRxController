//PausableThread.java:  Defines a thread with wait/notify/terminate
//                      functionality that may also be paused.
//
// 12/30/2016 -- [ET]
//

package com.etheli.util;

/**
 * Class PausableThread defines a thread with wait/notify/terminate
 * functionality that may also be paused.  Subclasses should use the
 * 'waitForNotify()' method to perform thread-sleep delays.
 */
public class PausableThread extends Thread
{
    /** Thread state:  Not yet started. */
  public static int STATE_NOT_STARTED = 0;
    /** Thread state:  Running (not waiting or paused). */
  public static int STATE_RUNNING = 1;
    /** Thread state:  Waiting for notify. */
  public static int STATE_WAITING = 2;
    /** Thread state:  Paused (may or may not be waiting for notify also). */
  public static int STATE_PAUSED = 3;
    /** Thread state:  Terminated. */
  public static int STATE_TERMINATED = 4;
    /** Thread state:  Resumed (after pause). */
  public static int STATE_RESUMED = 5;

    /** The default terminate wait time in milliseconds. */
  public static int DEFAULT_TERMINATE_WAIT_TIME = 100;
    /** Thread synchronization object used for 'wait' and 'notify'. */
  private final Object threadWaitSyncObject = new Object();
    /** Flag set true after 'notifyThread()' method called. */
  private boolean threadWaitNotifyFlag = false;
    /** Thread-synchronization-lock object for state change. */
  protected final Object curThreadStateSyncObj = new Object();
    /** Thread state. */
  protected int currentThreadState = STATE_NOT_STARTED;

  /**
   * Allocates a new PausableThread object. This constructor has
   * the same effect as PausableThread(null, target, name).
   * @param   target   the object whose run method is called.
   * @param   name     the name of the new thread.
   */
  public PausableThread(Runnable target, String name)
  {
    this(null, target, name);
  }

  /**
   * Allocates a new PausableThread object. This constructor has
   * the same effect as PausableThread(null, null, name).
   * @param   name   the name of the new thread.
   */
  public PausableThread(String name)
  {
    this(null, null, name);
  }

  /**
   * Allocates a new PausableThread object so that it has
   * target as its run object, has the specified
   * name as its name, and belongs to the thread group
   * referred to by group.
   * If group is null, the group is
   * set to be the same ThreadGroup as
   * the thread that is creating the new thread.
   * If there is a security manager, its checkAccess
   * method is called with the ThreadGroup as its argument.
   * This may result in a SecurityException.
   * If the target argument is not null, the
   * run method of the target is called when
   * this thread is started. If the target argument is
   * null, this thread's run method is called
   * when this thread is started.
   * The priority of the newly created thread is normally set equal to the
   * priority of the thread creating it, that is, the currently running
   * thread. If the thread is created on the event dispatch thread the
   * priority of the newly created thread is set to the normal priority.
   * The method setPriority may be used to
   * change the priority to a new value.
   * The newly created thread is initially marked as being a daemon
   * thread if and only if the thread creating it is currently marked
   * as a daemon thread. The method setDaemon  may be used
   * to change whether or not a thread is a daemon.
   * @param      group     the thread group.
   * @param      target   the object whose run method is called.
   * @param      name     the name of the new thread.
   * @exception  SecurityException  if the current thread cannot create a
   *               thread in the specified thread group.
   */
  public PausableThread(ThreadGroup group, Runnable target, String name)
  {
    super(group, target, name);
  }

  /**
   * Allocates a new PausableThread object. This constructor has
   * the same effect as PausableThread(group, null, name)
   * @param      group   the thread group.
   * @param      name    the name of the new thread.
   * @exception  SecurityException  if the current thread cannot create a
   *             thread in the specified thread group.
   */
  public PausableThread(ThreadGroup group, String name)
  {
    this(group, null, name);
  }

  /**
   * Starts thread (should only be done once).
   */
  public void start()
  {
    synchronized(curThreadStateSyncObj)
    {  //grab thread lock for state change
      if(currentThreadState == STATE_NOT_STARTED)     //if state "not started"
        currentThreadState = STATE_RUNNING;           //set new state
    }
    super.start();
  }

  /**
   * Performs a thread-notify and terminates this thread if the thread
   * is not terminated and alive.
   */
  public void terminate()
  {
    terminate(DEFAULT_TERMINATE_WAIT_TIME);
  }

  /**
   * Performs a thread-notify and terminates this thread if the thread
   * is not terminated and alive.
   * @param waitTimeMs the maximum number of milliseconds to wait for
   * the thread to terminate, or 0 to wait indefinitely.
   */
  public void terminate(long waitTimeMs)
  {
    synchronized(curThreadStateSyncObj)
    {  //grab thread lock for state change
      if(currentThreadState == STATE_TERMINATED ||
                      currentThreadState == STATE_NOT_STARTED || !isAlive())
      {  //thread already terminated, not started, or not alive
        return;
      }
      currentThreadState = STATE_TERMINATED;     //set new state
      curThreadStateSyncObj.notifyAll();         //notify state change
    }
    notifyThread();                    //wake up 'waitforNotify()' method
    waitForTerminate();                //wait for thread to terminate
    if(isAlive())                           //if thread is still alive then
      terminateWithInterrupt(waitTimeMs);   //call terminate with 'interrupt'
  }

  /**
   * Terminates this thread if the thread is not terminated and alive.
   * The 'interrupt()' method is also called to interrupt any 'sleep()'
   * in progress.
   */
  public void terminateWithInterrupt()
  {
    terminateWithInterrupt(DEFAULT_TERMINATE_WAIT_TIME);
  }

  /**
   * Terminates this thread if the thread is not terminated and alive.
   * The 'interrupt()' method is also called to interrupt any 'sleep()'
   * in progress.
   * @param waitTimeMs the maximum number of milliseconds to wait for
   * the thread to terminate, or 0 to wait indefinitely.
   */
  public void terminateWithInterrupt(long waitTimeMs)
  {
    synchronized(curThreadStateSyncObj)
    {  //grab thread lock for state change
      if(currentThreadState == STATE_TERMINATED ||
                      currentThreadState == STATE_NOT_STARTED || !isAlive())
      {  //thread already terminated, not started, or not alive
        return;
      }
      currentThreadState = STATE_TERMINATED;     //set new state
      curThreadStateSyncObj.notifyAll();         //notify state change
    }
    notifyThread();                    //wake up 'waitforNotify()' method
    try { interrupt(); }               //interrupt sleep
    catch(SecurityException ex) {}
    waitForTerminate();                //wait for thread to terminate
  }

  /**
   * Tests if this thread is terminated.
   * @return  true if this thread is terminated;
   *          false otherwise.
   */
  public boolean isTerminated()
  {
    return (currentThreadState == STATE_TERMINATED);
  }

  /**
   * Performs a thread wait.
   * If a notify has occurred since the last call to 'waitForNotify()'
   * or 'clearThreadWaitNotifyFlag()' then this method will return
   * immediately.
   */
  public void waitForNotify()
  {
    waitForNotify(0);
  }

  /**
   * Performs a thread wait, up to the given timeout value.
   * If a notify has occurred since the last call to 'waitForNotify()'
   * or 'clearThreadWaitNotifyFlag()' then this method will return
   * immediately.
   * @param waitTimeMs the maximum number of milliseconds to wait for
   * the thread-notify, or 0 to wait indefinitely.
   * @return true if the wait-timeout value was reached; false if a
   * thread-notify, thread-interrupt or thread-terminate occurred.
   */
  public boolean waitForNotify(long waitTimeMs)
  {
    boolean retFlag = false;
    try
    {
      synchronized(curThreadStateSyncObj)
      {  //grab thread lock for state change
        if(currentThreadState == STATE_TERMINATED)    //if terminated then
          return false;                               //return immediately
        currentThreadState = STATE_WAITING;      //set new state
        curThreadStateSyncObj.notifyAll();       //notify state change
      }
      synchronized(threadWaitSyncObject)
      {  //grab thread synchronization lock object
        if(!threadWaitNotifyFlag)
        {   //'notifyThread()' method not already called
                   //wait until specified time, notify or interrupt:
          threadWaitSyncObject.wait(waitTimeMs);
          if(!threadWaitNotifyFlag)  //if notify was not called then
            retFlag = true;          //indicate thread-wait finished
        }
        threadWaitNotifyFlag = false;
      }
    }
    catch(InterruptedException ex)
    {    //thread was interrupted
    }
    synchronized(curThreadStateSyncObj)
    {  //grab thread lock for state change
      if(currentThreadState == STATE_TERMINATED)
        return false;        //if terminated then return with not-finished flag
      if(retFlag)
      {  //thread-wait timeout was reached
        while(currentThreadState == STATE_PAUSED)
        {  //thread is in the paused state
          try
          {     //wait until thread resumed or terminated
            curThreadStateSyncObj.wait();
          }
          catch(InterruptedException ex)
          {  //thread was interrupted
          }
          if(currentThreadState == STATE_TERMINATED)
            return false;    //if terminated then return with not-finished flag
        }
      }
      currentThreadState = STATE_RUNNING;        //set new state
      curThreadStateSyncObj.notifyAll();         //notify state change
    }
    return retFlag;
  }

  /**
   * Performs a thread-notify.  This will "wake up" the 'waitForNotify()'
   * method.
   */
  public void notifyThread()
  {
    synchronized(threadWaitSyncObject)
    {    //grab thread synchronization lock object
      threadWaitNotifyFlag = true;          //indicate notify called
      threadWaitSyncObject.notifyAll();     //notify on object
    }
  }

  /**
   * Clears the thread-wait notify flag.  This will clear any previous
   * "notifies" set via the 'notifyThread()' method.
   */
  public void clearThreadWaitNotifyFlag()
  {
    synchronized(threadWaitSyncObject)
    {
      threadWaitNotifyFlag = false;
    }
  }

  /**
   * Determines if the thread was already started.
   * @return true if the thread was already started, false otherwise.
   */
  public boolean wasStarted()
  {
    return (currentThreadState != STATE_NOT_STARTED);
  }

  /**
   * Waits (up to 100 milliseconds) for the thread to terminate.
   */
  public void waitForTerminate()
  {
    waitForTerminate(DEFAULT_TERMINATE_WAIT_TIME);
  }

  /**
   * Waits (up to 'waitTimeMs' milliseconds) for the thread to terminate.
   * @param waitTimeMs the maximum number of milliseconds to wait for
   * the thread to terminate, or 0 to wait indefinitely.
   */
  public void waitForTerminate(long waitTimeMs)
  {
    try { join(waitTimeMs); }  //wait for thread to terminate
    catch(InterruptedException ex) {}
  }

  /**
   * Pauses the thread.
   * @param waitTimeMs the maximum number of milliseconds to wait for
   * the thread to pause, or 0 to return immediately.
   * @return false if a timeout occurred before the thread-pause could
   * be confirmed; true otherwise.
   */
  public boolean pauseThread(long waitTimeMs)
  {
    synchronized(curThreadStateSyncObj)
    {  //grab thread lock for state change
      if(currentThreadState == STATE_PAUSED)     //if already paused then
        return true;                             //just return
      if(currentThreadState == STATE_WAITING)
      {  //thread is in 'waitForNotify()' method
        currentThreadState = STATE_PAUSED;       //set new state
        return true;
      }
      if(currentThreadState == STATE_TERMINATED || currentThreadState == STATE_NOT_STARTED)
        return false;        //if terminated or not started then just return
      final int lastThreadState = currentThreadState;
      do
      {       //wait for change in thread state
        try
        {
          curThreadStateSyncObj.wait(waitTimeMs);
        }
        catch(InterruptedException ex)
        {
        }
      }
      while(currentThreadState == lastThreadState);
      if(currentThreadState == STATE_WAITING)
      {  //thread is in 'waitForNotify()' method
        currentThreadState = STATE_PAUSED;       //set new state
        return true;
      }
    }
    return false;       //indicate didn't enter paused state
  }

  /**
   * Resumes execution of the paused thread.  Has no effect if thread
   * not paused.
   */
  public void resumeThread()
  {
    synchronized(curThreadStateSyncObj)
    {  //grab thread lock for state change
      if(currentThreadState != STATE_TERMINATED && currentThreadState != STATE_WAITING)
      {  //thread is not terminating or in the 'waitForNotify()' method
        currentThreadState = STATE_RESUMED;      //cancel thread pause
        curThreadStateSyncObj.notifyAll();       //notify state change
      }
    }
  }

  /**
   * Returns an indicator of whether is not the thread is paused.
   * @return true if paused; false if not.
   */
  public boolean isThreadPaused()
  {
    return (currentThreadState == STATE_PAUSED);
  }

  /**
   * Returns an indicator of whether is not the thread is waiting
   * (currently in the 'waitForNotify()' method).  If the thread
   * is paused then this method will return false;
   * @return true if waiting; false if not.
   */
  public boolean isThreadWaiting()
  {
    return (currentThreadState == STATE_WAITING);
  }

  /**
   * This method is deprecated because it will not be interrupted by
   * the 'notifyThread()' method.  The 'waitForNotify()' method should
   * be used instead for thread-sleep delays.
   * Causes the currently executing thread to sleep (temporarily cease
   * execution) for the specified number of milliseconds. The thread
   * does not lose ownership of any monitors.
   * @param      millis   the length of time to sleep in milliseconds.
   * @exception  InterruptedException if another thread has interrupted
   *             the current thread.  The <i>interrupted status</i> of the
   *             current thread is cleared when this exception is thrown.
   * @see        java.lang.Object#notify()
   * @deprecated Use 'waitForNotify()' instead.
   */
  public static void sleep(long millis) throws InterruptedException
  {
    Thread.sleep(millis);
  }
}
