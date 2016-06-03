package valdezprojects.com.bluechat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by javier on 15/05/16.
 */
public class ChatAdapter extends ArrayAdapter<Chat> {


    Context context;
    int resource;
    ArrayList<Chat> data = null;


    public ChatAdapter(Context context, int resource, ArrayList<Chat> data) {
        super(context, resource, data);
        this.context = context;
        this.resource = resource;
        this.data = data;

    }

    public int getViewTypeCount() {
        return 4;
    }

    public int getItemViewType(int position) {
        return data.get(position).getType();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        ViewHolder hold = null;
        Chat obj = data.get(position);

        int listViewItemType = getItemViewType(position);
        System.out.println("type: " + listViewItemType);
        System.out.println("name: " + obj.clientName);
        System.out.println("text: " + obj.clientText);

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            if (listViewItemType == 1) {
                System.out.println("UNO");
                row = inflater.inflate(R.layout.listview_itemsend, parent, false);
            } else if (listViewItemType == 2) {
                System.out.println("DOS");
                row = inflater.inflate(R.layout.listview_itemreceiver, parent, false);
            } else if (listViewItemType == 3) {
                System.out.println("tres");
                row = inflater.inflate(R.layout.listview_itemimage, parent, false);
            }

            hold = new ViewHolder((TextView) row.findViewById(R.id.clientName), (TextView) row.findViewById(R.id.textClient), (ImageView) row.findViewById(R.id.photoSend));


            row.setTag(hold);
        } else {
            hold = (ViewHolder) row.getTag();
        }

        if (listViewItemType == 1 || listViewItemType == 2) {
            hold.getText().setText(obj.clientText);
            hold.getTextName().setText(obj.clientName);
        } else {
            System.out.println("sup nigga");
            hold.getPhoto().setImageBitmap(obj.b64);
            hold.getTextName().setText(obj.clientName);

        }


        return row;
    }


}
