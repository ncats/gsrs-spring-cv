package gsrs.controller;

/**
 * Common implementations of {@link IdHelpers}.
 */
public enum IdHelpers implements IdHelper {
    /**
     * An ID that is a number.
     */
    NUMBER("[0-9]+", "[^0-9]+"),
    STRING_NO_WHITESPACE("\\S+"),
    /**
     * An ID that is a UUID.
     */
    UUID( "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"),
    CUSTOM(".*")
    ;

    private final String regex;
    private final String notRegex;

    IdHelpers(String regex, String notRegex) {
        this.regex = regex;
        this.notRegex = notRegex;
    }
    IdHelpers(String regex) {
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
