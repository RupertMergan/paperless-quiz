package com.paperlessquiz;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.paperlessquiz.R;


/**
 * This Fragment represents a spinner that allows you to scroll up and down through a list of items
 * that are provided by the calling activity via the interface HasSpinner. This interface also allows you to change things in the activity based on spinner changes
 */
public class FragSpinner extends Fragment {

    private int position = 1, oldPosition = 1;
    //private int id;
    TextView tvPrimaryField, tvSecondaryField;
    Button btnDown, btnUp;
    HasSpinner callingActivity;

    public FragSpinner() {
        // Required empty public constructor
    }

    public interface HasSpinner {
        //Implement stuff to do in the activity when this spinner changes
        public void onSpinnerChange(int oldPos, int newPos);

        //Get the values to set in the first and second field
        public String getValueToSetForPrimaryField(int newPos);

        public String getValueToSetForSecondaryField(int newPos);

        //Get the size of the Array of the values to use for priSpinnerPos
        public int getSizeOfSpinnerArray();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_spinner, container, false);
        tvPrimaryField = v.findViewById(R.id.tvPrimaryField);
        tvSecondaryField = v.findViewById(R.id.tvSecondaryField);
        btnDown = v.findViewById(R.id.btnDown);
        btnUp = v.findViewById(R.id.btnUp);
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveDown();
            }
        });
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveUp();
            }
        });
        positionChanged();
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callingActivity = (HasSpinner) context;
    }

    public void positionChanged() {
        //Cover the case where we call this before the fragment is properly initialized
        if (callingActivity != null) {
            tvPrimaryField.setText(callingActivity.getValueToSetForPrimaryField(position));
            tvSecondaryField.setText(callingActivity.getValueToSetForSecondaryField(position));
            callingActivity.onSpinnerChange(oldPosition, position);
        }
    }

    public void moveToFirstPos() {

        if (callingActivity != null) {
            oldPosition = position;
            position = 1;
            tvPrimaryField.setText(callingActivity.getValueToSetForPrimaryField(position));
            tvSecondaryField.setText(callingActivity.getValueToSetForSecondaryField(position));
        }
    }

    public void moveUp() {
        oldPosition = position;
        if (position == callingActivity.getSizeOfSpinnerArray()) {
            position = 1;
        } else {
            position++;
        }
        positionChanged();
    }

    public void moveDown() {
        oldPosition = position;
        if (position == 1) {
            position = callingActivity.getSizeOfSpinnerArray();
        } else {
            position--;
        }
        positionChanged();
    }

    public int getPosition() {
        return position;
    }

    public int getOldPosition() {
        return oldPosition;
    }

    public void changeSpinnerColor(int colorID) {
        tvPrimaryField.setTextColor(colorID);
        tvSecondaryField.setTextColor(colorID);
    }
}
