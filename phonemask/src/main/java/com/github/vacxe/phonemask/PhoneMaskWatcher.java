package com.github.vacxe.phonemask;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by konstantinaksenov on 17.05.17.
 */
class PhoneMaskWatcher implements TextWatcher {
    private final String mask;
    private final String region;
    private final ValueListener valueListener;

    PhoneMaskWatcher(String mask, String region, ValueListener valueListener) {
        this.mask = mask;
        this.region = region;
        this.valueListener = valueListener;
    }

    private String result = "";
    private EditState state = EditState.IDLE;
    private Pattern notDigitRegex = Pattern.compile("[^\\d]+");


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        final String value = s.toString();
        if (state == EditState.RELEASE) {
            state = EditState.IDLE;
            return;
        }

        if (state == EditState.IDLE) {
            String rawString = value.replace(region, "");
            rawString = notDigitRegex.matcher(rawString).replaceAll("");
            Queue<Character> charsQueue = new LinkedList<>();
            for (char c : rawString.toCharArray()) {
                charsQueue.add(c);
            }

            StringBuilder rawMaskBuilder = new StringBuilder(region + mask);
            Pattern pattern = Pattern.compile("#");
            Matcher matcher = pattern.matcher(region + mask);
            while (matcher.find()) {
                int start = matcher.start();
                if (!charsQueue.isEmpty()) {
                    rawMaskBuilder.replace(start, start + 1, charsQueue.poll().toString());
                    if (charsQueue.isEmpty()) {
                        result = rawMaskBuilder.substring(0, start + 1);
                        break;
                    }
                } else {
                    result = rawMaskBuilder.substring(0, start);
                    break;
                }
            }

            String phone = notDigitRegex.matcher(rawMaskBuilder.toString()).replaceAll("");
            valueListener.onPhoneChanged("+" + phone);
            state = EditState.EDIT;
        }

        switch (state){
            case EDIT:
                state = EditState.CLEAR;
                s.clear();
                break;
            case CLEAR:
                state = EditState.RELEASE;
                s.append(result, 0, result.length());
                break;
            default:
                break;
        }
    }


    enum EditState {
        IDLE, EDIT, CLEAR, RELEASE
    }

}
