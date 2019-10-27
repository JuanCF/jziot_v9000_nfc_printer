package cordova.plugin.jziot;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64.Decoder;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;
import android.posapi.PosApi;
import android.posapi.PosApi.OnCommEventListener;
import android.zyapi.PrintQueue;
import android.zyapi.PrintQueue.OnPrintListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;


import cordova.plugin.jziot.util.BitmapTools;
import cordova.plugin.jziot.util.BarcodeCreater;

import android.util.Log;

/**
 * This class echoes a string called from JavaScript.
 */
public class jziotPrinter extends CordovaPlugin {

    private static final String TAG = "jziotPrinter";

    private PosApi mPosApi;

    private PrintQueue mPrintQueue = null;

    private Bitmap mBitmap = null;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
      super.initialize(cordova, webView);

      try {
          FileWriter localFileWriterOn = new FileWriter(new File("/proc/gpiocontrol/set_sam"));
          localFileWriterOn.write("1");
          localFileWriterOn.close();
      } catch (Exception e) {
          e.printStackTrace();
      }

      Context context = cordova.getActivity().getApplicationContext();
      mPosApi = PosApi.getInstance(context);

      cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            mPosApi.setOnComEventListener(createCommListener(context));
            mPosApi.initDeviceEx("/dev/ttyMT2");
            //preparePrinterQueue();
          }
      });
    }

    @Override
    public void onDestroy() {

      /*if(mBitmap!=null){
          mBitmap.recycle();
      }

      if(mPrintQueue!=null){
          mPrintQueue.close();
      }*/

      cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            if(mPosApi!=null){
		      mPosApi.closeDev();
            }
          }
      });

      try {
          FileWriter localFileWriterOn = new FileWriter(new File("/proc/gpiocontrol/set_sam"));
          localFileWriterOn.write("0");
          localFileWriterOn.close();
      } catch (Exception e) {
          e.printStackTrace();
      }

      super.onDestroy();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("testMethod")) {
            String message = args.getString(0);
            this.testMethod(message, callbackContext);
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

    private void testMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private OnCommEventListener createCommListener(Context context){
        //Create CommEventListener
        OnCommEventListener mCommEventListener = new OnCommEventListener() {
          @Override
          public void onCommState(int cmdFlag, int state, byte[] resp, int respLen) {
              // TODO Auto-generated method stub
              switch(cmdFlag){
              case PosApi.POS_INIT:
                  if(state==PosApi.COMM_STATUS_SUCCESS){
                       Toast.makeText(context, "Inicialización exitosa", Toast.LENGTH_SHORT).show();
                  }else {
                      Toast.makeText(context, "Inicialización exitosa", Toast.LENGTH_SHORT).show();
                  }
                  break;
              }
          }
      };
      return mCommEventListener;
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

      /*try {
          FileWriter localFileWriterOn = new FileWriter(new File("/proc/gpiocontrol/set_sam"));
          localFileWriterOn.write("1");
          localFileWriterOn.close();
      } catch (Exception e) {
          e.printStackTrace();
      }

      Context context = cordova.getActivity().getApplicationContext();
      mPosApi = PosApi.getInstance(context);

      cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            mPosApi.setOnComEventListener(createCommListener(callbackContext));
            mPosApi.initDeviceEx("/dev/ttyMT2");
            preparePrinterQueue();
          }
      });*/

      cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            preparePrinterQueue();
            Thread.sleep(1000);
            callbackContext.success("Controller enabled");
          }
      });
	}

    private void turnOffPrinter(CallbackContext callbackContext) {

      if(mBitmap!=null){
          mBitmap.recycle();
      }

      if(mPrintQueue!=null){
          mPrintQueue.close();
          mPrintQueue = null
      }

      callbackContext.success("Controller disabled");

      /*cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            if(mPosApi!=null){
		      mPosApi.closeDev();
              mPosApi = null;
            }
          }
      });

      try {
          FileWriter localFileWriterOn = new FileWriter(new File("/proc/gpiocontrol/set_sam"));
          localFileWriterOn.write("0");
          localFileWriterOn.close();
          callbackContext.success("Controller disabled");
      } catch (Exception e) {
          callbackContext.error("Controller can not be disabled");
          e.printStackTrace();
      }*/
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
                    printBase64Image(printable,false,callbackContext);
                  }
                  if(printable.has("qrtext")){
                    printQR(printable,false,callbackContext);
                  }
                }
                mPrintQueue.printStart();
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
          sb.append(justificateText(align,text));
          byte[] btUTF8 = null;
          btUTF8 =sb.toString().getBytes("UTF-8");
          //btUTF8 = sb.toString().getBytes("GBK");
          addPrintTextWithSize(1, concentration, btUTF8);
          if(standalone){
            callbackContext.success("Text  sent to printer");
          }
        }catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            callbackContext.error(sw.toString());
        }
    }

    private String justificateText(Integer align, String textToJustify){
      int textLength = textToJustify.length();
      String justifiedText = "";
      if(textLength == 32){
        return textToJustify;
      }else{
        if(textLength < 32){
          int length = 16;
          switch(align) {
            case 0:
              justifiedText = textToJustify;
              break;
            case 1:
              if(textLength % 2 == 0){
                length += textLength / 2;
              }else{
                length += textLength / 2 + 1;
              }
              justifiedText = String.format("%1$"+length+ "s", textToJustify);
              justifiedText = String.format("%1$-32s", justifiedText);
              //Log.i(TAG,justifiedText);
              break;
            case 2:
              justifiedText = String.format("%1$32s", textToJustify);
              break;
            default:
               justifiedText = textToJustify;
          }
        }else{
          //Add code here to cut string into slices of 32 or less
          justifiedText = textToJustify;
        }
      }
      return justifiedText;
    }

    private void printQR(JSONObject obj, Boolean standalone, CallbackContext callbackContext){
        try{
          String qr = obj.getString("qrtext");
          int  concentration = 60;
          int  mWidth = 384;
          int  mHeight = 384;
          int margin_left = 10;
          if(obj.has("margin_left")){
            margin_left = obj.getInt("margin_left");
          }
          mBitmap = BarcodeCreater.encode2dAsBitmap(qr, mWidth, mHeight, 2);
          byte[] printData = BitmapTools.bitmap2PrinterBytes(mBitmap);
          mPrintQueue.addBmp(concentration, margin_left, mBitmap.getWidth(), mBitmap.getHeight(), printData);
          if(standalone){
            callbackContext.success("QR sent to print");
          }
        }catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            callbackContext.error(sw.toString());
        }
    }

    private void printBase64Image(JSONObject obj, Boolean standalone, CallbackContext callbackContext){
        try{
          String base64Img = obj.getString("image");
          int  concentration = 60;
          int margin_left = 10;
          if(obj.has("margin_left")){
            margin_left = obj.getInt("margin_left");
          }
          if(obj.has("concentration")){
            concentration = obj.getInt("concentration");
          }
          String cleanImage = base64Img.replace("data:image/png;base64,", "").replace("data:image/jpeg;base64,","");
          byte[] decodedString = Base64.decode(cleanImage, Base64.DEFAULT);
          mBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
          byte[] printData = BitmapTools.bitmap2PrinterBytes(mBitmap);
          mPrintQueue.addBmp(concentration, margin_left, mBitmap.getWidth(), mBitmap.getHeight(), printData);
          if(standalone){
            callbackContext.success("Image sent to printer");
          }
        }catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            callbackContext.error(sw.toString());
        }
    }
}
