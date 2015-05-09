package org.aisen.weibo.sina.ui.fragment.basic;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.MenuBean;

import java.util.ArrayList;

/**
 * 所有的首页菜单维护
 *
 * Created by wangdan on 15/4/14.
 */
public class MenuGenerator {

    public static ArrayList<MenuBean> generateMenus() {
        ArrayList<MenuBean> menuList = new ArrayList<MenuBean>();

        // 首页
        menuList.add(generateMenu("1"));
        // 提及
        menuList.add(generateMenu("2"));
        // 评论
        menuList.add(generateMenu("3"));
        // 私信
        menuList.add(generateMenu("10"));
        // 分割线
        menuList.add(generateMenu("1000"));
        // 微博广场
//        menuList.add(generateMenu("12"));
        // 热门微博
        menuList.add(generateMenu("11"));
        // 离线阅读
//        menuList.add(generateMenu("14"));
        // 草稿
        menuList.add(generateMenu("6"));
        // 设置
        menuList.add(generateMenu("5"));

        return menuList;
    }

    public static MenuBean generateMenu(String type) {
        MenuBean menuBean = null;

        switch (Integer.parseInt(type)) {
            // 个人信息
            case 0:
                menuBean = new MenuBean(-1, R.string.draw_profile, R.string.draw_profile, "0");
                break;
            // 微博首页
            case 1:
                menuBean = new MenuBean(R.drawable.ic_view_day_grey600_24dp, R.string.draw_timeline, R.string.draw_timeline, "1");
                break;
            // 消息
            case 2:
                menuBean = new MenuBean(R.drawable.ic_drawer_at, R.string.draw_message, R.string.mention_title, "2");
                break;
            // 评论
            case 3:
                menuBean = new MenuBean(R.drawable.ic_question_answer_grey600_24dp, R.string.draw_comment, R.string.draw_comment, "3");
                break;
            // 朋友关系
            case 4:
                menuBean = new MenuBean(-1, R.string.draw_friendship, R.string.draw_friendship, "4");
                break;
            // 设置
            case 5:
                menuBean = new MenuBean(-1, R.string.draw_settings, R.string.draw_settings, "5");
                break;
            // 草稿
            case 6:
                menuBean = new MenuBean(-1, R.string.draw_draft, R.string.draw_draft, "6");
                break;
            // 收藏
            case 7:
                menuBean = new MenuBean(-1, R.string.draw_fav, R.string.draw_fav_title, "7");
                break;
            // 搜索
            case 8:
                menuBean = new MenuBean(-1, R.string.draw_search_v2, R.string.draw_search_title, "8");
                break;
            // 热门话题
            case 9:
                menuBean = new MenuBean(-1, R.string.draw_topic, R.string.draw_topic_title, "9");
                break;
            // 私信
            case 10:
                menuBean = new MenuBean(R.drawable.ic_email_grey600_24dp, R.string.draw_private_msg, R.string.draw_private_msg_title, "10");
                break;
            // 热门微博
            case 11:
                menuBean = new MenuBean(-1, R.string.draw_hot_statuses, R.string.draw_hot_statuses_title, "11");
                break;
            // 微博广场
            case 12:
                menuBean = new MenuBean(-1, R.string.draw_weibo_square, R.string.draw_weibo_square_title, "12");
                break;
            // 微博头条
            case 13:
                menuBean = new MenuBean(-1, R.string.draw_weibo_top, R.string.draw_weibo_top_title, "13");
                break;
            // 离线阅读
            case 14:
                menuBean = new MenuBean(-1, R.string.draw_weibo_offline_read, R.string.draw_weibo_offline_read, "14");
                break;
            // 分割线
            case 1000:
                menuBean = new MenuBean(-1, R.string.app_name, R.string.app_name, "1000");
                break;
        }

        return menuBean;
    }

}
