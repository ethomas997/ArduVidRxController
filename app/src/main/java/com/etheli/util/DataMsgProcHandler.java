//DataMsgProcHandler.java:  Defines an Android Handler that implements the
//                          methods in DataMsgSenderInterface.
//
//  5/16/2017 -- [ET]
//

package com.etheli.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Class DataMsgProcHandler defines an Android Handler that implements the
 * methods in DataMsgSenderInterface.
 */
public class DataMsgProcHandler extends Handler implements DataMsgSenderInterface
{
  /**
   * Creates a handler that implements the methods in DataMsgSenderInterface.
   */
  public DataMsgProcHandler()
  {
  }

  /**
   * Creates a handler that implements the methods in DataMsgSenderInterface.
   * @param looperObj Looper to use with the handler.
   */
  public DataMsgProcHandler(Looper looperObj)
  {
    super(looperObj);
  }

  /**
   * Determines if the processing thread was previously started.
   * @return Always returns true in this class.
   */
  public boolean wasStarted()
  {
    return true;
  }

  /**
   * Method does not function in this class.
   */
  public void quitProcessing()
  {
  }

  /**
   * Sends message with the given data item.
   * @param msgCode message code.
   */
  public void sendMessage(int msgCode)
  {
    obtainMessage(msgCode).sendToTarget();
  }

  /**
   * Sends message with the given data items.
   * @param msgCode message code.
   * @param paramStr parameter string for message.
   */
  public void sendMessage(int msgCode, String paramStr)
  {
    obtainMessage(msgCode,paramStr).sendToTarget();
  }

  /**
   * Sends message with the given data items.
   * @param msgCode message code.
   * @param val integer value for message.
   */
  public void sendMessage(int msgCode, int val)
  {
    obtainMessage(msgCode,val,0).sendToTarget();
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
    obtainMessage(msgCode,val1,val2,paramStr).sendToTarget();
  }
}
