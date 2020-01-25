package net.rahka.chess.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.rahka.chess.agent.heuristics.Heuristic;

@AllArgsConstructor
@NoArgsConstructor
public class AgentConfiguration {

    @Getter
    int depthLimit;

    @Getter
    Heuristic heuristic;

}
