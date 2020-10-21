package gsrs.controller;

public interface IdHelper {


    String getRegexAsString();

    String getInverseRegexAsString();

    default String replaceId(String input, String idLiteral){
        return input.replace(idLiteral, getRegexAsString());
    }
    default String replaceInverseId(String input, String idLiteral){
        return input.replace(idLiteral, getInverseRegexAsString());
    }

}
