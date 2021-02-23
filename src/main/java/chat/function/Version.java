package chat.function;

import com.google.gson.Gson;

public class Version {
    public static String prefix, type;
    public static float version;
    public static Gson gson;

    public Version(String prefix, String type, float version) {
        this.version = version;
        this.prefix = prefix;
        this.type = type;
        this.gson = new Gson();
    }
}
