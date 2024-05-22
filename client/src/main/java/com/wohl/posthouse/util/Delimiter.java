package com.wohl.posthouse.util;

public class Delimiter {
    public static String get() {
        return "</>";
    }
    public static String getHeadStart() {
        return "<HEAD>";
    }
    public static String getHeadEnd() {
        return "</HEAD>";
    }
    public static String getBodyStart() {
        return "<BODY>";
    }
    public static String getBodyEnd() {
        return "</BODY>";
    }
    public static String getDigitalSignatureStart() {
        return "<DS>";
    }
    public static String getDigitalSignatureEnd() {
        return "</DS>";
    }

    public static String getPostmanRegisterStart() {
        return "<BONJOUR>";
    }

    public static String getPostmanRegisterEnd() {
        return "</BONJOUR>";
    }

    public static String getPostmanLogoffStart() {
        return "<SALUT>";
    }

    public static String getPostmanLogoffEnd() {
        return "</SALUT>";
    }
}
