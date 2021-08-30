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
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Command(name = "gen-conf", description = "Generate configuration file for agents")
public class ConfigurationGenerator implements CLI.Command {

    @Option(name = {"-w", "--white"}, description = "The total number of games to run")
    @Required
    private String whiteAgentName;

    @Option(name = {"-b", "--black"}, description = "The total number of games to run")
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
            throw new RuntimeException("Could not find agent class with the name of \"" + blackAgentName + "\" or \""  + whiteAgentName + "\"");
        } else if (whiteAgentClass == null) {
            throw new RuntimeException("Could not find agent class with the name of \"" + whiteAgentName + "\"");
        } else if (blackAgentClass == null) {
            throw new RuntimeException("Could not find agent class with the name of \"" + blackAgentName + "\"");
        }


        var whiteConfig = new HashMap<String, Object>();
        var blackConfig = new HashMap<String, Object>();

        generateConfigMap(whiteAgentClass, whiteConfig);
        generateConfigMap(blackAgentClass, blackConfig);

        Map<String, Object> config = new HashMap<>();
        config.put("White agent", Map.of("name", whiteAgentClass.getName(), "configuration", whiteConfig));
        config.put("Black agent", Map.of("name", blackAgentClass.getName(), "configuration", blackConfig));

        Yaml yaml = new Yaml();
        yaml.dump(config, new BufferedWriter(new FileWriter(outputFile)));
        yaml.dump(config, new BufferedWriter(new OutputStreamWriter(System.out)));

        System.out.println("Configuration file written to \"" + outputFile.getAbsolutePath() + "\"");
    }

    private int generateConfigMap(ConfigurableClass<?> configurableClass, Map<String, Object> config) {
        int options = 0;
        for (ConfigurableItem dependency : configurableClass.getDependencies()) {
            if (dependency.isConfigurableInteger()) {
                config.put(dependency.getName(), dependency.asConfigurableInteger().getDef());
                options++;
            } else if (dependency.isConfigurableFloatingPoint()) {
                config.put(dependency.getName(), dependency.asConfigurableFloatingPoint().getDef());
                options++;
            } else if (dependency.isConfigurableBoolean()) {
                config.put(dependency.getName(), dependency.asConfigurableBoolean().getDef());
                options++;
            } else if (dependency.isConfigurableString()) {
                config.put(dependency.getName(), dependency.asConfigurableString().getDef());
                options++;
            } else if (dependency.isConfigurableClass()) {
                options++;
            }
        }

        for (ConfigurableItem dependency : configurableClass.getDependencies()) {
            if (dependency.isConfigurableClass()) {
                var dependencyConfig = new HashMap<String, Object>();
                int dependencyOptions = generateConfigMap(dependency.asConfigurableClass().getDef(), dependencyConfig);

                if (dependencyOptions > 1) {
                    config.put(dependency.getName(), Map.of("name", dependency.asConfigurableClass().getDef().getName(), "configuration", dependencyConfig));
                } else if (dependency.asConfigurableClass().getClasses().size() > 1) {
                    config.put(dependency.getName(), dependency.asConfigurableClass().getDef().getName());
                }

                options = Math.max(dependencyOptions, options);
            }
        }

        return options;
    }

}
