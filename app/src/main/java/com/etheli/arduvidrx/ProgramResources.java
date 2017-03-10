//ProgramResources.java:  Global program resources singleton.
//
//   3/8/2017 -- [ET]
//

package com.etheli.arduvidrx;

import android.os.Handler;
import android.os.Message;

/**
 * Class ProgramResources defines a global program resources singleton.
 */
public class ProgramResources
{
    /** Main-GUI update handler:  Version information. */
  public static final int MAINGUI_UPD_VERSION = 1;
    /** Main-GUI update handler:  Channel and RSSI values. */
  public static final int MAINGUI_UPD_CHANRSSI = 2;
    /** Main-GUI update handler:  Set value for freqCodeTextView. */
  public static final int MAINGUI_UPD_CHANTEXT = 3;
    /** Main-GUI update handler:  Show popup message. */
  public static final int MAINGUI_UPD_POPUPMSG = 4;
    /** Main-GUI update handler:  Video receiver started; enable button, etc. */
  public static final int MAINGUI_UPD_VRMGRSTARTED = 5;
    /** Main-GUI update handler:  Video receiver scanning started. */
  public static final int MAINGUI_UPD_SCANBEGIN = 6;
    /** Main-GUI update handler:  Video receiver scanning finished. */
  public static final int MAINGUI_UPD_SCANEND = 7;
    /** Main-GUI update handler:  Show select-channel choice dialog. */
  public static final int MAINGUI_UPD_SELCHANNEL = 8;

    /** Terminal state:  Started. */
  public static final int TERMINAL_STATE_STARTED = 1;
    /** Terminal state:  Stopped. */
  public static final int TERMINAL_STATE_STOPPED = 2;

  private static ProgramResources programResourcesObj = null;
  private BluetoothSerialService bluetoothSerialServiceObj = null;
  private VidReceiverManager vidReceiverManagerObj = null;
  private FrequencyTable videoFrequencyTableObj = null;
  private Handler terminalMsgHandlerObj = null;
  private DataWriteReceiver terminalWriteRecvrObj = null;
  private Handler terminalStateHandlerObj = null;

  /**
   * Returns the ProgramResources object, creating it if needed.
   * @return A ProgramResources object.
   */
  public static ProgramResources getProgramResourcesObj()
  {
    if(programResourcesObj == null)
      programResourcesObj = new ProgramResources();
    return programResourcesObj;
  }

  /**
   * Sets the BluetoothSerialService object.
   * @param serviceObj BluetoothSerialService object.
   */
  public void setBluetoothSerialServiceObj(BluetoothSerialService serviceObj)
  {
    bluetoothSerialServiceObj = serviceObj;
  }

  /**
   * Returns the BluetoothSerialService object.
   * @return The BluetoothSerialService object, or null if none set.
   */
  public BluetoothSerialService getBluetoothSerialServiceObj()
  {
    return bluetoothSerialServiceObj;
  }

  /**
   * Returns the VidReceiverManager object.
   * @return The VidReceiverManager object, or null if none set.
   */
  public VidReceiverManager getVidReceiverManagerObj()
  {
    return vidReceiverManagerObj;
  }

  /**
   * Sets the VidReceiverManager object.
   * @param mgrObj VidReceiverManager object.
   */
  public void setVidReceiverManagerObj(VidReceiverManager mgrObj)
  {
    vidReceiverManagerObj = mgrObj;
  }

  /**
   * Returns the FrequencyTable object.
   * @return The FrequencyTable object, or null if none set.
   */
  public FrequencyTable getFrequencyTableObj()
  {
    return videoFrequencyTableObj;
  }

  /**
   * Sets the FrequencyTable object.
   * @param tableObj FrequencyTable object.
   */
  public void setFrequencyTableObj(FrequencyTable tableObj)
  {
    videoFrequencyTableObj = tableObj;
  }

  /**
   * Sets the terminal-message handler object.  The handler will receive
   * messages from the bluetooth-serial service.
   * @param handlerObj terminal-message handler object.
   */
  public void setTerminalMsgHandlerObj(Handler handlerObj)
  {
    terminalMsgHandlerObj = handlerObj;
  }

  /**
   * Returns the terminal-message handler object.  The handler will receive
   * messages from the bluetooth-serial service.
   * @return The terminal-message handler object, or null if none set.
   */
  public Handler getTerminalMsgHandlerObj()
  {
    return terminalMsgHandlerObj;
  }

  /**
   * Invokes the terminal-message handler object (if set).  The handler
   * will receive messages from the bluetooth-serial service.
   * @param msgObj message for handler.
   */
  public void invokeTerminalMsgHandler(Message msgObj)
  {
    if(terminalMsgHandlerObj != null)
      terminalMsgHandlerObj.handleMessage(msgObj);
  }

  /**
   * Sets the terminal write-receiver object.  The object receives data
   * directly from the bluetooth-serial service.
   * @param recObj terminal write-receiver object.
   */
  public void setTerminalWriteRecvrObj(DataWriteReceiver recObj)
  {
    terminalWriteRecvrObj = recObj;
  }

  /**
   * Returns the terminal write-receiver object.  The object receives data
   * directly from the bluetooth-serial service.
   * @return The terminal write-receiver object, or null if none set.
   */
  public DataWriteReceiver getTerminalWriteRecvrObj()
  {
    return terminalWriteRecvrObj;
  }

  /**
   * Invokes the terminal write-receiver object (if set).  The object
   * receives data directly from the bluetooth-serial service.
   * @param buffer message data.
   * @param length size of message data.
   */
  public void invokeTerminalWriteRecvr(byte[] buffer, int length)
  {
    if(terminalWriteRecvrObj != null)
      terminalWriteRecvrObj.write(buffer,length);
  }

  /**
   * Determines if the terminal window is active.
   * @return true if the terminal window is active, false if not.
   */
  public boolean isTerminalActive()
  {
    return (terminalMsgHandlerObj != null && terminalWriteRecvrObj != null);
  }

  /**
   * Sets the terminal-state-change handler object.
   * @param handlerObj terminal-state-change handler object.
   */
  public void setTerminalStateHandlerObj(Handler handlerObj)
  {
    terminalStateHandlerObj = handlerObj;
  }

  /**
   * Returns the terminal-state-change handler object.
   * @return The terminal-state-change handler object, or null if none set.
   */
  public Handler getTerminalStateHandlerObj()
  {
    return terminalStateHandlerObj;
  }

  /**
   * Sets the terminal-state value to be passed via the
   * terminal-state-change handler.
   * @param stateVal on of the 'TERMINAL_STATE_...' values.
   */
  public void setTerminalStateValue(int stateVal)
  {
    if(terminalStateHandlerObj != null)
      terminalStateHandlerObj.obtainMessage(stateVal).sendToTarget();
  }
}
