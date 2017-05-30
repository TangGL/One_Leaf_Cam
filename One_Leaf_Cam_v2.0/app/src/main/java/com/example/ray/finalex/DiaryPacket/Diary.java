package com.example.ray.finalex.DiaryPacket;

import org.lasque.tusdk.core.utils.Base64Coder;
import org.lasque.tusdk.modules.view.widget.sticker.StickerGroup;

/**
 * Created by 43cm on 2016/12/14.
 */

public class Diary {
    private String time;
    private String pic;
    private String title;
    private String detail;
    private String address;

    public String getTime() {
        return time;
    }

    public String getPic() {
        return pic;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public String getAddress() {
        return address;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
