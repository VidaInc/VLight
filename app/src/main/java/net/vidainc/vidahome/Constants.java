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
    String KEY_BEACON_SERVICE_ROOM_NUMBER = "beacon_service_room_number";
    String KEY_BEACON_SERVICE_ROOM_CERTAINTY = "beacon_service_room_certainty";

    int BEACON_SERVICE_TRAIN_EVENT = 0;

    String BEACON_MAC_ONE = "B4:99:4C:89:70:53";
    String BEACON_MAC_TWO = "D0:FF:50:67:7C:4A";
    String BEACON_MAC_THREE = "B4:99:4C:89:72:9E";

    String ACTION = "action";
    // very simply notification handling :-)
    int NOTIFICATION_NR = 10;

    long GCM_DEFAULT_TTL = 2 * 24 * 60 * 60 * 1000; // two days

    boolean valid = false;
    int RESULT_SETTINGS = 1; // Tag for onActivityResult



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
