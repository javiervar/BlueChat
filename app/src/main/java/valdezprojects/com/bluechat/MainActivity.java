package valdezprojects.com.bluechat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends AppCompatActivity {
    TextView txtvName;
    EditText txtName;
    Button btnAccept,btnServer,btnClient;
    Dialog dialog;
    Intent intent;
    Data data;
    GifImageView img;
    private BlurMaskFilter.Blur blur;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        new Handler().post(new Runnable() {
            @Override
            public void run() {
                dialogName(MainActivity.this);
            }
        });

        txtvName=(TextView)findViewById(R.id.name);
        btnClient=(Button)findViewById(R.id.btnClient);
        btnServer=(Button)findViewById(R.id.btnServer);
        img=(GifImageView)findViewById(R.id.photo);





        btnServer.setOnClickListener(btnListener);
        btnClient.setOnClickListener(btnListener);
        data=new Data();

    }

    public void dialogName(Context context) {


        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_name, null);
        btnAccept=(Button)view.findViewById(R.id.btnAccept);
        txtName=(EditText)view.findViewById(R.id.txtName);

        btnAccept.setOnClickListener(btnListener);

        dialog = new Dialog(context);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        dialog.setContentView(view);
        dialog.show();


    }
    private View.OnClickListener btnListener = new View.OnClickListener() {

        public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.btnAccept:
                        txtvName.setText("Hi "+txtName.getText().toString());
                        Random r = new Random();
                        int random = r.nextInt(10 - 1) + 1;
                        int color=0;
                        switch (random){
                            case 1:
                                color=Color.CYAN;
                                break;
                            case 2:
                                color=Color.BLUE;
                                break;
                            case 3:
                                color=Color.DKGRAY;
                                break;
                            case 4:
                                color=Color.parseColor("0CE40C");
                                break;
                            case 5:
                                color=Color.MAGENTA;
                                break;
                            case 6:
                                color=Color.RED;
                                break;
                            case 7:
                                color=Color.YELLOW;
                                break;
                            case 8:
                                color=Color.parseColor("#FF00FC");
                                break;
                            case 9:
                                color=Color.parseColor("#333333");
                                break;
                            case 10:
                                color=Color.parseColor("#FFB700");
                                break;


                        }


                        data.name=txtName.getText().toString();
                        data.color=color;
                        dialog.dismiss();
                        break;
                    case R.id.btnClient:
                        intent = new Intent(MainActivity.this, BluetoothClient.class);
                        startActivity(intent);
                         break;
                    case R.id.btnServer:
                        intent = new Intent(MainActivity.this, BluetoothServer.class);
                        startActivity(intent);
                         break;
                }
            }



    };


}
