package com.example.learnapp


import android.util.Log
import androidx.constraintlayout.widget.Constraints.TAG
import com.zebra.rfid.api3.*

public class InventoryController (reader: RFIDReader)
{

    var mConnectedReader= reader//getReader(readers)

    fun performInventory() {
        // tag event with tag data
        mConnectedReader?.Events?.setTagReadEvent(true);
        // application will collect tag using getReadTags API
        mConnectedReader?.Events?.setAttachTagDataWithReadEvent(false);

        var triggerInfo= TriggerInfo()
        triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
        triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
        // set start and stop triggers
        mConnectedReader?.Config?.setStartTrigger(triggerInfo.StartTrigger);
        mConnectedReader?.Config?.setStopTrigger(triggerInfo.StopTrigger);

        try {
            // perform simple inventory
            mConnectedReader?.Actions?.Inventory?.perform()

            // Sleep or wait
            Thread.sleep(5000)

            // stop the inventory
            mConnectedReader?.Actions?.Inventory?.stop()
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }


    fun getReader(rfidReaders: Readers) : RFIDReader?
    {
        val readersListArray = rfidReaders!!.GetAvailableRFIDReaderList()
        var reader: RFIDReader? = null
        if (readersListArray.count() > 0) {

            // get first reader from list
            val readerDevice = readersListArray[0];
            reader = readerDevice.getRFIDReader();
            if (!reader.isConnected()) {

                // Establish connection to the RFID Reader
                reader.connect();
                //ConfigureReader();

            }
        }
        return reader
    }

    fun eventReadNotify(e: RfidReadEvents?, reader:RFIDReader) {
        val myTags: Array<TagData> = reader.Actions.getReadTags(100)
        if (myTags != null) {
            for (index in myTags.indices) {
                Log.d(TAG, "Tag ID " + myTags[index].tagID)
            }
        }
    }

}