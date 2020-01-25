package net.rahka.chess;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.rahka.chess.agent.heuristics.Heuristic;

import java.util.function.Supplier;

@AllArgsConstructor
public class HeuristicSupplier implements Supplier<Heuristic> {

	@NonNull @Getter
	private String name;

	@NonNull @Getter
	private Supplier<Heuristic> supplier;

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Heuristic get() {
		return getSupplier().get();
	}

}
