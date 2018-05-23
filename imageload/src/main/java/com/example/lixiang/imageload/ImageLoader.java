package com.example.lixiang.imageload;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.example.lixiang.imageload.utils.FileUtil;
import com.example.lixiang.imageload.utils.ImageUtil;
import com.example.lixiang.imageload.utils.Logs;
import com.example.lixiang.imageload.utils.ToastUtil;
import com.example.lixiang.imageload.utils.md5Utils;
import com.example.lixiang.okhttputil.OkHttpUtils;
import com.example.lixiang.okhttputil.callback.BitmapCallback;
import com.example.lixiang.okhttputil.callback.FileCallBack;

import java.io.File;
import java.util.regex.Pattern;

import okhttp3.Request;


@SuppressWarnings({"ALL", "FieldCanBeLocal"})
public class ImageLoader extends baseMultithreadLoader {
    private static ImageLoader uniqueInstance = null;
    public static boolean isDebug = false;

    private ImageLoader() {

    }

    ;

    public static ImageLoader getInstance() {

        if (uniqueInstance == null) {
            uniqueInstance = new ImageLoader();
        }

        return uniqueInstance;

    }


    /**
     * 图片缓存的核心对象
     */
    private LruCache<String, Bitmap> mLruCache;
    /**
     * UI线程中的Handler
     */
    private Handler mUIHandler;

    private static final String TAG = "ImageLoader";


    public void initSupplement() {
        // 初始化获取我们应用的最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
//		创建内存缓存
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }


    /**
     * 根据path为imageview设置图片
     * @param imageName      position
     * @param imageView 图片的视图对象
     */
    public void loadImage(final String imageName, final ImageView imageView,
                          final boolean isFromNet) {
        if (imageName == null) {
        throw new RuntimeException("图片名（图片路径）不可为null");
        }
        if (imageView == null) {
            throw new RuntimeException("图片展示对象imageView不可为null");
        }
        loadImage(new LoadImageBean().setImageName(imageName).setImageView(imageView).setFromNet(isFromNet));
    }
    public void loadImage(final String imageName, final ImageView imageView,
                          final boolean isFromNet,File cacheFile) {
        if (imageName == null) {
            throw new RuntimeException("图片名（图片路径）不可为null");
        }
        if (imageView == null) {
            throw new RuntimeException("图片展示对象imageView不可为null");
        }
        loadImage(new LoadImageBean().setImageName(imageName).setImageView(imageView).setFromNet(isFromNet).setCacheFile(cacheFile));
    }
    public void loadImage(LoadImageBean loadImageBean) {
        if (loadImageBean.imageName == null) {
            throw new RuntimeException("图片名（图片路径）不可为null");
        }
        if (loadImageBean.imageView == null) {
            throw new RuntimeException("图片展示对象imageView不可为null");
        }
//		为图片的视图对象设置target，值为路径   path（position）
        loadImageBean.imageView.setTag(loadImageBean.imageName);

//		当第一次调用主线程（UI线程）对象还没有被创建的时候
        if (mUIHandler == null) {
//			本handler是在主线程中创建的；当获取到子线程返回的holder对象就判断其target是否还相等
            mUIHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // 获取得到图片，为imageview回调设置图片
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    Bitmap bm = holder.bitmap;
                    ImageView imageview = holder.imageView;
                    String imageName = holder.imageName;
                    // 将imageName与getTag存储路径进行比较；当处于相等的时候说明Item还没有被回收；于是就进行展示
                    if (imageview.getTag().toString().equals(imageName)) {
                        imageview.setImageBitmap(bm);
                    }
                }

                ;
            };
        }
        // 根据imageName(key；内存缓存(lruCache)的存储值)在缓存中获取bitmap
        Bitmap bm = getBitmapFromLruCache(loadImageBean.imageName);

