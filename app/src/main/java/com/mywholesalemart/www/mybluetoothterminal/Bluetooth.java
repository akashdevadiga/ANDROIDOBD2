package com.mywholesalemart.www.mybluetoothterminal;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fr3ts0n.androbd.plugin.mgr.PluginManager;
import com.fr3ts0n.ecu.EcuDataPv;
import com.fr3ts0n.ecu.prot.obd.ElmProt;
import com.fr3ts0n.ecu.prot.obd.ObdProt;
import com.fr3ts0n.pvs.PvChangeEvent;
import com.fr3ts0n.pvs.PvChangeListener;
import com.fr3ts0n.pvs.PvList;
import com.google.gson.Gson;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TreeSet;
import java.util.logging.Level;

import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static android.media.MediaCodec.MetricsConstants.MODE;


public class Bluetooth extends AppCompatActivity  implements BTListAdapter.customButtonListener, PvChangeListener,
        PropertyChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        AbsListView.MultiChoiceModeListener {

    private Switch bluetoothonoff;
    private RelativeLayout rr;
    private TextView tv1, bttv2;
    private TextView text, bsName;
    private BluetoothAdapter mBluetoothAdapter;
    private ListView listView;
    private BTListAdapter adapter;
    private ArrayList<BluetoothDevice> dataItems = new ArrayList<BluetoothDevice>();
    public BroadcastReceiver mReceiver;
    private Dialog dialog;
    private ProgressDialog dialogS;
    private Button generateButton;
    private static boolean isBack = false;
    static String btaddress;

    HashMap<Object,Object> abc = new HashMap();
    HashMap<Object,HashMap<Object,Object>> ecuData = new HashMap<>();

    /**
     * operating modes
     */
    public enum MODE
    {
        OFFLINE,//< OFFLINE mode
        ONLINE,	//< ONLINE mode
        DEMO,	//< DEMO mode
        FILE,   //< FILE mode
    }

    /**
     * Preselection types
     */
    public enum PRESELECT
    {
        LAST_DEV_ADDRESS,
        LAST_ECU_ADDRESS,
        LAST_SERVICE,
        LAST_ITEMS,
        LAST_VIEW_MODE,
    }

    /**
     * Key names for preferences
     */
    public static final String DEVICE_NAME = "device_name";
    private static final String DEVICE_ADDRESS = "device_address";
    private static final String DEVICE_PORT = "device_port";
    public static final String TOAST = "toast";
    private static final String MEASURE_SYSTEM = "measure_system";
    private static final String NIGHT_MODE = "night_mode";
    private static final String ELM_ADAPTIVE_TIMING = "adaptive_timing_mode"; /**********/
    private static final String ELM_RESET_ON_NRC = "elm_reset_on_nrc";
    private static final String PREF_USE_LAST = "USE_LAST_SETTINGS";
    public static final String PREF_AUTOHIDE = "autohide_toolbar";
    private static final String PREF_OVERLAY = "toolbar_overlay";
    public static final String PREF_AUTOHIDE_DELAY = "autohide_delay";
    private static final String PREF_DATA_DISABLE_MAX = "data_disable_max";

    /**
     * Message types sent from the BluetoothChatService Handler
     */
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_FILE_READ = 2;
    private static final int MESSAGE_FILE_WRITTEN = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    private static final int MESSAGE_DATA_ITEMS_CHANGED = 6;
    public static final int MESSAGE_UPDATE_VIEW = 7;
    private static final int MESSAGE_OBD_STATE_CHANGED = 8;
    private static final int MESSAGE_OBD_NUMCODES = 9;
    private static final int MESSAGE_OBD_ECUS = 10;
    private static final int MESSAGE_OBD_NRC = 11;
    public static final int MESSAGE_TOOLBAR_VISIBLE = 12;
    private static final String TAG = "AndrOBD";

    /**
     * Member object for the BT comm services
     */
    private CommService mCommService = null;

    /** Logging */
    private static final Logger rootLogger = Logger.getLogger("");
    private static final Logger log = Logger.getLogger(TAG);

    /** current OBD service */
    private int obdService = ElmProt.OBD_SVC_NONE;
    /**
     * current operating mode
     */
    private MODE mode = Bluetooth.MODE.OFFLINE;

    /** dialog builder */
    private static AlertDialog.Builder dlgBuilder;

    /**
     * app preferences ...
     */
    static SharedPreferences prefs;
    /**
     * Member object for the BT comm services
     */
    //private CommService mCommService = null;
    /**
     * Local Bluetooth adapter
     */
    //private static BluetoothAdapter mBluetoothAdapter = null;
    /**
     * Name of the connected BT device
     */
    private static String mConnectedDeviceName = null;
    /**
     * menu object
     */
    private static Menu menu;
    /**
     * Data list adapters
     */
    private static ObdItemAdapter mPidAdapter;
    private static VidItemAdapter mVidAdapter;
    private static DfcItemAdapter mDfcAdapter;
    private static ObdItemAdapter currDataAdapter;
    /**
     * Timer for display updates
     */
    private static final Timer updateTimer = new Timer();
    /**
     * initial state of bluetooth adapter
     */
    private static boolean initialBtStateEnabled = false;
    /**
     * last time of back key pressed
     */
    private static long lastBackPressTime = 0;
    /**
     * toast for showing exit message
     */
    private static Toast exitToast = null;
    /** file helper */
    private FileHelper fileHelper;
    /** the local list view */
    private View mListView;
    /** current data view mode */
    //private DATA_VIEW_MODE dataViewMode = DATA_VIEW_MODE.LIST;
    /** AutoHider for the toolbar */
    //private AutoHider toolbarAutoHider;
    /** log file handler */
    private FileHandler logFileHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        bluetoothonoff = findViewById(R.id.onff);
        rr = findViewById(R.id.blutoothrr1);
        tv1 = findViewById(R.id.btonoffstatus);
        listView = findViewById(R.id.btlistview);
        bttv2 = findViewById(R.id.selectedDevices);
        generateButton = findViewById(R.id.generateCardBut);
        bsName = findViewById(R.id.busName);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        dlgBuilder = new AlertDialog.Builder(this);

        // set listeners for data structure changes
        setDataListeners();
        // automate elm status display
        CommService.elm.addPropertyChangeListener(this);

         dialogS = new ProgressDialog(this); // this = YourActivity
        dialogS.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialogS.setTitle("Fetching");
        dialogS.setMessage("Fetching. Please wait...");
        dialogS.setIndeterminate(true);
        dialogS.setCanceledOnTouchOutside(false);


        bluetoothonoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean bluetoothOff = !mBluetoothAdapter.isEnabled();

                if (bluetoothOff) {
                    dataItems.clear();
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                } else {
                    if(bluetoothonoff.isChecked()){
                        bluetoothonoff.setChecked(false);
                    }
                    mBluetoothAdapter.disable();
                    dataItems.clear();
                    tv1.setText("Bluetooth Off");
                }
            }
        });


        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                dialog = new Dialog(this);
                dialog.setContentView(R.layout.custom_dialog);
                dialog.setCancelable(false);
                dialog.setTitle("Please Wait");
                TextView text = (TextView) dialog.findViewById(R.id.textViewtv);
                text.setText("Scanning for Devices");
                dialog.show();
                ss.start();

                tv1.setText("Bluetooth On");
                dataItems.clear();
                /*Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                dataItems.addAll(devices);*/
                displayListOfFoundDevices();
            } else if (resultCode == RESULT_CANCELED) {
                dataItems.clear();
                bluetoothonoff.setChecked(false);
                finishActivity(1);
            }
        }
    }

    CountDownTimer ss = new CountDownTimer(30000, 1000) {

        public void onTick(long millisUntilFinished) {

        }

        public void onFinish() {
            try{
                if(dialog != null){
                    if(dialog.isShowing()){
                        dialog.dismiss();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    };

    protected void displayListOfFoundDevices() {
        // start looking for bluetooth devices
        mBluetoothAdapter.startDiscovery();
        findTimer.start();
        isBack = true;
        // Discover new devices
        // Create a BroadcastReceiver for ACTION_FOUND

        mReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    dataItems.add(device);
                }

                for(int i=0;i<dataItems.size();i++){

                    for(int j=i+1;j<dataItems.size();j++){
                        if(dataItems.get(i).equals(dataItems.get(j))){
                            dataItems.remove(j);
                            j--;
                        }
                    }
                }

                /*We can do like this also
                Set<BluetoothDevice> lhs = new LinkedHashSet<BluetoothDevice>(dataItems);
                dataItems.clear();
                dataItems.addAll(lhs);*/

                adapter = new BTListAdapter(Bluetooth.this, dataItems);
                adapter.setCustomButtonListner(Bluetooth.this);
                listView.setAdapter(adapter);
                isBack = false;
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
    }


    CountDownTimer findTimer = new CountDownTimer(20000, 1000) {

        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            mBluetoothAdapter.cancelDiscovery();
            dialog.dismiss();
        }

    };

    @Override
    public void onButtonClickListner(int position, final BluetoothDevice value) {
         btaddress = value.getAddress();

        if(value.getBondState() == BluetoothDevice.BOND_BONDED){
            bttv2.setText("Selected Device is: "+value.getName());

            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(btaddress);
            // Attempt to connect to the device
            mCommService = new BtCommService(this, mHandler);
            mCommService.connect(device, false);


        }
        else {
            value.createBond();
            bttv2.setText("Selected Device is: "+value.getName());

        }
    }


    /**
     * Initiate a connect to the selected bluetooth device
     *
     * @param address bluetooth device address
     * @param secure flag to indicate if the connection shall be secure, or not
     */
    private void connectBtDevice(String address, boolean secure)
    {
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mCommService = new BtCommService(this, mHandler);
        mCommService.connect(device, secure);
    }


    /**
     * Handle message requests
     */
    @SuppressLint("HandlerLeak")
    private transient final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            try
            {
                PropertyChangeEvent evt;

                // log trace message for received handler notification event
                //log.log(Level.FINEST, String.format("Handler notification: %s", msg.toString()));

                switch (msg.what)
                {
                    case MESSAGE_STATE_CHANGE:
                        // log trace message for received handler notification event
                       // log.log(Level.FINEST, String.format("State change: %s", msg.toString()));
                        switch ((CommService.STATE) msg.obj)
                        {
                            case CONNECTED:
                                onConnect();
                                break;

                            case CONNECTING:
                               // setStatus(R.string.title_connecting);
                                break;

                            default:
                                onDisconnect();
                                break;
                        }
                        break;

                    case MESSAGE_FILE_WRITTEN:
                        break;

                    // data has been read - finish up
                    case MESSAGE_FILE_READ:
                        // set listeners for data structure changes
                        setDataListeners();
                        // set adapters data source to loaded list instances
                        mPidAdapter.setPvList(ObdProt.PidPvs);
                        mVidAdapter.setPvList(ObdProt.VidPvs);
                        mDfcAdapter.setPvList(ObdProt.tCodes);

                        // set OBD data mode to the one selected by input file
                        //setObdService(CommService.elm.getService(), "saved data");
                        // Check if last data selection shall be restored
                        /*if(obdService == ObdProt.OBD_SVC_DATA)
                        {
                            checkToRestoreLastDataSelection();
                            checkToRestoreLastViewMode();
                        }*/
                        break;

                    case MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                        Toast.makeText(getApplicationContext(),
                                "Connected TO: " + mConnectedDeviceName,
                                Toast.LENGTH_SHORT).show();
                        break;

                    case MESSAGE_TOAST:
                        Toast.makeText(getApplicationContext(),
                                msg.getData().getString(TOAST),
                                Toast.LENGTH_SHORT).show();
                        break;

                    case MESSAGE_DATA_ITEMS_CHANGED:
                        PvChangeEvent event = (PvChangeEvent) msg.obj;
                        switch (event.getType())
                        {
                            case PvChangeEvent.PV_ADDED:
                                currDataAdapter.setPvList(currDataAdapter.pvs);
                                try
                                {
                                    /*if(event.getSource() == ObdProt.PidPvs)
                                    {
                                        // Check if last data selection shall be restored
                                        checkToRestoreLastDataSelection();
                                        checkToRestoreLastViewMode();
                                    }
                                    // set up data update timer
                                    updateTimer.schedule(updateTask, 0, DISPLAY_UPDATE_TIME);*/
                                } catch (Exception e)
                                {
                                    log.log(Level.FINER, "Error adding PV", e);
                                }
                                break;

                            case PvChangeEvent.PV_CLEARED:
                                currDataAdapter.clear();
                                break;
                        }
                        break;

                    case MESSAGE_UPDATE_VIEW:
                        //getListView().invalidateViews();
                        break;

                    // handle state change in OBD protocol
                    case MESSAGE_OBD_STATE_CHANGED:
                        evt = (PropertyChangeEvent) msg.obj;
                        ElmProt.STAT state = (ElmProt.STAT)evt.getNewValue();
                           // setObdService(ObdProt.OBD_SVC_DATA, "OBD DATA");
                        Toast.makeText(Bluetooth.this,getResources().getStringArray(R.array.elmcomm_states)[state.ordinal()],Toast.LENGTH_SHORT).show();
                        String ss = state.toString();
                        if( ss.equals("Connected"))
                        {
                            sendFaultCode.start();
                            return;
                        }


                        /* Show ELM status only in ONLINE mode */
                       /* if (getMode() !=MODE .DEMO)
                        {
                            setStatus(getResources().getStringArray(R.array.elmcomm_states)[state.ordinal()]);
                        }
                        // if last selection shall be restored ...
                        if(istRestoreWanted(PRESELECT.LAST_SERVICE))
                        {
                            if(state == ElmProt.STAT.ECU_DETECTED)
                            {
                                setObdService(prefs.getInt(PRESELECT.LAST_SERVICE.toString(),0), null);
                            }
                        }*/
                        break;

                    // handle change in number of fault codes
                    case MESSAGE_OBD_NUMCODES:
                        evt = (PropertyChangeEvent) msg.obj;
                        // HashMap<Object,Object> abc = new HashMap();
                        abc = ObdProt.tCodes;

                        dialogS.cancel();

                        Gson gson = new Gson();
                        String data = gson.toJson(ecuData);
                        SharedPreference.storeEcuData(Bluetooth.this, data);

                       /* Intent i = new Intent(Bluetooth.this,FaultRes.class);
                        i.putExtra("hashmap",abc);
                        //i.putExtra("ObdProtPidPvs;",ecuData);
                        startActivity(i);*/

                        /** process variable list which holds all parameters */
                        //PvList CanPvs = new PvList();

                        /*Object colVal = currPv.get(EcuDataPv.FID_VALUE);
                        EcuDataPv ecuDataPv = new EcuDataPv();*/
                      //CanPvs.get();




                        /*Intent i = new Intent(Bluetooth.this,FaultRes.class);
                        i.putExtra("hashmap",abc);
                        i.putExtra("ObdProt.PidPvs;",ObdProt.PidPvs);
                        startActivity(i);*/
                        //setNumCodes((Integer) evt.getNewValue());
                        break;

                    // handle ECU detection event
                    case MESSAGE_OBD_ECUS:
                        evt = (PropertyChangeEvent) msg.obj;
                        selectEcu((Set<Integer>) evt.getNewValue());
                        break;

                    // handle negative result code from OBD protocol
                    case MESSAGE_OBD_NRC:
                        // reset OBD mode to prevent infinite error loop
                        //setObdService(ObdProt.OBD_SVC_NONE, getText(R.string.obd_error));
                        // show error dialog ...
                        evt = (PropertyChangeEvent) msg.obj;
                        String nrcMessage = (String)evt.getNewValue();
                        dlgBuilder
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("OBD Error")
                                .setMessage(nrcMessage)
                                .setPositiveButton(null,null)
                                .show();
                        break;

                    // set toolbar visibility
                    case MESSAGE_TOOLBAR_VISIBLE:
                        Boolean visible = (Boolean)msg.obj;
                        // log action
                        log.fine(String.format("ActionBar: %s", visible ? "show" : "hide"));
                        // set action bar visibility
                        ActionBar ab = getActionBar();
                        if(ab != null)
                        {
                            if(visible)
                            {
                                ab.show();
                            } else
                            {
                                ab.hide();
                            }
                        }
                        break;
                }
            }
            catch(Exception ex)
            {
                log.log(Level.SEVERE, "Error in mHandler", ex);
            }
        }
    };


    /**
     * Handle bluetooth connection established ...
     */
    private void onConnect()
    {

        //mode = Bluetooth.MODE.ONLINE;

        // send RESET to Elm adapter
        CommService.elm.reset();
    }

    /**
     * Handle bluetooth connection lost ...
     */
    private void onDisconnect()
    {
        // handle further initialisations
        mode = Bluetooth.MODE.OFFLINE;
    }


    /**
     * set listeners for data structure changes
     */
    private void setDataListeners()
    {
        // add pv change listeners to trigger model updates
        ObdProt.PidPvs.addPvChangeListener(this,
                PvChangeEvent.PV_ADDED
                        | PvChangeEvent.PV_CLEARED
        );
        ObdProt.VidPvs.addPvChangeListener(this,
                PvChangeEvent.PV_ADDED
                        | PvChangeEvent.PV_CLEARED
        );
        ObdProt.tCodes.addPvChangeListener(this,
                PvChangeEvent.PV_ADDED
                        | PvChangeEvent.PV_CLEARED
        );
    }

    /**
     * set listeners for data structure changes
     */
    private void removeDataListeners()
    {
        // remove pv change listeners
        ObdProt.PidPvs.removePvChangeListener(this);
        ObdProt.VidPvs.removePvChangeListener(this);
        ObdProt.tCodes.removePvChangeListener(this);
    }




    /**
     * get current operating mode
     */
    private MODE getMode()
    {
        return mode;
    }

    /**
     * set new operating mode
     *
     * @param mode new mode
     */
    private void setMode(MODE mode)
    {
        // if this is a mode change, or file reload ...
        if (mode != this.mode || mode == Bluetooth.MODE.FILE)
        {
           // if (mode != MODE.DEMO) stopDemoService();

            switch (mode)
            {
                case OFFLINE:
                    // update menu item states
                    /*setMenuItemVisible(R.id.disconnect, false);
                    setMenuItemVisible(R.id.secure_connect_scan, true);
                    setMenuItemEnable(R.id.obd_services, false);*/
                    break;

                case ONLINE:
                   /* switch (CommService.medium)
                    {
                        case BLUETOOTH:
                            if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
                            {
                                Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show();
                                mode = MODE.OFFLINE;
                            }
                            else
                            {
                                // if pre-settings shall be used ...
                                //String address = prefs.getString(PRESELECT.LAST_DEV_ADDRESS.toString(), null);
                                if(istRestoreWanted(PRESELECT.LAST_DEV_ADDRESS)
                                        && address != null)
                                {
                                    // ... connect with previously connected device
                                    connectBtDevice(address, prefs.getBoolean("bt_secure_connection", false));
                                }
                                else
                                {
                                    // ... otherwise launch the BtDeviceListActivity to see devices and do scan
                                    Intent serverIntent = new Intent(this, BtDeviceListActivity.class);
                                    startActivityForResult(serverIntent,
                                            prefs.getBoolean("bt_secure_connection", false)
                                                    ? REQUEST_CONNECT_DEVICE_SECURE
                                                    : REQUEST_CONNECT_DEVICE_INSECURE );
                                }
                            }
                            break;

                        case USB:
                            Intent enableIntent = new Intent(this, UsbDeviceListActivity.class);
                            startActivityForResult(enableIntent, REQUEST_CONNECT_DEVICE_USB);
                            break;

                        case NETWORK:
                            connectNetworkDevice(prefs.getString(DEVICE_ADDRESS, null),
                                    getPrefsInt(DEVICE_PORT, 23));
                            break;
                    }*/
                    break;

                case DEMO:
                   // startDemoService();
                    break;

                case FILE:
                   /* setStatus(R.string.saved_data);
                    selectFileToLoad();*/
                    break;

            }
            // remember previous mode
            // set new mode
            this.mode = mode;
            //setStatus(mode.toString());
        }
    }







    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    @Override
    public void pvChanged(PvChangeEvent event) {

    }

    /**
     * Property change listener to ELM-Protocol
     *
     * @param evt the property change event to be handled
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        /* handle protocol status changes */
        if (ElmProt.PROP_STATUS.equals(evt.getPropertyName()))
        {
            // forward property change to the UI Activity
            Message msg = mHandler.obtainMessage(MESSAGE_OBD_STATE_CHANGED);
            msg.obj = evt;
            mHandler.sendMessage(msg);
        } else if (ElmProt.PROP_NUM_CODES.equals(evt.getPropertyName()))
        {
            // forward property change to the UI Activity
            Message msg = mHandler.obtainMessage(MESSAGE_OBD_NUMCODES);
            msg.obj = evt;
            mHandler.sendMessage(msg);
        } else if (ElmProt.PROP_ECU_ADDRESS.equals(evt.getPropertyName()))
        {
            // forward property change to the UI Activity
            Message msg = mHandler.obtainMessage(MESSAGE_OBD_ECUS);
            msg.obj = evt;
            mHandler.sendMessage(msg);
        } else if (ObdProt.PROP_NRC.equals(evt.getPropertyName()))
        {
            // forward property change to the UI Activity
            Message msg = mHandler.obtainMessage(MESSAGE_OBD_NRC);
            msg.obj = evt;
            mHandler.sendMessage(msg);
        }
    }


    /**
     * Prompt for selection of a single ECU from list of available ECUs
     * @param ecuAdresses List of available ECUs
     */
    private void selectEcu(final Set<Integer> ecuAdresses)
    {
        // if more than one ECUs available ...
        if(ecuAdresses.size() > 1)
        {
          /*  //int preferredAddress = prefs.getInt(PRESELECT.LAST_ECU_ADDRESS.toString(), 0);
            // check if last preferred address matches any of the reported addresses
            if(*//*istRestoreWanted(PRESELECT.LAST_ECU_ADDRESS)
                    && *//*ecuAdresses.contains(preferredAddress))
            {
                // set address
                CommService.elm.setEcuAddress(preferredAddress);
            }
            else*/

                // NO match with preference -> allow selection

                // .. allow selection of single ECU address ...
                final CharSequence[] entries = new CharSequence[ecuAdresses.size()];
                // create list of entries
                int i = 0;
                for (Integer addr : ecuAdresses)
                {
                    entries[i++] = String.format("0x%X", addr);
                }
                // show dialog ...
                dlgBuilder
                        .setTitle("select_ecu_addr")
                        .setItems(entries, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                int address = Integer.parseInt(entries[which].toString().substring(2), 16);
                                // set address
                                CommService.elm.setEcuAddress(address);
                                // set this as preference (preference change will trigger ELM command)
                               // prefs.edit().putInt(PRESELECT.LAST_ECU_ADDRESS.toString(), address).apply();

                               // sendFaultCode.start();

                            }
                        })
                        .show();

        }
    }

    CountDownTimer sendFaultCode = new CountDownTimer(3000, 1000) {

        public void onTick(long millisUntilFinished) {
            dialogS.show();
        }

        public void onFinish() {

            //setObdService(ObdProt.OBD_SVC_PENDINGCODES, "OBD_SVC_READ_CODES");
            setObdService(ObdProt.OBD_SVC_DATA, "OBD DATA");


        }
    };

    CountDownTimer sendData = new CountDownTimer(1000, 1000) {

        public void onTick(long millisUntilFinished) {

        }

        public void onFinish() {

            //setObdService(ObdProt.OBD_SVC_NONE, null);
            setObdService(ObdProt.OBD_SVC_PENDINGCODES, "OBD_SVC_READ_CODES");



        }
    };


    /**
     * Activate desired OBD service
     *
     * @param newObdService OBD service ID to be activated
     */
    private void setObdService(int newObdService, CharSequence menuTitle)
    {
        // remember this as current OBD service
        obdService = newObdService;
        CommService.elm.setService(newObdService, (getMode() != Bluetooth.MODE.FILE));


        switch (newObdService)
        {
            case ObdProt.OBD_SVC_DATA:
                currDataAdapter = mPidAdapter;
                // no break here
            case ObdProt.OBD_SVC_FREEZEFRAME:
                currDataAdapter = mPidAdapter;
               //  ecuData = ObdProt.PidPvs;
               //  sendData.start();
                break;

            case ObdProt.OBD_SVC_PENDINGCODES:
            case ObdProt.OBD_SVC_PERMACODES:
            case ObdProt.OBD_SVC_READ_CODES:
                currDataAdapter = mDfcAdapter;
                break;

            case ObdProt.OBD_SVC_NONE:

                // intentionally no
                // break to initialize adapter
            case ObdProt.OBD_SVC_VEH_INFO:
                currDataAdapter = mVidAdapter;
                break;
        }


    }



}
