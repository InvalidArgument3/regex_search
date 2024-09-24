package net.natte.re_search.query;

public enum Attribute {
    NAME,
    MOD,
    ID,
    TOOLTIP,
    TAG;

    public static Attribute fromPrefix(String prefix) {
        return switch (prefix){
            case "" -> NAME;
            case "@" -> MOD;
            case "*" -> ID;
            case "$" -> TOOLTIP;
            case "#" -> TAG;
            default -> throw new IllegalStateException("Unexpected value: " + prefix);
        };
    }
}
