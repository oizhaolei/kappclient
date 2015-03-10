package org.fdroid.fdroid.views.swap;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ruptech.k_app.R;

import org.fdroid.fdroid.ProgressListener;
import org.fdroid.fdroid.UpdateService;
import org.fdroid.fdroid.data.NewRepoConfig;
import org.fdroid.fdroid.data.Repo;

public class ConfirmReceiveSwapFragment extends Fragment implements ProgressListener {

    private NewRepoConfig newRepoConfig;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.swap_confirm_receive, container, false);

        view.findViewById(R.id.no_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        view.findViewById(R.id.yes_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        return view;
    }

    private void finish() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    public void onResume() {
        super.onResume();
        newRepoConfig = new NewRepoConfig(getActivity(), getActivity().getIntent());
        if (newRepoConfig.isValidRepo()) {
            ((TextView) getView().findViewById(R.id.text_description)).setText(
                    getString(R.string.swap_confirm_connect, newRepoConfig.getHost())
            );
        } else {
            // TODO: Show error message on screen (not in popup).
        }
    }

    private void confirm() {
        UpdateService.updateRepoNow(Repo.address, getActivity()).setListener(this);
    }


    @Override
    public void onProgress(Event event) {
        // TODO: Show progress, but we can worry about that later.
        // Might be nice to have it nicely embedded in the UI, rather than as
        // an additional dialog. E.g. White text on blue, letting the user
        // know what we are up to.

        if (event.type.equals(UpdateService.EVENT_COMPLETE_AND_SAME) ||
                event.type.equals(UpdateService.EVENT_COMPLETE_WITH_CHANGES)) {
        } else if (event.type.equals(UpdateService.EVENT_ERROR)) {
            // TODO: Show message on this screen (with a big "okay" button that goes back to K-App activity)
            // rather than finishing directly.
            finish();
        }
    }
}
