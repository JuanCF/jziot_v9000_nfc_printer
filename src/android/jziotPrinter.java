package cordova.plugin.jziot;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.posapi.PosApi;
import android.zyapi.PrintQueue;
import android.zyapi.PrintQueue.OnPrintListener;

/**
 * This class echoes a string called from JavaScript.
 */
public class jziotPrinter extends CordovaPlugin {

    private PosApi mPosApi;

    private PrintQueue mPrintQueue = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        } else if("turnOnPrinter".equals(action)){
            turnOnPrinter(callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void turnOnPrinter(CallbackContext callbackContext) {
      int last_module_flag = module_flag;
      mPosApi = getInstance().getPosApi();
      Context context = cordova.getActivity().getApplicationContext();
      //callbackContext.error("AIDL Service not connected");
      cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            callbackContext.success("Hola");
          }
      });
	}
}
