package org.aisen.weibo.sina.support.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.DateUtils;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by wangdan on 16/1/7.
 */
public class AisenUtils {

    public static String convDate(String time) {
        Context context = GlobalContext.getInstance();
        Resources res = context.getResources();

        StringBuffer buffer = new StringBuffer();

        Calendar createCal = Calendar.getInstance();
        createCal.setTimeInMillis(Date.parse(time));
        Calendar currentcal = Calendar.getInstance();
        currentcal.setTimeInMillis(System.currentTimeMillis());

        long diffTime = (currentcal.getTimeInMillis() - createCal.getTimeInMillis()) / 1000;

        // 同一月
        if (currentcal.get(Calendar.MONTH) == createCal.get(Calendar.MONTH)) {
            // 同一天
            if (currentcal.get(Calendar.DAY_OF_MONTH) == createCal.get(Calendar.DAY_OF_MONTH)) {
                if (diffTime < 3600 && diffTime >= 60) {
                    buffer.append((diffTime / 60) + res.getString(R.string.msg_few_minutes_ago));
                } else if (diffTime < 60) {
                    buffer.append(res.getString(R.string.msg_now));
                } else {
                    buffer.append(res.getString(R.string.msg_today)).append(" ").append(DateUtils.formatDate(createCal.getTimeInMillis(), "HH:mm"));
                }
            }
            // 前一天
            else if (currentcal.get(Calendar.DAY_OF_MONTH) - createCal.get(Calendar.DAY_OF_MONTH) == 1) {
                buffer.append(res.getString(R.string.msg_yesterday)).append(" ").append(DateUtils.formatDate(createCal.getTimeInMillis(), "HH:mm"));
            }
        }

        if (buffer.length() == 0) {
            buffer.append(DateUtils.formatDate(createCal.getTimeInMillis(), "MM-dd HH:mm"));
        }

        String timeStr = buffer.toString();
        if (currentcal.get(Calendar.YEAR) != createCal.get(Calendar.YEAR)) {
            timeStr = createCal.get(Calendar.YEAR) + " " + timeStr;
        }
        return timeStr;
    }

    public static String getId(Object t) {
        try {
            Field idField = t.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return idField.get(t).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getGender(WeiBoUser user) {
        Resources res = GlobalContext.getInstance().getResources();
        if (user != null) {
            if ("m".equals(user.getGender())) {
                return res.getString(R.string.msg_male);
            } else if ("f".equals(user.getGender())) {
                return res.getString(R.string.msg_female);
            } else if ("n".equals(user.getGender())) {
                return res.getString(R.string.msg_gender_unknow);
            }
        }
        return "";
    }

    public static String convCount(int count) {
        if (count < 10000) {
            return count + "";
        } else {
            Resources res = GlobalContext.getInstance().getResources();
            String result = new DecimalFormat("#.0").format(count * 1.0f / 10000) + res.getString(R.string.msg_ten_thousand);
            return result;
        }
    }

    public static String getCounter(int count) {
        Resources res = GlobalContext.getInstance().getResources();

        if (count < 10000)
            return String.valueOf(count);
        else if (count < 100 * 10000)
            return new DecimalFormat("#.0" + res.getString(R.string.msg_ten_thousand)).format(count * 1.0f / 10000);
        else
            return new DecimalFormat("#" + res.getString(R.string.msg_ten_thousand)).format(count * 1.0f / 10000);
    }

    /**
     * 显示高清头像
     *
     * @param user
     * @return
     */
    public static String getUserPhoto(WeiBoUser user) {
        if (user == null)
            return "";

        if (AppSettings.isLargePhoto()) {
            return user.getAvatar_large();
        }

        return user.getProfile_image_url();
    }

    public static void setImageVerified(ImageView imgVerified, WeiBoUser user) {
        // 2014-08-27 新增判断，VerifiedType存在为null的情况
        if (user == null || user.getVerified_type() == null) {
            imgVerified.setVisibility(View.GONE);
            return;
        }

        // 黄V
        if (user.getVerified_type() == 0) {
            imgVerified.setImageResource(R.drawable.avatar_vip);
        }
        // 200:初级达人 220:高级达人
        else if (user.getVerified_type() == 200 || user.getVerified_type() == 220) {
            imgVerified.setImageResource(R.drawable.avatar_grassroot);
        }
        // 蓝V
        else if (user.getVerified_type() > 0) {
            imgVerified.setImageResource(R.drawable.avatar_enterprise_vip);
        }
        if (user.getVerified_type() >= 0)
            imgVerified.setVisibility(View.VISIBLE);
        else
            imgVerified.setVisibility(View.GONE);
    }

    public static String getUserScreenName(WeiBoUser user) {
        if (AppSettings.isShowRemark() && !TextUtils.isEmpty(user.getRemark()))
            return user.getRemark();

        return user.getScreen_name();
    }

}
