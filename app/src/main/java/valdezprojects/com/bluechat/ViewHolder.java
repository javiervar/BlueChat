package valdezprojects.com.bluechat;

import android.widget.ImageView;
import android.widget.TextView;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by javier on 17/05/16.
 */
public class ViewHolder {
    TextView clientName;
    TextView clientText;
    GifImageView photo;

    public ViewHolder(TextView clientName, TextView clientText,GifImageView photo) {
        this.clientName = clientName;
        this.clientText = clientText;
        this.photo=photo;
    }

    public TextView getTextName() {
        return clientName;
    }

    public void setTextName(TextView text) {
        this.clientName = text;
    }

    public TextView getText() {
        return clientText;
    }

    public void setText(TextView text) {
        this.clientText = text;
    }

    public ImageView getPhoto() {
        return photo;
    }

    public void setPhoto(GifImageView photo) {
        this.photo = photo;
    }
}
