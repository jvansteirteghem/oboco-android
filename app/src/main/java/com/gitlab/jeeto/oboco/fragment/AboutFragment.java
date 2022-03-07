package com.gitlab.jeeto.oboco.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.pm.PackageInfoCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gitlab.jeeto.oboco.R;

import java.util.ArrayList;
import java.util.List;

public class AboutFragment extends Fragment implements View.OnClickListener {
    private class LibraryDescription {
        public final String name;
        public final String description;
        public final String license;
        public final String owner;
        public final String url;

        LibraryDescription(String name, String description, String license, String owner, String url) {
            this.name = name;
            this.description = description;
            this.license = license;
            this.owner = owner;
            this.url = url;
        }
    }

    private List<LibraryDescription> mLibraryDescriptionList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLibraryDescriptionList = new ArrayList<LibraryDescription>();
        mLibraryDescriptionList.add(new LibraryDescription(
            getResources().getString(R.string.about_bubble_name),
            getResources().getString(R.string.about_bubble_description),
            getResources().getString(R.string.about_bubble_license),
            getResources().getString(R.string.about_bubble_owner),
            getResources().getString(R.string.about_bubble_url)
        ));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_about, container, false);

        getActivity().setTitle(R.string.drawer_menu_about);

        LinearLayout libsLayout = (LinearLayout) view.findViewById(R.id.about_libraries);

        ((TextView) view.findViewById(R.id.aboutVersion)).setText(getVersionString());

        for (LibraryDescription libraryDescription: mLibraryDescriptionList) {
            View cardView = inflater.inflate(R.layout.card_deps, libsLayout, false);

            ((TextView) cardView.findViewById(R.id.libraryName)).setText(libraryDescription.name);
            ((TextView) cardView.findViewById(R.id.libraryOwner)).setText(libraryDescription.owner);
            ((TextView) cardView.findViewById(R.id.libraryDescription)).setText(libraryDescription.description);
            ((TextView) cardView.findViewById(R.id.libraryLicense)).setText(libraryDescription.license);

            cardView.setTag(libraryDescription.url);
            cardView.setOnClickListener(this);
            libsLayout.addView(cardView);
        }

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

    @Override
    public void onClick(View v) {
        String link = (String) v.getTag();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }
}
