//ConnectFragment.java:  Defines the initial connection screen and functions.
//
//  2/16/2017 -- [ET]
//

package com.etheli.arduvidrx;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.etheli.util.GuiUtils;

/**
 * Class ConnectFragment defines the initial connection screen and functions.
 */
public class ConnectFragment extends Fragment
{
  private final ProgramResources programResourcesObj = ProgramResources.getProgramResourcesObj();
  private TextView connectTextViewObj = null;
  private String lastDeviceAddressString = null;

  /**
   * Creates the view for the initial connection screen.
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
    final View viewObj = inflater.inflate(R.layout.connect_fragment,container,false);
    Button buttonObj;
    if((buttonObj=(Button)viewObj.findViewById(R.id.connectButton)) != null)
    {  //button found OK; setup click action
      buttonObj.setOnClickListener(
          new View.OnClickListener()
            {
              @Override
              public void onClick(View vObj)
              {
                connectDevicesButtonAction(false);    //'Connect' button
              }
            });
    }
    if((buttonObj=(Button)viewObj.findViewById(R.id.devicesButton)) != null)
    {  //button found OK; setup click action
      buttonObj.setOnClickListener(
          new View.OnClickListener()
            {
              @Override
              public void onClick(View vObj)
              {
                connectDevicesButtonAction(true);     //'Devices' button
              }
            });
    }
    if((buttonObj=(Button)viewObj.findViewById(R.id.connTestButton)) != null)
    {  //button found OK; setup click action
      buttonObj.setOnClickListener(
          new View.OnClickListener()
            {
              @Override
              public void onClick(View vObj)
              {
                connectTestButtonAction();            //'Test' button
              }
            });
    }
    return viewObj;
  }

  /**
   * Called when the fragment is visible to the user.
   */
  @Override
  public void onStart()
  {
    super.onStart();
    GuiUtils.setViewButtonsEnabledState(getView(),true);   //make sure buttons enabled
    connectTextViewObj = (TextView)getActivity().findViewById(R.id.connectTextView);
    if(connectTextViewObj != null)
      connectTextViewObj.setText("");       //clear any previous status message
  }

  /**
   * Enters information about the last device that was connected to.
   * @param devNameStr device name.
   * @param devAddrStr device MAC address.
   */
  public void setLastDeviceInfo(String devNameStr, String devAddrStr)
  {
    lastDeviceAddressString = devAddrStr;
  }

  /**
   * Invoked via the MainActivity when messages are received from the BluetoothSerialService.
   * @param msgObj message data object.
   */
  public void updateConnectStatus(Message msgObj)
  {
    switch(msgObj.what)
    {
      case BluetoothSerialService.MESSAGE_STATE_CHANGE:
        switch(msgObj.arg1)
        {
          case BluetoothSerialService.STATE_CONNECTED:
            if(connectTextViewObj != null)
              connectTextViewObj.setText("");         //clear status message
            break;
          case BluetoothSerialService.STATE_CONNECTING:
            if(connectTextViewObj != null)
            {  //handle to connect-status TextView OK
              String msgStr = getString(R.string.msg_connecting);
              final String nameStr;         //add device name (if available) to message
              if((nameStr=msgObj.getData().getString(BluetoothSerialService.DEVICE_NAME)) != null)
                msgStr += " " + nameStr;
              connectTextViewObj.setText(msgStr + "...");
            }
                   //clear so if connect fails next 'Connect' click shows devices:
            lastDeviceAddressString = null;
            break;
          case BluetoothSerialService.STATE_LISTEN:
          case BluetoothSerialService.STATE_NONE:
                   //connect attempt failed; re-enable buttons:
            GuiUtils.setViewButtonsEnabledState(getView(),true);
            break;
        }
        break;
//      case BluetoothSerialService.MESSAGE_DEVICE_INFO:
//        break;
      case BluetoothSerialService.MESSAGE_SHOWTEXT:
        if(connectTextViewObj != null)
        {
          connectTextViewObj.setText(       //show message in TextView
              msgObj.getData().getString(BluetoothSerialService.SHOW_TEXT));
        }
        break;
    }
  }

  /**
   * Called when the user clicks the 'Connect' or the 'Devices' button.
   * @param devButtonFlag true if invoked via 'Devices' button (and select-device
   * activity should be shown).
   */
  private void connectDevicesButtonAction(boolean devButtonFlag)
  {
    final BluetoothSerialService btServiceObj;
    if((btServiceObj=programResourcesObj.getBluetoothSerialServiceObj()) != null)
    {
      GuiUtils.setViewButtonsEnabledState(getView(),false);  //disable buttons while connecting
      if(connectTextViewObj != null)
        connectTextViewObj.setText("");          //clear any previous status message
      if(devButtonFlag || lastDeviceAddressString == null ||
                                                    lastDeviceAddressString.trim().length() <= 0)
      {  //invoked via 'Devices' button or no last-device-address value available
        btServiceObj.doConnectDeviceAction();    //show select-device activity
      }
      else
      {  //invoked via 'Connect' button and last-device-address value is available
                                                 //attempt connection to device address:
        btServiceObj.connectToDeviceAddress(lastDeviceAddressString);
      }
    }
  }

  /**
   * Called when the user clicks the 'Test' button.
   */
  private void connectTestButtonAction()
  {
    final BluetoothSerialService btServiceObj;
    if((btServiceObj=programResourcesObj.getBluetoothSerialServiceObj()) != null)
    {  //send message-state-connected message to main activity (for test purposes)
      btServiceObj.testSendStateConnectedMessage();
    }
  }
}
