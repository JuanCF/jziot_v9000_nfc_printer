package cordova.plugin.jziot;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

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

import cordova.plugin.jziot.util.BitmapTools;

//import android.util.Log;

/**
 * This class echoes a string called from JavaScript.
 */
public class jziotPrinter extends CordovaPlugin {

    private static final String TAG = "IntermecPR3";

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
        }if(action.equals("printBulkData")){
            this.printBulkData(args.getString(0), callbackContext);
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
        Context context = cordova.getActivity().getApplicationContext();
        mPrintQueue = new PrintQueue(context,mPosApi);
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

    private void addPrintTextWithSize(int size ,int concentration,  byte[] data){
		if(data == null) return ;
		//2倍字体大小
		byte[] _2x = new byte[]{0x1b,0x57,0x02};
		//1倍字体大小
		byte[] _1x = new byte[]{0x1b,0x57,0x01};
		byte[] mData = null;
		if(size == 1){
			mData = new byte[3+data.length];
			//1倍字体大小  默认
			System.arraycopy(_1x, 0, mData, 0, _1x.length);
			System.arraycopy(data, 0, mData, _1x.length, data.length);
			mPrintQueue.addText(concentration, mData);
		}else if(size == 2){
			mData = new byte[3+data.length];
			//1倍字体大小  默认
			System.arraycopy(_2x, 0, mData, 0, _2x.length);
			System.arraycopy(data, 0, mData, _2x.length, data.length);
			mPrintQueue.addText(concentration, mData);
		}

	}

    private void printBulkData(String arg, CallbackContext callbackContext){
      cordova.getThreadPool().execute(new Runnable() {
          public void run() {
              try{
                JSONObject obj = new JSONObject(arg);
                JSONArray printableArray = obj.getJSONArray("printableObjects");

                Integer datalen = printableArray.length();

                for (int i = 0; i < datalen; ++i){
                  JSONObject printable = printableArray.getJSONObject(i);
                  if(printable.has("text")){
                    printText(printable,false,callbackContext);
                  }
                  if(printable.has("image")){
                    //printBase64Image(printable,false,callbackContext);
                  }
                  if(printable.has("qrtext")){
                    Thread.sleep(100);
                    //printQR(printable,false,callbackContext);
                  }
                }
                callbackContext.success("Printed " + datalen + " objects.");
              } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    callbackContext.error(sw.toString());
              }
          }
      });
    }

    private void printText(JSONObject obj, Boolean standalone, CallbackContext callbackContext){
        // Charset UTF-8 new byte[]{0x1C, 0x43, (byte) 0xFF}
        try{
          //Log.i(TAG,"Sending to print");
          int  concentration = 60;
          StringBuilder sb = new StringBuilder();
          String text = obj.getString("text");
          Integer align = obj.getInt ("align");
          sb.append(text);
          byte[] btUTF8 = null;
          btUTF8 =sb.toString().getBytes("UTF-8");
          //btUTF8 = sb.toString().getBytes("GBK");
          addPrintTextWithSize(1, concentration, btUTF8);
          mPrintQueue.printStart();
          callbackContext.success("Printing");
        }catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            callbackContext.error(sw.toString());
        }
    }
}
