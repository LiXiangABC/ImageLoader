package com.example.lixiang.imageload;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;


public abstract class baseMultithreadLoader {
	public baseMultithreadLoader(){
		init();
	}

	/**
	 * 队列调度方式*/
	public enum Type
	{
		FIFO, LIFO;
	}
	/**
	 *线程
	 */
	public enum ThreadProxyType
	{
		LOCAL,IMAGE,FILE ;
	}

	/**
	 * 队列的调度方式
	 */
	private Type mType = Type.LIFO;
	/**
	 * 任务队列：是一个list链表集合
	 */
	private LinkedList<Runnable> mTaskQueue;
	/**
	 * 后台轮询线程
	 */
	private Thread mPoolThread;
	private static Handler mPoolThreadHandler;
	private void init( )
	{
		mTaskQueue = new LinkedList<Runnable>();
		//初始化后台轮询线程
		initBackThread();
		initSupplement();
	}
	

	/**
	 * @author lixiang 补充初始化条件
	 * */
	public void initSupplement() {
		
	}
	/**
	 * 初始化后台轮询线程
	 */
	private void initBackThread()
	{
		if(mPoolThread == null)
		{
			// 后台轮询的子线程
			mPoolThread = new Thread()
			{
				@Override
				public void run()
				{
//					开启一个looper循环【在这个过程中looper在这个子线程中一直处于循环状态，但一直循环最多同时只能执行threadCount；因为有
//					线程池和Semaphore信号量的阻塞】
					Looper.prepare();
					mPoolThreadHandler = new Handler()
					{
						@Override
						public void handleMessage(Message msg)
						{
							if(msg.what == 1)
							{
								// 线程池去取出一个任务进行执行；获取到消息就获取许可，执行下面的代码
								
//								mThreadPool.execute(getTask());//去获取list集合中的任务了
								Runnable task = getTask();
								if (task != null) {
									new Thread(task).start();
//									task.run();
								}
							}
						}
					};
					Looper.loop();
				};
			};	
		}
		

		mPoolThread.start();
	}

	/**
	 * 从任务队列取出一个方法
	 * @return
	 */
	private Runnable getTask()
	{
		if (mType == Type.FIFO)
		{
//			System.out.println("当前队列等待数值为：   " +mTaskQueue.size() );
			if(mTaskQueue.size()>0)
			{
				return mTaskQueue.removeFirst();
			}
		} else if (mType == Type.LIFO)
		{
//			System.out.println("当前队列等待数值为：   " +mTaskQueue.size() );
			try {
				if(mTaskQueue.size()>0)
				{
					return mTaskQueue.removeLast();
				}
				
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return null;
	}
	
	
	
	/**
	 * 根据传入的参数，新建一个任务
	 * 单独开启的子线程；
	 * 每一次加载都会开启一个；然后存储在list集合中
	 * @return
	 */
	public Runnable buildTask(LoadImageBean loadImageBean)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				runThreadPoolChildThread(loadImageBean);
			}
		};
	}
	
	/**
	 * @author lixiang 往队列里添加一个元素
	 * 
	 */
	public synchronized void addTask(Runnable runnable)
	{
		mTaskQueue.add(runnable);

		while(mPoolThreadHandler == null)
		{
			if(mPoolThreadHandler == null){
//				修复mPoolThreadHandler为null的BUG
				SystemClock.sleep(50);
				break ;
			}else {
				return;
			}
		}
		
		
		mPoolThreadHandler.sendEmptyMessage(1);
	}
	
	/**
	 * @author lixiang 队列线程池中需要执行的方法
	**/
	public abstract void runThreadPoolChildThread(LoadImageBean loadImageBean);

	/**
	 * @author lixiang 获取list集合取值类型
	 * */
	public abstract  ThreadProxyType   setThreadProxyType();
}
