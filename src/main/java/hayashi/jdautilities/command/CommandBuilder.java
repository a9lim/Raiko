/*
 * Copyright 2016-2018 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hayashi.jdautilities.command;

import net.dv8tion.jda.api.Permission;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A chain-setter based builder for {@link Command Commands}.
 *
 * <p>This is more useful for creation of commands "mid-runtime".
 * <br>A good usage would be to create a Command via eval and register it via
 * {@link CommandClient#addCommand(Command)
 * CommandClient#addCommand(Command)}.
 *
 * <p>While useful during runtime, this is completely inferior to extending Command as a superclass
 * before compilation, and shouldn't be used in place of the ladder.
 *
 * @author Kaidan Gustave
 * @since 1.6
 */
public class CommandBuilder {
    private String name = "null";
    private String help = "no help available";
    private Command.Category category;
    private String arguments, requiredRole;
    private boolean guildOnly = true;
    private boolean ownerCommand, hidden;
    private int cooldown;
    private Permission[] userPermissions = new Permission[0];
    private Permission[] botPermissions = new Permission[0];
    private final LinkedList<String> aliases = new LinkedList<>();
    private final LinkedList<Command> children = new LinkedList<>();
    private BiConsumer<CommandEvent, Command> helpBiConsumer;
    private boolean usesTopicTags = true;
    private Command.CooldownScope cooldownScope = Command.CooldownScope.USER;

    /**
     * Sets the {@link Command#name name}
     * of the Command built from this CommandBuilder.
     *
     * @param name The name of the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setName(String name) {
        this.name = name != null ? name : "null";
        return this;
    }

    /**
     * Sets the {@link Command#help help}
     * snippet of the Command built from this CommandBuilder.
     *
     * @param help The help snippet of the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setHelp(String help) {
        this.help = help != null ? help : "no help available";
        return this;
    }

    /**
     * Sets the {@link Command#category category}
     * of the Command built from this CommandBuilder.
     *
     * @param category The category of the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setCategory(Command.Category category) {
        this.category = category;
        return this;
    }

    /**
     * Sets the {@link Command#arguments arguments}
     * of the Command built from this CommandBuilder.
     *
     * @param arguments The arguments of the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setArguments(String arguments) {
        this.arguments = arguments;
        return this;
    }

    /**
     * Sets the Command built to be {@link Command#guildOnly
     * guild only}.
     *
     * @param guildOnly {@code true} if the Command built is guild only, {@code false} if it is not.
     * @return This CommandBuilder
     */
    public CommandBuilder setGuildOnly(boolean guildOnly) {
        this.guildOnly = guildOnly;
        return this;
    }

    /**
     * Sets the name of a {@link Command#requiredRole
     * required role} to use the Command built from this CommandBuilder.
     *
     * @param requiredRole The name of a role required to use the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setRequiredRole(String requiredRole) {
        this.requiredRole = requiredRole;
        return this;
    }

    /**
     * Sets the Command built to be {@link Command#ownerCommand
     * owner only}.
     *
     * @param ownerCommand {@code true} if the Command built is owner only, {@code false} if it is not.
     * @return This CommandBuilder
     */
    public CommandBuilder setOwnerCommand(boolean ownerCommand) {
        this.ownerCommand = ownerCommand;
        return this;
    }

