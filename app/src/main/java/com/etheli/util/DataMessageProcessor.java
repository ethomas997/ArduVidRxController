//DataMessageProcessor.java:  Defines a message processor that decouples the
//                            sending and handling of data messages.
//
//  5/15/2017 -- [ET]
//

package com.etheli.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Class DataMessageProcessor defines a message processor that decouples the
 * sending and handling of data messages.  Data messages are passed into this
 * processor via the 'DataMsgSenderInterface' methods and dispatched to the
 * 'DataMsgHandlerInterface' object given to the constructor.
 */
public class DataMessageProcessor extends Thread implements DataMsgSenderInterface
{
  private final DataMsgHandlerInterface dataMsgHandlerObj;
  private boolean wasStartedFlag = false;
  private Handler looperHandlerObj = null;
  private Looper threadLoopObj = null;

  /**
   * Creates a data-message processor.
   * @param msgHandlerObj data-message handler object that will receive messages.
   */
  public DataMessageProcessor(DataMsgHandlerInterface msgHandlerObj)
  {
    super("DataMessageProcessor");
    dataMsgHandlerObj = msgHandlerObj;
  }

  /**
   * Sets up and runs the processor for message handling.
   */
  public void run()
  {
    wasStartedFlag = true;
    Looper.prepare();
    looperHandlerObj = new Handler()
        {        //pass messages to data-message-handler object
          @Override
          public void handleMessage(Message msgObj)
          {
            try
            {
              dataMsgHandlerObj.handleDataMessage(msgObj.what,msgObj.arg1,msgObj.arg2,
                             ((msgObj.obj instanceof String) ? ((String)(msgObj.obj)) : null));
            }
            catch(Exception ex)
            {
              System.err.println("DataMessageProcessor exception:  " + ex);
              ex.printStackTrace();
            }
          }
        };
    threadLoopObj = Looper.myLooper();    //save handle to looper obj
    Looper.loop();         //run message-handling loop
  }

  /**
   * Determines if the thread was previously started.
   * @return true if the thread was previously started; false if not.
   */
  public boolean wasStarted()
  {
    return wasStartedFlag;
  }

  /**
   * Terminates the send-processing thread.
   */
  public void quitProcessing()
  {
    if(threadLoopObj != null)
      threadLoopObj.quit();
  }

  /**
   * Sends message with the given data item.
   * @param msgCode message code.
   */
  public void sendMessage(int msgCode)
  {
    if(looperHandlerObj != null)
      looperHandlerObj.obtainMessage(msgCode).sendToTarget();
  }

  /**
   * Sends message with the given data items.
   * @param msgCode message code.
   * @param paramStr parameter string for message.
   */
  public void sendMessage(int msgCode, String paramStr)
  {
    if(looperHandlerObj != null)
      looperHandlerObj.obtainMessage(msgCode,paramStr).sendToTarget();
  }

  /**
   * Sends message with the given data items.
   * @param msgCode message code.
   * @param val integer value for message.
   */
  public void sendMessage(int msgCode, int val)
  {
    if(looperHandlerObj != null)
      looperHandlerObj.obtainMessage(msgCode,val,0).sendToTarget();
  }

  /**
   * Sends message with the given data items.
   * @param msgCode message code.
   * @param val1 first integer value for message.
   * @param val2 second integer value for message.
   * @param paramStr parameter string for message.
   */
  public void sendMessage(int msgCode, int val1, int val2, String paramStr)
  {
    if(looperHandlerObj != null)
      looperHandlerObj.obtainMessage(msgCode,val1,val2,paramStr).sendToTarget();
  }
}
