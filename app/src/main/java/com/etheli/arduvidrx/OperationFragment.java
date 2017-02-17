//OperationFragment.java:  Defines the main operations screen and functions.
//
//  2/16/2017 -- [ET]
//

package com.etheli.arduvidrx;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.etheli.util.Averager;
import com.etheli.util.DialogUtils;
import com.etheli.util.GuiUtils;
import net.mabboud.android_tone_player.ContinuousBuzzer;
import es.pymasde.blueterm.BlueTerm;

/**
 * Class OperationFragment defines the main operations screen and functions.
 */
public class OperationFragment extends Fragment
{
  private final ProgramResources programResourcesObj = ProgramResources.getProgramResourcesObj();
  private final MainGuiUpdateHandler mainGuiUpdateHandlerObj = new MainGuiUpdateHandler();
  private TextView versionTextViewObj = null;
  private TextView freqCodeTextViewObj = null;
  private ProgressBar rssiProgressBarObj = null;
  private TextView rssiValueTextViewObj = null;
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
   * Called when the fragment is visible to the user.
   */
  @Override
  public void onStart()
  {
    super.onStart();
         //setup access to display widgets (in 'onCreateView()' is too early):
    versionTextViewObj = (TextView)getActivity().findViewById(R.id.versionTextView);
    freqCodeTextViewObj = (TextView)getActivity().findViewById(R.id.freqCodeTextView);
    rssiProgressBarObj = (ProgressBar)getActivity().findViewById(R.id.rssiProgressBar);
    rssiValueTextViewObj = (TextView)getActivity().findViewById(R.id.rssiValueTextView);
         //setup "delta" period for RSSI-audio-tone generator:
    rssiContinuousBuzzerObj.setPausePeriodSeconds(0.1);
    rssiContinuousBuzzerObj.setVolume(25);       //reduce volume
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
        DialogUtils.showSingleChoiceDialogFragment(getActivity(),R.string.chansel_dialog_title,
                                               videoFrequencyTableObj.getFreqChannelItemsArray(),
                   videoChannelTrackerObj.getCurFreqChannelItemIdx(),R.string.alert_dialog_close,
            new DialogUtils.DialogItemSelectedListener()
              {        //invoked when item is selected
                @Override
                public void itemSelected(int val)
                {           //translate array index to channel code and tune to it:
                  final FrequencyTable.FreqChannelItem itemObj;
                  if((itemObj=videoFrequencyTableObj.getFreqChanItemForIdx(val)) != null)
                    vidReceiverManagerObj.tuneReceiverToChannelCode(itemObj.channelCodeStr);
                }
              });
        break;
      case R.id.freqButton:            //enter tune frequency
        DialogUtils.showEditNumberDialogFragment(getActivity(),R.string.freqsel_dialog_title,
                                                   videoChannelTrackerObj.getCurFrequencyInMHz(),
            new DialogUtils.DialogItemSelectedListener()
              {        //invoked after 'Done' button pressed
                @Override
                public void itemSelected(int val)
                {           //tune to entered frequency value (in MHz):
                  vidReceiverManagerObj.tuneReceiverToFrequency(val);
                }
              });
        break;
      case R.id.upOneMhzButton:        //increase tune frequency by 1 MHZ
        vidReceiverManagerObj.tuneReceiverFreqByOneMhz(true);
        break;
      case R.id.downOneMhzButton:      //decrease tune frequency by 1 MHZ
        vidReceiverManagerObj.tuneReceiverFreqByOneMhz(false);
        break;
      case R.id.autoTuneButton:        //auto-tune to strongest channel
        vidReceiverManagerObj.autoTuneReceiver();
        break;
      case R.id.nextChanButton:        //select next channel
        vidReceiverManagerObj.selectPrevNextReceiverChannel(true);
        break;
      case R.id.prevChanButton:        //select previous channel
        vidReceiverManagerObj.selectPrevNextReceiverChannel(false);
        break;
    }
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
            setFreqCodeTextViewStr("Freq:  " + freqVal + " MHz" + codeStr);
          }
          setRssiDisplayValue(msgObj.arg2);
          break;
        case ProgramResources.MAINGUI_UPD_CHANTEXT:        //set value for freqCodeTextView
          setFreqCodeTextViewStr((String)msgObj.obj);
          break;
        case ProgramResources.MAINGUI_UPD_POPUPMSG:        //show popup message
          Toast.makeText(getActivity().getApplicationContext(),
                                                   (String)msgObj.obj,Toast.LENGTH_SHORT).show();
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
        default:                                 //unknown value
          super.handleMessage(msgObj);           //pass to parent handler
      }
    }
  }
}
