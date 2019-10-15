package cordova.plugin.jziot;

import java.io.File;
import java.io.FileWriter;

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
        } else if(action.equals("turnOnPrinter")){
            this.turnOnPrinter(callbackContext);
            return true;
        } else if(action.equals("turnOffPrinter")){
            this.turnOffPrinter(callbackContext);
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

      try {
          FileWriter localFileWriterOn = new FileWriter(new File("/proc/gpiocontrol/set_sam"));
          localFileWriterOn.write("1");
          localFileWriterOn.close();
      } catch (Exception e) {
          callbackContext.error("Controller can not enabled");
          e.printStackTrace();
      }

      mPosApi = PosApi.getInstance(context);
      mPosApi.setOnComEventListener(mCommEventListener);
      mPosApi.initDeviceEx("/dev/ttyMT2");
      cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            callbackContext.success("Controller enabled");
          }
      });
	}

    private void turnOffPrinter(CallbackContext callbackContext) {

      if(mApi!=null){
		  mPosApi.closeDev();
      }

      try {
          FileWriter localFileWriterOn = new FileWriter(new File("/proc/gpiocontrol/set_sam"));
          localFileWriterOn.write("0");
          localFileWriterOn.close();
          callbackContext.success("Controller disabled");
      } catch (Exception e) {
          callbackContext.error("Controller can not be disabled");
          e.printStackTrace();
      }
	}
}
