package net.rahka.chess;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Path;
import com.github.rvesse.airline.annotations.restrictions.PathKind;
import com.github.rvesse.airline.annotations.restrictions.Required;
import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.configuration.ConfigurableClass;
import net.rahka.chess.configuration.ConfigurableItem;
import net.rahka.chess.configuration.Configuration;
import net.rahka.chess.game.agent.Agent;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Command(name = "gen-conf", description = "Generate configuration file for agents")
public class ConfigurationGenerator implements CLI.Command {

    @Option(name = {"-w", "--white"}, description = "The white agent")
    @Required
    private String whiteAgentName;

    @Option(name = {"-b", "--black"}, description = "The black agent")
    @Required
    private String blackAgentName;

    @Option(name = {"-o", "--output"}, description = "The output path of the generated configuration")
    @Path(kind = PathKind.FILE)
    private File outputFile = new File("config.yaml");

    @Configurable(name = "Random")
    private static Random random() {
        return new Random();
    }

    @Override
    public void run() throws Exception {
        var configuration = new Configuration("net.rahka.chess", ConfigurationGenerator.class);

        ConfigurableClass<Agent> whiteAgentClass = null, blackAgentClass = null;
        for (var cls : configuration.find(Agent.class)) {
            if (cls.getName().equals(whiteAgentName)) {
                whiteAgentClass = cls;
            }

            if (cls.getName().equals(blackAgentName)) {
                blackAgentClass = cls;
            }
        }

        if (whiteAgentClass == null && blackAgentClass == null) {
            throw new RuntimeException("Could not find agent class with the name of \"" + blackAgentName + "\" or \"" + whiteAgentName + "\"");
        } else if (whiteAgentClass == null) {
            throw new RuntimeException("Could not find agent class with the name of \"" + whiteAgentName + "\"");
        } else if (blackAgentClass == null) {
            throw new RuntimeException("Could not find agent class with the name of \"" + blackAgentName + "\"");
        }

        var lines = new ArrayList<String>();
        lines.add("White agent:");
        generateConfigMap(whiteAgentClass, lines, 1);
        lines.add("Black agent:");
        generateConfigMap(blackAgentClass, lines, 1);

        try (var writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (String line : lines) {
                writer.write(line);
                System.out.println(line);
            }
        }

        System.out.println("Configuration file written to \"" + outputFile.getAbsolutePath() + "\"");
    }

    private void generateConfigMap(ConfigurableClass<?> configurableClass, ArrayList<String> config, int indent) {
        var indentation = new String(new char[indent * 2]).replace("\0", " ");
        for (ConfigurableItem dependency : configurableClass.getDependencies()) {
            if (dependency.isConfigurableInteger()) {
                config.add(indentation + dependency.getName() + ": " + dependency.asConfigurableInteger().getDef());
            } else if (dependency.isConfigurableFloatingPoint()) {
                config.add(indentation + dependency.getName() + ": " + dependency.asConfigurableFloatingPoint().getDef());
            } else if (dependency.isConfigurableBoolean()) {
                config.add(indentation + dependency.getName() + ": " + dependency.asConfigurableBoolean().getDef());
            } else if (dependency.isConfigurableString()) {
                config.add(indentation + dependency.getName() + ": \"" + dependency.asConfigurableString().getDef() + "\"");
            }
        }

        for (ConfigurableItem dependency : configurableClass.getDependencies()) {
            if (dependency.isConfigurableClass() && dependency.asConfigurableClass().getClasses().size() > 1) {
                if (dependency.asConfigurableClass().getDef().getDependencies().length > 0) {
                    config.add(indentation + "- " + dependency.getName() + ": " + dependency.asConfigurableClass().getDef().getName());
                    generateConfigMap(dependency.asConfigurableClass().getDef(), config, indent + 1);
                } else {
                    config.add(indentation + dependency.getName() + ": " + dependency.asConfigurableClass().getDef().getName());
                }
            }
        }
    }

}
