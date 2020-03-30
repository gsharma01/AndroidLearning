package com.example.learnapp

import android.content.ContentValues.TAG
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zebra.rfid.api3.*


/*import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;*/


class MainActivity : AppCompatActivity(), Readers.RFIDReaderEventHandler {

    public var readers: Readers? = null

    //private val availableRFIDReaderList: ArrayList? = null
    private val readerDevice: ReaderDevice? = null
    private val reader: RFIDReader? = null
    private val TAG = "DEMO"


    var textView: TextView? = null
    var tagList = ArrayList<String>()
    //var mobileArray = arrayOf("Android", "IPhone", "WindowsMobile", "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X" )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //val myView = findViewById<View>(android.R.id.mobile_list) as ListView
        val adapter: ArrayAdapter<*> = ArrayAdapter(
            this,
            R.layout.inventory_row, tagList
        )
        val listView =
            findViewById<View>(R.id.mobile_list) as ListView
        listView.adapter = adapter


        Toast.makeText(this, "Main View", Toast.LENGTH_SHORT).show()

        if (savedInstanceState == null) {

            //eventHandler = EventHandler()
            //initializeConnectionSettings()
        }

        readers = Readers(this, ENUM_TRANSPORT.BLUETOOTH)
        Readers.attach(this)
        LoadReader(this@MainActivity, readers,listView).execute()

        //val readersListArray = readers!!.GetAvailableRFIDReaderList()
        //val readerDevice: ReaderDevice = readersListArray[0];
        //var rfidReader = readerDevice.rfidReader
        //rfidReader.connect();
        //textView?.setText("Reader connected");

        //val myTags = rfidReader.Actions.getReadTags(100);
        //rfidReader.Actions.Inventory.perform()

        //Thread.sleep(5000);

