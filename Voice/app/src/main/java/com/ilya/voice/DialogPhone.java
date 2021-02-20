package com.ilya.voice;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

public class DialogPhone extends DialogFragment {

    //реализую диалоговое окно
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        try {
            builder.setMessage(getString(R.string.dialog_phone_message))
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // cancel
                        }
                    })
                    .setPositiveButton(R.string.turn_on, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //включить громкую связь и повысить звук мультимедиаф
                            try {
                                Thread.sleep(500);
                                AudioManager audioManager = (AudioManager)getActivity().
                                        getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                audioManager.setMode(AudioManager.MODE_IN_CALL);
                                audioManager.setSpeakerphoneOn(true);
                                audioManager = (AudioManager)getActivity().getApplicationContext()
                                        .getSystemService(Context.AUDIO_SERVICE);
                                MainActivity.VALUE_SOUND = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0);
                                Toast.makeText(getActivity().getApplicationContext(),
                                        getString(R.string.toast_phone_sound),
                                        Toast.LENGTH_LONG).show();
                            } catch (InterruptedException e) {}
                        }
                    });
        }catch (Exception e){}
        return builder.create();
    }
}
