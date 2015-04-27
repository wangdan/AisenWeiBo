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
package it.gmariotti.changelibs.library.parser;

import android.content.Context;

import it.gmariotti.changelibs.library.internal.ChangeLog;

/**
 * Abstract BaseParser for future implementations.
 *
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public abstract class BaseParser {

    /**
     *  Context
     */
    protected Context mContext;

    /**
     * Use a bulleted List
     */
    protected boolean bulletedList;

    //--------------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------------

    /**
     * Create a new instance for a context.
     *
     * @param context  current Context
     */
    public BaseParser(Context context){
        this.mContext=context;
    }

    //--------------------------------------------------------------------------------

    /**
     * Read and Parse the changeLog file and return a new {@link it.gmariotti.changelibs.library.internal.ChangeLog}.
     *
     * @return The content of changelog file
     *
     * @throws Exception
     */
    public abstract ChangeLog readChangeLogFile() throws Exception;

}
