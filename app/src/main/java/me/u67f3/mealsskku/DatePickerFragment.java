package me.u67f3.mealsskku;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import java.util.Date;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    interface DateSetListener {
        void onDateSet(int year, int month, int day);
    }

    DateSetListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DateSetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year, month, dayOfMonth;
        if (Build.VERSION.SDK_INT >= 24) {
            /*
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            */

            Date date = new Date();
            year = date.getYear();
            month = date.getMonth();
            dayOfMonth = date.getDay();
        } else {
            Date date = new Date();
            year = date.getYear();
            month = date.getMonth();
            dayOfMonth = date.getDate();
        }

        Log.d("DATE", String.valueOf(dayOfMonth));

        // Create a new instance of TimePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, 1900 + year, month, dayOfMonth);
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        listener.onDateSet(year, month, dayOfMonth);
    }
}