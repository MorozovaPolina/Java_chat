package common;

import java.util.HashMap;
import java.util.Map;
/**
 * Command enumeration
 *
 * @author Anastasiia Chernysheva
 */
public enum Command {
    INTRODUCE(1),
    UPLOAD_FILE(2),
    MESSAGE(3),
    DOWNLOAD_FILE(4),
    QUIT(5),
    GET_ONLINE(6),
    MESSAGE_HISTORY(7);

    private static final Map<Integer, Command> commandById = new HashMap<Integer, Command>();

    static {
        for (Command command : Command.values()) {
            if (commandById.put(command.getId(), command) != null) {
                throw new IllegalArgumentException("Duplicate id for enum Command value: " + command);
            }
        }
    }

    private final int id;

    Command(final int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static Command getById(int id) {
        return commandById.get(id);
    }


}
