package com.gitlab.jeeto.oboco.fragment;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.pm.PackageInfoCompat;
import androidx.fragment.app.Fragment;

import com.gitlab.jeeto.oboco.R;

public class AboutFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_about, container, false);

        getActivity().setTitle(R.string.drawer_menu_about);

        ((TextView) view.findViewById(R.id.aboutVersion)).setText(getVersionString());

        return view;
    }

    private String getVersionString() {
        try {
            PackageInfo pi = getActivity()
                    .getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
            return getResources().getString(R.string.about_version) + " " + pi.versionName + " (" + PackageInfoCompat.getLongVersionCode(pi) + ")";
        }
        catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}
