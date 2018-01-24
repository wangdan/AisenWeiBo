package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangdan on 16/8/10.
 */
public class StatusHots implements Serializable {

    private static final long serialVersionUID = -6265147941637272236L;

    private StatusHotCardInfo cardlistInfo;

    private List<StatusHotCard> cards;

    public StatusHotCardInfo getCardlistInfo() {
        return cardlistInfo;
    }

    public void setCardlistInfo(StatusHotCardInfo cardlistInfo) {
        this.cardlistInfo = cardlistInfo;
    }

    public List<StatusHotCard> getCards() {
        return cards;
    }

    public void setCards(List<StatusHotCard> cards) {
        this.cards = cards;
    }

}
