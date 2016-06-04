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
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import pl.droidsonroids.gif.GifImageButton;
import pl.droidsonroids.gif.GifImageView;

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

    ImageButton btnSend;
    GifImageView btnPhoto;
    EditText text;
    ListView messages,lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        //Always make sure that Bluetooth server is discoverable during listening...
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(discoverableIntent, DISCOVERABLE_REQUEST_CODE);
        //acceptThread.start();

        btnSend = (ImageButton) findViewById(R.id.send);
        btnPhoto = (GifImageView) findViewById(R.id.photo);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //registerReceiver(mPairReceiver, intent);
        android.util.Log.e("TrackingFlow", "Creating thread to start listening...");
        new Thread(reader).start();


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
                receiverImg(parts[1], Integer.parseInt(parts[2]));

                 break;

        }

    }
    public void receiver(String name,String txt){

        data_chat.add(new Chat(name , txt,0,2));

        chat_adapter = new ChatAdapter(BluetoothServer.this, R.layout.listview_itemreceiver, data_chat);
        messages.setAdapter(chat_adapter);
        chat_adapter.notifyDataSetChanged();

    }
    public void receiverImg(String name,int id){

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        data_chat.add(new Chat(name +" "+ df.format(c.getTime()), null,id, 4));
        chat_adapter = new ChatAdapter(BluetoothServer.this, R.layout.listview_itemsend, data_chat);
        messages.setAdapter(chat_adapter);
        chat_adapter.notifyDataSetChanged();

    }

    private View.OnClickListener gifListener = new View.OnClickListener() {

        public void onClick(View v) {
            int id=0;
            switch (v.getId()){
                case R.id.btnn1:
                    id=R.drawable.angry;
                    break;
                case R.id.btnn2:
                    id=R.drawable.angry2;
                    break;
                case R.id.btnn3:
                    id=R.drawable.angry3;
                    break;
                case R.id.btnn4:
                    id=R.drawable.cat3;
                    break;
                case R.id.btnn5:
                    id=R.drawable.cat_dos;
                    break;
                case R.id.btnn6:
                    id=R.drawable.cat_uno;
                    break;
                case R.id.btnn7:
                    id=R.drawable.sad_uno;
                    break;
                case R.id.btnn8:
                    id=R.drawable.sad_dos;
                    break;
                case R.id.btnn9:
                    id=R.drawable.sad3;
                    break;
                case R.id.btnn10:
                    id=R.drawable.dos;
                    break;
                case R.id.btnn11:
                    id=R.drawable.uno;
                    break;
                case R.id.btnn12:
                    id=R.drawable.dog_tres;
                    break;
                case R.id.btnn13:
                    id=R.drawable.nose;
                    break;
                case R.id.btnn14:
                    id=R.drawable.ok;
                    break;
                case R.id.btnn15:
                    id=R.drawable.sd;
                    break;
                case R.id.btnn16:
                    id=R.drawable.troll;
                    break;
                case R.id.btnn17:
                    id=R.drawable.tres;
                    break;
                case R.id.btnn18:
                    id=R.drawable.r1;
                    break;
                case R.id.btnn19:
                    id=R.drawable.r2;
                    break;
                case R.id.btnn20:
                    id=R.drawable.r3;
                    break;
                case R.id.btnn21:
                    id=R.drawable.r4;
                    break;
                case R.id.btnn22:
                    id=R.drawable.r5;
                    break;
                case R.id.btnn23:
                    id=R.drawable.r6;
                    break;

            }

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            data_chat.add(new Chat(data.name +" "+ df.format(c.getTime()), null,id, 3));
            chat_adapter = new ChatAdapter(BluetoothServer.this, R.layout.listview_itemsend, data_chat);
            messages.setAdapter(chat_adapter);
            chat_adapter.notifyDataSetChanged();
            settingsDialog.dismiss();
            sendText(""+id,"img",df.format(c.getTime()));

        }
    };

    private View.OnClickListener btnListener = new View.OnClickListener() {

        public void onClick(View v) {
            if (connected) {
                switch (v.getId()) {
                    case R.id.send:
                        System.out.println("sup");

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time=df.format(c.getTime());

                        data_chat.add(new Chat(data.name+" "+time, text.getText().toString(),0,1));

                        chat_adapter = new ChatAdapter(BluetoothServer.this, R.layout.listview_itemsend, data_chat);
                        messages.setAdapter(chat_adapter);
                        chat_adapter.notifyDataSetChanged();
                        sendText(text.getText().toString(), "msg",time);
                        text.setText("");


                        break;
                    case R.id.photo:
                       dialog(v.getContext());
                        break;

                }
            }

        }

    };



    public void dialog(Context context) {


        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_emoji, null);
        GifImageButton gif1=(GifImageButton)view.findViewById(R.id.btnn1);
        GifImageButton gif2=(GifImageButton)view.findViewById(R.id.btnn2);
        GifImageButton gif3=(GifImageButton)view.findViewById(R.id.btnn3);
        GifImageButton gif4=(GifImageButton)view.findViewById(R.id.btnn4);
        GifImageButton gif5=(GifImageButton)view.findViewById(R.id.btnn5);
        GifImageButton gif6=(GifImageButton)view.findViewById(R.id.btnn6);
        GifImageButton gif7=(GifImageButton)view.findViewById(R.id.btnn7);
        GifImageButton gif8=(GifImageButton)view.findViewById(R.id.btnn8);
        GifImageButton gif9=(GifImageButton)view.findViewById(R.id.btnn9);
        GifImageButton gif10=(GifImageButton)view.findViewById(R.id.btnn10);
        GifImageButton gif11=(GifImageButton)view.findViewById(R.id.btnn11);
        GifImageButton gif12=(GifImageButton)view.findViewById(R.id.btnn12);
        GifImageButton gif13=(GifImageButton)view.findViewById(R.id.btnn13);
        GifImageButton gif14=(GifImageButton)view.findViewById(R.id.btnn14);
        GifImageButton gif15=(GifImageButton)view.findViewById(R.id.btnn15);
        GifImageButton gif16=(GifImageButton)view.findViewById(R.id.btnn16);
        GifImageButton gif17=(GifImageButton)view.findViewById(R.id.btnn17);
        GifImageButton gif18=(GifImageButton)view.findViewById(R.id.btnn18);
        GifImageButton gif19=(GifImageButton)view.findViewById(R.id.btnn19);
        GifImageButton gif20=(GifImageButton)view.findViewById(R.id.btnn20);
        GifImageButton gif21=(GifImageButton)view.findViewById(R.id.btnn21);
        GifImageButton gif22=(GifImageButton)view.findViewById(R.id.btnn22);
        GifImageButton gif23=(GifImageButton)view.findViewById(R.id.btnn23);

        gif1.setOnClickListener(gifListener);
        gif2.setOnClickListener(gifListener);
        gif3.setOnClickListener(gifListener);
        gif4.setOnClickListener(gifListener);
        gif5.setOnClickListener(gifListener);
        gif6.setOnClickListener(gifListener);
        gif7.setOnClickListener(gifListener);
        gif8.setOnClickListener(gifListener);
        gif9.setOnClickListener(gifListener);
        gif10.setOnClickListener(gifListener);
        gif11.setOnClickListener(gifListener);
        gif12.setOnClickListener(gifListener);
        gif13.setOnClickListener(gifListener);
        gif14.setOnClickListener(gifListener);
        gif15.setOnClickListener(gifListener);
        gif16.setOnClickListener(gifListener);
        gif17.setOnClickListener(gifListener);
        gif18.setOnClickListener(gifListener);
        gif19.setOnClickListener(gifListener);
        gif20.setOnClickListener(gifListener);
        gif21.setOnClickListener(gifListener);
        gif22.setOnClickListener(gifListener);
        gif23.setOnClickListener(gifListener);


        settingsDialog = new Dialog(context);
        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        settingsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        settingsDialog.setContentView(view);
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

}

