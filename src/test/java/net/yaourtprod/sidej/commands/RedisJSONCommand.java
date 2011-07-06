package net.yaourtprod.sidej.commands;

public class RedisJSONCommand {
	private String name;
	private String summary;
	private String since;
	private String group;
	private RedisJSONCommandArgument[] arguments;

	public RedisJSONCommand() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(final String summary) {
		this.summary = summary;
	}

	public String getSince() {
		return since;
	}

	public void setSince(final String since) {
		this.since = since;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(final String group) {
		this.group = group;
	}

	public RedisJSONCommandArgument[] getArguments() {
		return arguments;
	}

	public void setArguments(final RedisJSONCommandArgument[] arguments) {
		this.arguments = arguments;
	}

}