//		当内存缓存中存在这张图片的时候
        if (bm != null) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            ImageUtil.ImageSize imageSize = ImageUtil.getImageViewSize(loadImageBean.imageView);

            int imageViewWidth= imageSize.width;
            int imageViewHeight= imageSize.height;

            Logs.Log("ImageLoader","width:"+width+"  height:"+height+"  imageViewWidth:"+imageViewWidth+"  imageViewHeight:"+imageViewHeight);
//          当imageView大于原图的时候，判断当前的bitmap尺寸是否大于0.7的imageView的尺寸,大于就返回
            if ((imageViewWidth*3 >= width && width >= imageViewWidth*0.3) || (imageViewHeight*3 >= height && bm.getHeight() >= imageViewHeight*0.3)) {
                Logs.Log("ImageLoader","图片符合规范");
            refreashBitmap(loadImageBean.imageName, loadImageBean.imageView, bm);
            }else if(imageViewWidth*3 <width||imageViewHeight*3 < height){
                int widthRadio = Math.round(width * 1.0f / imageViewWidth);
                int heightRadio = Math.round(height * 1.0f / imageViewHeight);
                int inSampleSize = Math.max(widthRadio, heightRadio);
//                当bitmap远远大于ImageView的时候，就再进行一次缩放压缩
                        Logs.Log("ImageLoader","图片过大进行了一次比例压缩");
                    Bitmap dst = Bitmap.createScaledBitmap(bm, width/inSampleSize, height/inSampleSize, true);
                        refreashBitmap(loadImageBean.imageName, loadImageBean.imageView, dst);
            }else {
                Logs.Log("ImageLoader","图片过小进行重新加载");
                addTask(buildTask(loadImageBean));//在buildTask（）方法中包含了遍历本地应硬盘缓存和获取网络缓存；buildTask（）中是创建了一个子线程；这样放置的好处就是，
            }
		} else
//			当内存缓存中不存在这张照片的时候,或者小于就重新去本地或者服务器请求，因为图片会模糊
		{
            Logs.Log("ImageLoader","当前没有内存缓存正在重新加载");
			addTask(buildTask(loadImageBean));//在buildTask（）方法中包含了遍历本地应硬盘缓存和获取网络缓存；buildTask（）中是创建了一个子线程；这样放置的好处就是，
			                                               //遍历磁盘和访问网络都是属于高耗时操作，放在子线程程里面不会影响主线程的执行效率
		}

	}

