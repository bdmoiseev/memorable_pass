package com.skyhawk.yourpasswords;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    int NumSyllables;
    int NumEndSymbols;
    int NumOuterSymbols;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: Read this from options
        NumEndSymbols = 2;
        NumOuterSymbols = 2;
        NumSyllables = 4;

        Button buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new ButtonStartOnClickListener());

        CheckBox showPassword = (CheckBox) findViewById(R.id.checkBoxShowPrivateKey);
        showPassword.setOnCheckedChangeListener(
                new ShowPrivateKeyCheckBoxOnCheckedChangeListener());

        ImageButton buttonClip = (ImageButton) findViewById(R.id.ClipButton);
        buttonClip.setOnClickListener(new ClipButtonOnClickListener());
        buttonClip.setOnTouchListener(new ClipButtonOnTouchListener());
    }

    private final class ButtonStartOnClickListener implements View.OnClickListener {
        private final List<Character> Consonants = Arrays.asList('q','w','r','t','y','p','s',
                'd','f','g','h','j','k','l','z','x','c','v','b','n','m','Q','W','R','T','Y',
                'P','S','D','F','G','H','J','K','L','Z','X','C','V','B','N','M');
        private final List<Character> Vowels = Arrays.asList('a','e','i','o','u','A','E',
                'I','O','U');
        private final List<Character> Digits = Arrays.asList('0','1','2','3','4','5','6',
                '7','8','9');
        private final List<String> Specsymbols = Arrays.asList("()", ")(", "[]", "][",
                "{}", "}{", "!!", "??", "..", ",,", "<>", "><", "/\\", "\\/", "||", "__",
                "@@", "::", ";;", "\"\"", "''", "##", "$$", "%%", "^^", "&&", "**", "--",
                "++", "==");

        @Override
        public void onClick(View v) {
            String serviceName = ((EditText) findViewById(R.id.editTextServiceName)).getText().toString();
            String privateKey = ((EditText) findViewById(R.id.editTextPrivateKey)).getText().toString();
            EditText newPassword = (EditText) findViewById(R.id.editTextNewPassword);
            newPassword.setText(passByString(serviceName + privateKey).toString());
            newPassword.setFocusable(true);
            newPassword.setFocusableInTouchMode(true);
            newPassword.requestFocus();
            newPassword.selectAll();
        }

        private String passByString(String string) {
            CharByHashGenerator charByHashGenerator = new CharByHashGenerator(string);
            String result = new String();
            for (int i = 0; i < NumSyllables; ++i) {
                result += charByHashGenerator.Next(Consonants);
                result += charByHashGenerator.Next(Vowels);
            }

            if (NumEndSymbols > 0) {
                result += "_";
            }

            List<Character> endSymbols = new ArrayList<>();
            endSymbols.addAll(Consonants);
            endSymbols.addAll(Digits);
            for (int i = 0; i < NumEndSymbols; ++i) {
                result += charByHashGenerator.Next(endSymbols);
            }

            for (int i = 0; i < NumOuterSymbols; ++i) {
                String pair = charByHashGenerator.Next(Specsymbols);
                result = pair.charAt(0) + result + pair.charAt(1);
            }

            return result;
        }

        private final class CharByHashGenerator {
            BigInteger Code;

            CharByHashGenerator(String string) {
                byte[] md5Bytes;
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.update(string.getBytes());
                    md5Bytes = md.digest();
                } catch(NoSuchAlgorithmException e) {
                    throw new RuntimeException("Bad algorithm type");
                }
                Code = new BigInteger(1, md5Bytes);
            }

            <T> T Next(List<T> chars) {
                BigInteger numChars = BigInteger.valueOf(chars.size());
                if (Code.compareTo(numChars.multiply(BigInteger.valueOf(1000))) < 0) {
                    throw new RuntimeException("Too many symbols required " + Code.toString());
                }
                T nextChar = chars.get(Code.mod(numChars).intValue());
                Code = Code.divide(numChars);
                return nextChar;
            }
        }
    }

    private final class ShowPrivateKeyCheckBoxOnCheckedChangeListener
            implements CheckBox.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton button, boolean value) {
            EditText privateKeyEditText = (EditText) findViewById(R.id.editTextPrivateKey);
            if (value) {
                privateKeyEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                privateKeyEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            privateKeyEditText.setSelection(privateKeyEditText.getText().length());
        }
    }

    private final class ClipButtonOnClickListener
            implements ImageButton.OnClickListener {
        @Override
        public void onClick(View v) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            EditText newPassword = (EditText) findViewById(R.id.editTextNewPassword);
            String text = newPassword.getText().toString();
            clipboard.setPrimaryClip(ClipData.newPlainText(text, text));
        }
    }

    private final class ClipButtonOnTouchListener
            implements ImageButton.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageButton button = (ImageButton) v;
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                button.setImageDrawable(
                        getResources().getDrawable(R.drawable.copy_icon_pressed));
            }
            if(event.getAction() == MotionEvent.ACTION_UP) {
                button.setImageDrawable(
                        getResources().getDrawable(R.drawable.copy_icon));
            }
            return false;
        }
    }
}
