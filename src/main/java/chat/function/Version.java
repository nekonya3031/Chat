package chat.function;

public class Version {
    public static String prefix, type;
    public static float version;
    public Version(String prefix, String type, float version){
        this.version = version;
        this.prefix = prefix;
        this.type = type;
    }
}
