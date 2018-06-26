package com.gpstracker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

interface SmsListener {
    void onSmsReceived(String sender, String text);
}

public class SmsBroadcastReceiver extends BroadcastReceiver {
    private SmsListener mSmsListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            String smsSender = "";
            String smsBody = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    smsSender = smsMessage.getOriginatingAddress();
                    smsBody += smsMessage.getMessageBody();
                }
            } else {
                Bundle smsBundle = intent.getExtras();
                if (smsBundle != null) {
                    Object[] pdus = (Object[]) smsBundle.get("pdus");
                    if (pdus == null) {
                        Log.e("SmsBroadcastReceiver", "SmsBundle had no pdus key");
                        return;
                    }

                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < messages.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        smsBody += messages[i].getMessageBody();
                    }
                    smsSender = messages[0].getOriginatingAddress();
                }
            }

            if (mSmsListener != null) {
                mSmsListener.onSmsReceived(smsSender, smsBody);
            }
        }
    }

    public void setSmsListener(SmsListener listener) {
        mSmsListener = listener;
    }

    public String getNumber() {
        TelephonyManager tMgr = (TelephonyManager) MainActivity.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("SmsBroadcastReceiver", "Not enough permissions");
            Toast.makeText(MainActivity.getContext(), "---> Not enough permissions", Toast.LENGTH_LONG).show();
            return "";
        }
        String mPhoneNumber = tMgr.getLine1Number();
        Toast.makeText(MainActivity.getContext(), "---> phoneNumber is" + mPhoneNumber, Toast.LENGTH_LONG).show();
        Log.e("SmsBroadcastReceiver", "mPhoneNumber is " + mPhoneNumber);
        return mPhoneNumber;
    }

    public void sendMessage(String number, String body) {
        SmsManager.getDefault().sendTextMessage(number, null, body, null, null);
    }
}
