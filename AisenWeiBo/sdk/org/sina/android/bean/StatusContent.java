package org.sina.android.bean;

import java.io.Serializable;
import java.util.Date;

import com.m.support.sqlite.annotation.Id;

public class StatusContent implements Serializable {

    private static final long serialVersionUID = 4658890626870999594L;

    /**
     * 创建时间
     */
    private String created_at;

    /**
     * 创建地点坐标
     */
    // {"type":"Point","coordinates":[22.5423681,113.9522767]}
    private Geo geo;

    /**
     * 微博ID
     */
    @Id(column = "id")
    private String id;

    /**
     * 微博信息内容
     */
    private String text;

    /**
     * 微博来源
     */
    private String source;

    /**
     * 是否已收藏
     */
    private Boolean favorited;

    /**
     * 是否被截断
     */
    private Boolean truncated;

    /**
     * 回复ID
     */
    private String in_reply_to_status_id;

    /**
     * 回复人UID
     */
    private String in_reply_to_user_id;

    /**
     * 回复人昵称
     */
    private String in_reply_to_screen_name;

    /**
     * 缩略图
     */
    private String thumbnail_pic;

    /**
     * 中型图片
     */
    private String bmiddle_pic;

    /**
     * 原始图片
     */
    private String original_pic;

    /**
     * 图片配图，多图时，返回多图链接
     */
    private PicUrls[] pic_urls;

    /**
     * 作者信息
     */
    private WeiBoUser user;

    /**
     * 转发的博文，内容为status，如果不是转发，则没有此字段
     */
    private StatusContent retweeted_status;

    /**
     * 转发的博文，内容为status，如果不是转发，则没有此字段
     */
    private String reposts_count;

    /**
     * 转发的博文，内容为status，如果不是转发，则没有此字段
     */
    private String comments_count;

    /**
     * 每次拉取的微博，首条都记录本组数据的拉取时间
     */
    private Date group_get_time;

    /**
     * 微博的可见性及指定可见分组信息。该object中type取值，0：普通微博，1：私密微博，3：指定分组微博，4：密友微博；
     * list_id为分组的组号
     */
    private Visible visible;

    /**
     * 表态数
     */
    private String attitudes_count;
    
    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getFavorited() {
        return favorited;
    }

    public void setFavorited(Boolean favorited) {
        this.favorited = favorited;
    }

    public Boolean getTruncated() {
        return truncated;
    }

    public void setTruncated(Boolean truncated) {
        this.truncated = truncated;
    }

    public String getIn_reply_to_status_id() {
        return in_reply_to_status_id;
    }

    public void setIn_reply_to_status_id(String in_reply_to_status_id) {
        this.in_reply_to_status_id = in_reply_to_status_id;
    }

    public String getIn_reply_to_user_id() {
        return in_reply_to_user_id;
    }

    public void setIn_reply_to_user_id(String in_reply_to_user_id) {
        this.in_reply_to_user_id = in_reply_to_user_id;
    }

    public String getIn_reply_to_screen_name() {
        return in_reply_to_screen_name;
    }

    public void setIn_reply_to_screen_name(String in_reply_to_screen_name) {
        this.in_reply_to_screen_name = in_reply_to_screen_name;
    }

    public String getThumbnail_pic() {
        return thumbnail_pic;
    }

    public void setThumbnail_pic(String thumbnail_pic) {
        this.thumbnail_pic = thumbnail_pic;
    }

    public String getBmiddle_pic() {
        return bmiddle_pic;
    }

    public void setBmiddle_pic(String bmiddle_pic) {
        this.bmiddle_pic = bmiddle_pic;
    }

    public String getOriginal_pic() {
        return original_pic;
    }

    public void setOriginal_pic(String original_pic) {
        this.original_pic = original_pic;
    }

    public PicUrls[] getPic_urls() {
        return pic_urls;
    }

    public void setPic_urls(PicUrls[] pic_urls) {
        this.pic_urls = pic_urls;
    }

    public WeiBoUser getUser() {
        return user;
    }

    public void setUser(WeiBoUser user) {
        this.user = user;
    }

    public StatusContent getRetweeted_status() {
        return retweeted_status;
    }

    public void setRetweeted_status(StatusContent retweeted_status) {
        this.retweeted_status = retweeted_status;
    }

    public String getReposts_count() {
        return reposts_count;
    }

    public void setReposts_count(String reposts_count) {
        this.reposts_count = reposts_count;
    }

    public String getComments_count() {
        return comments_count;
    }

    public void setComments_count(String comments_count) {
        this.comments_count = comments_count;
    }

    public Date getGroup_get_time() {
        return group_get_time;
    }

    public void setGroup_get_time(Date group_get_time) {
        this.group_get_time = group_get_time;
    }

    public String getAttitudes_count() {
        return attitudes_count;
    }

    public void setAttitudes_count(String attitudes_count) {
        this.attitudes_count = attitudes_count;
    }

    public Visible getVisible() {
        return visible;
    }

    public void setVisible(Visible visible) {
        this.visible = visible;
    }

}
