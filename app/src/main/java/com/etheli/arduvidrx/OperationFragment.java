//OperationFragment.java:  Defines the main operations screen and functions.
//
//   3/9/2017 -- [ET]
//

package com.etheli.arduvidrx;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import com.etheli.util.Averager;
import com.etheli.util.DialogUtils;
import com.etheli.util.GuiUtils;
import com.etheli.util.SwipeGestureDispatcher;
import net.mabboud.android_tone_player.ContinuousBuzzer;
import es.pymasde.blueterm.BlueTerm;

/**
 * Class OperationFragment defines the main operations screen and functions.
 */
public class OperationFragment extends Fragment
                                     implements SwipeGestureDispatcher.SwipeGestureEventInterface
{
    /** Tag string for logging. */
  public static final String LOG_TAG = "OperationFragment";
  private final ProgramResources programResourcesObj = ProgramResources.getProgramResourcesObj();
  private final MainGuiUpdateHandler mainGuiUpdateHandlerObj = new MainGuiUpdateHandler();
  private TextView versionTextViewObj = null;
  private TextView freqCodeTextViewObj = null;
  private ProgressBar rssiProgressBarObj = null;
  private TextView rssiValueTextViewObj = null;
  private TabHost operationFragTabHostObj = null;
  private VidReceiverManager vidReceiverManagerObj = null;
  private FrequencyTable videoFrequencyTableObj = null;
  private ChannelTracker videoChannelTrackerObj = null;
  private boolean rssiToneEnabledFlag = false;
  private final ContinuousBuzzer rssiContinuousBuzzerObj = new ContinuousBuzzer();
  private final Averager rssiToneAveragerObj = new Averager(5);


  /**
   * Creates the view for the main operations screen.
   * @param inflater LayoutInflater: The LayoutInflater object that can be used to inflate
   * any views in the fragment.
   * @param container ViewGroup: If non-null, this is the parent view that the fragment's UI
   * should be attached to.
   * @param savedInstanceState Bundle: If non-null, this fragment is being re-constructed
   * from a previous saved state as given here.
   * @return Return the View for the fragment's UI, or null.
   */
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                                             Bundle savedInstanceState)
  {                                    //inflate fragment view from XML:
    final View viewObj = inflater.inflate(R.layout.operation_fragment,container,false);
    GuiUtils.setViewButtonsClickListener(viewObj,
          new View.OnClickListener()
            {
              @Override
              public void onClick(View vObj)
              {
                processButtonClick(vObj);
              }
            });
         //setup resources and channel tracker:
    vidReceiverManagerObj = programResourcesObj.getVidReceiverManagerObj();
    videoFrequencyTableObj = programResourcesObj.getFrequencyTableObj();
    videoChannelTrackerObj = new ChannelTracker(videoFrequencyTableObj);
         //setup so video-receiver-manager can update channel tracker:
    vidReceiverManagerObj.setChannelTrackerObj(videoChannelTrackerObj);
         //setup so video-receiver-manager can update GUI widgets:
    vidReceiverManagerObj.setGuiUpdateHandlerObj(mainGuiUpdateHandlerObj);
    return viewObj;
  }

  /**
   * Called when the fragment's activity has been created and this
   * fragment's view hierarchy instantiated.
   */
  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);
         //setup access to display widgets (in 'onCreateView()' is too early):
    versionTextViewObj = (TextView)getActivity().findViewById(R.id.versionTextView);
    freqCodeTextViewObj = (TextView)getActivity().findViewById(R.id.freqCodeTextView);
    rssiProgressBarObj = (ProgressBar)getActivity().findViewById(R.id.rssiProgressBar);
    rssiValueTextViewObj = (TextView)getActivity().findViewById(R.id.rssiValueTextView);
         //setup "delta" period for RSSI-audio-tone generator:
    rssiContinuousBuzzerObj.setPausePeriodSeconds(0.1);
    rssiContinuousBuzzerObj.setVolume(25);       //reduce volume
         //create and setup tab host:
    operationFragTabHostObj = (TabHost)getActivity().findViewById(R.id.tabHost);
    operationFragTabHostObj.setup();
         //"Tune" Tab:
    TabHost.TabSpec specObj = operationFragTabHostObj.newTabSpec(getActivity().getString(R.string.tab_tune_name));
    specObj.setContent(R.id.tabTune);
    specObj.setIndicator(specObj.getTag());
    operationFragTabHostObj.addTab(specObj);
         //"Fine" Tab:
    specObj = operationFragTabHostObj.newTabSpec(getActivity().getString(R.string.tab_fine_name));
    specObj.setContent(R.id.tabFine);
    specObj.setIndicator(specObj.getTag());
    operationFragTabHostObj.addTab(specObj);
         //"Monitor" Tab:
    specObj = operationFragTabHostObj.newTabSpec(getActivity().getString(R.string.tab_monitor_name));
    specObj.setContent(R.id.tabMonitor);
    specObj.setIndicator(specObj.getTag());
    operationFragTabHostObj.addTab(specObj);
         //"Scan" Tab:
    specObj = operationFragTabHostObj.newTabSpec(getActivity().getString(R.string.tab_scan_name));
    specObj.setContent(R.id.tabScan);
    specObj.setIndicator(specObj.getTag());
    operationFragTabHostObj.addTab(specObj);
  }

  /**
   * Called when the fragment is stopped.
   */
  @Override
  public void onStop()
  {
    super.onStop();
    rssiContinuousBuzzerObj.stop();    //stop RSSI tone (if sounding)
  }

  /**
   * Invoked when a 'swipe' event is detected.  This method implements the
   * 'SwipeGestureDispatcher.SwipeGestureEventInterface' interface.
   * @param rightFlag true if the swipe went left to right; false if the
   * swipe went right to left.
   */
  @Override
  public void onSwipeGesture(boolean rightFlag)
  {
    try
    {
      if(rightFlag)
      {  //swipe right
        final int idx;
        if((idx=operationFragTabHostObj.getCurrentTab()) <
                     operationFragTabHostObj.getTabWidget().getTabCount()-1)
        {  //not at last tab; select next tab
          operationFragTabHostObj.setCurrentTab(idx+1);
        }
        else  //at last tab; select first tab
          operationFragTabHostObj.setCurrentTab(0);
      }
      else
      {  //swipe left
        final int idx;
        if((idx=operationFragTabHostObj.getCurrentTab()) > 0)   //if not at first tab then
          operationFragTabHostObj.setCurrentTab(idx-1);         //select previous tab
        else
        {  //at first tab; select last tab
          operationFragTabHostObj.setCurrentTab(
                    operationFragTabHostObj.getTabWidget().getTabCount()-1);
        }
      }
    }
    catch(Exception ex)
    {  //some kind of exception error; log it and move on
      Log.e(LOG_TAG, "Exception in onSwipeGesture()", ex);
    }
  }

  /**
   * Processes the click event for the given Button object.
   * @param vObj Button object.
   */
  public void processButtonClick(View vObj)
  {
    switch(vObj.getId())
    {
      case R.id.disconnectButton:      //close connection
        final BluetoothSerialService serviceObj;
        if((serviceObj=programResourcesObj.getBluetoothSerialServiceObj()) != null)
          serviceObj.doDisconnectDeviceAction();
        break;
      case R.id.terminalButton:        //start 'terminal' activity
        final Intent intentObj = new Intent(getActivity(),BlueTerm.class);
        startActivity(intentObj);
        break;
      case R.id.rssiToneCheckBox:      //RSSI audio tone checkbox
        if(vObj instanceof CheckBox)
          setRssiToneEnabled(((CheckBox)vObj).isChecked());
        break;
      case R.id.channelButton:         //select tune channel from list
        showSelectChannelChoiceDialog(R.string.chansel_dialog_title,
                                               videoFrequencyTableObj.getFreqChannelItemsArray(),
                                              videoChannelTrackerObj.getCurFreqChannelItemIdx());
        break;
      case R.id.freqButton:            //enter tune frequency
        showEditFreqNumberDialog(R.string.freqsel_dialog_title,
                                                  videoChannelTrackerObj.getCurFrequencyInMHz());
        break;
      case R.id.autoTuneButton:        //auto-tune to strongest channel
        vidReceiverManagerObj.autoTuneReceiver();
        break;
      case R.id.nextBandButton:        //tune receiver to next freq-code band
        vidReceiverManagerObj.tuneNextPrevBandChannel(true,true);
        break;
      case R.id.nextChanButton:        //tune receiver to next freq-code channel
        vidReceiverManagerObj.tuneNextPrevBandChannel(false,true);
        break;
      case R.id.prevBandButton:        //tune receiver to previous freq-code band
        vidReceiverManagerObj.tuneNextPrevBandChannel(true,false);
        break;
      case R.id.prevChanButton:        //tune receiver to previous freq-code channel
        vidReceiverManagerObj.tuneNextPrevBandChannel(false,false);
        break;
      case R.id.upOneMhzButton:        //increase tune frequency by 1 MHZ
        vidReceiverManagerObj.tuneReceiverFreqByOneMhz(true);
        break;
      case R.id.downOneMhzButton:      //decrease tune frequency by 1 MHZ
        vidReceiverManagerObj.tuneReceiverFreqByOneMhz(false);
        break;
      case R.id.upTenMhzButton:        //increase tune frequency by 10 MHZ
        vidReceiverManagerObj.tuneReceiverToFrequency(
                                               videoChannelTrackerObj.getCurFrequencyInMHz()+10);
        break;
      case R.id.downTenMhzButton:      //decrease tune frequency by 10 MHZ
        vidReceiverManagerObj.tuneReceiverToFrequency(
                                               videoChannelTrackerObj.getCurFrequencyInMHz()-10);
        break;
      case R.id.upHunMhzButton:        //increase tune frequency by 100 MHZ
        vidReceiverManagerObj.tuneReceiverToFrequency(
                                              videoChannelTrackerObj.getCurFrequencyInMHz()+100);
        break;
      case R.id.downHunMhzButton:      //decrease tune frequency by 100 MHZ
        vidReceiverManagerObj.tuneReceiverToFrequency(
                                              videoChannelTrackerObj.getCurFrequencyInMHz()-100);
        break;
      case R.id.monNextChButton:        //select next (monitor) channel
        vidReceiverManagerObj.selectPrevNextMonitorChannel(true);
        break;
      case R.id.monPrevChButton:        //select previous (monitor) channel
        vidReceiverManagerObj.selectPrevNextMonitorChannel(false);
        break;
      case R.id.monitorButton:         //send command to enter 'monitor' mode
        vidReceiverManagerObj.sendMonitorCmdToReceiver();
        break;
      case R.id.monRescanButton:       //send command to (re)scab
        vidReceiverManagerObj.sendMonRescanCmdToReceiver();
        break;
      case R.id.monMinRssiButton:      //edit/send minimum-RSSI-for-scans value
      case R.id.scanMinRssiButton:
        DialogUtils.showEditNumberDialogFragment(getActivity(),R.string.minrssi_dialog_title,
                                               vidReceiverManagerObj.getMinRssiForScansValue(),2,
            new DialogUtils.DialogItemSelectedListener()
              {        //invoked after 'Done' button pressed
                @Override
                public void itemSelected(int val)
                {           //send new minimum-RSSI-for-scans value to receiver
                  vidReceiverManagerObj.sendMinRssiValToReceiver(val);
                }
              });
        break;
      case R.id.intervalButton:        //edit/send monitor-interval value
        DialogUtils.showEditNumberDialogFragment(getActivity(),R.string.monintvl_dialog_title,
                                               vidReceiverManagerObj.getMonitorIntervalValue(),5,
            new DialogUtils.DialogItemSelectedListener()
              {        //invoked after 'Done' button pressed
                @Override
                public void itemSelected(int val)
                {           //send new monitor-interval value to receiver
                  vidReceiverManagerObj.sendMonIntvlValToReceiver(val);
                }
              });
        break;
      case R.id.chanScanButton:        //do channel-scan and select channel
        vidReceiverManagerObj.startScanSelectChanFunction(false);
        break;
      case R.id.fullScanButton:        //do full-scan and select channel
        vidReceiverManagerObj.startScanSelectChanFunction(true);
        break;
    }
  }

  /**
   * Shows a select-channel choice dialog and handles the response.
   * @param titleId resource ID of title for dialog.
   * @param charSeqArr array of items to be displayed.
   * @param initIdx index of initial item to be selected, or -1 for none.
   */
  private void showSelectChannelChoiceDialog(int titleId, final CharSequence [] charSeqArr, int initIdx)
  {
    DialogUtils.showSingleChoiceDialogFragment(getActivity(),titleId,charSeqArr,
                                            initIdx,R.string.alert_dialog_close,
        new DialogUtils.DialogItemSelectedListener()
          {        //invoked when item is selected
            @Override
            public void itemSelected(int selIdx)
            {           //translate array index to channel and tune to it:
              final CharSequence chSeqObj;
              if(selIdx >= 0 && selIdx < charSeqArr.length &&
                         (chSeqObj=charSeqArr[selIdx]) instanceof FrequencyTable.FreqChannelItem)
              {  //FreqChannelItem fetched from array OK
                final FrequencyTable.FreqChannelItem itemObj =
                                                         (FrequencyTable.FreqChannelItem)chSeqObj;
                         //if channel code OK then tune it:
                if(itemObj.channelCodeStr != null && itemObj.channelCodeStr.trim().length() > 0)
                  vidReceiverManagerObj.tuneReceiverToChannelCode(itemObj.channelCodeStr);
                else     //if no channel code then tune to frequency value
                  vidReceiverManagerObj.tuneReceiverToFrequency(itemObj.frequencyVal);
              }
            }
          });
  }

  /**
   * Shows an edit-frequency number dialog and handles the response.
   * @param titleId resource ID of title for dialog.
   * @param initVal initial value for editor in dialog.
   */
  private void showEditFreqNumberDialog(int titleId, int initVal)
  {
    DialogUtils.showEditNumberDialogFragment(getActivity(),titleId,initVal,4,
        new DialogUtils.DialogItemSelectedListener()
          {        //invoked after 'Done' button pressed
            @Override
            public void itemSelected(int val)
            {           //tune to entered frequency value (in MHz):
              vidReceiverManagerObj.tuneReceiverToFrequency(val);
            }
          });
  }

  /**
   * Shows a select-channel choice dialog for the frequency-channel items
   * matching the frequencies (and RSSI values) in the given list string
   * and handles the response.
   * @param scanStr list string of space-delimited "freq=RSSI" entries,
   * or null or an error message if error.
   */
  private void showSelChanDialogForScanStr(String scanStr)
  {
    if(scanStr != null && (scanStr=scanStr.trim()).length() > 0)
    {  //given entries-list string not empty
      final FrequencyTable.FreqChannelItem [] itemsArr;
      if((itemsArr=videoFrequencyTableObj.getFChanItemsArrForScanStr(scanStr)) != null &&
                                                                             itemsArr.length > 0)
      {  //entries-list string parsed OK; show dialog and handle response
        showSelectChannelChoiceDialog(R.string.chansel_dialog_title,itemsArr,-1);
      }
      else
      {  //entries-list string parsing failed
        final char ch;
        if((ch=scanStr.charAt(0)) < '0' || ch > '9')  //if doesn't start with numeric then
          showPopupMessage(scanStr);                  //show received data as popup message
        else
        {  //received data starts with numeric; show parsing-error popup message
          showPopupMessage(R.string.msg_unable_scanparse);
          Log.e(LOG_TAG, "showSelChanDialogForScanStr() error parsing:  " + scanStr);
        }
      }
    }
    else
      showPopupMessage(R.string.msg_unable_scanrec);
  }

  /**
   * Sets the value for the 'version' text field.
   * @param str text to set.
   */
  private void setVersionTextViewStr(String str)
  {
    if(versionTextViewObj != null)
      versionTextViewObj.setText(str);
  }

  /**
   * Sets the value for the frequency/code text field.
   * @param str text to set.
   */
  private void setFreqCodeTextViewStr(String str)
  {
    if(freqCodeTextViewObj != null)
      freqCodeTextViewObj.setText(str);
  }

  /**
   * Sets the value for the RSSI progress bar, text view and audio tone (if enabled).
   * @param val progress value to set (0-100).
   */
  private void setRssiDisplayValue(int val)
  {
    if(rssiProgressBarObj != null)
      rssiProgressBarObj.setProgress(val);
    if(rssiValueTextViewObj != null)
      rssiValueTextViewObj.setText(Integer.toString(val));
    if(rssiToneEnabledFlag)
    {  //RSSI-audio tone enabled; calc freq & average and update tone frequency
      rssiContinuousBuzzerObj.setToneFreqInHz(rssiToneAveragerObj.enter(val*20 + 250));
      rssiContinuousBuzzerObj.play();
    }
  }

  /**
   * Enables or disables RSSI-audio tone.
   * @param flagVal true to enabled; false to disable.
   */
  private void setRssiToneEnabled(boolean flagVal)
  {
    if(rssiToneEnabledFlag && !flagVal)
    {  //going from enabled to disabled
      rssiContinuousBuzzerObj.stop();       //stop tone output
      rssiToneAveragerObj.clear();          //reset averager
    }
    rssiToneEnabledFlag = flagVal;
  }

  /**
   * Displays a popup message.
   * @param msgStr message text to display.
   */
  private void showPopupMessage(String msgStr)
  {
    try
    {
      Toast.makeText(getActivity().getApplicationContext(),msgStr,Toast.LENGTH_SHORT).show();
    }
    catch(Exception ex)
    {
      Log.e(LOG_TAG, "Error showing popup message", ex);
      Log.e(LOG_TAG, "Popup message:  " + msgStr);
    }
  }

  /**
   * Displays a popup message.
   * @param msgId resource ID of message.
   */
  private void showPopupMessage(int msgId)
  {
    showPopupMessage(getString(msgId));
  }

  /**
   * Class MainGuiUpdateHandler handles updates to the main GUI components.
   */
  private class MainGuiUpdateHandler extends Handler
  {
    /**
     * Creates the update handler.
     */
    public MainGuiUpdateHandler()
    {
      super(Looper.getMainLooper());        //run on UI thread
    }

    /**
     * Receives and processes GUI-update messages.
     * @param msgObj message object to process.
     */
    @Override
    public void handleMessage(Message msgObj)
    {
      switch(msgObj.what)
      {
        case ProgramResources.MAINGUI_UPD_VERSION:         //version information
          if(msgObj.obj instanceof String)
            setVersionTextViewStr((String)msgObj.obj);
          break;
        case ProgramResources.MAINGUI_UPD_CHANRSSI:        //channel and RSSI values
          final int freqVal;
          if((freqVal=msgObj.arg1) > 0)
          {  //frequency value OK to display
            final FrequencyTable.FreqChannelItem itemObj;
            String codeStr;         //get channel-code string for current channel (if any)
            if((itemObj=videoChannelTrackerObj.getCurFreqChannelItemObj()) != null &&
                                                            (short)freqVal == itemObj.frequencyVal)
            {  //frequency-channel item available and frequency matches
              codeStr = "  " + itemObj.channelCodeStr;       //show channel-code string
            }
            else     //no channel-code string for frequency
              codeStr = "";
                             //if message 'obj' is a string then append it:
            final String dispStr = (msgObj.obj instanceof String) ? ("  " + msgObj.obj) : "";
            setFreqCodeTextViewStr("Freq:  " + freqVal + " MHz" + codeStr + dispStr);
          }
          setRssiDisplayValue(msgObj.arg2);
          break;
        case ProgramResources.MAINGUI_UPD_CHANTEXT:        //set value for freqCodeTextView
          if(msgObj.obj instanceof String)
            setFreqCodeTextViewStr((String)msgObj.obj);
          break;
        case ProgramResources.MAINGUI_UPD_POPUPMSG:        //show popup message
          if(msgObj.obj instanceof String)
            showPopupMessage((String)msgObj.obj);
          break;
        case ProgramResources.MAINGUI_UPD_VRMGRSTARTED:    //video receiver started
          break;
        case ProgramResources.MAINGUI_UPD_SCANBEGIN:       //video-receiver scanning started
                                                      //disable buttons while scanning:
          GuiUtils.setViewButtonsEnabledState(getView(),false);
          setRssiDisplayValue(msgObj.arg2);           //set RSSI display to zero
                                                      //show "Scanning..." text while scanning:
          setFreqCodeTextViewStr(getString(R.string.scanning_status_text));
          break;
        case ProgramResources.MAINGUI_UPD_SCANEND:         //video-receiver scanning finished
          setFreqCodeTextViewStr("");                 //clear "Scanning..." text
          GuiUtils.setViewButtonsEnabledState(getView(),true);       //re-enable buttons
          break;
        case ProgramResources.MAINGUI_UPD_SELCHANNEL:      //show select-channel choice dialog
          if(msgObj.obj instanceof String)
            showSelChanDialogForScanStr((String)msgObj.obj);
          break;
        default:                                 //unknown value
          super.handleMessage(msgObj);           //pass to parent handler
      }
    }
  }
}
