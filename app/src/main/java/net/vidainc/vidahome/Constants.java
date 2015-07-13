/**
 * Created by Aaron on 13/07/2015.
 */

package net.vidainc.vidahome;
public interface Constants {
    String KEY_STATE = "keyState";
    String KEY_REG_ID = "keyRegId";
    String KEY_MSG_ID = "keyMsgId";
    String KEY_ACCOUNT = "keyAccount";
    String KEY_MESSAGE_TXT = "keyMessageTxt";
    String KEY_EVENT_TYPE = "keyEventbusType";

    String ACTION = "action";
    // very simply notification handling :-)
    int NOTIFICATION_NR = 10;

    long GCM_DEFAULT_TTL = 2 * 24 * 60 * 60 * 1000; // two days


    String SERVER_PACKAGE = "net.vidainc.home.server";
    // actions for server interaction
    String ACTION_REGISTER = SERVER_PACKAGE + ".REGISTER";
    String ACTION_UNREGISTER = SERVER_PACKAGE + ".UNREGISTER";
    String ACTION_BEACON_DATA = SERVER_PACKAGE + ".BEACON_DATA";

    enum EventbusMessageType {
        REGISTRATION_FAILED, REGISTRATION_SUCCEEDED, UNREGISTRATION_SUCCEEDED, UNREGISTRATION_FAILED
    }

    enum State {
        REGISTERED, UNREGISTERED
    }
}
