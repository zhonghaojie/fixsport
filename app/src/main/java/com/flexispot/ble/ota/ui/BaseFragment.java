package com.flexispot.ble.ota.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.widget.Toast;


/**
 * Created by Administrator on 2017/2/24.
 */
public class BaseFragment extends Fragment {

    protected Toast toast;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
    }

    public void toastMsg(CharSequence s) {

        if (this.toast != null) {
            this.toast.setView(this.toast.getView());
            this.toast.setDuration(Toast.LENGTH_SHORT);
            this.toast.setText(s);
            this.toast.show();
        }
    }
}
