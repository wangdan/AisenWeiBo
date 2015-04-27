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
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.aisen.weibo.sina.R;

import java.util.List;

import it.gmariotti.changelibs.library.Constants;

/**
 * Adapter for ChangeLog model
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class ChangeLogAdapter extends ArrayAdapter<ChangeLogRow> {

    protected static final int TYPE_ROW = 0;
    protected static final int TYPE_HEADER = 1;

    private int mRowLayoutId = Constants.mRowLayoutId;
    private int mRowHeaderLayoutId = Constants.mRowHeaderLayoutId;
    private int mStringVersionHeader = Constants.mStringVersionHeader;

    private final Context mContext;

    //private boolean mChangeLogRowClickable = false;

    public ChangeLogAdapter(Context context,
                            List<ChangeLogRow> items) {
        super(context, 0, items);
        mContext=context;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //It uses ViewHolder Pattern.

        ChangeLogRow item = getItem(position);
        View view = convertView;
        int viewType = this.getItemViewType(position);
       // LayoutInflater mInflater = mContext.getLayoutInflater();
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        switch (viewType) {
            case TYPE_HEADER:
                ViewHolderHeader viewHolderHeader = null;

                if (view != null) {
                    Object obj = view.getTag();
                    if (obj instanceof ViewHolderHeader) {
                        viewHolderHeader = (ViewHolderHeader) obj;
                    } else {
                        viewHolderHeader = null;
                    }
                }

                if (view == null || viewHolderHeader == null) {
                    int layout = mRowHeaderLayoutId;
                    view = mInflater.inflate(layout, parent,false);

                    //VersionName text
                    TextView textHeader = (TextView) view.findViewById(R.id.chg_headerVersion);
                    //ChangeData text
                    TextView textDate =  (TextView) view.findViewById(R.id.chg_headerDate);
                    viewHolderHeader= new ViewHolderHeader(textHeader,textDate);

                    view.setTag(viewHolderHeader);
                }

                if (item != null && viewHolderHeader != null) {
                    if (viewHolderHeader.version != null){
                        StringBuilder sb=new StringBuilder();
                        //String resource for Version
                        String versionHeaderString=getContext().getString(mStringVersionHeader);
                        if (versionHeaderString!=null)
                            sb.append(versionHeaderString);
                        //VersionName text
                        sb.append(item.versionName);

                        viewHolderHeader.version.setText(sb.toString());
                    }

                    //ChangeData text
                    if (viewHolderHeader.date !=null){
                        //Check if exists

                        if (item.changeDate!=null){
                            viewHolderHeader.date.setText(item.changeDate);
                            viewHolderHeader.date.setVisibility(View.VISIBLE);
                        }else{
                            //If item hasn't changedata, hide TextView
                            viewHolderHeader.date.setText("");
                            viewHolderHeader.date.setVisibility(View.GONE);
                        }
                    }
                }
                break;

            case TYPE_ROW:
                ViewHolderRow viewHolder = null;

                if (view != null) {
                    Object obj = view.getTag();
                    if (obj instanceof ViewHolderRow) {
                        viewHolder = (ViewHolderRow) obj;
                    } else {
                        viewHolder = null;
                    }
                }

                if (view == null || viewHolder == null) {
                    int layout = mRowLayoutId;
                    view = mInflater.inflate(layout, parent, false);

                    TextView textText = (TextView) view.findViewById(R.id.chg_text);
                    TextView bulletText = (TextView) view.findViewById(R.id.chg_textbullet);
                    viewHolder = new ViewHolderRow(textText,bulletText);
                    view.setTag(viewHolder);

                }


                if (item != null && viewHolder != null) {
                    if (viewHolder.text != null){
                        viewHolder.text.setText(Html.fromHtml(item.getChangeText(mContext)));
                        viewHolder.text.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                    if (viewHolder.bulletText!=null){
                        if (item.isBulletedList()){
                            viewHolder.bulletText.setVisibility(View.VISIBLE);
                        }else{
                            viewHolder.bulletText.setVisibility(View.GONE);
                        }
                    }
                }

                break;
            default:
                //Throw exception, unknown data type
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isHeader())
            return TYPE_HEADER;
        return TYPE_ROW;
    }

    //-----------------------------------------------------------------------------------
    // View Holder
    //-----------------------------------------------------------------------------------

    static class ViewHolderHeader {
        TextView version;
        TextView date;

        public ViewHolderHeader(TextView version, TextView date) {
            this.version = version;
            this.date = date;
        }
    }

    static class ViewHolderRow {
        TextView text;
        TextView bulletText;

        public ViewHolderRow( TextView text,TextView bulletText) {
            this.text = text;
            this.bulletText=bulletText;
        }
    }

    //-----------------------------------------------------------------------------------
    // Getter and Setter
    //-----------------------------------------------------------------------------------


    public int getmRowLayoutId() {
        return mRowLayoutId;
    }

    public void setmRowLayoutId(int mRowLayoutId) {
        this.mRowLayoutId = mRowLayoutId;
    }

    public int getmRowHeaderLayoutId() {
        return mRowHeaderLayoutId;
    }

    public void setmRowHeaderLayoutId(int mRowHeaderLayoutId) {
        this.mRowHeaderLayoutId = mRowHeaderLayoutId;
    }

    public int getmStringVersionHeader() {
        return mStringVersionHeader;
    }

    public void setmStringVersionHeader(int mStringVersionHeader) {
        this.mStringVersionHeader = mStringVersionHeader;
    }
}
