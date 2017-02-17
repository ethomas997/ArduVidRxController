//BluetoothSerialService.java:  Bluetooth communications manager.
//
// 10/22/2016 -- [ET]  File modified from BlueTerm project.

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.etheli.arduvidrx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothSerialService {
    // Debugging
    private static final String TAG = "BluetoothSerialService";
    private static final boolean D = false;

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;

	private static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
	private BluetoothAdapter mBluetoothAdapter = null;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
	private boolean mEnablingFlag = false;

    private boolean mAllowInsecureConnections;

    private DataWriteReceiver mDataWriteReceiverObj;
    private Activity mParentActivityObj;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Key names sent from the BluetoothReadService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String SHOW_TEXT = "showText";

    // Message types sent from the BluetoothReadService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_INFO = 4;
    public static final int MESSAGE_SHOWTEXT = 5;

    /**
     * Constructor. Prepares a new service session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     * @param dataWriteRecObj object that will receive data directly from the serial channel.
     */
    public BluetoothSerialService(Activity context, Handler handler, DataWriteReceiver dataWriteRecObj) {
        mState = STATE_NONE;
        mHandler = handler;
        mDataWriteReceiverObj = dataWriteRecObj;
        mParentActivityObj = context;
        mAllowInsecureConnections = true;
    }

    /**
     * Starts or resumes the service.
     */
    public synchronized void startResumeService() {
        if(mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                showNoBluetoothDialog();
                return;
            }
        }

		if (!mEnablingFlag) {     //enabling of adapter not already attempted
		    if (!mBluetoothAdapter.isEnabled()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivityObj);
                builder.setMessage(R.string.alert_dialog_turn_on_bt)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.alert_dialog_warning_title)
                    .setCancelable( false )
                    .setPositiveButton(R.string.alert_dialog_yes, new DialogInterface.OnClickListener() {
                    	public void onClick(DialogInterface dialog, int id) {
                    		mEnablingFlag = true;
                    		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    		mParentActivityObj.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    	}
                    })
                    .setNegativeButton(R.string.alert_dialog_no, new DialogInterface.OnClickListener() {
                    	public void onClick(DialogInterface dialog, int id) {
                    		showNoBluetoothDialog();
                    	}
                    });
                AlertDialog alert = builder.create();
                alert.show();
		    }

            if (mState == STATE_NONE) {          //if not started already
                   //Start the service. Specifically start AcceptThread to begin a
                   // session in listening (server) mode
                if (D) Log.d(TAG, "start");

                // Cancel any thread attempting to make a connection
                if (mConnectThread != null) {
                    mConnectThread.cancel();
                    mConnectThread = null;
                }
                // Cancel any thread currently running a connection
                if (mConnectedThread != null) {
                    mConnectedThread.cancel();
                    mConnectedThread = null;
                }
                setState(STATE_NONE);
            }
        }
    }

    /**
     * Performs select/connect action.
     */
    public void doConnectDeviceAction() {
        if (mState == STATE_NONE) {
            // Launch the DeviceListActivity to see devices and do scan
            final Intent intentObj = new Intent(mParentActivityObj, DeviceListActivity.class);
            mParentActivityObj.startActivityForResult(intentObj,
                                                 BluetoothSerialService.REQUEST_CONNECT_DEVICE);
        }
    }

    /**
     * Performs disconnect action.
     */
    public void doDisconnectDeviceAction() {
        if (mState == STATE_CONNECTED) {
            stop();
            startResumeService();
        }
    }

    /**
     * Processes the result of the DeviceListActivity.
     */
    public void processReqConnDevResult(int resultCode, Intent data) {
        // When DeviceListActivity returns with a device to connect
        if (resultCode == Activity.RESULT_OK) {
            // Get the device MAC address
            String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            // Attempt to connect to the device
            connectToDeviceAddress(address);
        }
    }

    /**
     * Connects to the given device.
     * @param devAddrStr MAC address of device.
     */
    public void connectToDeviceAddress(String devAddrStr) {
        if (mBluetoothAdapter != null) {
            // Get the BluetoothDevice object
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(devAddrStr);
            // Attempt to connect to the device
            connect(device);
        }
    }

    /**
     * Processes the result of the BluetoothAdapter.ACTION_REQUEST_ENABLE activity.
     */
    public void processReqEnableBtResult(int resultCode, Intent data) {
        // When the request to enable Bluetooth returns
        if (resultCode != Activity.RESULT_OK) {
            showNoBluetoothDialog();
        }
    }

    public void showNoBluetoothDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivityObj);
        builder.setMessage(R.string.alert_dialog_no_bt)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle(R.string.app_name)
        .setCancelable( false )
        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
