package com.fluffr.app.fluffr;


import android.content.Context;
import android.util.Log;

import java.util.Locale;
import com.google.i18n.phonenumbers.*;


/**
 * Created by Patrick on 12/17/14.
 */
public class PhoneNumberFormatter{

    private static final String TAG = "PhoneNumberFormatter";

    public static String getFormattedNumber(String number) {

        // converts string phone number into E164 formatted international number

        Log.d(TAG,"Input Number: " + number);

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        Locale currentLocale = FluffrApplication.getContext().getResources().getConfiguration().locale;
        Log.d(TAG,"Current Locale: " + currentLocale.getCountry());


        String formattedNumber;
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();

        try {
            phoneNumber = phoneNumberUtil.parse(number, currentLocale.getCountry());
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        if (phoneNumber != null) {
            formattedNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            Log.d(TAG,"Output Number: " + formattedNumber);
            return formattedNumber;
        } else {
            Log.e(TAG,"Formatting Error!");
        }



        return null;
    }
}
