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
package hayashi.jdautilities.command.annotation;

import hayashi.jdautilities.command.*;
import net.dv8tion.jda.api.Permission;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JDACommand {

    String[] name() default {"null"};

    String help() default "no help available";

    boolean guildOnly() default true;

    String requiredRole() default "";

    boolean ownerCommand() default false;

    String arguments() default "";

    Cooldown cooldown() default @Cooldown(0);

    Permission[] botPermissions() default {};

    Permission[] userPermissions() default {};

    boolean useTopicTags() default true;

    String[] children() default {};

    boolean isHidden() default false;

    Category category() default @Category(name = "null", location = Category.class);

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Module {
        String[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Cooldown {
        int value();
        Command.CooldownScope scope() default Command.CooldownScope.USER;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Category {
        String name();
        Class<?> location();
    }

}
