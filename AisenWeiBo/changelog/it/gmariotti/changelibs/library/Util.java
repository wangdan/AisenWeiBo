/*
 *  ******************************************************************************
 *     Copyright (c) 2013 Gabriele Mariotti.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *    *****************************************************************************
 */

package it.gmariotti.changelibs.library;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class Util {

    public static boolean isConnected(Context context) {

        if (context==null) return false;

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager!=null){
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            if (activeNetworkInfo == null)
                return false;

            return ((activeNetworkInfo != null) && (activeNetworkInfo
                    .isConnectedOrConnecting()));
        }

        return false;
    }
}
