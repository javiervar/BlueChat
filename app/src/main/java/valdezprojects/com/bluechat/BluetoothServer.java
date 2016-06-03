package valdezprojects.com.bluechat;

/**
 * Created by javier on 6/05/16.
 */

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.ByteArrayInputStream;
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

public class BluetoothServer extends Activity {

    private static final int DISCOVERABLE_REQUEST_CODE = 0x1;
    private boolean CONTINUE_READ_WRITE = true;
    private int PICK_IMAGE_REQUEST = 1;
    Bitmap bitmap;
    private BluetoothSocket socket;
    private InputStream is;
    private OutputStreamWriter os;
    private OutputStream ots;


    Dialog settingsDialog;

    Handler handler;


    private ArrayList<Chat> data_chat=new ArrayList<Chat>();

    ChatAdapter chat_adapter;
    Data data;

    Boolean connected = false;

    ImageButton btnSend,btnPhoto;
    EditText text;
    ListView messages,lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        //Always make sure that Bluetooth server is discoverable during listening...
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(discoverableIntent, DISCOVERABLE_REQUEST_CODE);
        //acceptThread.start();

        btnSend = (ImageButton) findViewById(R.id.send);
        btnPhoto = (ImageButton) findViewById(R.id.photo);
        text=(EditText) findViewById(R.id.text);
        messages=(ListView) findViewById(R.id.menssage_list);
        messages.setDivider(null);
        handler = new Handler();
        data=new Data();


        //mp = MediaPlayer.create(getApplicationContext(), R.raw.lp_effect);


        btnSend.setOnClickListener(btnListener);
        btnPhoto.setOnClickListener(btnListener);



    }
    private static Bitmap codec(Bitmap src, Bitmap.CompressFormat format,
                                int quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        src.compress(format, quality, os);

        byte[] array = os.toByteArray();
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //registerReceiver(mPairReceiver, intent);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            System.out.println(uri);

            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                //bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                data_chat.add(new Chat("fulanito " + df.format(c.getTime()), null, bitmap, 3));

                chat_adapter = new ChatAdapter(BluetoothServer.this, R.layout.listview_itemsend, data_chat);
                messages.setAdapter(chat_adapter);
                chat_adapter.notifyDataSetChanged();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                sendImg(encoded);



            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            android.util.Log.e("TrackingFlow", "Creating thread to start listening...");
            new Thread(reader).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            try {
                is.close();
                os.close();
                socket.close();
            } catch (Exception e) {
            }
            CONTINUE_READ_WRITE = false;
        }
    }




    private Runnable reader = new Runnable() {
        public void run() {
            System.out.println("sudask");
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            UUID uuid = UUID.fromString("4e5d48e0-75df-11e3-981f-0800200c9a66");
            try {
                BluetoothServerSocket serverSocket = adapter.listenUsingRfcommWithServiceRecord("BLTServer", uuid);
                android.util.Log.e("TrackingFlow", "Listening...");
                socket = serverSocket.accept();
                android.util.Log.e("TrackingFlow", "Socket accepted...");

                is = socket.getInputStream();
                os = new OutputStreamWriter(socket.getOutputStream());
                //ByteArrayInputStream bInput = new ByteArrayInputStream(socket.gett tras);

                //Toast.makeText(BluetoothServer.this, "CONNECTED WITH "+adapter.getName(), Toast.LENGTH_LONG).show();
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(BluetoothServer.this, "CONNECTED WITH " + socket.getRemoteDevice().getName(), Toast.LENGTH_LONG).show();
                        connected = true;
                    }
                });
                int bufferSize = 1024;
                int bytesRead = -1;
                byte[] buffer = new byte[bufferSize];
                //Keep reading the messages while connection is open...
                while (CONTINUE_READ_WRITE) {
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


                    android.util.Log.e("TrackingFlow", "Read: " + sb.toString());

                    //Show message on UIThread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                             parsear(sb.toString());
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    public void showToastFromBackground(final String message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(BluetoothServer.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }




    public void parsear(String text) {

        String[] parts = text.split("&&");
        switch (parts[0]) {

            case "msg":

                receiver(parts[1], parts[2]);

                break;
            case "img":


                 break;

        }

    }
    public void receiver(String name,String txt){

        data_chat.add(new Chat(name , txt,null,2));

        chat_adapter = new ChatAdapter(BluetoothServer.this, R.layout.listview_itemreceiver, data_chat);
        messages.setAdapter(chat_adapter);
        chat_adapter.notifyDataSetChanged();

    }



    private View.OnClickListener btnListener = new View.OnClickListener() {

        public void onClick(View v) {
            if (connected) {
                switch (v.getId()) {
                    case R.id.send:
                        System.out.println("sup");

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time=df.format(c.getTime());

                        data_chat.add(new Chat(data.name+" "+time, text.getText().toString(),null,1));

                        chat_adapter = new ChatAdapter(BluetoothServer.this, R.layout.listview_itemsend, data_chat);
                        messages.setAdapter(chat_adapter);
                        chat_adapter.notifyDataSetChanged();
                        sendText(text.getText().toString(), "msg",time);
                        text.setText("");


                        break;
                    case R.id.photo:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                        break;

                }
            }

        }

    };



    public void dice(Context context) {


        LayoutInflater inflater = LayoutInflater.from(context);
       // View view = inflater.inflate(R.layout.dice_layout, null);



        settingsDialog = new Dialog(context);
        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        settingsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

       // dialog.setContentView(view);
         settingsDialog.show();


    }



    public void sendText( String txt,String opc,String time) {
        String send=opc+"&&"+data.name+" " +time+"&&"+txt+" ";
        try {
            os.write(send);
            os.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void sendImg(String imgB64) {
        byte[] decodedString = Base64.decode(imgB64, Base64.DEFAULT);


        try {
            //os.write();
            ots.write(decodedString);
            ots.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