    /**
     * Sets the {@link Command#cooldown cooldown}
     * of the Command built from this CommandBuilder.
     *
     * @param cooldown The number of seconds the built Command will be on cooldown.
     * @return This CommandBuilder
     */
    public CommandBuilder setCooldown(int cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    /**
     * Sets the {@link Command#userPermissions
     * required user permissions} of the Command built from this CommandBuilder.
     *
     * @param userPermissions The required Permissions a User must have when using the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setUserPermissions(Permission... userPermissions) {
        this.userPermissions = userPermissions != null ? userPermissions : new Permission[0];
        return this;
    }

    /**
     * Sets the {@link Command#userPermissions
     * required user permissions} of the Command built from this CommandBuilder.
     *
     * @param userPermissions The required Permissions a User must have when using the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setUserPermissions(Collection<Permission> userPermissions) {
        this.userPermissions = userPermissions != null ? (Permission[]) userPermissions.toArray() : new Permission[0];
        return this;
    }

    /**
     * Sets the {@link Command#botPermissions
     * required bot permissions} of the Command built from this CommandBuilder.
     *
     * @param botPermissions The required Permissions the bot must have when using the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setBotPermissions(Permission... botPermissions) {
        this.botPermissions = botPermissions != null ? botPermissions : new Permission[0];
        return this;
    }

    /**
     * Sets the {@link Command#botPermissions
     * required bot permissions} of the Command built from this CommandBuilder.
     *
     * @param botPermissions The required Permissions the bot must have when using the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setBotPermissions(Collection<Permission> botPermissions) {
        this.botPermissions = botPermissions != null ? (Permission[]) botPermissions.toArray() : new Permission[0];
        return this;
    }

    /**
     * Adds a {@link Command#aliases alias}
     * for the Command built from this CommandBuilder.
     *
     * @param alias The Command alias to add.
     * @return This CommandBuilder.
     */
    public CommandBuilder addAlias(String alias) {
        aliases.add(alias);
        return this;
    }

    /**
     * Adds {@link Command#aliases aliases}
     * for the Command built from this CommandBuilder.
     *
     * @param aliases The Command aliases to add.
     * @return This CommandBuilder.
     */
    public CommandBuilder addAliases(String... aliases) {
        for (String alias : aliases)
            addAlias(alias);
        return this;
    }

    /**
     * Sets the {@link Command#aliases aliases}
     * of the Command built from this CommandBuilder.
     *
     * @param aliases The aliases of the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setAliases(String... aliases) {
        this.aliases.clear();
        if (aliases != null)
            for (String alias : aliases)
                addAlias(alias);
        return this;
    }

    /**
     * Sets the {@link Command#aliases aliases}
     * of the Command built from this CommandBuilder.
     *
     * @param aliases The aliases of the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setAliases(Collection<String> aliases) {
        this.aliases.clear();
        if (aliases != null)
            this.aliases.addAll(aliases);
        return this;
    }

    /**
     * Adds a {@link Command#children child}
     * Command to the Command built from this CommandBuilder.
     *
     * @param child The child Command to add.
     * @return This CommandBuilder.
     */
    public CommandBuilder addChild(Command child) {
        children.add(child);
        return this;
    }

    /**
     * Adds {@link Command#children child}
     * Commands to the Command built from this CommandBuilder.
     *
     * @param children The child Commands to add.
     * @return This CommandBuilder.
     */
    public CommandBuilder addChildren(Command... children) {
        for (Command child : children)
            addChild(child);
        return this;
    }

    /**
     * Sets the {@link Command#children children}
     * of the Command built from this CommandBuilder.
     *
     * @param children The children of the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setChildren(Command... children) {
        this.children.clear();
        if (children != null)
            for (Command child : children)
                addChild(child);
        return this;
    }

    /**
     * Sets the {@link Command#children children}
     * of the Command built from this CommandBuilder.
     *
     * @param children The children of the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setChildren(Collection<Command> children) {
        this.children.clear();
        if (children != null)
            this.children.addAll(children);
        return this;
    }

    /**
     * Sets the {@link Command#helpBiConsumer
     * help BiConsumer} of the Command built from this CommandBuilder.
     *
     * @param helpBiConsumer The help BiConsumer of the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setHelpBiConsumer(BiConsumer<CommandEvent, Command> helpBiConsumer) {
        this.helpBiConsumer = helpBiConsumer;
        return this;
    }

    /**
     * Sets the Command built to {@link Command#usesTopicTags
     * use TopicTags}.
     *
     * @param usesTopicTags {@code true} if the Command built is uses topic tags, {@code false} if it does not.
     * @return This CommandBuilder
     */
    public CommandBuilder setUsesTopicTags(boolean usesTopicTags) {
        this.usesTopicTags = usesTopicTags;
        return this;
    }

    /**
     * Sets the {@link Command#cooldownScope
     * cooldown scope} of the Command built from this CommandBuilder.
     *
     * @param cooldownScope The CooldownScope of the Command to be built.
     * @return This CommandBuilder
     */
    public CommandBuilder setCooldownScope(Command.CooldownScope cooldownScope) {
        this.cooldownScope = cooldownScope != null ? cooldownScope : Command.CooldownScope.USER;
        return this;
    }

    /**
     * Sets the Command built to be {@link Command#hidden hidden}
     * from the help builder.
     *
     * @param hidden {@code true} if this will be hidden from the help builder, {@code false} otherwise.
     * @return This CommandBuilder
     */
    public CommandBuilder setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    /**
     * Builds the {@link Command Command}
     * using the previously provided information.
     *
     * <p>This uses the only the {@link CommandEvent
     * CommandEvent} parameter that would be provided during
     * {@link Command#execute(CommandEvent) #execute(CommandEvent)},
     * and no information about the Command can be retrieved using this.
     *
     * <p>An alternate method {@link CommandBuilder#build(java.util.function.BiConsumer)} exists if you wish to retrieve information
     * about the Command built during execution.
     *
     * @param execution The {@link java.util.function.Consumer} that runs on Command#execute(CommandEvent).
     * @return The Command built
     */
    public Command build(Consumer<CommandEvent> execution) {
        return build((c, e) -> execution.accept(e));
    }

    /**
     * Builds the {@link Command Command}
     * using the previously provided information.
     *
     * <p>This uses the both the {@link CommandEvent
     * CommandEvent} parameter that would be provided during
     * {@link Command#execute(CommandEvent) #execute(CommandEvent)},
     * and the Command built when, allowing info on the Command to be retrieved during execution.
     *
     * @param execution The {@link java.util.function.BiConsumer} that runs on  {@link Command#execute(CommandEvent)}.
     * @return The Command built
     */
    public Command build(BiConsumer<Command, CommandEvent> execution) {
        return new BlankCommand(name, help, category, arguments,
            guildOnly, requiredRole, ownerCommand, cooldown,
            userPermissions, botPermissions, aliases.toArray(new String[0]),
            children.toArray(new Command[0]), helpBiConsumer, usesTopicTags,
            cooldownScope, hidden) {
            @Override
            protected void execute(CommandEvent event) {
                execution.accept(this, event);
            }
        };
    }

    private abstract static class BlankCommand extends Command {
        BlankCommand(String name, String help, Category category,
                     String arguments, boolean guildOnly, String requiredRole,
                     boolean ownerCommand, int cooldown, Permission[] userPermissions,
                     Permission[] botPermissions, String[] aliases, Command[] children,
                     BiConsumer<CommandEvent, Command> helpBiConsumer,
                     boolean usesTopicTags, CooldownScope cooldownScope, boolean hidden) {
            this.name = name;
            this.help = help;
            this.category = category;
            this.arguments = arguments;
            this.guildOnly = guildOnly;
            this.requiredRole = requiredRole;
            this.ownerCommand = ownerCommand;
            this.cooldown = cooldown;
            this.userPermissions = userPermissions;
            this.botPermissions = botPermissions;
            this.aliases = aliases;
            this.children = children;
            this.helpBiConsumer = helpBiConsumer;
            this.usesTopicTags = usesTopicTags;
            this.cooldownScope = cooldownScope;
            this.hidden = hidden;
        }
    }
}
