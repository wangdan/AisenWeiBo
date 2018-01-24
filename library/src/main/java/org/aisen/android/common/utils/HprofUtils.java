package org.aisen.android.common.utils;

import android.os.Debug;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by wangdan on 15/4/21.
 */
public class HprofUtils {

    private final static String OOM_SUFFIX = ".hprof";

    public static void dumpHprof(String path) {
        try {
            String name = getDate() + OOM_SUFFIX;
            path = path + File.separator + name;
            File file = path != null ? new File(path) : null;
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            Debug.dumpHprofData(path);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static String getDate() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
    }

}
