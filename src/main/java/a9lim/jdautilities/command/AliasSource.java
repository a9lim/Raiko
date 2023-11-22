package a9lim.jdautilities.command;

@FunctionalInterface
public interface AliasSource {

    // return array of aliases for a certain command
    String[] getAliases(String command);
}
