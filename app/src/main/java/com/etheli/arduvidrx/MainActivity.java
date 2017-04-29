//MainActivity.java:  Defines the main activity for the ArduVidRx Controller.
//
//  4/28/2017 -- [ET]
//

package com.etheli.arduvidrx;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.etheli.util.SwipeGestureDispatcher;

/**
 * Class MainActivity defines the main activity for the ArduVidRx Controller.
 */
public class MainActivity extends Activity
{
    /** Tag string for logging. */
  public static final String LOG_TAG = "MainActivity";
    /** Name for SharedPreferences access. */
  public static final String SHARED_PREFS_NAME = "ArduVidRxPrefs";
    /** SharedPreferences key for last-connected bluetooth device name. */
  public static final String LAST_BTDEV_NAME = "last_btdev_name";
    /** SharedPreferences key for last-connected bluetooth device MAC address. */
  public static final String LAST_BTDEV_ADDRESS = "last_btdev_address";

  private final ProgramResources programResourcesObj = ProgramResources.getProgramResourcesObj();
  private final FrequencyTable videoFrequencyTableObj = new FrequencyTable();
  private final SwipeGestureDispatcher swipeGestureDispatcherObj =  new SwipeGestureDispatcher();
  private View fragementContainerViewObj = null;
  private BluetoothSerialService bluetoothSerialServiceObj = null;
  private VidReceiverManager vidReceiverManagerObj = null;
  private ConnectFragment connectFragmentObj = null;
  private int bluetoothSerServiceState = BluetoothSerialService.STATE_NONE;
  private String lastDeviceNameString = null;
  private String lastDeviceAddressString = null;

  /**
   * Called when the activity is starting.
   * @param savedInstanceState Bundle: If the activity is being re-initialized after
   * previously being shut down then this Bundle contains the data it most recently
   * supplied in onSaveInstanceState(Bundle).  Otherwise it is null.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    loadPersistentSettings();
    setContentView(R.layout.fragment_container);      //show content frame

    if((bluetoothSerialServiceObj=programResourcesObj.getBluetoothSerialServiceObj()) == null ||
                  (vidReceiverManagerObj=programResourcesObj.getVidReceiverManagerObj()) == null)
    {  //resources not created via previous instance; create them now
      bluetoothSerialServiceObj = new BluetoothSerialService(
                                                  this,bluetoothHandlerObj,bluetoothWriteRecObj);
      programResourcesObj.setBluetoothSerialServiceObj(bluetoothSerialServiceObj);
      vidReceiverManagerObj = new VidReceiverManager(this,bluetoothSerialServiceObj);
      programResourcesObj.setVidReceiverManagerObj(vidReceiverManagerObj);
      programResourcesObj.setFrequencyTableObj(videoFrequencyTableObj);
      setupTerminalStartupAction();    //setup startup action for terminal
    }
    else  //resources created via previous instance
      ensureConnectionClosed();        //make sure connection not stuck

         //show ConnectFragement:
    if((fragementContainerViewObj=findViewById(R.id.fragment_container)) != null)
    {  //able to locate 'fragment_container' OK
      // if we're being restored from a previous state,
      // then we don't need to do anything and should return or else
      // we could end up with overlapping fragments
      if (savedInstanceState != null)
          return;
      connectFragmentObj = new ConnectFragment();
      connectFragmentObj.setLastDeviceInfo(lastDeviceNameString,lastDeviceAddressString);
         //pass any Intent extras to fragment as arguments:
//      connectFragObj.setArguments(getIntent().getExtras());
      getFragmentManager().beginTransaction().        //add fragment to frame
                   add(R.id.fragment_container,connectFragmentObj).commit();
    }
  }

  /**
   * Called when the activity is ready to interact with the user.
   */
  @Override
  public void onResume()
  {
    super.onResume();
    if(bluetoothSerialServiceObj != null)
    {  //service has been created
      bluetoothSerialServiceObj.setDestinationObjects(     //set objects for this instance
                                                  this,bluetoothHandlerObj,bluetoothWriteRecObj);
              //if local 'bluetoothSerServiceState' is "connected" but service state
              // is "not connected" then test mode is active; otherwise do start/resume:
      if(bluetoothSerServiceState != BluetoothSerialService.STATE_CONNECTED ||
                  bluetoothSerialServiceObj.getState() == BluetoothSerialService.STATE_CONNECTED)
      {
        bluetoothSerialServiceObj.startResumeService();
      }
    }
  }

