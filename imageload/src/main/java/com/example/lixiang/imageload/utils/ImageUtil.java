package com.example.lixiang.imageload.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;

/**
 * http://blog.csdn.net/lmj623565791/article/details/41874561
 * @author zhy
 *
 */
public class ImageUtil
{
	/**
	 * 根据需求的宽和高以及图片实际的宽和高计算SampleSize
	 * 
	 * @param options
	 * @param
	 * @param
	 * @return
	 */
	public static int caculateInSampleSize(Options options, int reqWidth,
			int reqHeight)
	{
		int width = options.outWidth;
		int height = options.outHeight;

		int inSampleSize = 1;

		if (width > reqWidth || height > reqHeight)
		{
			int widthRadio = Math.round(width * 1.0f / reqWidth);
			int heightRadio = Math.round(height * 1.0f / reqHeight);

			inSampleSize = Math.max(widthRadio, heightRadio);
		}

		return inSampleSize;
	}

	/**
	 * 根据ImageView获适当的压缩的宽和高
	 * 
	 * @param imageView
	 * @return
	 */
	public static ImageSize getImageViewSize(ImageView imageView)
	{

		ImageSize imageSize = new ImageSize();
		DisplayMetrics displayMetrics = imageView.getContext().getResources()
				.getDisplayMetrics();
		

		LayoutParams lp = imageView.getLayoutParams();

		int width = imageView.getWidth();// 获取imageview的实际宽度
		if (width <= 0)
		{
			width = lp.width;// 获取imageview在layout中声明的宽度
		}
		if (width <= 0)
		{
			 //width = imageView.getMaxWidth();// 检查最大值
			width = getImageViewFieldValue(imageView, "mMaxWidth");
		}
		if (width <= 0)
		{
			width = displayMetrics.widthPixels;
		}

		int height = imageView.getHeight();// 获取imageview的实际高度
		if (height <= 0)
		{
			height = lp.height;// 获取imageview在layout中声明的宽度
		}
		if (height <= 0)
		{
			height = getImageViewFieldValue(imageView, "mMaxHeight");// 检查最大值
		}
		if (height <= 0)
		{
			height = displayMetrics.heightPixels;
		}
		imageSize.width = width;
		imageSize.height = height;

		return imageSize;
	}

	public static class ImageSize
	{
		public int width;
		public int height;
	}
	
	/**
	 * 通过反射获取imageview的某个属性值
	 * 
	 * @param object
	 * @param fieldName
	 * @return
	 */
	private static int getImageViewFieldValue(Object object, String fieldName)
	{
		int value = 0;
		try
		{
			Field field = ImageView.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			int fieldValue = field.getInt(object);
			if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE)
			{
				value = fieldValue;
			}
		} catch (Exception e)
		{
		}
		return value;

	}

	
public static Bitmap comp(Bitmap image) {

	    if(image == null)
	    {
//	    	BitmapDrawable bd = (BitmapDrawable)this.getResources().getDrawable(R.drawable.interntshibai);
//	    	return bd.getBitmap();
	    	return null;
	    }
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
	    if( baos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
	        baos.reset();//重置baos即清空baos
	        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩70%，把压缩后的数据存放到baos中
	    }
	    ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
	    Options newOpts = new Options();
	    //开始读入图片，此时把options.inJustDecodeBounds 设回true了
	    newOpts.inJustDecodeBounds = true;
	    Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
	    newOpts.inJustDecodeBounds = false;
	    int w = newOpts.outWidth;
	    int h = newOpts.outHeight;
	    //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
	    float hh = 800f;//这里设置高度为800f
	    float ww = 480f;//这里设置宽度为480f
	    //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
	    int be = 1;//be=1表示不缩放
	    if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
	        be = (int) (newOpts.outWidth / ww);
	    } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
	        be = (int) (newOpts.outHeight / hh);
	    }
	    if (be <= 0)
	        be = 1;
	    newOpts.inSampleSize = be;//设置缩放比例
	    //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
	    isBm = new ByteArrayInputStream(baos.toByteArray());
	    bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
	    return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
	}
	private static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>100) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }




	/**        Explain : 将指定路径下的图片返回给指定的image对象并且进行压缩
	* @author LiXiang create at 2017/10/24 17:46*/
	public static  Bitmap  loadImageFromLocalToView(final String path,
		final ImageView imageView)
{
	Bitmap bm;
	// 加载图片
	// 图片的压缩
	// 获得图片需要显示的大小
	ImageSize imageSize = ImageUtil.getImageViewSize(imageView);
	// 压缩图片
	bm = decodeSampledBitmapFromPath(path, imageSize.width,
			imageSize.height);
	return bm;
}


