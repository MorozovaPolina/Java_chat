package common;

public enum Command {
    INTRODUCE(1),
    UPLOAD_FILE(2),
    MESSAGE(3),
    DOWNLOAD_FILE(4),
    QUIT(5),
    GET_ONLINE(6),
    MESSAGE_HISTORY(7);

    private final int type;

    Command(final int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public static Command getCommand(int type) throws CommandNotFoundException {
        for (Command command : Command.values()) {
            if (command.getType() == type) {
                return command;
            }
        }
        throw new CommandNotFoundException(type);
    }

}
