package net.smellydog.robotcontroller;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class EditBrightnessDialog extends DialogFragment implements OnEditorActionListener {
	private static final String TAG = "EditBrightnessDialog";

    public interface EditBrightnessDialogListener {
        void onFinishEditDialog(String inputText);
    }

	
    private EditText mEditBrigtness;

    public EditBrightnessDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_brightness, container);
        mEditBrigtness = (EditText) view.findViewById(R.id.edit_brightness);
        getDialog().setTitle("Set LCD Brightness");

        // Show soft keyboard automatically
        mEditBrigtness.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditBrigtness.setOnEditorActionListener(this);

        return view;
    }

	public int getBrigtness() {
		return Integer.parseInt(mEditBrigtness.getText().toString());
	}

	public void setBrigtness(int brigtness) {
		this.mEditBrigtness.setText(Integer.toString(brigtness));
	}
	
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		Log.i(TAG, "onEditorAction");
        if (EditorInfo.IME_ACTION_DONE == actionId) {
    		Log.i(TAG, "EditorInfo.IME_ACTION_DONE == actionId");
            // Return input text to activity
        	EditBrightnessDialogListener activity = (EditBrightnessDialogListener) getActivity();
        	int newBrightness = Integer.parseInt(mEditBrigtness.getText().toString());
        	if(newBrightness > 255) {
        		newBrightness = 255;
        	}
        	String brightnessInHex = Integer.toHexString(newBrightness);
            activity.onFinishEditDialog(brightnessInHex);
            this.dismiss();
            return true;
        }
        return false;
    }

}