/**        Explain : 按指定尺寸进行图片压缩
* @author LiXiang create at 2017/10/25 0:33*/
	public static  Bitmap  loadImageFromLocalToView(final String path, int width,int height)
	{
		Bitmap bm;
		// 加载图片
		// 图片的压缩
		// 获得图片需要显示的大小
		// 压缩图片
		bm = decodeSampledBitmapFromPath(path, width, height);
		return bm;
	}


	public static  Bitmap loadByteFromLocalToView(byte[] byt,
			final ImageView imageView)
	{
		Bitmap bm;
		// 加载图片
		// 图片的压缩
		// 获得图片需要显示的大小
		ImageSize imageSize = ImageUtil.getImageViewSize(imageView);
		// 压缩图片
		bm = decodeSampledBitmapFromByte(byt, imageSize.width,
				imageSize.height);
		return bm;
	}



/**
 * 根据图片需要显示的宽和高对图片进行压缩
 *
 * @param path
 * @param width
 * @param height
 * @return
 */
	public static  Bitmap decodeSampledBitmapFromPath(String path, int width,
		int height)
{
	// 获得图片的宽和高，并不把图片加载到内存中
	Options options = new Options();
	options.inJustDecodeBounds = true;
	BitmapFactory.decodeFile(path, options);

	options.inSampleSize = ImageUtil.caculateInSampleSize(options,
			width, height);

	// 使用获得到的InSampleSize再次解析图片
	options.inJustDecodeBounds = false;
	Bitmap bitmap = BitmapFactory.decodeFile(path, options);
	return bitmap;
}
	public static  Bitmap decodeSampledBitmapFromPath(String path)
{
	// 获得图片的宽和高，并不把图片加载到内存中
	Options options = new Options();
	options.inJustDecodeBounds = true;
	BitmapFactory.decodeFile(path, options);
	// 使用获得到的InSampleSize再次解析图片
	options.inJustDecodeBounds = false;
	Bitmap bitmap = BitmapFactory.decodeFile(path, options);
	return bitmap;
}









	//通过byte获取位图
	public static  Bitmap decodeSampledBitmapFromByte(byte[] byt, int width,
			int height)
	{
		// 获得图片的宽和高，并不把图片加载到内存中
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(byt, 0, byt.length, options);

		options.inSampleSize = ImageUtil.caculateInSampleSize(options,
				width, height);

		// 使用获得到的InSampleSize再次解析图片
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeByteArray(byt, 0, byt.length, options);
		return bitmap;
	}


	/**
	* 获取本地图片
	*@author LiXaing
	*create at 2016/8/28 21:00
	*/
	public static Intent picFromLocalphoto(File picFile)
	{
		Uri photoUri = Uri.fromFile(picFile);
		Intent intent = new Intent(Intent.ACTION_PICK, null);
		//setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")调用了图片选择器
		//如果直接写intent.setType("image/*");  调用的是系统图库
		intent.setType("image/*");
		intent.putExtra("crop", "true");
//            intent.putExtra("aspectX", 1);
//
//            intent.putExtra("aspectY", 3);
//
//            intent.putExtra("outputX", 300);
//
//            intent.putExtra("outputY", 900);
		intent.putExtra("noFaceDetection", true); // no face detection
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		return intent;

	}
}
