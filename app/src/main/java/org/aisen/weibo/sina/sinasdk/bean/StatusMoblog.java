package org.aisen.weibo.sina.sinasdk.bean;

import java.util.List;

/**
 * Created by wangdan on 16/8/10.
 */
public class StatusMoblog extends StatusContent {

    private static final long serialVersionUID = -2497135304462739091L;

    private List<HotPicLarge> pics;

    public List<HotPicLarge> getPics() {
        return pics;
    }

    public void setPics(List<HotPicLarge> pics) {
        this.pics = pics;
    }

    @Override
    public PicUrls[] getPic_urls() {
        if (pics != null && pics.size() > 0) {
            PicUrls[] picUrls = new PicUrls[pics.size()];

            for (int i = 0; i < pics.size(); i++) {
                PicUrls url = new PicUrls();
                url.setThumbnail_pic(pics.get(i).getLarge().getUrl().replace("large", "thumbnail").replace("large", "bmiddle"));
                picUrls[i] = url;
            }

            return picUrls;
        }

        return null;
    }
}
