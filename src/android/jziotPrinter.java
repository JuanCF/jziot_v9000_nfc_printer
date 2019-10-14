package cordova.plugin.jziot;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;
import android.posapi.PosApi;
import android.posapi.PosApi.OnCommEventListener;
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
            this.turnOnPrinter(callbackContext);
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

    OnCommEventListener mCommEventListener = new OnCommEventListener() {
		@Override
		public void onCommState(int cmdFlag, int state, byte[] resp, int respLen) {
            Context context = cordova.getActivity().getApplicationContext();
			// TODO Auto-generated method stub
			switch(cmdFlag){
			case PosApi.POS_INIT:
				if(state==PosApi.COMM_STATUS_SUCCESS){
					Toast.makeText(context, "Inicialización exitosa", Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(context, "Inicialización fallida", Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}
	};

    private void turnOnPrinter(CallbackContext callbackContext) {
      Context context = cordova.getActivity().getApplicationContext();
      mPosApi = PosApi.getInstance(context);
      mPosApi.setOnComEventListener(mCommEventListener);
      mPosApi.initDeviceEx("/dev/ttyMT2");
      //callbackContext.error("AIDL Service not connected");
      cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            callbackContext.success("Hola");
          }
      });
	}
}
