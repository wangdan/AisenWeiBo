package org.aisen.weibo.sina.support.utils;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;

/**
 * Created by wangdan on 15/4/30.
 */
public class ThemeUtils {

    public static int[][] themeArr = {
            { R.style.AppTheme_Red, R.style.AppTheme_Main_Red, R.style.AppTheme_Profile_Red, R.style.AppTheme_Search_Red },
            { R.style.AppTheme_Pink, R.style.AppTheme_Main_Pink, R.style.AppTheme_Profile_Pink, R.style.AppTheme_Search_Pink },
            { R.style.AppTheme_Purple, R.style.AppTheme_Main_Purple, R.style.AppTheme_Profile_Purple, R.style.AppTheme_Search_Purple },
            { R.style.AppTheme_DeepPurple, R.style.AppTheme_Main_DeepPurple, R.style.AppTheme_Profile_DeepPurple, R.style.AppTheme_Search_DeepPurple },
            { R.style.AppTheme_Indigo, R.style.AppTheme_Main_Indigo, R.style.AppTheme_Profile_Indigo, R.style.AppTheme_Search_Indigo },
            { R.style.AppTheme_Blue, R.style.AppTheme_Main_Blue, R.style.AppTheme_Profile_Blue, R.style.AppTheme_Search_Blue },
            { R.style.AppTheme_LightBlue, R.style.AppTheme_Main_LightBlue, R.style.AppTheme_Profile_LightBlue, R.style.AppTheme_Search_LightBlue },
            { R.style.AppTheme_Cyan, R.style.AppTheme_Main_Cyan, R.style.AppTheme_Profile_Cyan, R.style.AppTheme_Search_Cyan },
            { R.style.AppTheme_Teal, R.style.AppTheme_Main_Teal, R.style.AppTheme_Profile_Teal, R.style.AppTheme_Search_Teal },
            { R.style.AppTheme_Green, R.style.AppTheme_Main_Green, R.style.AppTheme_Profile_Green, R.style.AppTheme_Search_Green },
            { R.style.AppTheme_LightGreen, R.style.AppTheme_Main_LightGreen, R.style.AppTheme_Profile_LightGreen, R.style.AppTheme_Search_LightGreen },
            { R.style.AppTheme_Lime, R.style.AppTheme_Main_Lime, R.style.AppTheme_Profile_Lime, R.style.AppTheme_Search_Lime },
            { R.style.AppTheme_Yellow, R.style.AppTheme_Main_Yellow, R.style.AppTheme_Profile_Yellow, R.style.AppTheme_Search_Yellow },
            { R.style.AppTheme_Amber, R.style.AppTheme_Main_Amber, R.style.AppTheme_Profile_Amber, R.style.AppTheme_Search_Amber },
            { R.style.AppTheme_Orange, R.style.AppTheme_Main_Orange, R.style.AppTheme_Profile_Orange, R.style.AppTheme_Search_Orange },
            { R.style.AppTheme_DeepOrange, R.style.AppTheme_Main_DeepOrange, R.style.AppTheme_Profile_DeepOrange, R.style.AppTheme_Search_DeepOrange },
            { R.style.AppTheme_Brown, R.style.AppTheme_Main_Brown, R.style.AppTheme_Profile_Brown, R.style.AppTheme_Search_Brown },
            { R.style.AppTheme_Grey, R.style.AppTheme_Main_Grey, R.style.AppTheme_Profile_Grey, R.style.AppTheme_Search_Grey },
            { R.style.AppTheme_BlueGrey, R.style.AppTheme_Main_BlueGrey, R.style.AppTheme_Profile_BlueGrey, R.style.AppTheme_Search_BlueGrey }
    };

    public static int[][] themeColorArr = {
            { R.color.md_red_500, R.color.md_red_700 },
            { R.color.md_pink_500, R.color.md_pink_700 },
            { R.color.md_purple_500, R.color.md_purple_700 },
            { R.color.md_deep_purple_500, R.color.md_deep_purple_700 },
            { R.color.md_indigo_500, R.color.md_indigo_700 },
            { R.color.md_blue_500, R.color.md_blue_700 },
            { R.color.md_light_blue_500, R.color.md_light_blue_700 },
            { R.color.md_cyan_500, R.color.md_cyan_700 },
            { R.color.md_teal_500, R.color.md_teal_700 },
            { R.color.md_green_500, R.color.md_green_700 },
            { R.color.md_light_green_500, R.color.md_light_green_700 },
            { R.color.md_lime_500, R.color.md_lime_700 },
            { R.color.md_yellow_500, R.color.md_yellow_700 },
            { R.color.md_amber_500, R.color.md_amber_700 },
            { R.color.md_orange_500, R.color.md_orange_700 },
            { R.color.md_deep_orange_500, R.color.md_deep_orange_700 },
            { R.color.md_brown_500, R.color.md_brown_700 },
            { R.color.md_grey_500, R.color.md_grey_700 },
            { R.color.md_blue_grey_500, R.color.md_blue_grey_700 }
    };

        public static int getThemeColor() {
                return GlobalContext.getInstance().getResources().getColor(themeColorArr[AppSettings.getThemeColor()][0]);
        }
}
