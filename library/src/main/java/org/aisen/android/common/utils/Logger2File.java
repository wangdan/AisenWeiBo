package org.aisen.android.common.utils;

import android.app.Application;
import android.content.Context;

import org.aisen.android.common.context.GlobalContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 将日志写入文件保存
 *
 * Created by wangdan on 16/4/26.
 */
public class Logger2File {

    public static boolean DEBUG = Logger.DEBUG;

    private static Calendar mCal;

    private static LoggerThread mThread;

    static void log2File(String tag, String log) {
        if (!DEBUG) {
            return;
        }

        try {
            if (GlobalContext.getInstance() != null) {
                LoggerThread thread = getThread(GlobalContext.getInstance());
                if (thread != null) {
                    thread.addLog(new Log(tag, log));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static void log2File(String tag, Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);

        log2File(tag, sw.toString());
    }

    private static LoggerThread getThread(Context context) {
        if (mCal == null) {
            mCal = Calendar.getInstance();
        }

        String fileName = String.format("%s_%s_%s_%s.txt", mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH) + 1,
                mCal.get(Calendar.DAY_OF_MONTH), mCal.get(Calendar.HOUR_OF_DAY));

        if (mThread == null || !mThread.fileName.equals(fileName)) {
            mThread = new LoggerThread(context, fileName);
            mThread.start();
        }

        return mThread;
    }

    static class LoggerThread extends Thread {

        FileWriter fileWriter;
        String fileName;
        DateFormat formatter;

        public LoggerThread(Context context, String fileName) {
            String filePath = context.getExternalFilesDir("logs").getAbsolutePath() + File.separator;
            this.fileName = fileName;
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                fileWriter = new FileWriter(file.getAbsolutePath(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            logsQueue = new LinkedBlockingQueue<>();
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }

        LinkedBlockingQueue<Log> logsQueue;

        void addLog(Log log) {
            if (fileWriter != null && logsQueue != null) {
                logsQueue.add(log);
            }
        }

        @Override
        public void run() {
            super.run();

            while (true) {
                try {
                    Log log = logsQueue.poll(30, TimeUnit.SECONDS);

                    if (log != null && fileWriter != null ) {
                        String line = formatter.format(mCal.getTime()) + "/" + log.tag + ":" + log.log;
                        try {
                            fileWriter.write(line + "\n\r");
                            fileWriter.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();

                    break;
                }
            }

            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (mThread == this) {
                mThread = null;
            }
        }

    }

    static class Log {
        String tag;

        String log;

        public Log(String tag, String log) {
            this.tag = tag;
            this.log = log;
        }
    }

}
