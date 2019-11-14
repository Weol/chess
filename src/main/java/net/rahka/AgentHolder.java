package net.rahka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public class AgentHolder<T> {

	@NonNull @Getter
	private Class<T> cls;

	@NonNull @Getter
	private String name;

	@Override
	public String toString() {
		return name;
	}

}
