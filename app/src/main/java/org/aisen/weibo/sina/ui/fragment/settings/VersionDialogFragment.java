package org.aisen.weibo.sina.ui.fragment.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.aisen.weibo.sina.R;

import it.gmariotti.changelibs.library.view.ChangeLogListView;

public class VersionDialogFragment extends DialogFragment {

	public static void launch(Activity context) {
		Fragment fragment = context.getFragmentManager().findFragmentByTag("DialogFragment");
    	if (fragment != null) {
    		context.getFragmentManager().beginTransaction().remove(fragment).commit();
    	}
    	
    	VersionDialogFragment dialogFragment = new VersionDialogFragment();
    	dialogFragment.show(context.getFragmentManager(), "DialogFragment");
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ChangeLogListView chgList = (ChangeLogListView) View.inflate(getActivity(), R.layout.demo_changelog_fragment_dialogstandard, null);
		
		return new AlertDialogWrapper.Builder(getActivity())
				        .setTitle(R.string.settings_version_detail)
				        .setView(chgList)
				        .setPositiveButton("OK",
				                new DialogInterface.OnClickListener() {
				                    public void onClick(DialogInterface dialog, int whichButton) {
				                        dialog.dismiss();
				                        
				                        getActivity().getFragmentManager().beginTransaction().remove(VersionDialogFragment.this)
                    										.commit();
				                    }
				                }
				        )
				        .create();
	}
	
}