//  调用此方法，将从内存缓存中获取到的图片资源通过UI handler返回给指定的视图展示对象
private void refreashBitmap(final String path, final ImageView imageView,
		Bitmap bm)
{
	Message message = Message.obtain();
	ImgBeanHolder holder = new ImgBeanHolder();
	holder.bitmap = bm;
	holder.imageName = path;
	holder.imageView = imageView;
	message.obj = holder;
	mUIHandler.sendMessage(message);
}

	/**
	 * 根据path在缓存中获取bitmap
	 *
	 * @param key
	 * @return
	 */
	private Bitmap getBitmapFromLruCache(String key)
	{
		if (key != null) {
		return mLruCache.get(key);
		}
		return null;
	}




    @Override
    public void runThreadPoolChildThread(LoadImageBean loadImageBean) {

        File file;
        //		创建一个将以MD5加密的文件名到文件目录,会在指定目录中查找和缓存文件
        if (loadImageBean.cacheFile != null) {
            file = new File( loadImageBean.cacheFile, md5Utils.md5(loadImageBean.getImageName()));
            Logs.Log("走的是当前路径",file.getAbsolutePath());
        }else {
            /**        Explain : 当没有设定缓存目录名称就设定默认名称，会在默认目录中查找和缓存文件
             * @author LiXiang create at 2018/4/3 17:38*/
            file = FileUtil.getDiskCacheDir(loadImageBean.imageView.getContext(),
                    "ICarZooImageLoader" , md5Utils.md5(loadImageBean.imageName));
        }



        /**        Explain : 先判断当前的imageView是否是网址，需要要下载，当不是的时候，判断是不是本地生成的文件
        * @author LiXiang create at 2018/4/4 18:17*/
        Pattern pattern = Pattern
//                .compile("^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$");
                .compile("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");
        if (!pattern.matcher(loadImageBean.imageName).matches()) {
            Logs.Log("当前地址是不合规URL:",loadImageBean.imageName);
            loadLocalImage(loadImageBean, file);
            return;
        }
            Logs.Log("当前地址是合规URL:",loadImageBean.imageName);

	    /**        Explain : 读取本地配置文件中的信息
	    * @author LiXiang create at 2018/4/3 17:40*/
        SharedPreferences preferences = loadImageBean.imageView.getContext().getSharedPreferences(md5Utils.md5(loadImageBean.getImageName()), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
//        preferences.getLong("imageDownLoadTatol-"+md5Utils.md5(loadImageBean.getImageName()), 0);
        Logs.Log("当前图片文件总大小",preferences.getLong("imageDownLoadSum", 0)+"");
        Logs.Log("当前图片文件已下载大小",preferences.getLong("imageDownLoadTatol", 0)+"");
        /**        Explain : 获取当前的下载量是否的为0，当为0的时候就认定为未下载过，
         *                   会重置文件总大小、URL地址数据
        * @author LiXiang create at 2018/4/3 17:53*/
        if (preferences.getLong("imageDownLoadSum", 0) == 0) {
            Logs.Log("初始化下载当前图片文件:",loadImageBean.imageName);
            editor.putLong("imageDownLoadTatol", 0);
            editor.putLong("imageDownLoadSum", 0);
//            editor.putString("imageURL", md5Utils.md5(loadImageBean.getImageName()));
            editor.commit();

            /**        Explain : 直接进行网络下载图片
            * @author LiXiang create at 2018/4/3 18:10*/
            downLoadImage(loadImageBean, file, preferences);
            
            
            /**        Explain : 下载数和文件总大小相等就是文件下载完成
            * @author LiXiang create at 2018/4/4 15:54*/
        }else if(preferences.getLong("imageDownLoadSum", 0) == preferences.getLong("imageDownLoadTatol", 0)){
            Logs.Log("当前图片文件是完整图片文件正在从本地加载:",loadImageBean.imageName);

            /**        Explain : 本地获取图片，获取到了就终止下面的代码逻辑
             * @author LiXiang create at 2018/4/3 18:17*/
            if (loadLocalImage(loadImageBean, file)) return;
            Logs.Log("当前图片文件不存在正在重新下载:",loadImageBean.imageName);
            /**        Explain : 当配置文件存在，而图片文件不存在则直接重置下载
            * @author LiXiang create at 2018/4/4 15:58*/
            editor.putLong("imageDownLoadTatol", 0);
            editor.putLong("imageDownLoadSum", 0);
//            editor.putString("imageURL", md5Utils.md5(loadImageBean.getImageName()));
            editor.commit();
            downLoadImage(loadImageBean, file,preferences);
        }else {
            Logs.Log("当前图片文件未下载完成继续下载:",loadImageBean.imageName);
            downLoadImage(loadImageBean, file,preferences);
        }



	}

    private void downLoadImage(LoadImageBean loadImageBean, File file, SharedPreferences preferences) {
        if (!loadImageBean.isFromNet)// 当本地文件中没有判断当前是否有让支持网络
        {
            return;
        }
        long imageDownLoadSum = preferences.getLong("imageDownLoadSum", 0);
        SharedPreferences.Editor editor = preferences.edit();
        OkHttpUtils//
                .get()//
                .addHeader("RANGE", "bytes=" + imageDownLoadSum+"-")//
                .url(loadImageBean.imageName)//
                .build()//
                .execute(new FileCallBack(file.getParentFile().getAbsolutePath(), md5Utils.md5(loadImageBean.imageName), imageDownLoadSum)
                {//mProgressBar.setProgress((int) (100 * progress));
                    @Override
                    public void inProgress(float progress) {}

                    @Override
                    public void inProgress(long total, long sum, float progress) {
                        super.inProgress(total, sum, progress);
                        /**        Explain : 当,当前下载为0的时候就写入总量大小
                         * @author LiXiang create at 2018/5/23 12:34*/
                        synchronized (ImageLoader.this){
                            //TODO : 此处后面需加上判断
//                            if (imageDownLoadSum == 0) {
                            editor.putLong("imageDownLoadTatol", total);
//                            }
                            editor.putLong("imageDownLoadSum", sum);
                            editor.commit();
                        }
                    }

                    public void onError(Request request, Exception e) {
                        Log.e(TAG, "onError :" + e.getMessage());
//									bm[0] = ImageUtil.loadImageFromLocalToView(file.getAbsolutePath(),
//											imageView);
//						当这次下载失败就用失败的图片代替，但要删除本地对这个地址的错误显示图片的缓存图片文件，这样，下次打开的时候就能够
//						重新下载，而不会还是显示缓存中错误图片
                        editor.clear();
                        editor.commit();
                        if (file.exists()) {
                            file.delete();
                        }

                        OkHttpUtils
                                .get()//
                                .url(loadImageBean.imageName)//
                                .build()//
                                .execute(new BitmapCallback() {
                                    public void onError(Request request, Exception e) {
                                    }

                                    @Override
                                    public void onResponse(Bitmap bitmap) {
                                        loadImageBean.imageView.setImageBitmap(bitmap);
                                    }
                                });
                    }

                    @Override
                    public void onResponse(File file) {

                        Bitmap bitmap = ImageUtil.loadImageFromLocalToView(file.getAbsolutePath(), loadImageBean.imageView);
                        if (bitmap != null) {
                            //						将指定路径下的图片返回给指定的image对象
                            refreashImageViewAndLruCache(loadImageBean.imageName, loadImageBean.imageView, bitmap);
                            Log.e(TAG, "find image for net:" + loadImageBean.imageName+ " in disk cache .");
                            return;
                        }else {
                            file.delete();
                            ToastUtil.showToast(loadImageBean.imageView.getContext(),"图片加载失败请重新操作");
                        }
                    }
                });
    }

    private boolean loadLocalImage(LoadImageBean loadImageBean, File file) {
        if (file.exists() && file.length() > 0)// 如果在缓存文件中发现
        {
            File mFile = new File(file.getAbsolutePath());
            Bitmap bitmap = ImageUtil.loadImageFromLocalToView(file.getAbsolutePath(), loadImageBean.imageView);
            if (bitmap != null) {
            refreashImageViewAndLruCache(loadImageBean.imageName, loadImageBean.imageView, bitmap);
                Logs.Log("本地发现图片文件:",loadImageBean.imageName);
                return true;
            }
        }else {
                Logs.Log("本地未发现图片文件:",loadImageBean.imageName);
        }
        return false;
    }

    private void refreashImageViewAndLruCache(String path, ImageView imageView, Bitmap bitmap) {
        //把图片加入到内存缓存
        addBitmapToLruCache(path, bitmap);
//		从内存缓存中获取对应Bitmap资源
        refreashBitmap(path, imageView, bitmap);
    }

    /**
     * 将图片加入LruCache
     *
     * @param path
     * @param bm
     */
    protected void addBitmapToLruCache(String path, Bitmap bm) {
        if (path != null) {


//		判断该要缓存的图片，是否已经存在于内存缓存中
            if (getBitmapFromLruCache(path) == null) {
//			当不存在的时候就将该图片缓存到内存缓存中
                if (bm != null)
                    mLruCache.put(path, bm);
            } else {
            }
        }
    }


    public void deletedBitmapToLruCache(String key) {
        mLruCache.remove(key);
    }

    @Override
    public ThreadProxyType setThreadProxyType() {
        // TODO Auto-generated method stub
        return ThreadProxyType.IMAGE;
    }
}
