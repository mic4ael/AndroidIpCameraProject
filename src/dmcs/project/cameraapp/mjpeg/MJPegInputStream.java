package dmcs.project.cameraapp.mjpeg;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class MJPegInputStream extends DataInputStream {
	private final byte[] SOI_MARKER = {(byte) 0xFF, (byte)0xD8 };
	private final byte[] EOF_MARKER = {(byte) 0xFF, (byte)0xD9 };
	private final String CONTENT_LENGTH = "Content-Length";
	private final static int HEADER_MAX_LENGTH = 100;
	private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
	private int mContentLength = -1;
	
	public MJPegInputStream(InputStream is) {
		super(new BufferedInputStream(is, FRAME_MAX_LENGTH));
	}
	
	private int getEndOfSequence(DataInputStream in, byte[] seq)
		throws IOException {
		int seqIndex = 0;
		byte c;
		for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
			c = (byte) in.readUnsignedByte();
			if (c == seq[seqIndex]) {
				seqIndex++;
				if (seqIndex == seq.length) {
					return i + 1;
				}
			} else {
				seqIndex = 0;
			}
		}
		
		return -1;
	}
	
	private int getStartOfSequence(DataInputStream is, byte[] seq) throws IOException {
		int end = getEndOfSequence(is, seq);
		return (end < 0) ? -1 : (end - seq.length);
	}
	
	private int parseContentLength(byte[] headerBytes) throws IOException {
		ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
		Properties props = new Properties();
		props.load(headerIn);
		return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
	}
	
	public Bitmap readMjpegFrame() throws IOException {
		mark(FRAME_MAX_LENGTH);
		int headerLen = getStartOfSequence(this, SOI_MARKER);
		reset();
		byte[] header = new byte[headerLen];
		readFully(header);
		
		try {
			mContentLength = parseContentLength(header);
		} catch (NumberFormatException ex) {
			mContentLength = getEndOfSequence(this, EOF_MARKER);
		}
		
		reset();
		byte[] frameData = new byte[mContentLength];
		skipBytes(headerLen);
		readFully(frameData);
		return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
	}
}
