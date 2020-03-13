package org.odk.share.views.ui.settings;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.odk.share.R;

public class SetHotspotPasswordDialog extends DialogFragment {

    private SharedPreferences prefs;
    private View dialogView;
    private TextInputLayout tlPassword;
    private TextInputEditText edtpass;

    public static SetHotspotPasswordDialog newInstance() {
        return new SetHotspotPasswordDialog();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        LayoutInflater factory = LayoutInflater.from(context);
        dialogView = factory.inflate(R.layout.dialog_password_til, null);

        tlPassword = dialogView.findViewById(R.id.et_password_layout);
        tlPassword.getEditText().setText(prefs.getString(PreferenceKeys.KEY_HOTSPOT_PASSWORD, getString(R.string.default_hotspot_password)));
        edtpass = (TextInputEditText) dialogView.findViewById(R.id.edtpass);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        setRetainInstance(true);

        AlertDialog alertDialog = (AlertDialog) getDialog();

        if (alertDialog == null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(getString(R.string.title_hotspot_password));
            builder.setView(dialogView);
            builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {

                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                String password = tlPassword.getEditText().getText().toString();
                prefs.edit().putString(PreferenceKeys.KEY_HOTSPOT_PASSWORD, password).apply();
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

            builder.setCancelable(false);
            alertDialog = builder.create();

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    edtpass.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (edtpass.getText().toString().length() >= 8) {
                                ((AlertDialog) dialog) .getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            } else {
                                ((AlertDialog) dialog) .getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                }
            });
            alertDialog.setCancelable(true);
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        return alertDialog;
    }

    @Override
    public void onDestroyView() {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
            dialog.dismiss();
        }
        super.onDestroyView();
    }
}