        // stop the inventory
        //rfidReader.Actions.Inventory.stop();
       // val myTags = reader!!.Actions.getReadTags(100)

    }

    class LoadReader(mainContext: Context, readers: Readers?, mainList:ListView) : AsyncTask<Void?, Void?, Boolean?>() {
        val mainActivity = mainContext
        var rfidReaders = readers
        var lstMain = mainList

        private var eventHandler: EventHandler? = null

        override fun onPostExecute(aBoolean: Boolean?) {
            super.onPostExecute(aBoolean)
            //AutoConnectDevice()
            Toast.makeText(mainActivity, "Reader Connected", Toast.LENGTH_LONG).show();
            //textView.setText("Reader connected");
        }

        override fun doInBackground(vararg params: Void?): Boolean? {

            var invalidUsageException: InvalidUsageException? = null
            try {
                if (rfidReaders!= null) {
                    val readersListArray = rfidReaders!!.GetAvailableRFIDReaderList()
                    if (readersListArray.count() > 0) {

                        // get first reader from list
                        val readerDevice = readersListArray[0];
                        val reader = readerDevice.getRFIDReader();
                        if (!reader.isConnected()) {

                            // Establish connection to the RFID Reader
                            reader.connect();
                            ConfigureReader(reader);
                            return true;
                        }
                    }
                }
            } catch (e: InvalidUsageException) {
                e.printStackTrace()
                invalidUsageException = e
            }
            /*if (invalidUsageException != null) {
                 readers!!.Dispose()
                 readers = null
                 if (!MainActivity.isBluetoothEnabled()) {
                     val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                     startActivity(enableIntent)
                 }
                 Application.isReaderConnectedThroughBluetooth = true
                 readers = Readers(context, ENUM_TRANSPORT.BLUETOOTH)
                 readers.attach(context)
             }*/
            return null
        }

        private fun ConfigureReader(reader: RFIDReader) {
            if (reader.isConnected()) {
                val triggerInfo = TriggerInfo()
                triggerInfo.StartTrigger.triggerType =
                    START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE
                triggerInfo.StopTrigger.triggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE
                try {
                    // receive events from reader
                     if (eventHandler == null) eventHandler = EventHandler(reader,lstMain,mainActivity )
                     reader.Events.addEventsListener(eventHandler)
                     // HH event
                     reader.Events.setHandheldEvent(true)
                     // tag event with tag data
                     reader.Events.setTagReadEvent(true)
                     // application will collect tag using getReadTags API
                     reader.Events.setAttachTagDataWithReadEvent(false)
                    // set trigger mode as rfid so scanner beam will not come
                    reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
                    // set start and stop triggers
                    reader.Config.setStartTrigger(triggerInfo.StartTrigger)
                    reader.Config.setStopTrigger(triggerInfo.StopTrigger)
                } catch (e: InvalidUsageException) {
                    e.printStackTrace()
                } catch (e: OperationFailureException) {
                    e.printStackTrace()
                }
            }
        }
    }



    override fun RFIDReaderDisappeared(p0: ReaderDevice?) {
        TODO("Not yet implemented")
    }

    override fun RFIDReaderAppeared(p0: ReaderDevice?) {
        TODO("Not yet implemented")
    }


}


    public class EventHandler(reader:RFIDReader,  mainView:ListView, mainContext: Context) : RfidEventsListener {
        private val mConnectedReader=reader
        var lstMainView = mainView
        val mainActivity = mainContext

        override fun eventReadNotify(e: RfidReadEvents) {
            var tagList = ArrayList<String>()
            if (mConnectedReader != null) {
                val myTags = mConnectedReader.Actions.getReadTags(100);
                if (myTags != null) {
                    for (index in 0.. myTags.size-1) {
                        Log.d(TAG, "Tag ID " + myTags[index].getTagID());
                        tagList.add((myTags[index].getTagID()))
                    }


                    val adapter: ArrayAdapter<*> = ArrayAdapter(
                        mainActivity,
                        R.layout.inventory_row, tagList
                    )

                }
            }
        }

        override fun eventStatusNotify(rfidEvents: RfidStatusEvents?) {
            Log.d(TAG, "Status Notification: " + rfidEvents?.StatusEventData?.getStatusEventType())
            if (rfidEvents?.StatusEventData?.getStatusEventType() === STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidEvents?.StatusEventData?.HandheldTriggerEventData?.getHandheldEvent() === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED)
                {
                    object:AsyncTask<Void?, Void?, Boolean?>() {
                        override fun doInBackground(vararg params: Void?): Boolean? {
                            try
                            {
                                mConnectedReader.Actions.Inventory.perform()
                            }
                            catch (e:InvalidUsageException) {
                                e.printStackTrace()
                            }
                            catch (e:OperationFailureException) {
                                e.printStackTrace()
                            }
                            return null
                        }
                    }.execute()
                }
                if (rfidEvents?.StatusEventData?.HandheldTriggerEventData?.getHandheldEvent() === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    object : AsyncTask<Void?, Void?, Boolean?>() {
                        override fun doInBackground(vararg params: Void?): Boolean? {
                            try {
                                mConnectedReader.Actions.Inventory.stop()
                            } catch (e: InvalidUsageException) {
                                e.printStackTrace()
                            } catch (e: OperationFailureException) {
                                e.printStackTrace()
                            }
                            return null
                        }
                    }.execute()
                }
            }

            }
        }




/*
    override fun RFIDReaderDisappeared(p0: ReaderDevice?) {
        /*if (RFIDController.autoConnectDeviceTask != null) {
            RFIDController.autoConnectDeviceTask.cancel(true)
        }
        mReaderDisappeared = device
        val fragment: Fragment? =
            supportFragmentManager.findFragmentByTag(MainActivity.TAG_CONTENT_FRAGMENT)
        if (fragment is ReadersListFragment) (fragment as ReadersListFragment?).RFIDReaderDisappeared(
            device
        )
        if (NOTIFY_READER_AVAILABLE) sendNotification(
            Constants.ACTION_READER_AVAILABLE,
            device.getName() + " is unavailable."
        )*/
    }

    override fun RFIDReaderAppeared(p0: ReaderDevice?) {
        TODO("Not yet implemented")
    }


}*/
