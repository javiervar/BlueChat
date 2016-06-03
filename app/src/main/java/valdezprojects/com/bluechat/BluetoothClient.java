package valdezprojects.com.bluechat;

/**
 * Created by javier on 7/05/16.
 */

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import pl.droidsonroids.gif.GifImageView;

public class BluetoothClient extends Activity {

    private boolean CONTINUE_READ_WRITE = true;
    private static final int REQUEST_ENABLE_BT = 3;
    private int PICK_IMAGE_REQUEST = 1;
    BluetoothDevice bdDevice;
    ArrayList<BluetoothDevice> arrayListBluetoothDevices = null;

    private BluetoothSocket socket;
    private OutputStreamWriter os;
    private OutputStream ots;
    private InputStream is;
    private BluetoothDevice remoteDevice;
    Bitmap bitmap;


    private ArrayList<String> mDeviceList;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private ArrayList<Chat> data_chat = new ArrayList<Chat>();

    ChatAdapter chat_adapter;
    Data data;

    Boolean connected = false;

    ImageButton  btnSend;
    GifImageView btnPhoto;
    EditText text;
    ListView messages, lista;
    Dialog settingsDialog;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if (!adapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            adapter.startDiscovery();
            bluetoothSearch();

        } else if (adapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            //;
            if (adapter != null && adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }

            adapter.startDiscovery();
            bluetoothSearch();

        }

        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        btnSend = (ImageButton) findViewById(R.id.send);
        btnPhoto = (GifImageView) findViewById(R.id.photo);



        text = (EditText) findViewById(R.id.text);



        messages = (ListView) findViewById(R.id.menssage_list);
        messages.setDivider(null);
        handler = new Handler();
        data = new Data();


        //mp = MediaPlayer.create(getApplicationContext(), R.raw.lp_effect);



        btnSend.setOnClickListener(btnListener);
        btnPhoto.setOnClickListener(btnListener);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(discoveryResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (socket != null) {
            try {
                is.close();
                os.close();
                socket.close();
                CONTINUE_READ_WRITE = false;
            } catch (Exception e) {
            }
        }
    }

    private BroadcastReceiver discoveryResult = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            android.util.Log.e("TrackingFlow", "WWWTTTFFF");
            unregisterReceiver(this);
            remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // new Thread(reader).start();
        }
    };


    private Runnable readerConnect = new Runnable() {
        UUID uuid = UUID.fromString("4e5d48e0-75df-11e3-981f-0800200c9a66");

        @Override
        public void run() {
            try {
                BluetoothSocket clientSocket =
                        bdDevice.createRfcommSocketToServiceRecord(uuid);
                clientSocket.connect();
                os = new OutputStreamWriter(clientSocket.getOutputStream());
                is = clientSocket.getInputStream();
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(BluetoothClient.this, "CONNECTED WITH " + bdDevice.getName(), Toast.LENGTH_LONG).show();
                        connected = true;
                    }
                });
                int bufferSize = 1024;
                int bytesRead = -1;
                byte[] buffer = new byte[bufferSize];
                while (CONTINUE_READ_WRITE) {
                    android.util.Log.e("TrackingFlow", "WWWTTTFFF3wwwww4243");
                    final StringBuilder sb = new StringBuilder();
                    bytesRead = is.read(buffer);
                    if (bytesRead != -1) {
                        String result = "";
                        while ((bytesRead == bufferSize) && (buffer[bufferSize - 1] != 0)) {
                            result = result + new String(buffer, 0, bytesRead - 1);
                            bytesRead = is.read(buffer);
                        }
                        result = result + new String(buffer, 0, bytesRead - 1);
                        sb.append(result);

                    }



                    //Show message on UIThread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            parsear(sb.toString());
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            System.out.println(uri);

            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                //bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                data_chat.add(new Chat("fulanito " + df.format(c.getTime()), null, bitmap, 3));

                chat_adapter = new ChatAdapter(BluetoothClient.this, R.layout.listview_itemsend, data_chat);
                messages.setAdapter(chat_adapter);
                chat_adapter.notifyDataSetChanged();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                sendImg(byteArray);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println(device.getName());
                mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                Log.i("BT", device.getName() + "\n" + device.getAddress());
                lista.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mDeviceList));
                mNewDevicesArrayAdapter.notifyDataSetChanged();
                arrayListBluetoothDevices.add(device);

            }
        }
    };


    public void bluetoothSearch() {

        LayoutInflater inflater = LayoutInflater.from(BluetoothClient.this);
        View view = inflater.inflate(R.layout.dialog_btsearch, null);
        mDeviceList = new ArrayList<String>();
        lista = (ListView) view.findViewById(R.id.bluetooth_list);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDeviceList);
        arrayListBluetoothDevices = new ArrayList<BluetoothDevice>();
        lista.setAdapter(mNewDevicesArrayAdapter);
        ListItemClicked listItemClicked = new ListItemClicked();
        lista.setOnItemClickListener(listItemClicked);


        settingsDialog = new Dialog(BluetoothClient.this);
        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        settingsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        settingsDialog.setContentView(view);
        settingsDialog.show();
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));


    }

    public void parsear(String text) {
        System.out.println(text);

        String[] parts = text.split("&&");
        switch (parts[0]) {

            case "msg":
                receiver(parts[1], parts[2] );
                break;
            case "img":
                break;
        }


    }


    public void receiver(String name, String txt ) {

        data_chat.add(new Chat(name, txt, null, 2));
        chat_adapter = new ChatAdapter(BluetoothClient.this, R.layout.listview_itemreceiver, data_chat);
        messages.setAdapter(chat_adapter);
        chat_adapter.notifyDataSetChanged();



    }

    private View.OnClickListener btnListener = new View.OnClickListener() {

        public void onClick(View v) {


            switch (v.getId()) {

                case R.id.send:
                    System.out.println("sup");

                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time=df.format(c.getTime());
                    data_chat.add(new Chat(data.name + " " + time, text.getText().toString(), null, 1));
                    chat_adapter = new ChatAdapter(BluetoothClient.this, R.layout.listview_itemsend, data_chat);
                    messages.setAdapter(chat_adapter);
                    chat_adapter.notifyDataSetChanged();
                    sendText(text.getText().toString(), "msg",time);

                    text.setText("");

                    break;
                case R.id.photo:
                    dialog(BluetoothClient.this);
                    break;

            }


        }

    };



    public void sendText(String txt,String opc,String time) {
        String send = opc+"&&" + data.name +" "+time+ "&&" + txt+" " ;
        try {
            os.write(send);
            os.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void sendImg(byte [] imgB64) {
        // byte[] decodedString = Base64.decode(imgB64, Base64.DEFAULT);


        try {
             ots.write(imgB64);
             ots.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void dialog(Context context) {


        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_emoji, null);


        settingsDialog = new Dialog(context);
        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        settingsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        settingsDialog.setContentView(view);
        settingsDialog.show();


    }

    class ListItemClicked implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            settingsDialog.dismiss();
            bdDevice = arrayListBluetoothDevices.get(position);
            //bdClass = arrayListBluetoothDevices.get(position);
            Log.i("Log", "The dvice : " + bdDevice.toString());

            //callThread();
            //connect(bdDevice);

            new Thread(readerConnect).start();


        }
    }

}

