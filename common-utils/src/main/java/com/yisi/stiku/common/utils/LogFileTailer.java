package com.yisi.stiku.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.common.bean.LogFileTailerListener;

/**
 * @author shangfeng
 *
 */
public class LogFileTailer extends Thread {

	private final static Logger LOG = LoggerFactory.getLogger(LogFileTailer.class);

	private long sampleInterval = 1000;

	private File logfile;

	private boolean startAtBeginning = false;

	private boolean tailing = true;

	private String fileEncoding = "UTF-8";

	private Set<LogFileTailerListener> listeners = new HashSet<LogFileTailerListener>();

	public LogFileTailer(File file) {

		this(file, false);
	}

	public LogFileTailer(File file, boolean startAtBeginning) {

		this(file, 1000, startAtBeginning);
	}

	public LogFileTailer(File file, boolean startAtBeginning, String fileEncoding) {

		this(file, 1000, startAtBeginning, fileEncoding);
	}

	public LogFileTailer(File file, long sampleInterval,
			boolean startAtBeginning) {

		this(file, sampleInterval, startAtBeginning, "UTF-8");
	}

	public LogFileTailer(File file, long sampleInterval,
			boolean startAtBeginning, String fileEncoding) {

		this.logfile = file;
		this.sampleInterval = sampleInterval;
		this.startAtBeginning = startAtBeginning;
		this.fileEncoding = fileEncoding;
	}

	public void addLogFileTailerListener(LogFileTailerListener l) {

		this.listeners.add(l);
	}

	public void removeLogFileTailerListener(LogFileTailerListener l) {

		this.listeners.remove(l);
	}

	protected void fireNewLogFileLine(String line) {

		for (Iterator<LogFileTailerListener> i = this.listeners.iterator(); i.hasNext();) {
			LogFileTailerListener l = (LogFileTailerListener) i.next();
			l.newLine(line);
		}
	}

	public void stopTailing() {

		this.tailing = false;
	}

	public void run() {

		long filePointer = 0;

		if (this.startAtBeginning) {
			filePointer = 0;
		} else {
			filePointer = this.logfile.length();
		}

		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(logfile, "r");
			while (this.tailing) {
				long fileLength = this.logfile.length();
				if (fileLength < filePointer) {
					file = new RandomAccessFile(logfile, "r");
					filePointer = 0;
				}
				if (fileLength > filePointer) {
					file.seek(filePointer);
					String line = file.readLine();
					while (line != null) {
						line = new String(line.getBytes("ISO8859-1"), this.fileEncoding);
						this.fireNewLogFileLine(line);
						line = file.readLine();
					}
					filePointer = file.getFilePointer();
				}
				sleep(this.sampleInterval);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				file.close();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

}
