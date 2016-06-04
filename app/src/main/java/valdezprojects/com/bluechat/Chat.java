package valdezprojects.com.bluechat;

import android.graphics.Bitmap;

/**
 * Created by javier on 15/05/16.
 */
public class Chat {
    String clientName;
    String clientText;
    int gifId;
    int type;

    public Chat(String clientName,String clientText,int gifId,int type){
        super();
        this.clientName=clientName;
        this.clientText=clientText;
        this.gifId=gifId;
        this.type=type;
    }
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
