package gsrs.controller;

import java.util.regex.Pattern;

public enum CommonIDRegexes implements IdHelper {

    NUMBER("[0-9]+", "[^0-9]+"),
    STRING_NO_WHITESPACE("\\W+"),
    UUID( "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"),
    CUSTOM(".*")
    ;

    private final String regex;
    private final String notRegex;

    CommonIDRegexes(String regex, String notRegex) {
        this.regex = regex;
        this.notRegex = notRegex;
    }
    CommonIDRegexes(String regex) {
        this(regex, "\\.+");
    }

    @Override
    public String getRegexAsString() {
        return regex;
    }

    @Override
    public String getInverseRegexAsString() {
        return notRegex;
    }

    public String replaceId(String input, String idLiteral){
        return input.replace(idLiteral, regex);
    }
    public String replaceInverseId(String input, String idLiteral){
        return input.replace(idLiteral, notRegex);
    }
}