  /**
   * Performs final cleanup (disconnects Bluetooth).
   */
  @Override
  public void onDestroy()
  {
    if(bluetoothSerialServiceObj != null)
    {
      if(!programResourcesObj.isTerminalActive())     //if terminal not running then
        bluetoothSerialServiceObj.stop();             //stop service
      bluetoothSerialServiceObj.clearDestinationObjects(   //release objects for this instance
                                                       bluetoothHandlerObj,bluetoothWriteRecObj);
    }
    super.onDestroy();
  }

  /**
   * Initializes the contents of the Activity's options menu.
   * @param menu Menu: The options menu in which you place your items.
   * @return true (for the menu to be displayed).
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_option_menu, menu);
    return true;
  }

  /**
   * Hook called when an item in the options menu is selected.
   * @param item MenuItem: The menu item that was selected.
   * @return false to allow normal menu processing to proceed, true to consume it here.
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch(item.getItemId())
    {
      case R.id.menu_about:
        showAboutDialog();
        return true;
    }
    return false;
  }

  /**
   * Displays the "About" dialog.
   */
  private void showAboutDialog()
  {
    final Dialog aboutDialog = new Dialog(this);
    aboutDialog.setContentView(R.layout.about);
    aboutDialog.setTitle(getString(R.string.app_name) + " " + getString(R.string.app_version));
    final Button buttonObj = (Button) aboutDialog.findViewById(R.id.buttonDialog);
    buttonObj.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            aboutDialog.dismiss();
          }
        });
    aboutDialog.show();
  }

  /**
   * Loads settings from persistent storage.
   */
  private void loadPersistentSettings()
  {
    try
    {
      final SharedPreferences sPrefsObj = getSharedPreferences(SHARED_PREFS_NAME,0);
      lastDeviceNameString = sPrefsObj.getString(LAST_BTDEV_NAME,null);
      lastDeviceAddressString = sPrefsObj.getString(LAST_BTDEV_ADDRESS,null);
    }
    catch(Exception ex)
    {  //some kind of exception reading prefs; ignore and move one
    }
  }

  /**
   * Saves 'device' settings to persistent storage.
   */
  private void saveDevPersistentSettings()
  {
    try
    {
      final SharedPreferences sPrefsObj = getSharedPreferences(SHARED_PREFS_NAME,0);
      final SharedPreferences.Editor prefsEdObj = sPrefsObj.edit();
      prefsEdObj.putString(LAST_BTDEV_NAME,lastDeviceNameString);
      prefsEdObj.putString(LAST_BTDEV_ADDRESS,lastDeviceAddressString);
      prefsEdObj.commit();
    }
    catch(Exception ex)
    {  //some kind of exception reading prefs; ignore and move one
    }
  }

  //Shows the OperationFragment and starts the video-receiver manager.
  private void startReceiverOperations()
  {
    try
    {
      final OperationFragment opFragObj = new OperationFragment();
//      Bundle args = new Bundle();
//      args.putInt(ArticleFragment.ARG_POSITION, position);
//      opFragObj.setArguments(args);
      final FragmentTransaction fragTransObj = getFragmentManager().beginTransaction();
      fragTransObj.replace(R.id.fragment_container,opFragObj);    //swap in new fragment
      fragTransObj.commit();
      if(vidReceiverManagerObj != null)
        vidReceiverManagerObj.startManager();      //start receiver-manager threads
           //if tabs then setup so operation-fragment TabHost responds to swipe gestures:
      if(opFragObj.isTabHostInUse())
        swipeGestureDispatcherObj.setSwipeGestureEventIntfObj(opFragObj);
    }
    catch(Exception ex)
    {  //some kind of exception error; log it
      Log.e(LOG_TAG, "Exception in startReceiverOperations()", ex);
    }
  }

  //Stops the video-receiver manager and shows the ConnectFragment.
  private void stopReceiverOperations()
  {
    try
    {
      ensureConnectionClosed();        //make sure receiver and bluetooth are disconnected
              //clear swipe-gesture dispatches to operation-fragment TabHost:
      swipeGestureDispatcherObj.setSwipeGestureEventIntfObj(null);
      if(!isFinishing())
      {  //activity not exiting ('onDestroy()' not called)
        final FragmentManager fragMgrObj = getFragmentManager();
        final Fragment fObj = fragMgrObj.findFragmentById(R.id.fragment_container);
        if((!(fObj instanceof ConnectFragment)) || !fObj.isVisible())
        {  //ConnectFragment not currently showing; show it now
          connectFragmentObj = new ConnectFragment();    //create new fragment
          connectFragmentObj.setLastDeviceInfo(lastDeviceNameString,lastDeviceAddressString);
          final FragmentTransaction fragTransObj = fragMgrObj.beginTransaction();
          fragTransObj.replace(R.id.fragment_container,connectFragmentObj);  //swap in new fragment
          fragTransObj.commit();
        }
        else       //ConnectFragment already displayed; make sure buttons are enabled
          ((ConnectFragment)fObj).enableFragmentButtons();
      }
      else if(connectFragmentObj != null)             //if connect fragment still around then
        connectFragmentObj.enableFragmentButtons();   //make sure buttons are enabled
    }
    catch(Exception ex)
    {  //some kind of exception error; log it
      Log.e(LOG_TAG, "Exception in stopReceiverOperations()", ex);
    }
  }

  /**
   * Makes sure that the video-receiver manager is stopped and the
   * bluetooth-serial service is disconnected.
   */
  private void ensureConnectionClosed()
  {
    ProgramResources progResObj = null;
    try
    {
      progResObj = ProgramResources.getProgramResourcesObj();
      final VidReceiverManager mgrObj;
      if((mgrObj=progResObj.getVidReceiverManagerObj()) != null)
        mgrObj.stopManager();
    }
    catch(Exception ex)
    {  //some kind of exception error; log it
      Log.e(LOG_TAG, "Exception in ensureConnectionClosed() vidRecMgr", ex);
    }
    try
    {
      final BluetoothSerialService serviceObj;
      if((serviceObj=progResObj.getBluetoothSerialServiceObj()) != null)
        serviceObj.doDisconnectDeviceAction();
    }
    catch(Exception ex)
    {  //some kind of exception error; log it
      Log.e(LOG_TAG, "Exception in ensureConnectionClosed() bluetooth", ex);
    }
  }

  /**
   * Sets up the action to be taken when the terminal is started.
   */
  private void setupTerminalStartupAction()
  {
    ProgramResources.getProgramResourcesObj().setTerminalStartupActionObj(
        new Runnable()
          {
            @Override
            public void run()
            {
              final VidReceiverManager vidRecMgrObj =
                            ProgramResources.getProgramResourcesObj().getVidReceiverManagerObj();
              if(vidRecMgrObj != null)
              {
                vidRecMgrObj.pauseReceiverUpdateWorker();       //pause update worker
                vidRecMgrObj.outputReceiverEchoCommand(true);   //send echo-on
                vidRecMgrObj.outputQueryVersionCmd();      //show version info in terminal
              }
            }
          });
  }

  /**
   * Receives the result of the DeviceListActivity or the
   * BluetoothAdapter.ACTION_REQUEST_ENABLE activity (via BluetoothSerialService).
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    switch(requestCode)
    {
      case BluetoothSerialService.REQUEST_CONNECT_DEVICE:
        if(bluetoothSerialServiceObj != null)    //dispatch event to Bluetooth service
          bluetoothSerialServiceObj.processReqConnDevResult(resultCode,data);
        break;
      case BluetoothSerialService.REQUEST_ENABLE_BT:
        if(bluetoothSerialServiceObj != null)    //dispatch event to Bluetooth service
          bluetoothSerialServiceObj.processReqEnableBtResult(resultCode,data);
        break;
    }
  }

  /**
   * Called when touch-screen events are processed.  Overridden to intercept
   * all touch-screen events before they are dispatched to the activity.
   * The SwipeGestureDispatcher will detected 'swipe' events and pass them
   * on to the listener.
   * @param event MotionEvent: The touch screen event.
   * @return Return true if the event was consumed.
   */
  @Override
  public boolean dispatchTouchEvent(MotionEvent event)
  {
    try
    {              //process touch event (will be dispatched if 'swipe' event):
      swipeGestureDispatcherObj.onTouch(fragementContainerViewObj,event);
    }
    catch(Exception ex)
    {  //some kind of exception error; log it and move on
      Log.e(LOG_TAG, "Exception in swipeGestureDispatcherObj.onTouch()", ex);
    }
    return super.dispatchTouchEvent(event);      //pass event to activity
  }

  //Handler that gets information back from the BluetoothSerialService
  private final Handler bluetoothHandlerObj =
        new Handler(Looper.getMainLooper())           //run on UI thread
          {
            @Override
            public void handleMessage(Message msgObj)
            {
              try
              {
                switch(msgObj.what)
                {
                  case BluetoothSerialService.MESSAGE_STATE_CHANGE:
                    switch(msgObj.arg1)
                    {
                      case BluetoothSerialService.STATE_CONNECTED:
                        connectFragmentObj = null;         //release ConnectFragment object
                        startReceiverOperations();
                        break;
                      case BluetoothSerialService.STATE_CONNECTING:
                        if(connectFragmentObj == null)
                        {  //ConnectFragment not active (to display message); show as popup
                          Toast.makeText(getApplicationContext(),
                                  getString(R.string.msg_connecting), Toast.LENGTH_SHORT).show();
                        }
                        lastDeviceNameString = null;       //clear so if connect fails next
                        lastDeviceAddressString = null;    // 'Connect' click will show devices
                        break;
                      case BluetoothSerialService.STATE_LISTEN:
                      case BluetoothSerialService.STATE_NONE:
                        if(bluetoothSerServiceState != BluetoothSerialService.STATE_NONE)
                        {  //going from connected to not connected
                          stopReceiverOperations();   //stop vid-manager and show ConnectFragment
                        }
                        break;
                    }
                    bluetoothSerServiceState = msgObj.arg1;     //track state
                    break;
                  case BluetoothSerialService.MESSAGE_DEVICE_INFO:
                    // save connected device's info
                    lastDeviceNameString =
                                  msgObj.getData().getString(BluetoothSerialService.DEVICE_NAME);
                    lastDeviceAddressString =
                               msgObj.getData().getString(BluetoothSerialService.DEVICE_ADDRESS);
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_connected_to) +
                                          " " + lastDeviceNameString, Toast.LENGTH_SHORT).show();
                    saveDevPersistentSettings();        //save device persistent settings
                    break;
                  case BluetoothSerialService.MESSAGE_SHOWTEXT:
                    if(connectFragmentObj == null)
                    {  //ConnectFragment not active (to display message); show as popup
                      Toast.makeText(getApplicationContext(),
                                    msgObj.getData().getString(BluetoothSerialService.SHOW_TEXT),
                                                                      Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                        //if ConnectFragment still active then dispatch update to it:
                if(connectFragmentObj != null)
                  connectFragmentObj.updateConnectStatus(msgObj);
              }
              catch(Exception ex)
              {  //some kind of exception error; log it
                Log.e(LOG_TAG, "Exception in bluetoothHandlerObj.handleMessage()", ex);
              }
            }
          };

  //Receiver that gets data directly from the BluetoothService
  private final DataWriteReceiver bluetoothWriteRecObj =
          new DataWriteReceiver()
          {
            @Override
            public void write(byte[] buffer, int length)
            {
              try
              {
                if(vidReceiverManagerObj != null)
                  vidReceiverManagerObj.storeReceivedChars(buffer,length);
              }
              catch(Exception ex)
              {  //some kind of exception error; log it
                Log.e(LOG_TAG, "Exception in bluetoothWriteRecObj.handleMessage()", ex);
              }
            }
          };
}
