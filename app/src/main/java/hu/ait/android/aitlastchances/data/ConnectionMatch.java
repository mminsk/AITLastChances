package hu.ait.android.aitlastchances.data;

import android.net.Uri;

/**
 * Created by madisonminsk on 11/20/17.
 */

public class ConnectionMatch {

    private String name;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private String imageUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public ConnectionMatch() {

    }

    public ConnectionMatch(String name) {
        this.name = name;
    }

}
