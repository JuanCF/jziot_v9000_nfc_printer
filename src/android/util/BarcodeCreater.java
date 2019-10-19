package cordova.plugin.jziot.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.TextView;


public abstract class BarcodeCreater {

	/**
	 * 鍥剧墖涓ょ鎵�淇濈暀鐨勭┖鐧界殑瀹藉害
	 */
	private static int marginW = 20;
	/**
	 * 鏉″舰鐮佺殑缂栫爜绫诲瀷
	 */
	public static BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;

	/**
	 * 鐢熸垚鏉″舰鐮�
	 *
	 * @param context
	 * @param contents
	 *            闇�瑕佺敓鎴愮殑鍐呭
	 * @param desiredWidth
	 *            鐢熸垚鏉″舰鐮佺殑瀹藉甫
	 * @param desiredHeight
	 *            鐢熸垚鏉″舰鐮佺殑楂樺害
	 * @param displayCode
	 *            鏄惁鍦ㄦ潯褰㈢爜涓嬫柟鏄剧ず鍐呭
	 * @return
	 */
	public static Bitmap creatBarcode(Context context, String contents,
			int desiredWidth, int desiredHeight, boolean displayCode,
			int barType) {
		Bitmap ruseltBitmap = null;
		if (barType == 1) {
			barcodeFormat = BarcodeFormat.CODE_128;
		} else if (barType == 2) {
			barcodeFormat = BarcodeFormat.QR_CODE;
		}
		if (displayCode) {
			Bitmap barcodeBitmap = null;
			try {
				barcodeBitmap = encodeAsBitmap(contents, barcodeFormat,
						desiredWidth, desiredHeight);
			} catch (WriterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Bitmap codeBitmap = creatCodeBitmap(contents, desiredWidth,
					desiredHeight, context);
			ruseltBitmap = mixtureBitmap(barcodeBitmap, codeBitmap, new PointF(
					0, desiredHeight));
		} else {
			try {
				ruseltBitmap = encodeAsBitmap(contents, barcodeFormat,
						desiredWidth, desiredHeight);
			} catch (WriterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ruseltBitmap;
	}

	/**
	 * 鐢熸垚鏄剧ず缂栫爜鐨凚itmap
	 *
	 * @param contents
	 * @param width
	 * @param height
	 * @param context
	 * @return
	 */
	public static Bitmap creatCodeBitmap(String contents, int width,
			int height, Context context) {
		TextView tv = new TextView(context);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				width, height);
		tv.setLayoutParams(layoutParams);
		tv.setText(contents);
		//tv.setHeight(48);
		tv.setTextSize(25);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		tv.setWidth(width);
		tv.setDrawingCacheEnabled(true);
		tv.setTextColor(Color.BLACK);
		tv.setBackgroundColor(Color.WHITE);
		tv.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

		tv.buildDrawingCache();
		Bitmap bitmapCode = tv.getDrawingCache();
		return bitmapCode;
	}

	/**
	 * 鐢熸垚鏉″舰鐮佺殑Bitmap
	 *
	 * @param contents
	 *            闇�瑕佺敓鎴愮殑鍐呭
	 * @param format
	 *            缂栫爜鏍煎紡
	 * @param desiredWidth
	 * @param desiredHeight
	 * @return
	 * @throws WriterException
	 */
	public static Bitmap encode2dAsBitmap(String contents, int desiredWidth,
			int desiredHeight, int barType) {
		if (barType == 1) {
			barcodeFormat = BarcodeFormat.CODE_128;
		} else if (barType == 2) {
			barcodeFormat = BarcodeFormat.QR_CODE;
		}
		Bitmap barcodeBitmap = null;
		try {
			barcodeBitmap = encodeAsBitmap(contents, barcodeFormat,
					desiredWidth, desiredHeight);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return barcodeBitmap;
	}

	/**
	 * 灏嗕袱涓狟itmap鍚堝苟鎴愪竴涓�
	 *
	 * @param first
	 * @param second
	 * @param fromPoint
	 *            绗簩涓狟itmap寮�濮嬬粯鍒剁殑璧峰浣嶇疆锛堢浉瀵逛簬绗竴涓狟itmap锛�
	 * @return
	 */
	public static Bitmap mixtureBitmap(Bitmap first, Bitmap second,
			PointF fromPoint) {
		if (first == null || second == null || fromPoint == null) {
			return null;
		}

		Bitmap newBitmap = Bitmap.createBitmap(first.getWidth(),
				first.getHeight() + second.getHeight(), Config.ARGB_4444);
		Canvas cv = new Canvas(newBitmap);
		cv.drawBitmap(first, 0, 0, null);
		cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
		cv.save(Canvas.ALL_SAVE_FLAG);
		cv.restore();

		return newBitmap;
	}

	public static Bitmap encodeAsBitmap(String contents, BarcodeFormat format,
			int desiredWidth, int desiredHeight) throws WriterException {
		final int WHITE = 0xFFFFFFFF; // 鍙互鎸囧畾鍏朵粬棰滆壊锛岃浜岀淮鐮佸彉鎴愬僵鑹叉晥鏋�
		final int BLACK = 0xFF000000;

		HashMap<EncodeHintType, String> hints = null;
		String encoding = guessAppropriateEncoding(contents);
		if (encoding != null) {
			hints = new HashMap<EncodeHintType, String>(2);
			hints.put(EncodeHintType.CHARACTER_SET, encoding);
		}
		MultiFormatWriter writer = new MultiFormatWriter();
		BitMatrix result = writer.encode(contents, format, desiredWidth,
				desiredHeight, hints);
		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		// All are 0, or black, by default
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	public static String guessAppropriateEncoding(CharSequence contents) {
		// Very crude at the moment
		for (int i = 0; i < contents.length(); i++) {
			if (contents.charAt(i) > 0xFF) {
				return "UTF-8";
			}
		}
		return null;
	}

	public static boolean saveBitmap2file(Bitmap bmp, String filename) {
		CompressFormat format = Bitmap.CompressFormat.JPEG;
		int quality = 100;
		OutputStream stream = null;
		try {
			stream = new FileOutputStream("/sdcard/" + filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bmp.compress(format, quality, stream);
	}
}
