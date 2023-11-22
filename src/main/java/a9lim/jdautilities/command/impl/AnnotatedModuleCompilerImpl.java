// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>
// Copyright 2016-2018 John Grosh (jagrosh) <john.a.grosh@gmail.com> & Kaidan Gustave (TheMonitorLizard).
//
// This file is part of Raiko.
//
// Raiko is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// Raiko is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Raiko. If not, see <http://www.gnu.org/licenses/>.

package a9lim.jdautilities.command.impl;

import a9lim.jdautilities.command.AnnotatedModuleCompiler;
import a9lim.jdautilities.command.Command;
import a9lim.jdautilities.command.CommandBuilder;
import a9lim.jdautilities.command.CommandEvent;
import a9lim.jdautilities.command.annotation.JDACommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AnnotatedModuleCompilerImpl implements AnnotatedModuleCompiler {
    private static final Logger LOG = LoggerFactory.getLogger(AnnotatedModuleCompiler.class);

    @Override
    public List<Command> compile(Object o) {
        JDACommand.Module module = o.getClass().getAnnotation(JDACommand.Module.class);
        if (module == null)
            throw new IllegalArgumentException("Object provided is not annotated with JDACommand.Module!");
        if (module.value().length < 1)
            throw new IllegalArgumentException("Object provided is annotated with an empty command module!");


        List<Command> list = new ArrayList<>();
        collect((Method method) -> {
            for (String name : module.value()) {
                if (name.equalsIgnoreCase(method.getName()))
                    return true;
            }
            return false;
        }, o.getClass().getMethods()).forEach(method -> {
            try {
                list.add(compileMethod(o, method));
            } catch (MalformedParametersException e) {
                LOG.error(e.getMessage());
            }
        });
        return list;
    }

    private static Command compileMethod(Object o, Method method) throws MalformedParametersException {
        JDACommand properties = method.getAnnotation(JDACommand.class);
        if (properties == null)
            throw new IllegalArgumentException("Method named " + method.getName() + " is not annotated with JDACommand!");
        CommandBuilder builder = new CommandBuilder();

        // Name
        String[] names = properties.name();
        builder.setName(names.length < 1 ? "null" : names[0]);

        // Aliases
        if (names.length > 1)
            for (int i = 1; i < names.length; i++)
                builder.addAlias(names[i]);

        // Help
        builder.setHelp(properties.help());

        // Arguments
        builder.setArguments(properties.arguments().trim().isEmpty() ? null : properties.arguments().trim());

        // Category
        if (!properties.category().location().equals(JDACommand.Category.class)) {
            JDACommand.Category category = properties.category();
            for (Field field : category.location().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(Command.Category.class) && category.name().equalsIgnoreCase(field.getName())){
                    try {
                        builder.setCategory((Command.Category) field.get(null));
                    } catch (IllegalAccessException e) {
                        LOG.error("Encountered Exception ", e);
                    }
                }
            }
        }

        // Guild Only
        builder.setGuildOnly(properties.guildOnly())
                .setRequiredRole(properties.requiredRole().trim().isEmpty() ? null : properties.requiredRole().trim())
                .setOwnerCommand(properties.ownerCommand())
                .setCooldown(properties.cooldown().value())
                .setCooldownScope(properties.cooldown().scope())
                .setBotPermissions(properties.botPermissions())
                .setUserPermissions(properties.userPermissions())
                .setHidden(properties.isHidden());

        // Child Commands
        if (properties.children().length > 0) {
            collect((Method m) -> {
                for (String cName : properties.children()) {
                    if (cName.equalsIgnoreCase(m.getName()))
                        return true;
                }
                return false;
            }, o.getClass().getMethods()).forEach(cm -> {
                try {
                    builder.addChild(compileMethod(o, cm));
                } catch (MalformedParametersException e) {
                    LOG.error("Encountered Exception ", e);
                }
            });
        }

        // Analyze parameter types as a final check.

        Class<?>[] parameters = method.getParameterTypes();
        // Dual Parameter Command, CommandEvent
        if (parameters[0] == Command.class && parameters[1] == CommandEvent.class) {
            return builder.build((command, event) -> {
                try {
                    method.invoke(o, command, event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOG.error("Encountered Exception ", e);
                }
            });
        } else if (parameters[0] == CommandEvent.class) {
            // Single parameter CommandEvent
            if (parameters.length == 1) {
                return builder.build(event -> {
                    try {
                        method.invoke(o, event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        LOG.error("Encountered Exception ", e);
                    }
                });
            }
            // Dual Parameter CommandEvent, Command
            if (parameters[1] == Command.class) {
                return builder.build((command, event) -> {
                    try {
                        method.invoke(o, event, command);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        LOG.error("Encountered Exception ", e);
                    }
                });
            }
        }

        // If we reach this point there is a malformed method and we shouldn't finish the compilation.
        throw new MalformedParametersException("Method named " + method.getName() + " was not compiled due to improper parameter types!");
    }

    @SafeVarargs
    private static <T> List<T> collect(Predicate<T> filter, T... entities) {
        List<T> list = new ArrayList<>();
        for (T entity : entities)
            if (filter.test(entity))
                list.add(entity);
        return list;
    }

}
