/*******************************************************************************
 * Copyright (c) 2013 Gabriele Mariotti.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.gmariotti.changelibs.library.internal;

import android.content.Context;

import org.aisen.weibo.sina.R;

/**
 * ChangeLogRow model
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class ChangeLogRow {

    /**
     * Default type
     */
    public static final int DEFAULT = 0;

    /**
     * BugFix type
     */
    public static final int BUGFIX = 1;

    /**
     * Improvement type
     */
    public static final int IMPROVEMENT = 2;

    //-------------------------------------------------------------------------------------------------------------------

    /**
     * Flag to indicate a header row
     */
    protected boolean header;

    /**
     * This corresponds to the android:versionName attribute in your manifest file. It is a required data
     */
    protected String versionName;

    /**
     * This corresponds to the android:versionCode attribute in your manifest file. It is an optional data.
     */
    protected int versionCode;

    /**
     * Change data. It is optional
     */
    protected String changeDate;

    //-------------------------------------------------------------------------------------------------------------------

    /**
     * Use a bulleted list. It overrides general flag. It is optional
     */
    private boolean bulletedList;

    /**
     * Special marker in change text. It is optional
     * @deprecated use custom tags as changelogbug or changelogimprovement
     */
    private String changeTextTitle;

    /**
     * Contains the actual text that will be displayed in your change log. It is required
     */
    private String changeText;

    /**
     * The type of change: bug, improvement, default.
     */
    private int type;

    //-------------------------------------------------------------------------------------------------------------------

    /**
     * Replace special tags [b] [i]
     *
     * @param changeLogText
     */
    public void parseChangeText(String changeLogText) {
        if (changeLogText != null) {
            changeLogText = changeLogText.replaceAll("\\[", "<").replaceAll("\\]", ">");
        }
        setChangeText(changeLogText);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("header=" + header);
        sb.append(",");
        sb.append("versionName=" + versionName);
        sb.append(",");
        sb.append("versionCode=" + versionCode);
        sb.append(",");
        sb.append("bulletedList=" + bulletedList);
        sb.append(",");
        sb.append("changeText=" + changeText);
        //sb.append(",");
        //sb.append("changeTextTitle="+changeTextTitle);
        return sb.toString();
    }

    //-------------------------------------------------------------------------------------------------------------------


    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public boolean isBulletedList() {
        return bulletedList;
    }

    public void setBulletedList(boolean bulletedList) {
        this.bulletedList = bulletedList;
    }

    public String getChangeText() {
        return changeText;
    }

    public String getChangeText(Context context) {
        if (context == null)
            return getChangeText();

        String prefix = "";
        switch (type) {
            case BUGFIX:
                prefix = context.getResources().getString(R.string.changelog_row_prefix_bug);
                prefix = prefix.replaceAll("\\[", "<").replaceAll("\\]", ">");
                break;
            case IMPROVEMENT:
                prefix = context.getResources().getString(R.string.changelog_row_prefix_improvement);
                prefix = prefix.replaceAll("\\[", "<").replaceAll("\\]", ">");
                break;
        }
        return prefix + " " + changeText;
    }

    public void setChangeText(String changeText) {
        this.changeText = changeText;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    /**
     * @deprecated use custom tags as changelogbug or changelogimprovement
     */
    public String getChangeTextTitle() {
        return changeTextTitle;
    }

    /**
     *  @deprecated use custom tags as changelogbug or changelogimprovement
     */
    public void setChangeTextTitle(String changeTextTitle) {
        this.changeTextTitle = changeTextTitle;
    }

    public String getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(String changeDate) {
        this.changeDate = changeDate;
    }

    public void setType(int type) {
        this.type = type;
    }

}
