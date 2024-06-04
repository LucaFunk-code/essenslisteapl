package org.example;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private ResourceBundle bundle;

    public LanguageManager() {
        bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public void changeLanguage(Locale locale) {
        bundle = ResourceBundle.getBundle("messages", locale);
    }
}
