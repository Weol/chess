package net.rahka.chess;

import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.help.cli.CliGlobalUsageGenerator;
import com.github.rvesse.airline.parser.errors.ParseCommandMissingException;
import com.github.rvesse.airline.parser.errors.ParseException;

@Cli(name = "chess",
        description = "Chess",
        commands = { Application.class, ChessRunner.class, ConfigurationGenerator.class })
public class CLI {

    public static void main(String[] args) throws Exception {
        var cli = new com.github.rvesse.airline.Cli<Command>(CLI.class);

        try{
            Command cmd = cli.parse(args);
            cmd.run();
        } catch (ParseCommandMissingException e) {
            CliGlobalUsageGenerator<Command> helpGenerator = new CliGlobalUsageGenerator<>();
            helpGenerator.usage(cli.getMetadata(), System.out);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }
    }

    public interface Command {

        void run() throws Exception;

    }

}
