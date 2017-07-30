//VidReceiverManager.java:  Manages command I/O for an ArduVidRx unit.
//
//  5/16/2017 -- [ET]
//

package com.etheli.arduvidrx.rec;

import com.etheli.util.DataMessageProcessor;
import com.etheli.util.DataMsgHandlerInterface;
import com.etheli.util.DataMsgSenderInterface;
import com.etheli.util.SerialWriterInterface;
import com.etheli.util.PausableWorker;
import com.etheli.util.ULog;
import java.util.Vector;

/**
 * Class VidReceiverManager manages command I/O for an ArduVidRx unit.
 * The I/O and processing of receiver commands is decoupled from the
 * command methods (i.e., 'tuneReceiverToChannelCode()') via message
 * handlers (so the calling thread will not be blocked or delayed).
 */
public class VidReceiverManager
{
    /** Delay (ms) to be executed between update intervals. */
  public static final int UPDWKR_PERIODIC_DELAYMS = 100;
    /** Size of received-line buffer. */
  public static final int BUFF_MAX_LINES = 100;
    /** Length of time to wait for expected responses from receiver. */
  public static final int RESP_WAIT_TIMEMS = 250;
    /** String to be displayed when receiver is in 'monitor' mode. */
  public static final String MONITOR_STRING = "Monitor";
    /** Tag string for logging. */
  public static final String LOG_TAG = "VidRecMgr";

    /** Video-receiver-mgr response:  Version information. */
  public static final int VRECMGR_RESP_VERSION = 1;
    /** Video-receiver-mgr response:  Channel and RSSI values. */
  public static final int VRECMGR_RESP_CHANRSSI = 2;
    /** Video-receiver-mgr response:  Set value for freqCodeTextView. */
  public static final int VRECMGR_RESP_CHANTEXT = 3;
    /** Video-receiver-mgr response:  Error fetching version info. */
  public static final int VRECMGR_RESP_ERRFETCHVER = 4;
    /** Video-receiver-mgr response:  Error fetching channel/RSSI info. */
  public static final int VRECMGR_RESP_ERRFETCHCHR = 5;
    /** Video-receiver-mgr response:  Show popup message. */
  public static final int VRECMGR_RESP_POPUPMSG = 6;
    /** Video-receiver-mgr response:  Video receiver started; enable button, etc. */
  public static final int VRECMGR_RESP_VRMGRSTARTED = 7;
    /** Video-receiver-mgr response:  Video receiver scanning started. */
  public static final int VRECMGR_RESP_SCANBEGIN = 8;
    /** Video-receiver-mgr response:  Video receiver scanning finished. */
  public static final int VRECMGR_RESP_SCANEND = 9;
    /** Video-receiver-mgr response:  Show select-channel choice dialog. */
  public static final int VRECMGR_RESP_SELCHANNEL = 10;
    /** Video-receiver-mgr response:  Not connected (test mode). */
  public static final int VRECMGR_RESP_TESTMODE = 11;

