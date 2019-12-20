package com.example.smartparking;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

//Select Time from Time Picker Dialogue Fragement
public class TimerDialogueFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int startHour = calendar.get(Calendar.HOUR_OF_DAY);
        int startMinute = calendar.get(Calendar.MINUTE);

        boolean is24hourformat = true;
        Activity parentActivity = getActivity();
        TimePickerDialog.OnTimeSetListener timePickListener =
                (TimePickerDialog.OnTimeSetListener) parentActivity;
        TimePickerDialog timeDialog = new TimePickerDialog(parentActivity,
                timePickListener, startHour, startMinute, is24hourformat);
                //DateFormat.is24HourFormat(parentActivity));
        return timeDialog;
    }
}