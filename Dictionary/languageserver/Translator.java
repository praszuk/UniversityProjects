package zad1.languageserver;

import java.util.HashMap;

public class Translator {
    private String lang;
    private String fullName;
    private HashMap<String, String> dict;

    public String getLang() {
        return lang;
    }

    public String getFullName() {
        return fullName;
    }

    public String getTranslation(String word) {
        return dict.getOrDefault(word, null);
    }

    @Override
    public String toString() {
        return "Lang: " + lang+
                "\nFullName: " + fullName+
                "\nDictionary: " + dict.toString();
    }
}
