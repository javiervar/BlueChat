package valdezprojects.com.bluechat;

import android.graphics.Bitmap;

/**
 * Created by javier on 15/05/16.
 */
public class Chat {
    String clientName;
    String clientText;
    Bitmap b64;
    int type;

    public Chat(String clientName,String clientText,Bitmap b64,int type){
        super();
        this.clientName=clientName;
        this.clientText=clientText;
        this.b64=b64;
        this.type=type;
    }
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
