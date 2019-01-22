package common;

public class CommandNotFoundException extends Exception {

    public CommandNotFoundException(int type){
        super("There is no command with type "+type);
    }
}
