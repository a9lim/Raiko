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

    public CommandBuilder setName(String n) {
        name = n != null ? n : "null";
        return this;
    }

    public CommandBuilder setHelp(String h) {
        help = h != null ? h : "no help available";
        return this;
    }

    public CommandBuilder setCategory(Command.Category cat) {
        category = cat;
        return this;
    }

    public CommandBuilder setArguments(String args) {
        arguments = args;
        return this;
    }

    public CommandBuilder setGuildOnly(boolean b) {
        guildOnly = b;
        return this;
    }

    public CommandBuilder setRequiredRole(String role) {
        requiredRole = role;
        return this;
    }

    public CommandBuilder setOwnerCommand(boolean b) {
        ownerCommand = b;
        return this;
    }

    public CommandBuilder setCooldown(int i) {
        cooldown = i;
        return this;
    }

    public CommandBuilder setUserPermissions(Permission... permissions) {
        userPermissions = permissions != null ? permissions : new Permission[0];
        return this;
    }

    public CommandBuilder setUserPermissions(Collection<Permission> permissions) {
        userPermissions = permissions != null ? (Permission[]) permissions.toArray() : new Permission[0];
        return this;
    }

    public CommandBuilder setBotPermissions(Permission... permissions) {
        botPermissions = permissions != null ? permissions : new Permission[0];
        return this;
    }

    public CommandBuilder setBotPermissions(Collection<Permission> permissions) {
        botPermissions = permissions != null ? (Permission[]) permissions.toArray() : new Permission[0];
        return this;
    }

    public CommandBuilder addAlias(String alias) {
        aliases.add(alias);
        return this;
    }

    public CommandBuilder addAliases(String... strings) {
        for (String alias : strings)
            addAlias(alias);
        return this;
    }

    public CommandBuilder setAliases(String... strings) {
        aliases.clear();
        if (strings != null)
            for (String alias : strings)
                addAlias(alias);
        return this;
    }

    public CommandBuilder setAliases(Collection<String> strings) {
        aliases.clear();
        if (strings != null)
            aliases.addAll(strings);
        return this;
    }

    public CommandBuilder addChild(Command child) {
        children.add(child);
        return this;
    }

    public CommandBuilder addChildren(Command... commands) {
        for (Command child : commands)
            addChild(child);
        return this;
    }

    public CommandBuilder setChildren(Command... commands) {
        children.clear();
        if (commands != null)
            for (Command child : commands)
                addChild(child);
        return this;
    }

    public CommandBuilder setChildren(Collection<Command> commands) {
        children.clear();
        if (commands != null)
            children.addAll(commands);
        return this;
    }

    public CommandBuilder setHelpBiConsumer(BiConsumer<CommandEvent, Command> consumer) {
        helpBiConsumer = consumer;
        return this;
    }

    public CommandBuilder setUsesTopicTags(boolean b) {
        usesTopicTags = b;
        return this;
    }

    public CommandBuilder setCooldownScope(Command.CooldownScope scope) {
        cooldownScope = scope != null ? scope : Command.CooldownScope.USER;
        return this;
    }

    public CommandBuilder setHidden(boolean h) {
        hidden = h;
        return this;
    }

    public Command build(Consumer<CommandEvent> execution) {
        return build((c, e) -> execution.accept(e));
    }

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
        BlankCommand(String n, String h, Category cat,
                     String args, boolean b1, String role,
                     boolean b2, int c, Permission[] uperms,
                     Permission[] bperms, String[] al, Command[] commands,
                     BiConsumer<CommandEvent, Command> consumer,
                     boolean b3, CooldownScope scope, boolean b4) {
            name = n;
            help = h;
            category = cat;
            arguments = args;
            guildOnly = b1;
            requiredRole = role;
            ownerCommand = b2;
            cooldown = c;
            userPermissions = uperms;
            botPermissions = bperms;
            aliases = al;
            children = commands;
            helpBiConsumer = consumer;
            usesTopicTags = b3;
            cooldownScope = scope;
            hidden = b4;
        }
    }
}