  public static final String VIDRX_CR_STR = "\r";
  public static final byte [] VIDRX_CR_ARR = VIDRX_CR_STR.getBytes();
  public static final String VIDRX_SPACECR_STR = " \r\r";
  public static final byte [] VIDRX_SPACECR_ARR = VIDRX_SPACECR_STR.getBytes();
  public static final byte [] VIDRX_RESET_CMD = ("XZ"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_VERSION_CMD = ("V"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_ECHOOFF_CMD = ("E0"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_ECHOON_CMD = ("E1"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_REPCHRSSI_CMD = "~".getBytes();
  public static final byte [] VIDRX_AUTOTUNE_CMD = ("A"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_NEXTBAND_CMD = ("B"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_NEXTCHAN_CMD = ("C"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_PREVBAND_CMD = ("XB"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_PREVCHAN_CMD = ("XC"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_UPONEMHZ_CMD = ("U"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_DOWNONEMHZ_CMD = ("D"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_MNEXTCH_CMD = ("N"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_MPREVCH_CMD = ("P"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_MONITOR_CMD = ("M"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_CHANSCAN_CMD = ("S"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_FULLSCAN_CMD = ("F"+VIDRX_CR_STR).getBytes();
  public static final byte [] VIDRX_BANDSCAN_CMD = ("XF"+VIDRX_CR_STR).getBytes();
  public static final String VIDRX_TUNE_PRESTR = "T";
  public static final String VIDRX_MINRSSI_PRESTR = "XM";
  public static final String VIDRX_MONINTVL_PRESTR = "XI";
  public static final String VIDRX_SCANLIST_PRESTR = "L";

  private static final int VIDCMD_TUNECODE_MSGC = 0;       //codes for command messages
  private static final int VIDCMD_TUNEFREQ_MSGC = 1;       // via DataMessageProcessor
  private static final int VIDCMD_AUTOTUNE_MSGC = 2;
  private static final int VIDCMD_NEXTBAND_MSGC = 3;
  private static final int VIDCMD_NEXTCHAN_MSGC = 4;
  private static final int VIDCMD_PREVBAND_MSGC = 5;
  private static final int VIDCMD_PREVCHAN_MSGC = 6;
  private static final int VIDCMD_UPONEMHZ_MSGC = 7;
  private static final int VIDCMD_DOWNONEMHZ_MSGC = 8;
  private static final int VIDCMD_MNEXTCH_MSGC = 9;
  private static final int VIDCMD_MPREVCH_MSGC = 10;
  private static final int VIDCMD_MONITOR_MSGC = 11;
  private static final int VIDCMD_MONRESCAN_MSGC = 12;
  private static final int VIDCMD_SETMINRSSI_MSGC = 13;
  private static final int VIDCMD_SETMONINTVL_MSGC = 14;
  private static final int VIDCMD_SETSCANLIST_MSGC = 15;
  private static final int VIDCMD_CHANSCAN_MSGC = 16;
  private static final int VIDCMD_FULLSCAN_MSGC = 17;

  private static final String SCANNING_CHECK_STR = " Scanning";
  private static final int SCANNING_CHKSTR_LEN = SCANNING_CHECK_STR.length();
  private int vidrxScanInProgStrMatchPos = 0;              //position for string matching
  private boolean vidrxScanningInProgressFlag = false;     //true while receiver scanning

  private final SerialWriterInterface serialServiceWriterObj;
  private final StringBuffer receivedCharsBuffer = new StringBuffer();
  private final Vector<String> receivedLinesList = new Vector<String>();
  private char firstReceivedCharacter = '\0';
  private ChannelTracker vidChannelTrackerObj = null;
  private DataMsgSenderInterface vidRecMgrRespProcessorObj = null;
  private char receivedLinesLastEndChar = '\0';
  private ReceiverUpdateWorker receiverUpdateWorkerObj = null;
  private boolean recUpdWrkrPausedRequestedFlag = false;
  private DataMessageProcessor vidCmdMesssageProcessorObj = null;
  private boolean monitorModeActiveFlag = false;
  private int minRssiForScansValue = 30;
  private int monitorIntervalValue = 5;
  private String monitorScanListString = "";


  /**
   * Creates an ArduVidRx manager.
   * @param sWriterObj SerialWriterInterface object to use for sending
   * commands to the video receiver.
   */
  public VidReceiverManager(SerialWriterInterface sWriterObj)
  {
    serialServiceWriterObj = sWriterObj;
  }

  /**
   * Sets the channel-tracker object.
   * @param chTrackerObj tracker object for currently-tuned video-receiver channel.
   */
  public void setChannelTrackerObj(ChannelTracker chTrackerObj)
  {
    vidChannelTrackerObj = chTrackerObj;
  }

  /**
   * Sets the data-message processor object for response messages.
   * @param respMsgProcObj data-message processor for response messages.
   */
  public void setRespMsgProcessorObj(DataMsgSenderInterface respMsgProcObj)
  {
    vidRecMgrRespProcessorObj = respMsgProcObj;
  }

  /**
   * Starts the video-receiver-manager threads.
   */
  public void startManager()
  {
    recUpdWrkrPausedRequestedFlag = false;       //clear pause-requested flag
    (new Thread("vidRecMgrStartup")
        {
          public void run()
          {
            doManagerStartup();
          }
        }).start();
  }

  /**
   * Performs the work of starting the video-receiver-manager threads.
   */
  private void doManagerStartup()
  {
    try
    {
      try { Thread.sleep(250); }            //start with delay in case receiver
      catch(InterruptedException ex) {}     // needs some setup time

      if(!isReceiverSerialConnected())
      {  //not connected (test mode)
        if(vidRecMgrRespProcessorObj != null)    //respond with test-mode indicator
          vidRecMgrRespProcessorObj.sendMessage(VRECMGR_RESP_TESTMODE);
        ULog.d(LOG_TAG, "Not connected (test mode)");
        return;
      }

      ULog.d(LOG_TAG, "Began manager startup");

         //start with " <CR><CR>" in case monitor mode is in progress
         // (prepend space to make sure isn't command-repeat via <Enter>):
      outputCmdNoResponse(VIDRX_SPACECR_ARR);
      try { Thread.sleep(100); }            //bit of delay in case receiver
      catch(InterruptedException ex) {}     // sends unexpected output

      final String versionStr;
      if((versionStr=queryGetVersionInfo()) != null && versionStr.trim().length() > 0)
      {  //non-empty version string successfully fetched from receiver
        ULog.d(LOG_TAG, "Receiver version info:  " + versionStr);
        if(vidRecMgrRespProcessorObj != null)
          vidRecMgrRespProcessorObj.sendMessage(VRECMGR_RESP_VERSION,versionStr);
        getNextReceivedLine(RESP_WAIT_TIMEMS);   //receive and discard 2nd line of response
      }
      else
      {  //unable to fetch version string from receiver
        ULog.e(LOG_TAG, "Unable to fetch version info");
        if(vidRecMgrRespProcessorObj != null)         //respond with error notification
          vidRecMgrRespProcessorObj.sendMessage(VRECMGR_RESP_ERRFETCHVER);
      }
      outputReceiverEchoCommand(false);     //send echo-off command

      if(!queryReportChanRssiVals())   //do initial query/report of channel/RSSI values
      {  //query failed
        ULog.e(LOG_TAG, "Unable to fetch chan/RSSI");
        if(vidRecMgrRespProcessorObj != null)         //respond with error notification
          vidRecMgrRespProcessorObj.sendMessage(VRECMGR_RESP_ERRFETCHCHR);
      }

      if(receiverUpdateWorkerObj != null)        //if previous worker then
        receiverUpdateWorkerObj.terminate();     //stop it
      receiverUpdateWorkerObj = new ReceiverUpdateWorker();
      doReceiverUpdateWorkerStartup();

      if(vidCmdMesssageProcessorObj != null && vidCmdMesssageProcessorObj.isAlive())
        vidCmdMesssageProcessorObj.quitProcessing();  //if previous proc running then stop it
      vidCmdMesssageProcessorObj = new DataMessageProcessor(
          new DataMsgHandlerInterface()
            {
              @Override
              public boolean handleDataMessage(int msgCode, int val1, int val2, String paramStr)
              {
                return handleReceiverCommandMessage(msgCode,val1,val2,paramStr);
              }
            });
      vidCmdMesssageProcessorObj.start();        //start command-message processor
      if(vidRecMgrRespProcessorObj != null)      //send notification that manager startup is done
        vidRecMgrRespProcessorObj.sendMessage(VRECMGR_RESP_VRMGRSTARTED);
      ULog.d(LOG_TAG, "Finished manager startup");
    }
    catch(Exception ex)
    {
      ULog.e(LOG_TAG, "Exception during manager startup", ex);
    }
  }

  /**
   * Starts (or resumes) the receiver update worker.  Settings values are
   * fetched from the receiver before the startup.
   */
  private void doReceiverUpdateWorkerStartup()
  {
    try { Thread.sleep(UPDWKR_PERIODIC_DELAYMS); }
    catch(InterruptedException ex) {}       //do one interval delay sending commands
    if(recUpdWrkrPausedRequestedFlag)
    {  //method 'pauseReceiverUpdateWorker()' was called
      ULog.d(LOG_TAG, "Aborting 'doReceiverUpdateWorkerStartup()' because of worker-pause request");
      return;
    }
    if(!isReceiverSerialConnected())
    {  //receiver serial link not connected
      ULog.d(LOG_TAG, "Aborting 'doReceiverUpdateWorkerStartup()' because serial disconnected");
      return;
    }
    if(outputReceiverEchoCommand(false) == null)      //send echo-off command
    {  //no response after command (can happen while waiting for TERMINAL_STATE_STOPPED msg)
      for(int c=0; c<20; ++c)
      {  //delay and retry command
        try { Thread.sleep(UPDWKR_PERIODIC_DELAYMS); }
        catch(InterruptedException ex) {}
        if(outputReceiverEchoCommand(false) != null)
          break;        //response received OK; move on
      }
    }
    fetchMinRssiValFromReceiver();          //get/save min-RSSI-for-scans value from receiver
    fetchMonIntvlValFromReceiver();         //get/save monitor-interval value from receiver
    fetchMonScanListStrFromReceiver();      //get/save monitor/scan list from receiver
    try { Thread.sleep(UPDWKR_PERIODIC_DELAYMS); }
    catch(InterruptedException ex) {}       //do one interval delay before starting worker
    if(!recUpdWrkrPausedRequestedFlag)
    {  //method 'pauseReceiverUpdateWorker()' was not called during startup
      if(!receiverUpdateWorkerObj.isAlive())
      {  //worker thread not running
        ULog.d(LOG_TAG, "Starting receiver-update worker");
        receiverUpdateWorkerObj.start();           //start update worker
      }
      else
      {  //worker thread is running (paused)
        ULog.d(LOG_TAG, "Resuming receiver-update worker in startup");
        receiverUpdateWorkerObj.resumeThread();    //resume update worker
      }
    }
    else
    {  //method 'pauseReceiverUpdateWorker()' was called during startup
      ULog.d(LOG_TAG, "Not starting worker in 'doReceiverUpdateWorkerStartup()' " +
                                                "because of worker-pause request");
    }
  }

  /**
   * Starts up the receiver update worker (via a thread separate from the
   * caller's thread).  Settings values are fetched from the receiver
   * before the startup.
   */
  public void startupReceiverUpdateWorker()
  {
    recUpdWrkrPausedRequestedFlag = false;       //clear pause-requested flag
    (new Thread("vidUpdWorkerStartup")
        {
          public void run()
          {
            doReceiverUpdateWorkerStartup();
          }
        }).start();
  }

  /**
   * Stops the video-receiver-manager threads.
   */
  public void stopManager()
  {
    try
    {
      if(vidCmdMesssageProcessorObj != null)
        vidCmdMesssageProcessorObj.quitProcessing();  //stop command-message processor
      if(receiverUpdateWorkerObj != null)
      {  //worker was created; stop it
        ULog.d(LOG_TAG, "Stopping receiver-update worker");
        receiverUpdateWorkerObj.terminate(10);
        receiverUpdateWorkerObj = null;
      }
    }
    catch(Exception ex)
    {  //some kind of exception error
      ULog.e(LOG_TAG, "Error stopping video-receiver-manager threads", ex);
    }
  }

  /**
   * Sends command to tune receiver to given channel-code value (i.e., "F4").
   * @param chanCodeStr channel-code value (i.e., "F4").
   */
  public void tuneReceiverToChannelCode(String chanCodeStr)
  {
    vidCmdMesssageProcessorObj.sendMessage(VIDCMD_TUNECODE_MSGC,chanCodeStr);
  }

  /**
   * Sends command to tune receiver to given frequency value (in MHz).
   * @param freqVal frequency value (in MHz).
   */
  public void tuneReceiverToFrequency(int freqVal)
  {
    vidCmdMesssageProcessorObj.sendMessage(VIDCMD_TUNECODE_MSGC,Integer.toString(freqVal));
  }

  /**
   * Sends command to auto-tune receiver to strongest channel.
   */
  public void autoTuneReceiver()
  {
    vidCmdMesssageProcessorObj.sendMessage(VIDCMD_AUTOTUNE_MSGC);
  }

  /**
   * Sends command to tune receiver to next (or previous) frequency-code
   * band or channel.
   * @param bandFlag true for band; false for channel.
   * @param nextFlag true for next; false for previous.
   */
  public void tuneNextPrevBandChannel(boolean bandFlag, boolean nextFlag)
  {
    if(bandFlag)
      vidCmdMesssageProcessorObj.sendMessage(nextFlag ? VIDCMD_NEXTBAND_MSGC : VIDCMD_PREVBAND_MSGC);
    else
      vidCmdMesssageProcessorObj.sendMessage(nextFlag ? VIDCMD_NEXTCHAN_MSGC : VIDCMD_PREVCHAN_MSGC);
  }

  /**
   * Sends command to tune receiver frequency up or down by one MHz.
   * @param upFlag true for +1 MHz; false for -1 MHz.
   */
  public void tuneReceiverFreqByOneMhz(boolean upFlag)
  {
    vidCmdMesssageProcessorObj.sendMessage(upFlag ? VIDCMD_UPONEMHZ_MSGC : VIDCMD_DOWNONEMHZ_MSGC);
  }

  /**
   * Selects the next (or previous) monitored channel from amongst those that
   * have a signal on them.
   * @param nextFlag true for next; false for previous.
   */
  public void selectPrevNextMonitorChannel(boolean nextFlag)
  {
    vidCmdMesssageProcessorObj.sendMessage(nextFlag ? VIDCMD_MNEXTCH_MSGC : VIDCMD_MPREVCH_MSGC);
  }

  /**
   * Sends command to enter monitor-mode to receiver.
   */
  public void sendMonitorCmdToReceiver()
  {
    vidCmdMesssageProcessorObj.sendMessage(VIDCMD_MONITOR_MSGC);
  }

  /**
   * Sends a (re)scan command to the receiver.
   */
  public void sendMonRescanCmdToReceiver()
  {
    vidCmdMesssageProcessorObj.sendMessage(VIDCMD_MONRESCAN_MSGC);
  }

  /**
   * Sends a new minimum-RSSI-for-scans value to the receiver.
   * @param minRssiVal new minimum-RSSI-for-scans value.
   */
  public void sendMinRssiValToReceiver(int minRssiVal)
  {
    vidCmdMesssageProcessorObj.sendMessage(VIDCMD_SETMINRSSI_MSGC,minRssiVal);
  }

  /**
   * Sends a new monitor-interval value to the receiver.
   * @param intervalVal new monitor-interval value (in seconds).
   */
  public void sendMonIntvlValToReceiver(int intervalVal)
  {
    vidCmdMesssageProcessorObj.sendMessage(VIDCMD_SETMONINTVL_MSGC,intervalVal);
  }

  /**
   * Sends a new monitor/scan list to the receiver.
   * @param listStr new monitor/scan list string.
   */
  public void sendMonScanListToReceiver(String listStr)
  {
    if(isReceiverSerialConnected())
      vidCmdMesssageProcessorObj.sendMessage(VIDCMD_SETSCANLIST_MSGC,listStr);
    else                                    //if not connected (test mode) then
      monitorScanListString = listStr;      //set monitor/scan list directly
  }

  /**
   * Starts the scan-and-select-channel function.  A scan is performed,
   * and then the result is shown (with RSSI values) in a select-channel
   * dialog.
   * @param fullFlag false for channel-scan ('S'); true for full-scan ('F').
   */
  public void startScanSelectChanFunction(boolean fullFlag)
  {
    vidCmdMesssageProcessorObj.sendMessage(fullFlag ? VIDCMD_FULLSCAN_MSGC : VIDCMD_CHANSCAN_MSGC);
  }

  /**
   * Pauses the receiver-update worker (so commands can be sent to the receiver).
   * Method does not return until the pause is confirmed (or a timeout occurs).
   * @return true if the thread was not already paused; false if the thread was
   * already paused.
   */
  public boolean pauseReceiverUpdateWorker()
  {
    return pauseReceiverUpdateWorker(false);
  }

  /**
   * Pauses the receiver-update worker (so commands can be sent to the receiver).
   * @param retImmedFlag true to return immediately; false to wait until the
   * pause is confirmed (or a timeout occurs).
   * @return true if the thread was not already paused; false if the thread was
   * already paused.
   */
  public boolean pauseReceiverUpdateWorker(boolean retImmedFlag)
  {
    recUpdWrkrPausedRequestedFlag = true;        //indicate pause requested
    if(receiverUpdateWorkerObj != null && !receiverUpdateWorkerObj.isThreadPaused())
    {
//      ULog.d(LOG_TAG, "Pausing receiver-update worker");
      receiverUpdateWorkerObj.pauseThread(retImmedFlag ? 0 : 1000);
      return true;
    }
    return false;
  }

  /**
   * Resumes the receiver-update worker.
   */
  private void resumeReceiverUpdateWorker()
  {
    recUpdWrkrPausedRequestedFlag = false;       //clear pause-requested flag
    if(receiverUpdateWorkerObj != null)
    {
//      ULog.d(LOG_TAG, "Resuming receiver-update worker");
      receiverUpdateWorkerObj.resumeThread();
    }
  }

  /**
   * Determines if the receiver-update worker thread is paused.
   * @return true if the receiver-update worker thread is paused; false if not.
   */
  public boolean isReceiverUpdateWorkerPaused()
  {
    return (receiverUpdateWorkerObj != null && receiverUpdateWorkerObj.isThreadPaused());
  }

  /**
   * Determines if the receiver serial service is connected.
   * @return true if the receiver serial service is connected; false if not.
   */
  public boolean isReceiverSerialConnected()
  {
    return (serialServiceWriterObj != null && serialServiceWriterObj.isConnected());
  }

  /**
   * Receives and processes command messages via the DataMessageProcessor.
   * The processing is performed on a (non-UI) looper-worker thread.
   * @param msgCode message code.
   * @param val1 first integer value for message.
   * @param val2 second integer value for message.
   * @param paramStr parameter string for message.
   * @return true if the message was recognized and handled; false if not.
   */
  private boolean handleReceiverCommandMessage(int msgCode, int val1, int val2, String paramStr)
  {
    switch(msgCode)
    {
      case VIDCMD_TUNECODE_MSGC:       //tune receiver to channel code
      case VIDCMD_TUNEFREQ_MSGC:       //tune receiver to frequency in MHz
        sendCommandNoResponse(VIDRX_TUNE_PRESTR + paramStr + VIDRX_CR_STR);
        break;
      case VIDCMD_AUTOTUNE_MSGC:       //auto-tune receiver to strongest channel
        doAutoTuneReceiver();
        break;
      case VIDCMD_NEXTBAND_MSGC:       //tune receiver to next freq-code band
        sendCommandNoResponse(VIDRX_NEXTBAND_CMD);
        break;
      case VIDCMD_NEXTCHAN_MSGC:       //tune receiver to next freq-code channel
        sendCommandNoResponse(VIDRX_NEXTCHAN_CMD);
        break;
      case VIDCMD_PREVBAND_MSGC:       //tune receiver to previous freq-code band
        sendCommandNoResponse(VIDRX_PREVBAND_CMD);
        break;
      case VIDCMD_PREVCHAN_MSGC:       //tune receiver to previous freq-code channel
        sendCommandNoResponse(VIDRX_PREVCHAN_CMD);
        break;
      case VIDCMD_UPONEMHZ_MSGC:       //tune receiver frequency up by one MHz
        sendCommandNoResponse(VIDRX_UPONEMHZ_CMD);
        break;
      case VIDCMD_DOWNONEMHZ_MSGC:     //tune receiver frequency down by one MHz
        sendCommandNoResponse(VIDRX_DOWNONEMHZ_CMD);
        break;
      case VIDCMD_MNEXTCH_MSGC:        //select next (monitor) channel
        doSendNextPrevMonToReceiver(VIDRX_MNEXTCH_CMD);
        break;
      case VIDCMD_MPREVCH_MSGC:        //select previous (monitor) channel
        doSendNextPrevMonToReceiver(VIDRX_MPREVCH_CMD);
        break;
      case VIDCMD_MONITOR_MSGC:        //enter (or exit) monitor mode
        if(!monitorModeActiveFlag)
        {  //monitor mode not active; start it now
          doSendNextPrevMonToReceiver(VIDRX_MONITOR_CMD);
          monitorModeActiveFlag = true;
        }
        else
        {  //monitor mode is active; send CR to deactivate it
          outputCmdNoResponse(VIDRX_CR_ARR);
          monitorModeActiveFlag = false;
        }
        break;
      case VIDCMD_MONRESCAN_MSGC:       //send (re)scan command
        doSendScanCommandToReceiver(VIDRX_CHANSCAN_CMD,true);
        break;
      case VIDCMD_SETMINRSSI_MSGC:     //send new min-RSSI-for-scans value to receiver
        doSendMinRssiValToReceiver(val1);
        break;
      case VIDCMD_SETMONINTVL_MSGC:    //send new monitor-interval value to receiver
        doSendMonIntvlValToReceiver(val1);
        break;
      case VIDCMD_SETSCANLIST_MSGC:    //send new monitor/scan list to receiver
        doSendMonScanListToReceiver(paramStr);
        break;
      case VIDCMD_CHANSCAN_MSGC:       //do channel-scan and show select-channel dialog
        doScanSelectChanFunction(VIDRX_CHANSCAN_CMD);
        break;
      case VIDCMD_FULLSCAN_MSGC:       //do full-scan and show select-channel dialog
        doScanSelectChanFunction(VIDRX_FULLSCAN_CMD);
        break;
      default:                         //unrecognized command
        return false;
    }
    return true;
  }

  /**
   * Sends given command and returns "echo" characters (if any).
   * @param cmdBuff command to be sent.
   * @return Received "echo" characters (if any), or null if timeout reached
   * before receiving an end-of-line character.
   */
  protected String sendCommandNoResponse(byte [] cmdBuff)
  {
    final boolean resFlag = pauseReceiverUpdateWorker();
    final String retStr = outputCmdNoResponse(cmdBuff);
    resumeReceiverUpdateWorker();
    return retStr;
  }

  /**
   * Sends given command and returns "echo" characters (if any).
   * @param cmdStr command to be sent.
   * @return Received "echo" characters (if any), or null if timeout reached
   * before receiving an end-of-line character.
   */
  protected String sendCommandNoResponse(String cmdStr)
  {
    final boolean resFlag = pauseReceiverUpdateWorker();
    final String retStr = outputCmdNoResponse(cmdStr.getBytes());
    if(resFlag)                        //if was not already paused on entry
      resumeReceiverUpdateWorker();    // then resume worker thread
    return retStr;
  }

  /**
   * Sends given command and returns received response line.
   * @param cmdBuff command to be sent.
   * @return Received response line, or null if timeout reached
   * before receiving response.
   */
  protected String sendCommandRecvResponse(byte [] cmdBuff)
  {
    final boolean resFlag = pauseReceiverUpdateWorker();
    final String retStr = outputCmdRecvResponse(cmdBuff);
    if(resFlag)                        //if was not already paused on entry
      resumeReceiverUpdateWorker();    // then resume worker thread
    return retStr;
  }

  /**
   * Sends given command and returns received response line.
   * @param cmdStr command to be sent.
   * @return Received response line, or null if timeout reached
   * before receiving response.
   */
  protected String sendCommandRecvResponse(String cmdStr)
  {
    final boolean resFlag = pauseReceiverUpdateWorker();
    final String retStr = outputCmdRecvResponse(cmdStr.getBytes());
    if(resFlag)                        //if was not already paused on entry
      resumeReceiverUpdateWorker();    // then resume worker thread
    return retStr;
  }

  /**
   * Performs the work of sending the command to auto-tune receiver to strongest channel.
   */
  private void doAutoTuneReceiver()
  {
    final boolean resFlag = pauseReceiverUpdateWorker();
    if(outputCmdNoResponse(VIDRX_AUTOTUNE_CMD) != null)    //send 'A' command
    {  //initial newline response received OK
      processReceiverScanning();                 //wait for scanning to finish
    }
    if(resFlag)                        //if was not already paused on entry
      resumeReceiverUpdateWorker();    // then resume worker thread
  }

  /**
   * Performs the work of sending a 'monitor' command (N/P/M) to the
   * receiver and checking if the receiver has started scanning.
   * @param cmdBuff command to be sent.
   */
  private void doSendNextPrevMonToReceiver(byte [] cmdBuff)
  {
    final boolean resFlag = pauseReceiverUpdateWorker();
    if(outputCmdNoResponse(cmdBuff) != null)     //send 'N', 'P' or 'M' command
    {  //initial newline response received OK
      if(peekFirstReceivedChar() == ' ')         //if receiver is scanning then
        processReceiverScanning();               //wait for scanning to finish
    }
    if(resFlag)                        //if was not already paused on entry
      resumeReceiverUpdateWorker();    // then resume worker thread
  }

  /**
   * Performs the work of sending a scan command ('S' or 'F') to the
   * receiver and checking if the receiver has started scanning.
   * @param cmdBuff command to be sent; channel-scan ('S') or full-scan
   * command ('F').
   * @param restoreMonModeFlag true to check if monitor mode is active
   * before scan and restore it after scan if so.
   * @return The scan-data results string returned by the receiver, or null
   * if no response received.
   */
  private String doSendScanCommandToReceiver(byte [] cmdBuff, boolean restoreMonModeFlag)
  {
    final boolean resFlag = pauseReceiverUpdateWorker();
    final boolean monFlag = restoreMonModeFlag && monitorModeActiveFlag;  //true = restore mode
    String retStr = null;
    if(outputCmdNoResponse(cmdBuff) != null)     //send 'S' or 'F' command:
    {  //initial newline response received OK
      if(peekFirstReceivedChar() == ' ')         //if receiver is scanning then
        processReceiverScanning();               //wait for scanning to finish
      retStr = getNextReceivedLine(RESP_WAIT_TIMEMS);      //get scan-data results
//      ULog.d(LOG_TAG, "doSendScanCommandToReceiver received:  " + retStr);
      if(monFlag)
      {  //monitor mode was active and should be restored
        if(outputCmdNoResponse(VIDRX_MONITOR_CMD) != null)
        {  //initial newline response received OK
          if(peekFirstReceivedChar() == ' ')          //if receiver is scanning then
            processReceiverScanning();                //wait for scanning to finish
        }
      }
    }
    if(resFlag)                        //if was not already paused on entry
      resumeReceiverUpdateWorker();    // then resume worker thread
    return retStr;
  }

  /**
   * Performs the work for the scan-and-select-channel function.  A scan
   * is performed, and then the result is shown (with RSSI values) in a
   * select-channel dialog.
   * @param cmdBuff command to be sent; channel-scan ('S') or full-scan
   * command ('F').
   */
  private void doScanSelectChanFunction(byte [] cmdBuff)
  {
    final String scanStr = doSendScanCommandToReceiver(cmdBuff,false);
    if(vidRecMgrRespProcessorObj != null)
    {  //processor OK; respond with entries list
      vidRecMgrRespProcessorObj.sendMessage(VRECMGR_RESP_SELCHANNEL,scanStr);
    }
  }

  /**
   * Sends response to indicate that receiver is scanning and waits for
   * scanning to finish.
   */
  private void processReceiverScanning()
  {
    if(vidRecMgrRespProcessorObj != null)
    {  //processor OK; notify that receiver scanning has started
      vidRecMgrRespProcessorObj.sendMessage(VRECMGR_RESP_SCANBEGIN);
    }
    getNextReceivedLine(8000);     //wait for response (allow for scanning time)
    if(vidRecMgrRespProcessorObj != null)
    {  //handler OK; notify that receiver scanning is finished
      vidRecMgrRespProcessorObj.sendMessage(VRECMGR_RESP_SCANEND);
    }
  }

  /**
   * Performs the work of sending a new minimum-RSSI-for-scans value to the receiver.
   * @param minRssiVal new minimum-RSSI-for-scans value.
   */
  private void doSendMinRssiValToReceiver(int minRssiVal)
  {
    final boolean resFlag = pauseReceiverUpdateWorker();
    outputCmdNoResponse((VIDRX_MINRSSI_PRESTR + minRssiVal + VIDRX_CR_STR).getBytes());
    fetchMinRssiValFromReceiver();                    //fetch value from receiver
    if(getMinRssiForScansValue() != minRssiVal)       //check value
      ULog.e(LOG_TAG, "Mismatch confirming min-RSSI value sent to receiver");
    if(resFlag)                        //if was not already paused on entry
      resumeReceiverUpdateWorker();    // then resume worker thread
  }

  /**
   * Performs the work of sending a new monitor-interval value to the receiver.
   * @param intervalVal new monitor-interval value (in seconds).
   */
  private void doSendMonIntvlValToReceiver(int intervalVal)
  {
    final boolean resFlag = pauseReceiverUpdateWorker();
    outputCmdNoResponse((VIDRX_MONINTVL_PRESTR + intervalVal + VIDRX_CR_STR).getBytes());
    fetchMonIntvlValFromReceiver();                   //fetch value from receiver
    if(getMonitorIntervalValue() != intervalVal)      //check value
      ULog.e(LOG_TAG, "Mismatch confirming monitor-interval value sent to receiver");
    if(resFlag)                        //if was not already paused on entry
      resumeReceiverUpdateWorker();    // then resume worker thread
  }

  /**
   * Performs the work of sending a new monitor/scan list to the receiver.
   * @param listStr monitor/scan list string.
   */
  private void doSendMonScanListToReceiver(Object listStr)
  {
    if(!(listStr instanceof String))
      return;
    final boolean resFlag = pauseReceiverUpdateWorker();
//    ULog.d(LOG_TAG, "Sending:  " + VIDRX_SCANLIST_PRESTR + listStr);
              //send "L..." command, fetch response (expected response is # of frequencies):
    String respStr = outputCmdRecvResponse(
                                    (VIDRX_SCANLIST_PRESTR + listStr + VIDRX_CR_STR).getBytes());
    if(respStr != null && (respStr=respStr.trim()).length() > 0 &&
                                                           !Character.isDigit(respStr.charAt(0)))
    {  //response not empty and does not being with a digit (take as error message)
      final String errStr = "List error:  " + respStr;
      ULog.e(LOG_TAG,errStr);                          //log error message
      if(vidRecMgrRespProcessorObj != null)           //respond with error message
        vidRecMgrRespProcessorObj.sendMessage(VRECMGR_RESP_POPUPMSG,errStr);
    }
    fetchMonScanListStrFromReceiver();           //fetch value from receiver
    if(!isEqualToMonScanListStr(listStr))        //check given value vs fetched
      ULog.e(LOG_TAG, "Mismatch confirming monitor/scan list sent to receiver");
    if(resFlag)                        //if was not already paused on entry
      resumeReceiverUpdateWorker();    // then resume worker thread
  }

  /**
   * Sends the reset command to the receiver.  This method should only be
   * used while the receiver worker is stopped or paused.
   */
  public void outputReceiverResetCommand()
  {
    try
    {
      clearBuffer();
      serialServiceWriterObj.write(VIDRX_RESET_CMD);
    }
    catch(Exception ex)
    {
      ULog.e(LOG_TAG, "Error sending reset command to receiver", ex);
    }
  }

  /**
   * Sends the tune-channel/frequency command to the receiver.  This method
   * should only be used while the receiver worker is stopped or paused.
   * @param chanStr channel code or frequency value string.
   * @return Received "echo" characters (if any), or null if timeout reached
   * before receiving an end-of-line character.
   */
  public String outputTuneChannelCommand(String chanStr)
  {
    return outputCmdNoResponse((VIDRX_TUNE_PRESTR + chanStr + VIDRX_CR_STR).getBytes());
  }

  /**
   * Sends the echo-on or echo-off command to the receiver.  This method
   * should only be used while the receiver worker is stopped or paused.
   * @param onFlag true for echo on; false for echo off.
   * @return Received "echo" characters (if any), or null if timeout reached
   * before receiving an end-of-line character.
   */
  public String outputReceiverEchoCommand(boolean onFlag)
  {
    return outputCmdNoResponse(onFlag ? VIDRX_ECHOON_CMD : VIDRX_ECHOOFF_CMD);
  }

  /**
   * Sends the full-band-scan command ("XF") to the receiver.  This method
   * should only be used while the receiver worker is stopped or paused.
   * @return Received "echo" characters (if any), or null if timeout reached
   * before receiving an end-of-line character.
   */
  public String outputFullBandScanCommand()
  {
    return outputCmdNoResponse(VIDRX_BANDSCAN_CMD);
  }

  /**
   * Sends the command to query the program-version information from receiver
   * (but does not receive the response).
   * This method should only be used while the receiver worker is stopped
   * or paused.
   * @return Received "echo" characters (if any), or null if timeout reached
   * before receiving an end-of-line character.
   */
  public String outputQueryVersionCmd()
  {
    return outputCmdNoResponse(VIDRX_VERSION_CMD);
  }

  /**
   * Determines if monitor mode is active.
   * @return true if monitor mode is active.
   */
  public boolean isMonitorModeActive()
  {
    return monitorModeActiveFlag;
  }

  /**
   * Fetches and saves the minimum-RSSI-for-scans value from the receiver.
   * This method should only be used while the receiver worker is
   * stopped or paused.
   */
  public void fetchMinRssiValFromReceiver()
  {
    try
    {         //send command, receive and save numeric response
      minRssiForScansValue = outputCmdRecvIntResp(
                                                 (VIDRX_MINRSSI_PRESTR+VIDRX_CR_STR).getBytes());
    }
    catch(Exception ex)
    {
      ULog.e(LOG_TAG, "Error fetching min-RSSI value from receiver", ex);
    }
  }

  /**
   * Returns the current minimum-RSSI-for-scans value.
   * @return The current minimum-RSSI-for-scans value.
   */
  public int getMinRssiForScansValue()
  {
    return minRssiForScansValue;
  }

  /**
   * Fetches and saves the monitor-interval value from the receiver.
   * This method should only be used while the receiver worker is
   * stopped or paused.
   */
  public void fetchMonIntvlValFromReceiver()
  {
    try
    {         //send command, receive and save numeric response
      monitorIntervalValue = outputCmdRecvIntResp(
                                                (VIDRX_MONINTVL_PRESTR+VIDRX_CR_STR).getBytes());
    }
    catch(Exception ex)
    {
      ULog.e(LOG_TAG, "Error fetching monitor-interval value from receiver", ex);
    }
  }

  /**
   * Returns the current monitor-interval value (in seconds).
   * @return The current monitor-interval value (in seconds).
   */
  public int getMonitorIntervalValue()
  {
    return monitorIntervalValue;
  }

  /**
   * Fetches and saves the monitor/scan list from the receiver.
   * This method should only be used while the receiver worker is
   * stopped or paused.
   */
  public void fetchMonScanListStrFromReceiver()
  {
    try
    {         //send command, receive, check and save string response
      String respStr = outputCmdRecvResponse((VIDRX_SCANLIST_PRESTR+VIDRX_CR_STR).getBytes());
//      ULog.d(LOG_TAG, "Received (L):  " + respStr);
      if(respStr != null && respStr.trim().length() > 0)
      {  //string contains data
        respStr = respStr.replace(',',' ');           //change commas to spaces
                   //check that data can be parsed as list of numeric values:
        if(FrequencyTable.convStringToShortsList(respStr) != null)
          monitorScanListString = respStr;            //save response data
        else
          ULog.e(LOG_TAG, "Unable to parse monitor/scan list fetched from receiver:  " + respStr);
      }
      else
        monitorScanListString = "";
    }
    catch(Exception ex)
    {
      ULog.e(LOG_TAG, "Error fetching monitor/scan list from receiver", ex);
    }
  }

  /**
   * Returns the monitor/scan list string.
   * @return The monitor/scan list string.
   */
  public String getMonScanListString()
  {
    return monitorScanListString;
  }

  /**
   * Checks if the given string is equal to the current monitor/scan list string.
   * @param strObj string to check.
   * @return true if the given string is equal to the current monitor/scan list string.
   */
  public boolean isEqualToMonScanListStr(Object strObj)
  {
    return (strObj instanceof String && monitorScanListString != null &&
                                   ((String)strObj).trim().equals(monitorScanListString.trim()));
  }

  /**
   * Transmits a carriage-return character to the receiver.  (Does not
   * attempt to receive any "echo" characters.)
   */
  public void transmitCarriageReturn()
  {
    try
    {
      serialServiceWriterObj.write(VIDRX_CR_ARR);
    }
    catch(Exception ex)
    {
      ULog.e(LOG_TAG, "Error sending CR to receiver", ex);
    }
  }

  /**
   * Sets the receiver to the channel held in the ChannelTracker object.
   * This method can be used to restore the tuned channel after a scan.
   * This method should only be used while the receiver worker is stopped
   * or paused.
   * @return Received "echo" characters (if any), or null if timeout reached
   * before receiving an end-of-line character (or if ChannelTracker not set).
   */
  public String setReceiverViaChannelTracker()
  {
    if(vidChannelTrackerObj != null)
    {  //ChannelTracker has been setup
      final String str;      //if channel code available then tune to it
      if((str=vidChannelTrackerObj.getCurChannelCode()) != null)
        return outputTuneChannelCommand(str);
      final int freqVal;     //if frequency value valid then tune to it
      if((freqVal=vidChannelTrackerObj.getCurFrequencyInMHz()) > 0)
        return outputTuneChannelCommand(Integer.toString(freqVal));
    }
    return null;
  }

  /**
   * Queries, receives and returns program-version information from receiver.
   * This method should only be used while the receiver worker is stopped
   * or paused.
   * @return Program-version-information string, or null if unable to receive.
   */
  private String queryGetVersionInfo()
  {
    return outputCmdRecvResponse(VIDRX_VERSION_CMD);
  }

  /**
   * Sends given command and returns received numeric response.  This
   * method should only be used while the receiver worker is stopped
   * or paused.
   * @param cmdBuff command to be sent.
   * @return Received response value.
   * @throws NumberFormatException If the received response could not be
   * parsed as an integer, or if no response was received.
   */
  protected int outputCmdRecvIntResp(byte [] cmdBuff) throws NumberFormatException
  {
    final String str;
    if((str=outputCmdRecvResponse(cmdBuff)) != null)
    {  //received response; attempt to parse as number
      return Integer.parseInt(str.trim());
    }
    throw new NumberFormatException("No response from receiver");
  }

  /**
   * Outputs given command and returns "echo" characters (if any).
   * (Lower-level I/O).
   * @param cmdBuff command to be sent.
   * @return Received "echo" characters (if any), or null if timeout reached
   * before receiving an end-of-line character.
   */
  private String outputCmdNoResponse(byte [] cmdBuff)
  {
    try
    {
      clearBuffer();
      serialServiceWriterObj.write(cmdBuff);
      return getNextReceivedLine(RESP_WAIT_TIMEMS);
    }
    catch(Exception ex)
    {
      ULog.e(LOG_TAG, "Error sending command to receiver", ex);
      return null;
    }
  }

  /**
   * Outputs given command and returns received response line.
   * (Lower-level I/O).
   * @param cmdBuff command to be sent.
   * @return Received response line, or null if timeout reached
   * before receiving response.
   */
  private String outputCmdRecvResponse(byte [] cmdBuff)
  {
    return outputCmdRecvResponse(cmdBuff,RESP_WAIT_TIMEMS);
  }

  /**
   * Outputs given command and returns received response line.
   * (Lower-level I/O).
   * @param cmdBuff command to be sent.
   * @param timeoutMs maximum number of milliseconds to wait.
   * @return Received response line, or null if timeout reached
   * before receiving response.
   */
  private String outputCmdRecvResponse(byte [] cmdBuff, int timeoutMs)
  {
    if(outputCmdNoResponse(cmdBuff) == null)
      return null;
    return getNextReceivedLine(timeoutMs);
  }

  /**
   * Clears any received lines in the buffer.
   */
  public void clearBuffer()
  {
    synchronized(receivedLinesList)
    {  //grab thread lock for list
      receivedLinesList.clear();             //clear lines list
    }
  }

  /**
   * Stores the received characters.
   * @param buff buffer of received characters.
   * @param numChars number of characters in buffer.
   */
  public void storeReceivedChars(byte [] buff, int numChars)
  {
    char ch;
    for(int i=0; i<numChars; ++i)
    {  //for each character received
      if((ch=(char)buff[i]) == '\r' || ch == '\n')
      {  //end of line; add received line to list
        if(receivedCharsBuffer.length() > 0 || ch == receivedLinesLastEndChar ||
                                                                receivedLinesLastEndChar == '\0')
        {  //character is not second char of CR/LF sequence
          synchronized(receivedLinesList)
          {  //grab thread lock for list
            receivedLinesList.add(receivedCharsBuffer.toString());
            receivedCharsBuffer.setLength(0);    //clear character buffer
            if(receivedLinesList.size() > BUFF_MAX_LINES)  //if too many lines then
              receivedLinesList.remove(0);                 //remove oldest
            receivedLinesList.notifyAll();       //wake 'get' method if waiting
            firstReceivedCharacter = '\0';       //clear first-received character
          }
          receivedLinesLastEndChar = ch;         //track last CR/LF character
          vidrxScanInProgStrMatchPos = 0;        //reset matcher pos for receiver scanning
          vidrxScanningInProgressFlag = false;   //reset flag for receiver scanning
        }
      }
      else
      {  //not end of line
        receivedCharsBuffer.append(ch);          //add to character buffer
        if(firstReceivedCharacter == '\0')       //if no previous for line then
          firstReceivedCharacter = ch;           //save first-received character
              //track received chars to see if buffer matches SCANNING_CHECK_STR:
        if(vidrxScanInProgStrMatchPos >= 0 && vidrxScanInProgStrMatchPos < SCANNING_CHKSTR_LEN)
        {  //matcher position is not -1 (for mismatch) or > length (for match complete)
          if(ch == SCANNING_CHECK_STR.charAt(vidrxScanInProgStrMatchPos))
          {  //character matches at position; check if all have been matched
            if(++vidrxScanInProgStrMatchPos >= SCANNING_CHKSTR_LEN)
              vidrxScanningInProgressFlag = true;     //indicate 'scanning' string received
          }
          else  ////character does not match at position
            vidrxScanInProgStrMatchPos = -1;     //set value to stop checking
        }
      }
    }
  }

  /**
   * Waits for and returns next line of received characters, up to the timeout.
   * @param timeoutMs maximum number of milliseconds to wait.
   * @return A string containing the next received line, or null if timeout.
   */
  public String getNextReceivedLine(int timeoutMs)
  {
    synchronized(receivedLinesList)
    {  //grab thread lock for list
      if(receivedLinesList.size() > 0)
      {  //list not empty; fetch and return received line
        final String str = receivedLinesList.remove(0);    //remove line from list
        return str;
      }
      try
      {       //wait for line of data received; up to timeout
        receivedLinesList.wait(timeoutMs);
      }
      catch(InterruptedException ex)
      {
      }
      if(receivedLinesList.size() <= 0)
      {  //line not received before timeout
        ULog.d(LOG_TAG, "getNextReceivedLine() returning null (timeout)");
        return null;                        //indicate no data
      }
              //fetch and return received line:
      final String str = receivedLinesList.remove(0);      //remove line from list
      return str;
    }
  }

  /**
   * Waits for and returns the first character received from the video
   * receiver (waits up to 1 second).
   * @return The received character, or '\0' if none received.
   */
  private char peekFirstReceivedChar()
  {
    int count = 0;
    while(true)
    {  //loop while waiting for character to arrive
      if(firstReceivedCharacter != '\0')    //if character received then
        return firstReceivedCharacter;      //return character
      if(count % 10 == 0)
      {  //check if full line received (but not every loop)
        synchronized(receivedLinesList)
        {  //grab thread lock for list
          if(receivedLinesList.size() > 0)
          {  //line of characters was received
            final String str = receivedLinesList.get(0);        //get line from list
            return (str.length() > 0) ? str.charAt(0) : '\n';   //return first char
          }
        }
      }
      if(++count > 99)       //increment loop count
        break;               //if too many then abort
      try
      {       //wait 10ms each loop
        Thread.sleep(10);
      }
      catch(InterruptedException ex)
      {
      }
    }
    return '\0';        //timeout reached
  }

  /**
   * Queries, receives and reports the channel and RSSI values from the receiver.
   * @return true if successful; false if error.
   */
  private boolean queryReportChanRssiVals()
  {
    if(vidrxScanningInProgressFlag)
    {  //receiver returned 'scanning' indicator string
      processReceiverScanning();       //notify and wait for scanning to finish
      return true;
    }
    clearBuffer();                //clear anything already received
    String respStr;
                        //send "~" and get response (no LF in between):
    if((respStr=outputCmdNoResponse(VIDRX_REPCHRSSI_CMD)) != null)
    {  //command sent and response received OK
      try
      {       //parse "freqCC=rssi" response
        if(respStr.startsWith(">"))         //ignore any leading '>' prompt
          respStr = respStr.substring(1);
        respStr = respStr.trim();           //remove leading space
        final int p;
        if((p=respStr.indexOf('=')) > 3)
        {  //length of response OK
          final int freqVal = Integer.parseInt(respStr.substring(0,4));
          final String chCodeStr = (p >= 6) ? respStr.substring(4,6) : null;
          String dispStr = null;
          int q;             //check for trailing "M" (for 'monitor' mode)
          if((q=respStr.indexOf(' ',p+1)) > 0)
          {  //trailing space found
            if(respStr.indexOf('M',q) == q+1)
            {  //'M' found after space
              dispStr = MONITOR_STRING;          //setup to display 'monitor' indicator
              monitorModeActiveFlag = true;      //indicate 'monitor' mode active
            }
          }
          else
          {  //no trailing space found
            q = respStr.length();
            monitorModeActiveFlag = false;       //indicate 'monitor' mode not active
          }
          final int rssiVal = Integer.parseInt(respStr.substring(p+1,q));
              //send update to channel tracker:
          if(vidChannelTrackerObj != null)
            vidChannelTrackerObj.setFreqChannel(chCodeStr,(short)freqVal);
              //respond with values:
          if(vidRecMgrRespProcessorObj != null)
            vidRecMgrRespProcessorObj.sendMessage(
                                   VRECMGR_RESP_CHANRSSI,freqVal,rssiVal,dispStr);
          return true;       //indicate success
        }
      }
      catch(NumberFormatException ex)
      {  //error parsing; return false for error
      }
    }
    return false;            //indicate failure
  }


  /**
   * Class ReceiverScanDataWorker defines a background-worker thread for
   * reading data from the receiver.
   */
  private class ReceiverUpdateWorker extends PausableWorker
  {
    /**
     * Creates background-worker thread for reading data from the receiver.
     */
    public ReceiverUpdateWorker()
    {
      super("VidReceiverUpdateWorker",UPDWKR_PERIODIC_DELAYMS);
    }

  /**
   * Receiver-update task method to be invoked at each interval.
   * @return true if the worker should continue running;
   * false if the worker should terminate.
   */
    @Override
    public boolean doWorkerTask()
    {
      if(!queryReportChanRssiVals())                  //do query and report
        waitForNotify(UPDWKR_PERIODIC_DELAYMS*2);     //if failure then extra delay
      return true;
    }
  }
}
