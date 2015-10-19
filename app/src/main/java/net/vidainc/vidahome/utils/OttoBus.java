package net.vidainc.vidahome.utils;

import com.squareup.otto.Bus;

/**
 * Created by Staple on 10/18/2015.
 */
public class OttoBus {
    private static final Bus INSTANCE = new Bus();

    private OttoBus(){
    }

    public static Bus getInstance(){
        return INSTANCE;
    }
}