//                       finish();
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Set the current state of the connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        // Create message object
        final Message msgObj = mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1);
        // Add device info (if available) to message object
        final BluetoothDevice devObj;
        if(mConnectThread != null && (devObj=mConnectThread.getDeviceObj()) != null) {
            final Bundle bundleObj = new Bundle();
            bundleObj.putString(DEVICE_NAME,devObj.getName());
            bundleObj.putString(DEVICE_ADDRESS,devObj.getAddress());
            msgObj.setData(bundleObj);
        }
        // Give the new state to the Handler so the UI Activity can update
        msgObj.sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
        	mConnectThread.cancel();
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel();
        	mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        final Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_INFO);
        final Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        bundle.putString(DEVICE_ADDRESS, device.getAddress());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");


        if (mConnectThread != null) {
        	mConnectThread.cancel();
        	mConnectThread = null;
        }

        if (mConnectedThread != null) {
        	mConnectedThread.cancel();
        	mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_NONE);
        String msgStr = mParentActivityObj.getString(R.string.nsg_unable_to_connect);
        // Add device name (if available) to message text
        final BluetoothDevice devObj;
        if(mConnectThread != null && (devObj=mConnectThread.getDeviceObj()) != null) {
            msgStr += " " + devObj.getName();
        }
        // Send a failure message back to the Activity
        final Message msg = mHandler.obtainMessage(MESSAGE_SHOWTEXT);
        final Bundle bundle = new Bundle();
        bundle.putString(SHOW_TEXT, msgStr);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost(boolean canceledFlag) {
        setState(STATE_NONE);

        if(!canceledFlag) {       //if not canceled by user
            // Send a failure message back to the Activity
            Message msg = mHandler.obtainMessage(MESSAGE_SHOWTEXT);
            Bundle bundle = new Bundle();
            bundle.putString(SHOW_TEXT, mParentActivityObj.getString(R.string.msg_connection_lost));
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
            	if ( mAllowInsecureConnections ) {
            		Method method;

            		method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class } );
                    tmp = (BluetoothSocket) method.invoke(device, 1);
            	}
            	else {
            		tmp = device.createRfcommSocketToServiceRecord( SerialPortServiceClass_UUID );
            	}
            } catch (Exception e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            final BluetoothAdapter btAdapterObj;
            if((btAdapterObj=BluetoothAdapter.getDefaultAdapter()) != null)
                btAdapterObj.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                //BluetoothSerialService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothSerialService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

        public BluetoothDevice getDeviceObj() {
            return mmDevice;
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean mmCanceledFlag = false;


        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    mDataWriteReceiverObj.write(buffer, bytes);
                    // Send the obtained bytes to the UI Activity
                    //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    if(mmCanceledFlag)
                      Log.d(TAG, "disconnected by user");
                    else
                      Log.e(TAG, "disconnected", e);
                    connectionLost(mmCanceledFlag);
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MESSAGE_WRITE, buffer.length, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            mmCanceledFlag = true;
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    public void setAllowInsecureConnections( boolean allowInsecureConnections ) {
    	mAllowInsecureConnections = allowInsecureConnections;
    }

    public boolean getAllowInsecureConnections() {
    	return mAllowInsecureConnections;
    }

}
