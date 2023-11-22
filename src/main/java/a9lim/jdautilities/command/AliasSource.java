package a9lim.jdautilities.command;

@FunctionalInterface
public interface AliasSource {

    String[] getAliases(String command);
}
