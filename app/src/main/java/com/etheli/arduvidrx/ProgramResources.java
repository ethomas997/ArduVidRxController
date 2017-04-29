//ProgramResources.java:  Global program resources singleton.
//
//  4/27/2017 -- [ET]
//

package com.etheli.arduvidrx;

import android.os.Handler;

/**
 * Class ProgramResources defines a global program resources singleton.
 */
public class ProgramResources
{
    /** When screen size is smaller than this, button-text sizes will be reduced. */
  public static final int SMALL_SCREEN_LIMIT = 550;
    /** Scale value for when button-text sizes are reduced. */
  public static final float BUTTON_SMSCALE_VAL = 0.8f;

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

  private static ProgramResources programResourcesObj = null;
  private BluetoothSerialService bluetoothSerialServiceObj = null;
  private VidReceiverManager vidReceiverManagerObj = null;
  private FrequencyTable videoFrequencyTableObj = null;
  private Runnable terminalStartupActionObj = null;
  private boolean terminalActiveFlag = false;

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
   * Sets the terminal-active flag.
   * @param flagVal true if the terminal window is active, false if not.
   */
  public void setTerminalActiveFlag(boolean flagVal)
  {
    terminalActiveFlag = flagVal;
  }

  /**
   * Determines if the terminal window is active.
   * @return true if the terminal window is active, false if not.
   */
  public boolean isTerminalActive()
  {
    return terminalActiveFlag;
  }

  /**
   * Sets the terminal-startup-action object.
   * @param runnableObj action to be executed, or null for none.
   */
  public void setTerminalStartupActionObj(Runnable runnableObj)
  {
    terminalStartupActionObj = runnableObj;
  }

  /**
   * Returns the terminal-startup-action object.
   * @return The action to be executed, or null for none.
   */
  public Runnable getTerminalStartupActionObj()
  {
    return terminalStartupActionObj;
  }
}
