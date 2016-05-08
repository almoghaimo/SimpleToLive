package com.h.almog.simpletolive.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.h.almog.simpletolive.R;

/**
 * Created by Almog on 13/04/2016.
 */
public class ChargeConnection extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case Intent.ACTION_POWER_CONNECTED:
                Toast.makeText(context, R.string.charge_connected, Toast.LENGTH_SHORT).show();
                break;
            case Intent.ACTION_POWER_DISCONNECTED:
                Toast.makeText(context, R.string.charge_disconnected, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
