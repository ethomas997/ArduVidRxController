//DataMsgSenderInterface.java:  Defines methods for a message sender.
//
//  5/15/2017 -- [ET]
//

package com.etheli.util;

/**
 * Interface DataMsgSenderInterface defines methods for a message sender.
 */
public interface DataMsgSenderInterface
{
  /**
   * Determines if the processing thread was previously started.
   * @return true if the processing thread was previously started;
   * false if not.
   */
  public boolean wasStarted();

  /**
   * Terminates the message-processing thread.
   */
  public void quitProcessing();

  /**
   * Sends message with the given data item.
   * @param msgCode message code.
   */
  public void sendMessage(int msgCode);

  /**
   * Sends message with the given data items.
   * @param msgCode message code.
   * @param paramStr parameter string for message.
   */
  public void sendMessage(int msgCode, String paramStr);

  /**
   * Sends message with the given data items.
   * @param msgCode message code.
   * @param val integer value for message.
   */
  public void sendMessage(int msgCode, int val);

  /**
   * Sends message with the given data items.
   * @param msgCode message code.
   * @param val1 first integer value for message.
   * @param val2 second integer value for message.
   * @param paramStr parameter string for message.
   */
  public void sendMessage(int msgCode, int val1, int val2, String paramStr);

}
