package net.yaourtprod.sidej.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.yaourtprod.sidej.RedisCommand;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class CheckAvailableRedisCommandsIT {
    /** URL where we can find up-to-date Redis commands list. */
    public static final String COMMANDS_URL_LIST = "https://raw.github.com/antirez/redis-doc/master/commands.json";
    public static final String DOCUMENTATION_BASE_URL = "https://raw.github.com/antirez/redis-doc/master/commands/";
    public static final String DOCUMENTATION_SUFFIX = ".md";
    private final SortedSet<RedisJSONCommand> commands = getAvailableRedisCommands();

    public CheckAvailableRedisCommandsIT() throws IOException {
        super();
    }

    private static SortedSet<RedisJSONCommand> getAvailableRedisCommands()
            throws IOException {
        InputStream remoteCommandsStream = null;
        try {
            final URL remoteCommandsUrl = new URL(COMMANDS_URL_LIST);
            remoteCommandsStream = remoteCommandsUrl.openStream();
            final ObjectMapper mapper = new ObjectMapper();
            final Map<String, RedisJSONCommand> commandsMap = mapper.readValue(
                    remoteCommandsStream,
                    new TypeReference<Map<String, RedisJSONCommand>>() {
                    });
            final TreeSet<RedisJSONCommand> commands = new TreeSet<RedisJSONCommand>();
            for (Entry<String, RedisJSONCommand> entry : commandsMap.entrySet()) {
                final RedisJSONCommand cmd = entry.getValue();
                cmd.setName(entry.getKey().toLowerCase());
                commands.add(cmd);
            }
            return commands;
        } finally {
            if (null != remoteCommandsStream) {
                remoteCommandsStream.close();
            }
        }
    }

    /**
     * Checks if there is a new command that is not currently supported.
     */
    @Test
    public void missingRedisCommandSupportTest() throws Exception {
        final StringBuilder missingCommands = new StringBuilder("\n");
        for (final RedisJSONCommand cmd : commands) {
            try {
                RedisCommand.valueOf(cmd.getName().toLowerCase());
            } catch (final IllegalArgumentException iae) {
                missingCommands.append(cmd.getName())
                        .append(" missing in group ").append(cmd.getGroup())
                        .append('\n');
            }
        }

        generateIRedisClient();

        assertEquals(missingCommands.toString(), "\n");
    }

    private void generateIRedisClient() throws IOException {
        final StringBuilder strb = new StringBuilder();
        strb.append("package net.yaourtprod.sidej;\n").append(
                "public interface IRedisClient<K,V> {\n");
        for (final RedisJSONCommand cmd : commands) {
            strb.append("\t/**\n").append("\t * ").append(cmd.getSummary())
                    .append(".<br />\n").append("\t * (")
                    .append(cmd.getGroup()).append(" operation)<br /><br />\n");
            for (final String doc : getDocumentation(cmd)) {
                strb.append("\t * ").append(doc).append("\n");
            }
            strb.append("\t */\n").append("\tUpdateMe ").append(cmd.getName())
                    .append('(');
            if (null != cmd.getArguments()) {
                for (final RedisJSONCommandArgument arg : cmd.getArguments()) {
                    final Object argname = arg.get("name");
                    final Object argtype = arg.get("type");
                    if ("key".equals(argtype)) {
                        strb.append("final K ").append(argname).append(", ");
                    }
                    if ("posix time".equals(argtype)) {
                        strb.append("final long ").append(argname).append(", ");
                    } else {
                        strb.append("final ").append(argtype).append(' ')
                                .append(argname).append(", ");
                    }
                }
                strb.setLength(strb.length() - 2);
            }
            strb.append(");\n\n");
        }
        strb.append("}\n");

        System.out.println(strb.toString());

    }

    private List<String> getDocumentation(final RedisJSONCommand command)
            throws IOException {
        final ArrayList<String> result = new ArrayList<String>(20);
        BufferedReader reader = null;
        try {
            final StringBuilder strb = new StringBuilder(DOCUMENTATION_BASE_URL)
                    .append(command.getName()).append(DOCUMENTATION_SUFFIX);
            final URL url = new URL(strb.toString());
            final InputStream is = url.openStream();
            reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while (null != (line = reader.readLine())) {
                if (line.startsWith("@")) {
                    line = line.substring(1);
                }
                if (line.startsWith("    @cli")) {
                    line = line.substring(8);
                }
                result.add(line);
            }
        } finally {
            if (null != reader) {
                reader.close();
            }
        }
        return result;
    }

}
