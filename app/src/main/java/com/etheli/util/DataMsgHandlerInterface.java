//DataMsgHandlerInterface.java:  Defines method for a message handler.
//
//  5/15/2017 -- [ET]
//

package com.etheli.util;

/**
 * Interface DataMsgHandlerInterface defines a method for a message handler.
 */

public interface DataMsgHandlerInterface
{
  /**
   * Handles message with the given data items.
   * @param msgCode message code.
   * @param val1 first integer value for message.
   * @param val2 second integer value for message.
   * @param paramStr parameter string for message.
   * @return true if the message was recognized and handled; false if not.
   */
  public boolean handleDataMessage(int msgCode, int val1, int val2, String paramStr);
}
