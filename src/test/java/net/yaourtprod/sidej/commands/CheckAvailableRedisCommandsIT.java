package net.yaourtprod.sidej.commands;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.yaourtprod.sidej.RedisCommand;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
public class CheckAvailableRedisCommandsIT {
	/** URL where we can find up-to-date Redis commands list. */
	public static final String COMMANDS_URL_LIST = "https://raw.github.com/antirez/redis-doc/master/commands.json";
	
	@Test
	public void checkAvailableCommandsTest() throws Exception {
		final URL remoteCommandsUrl = new URL(COMMANDS_URL_LIST);
		final InputStream remoteCommandsStream = remoteCommandsUrl.openStream();
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, RedisJSONCommand> commands = mapper.readValue(remoteCommandsStream, new TypeReference<Map<String, RedisJSONCommand>>(){});
		remoteCommandsStream.close();
		
		final StringBuilder missingCommands = new StringBuilder();
		for(Entry<String, RedisJSONCommand> entry : commands.entrySet()) {
			final RedisJSONCommand cmd = entry.getValue();
			cmd.setName(entry.getKey());
			try {
				final RedisCommand redisCommand = RedisCommand.valueOf(cmd.getName().toLowerCase());
			} catch (final IllegalArgumentException iae) {
				missingCommands
					.append(cmd.getName())
					.append(" missing in group ")
					.append(cmd.getGroup())
					.append('\n');
			}
		}
		
		assertEquals(missingCommands.toString(), "");
	}
}
