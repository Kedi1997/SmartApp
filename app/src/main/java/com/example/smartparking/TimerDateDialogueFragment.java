package com.example.smartparking;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimerDateDialogueFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int startYear = calendar.get(Calendar.YEAR);
        int startMonth = calendar.get(Calendar.MONTH);
        int startDay = calendar.get(Calendar.DATE);

        Activity parentActivity = getActivity();

        DatePickerDialog.OnDateSetListener dtsetListener=
                (DatePickerDialog.OnDateSetListener) parentActivity;

        DatePickerDialog dateDialog = new DatePickerDialog(parentActivity,
                dtsetListener, startYear,startMonth,startDay);

        return dateDialog;
    }
}