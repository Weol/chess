package net.rahka.chess;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.rahka.chess.agent.Agent;

import java.util.function.Supplier;

@AllArgsConstructor
public class AgentHolder {

	@NonNull @Getter
	private Supplier<Agent> supplier;

	@NonNull @Getter
	private String name;

	@Override
	public String toString() {
		return name;
	}

}
