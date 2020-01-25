package net.rahka.chess;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.rahka.chess.agent.Agent;
import net.rahka.chess.agent.AgentConfiguration;

import java.util.function.Function;

@AllArgsConstructor
public class AgentSupplier implements Function<AgentConfiguration, Agent> {

	@NonNull @Getter
	private Function<AgentConfiguration, Agent> supplier;

	@NonNull @Getter
	private String name;

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Agent apply(AgentConfiguration agentConfiguration) {
		return getSupplier().apply(agentConfiguration);
	}

}
