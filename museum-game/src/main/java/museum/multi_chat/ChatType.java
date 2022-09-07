package museum.multi_chat;

public enum ChatType {
    SYSTEM("Системный чат", "С", "system");

    public final String title;
    public final String symbol;
    public final String key;

    ChatType(String title, String symbol, String key) {
        this.title = title;
        this.symbol = symbol;
        this.key = key;
    }
}
