package com.fluffr.app.fluffr;


import java.util.Locale;
import com.google.i18n.phonenumbers.*;


/**
 * Created by Patrick on 12/17/14.
 */
public class PhoneNumberFormatter{

    public String getFormattedNumber(String number) {

        // converts string phone number into E164 formatted international number

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();


        String formattedNumber;
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();

        try {
            phoneNumber = phoneNumberUtil.parse(number, Locale.US.getISO3Country());
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        if (phoneNumber != null) {
            formattedNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            return formattedNumber;
        }



        return null;
    }
}
