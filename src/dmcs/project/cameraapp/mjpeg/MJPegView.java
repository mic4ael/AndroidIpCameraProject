package dmcs.project.cameraapp.mjpeg;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import dmcs.project.cameraapp.GlobalStore;

public class MJPegView extends SurfaceView implements SurfaceHolder.Callback {
	public static final int POSITION_UPPER_LEFT = 9;
	public static final int POSITION_UPPER_RIGHT = 3;
	public final static int POSITION_LOWER_LEFT = 12;
	public final static int POSITION_LOWER_RIGHT = 6;
	
	public final static int SIZE_STANDARD = 1;
	public final static int SIZE_BEST_FIT = 4;
	public final static int SIZE_FULLSCREEN = 8;
	
	private MJPegViewThread thread;
	private MJPegInputStream mIn = null;
	private boolean showFps = false;
	private boolean mRun = false;
	private boolean surfaceDone = false;
	private Paint overlayPaint;
	private int overlayTextColor;
	private int overlayBgColor;
	private int ovlPos;
	private int dispWidth;
	private int dispHeight;
	private int dispMode;
	
	public class MJPegViewThread extends Thread {
		private SurfaceHolder surfaceHolder;
		private int frameCounter = 0;
		private long start;
		private Bitmap ovl;
		
		public MJPegViewThread(SurfaceHolder sh, Context ctx) {
			this.surfaceHolder = sh;
		}
		
		@Override
		public void run() {
			start = System.currentTimeMillis();
			PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
			Bitmap bm;
			int width;
			int height;
			Rect destRect;
			Canvas c = null;
			Paint p = new Paint();
			String fps = null;
			HttpResponse res = null;
			
			while(mRun) {
				if (surfaceDone) {
					try {
						c = surfaceHolder.lockCanvas();
						synchronized(surfaceHolder) {
							try {
								res = GlobalStore.getHttpClient().execute(
										  new HttpGet(URI.create(GlobalStore.url)));
								bm = new MJPegInputStream(res.getEntity().getContent())
														  .readMjpegFrame();
								destRect = destRect(dispWidth, dispHeight / 2);
								c.drawColor(Color.BLACK);
								c.drawBitmap(bm, null, destRect, p);
								
								if (showFps) {
									p.setXfermode(mode);
									if (ovl != null ) {
										height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom - ovl.getHeight(); 
										width = ((ovlPos & 8) == 8) ? destRect.left : destRect.right - ovl.getWidth();
										c.drawBitmap(ovl, width, height, null);
									}
									
									p.setXfermode(null);
									frameCounter++;
									if ((System.currentTimeMillis() - start) >= 1000) {
										fps = frameCounter + " FPS";
										frameCounter = 0;
										start = System.currentTimeMillis();
										ovl = makeFpsOverlay(overlayPaint, fps);
									}
								}
								
							} catch (IOException ex) {
								continue;
							}
						}
					} finally {
						try {
							res.getEntity().consumeContent();
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (c != null)
							surfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		};
		
		private Rect destRect(int bmw, int bmh) {
			int tmpx;
			int tmpy;
			
			if (dispMode == MJPegView.SIZE_STANDARD) {
				tmpx = (dispWidth / 2) - (bmw / 2);
				tmpy = (dispHeight / 2) - (bmh / 2);
				return new Rect(tmpx, tmpy, bmw + tmpx, bmh + tmpy);
			}
			
			if (dispMode == MJPegView.SIZE_BEST_FIT) {
				float bmasp = (float) bmw / (float) bmh;
				bmw = dispWidth;
				bmh = (int) (dispWidth / bmasp);
				
				if (bmh > dispHeight) {
					bmh = dispHeight;
					bmw = (int) (dispHeight * bmasp);
				}
				
				tmpx = (dispWidth / 2) - (bmw / 2);
				tmpy = (dispHeight / 2) - (bmh / 2);
				return new Rect(tmpx, tmpy, bmw + tmpx, bmh + tmpy);
			}
			
			if (dispMode == MJPegView.SIZE_FULLSCREEN) {
				return new Rect(0, 0, dispWidth, dispHeight);
			}
			
			return null;
		}
		
		public void setSurfaceSize(int w, int h) {
			synchronized (surfaceHolder) {
				dispWidth = w;
				dispHeight = h;
			}
		}
		
		private Bitmap makeFpsOverlay(Paint p, String text) {
			Rect b = new Rect();
			p.getTextBounds(text, 0, text.length(), b);
			int bwidth = b.width() + 2;
			int bheight = b.height() + 2;
			Bitmap bm = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(bm);
			p.setColor(overlayBgColor);
			c.drawRect(0, 0, bwidth, bheight, p);
			p.setColor(overlayTextColor);
			c.drawText(text, -b.left + 1, (bheight / 2) - 
					   ((p.ascent() + p.descent()) / 2)  +1, p);
			return bm;
		}
	}
	
	
	private void init(Context ctx) {
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		thread = new MJPegViewThread(holder, ctx);
		setFocusable(true);
		overlayPaint = new Paint();
		overlayPaint.setTextAlign(Paint.Align.LEFT);
		overlayPaint.setTextSize(12);
		overlayTextColor = Color.WHITE;
		overlayBgColor = Color.BLACK;
		ovlPos = MJPegView.POSITION_LOWER_RIGHT;
		dispMode = MJPegView.SIZE_STANDARD;
		dispWidth = getWidth();
		dispHeight = getHeight();
	}
	
	public void startPlayback() {
		if (mIn != null) {
			mRun = true;
			thread.start();
		}
	}
	
	public void stopPlayback() {
		mRun = false;
		boolean retry = false;
		while(retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException ex) {
				Log.d(this.getClass().toString(), ex.toString());
			}
		}
	}
	
	public MJPegView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		init(ctx);
	}
	
	public MJPegView(Context context) {
		super(context);
		init(context);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int f, int w, int h) {
		thread.setSurfaceSize(w, h);
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		surfaceDone = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		surfaceDone = false;
		stopPlayback();
	}
	
	public void setSource(MJPegInputStream source) {
		mIn = source;
		startPlayback();
	}
	
	public void setOverlayPaint(Paint p) {
		overlayPaint = p;
	}
	
	public void setOverlayTextColor(int c) {
		overlayTextColor = c;
	}
	
	public void setOverlayBgColor(int c) {
		overlayBgColor = c;
	}
	
	public void setOverlayPosition(int pos) {
		ovlPos = pos;
	}
	
	public void setDispMode(int mode) {
		dispMode = mode;
	}
	
	public void setShowFps(boolean fps) {
		showFps = fps;
	}
} 
