package a9lim.raiko.commands.general;

import a9lim.jdautilities.command.Command;
import a9lim.jdautilities.command.CommandEvent;

import java.util.Random;

public class RollCmd extends Command {
    private final Random rand;
    public RollCmd() {
        name = "roll";
        help = "rolls dice";
        category = new Command.Category("General");
        guildOnly = true;
        rand = new Random();
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("d");
        long dice = Long.parseLong(args[0]);
        long sides = Long.parseLong(args[1])+1;
        long sum = 0;
        while(dice-- > 0)
            sum += rand.nextLong(sides);

        event.replySuccess("Result: " + sum);
    }
}
