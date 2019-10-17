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
        }if("printText".equals(action)){
            this.printText(args.getString(0), true, callbackContext);
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

    private OnCommEventListener createCommListener(CallbackContext callbackContext){
        //Create CommEventListener
        OnCommEventListener mCommEventListener = new OnCommEventListener() {
          @Override
          public void onCommState(int cmdFlag, int state, byte[] resp, int respLen) {
              // TODO Auto-generated method stub
              switch(cmdFlag){
              case PosApi.POS_INIT:
                  if(state==PosApi.COMM_STATUS_SUCCESS){
                       callbackContext.success("Controller enabled");
                  }else {
                      callbackContext.error("Controller can not enabled");
                  }
                  break;
              }
          }
      };
      return mCommEventListener;
    }

    private OnPrintListener createPrintListener(){

      Context context = cordova.getActivity().getApplicationContext();

      OnPrintListener printListener = new OnPrintListener() {

			@Override
			public void onGetState(int state) {
				switch(state){
					case 0:

						//有纸
						Toast.makeText(context, "Impresora Ok", Toast.LENGTH_SHORT).show();

						break;

					case 1:

						//缺纸
						Toast.makeText(context, "Sin papel", Toast.LENGTH_SHORT).show();

						break;

				}
			}

			@Override
			public void onPrinterSetting(int state) {
				switch(state){
					case 0:
						Toast.makeText(context, "Papel continuo", Toast.LENGTH_SHORT).show();
						break;
					case 1:
						Toast.makeText(context, "Sin papel", Toast.LENGTH_SHORT).show();
						break;
					case 2:
						Toast.makeText(context, "Marca negra detectada", Toast.LENGTH_SHORT).show();
						break;
				}
			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				//mPosApi.gpioControl((byte)0x23,2,0);
				Toast.makeText(context, "Impresión completada", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailed(int state) {
				// TODO Auto-generated method stub
				//mPosApi.gpioControl((byte)0x23,2,0);
				switch(state){

					case PosApi.ERR_POS_PRINT_NO_PAPER:
						//打印缺纸
						//showTip(getString(R.string.print_no_paper));
						break;
					case PosApi.ERR_POS_PRINT_FAILED:
						//打印失败
						//showTip(getString(R.string.print_failed));
						break;
					case PosApi.ERR_POS_PRINT_VOLTAGE_LOW:
						//电压过低
						//showTip(getString(R.string.print_voltate_low));
						break;
					case PosApi.ERR_POS_PRINT_VOLTAGE_HIGH:
						//电压过高
						//showTip(getString(R.string.print_voltate_high));
						break;
				}
			}
		};
        return printListener;
    }

    private void preparePrinterQueue(){
        mPrintQueue = new PrintQueue(this,mPosApi);
		mPrintQueue.init();
        mPrintQueue.setOnPrintListener(createPrintListener());
    }

    private void turnOnPrinter(CallbackContext callbackContext) {

      try {
          FileWriter localFileWriterOn = new FileWriter(new File("/proc/gpiocontrol/set_sam"));
          localFileWriterOn.write("1");
          localFileWriterOn.close();
      } catch (Exception e) {
          e.printStackTrace();
      }

      Context context = cordova.getActivity().getApplicationContext();
      mPosApi = PosApi.getInstance(context);
      mPrintQueue = new PrintQueue(context, mPosApi);
      mPrintQueue.init();

      cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            mPosApi.setOnComEventListener(createCommListener(callbackContext));
            mPosApi.initDeviceEx("/dev/ttyMT2");
            preparePrinterQueue();
          }
      });
	}

    private void turnOffPrinter(CallbackContext callbackContext) {

      if(mPosApi!=null){
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

    private void printText(JSONObject obj, Boolean standalone, CallbackContext callbackContext){
        /*try{
          byte[] btUTF8 = new byte[0];
          String text = obj.getString("text");
          Integer align = obj.getInt ("align");
          if(mIzkcService.checkPrinterAvailable() == true){
            mIzkcService.sendRAWData("print", new byte[]{0x1C, 0x43, (byte) 0xFF});
            printer_available = "Text sent to printer.";
            mIzkcService.setAlignment(align);
            btUTF8 = text.getBytes("UTF-8");
            mIzkcService.sendRAWData("print", btUTF8);
          }else{
              printer_available = "Printer not initialized or unavailable.";
          }
          if(standalone){
            callbackContext.success(printer_available);
          }
        }catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            callbackContext.success(sw.toString());
        }*/
    }
}
