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
package it.gmariotti.changelibs.library;

import org.aisen.weibo.sina.R;

/**
 * Constants used by library
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class Constants {

    /**
     *  Resource id for changelog.xml file.
     *
     *  You shouldn't modify this value.
     *  You can use changeLogResourceId attribute in ChangeLogListView
     **/
    public static final int mChangeLogFileResourceId= R.raw.changelog;

    /**
     *  Layout resource id for changelog item rows.
     *
     *  You shouldn't modify this value.
     *  You can use rowLayoutId attribute in ChangeLogListView
     **/
    public static final int mRowLayoutId= R.layout.changelogrow_layout;

    /**
     * Layout resource id for changelog header rows.
     *
     *  You shouldn't modify this value.
     *  You can use rowHeaderLayoutId attribute in ChangeLogListView
     **/
    public static final int mRowHeaderLayoutId= R.layout.changelogrowheader_layout;


    /**
     *  String resource id for text Version in header row.
     *
     * You shouldn't modify this value.
     *  You can use changelog_header_version in strings.xml
     */
    public static final int mStringVersionHeader= R.string.changelog_header_version;
}
