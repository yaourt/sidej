package net.yaourtprod.sidej;

import java.util.List;
import java.util.Map;

public interface IRedisClient<K,V, HK, HV, LK, LV> {
    public static enum LINSERT_POSITION {
        BEFORE,
        AFTER
    }
    
    public static enum OBJECT_COMMAND {
        encoding,
        idletime,
        refcount
    }
    
    public static class Status {
        private final String mesg;
        private final boolean error;
        
        public Status(final boolean error, final String mesg) {
            this.error = error;
            this.mesg = mesg;
        }

        public String getMessage() {
            return mesg;
        }

        public boolean isError() {
            return error;
        }
        
    }
    
    /**
     * Authenticate to the server.<br />
     * (connection operation)<br /><br />
     * 
     * Request for authentication in a password protected Redis server.
     * Redis can be instructed to require a password before allowing clients
     * to execute commands. This is done using the `requirepass` directive in the
     * configuration file.
     * 
     * If `password` matches the password in the configuration file, the server replies with
     * the `OK` status code and starts accepting commands.
     * Otherwise, an error is returned and the clients needs to try a new password.
     * 
     * **Note**: because of the high performance nature of Redis, it is possible to try
     * a lot of passwords in parallel in very short time, so make sure to generate
     * a strong and very long password so that this attack is infeasible.
     * 
     * 
     * @return a status reply 
     * 
     */
    Status auth(final String password);

    /**
     * Echo the given string.<br />
     * (connection operation)<br /><br />
     * 
     * Returns `message`.
     * 
     * 
     * @return a bulk reply 
     * 
     * Examples :
     * 
     *     ECHO "Hello World!"
     * 
     */
    V echo(final V message);

    /**
     * Ping the server.<br />
     * (connection operation)<br /><br />
     * 
     * Returns `PONG`. This command is often used to test if a connection is still
     * alive, or to measure latency.
     * 
     * 
     * @return a status reply 
     * 
     * Examples :
     * 
     *     PING
     * 
     */
    Status ping();

    /**
     * Close the connection.<br />
     * (connection operation)<br /><br />
     * 
     * Ask the server to close the connection. The connection is closed as soon as all
     * pending replies have been written to the client.
     * 
     * 
     * @return a status reply : always OK.
     * 
     */
    Status quit();

    /**
     * Change the selected database for the current connection.<br />
     * (connection operation)<br /><br />
     * 
     * Select the DB with having the specified zero-based numeric index.
     * New connections always use DB 0.
     * 
     * 
     * @return a status reply 
     * 
     */
    Status select(final int index);

    /**
     * Delete a key.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of keys that will be removed. When a key to remove
     * holds a value other than a string, the individual complexity for this key is
     * O(M) where M is the number of elements in the list, set, sorted set or hash.
     * Removing a single key that holds a string value is O(1).
     * 
     * Removes the specified keys.  A key is ignored if it does not exist.
     * 
     * 
     * @return an integer reply : The number of keys that were removed.
     * 
     * Examples :
     * 
     *     SET key1 "Hello"
     *     SET key2 "World"
     *     DEL key1 key2 key3
     * 
     */
    long del(final K... keys);

    /**
     * Determine if a key exists.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns if `key` exists.
     * 
     * 
     * return an integer reply , specifically:
     * 
     * * `1` if the key exists.
     * * `0` if the key does not exist.
     * 
     * Examples :
     * 
     *     SET key1 "Hello"
     *     EXISTS key1
     *     EXISTS key2
     * 
     * @return true if the key exists, false if the key does not exist
     */
    boolean exists(final K key);

    /**
     * Set a key's time to live in seconds.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Set a timeout on `key`. After the timeout has expired, the key will
     * automatically be deleted. A key with an associated timeout is said to be
     * _volatile_ in Redis terminology.
     * 
     * If `key` is updated before the timeout has expired, then the timeout is removed
     * as if the `PERSIST` command was invoked on `key`.
     * 
     * For Redis versions **< 2.1.3**, existing timeouts cannot be overwritten. So, if
     * `key` already has an associated timeout, it will do nothing and return `0`.
     * Since Redis **2.1.3**, you can update the timeout of a key. It is also possible
     * to remove the timeout using the `PERSIST` command. See the page on [key expiry][1]
     * for more information.
     * 
     * [1]: /topics/expire
     * 
     * 
     * return an integer reply , specifically:
     * 
     * * `1` if the timeout was set.
     * * `0` if `key` does not exist or the timeout could not be set.
     * 
     * Examples :
     * 
     *     SET mykey "Hello"
     *     EXPIRE mykey 10
     *     TTL mykey
     *     SET mykey "Hello World"
     *     TTL mykey
     * 
     * @return true if the timeout was set, false if `key` does not exist or the timeout could not be set
     */
    boolean expire(final K key, final int seconds);

    /**
     * Set the expiration for a key as a UNIX timestamp.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Set a timeout on `key`. After the timeout has expired, the key will
     * automatically be deleted. A key with an associated timeout is said to be
     * _volatile_ in Redis terminology.
     * 
     * `EXPIREAT` has the same effect and semantic as `EXPIRE`, but instead of
     * specifying the number of seconds representing the TTL (time to live), it takes
     * an absolute [UNIX timestamp][2] (seconds since January 1, 1970).
     * 
     * As in the case of `EXPIRE` command, if `key` is updated before the timeout has
     * expired, then the timeout is removed as if the `PERSIST` command was invoked on
     * `key`.
     * 
     * [2]: http://en.wikipedia.org/wiki/Unix_time
     * 
     * ## Background
     * 
     * `EXPIREAT` was introduced in order to convert relative timeouts to absolute
     * timeouts for the AOF persistence mode. Of course, it can be used directly to
     * specify that a given key should expire at a given time in the future.
     * 
     * 
     * return an integer reply , specifically:
     * 
     * * `1` if the timeout was set.
     * * `0` if `key` does not exist or the timeout could not be set (see: `EXPIRE`).
     * 
     * Examples :
     * 
     *     SET mykey "Hello"
     *     EXISTS mykey
     *     EXPIREAT mykey 1293840000
     *     EXISTS mykey
     * @return true if the timeout was set, false if `key` does not exist or the timeout could not be set
     */
    long expireat(final K key, final long timestamp);

    /**
     * Find all keys matching the given pattern.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(N) with N being the number of keys in the database, under the assumption that
     * the key names in the database and the given pattern have limited length.
     * 
     * Returns all keys matching `pattern`.
     * 
     * While the time complexity for this operation is O(N), the constant
     * times are fairly low. For example, Redis running on an entry level laptop can
     * scan a 1 million key database in 40 milliseconds.
     * 
     * **Warning**: consider `KEYS` as a command that should only be used in
     * production environments with extreme care.  It may ruin performance when it is
     * executed against large databases. This command is intended for debugging and
     * special operations, such as changing your keyspace layout. Don't use `KEYS`
     * in your regular application code.  If you're looking for a way to find keys in
     * a subset of your keyspace, consider using [sets](/topics/data-types#sets).
     * 
     * Supported glob-style patterns:
     * 
     * * `h?llo` matches `hello`, `hallo` and `hxllo`
     * * `h*llo` matches `hllo` and `heeeello`
     * * `h[ae]llo` matches `hello` and `hallo,` but not `hillo`
     * 
     * Use `\` to escape special characters if you want to match them verbatim.
     * 
     * 
     * @return a multi-bulk reply : list of keys matching `pattern`.
     * 
     * Examples :
     * 
     *     MSET one 1 two 2 three 3 four 4
     *     KEYS *o*
     *     KEYS t??
     *     KEYS *
     * 
     */
    List<K> keys(final K pattern);

    /**
     * Move a key to another database.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Move `key` from the currently selected database (see `SELECT`) to the specified
     * destination database. When `key` already exists in the destination database, or
     * it does not exist in the source database, it does nothing. It is possible to
     * use `MOVE` as a locking primitive because of this.
     * 
     * 
     * return an integer reply , specifically:
     * 
     * * `1` if `key` was moved.
     * * `0` if `key` was not moved.
     * @return true if `key` was moved, false if `key` was not moved
     * 
     */
    boolean move(final K key, final int db);

    /**
     * Inspect the internals of Redis objects.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(1) for all the currently implemented subcommands.
     * 
     * The `OBJECT` command allows to inspect the internals of Redis Objects associated
     * with keys. It is useful for debugging or to understand if your keys are using
     * the specially encoded data types to save space. Your application may also use
     * the information reported by the `OBJECT` command to implement application level
     * key eviction policies when using Redis as a Cache.
     * 
     * The `OBJECT` command supports multiple sub commands:
     * 
     * * `OBJECT REFCOUNT <key>` returns the number of references of the value associated with the specified key. This command is mainly useful for debugging.
     * * `OBJECT ENCODING <key>` returns the kind of internal representation used in order to store the value associated with a key.
     * * `OBJECT IDLETIME <key>` returns the number of seconds since the object stored at the specified key is idle (not requested by read or write operations). While the value is returned in seconds the actual resolution of this timer is 10 seconds, but may vary in future implementations.
     * 
     * Objects can be encoded in different ways:
     * 
     * * Strings can be encoded as `raw` (normal string encoding) or `int` (strings representing integers in a 64 bit signed interval are encoded in this way in order to save space).
     * * Lists can be encoded as `ziplist` or `linkedlist`. The `ziplist` is the special representation that is used to save space for small lists.
     * * Sets can be encoded as `intset` or `hashtable`. The `intset` is a special encoding used for small sets composed solely of integers.
     * * Hashes can be encoded as `zipmap` or `hashtable`. The `zipmap` is a special encoding used for small hashes.
     * * Sorted Sets can be encoded as `ziplist` or `skiplist` format. As for the List type small sorted sets can be specially encoded using `ziplist`, while the `skiplist` encoding is the one that works with sorted sets of any size.
     * 
     * All the specially encoded types are automatically converted to the general type once you perform an operation that makes it no possible for Redis to retain the space saving encoding.
     * 
     * 
     * Different return values are used for different subcommands.
     * 
     * * Subcommands `refcount` and `idletime` returns integers.
     * * Subcommand `encoding` returns a bulk reply.
     * 
     * If the object you try to inspect is missing, a null bulk reply is returned.
     * 
     * Examples :
     * 
     *     redis> lpush mylist "Hello World"
     *     (integer) 4
     *     redis> object refcount mylist
     *     (integer) 1
     *     redis> object encoding mylist
     *     "ziplist"
     *     redis> object idletime mylist
     *     (integer) 10
     * 
     * In the following example you can see how the encoding changes once Redis is no longer able to use the space saving encoding.
     * 
     *     redis> set foo 1000
     *     OK
     *     redis> object encoding foo
     *     "int"
     *     redis> append foo bar
     *     (integer) 7
     *     redis> get foo
     *     "1000bar"
     *     redis> object encoding foo
     *     "raw"
     * 
     */
    String object(final OBJECT_COMMAND command, final K key);

    /**
     * Remove the expiration from a key.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Remove the existing timeout on `key`.
     * 
     * 
     * return an integer reply , specifically:
     * 
     * * `1` if the timeout was removed.
     * * `0` if `key` does not exist or does not have an associated timeout.
     * 
     * Examples :
     * 
     *     SET mykey "Hello"
     *     EXPIRE mykey 10
     *     TTL mykey
     *     PERSIST mykey
     *     TTL mykey
     * @return true if the timeout was removed, false if `key` does not exist or does not have an associated timeout
     */
    boolean persist(final K key);

    /**
     * Return a random key from the keyspace.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Return a random key from the currently selected database.
     * 
     * 
     * @return a bulk reply : the random key, or `nil` when the database is empty.
     * 
     */
    V randomkey();

    /**
     * Rename a key.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Renames `key` to `newkey`. It returns an error when the source and destination
     * names are the same, or when `key` does not exist. If `newkey` already exists it
     * is overwritten.
     * 
     * 
     * @return a status reply 
     * 
     * Examples :
     * 
     *     SET mykey "Hello"
     *     RENAME mykey myotherkey
     *     GET myotherkey
     * 
     */
    Status rename(final K key, final K newkey);

    /**
     * Rename a key, only if the new key does not exist.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Renames `key` to `newkey` if `newkey` does not yet exist.
     * It returns an error under the same conditions as `RENAME`.
     * 
     * 
     * return an integer reply , specifically:
     * 
     * * `1` if `key` was renamed to `newkey`.
     * * `0` if `newkey` already exists.
     * 
     * Examples :
     * 
     *     SET mykey "Hello"
     *     SET myotherkey "World"
     *     RENAMENX mykey myotherkey
     *     GET myotherkey
     * 
     * @return true if `key` was renamed to `newkey`, false if `newkey` already exists
     */
    boolean renamenx(final K key, final K newkey);

    /**
     * Sort the elements in a list, set or sorted set.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(N+M\*log(M)) where N is the number of elements in the list or set to sort, and M the number of returned elements. When the elements are not sorted, complexity is currently O(N) as there is a copy step that will be avoided in next releases.
     * 
     * Returns or stores the elements contained in the
     * [list](/topics/data-types#lists), [set](/topics/data-types#set) or [sorted
     * set](/topics/data-types#sorted-sets) at `key`.  By default, sorting is numeric
     * and elements are compared by their value interpreted as double precision
     * floating point number.  This is `SORT` in its simplest form:
     * 
     *     SORT mylist
     * 
     * Assuming `mylist` is a list of numbers, this command will return the same list
     * with the elements sorted from small to large. In order to sort the numbers from
     * large to small, use the `!DESC` modifier:
     * 
     *     SORT mylist DESC
     * 
     * When `mylist` contains string values and you want to sort them lexicographically,
     * use the `!ALPHA` modifier:
     * 
     *     SORT mylist ALPHA
     * 
     * Redis is UTF-8 aware, assuming you correctly set the `!LC_COLLATE` environment
     * variable.
     * 
     * The number of returned elements can be limited using the `!LIMIT` modifier.
     * This modifier takes the `offset` argument, specifying the number of elements to
     * skip and the `count` argument, specifying the number of elements to return from
     * starting at `offset`.  The following example will return 10 elements of the
     * sorted version of `mylist`, starting at element 0 (`offset` is zero-based):
     * 
     *     SORT mylist LIMIT 0 10
     * 
     * Almost all modifiers can be used together. The following example will return
     * the first 5 elements, lexicographically sorted in descending order:
     * 
     *     SORT mylist LIMIT 0 5 ALPHA DESC
     * 
     * ## Sorting by external keys
     * 
     * Sometimes you want to sort elements using external keys as weights to compare
     * instead of comparing the actual elements in the list, set or sorted set.  Let's
     * say the list `mylist` contains the elements `1`, `2` and `3` representing
     * unique IDs of objects stored in `object_1`, `object_2` and `object_3`.  When
     * these objects have associated weights stored in `weight_1`, `weight_2` and
     * `weight_3`, `SORT` can be instructed to use these weights to sort `mylist` with
     * the following statement:
     * 
     *     SORT mylist BY weight_*
     * 
     * The `BY` option takes a pattern (equal to `weight_*` in this example) that is
     * used to generate the keys that are used for sorting.  These key names are
     * obtained substituting the first occurrence of `*` with the actual value of the
     * element in the list (`1`, `2` and `3` in this example).
     * 
     * ## Skip sorting the elements
     * 
     * The `!BY` option can also take a non-existent key, which causes `SORT` to skip
     * the sorting operation. This is useful if you want to retrieve external keys
     * (see the `!GET` option below) without the overhead of sorting.
     * 
     *     SORT mylist BY nosort
     * 
     * ## Retrieving external keys
     * 
     * Our previous example returns just the sorted IDs. In some cases, it is more
     * useful to get the actual objects instead of their IDs (`object_1`, `object_2`
     * and `object_3`).  Retrieving external keys based on the elements in a list, set
     * or sorted set can be done with the following command:
     * 
     *     SORT mylist BY weight_* GET object_*
     * 
     * The `!GET` option can be used multiple times in order to get more keys for
     * every element of the original list, set or sorted set.
     * 
     * It is also possible to `!GET` the element itself using the special pattern `#`:
     * 
     *     SORT mylist BY weight_* GET object_* GET #
     * 
     * ## Storing the result of a SORT operation
     * 
     * By default, `SORT` returns the sorted elements to the client. With the `!STORE`
     * option, the result will be stored as a list at the specified key instead of
     * being returned to the client.
     * 
     *     SORT mylist BY weight_* STORE resultkey
     * 
     * An interesting pattern using `SORT ... STORE` consists in associating an
     * `EXPIRE` timeout to the resulting key so that in applications where the result
     * of a `SORT` operation can be cached for some time. Other clients will use the
     * cached list instead of calling `SORT` for every request. When the key will
     * timeout, an updated version of the cache can be created by calling `SORT ... STORE` again.
     * 
     * Note that for correctly implementing this pattern it is important to avoid multiple
     * clients rebuilding the cache at the same time. Some kind of locking is needed here
     * (for instance using `SETNX`).
     * 
     * ## Using hashes in `!BY` and `!GET`
     * 
     * It is possible to use `!BY` and `!GET` options against hash fields with the
     * following syntax:
     * 
     *     SORT mylist BY weight_*->fieldname GET object_*->fieldname
     * 
     * The string `->` is used to separate the key name from the hash field name.
     * The key is substituted as documented above, and the hash stored at the
     * resulting key is accessed to retrieve the specified hash field.
     * 
     * 
     * @return a multi-bulk reply : list of sorted elements.
     * 
     */
    List<byte[]> sort(final K key, final SortCommand... sortCommands);

    /**
     * Get the time to live for a key.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns the remaining time to live of a key that has a timeout.  This
     * introspection capability allows a Redis client to check how many seconds a
     * given key will continue to be part of the dataset.
     * 
     * 
     * @return an integer reply : TTL in seconds or `-1` when `key` does not exist or does not have a timeout.
     * 
     * Examples :
     * 
     *     SET mykey "Hello"
     *     EXPIRE mykey 10
     *     TTL mykey
     * 
     */
    long ttl(final K key);

    /**
     * Determine the type stored at key.<br />
     * (generic operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns the string representation of the type of the value stored at `key`.
     * The different types that can be returned are: `string`, `list`, `set`, `zset`
     * and `hash`.
     * 
     * 
     * @return a status reply : type of `key`, or `none` when `key` does not exist.
     * 
     * Examples :
     * 
     *     SET key1 "value"
     *     LPUSH key2 "value"
     *     SADD key3 "value"
     *     TYPE key1
     *     TYPE key2
     *     TYPE key3
     * 
     */
    Status type(final K key);

    /**
     * Delete a hash field.<br />
     * (hash operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of fields that will be removed.
     * 
     * Removes the specified fields from the hash stored at `key`. Non-existing fields
     * are ignored. Non-existing keys are treated as empty hashes and this command
     * returns `0`.
     * 
     * For Redis versions 2.2 and below, this command is only available as a
     * non-variadic variant. To remove multiple fields from a hash in an atomic
     * fashion for those versions, use a `MULTI`/`EXEC` block.
     * 
     * 
     * @return an integer reply : The number of fields that were removed.
     * 
     * Examples :
     * 
     *     HSET myhash field1 "foo"
     *     HDEL myhash field1
     *     HDEL myhash field2
     * 
     */
    int hdel(final K key, final HK... fields);

    /**
     * Determine if a hash field exists.<br />
     * (hash operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns if `field` is an existing field in the hash stored at `key`.
     * 
     * 
     * return an integer reply , specifically:
     * 
     * * `1` if the hash contains `field`.
     * * `0` if the hash does not contain `field`, or `key` does not exist.
     * 
     * Examples :
     * 
     *     HSET myhash field1 "foo"
     *     HEXISTS myhash field1
     *     HEXISTS myhash field2
     * @return true if the hash contains `field`, false if the hash does not contain `field`, or `key` does not exist
     */
    boolean hexists(final K key, final HK field);

    /**
     * Get the value of a hash field.<br />
     * (hash operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns the value associated with `field` in the hash stored at `key`.
     * 
     * 
     * @return a bulk reply : the value associated with `field`, or `nil` when `field` is not
     * present in the hash or `key` does not exist.
     * 
     * Examples :
     * 
     *     HSET myhash field1 "foo"
     *     HGET myhash field1
     *     HGET myhash field2
     * 
     */
    HV hget(final K key, final HK field);

    /**
     * Get all the fields and values in a hash.<br />
     * (hash operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the size of the hash.
     * 
     * Returns all fields and values of the hash stored at `key`. In the returned
     * value, every field name is followed by its value, so the length
     * of the reply is twice the size of the hash.
     * 
     * 
     * @return a multi-bulk reply : list of fields and their values stored in the hash, or an
     * empty list when `key` does not exist.
     * 
     * Examples :
     * 
     *     HSET myhash field1 "Hello"
     *     HSET myhash field2 "World"
     *     HGETALL myhash
     * 
     */
    List<HV> hgetall(final K key);

    /**
     * Increment the integer value of a hash field by the given number.<br />
     * (hash operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Increments the number stored at `field` in the hash stored at `key` by
     * `increment`. If `key` does not exist, a new key holding a hash is created. If
     * `field` does not exist or holds a string that cannot be interpreted as integer,
     * the value is set to `0` before the operation is performed.
     * 
     * The range of values supported by `HINCRBY` is limited to 64 bit signed
     * integers.
     * 
     * 
     * @return an integer reply : the value at `field` after the increment operation.
     * 
     * Examples :
     * 
     * Since the `increment` argument is signed, both increment and decrement
     * operations can be performed:
     * 
     *     HSET myhash field 5
     *     HINCRBY myhash field 1
     *     HINCRBY myhash field -1
     *     HINCRBY myhash field -10
     * 
     */
    int hincrby(final K key, final HK field, final long increment);

    /**
     * Get all the fields in a hash.<br />
     * (hash operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the size of the hash.
     * 
     * Returns all field names in the hash stored at `key`.
     * 
     * 
     * @return a multi-bulk reply : list of fields in the hash, or an empty list when `key` does
     * not exist.
     * 
     * Examples :
     * 
     *     HSET myhash field1 "Hello"
     *     HSET myhash field2 "World"
     *     HKEYS myhash
     * 
     */
    List<HK> hkeys(final K key);

    /**
     * Get the number of fields in a hash.<br />
     * (hash operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns the number of fields contained in the hash stored at `key`.
     * 
     * 
     * @return an integer reply : number of fields in the hash, or `0` when `key` does not exist.
     * 
     * Examples :
     * 
     *     HSET myhash field1 "Hello"
     *     HSET myhash field2 "World"
     *     HLEN myhash
     * 
     */
    int hlen(final K key);

    /**
     * Get the values of all the given hash fields.<br />
     * (hash operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of fields being requested.
     * 
     * Returns the values associated with the specified `fields` in the hash stored at
     * `key`.
     * 
     * For every `field` that does not exist in the hash, a `nil` value is returned.
     * Because a non-existing keys are treated as empty hashes, running `HMGET`
     * against a non-existing `key` will return a list of `nil` values.
     * 
     * 
     * @return a multi-bulk reply : list of values associated with the given fields, in the same
     * order as they are requested.
     * 
     *     HSET myhash field1 "Hello"
     *     HSET myhash field2 "World"
     *     HMGET myhash field1 field2 nofield
     * 
     */
    List<HV> hmget(final K key, final HK... fields);

    /**
     * Set multiple hash fields to multiple values.<br />
     * (hash operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of fields being set.
     * 
     * Sets the specified fields to their respective values in the hash
     * stored at `key`. This command overwrites any existing fields in the hash.
     * If `key` does not exist, a new key holding a hash is created.
     * 
     * 
     * @return a status reply 
     * 
     * Examples :
     * 
     *     HMSET myhash field1 "Hello" field2 "World"
     *     HGET myhash field1
     *     HGET myhash field2
     * 
     */
    Status hmset(final K key, final Map<HK, HV> hash);

    /**
     * Set the string value of a hash field.<br />
     * (hash operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Sets `field` in the hash stored at `key` to `value`. If `key` does not exist, a
     * new key holding a hash is created. If `field` already exists in the hash, it
     * is overwritten.
     * 
     * 
     * @return an integer reply , specifically:
     * 
     * * `1` if `field` is a new field in the hash and `value` was set.
     * * `0` if `field` already exists in the hash and the value was updated.
     * 
     * Examples :
     * 
     *     HSET myhash field1 "Hello"
     *     HGET myhash field1
     * 
     */
    boolean hset(final K key, final HK field, final HV value);

    /**
     * Set the value of a hash field, only if the field does not exist.<br />
     * (hash operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Sets `field` in the hash stored at `key` to `value`, only if `field` does not
     * yet exist. If `key` does not exist, a new key holding a hash is created. If
     * `field` already exists, this operation has no effect.
     * 
     * 
     * return an integer reply , specifically:
     * 
     * * `1` if `field` is a new field in the hash and `value` was set.
     * * `0` if `field` already exists in the hash and no operation was performed.
     * 
     * Examples :
     * 
     *     HSETNX myhash field "Hello"
     *     HSETNX myhash field "World"
     *     HGET myhash field
     * @return
     *  true if `field` is a new field in the hash and `value` was set
     *  false if `field` already exists in the hash and no operation was performed
     */
    boolean hsetnx(final K key, final HK field, final HV value);

    /**
     * Get all the values in a hash.<br />
     * (hash operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the size of the hash.
     * 
     * Returns all values in the hash stored at `key`.
     * 
     * 
     * @return a multi-bulk reply : list of values in the hash, or an empty list when `key` does
     * not exist.
     * 
     * Examples :
     * 
     *     HSET myhash field1 "Hello"
     *     HSET myhash field2 "World"
     *     HVALS myhash
     * 
     */
    List<HV> hvals(final K key);

    /**
     * Remove and get the first element in a list, or block until one is available.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * `BLPOP` is a blocking list pop primitive.  It is the blocking version of `LPOP`
     * because it blocks the connection when there are no elements to pop from any of
     * the given lists. An element is popped from the head of the first list that is
     * non-empty, with the given keys being checked in the order that they are given.
     * 
     * ## Non-blocking behavior
     * 
     * When `BLPOP` is called, if at least one of the specified keys contain a
     * non-empty list, an element is popped from the head of the list and returned to
     * the caller together with the `key` it was popped from.
     * 
     * Keys are checked in the order that they are given. Let's say that the key
     * `list1` doesn't exist and `list2` and `list3` hold non-empty lists. Consider
     * the following command:
     * 
     *     BLPOP list1 list2 list3 0
     * 
     * `BLPOP` guarantees to return an element from the list stored at `list2` (since
     * it is the first non empty list when checking `list1`, `list2` and `list3` in
     * that order).
     * 
     * ## Blocking behavior
     * 
     * If none of the specified keys exist or contain non-empty lists, `BLPOP` blocks
     * the connection until another client performs an `LPUSH` or `RPUSH` operation
     * against one of the lists.
     * 
     * Once new data is present on one of the lists, the client returns with the name
     * of the key unblocking it and the popped value.
     * 
     * When `BLPOP` causes a client to block and a non-zero timeout is specified, the
     * client will unblock returning a `nil` multi-bulk value when the specified
     * timeout has expired without a push operation against at least one of the
     * specified keys.
     * 
     * The timeout argument is interpreted as an integer value. A timeout of zero can
     * be used to block indefinitely.
     * 
     * ## Multiple clients blocking for the same keys
     * 
     * Multiple clients can block for the same key. They are put into
     * a queue, so the first to be served will be the one that started to wait
     * earlier, in a first-`!BLPOP` first-served fashion.
     * 
     * ## `!BLPOP` inside a `!MULTI`/`!EXEC` transaction
     * 
     * `BLPOP` can be used with pipelining (sending multiple commands and reading the
     * replies in batch), but it does not make sense to use `BLPOP` inside a
     * `MULTI`/`EXEC` block. This would require blocking the entire server in order to
     * execute the block atomically, which in turn does not allow other clients to
     * perform a push operation.
     * 
     * The behavior of `BLPOP` inside `MULTI`/`EXEC` when the list is empty is to
     * return a `nil` multi-bulk reply, which is the same thing that happens when the
     * timeout is reached. If you like science fiction, think of time flowing at
     * infinite speed inside a `MULTI`/`EXEC` block.
     * 
     * 
     * @return a multi-bulk reply : specifically:
     * 
     * * A `nil` multi-bulk when no element could be popped and the timeout expired.
     * * A two-element multi-bulk with the first element being the name of the key where an element
     *   was popped and the second element being the value of the popped element.
     * 
     */
    List<LV> blpop(final int timeout, final LK... keys);

    /**
     * Remove and get the last element in a list, or block until one is available.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * `BRPOP` is a blocking list pop primitive.  It is the blocking version of `RPOP`
     * because it blocks the connection when there are no elements to pop from any of
     * the given lists. An element is popped from the tail of the first list that is
     * non-empty, with the given keys being checked in the order that they are given.
     * 
     * See `BLPOP` for the exact semantics. `BRPOP` is identical to `BLPOP`, apart
     * from popping from the tail of a list instead of the head of a list.
     * 
     * 
     * @return a multi-bulk reply : specifically:
     * 
     * * A `nil` multi-bulk when no element could be popped and the timeout expired.
     * * A two-element multi-bulk with the first element being the name of the key where an element
     *   was popped and the second element being the value of the popped element.
     * 
     */
    List<LV> brpop(final int timeout, final LK... keys);

    /**
     * Pop a value from a list, push it to another list and return it; or block until one is available.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(1).
     * 
     * `BRPOPLPUSH` is the blocking variant of `RPOPLPUSH`. When `source`
     * contains elements, this command behaves exactly like `RPOPLPUSH`.  When
     * `source` is empty, Redis will block the connection until another client
     * pushes to it or until `timeout` is reached. A `timeout` of zero can be
     * used to block indefinitely.
     * 
     * See `RPOPLPUSH` for more information.
     * 
     * 
     * @return a bulk reply : the element being popped from `source` and pushed to
     * `destination`. If `timeout` is reached, a @nil-reply is returned.
     */
    LV brpoplpush(final int timeout, final LK source, final LK destination);

    /**
     * Get an element from a list by its index.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of elements to traverse to get to the element
     * at `index`. This makes asking for the first or the last
     * element of the list O(1).
     * 
     * Returns the element at index `index` in the list stored at `key`.
     * The index is zero-based, so `0` means the first element, `1` the second
     * element and so on. Negative indices can be used to designate elements
     * starting at the tail of the list. Here, `-1` means the last element, `-2` means
     * the penultimate and so forth.
     * 
     * When the value at `key` is not a list, an error is returned.
     * 
     * 
     * @return a bulk reply : the requested element, or `nil` when `index` is out of range.
     * 
     * Examples :
     * 
     *     LPUSH mylist "World"
     *     LPUSH mylist "Hello"
     *     LINDEX mylist 0
     *     LINDEX mylist -1
     *     LINDEX mylist 3
     * 
     */
    LV lindex(final LK key, final int index);

    /**
     * Insert an element before or after another element in a list.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of elements to traverse before seeing the value
     * `pivot`. This means that inserting somewhere on the left end on the list (head)
     * can be considered O(1) and inserting somewhere on the right end (tail) is O(N).
     * 
     * Inserts `value` in the list stored at `key` either before or after the
     * reference value `pivot`.
     * 
     * When `key` does not exist, it is considered an empty list and no operation is
     * performed.
     * 
     * An error is returned when `key` exists but does not hold a list value.
     * 
     * 
     * @return an integer reply : the length of the list after the insert operation, or `-1` when
     * the value `pivot` was not found.
     * 
     * Examples :
     * 
     *     RPUSH mylist "Hello"
     *     RPUSH mylist "World"
     *     LINSERT mylist BEFORE "World" "There"
     *     LRANGE mylist 0 -1
     * 
     */
    int linsert(final LK key, final LINSERT_POSITION where, final LV pivot, final LV value);

    /**
     * Get the length of a list.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns the length of the list stored at `key`.
     * If `key` does not exist, it is interpreted as an empty list and `0` is returned.
     * An error is returned when the value stored at `key` is not a list.
     * 
     * 
     * @return an integer reply : the length of the list at `key`.
     * 
     * Examples :
     * 
     *     LPUSH mylist "World"
     *     LPUSH mylist "Hello"
     *     LLEN mylist
     * 
     */
    int llen(final LK key);

    /**
     * Remove and get the first element in a list.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Removes and returns the first element of the list stored at `key`.
     * 
     * 
     * @return a bulk reply : the value of the first element, or `nil` when `key` does not exist.
     * 
     * Examples :
     * 
     *     RPUSH mylist "one"
     *     RPUSH mylist "two"
     *     RPUSH mylist "three"
     *     LPOP mylist
     *     LRANGE mylist 0 -1
     * 
     */
    LV lpop(final LK key);

    /**
     * Prepend a value to a list.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Inserts `value` at the head of the list stored at `key`.  If `key` does not
     * exist, it is created as empty list before performing the push operation.
     * When `key` holds a value that is not a list, an error is returned.
     * 
     * 
     * @return an integer reply : the length of the list after the push operation.
     * 
     * History
     * ---
     * 
     * Up until Redis 2.3, `LPUSH` accepted a single `value`.
     * 
     * Examples :
     * 
     *     LPUSH mylist "World"
     *     LPUSH mylist "Hello"
     *     LRANGE mylist 0 -1
     * 
     */
    int lpush(final LK key, final LV value);

    /**
     * Prepend a value to a list, only if the list exists.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Inserts `value` at the head of the list stored at `key`, only if `key`
     * already exists and holds a list. In contrary to `LPUSH`, no operation will
     * be performed when `key` does not yet exist.
     * 
     * 
     * @return an integer reply : the length of the list after the push operation.
     * 
     * Examples :
     * 
     *     LPUSH mylist "World"
     *     LPUSHX mylist "Hello"
     *     LPUSHX myotherlist "Hello"
     *     LRANGE mylist 0 -1
     *     LRANGE myotherlist 0 -1
     * 
     */
    int lpushx(final LK key, final LV value);

    /**
     * Get a range of elements from a list.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(S+N) where S is the `start` offset and N is the number of elements in the
     * specified range.
     * 
     * Returns the specified elements of the list stored at `key`.  The offsets
     * `start` and `end` are zero-based indexes, with `0` being the first element of
     * the list (the head of the list), `1` being the next element and so on.
     * 
     * These offsets can also be negative numbers indicating offsets starting at the
     * end of the list. For example, `-1` is the last element of the list, `-2` the
     * penultimate, and so on.
     * 
     * ## Consistency with range functions in various programming languages
     * 
     * Note that if you have a list of numbers from 0 to 100, `LRANGE list 0 10` will
     * return 11 elements, that is, the rightmost item is included. This **may or may
     * not** be consistent with behavior of range-related functions in your
     * programming language of choice (think Ruby's `Range.new`, `Array#slice` or
     * Python's `range()` function).
     * 
     * ## Out-of-range indexes
     * 
     * Out of range indexes will not produce an error. If `start` is larger than the
     * end of the list, or `start > end`, an empty list is returned.  If `end` is
     * larger than the actual end of the list, Redis will treat it like the last
     * element of the list.
     * 
     * 
     * @return a multi-bulk reply : list of elements in the specified range.
     * 
     * Examples :
     * 
     *     RPUSH mylist "one"
     *     RPUSH mylist "two"
     *     RPUSH mylist "three"
     *     LRANGE mylist 0 0
     *     LRANGE mylist -3 2
     *     LRANGE mylist -100 100
     *     LRANGE mylist 5 10
     * 
     */
    List<LV> lrange(final LK key, final int start, final int stop);

    /**
     * Remove elements from a list.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the length of the list.
     * 
     * Removes the first `count` occurrences of elements equal to `value` from the
     * list stored at `key`. The `count` argument influences the operation in the
     * following ways:
     * 
     * * `count > 0`: Remove elements equal to `value` moving from head to tail.
     * * `count < 0`: Remove elements equal to `value` moving from tail to head.
     * * `count = 0`: Remove all elements equal to `value`.
     * 
     * For example, `LREM list -2 "hello"` will remove the last two occurrences of
     * `"hello"` in the list stored at `list`.
     * 
     * Note that non-existing keys are treated like empty lists, so when `key` does
     * not exist, the command will always return `0`.
     * 
     * 
     * @return an integer reply : the number of removed elements.
     * 
     * Examples :
     * 
     *     RPUSH mylist "hello"
     *     RPUSH mylist "hello"
     *     RPUSH mylist "foo"
     *     RPUSH mylist "hello"
     *     LREM mylist -2 "hello"
     *     LRANGE mylist 0 -1
     * 
     */
    int lrem(final LK key, final int count, final LV value);

    /**
     * Set the value of an element in a list by its index.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the length of the list. Setting either the first or the last
     * element of the list is O(1).
     * 
     * Sets the list element at `index` to `value`. For more information on the
     * `index` argument, see `LINDEX`.
     * 
     * An error is returned for out of range indexes.
     * 
     * 
     * @return a status reply 
     * 
     * Examples :
     * 
     *     RPUSH mylist "one"
     *     RPUSH mylist "two"
     *     RPUSH mylist "three"
     *     LSET mylist 0 "four"
     *     LSET mylist -2 "five"
     *     LRANGE mylist 0 -1
     * 
     */
    Status lset(final LK key, final int index, final LV value);

    /**
     * Trim a list to the specified range.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of elements to be removed by the operation.
     * 
     * Trim an existing list so that it will contain only the specified range of
     * elements specified. Both `start` and `stop` are zero-based indexes, where `0`
     * is the first element of the list (the head), `1` the next element and so on.
     * 
     * For example: `LTRIM foobar 0 2` will modify the list stored at `foobar` so that
     * only the first three elements of the list will remain.
     * 
     * `start` and `end` can also be negative numbers indicating offsets from the end
     * of the list, where `-1` is the last element of the list, `-2` the penultimate
     * element and so on.
     * 
     * Out of range indexes will not produce an error: if `start` is larger than the
     * end of the list, or `start > end`, the result will be an empty list (which
     * causes `key` to be removed).  If `end` is larger than the end of the list,
     * Redis will treat it like the last element of the list.
     * 
     * A common use of `LTRIM` is together with `LPUSH`/`RPUSH`. For example:
     * 
     *     LPUSH mylist someelement
     *     LTRIM mylist 0 99
     * 
     * This pair of commands will push a new element on the list, while making sure
     * that the list will not grow larger than 100 elements. This is very useful when
     * using Redis to store logs for example. It is important to note that when used
     * in this way `LTRIM` is an O(1) operation because in the average case just one
     * element is removed from the tail of the list.
     * 
     * 
     * @return a status reply 
     * 
     * Examples :
     * 
     *     RPUSH mylist "one"
     *     RPUSH mylist "two"
     *     RPUSH mylist "three"
     *     LTRIM mylist 1 -1
     *     LRANGE mylist 0 -1
     * 
     */
    Status ltrim(final LK key, final int start, final int stop);

    /**
     * Remove and get the last element in a list.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Removes and returns the last element of the list stored at `key`.
     * 
     * 
     * @return a bulk reply : the value of the last element, or `nil` when `key` does not exist.
     * 
     * Examples :
     * 
     *     RPUSH mylist "one"
     *     RPUSH mylist "two"
     *     RPUSH mylist "three"
     *     RPOP mylist
     *     LRANGE mylist 0 -1
     * 
     */
    LV rpop(final LK key);

    /**
     * Remove the last element in a list, append it to another list and return it.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Atomically returns and removes the last element (tail) of the list stored at
     * `source`, and pushes the element at the first element (head) of the list stored
     * at `destination`.
     * 
     * For example: consider `source` holding the list `a,b,c`, and `destination`
     * holding the list `x,y,z`. Executing `RPOPLPUSH` results in `source` holding
     * `a,b` and `destination` holding `c,x,y,z`.
     * 
     * If `source` does not exist, the value `nil` is returned and no operation is
     * performed. If `source` and `destination` are the same, the operation is
     * equivalent to removing the last element from the list and pushing it as first
     * element of the list, so it can be considered as a list rotation command.
     * 
     * 
     * @return a bulk reply : the element being popped and pushed.
     * 
     * Examples :
     * 
     *     RPUSH mylist "one"
     *     RPUSH mylist "two"
     *     RPUSH mylist "three"
     *     RPOPLPUSH mylist myotherlist
     *     LRANGE mylist 0 -1
     *     LRANGE myotherlist 0 -1
     * 
     * ## Design pattern: safe queues
     * 
     * Redis lists are often used as queues in order to exchange messages between
     * different programs. A program can add a message performing an `LPUSH` operation
     * against a Redis list (we call this program the _Producer_), while another program
     * (that we call _Consumer_) can process the messages performing an `RPOP` command
     * in order to start reading the messages starting at the oldest.
     * 
     * Unfortunately, if a _Consumer_ crashes just after an `RPOP` operation, the message
     * is lost. `RPOPLPUSH` solves this problem since the returned message is
     * added to another backup list. The _Consumer_ can later remove the message
     * from the backup list using the `LREM` command when the message was correctly
     * processed.
     * 
     * Another process (that we call _Helper_), can monitor the backup list to check for
     * timed out entries to re-push against the main queue.
     * 
     * ## Design pattern: server-side O(N) list traversal
     * 
     * Using `RPOPLPUSH` with the same source and destination key, a process can
     * visit all the elements of an N-elements list in O(N) without transferring
     * the full list from the server to the client in a single `LRANGE` operation.
     * Note that a process can traverse the list even while other processes
     * are actively pushing to the list, and still no element will be skipped.
     * 
     */
    LV rpoplpush(final LK source, final LK destination);

    /**
     * Append a value to a list.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Inserts `value` at the tail of the list stored at `key`.  If `key` does not
     * exist, it is created as empty list before performing the push operation.
     * When `key` holds a value that is not a list, an error is returned.
     * 
     * 
     * @return an integer reply : the length of the list after the push operation.
     * 
     * History
     * ---
     * 
     * Up until Redis 2.3, `RPUSH` accepted a single `value`.
     * 
     * Examples :
     * 
     *     RPUSH mylist "hello"
     *     RPUSH mylist "world"
     *     LRANGE mylist 0 -1
     * 
     */
    int rpush(final LK key, final LV value);

    /**
     * Append a value to a list, only if the list exists.<br />
     * (list operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Inserts `value` at the tail of the list stored at `key`, only if `key`
     * already exists and holds a list. In contrary to `RPUSH`, no operation will
     * be performed when `key` does not yet exist.
     * 
     * 
     * @return an integer reply : the length of the list after the push operation.
     * 
     * Examples :
     * 
     *     RPUSH mylist "Hello"
     *     RPUSHX mylist "World"
     *     RPUSHX myotherlist "World"
     *     LRANGE mylist 0 -1
     *     LRANGE myotherlist 0 -1
     * 
     */
    int rpushx(final LK key, final LV value);

    /**
     * Listen for messages published to channels matching the given patterns.<br />
     * (pubsub operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of patterns the client is already subscribed to.
     * 
     * Subscribes the client to the given patterns.
     */
    UpdateMe psubscribe(final [pattern] [pattern]);

    /**
     * Post a message to a channel.<br />
     * (pubsub operation)<br /><br />
     * Complexity :
     * 
     * O(N+M) where N is the number of clients subscribed to the receiving
     * channel and M is the total number of subscribed patterns (by any
     * client).
     * 
     * Posts a message to the given channel.
     * 
     * 
     * @return an integer reply : the number of clients that received the message.
     */
    int publish(final PSK channel, final PSV message);

    /**
     * Stop listening for messages posted to channels matching the given patterns.<br />
     * (pubsub operation)<br /><br />
     * Complexity :
     * 
     * O(N+M) where N is the number of patterns the client is already
     * subscribed and M is the number of total patterns subscribed in the
     * system (by any client).
     * 
     * Unsubscribes the client from the given patterns, or from all of them if
     * none is given.
     * 
     * When no patters are specified, the client is unsubscribed from all
     * the previously subscribed patterns. In this case, a message for every
     * unsubscribed pattern will be sent to the client.
     */
    UpdateMe punsubscribe(final pattern pattern);

    /**
     * Listen for messages published to the given channels.<br />
     * (pubsub operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of channels to subscribe to.
     * 
     * Subscribes the client to the specified channels.
     * 
     * Once the client enters the subscribed state it is not supposed to issue
     * any other commands, except for additional `SUBSCRIBE`, `PSUBSCRIBE`,
     * `UNSUBSCRIBE` and `PUNSUBSCRIBE` commands.
     */
    UpdateMe subscribe(final [string] [channel]);

    /**
     * Stop listening for messages posted to the given channels.<br />
     * (pubsub operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of clients already subscribed to a channel.
     * 
     * Unsubscribes the client from the given channels, or from all of them if
     * none is given.
     * 
     * When no channels are specified, the client is unsubscribed from all
     * the previously subscribed channels. In this case, a message for every
     * unsubscribed channel will be sent to the client.
     */
    UpdateMe unsubscribe(final string channel);

    /**
     * Asynchronously rewrite the append-only file.<br />
     * (server operation)<br /><br />
     * Rewrites the [append-only file](/topics/persistence#append-only-file) to reflect the current dataset in memory.
     * 
     * If `BGREWRITEAOF` fails, no data gets lost as the old AOF will be untouched.
     * 
     * 
     * @return a status reply : always `OK`.
     */
    Status bgrewriteaof();

    /**
     * Asynchronously save the dataset to disk.<br />
     * (server operation)<br /><br />
     * 
     * 
     * Save the DB in background. The OK code is immediately returned.
     * Redis forks, the parent continues to server the clients, the child
     * saves the DB on disk then exit. A client my be able to check if the
     * operation succeeded using the `LASTSAVE` command.
     * 
     * 
     * @return a status reply 
     */
    Status bgsave();

    /**
     * Get the value of a configuration parameter.<br />
     * (server operation)<br /><br />
     * Complexity :
     * 
     * Not applicable.
     * 
     * 
     * The `CONFIG GET` command is used to read the configuration parameters of a running
     * Redis server. Not all the configuration parameters are supported.
     * The symmetric command used to alter the configuration at run time is
     * `CONFIG SET`.
     * 
     * `CONFIG GET` takes a single argument, that is glob style pattern. All the
     * configuration parameters matching this parameter are reported as a
     * list of key-value pairs. Example:
     * 
     *     redis> config get *max-*-entries*
     *     1) "hash-max-zipmap-entries"
     *     2) "512"
     *     3) "list-max-ziplist-entries"
     *     4) "512"
     *     5) "set-max-intset-entries"
     *     6) "512"
     * 
     * You can obtain a list of all the supported configuration parameters typing
     * `CONFIG GET *` in an open `redis-cli` prompt.
     * 
     * All the supported parameters have the same meaning of the equivalent
     * configuration parameter used in the [redis.conf](http://github.com/antirez/redis/raw/2.2/redis.conf) file, with the following important differences:
     * 
     * * Where bytes or other quantities are specified, it is not possible to use the redis.conf abbreviated form (10k 2gb ... and so forth), everything should be specified as a well formed 64 bit integer, in the base unit of the configuration directive.
     * * The save parameter is a single string of space separated integers. Every pair of integers represent a seconds/modifications threshold.
     * 
     * For instance what in redis.conf looks like:
     * 
     *     save 900 1
     *     save 300 10
     * 
     * that means, save after 900 seconds if there is at least 1 change to the
     * dataset, and after 300 seconds if there are at least 10 changes to the
     * datasets, will be reported by `CONFIG GET` as "900 1 300 10".
     * 
     * 
     * The return type of the command is a @bulk-reply.
     */
    UpdateMe config get(final string parameter);

    /**
     * Reset the stats returned by INFO.<br />
     * (server operation)<br /><br />
     * Complexity :
     * 
     * O(1).
     * 
     * Resets the statistics reported by Redis using the `INFO` command.
     * 
     * These are the counters that are reset:
     * 
     * * Keyspace hits
     * * Keyspace misses
     * * Number of commands processed
     * * Number of connections received
     * * Number of expired keys
     * 
     * 
     * @return a status reply : always `OK`.
     */
    Status config resetstat();

    /**
     * Set a configuration parameter to the given value.<br />
     * (server operation)<br /><br />
     * Complexity :
     * 
     * Not applicable.
     * 
     * 
     * The `CONFIG SET` command is used in order to reconfigure the server at runtime
     * without the need to restart Redis. You can change both trivial parameters or
     * switch from one to another persistence option using this command.
     * 
     * The list of configuration parameters supported by `CONFIG SET` can be
     * obtained issuing a `CONFIG GET *` command, that is the symmetrical command
     * used to obtain information about the configuration of a running
     * Redis instance.
     * 
     * All the configuration parameters set using `CONFIG SET` are immediately loaded
     * by Redis that will start acting as specified starting from the next command
     * executed.
     * 
     * All the supported parameters have the same meaning of the equivalent
     * configuration parameter used in the [redis.conf](http://github.com/antirez/redis/raw/2.2/redis.conf) file, with the following important differences:
     * 
     * * Where bytes or other quantities are specified, it is not possible to use the redis.conf abbreviated form (10k 2gb ... and so forth), everything should be specified as a well formed 64 bit integer, in the base unit of the configuration directive.
     * * The save parameter is a single string of space separated integers. Every pair of integers represent a seconds/modifications threshold.
     * 
     * For instance what in redis.conf looks like:
     * 
     *     save 900 1
     *     save 300 10
     * 
     * that means, save after 900 seconds if there is at least 1 change to the
     * dataset, and after 300 seconds if there are at least 10 changes to the
     * datasets, should be set using `CONFIG SET` as "900 1 300 10".
     * 
     * It is possible to switch persistence form .rdb snapshotting to append only file
     * (and the other way around) using the `CONFIG SET` command. For more information
     * about how to do that please check [persistence page](/topics/persistence).
     * 
     * In general what you should know is that setting the *appendonly* parameter to
     * *yes* will start a background process to save the initial append only file
     * (obtained from the in memory data set), and will append all the subsequent
     * commands on the append only file, thus obtaining exactly the same effect of
     * a Redis server that started with AOF turned on since the start.
     * 
     * You can have both the AOF enabled with .rdb snapshotting if you want, the
     * two options are not mutually exclusive.
     * 
     * 
     * @return a status reply : `OK` when the configuration was set properly. Otherwise an error is returned.
     */
    Status config set(final string parameter, final string value);

    /**
     * Return the number of keys in the selected database.<br />
     * (server operation)<br /><br />
     * 
     * 
     * Return the number of keys in the currently selected database.
     * 
     * 
     * @return an integer reply 
     */
    long dbsize();

    /**
     * Get debugging information about a key.<br />
     * (server operation)<br /><br />
     * Complexity :
     * 
     * 
     * Examples :
     * 
     */
    UpdateMe debug object(final K key);

    /**
     * Make the server crash.<br />
     * (server operation)<br /><br />
     * Complexity :
     * 
     * 
     * Examples :
     * 
     */
    UpdateMe debug segfault();

    /**
     * Remove all keys from all databases.<br />
     * (server operation)<br /><br />
     * 
     * 
     * Delete all the keys of all the existing databases, not just the currently selected one. This command never fails.
     * 
     * 
     * @return a status reply 
     */
    Status flushall();

    /**
     * Remove all keys from the current database.<br />
     * (server operation)<br /><br />
     * 
     * 
     * Delete all the keys of the currently selected DB. This command never fails.
     * 
     * 
     * @return a status reply 
     */
    Status flushdb();

    /**
     * Get information and statistics about the server.<br />
     * (server operation)<br /><br />
     * The `INFO` command returns information and statistics about the server
     * in format that is simple to parse by computers and easy to red by humans.
     * 
     * 
     * @return a bulk reply : in the following format (compacted for brevity):
     * 
     *     redis_version:2.2.2
     *     uptime_in_seconds:148
     *     used_cpu_sys:0.01
     *     used_cpu_user:0.03
     *     used_memory:768384
     *     used_memory_rss:1536000
     *     mem_fragmentation_ratio:2.00
     *     changes_since_last_save:118
     *     keyspace_hits:174
     *     keyspace_misses:37
     *     allocation_stats:4=56,8=312,16=1498,...
     *     db0:keys=1240,expires=0
     * 
     * All the fields are in the form of `field:value` terminated by `\r\n`.
     * 
     * ## Notes
     * 
     * * `used_memory` is the total number of bytes allocated by Redis using its
     *   allocator (either standard `libc` `malloc`, or an alternative allocator such as
     *   [`tcmalloc`][1]
     * 
     * * `used_memory_rss` is the number of bytes that Redis allocated as seen by the
     *   operating system. Optimally, this number is close to `used_memory` and there
     *   is little memory fragmentation. This is the number reported by tools such as
     *   `top` and `ps`. A large difference between these numbers means there is
     *   memory fragmentation. Because Redis does not have control over how its
     *   allocations are mapped to memory pages, `used_memory_rss` is often the result
     *   of a spike in memory usage. The ratio between `used_memory_rss` and
     *   `used_memory` is given as `mem_fragmentation_ratio`.
     * 
     * * `changes_since_last_save` refers to the number of operations that produced
     *   some kind of change in the dataset since the last time either `SAVE` or
     *   `BGSAVE` was called.
     * 
     * * `allocation_stats` holds a histogram containing the number of allocations of
     *   a certain size (up to 256). This provides a means of introspection for the
     *   type of allocations performed by Redis at run time.
     * 
     * [1]: http://code.google.com/p/google-perftools/
     */
    byte[] info();

    /**
     * Get the UNIX time stamp of the last successful save to disk.<br />
     * (server operation)<br /><br />
     * 
     * 
     * Return the UNIX TIME of the last DB save executed with success.
     * A client may check if a `BGSAVE` command succeeded reading the `LASTSAVE`
     * value, then issuing a `BGSAVE` command and checking at regular intervals
     * every N seconds if `LASTSAVE` changed.
     * 
     * 
     * @return an integer reply : an UNIX time stamp.
     */
    long lastsave();

    /**
     * Listen for all requests received by the server in real time.<br />
     * (server operation)<br /><br />
     * 
     * 
     * `MONITOR` is a debugging command that outputs the whole sequence of commands
     * received by the Redis server. is very handy in order to understand
     * what is happening into the database. This command is used directly
     * via telnet.
     *     % telnet 127.0.0.1 6379
     *     Trying 127.0.0.1...
     *     Connected to segnalo-local.com.
     *     Escape character is '^]'.
     *     MONITOR
     *     +OK
     *     monitor
     *     keys *
     *     dbsize
     *     set x 6
     *     foobar
     *     get x
     *     del x
     *     get x
     *     set key_x 5
     *     hello
     *     set key_y 5
     *     hello
     *     set key_z 5
     *     hello
     *     set foo_a 5
     *     hello
     * The ability to see all the requests processed by the server is useful in order
     * to spot bugs in the application both when using Redis as a database and as
     * a distributed caching system.
     * 
     * In order to end a monitoring session just issue a `QUIT` command by hand.
     * 
     * 
     * **Non standard return value**, just dumps the received commands in an infinite
     * flow.
     */
    UpdateMe monitor();

    /**
     * Synchronously save the dataset to disk.<br />
     * (server operation)<br /><br />
     * Complexity :
     * 
     * 
     * Examples :
     * 
     */
    UpdateMe save();

    /**
     * Synchronously save the dataset to disk and then shut down the server.<br />
     * (server operation)<br /><br />
     * 
     * 
     * Stop all the clients, save the DB, then quit the server. This commands
     * makes sure that the DB is switched off without the lost of any data.
     * This is not guaranteed if the client uses simply `SAVE` and then
     * `QUIT` because other clients may alter the DB data between the two
     * commands.
     * 
     * 
     * @return a status reply  on error. On success nothing is returned since the server
     * quits and the connection is closed.
     */
    Status shutdown();

    /**
     * Make the server a slave of another instance, or promote it as master.<br />
     * (server operation)<br /><br />
     * 
     * The `SLAVEOF` command can change the replication settings of a slave on the fly.
     * If a Redis server is already acting as slave, the command `SLAVEOF` NO ONE
     * will turn off the replication turning the Redis server into a MASTER.
     * In the proper form `SLAVEOF` hostname port will make the server a slave of the
     * specific server listening at the specified hostname and port.
     * 
     * If a server is already a slave of some master, `SLAVEOF` hostname port will
     * stop the replication against the old server and start the synchronization
     * against the new one discarding the old dataset.
     * 
     * The form `SLAVEOF` no one will stop replication turning the server into a
     * MASTER but will not discard the replication. So if the old master stop working
     * it is possible to turn the slave into a master and set the application to
     * use the new master in read/write. Later when the other Redis server will be
     * fixed it can be configured in order to work as slave.
     * 
     * 
     * @return a status reply 
     */
    Status slaveof(final string host, final string port);

    /**
     * Manages the Redis slow queries log.<br />
     * (server operation)<br /><br />
     * This command is used in order to read and reset the Redis slow queries log.
     * 
     * ## Redis slow log overview
     * 
     * The Redis Slow Log is a system to log queries that exceeded a specified
     * execution time. The execution time does not include the I/O operations
     * like talking with the client, sending the reply and so forth,
     * but just the time needed to actually execute the command (this is the only
     * stage of command execution where the thread is blocked and can not serve
     * other requests in the meantime).
     * 
     * You can configure the slow log with two parameters: one tells Redis
     * what is the execution time, in microseconds, to exceed in order for the
     * command to get logged, and the other parameter is the length of the
     * slow log. When a new command is logged and the slow log is already at its
     * maximum length, the oldest one is removed from the queue of logged commands
     * in order to make space.
     * 
     * The configuration can be done both editing the redis.conf file or 
     * while the server is running using
     * the [CONFIG GET](/commands/config-get) and [CONFIG SET](/commands/config-set)
     * commands.
     * 
     * ## Reding the slow log
     * 
     * The slow log is accumulated in memory, so no file is written with information
     * about the slow command executions. This makes the slow log remarkably fast
     * at the point that you can enable the logging of all the commands (setting the
     * *slowlog-log-slower-than* config parameter to zero) with minor performance
     * hit.
     * 
     * To read the slow log the **SLOWLOG GET** command is used, that returns every
     * entry in the slow log. It is possible to return only the N most recent entries
     * passing an additional argument to the command (for instance **SLOWLOG GET 10**).
     * 
     * Note that you need a recent version of redis-cli in order to read the slow
     * log output, since this uses some feature of the protocol that was not
     * formerly implemented in redis-cli (deeply nested multi bulk replies).
     * 
     * ## Output format
     * 
     *     redis 127.0.0.1:6379> slowlog get 2
     *     1) 1) (integer) 14
     *        2) (integer) 1309448221
     *        3) (integer) 15
     *        4) 1) "ping"
     *     2) 1) (integer) 13
     *        2) (integer) 1309448128
     *        3) (integer) 30
     *        4) 1) "slowlog"
     *           2) "get"
     *           3) "100"
     * 
     * Every entry is composed of four fields:
     * * An unique progressive identifier for every slow log entry.
     * * The unix timestamp at which the logged command was processed.
     * * The amount of time needed for its execution, in microseconds.
     * * The array composing the arguments of the command.
     * 
     * The entries unique ID can be used in order to void processing slow log entries
     * multiple times (for instance you may have a scripting sending you an email
     * alert for every new slow log entry).
     * 
     * The ID is never reset in the course of the Redis server execution, only a
     * server restart will reset it.
     * 
     * ## Obtaining the current length of the slow log
     * 
     * It is possible to get just the length of the slow log using the command **SLOWLOG LEN**.
     * 
     * ## Resetting the slow log.
     * 
     * You can reset the slow log using the **SLOWLOG RESET** command.
     * Once deleted the information is lost forever.
     */
    UpdateMe slowlog(final string subcommand, final string argument);

    /**
     * Internal command used for replication.<br />
     * (server operation)<br /><br />
     * Complexity :
     * 
     * 
     * Examples :
     * 
     */
    UpdateMe sync();

    /**
     * Add a member to a set.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Add `member` to the set stored at `key`. If `member` is already a member of
     * this set, no operation is performed. If `key` does not exist, a new set is
     * created with `member` as its sole member.
     * 
     * An error is returned when the value stored at `key` is not a set.
     * 
     * 
     * @return an integer reply : the number of elements actually added to the set.
     * 
     * History
     * ---
     * 
     * Up until Redis 2.3, `SADD` accepted a single `member`.
     * 
     * Examples :
     * 
     *     SADD myset "Hello"
     *     SADD myset "World"
     *     SADD myset "World"
     *     SMEMBERS myset
     * 
     */
    long sadd(final K key, final string member);

    /**
     * Get the number of members in a set.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns the set cardinality (number of elements) of the set stored at `key`.
     * 
     * 
     * @return an integer reply : the cardinality (number of elements) of the set, or `0` if
     * `key` does not exist.
     * 
     * Examples :
     * 
     *     SADD myset "Hello"
     *     SADD myset "World"
     *     SCARD myset
     * 
     */
    long scard(final K key);

    /**
     * Subtract multiple sets.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the total number of elements in all given sets.
     * 
     * Returns the members of the set resulting from the difference between the first
     * set and all the successive sets.
     * 
     * For example:
     * 
     *     key1 = {a,b,c,d}
     *     key2 = {c}
     *     key3 = {a,c,e}
     *     SDIFF key1 key2 key3 = {b,d}
     * 
     * Keys that do not exist are considered to be empty sets.
     * 
     * 
     * @return a multi-bulk reply : list with members of the resulting set.
     * 
     * Examples :
     * 
     *     SADD key1 "a"
     *     SADD key1 "b"
     *     SADD key1 "c"
     *     SADD key2 "c"
     *     SADD key2 "d"
     *     SADD key2 "e"
     *     SDIFF key1 key2
     * 
     */
    List<byte[]> sdiff(final K key);

    /**
     * Subtract multiple sets and store the resulting set in a key.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the total number of elements in all given sets.
     * 
     * This command is equal to `SDIFF`, but instead of returning the resulting set,
     * it is stored in `destination`.
     * 
     * If `destination` already exists, it is overwritten.
     * 
     * 
     * @return an integer reply : the number of elements in the resulting set.
     */
    long sdiffstore(final K destination, final K key);

    /**
     * Intersect multiple sets.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(N\*M) worst case where N is the cardinality of the smallest set and M is the
     * number of sets.
     * 
     * Returns the members of the set resulting from the intersection of all the given
     * sets.
     * 
     * For example:
     * 
     *     key1 = {a,b,c,d}
     *     key2 = {c}
     *     key3 = {a,c,e}
     *     SINTER key1 key2 key3 = {c}
     * 
     * Keys that do not exist are considered to be empty sets. With one of the keys
     * being an empty set, the resulting set is also empty (since set intersection
     * with an empty set always results in an empty set).
     * 
     * 
     * @return a multi-bulk reply : list with members of the resulting set.
     * 
     * Examples :
     * 
     *     SADD key1 "a"
     *     SADD key1 "b"
     *     SADD key1 "c"
     *     SADD key2 "c"
     *     SADD key2 "d"
     *     SADD key2 "e"
     *     SINTER key1 key2
     * 
     */
    List<byte[]> sinter(final K key);

    /**
     * Intersect multiple sets and store the resulting set in a key.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(N*M) worst case where N is the cardinality of the smallest set and M is the
     * number of sets.
     * 
     * This command is equal to `SINTER`, but instead of returning the resulting set,
     * it is stored in `destination`.
     * 
     * If `destination` already exists, it is overwritten.
     * 
     * 
     * @return an integer reply : the number of elements in the resulting set.
     */
    long sinterstore(final K destination, final K key);

    /**
     * Determine if a given value is a member of a set.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns if `member` is a member of the set stored at `key`.
     * 
     * 
     * @return an integer reply , specifically:
     * 
     * * `1` if the element is a member of the set.
     * * `0` if the element is not a member of the set, or if `key` does not exist.
     * 
     * Examples :
     * 
     *     SADD myset "one"
     *     SISMEMBER myset "one"
     *     SISMEMBER myset "two"
     * 
     */
    long sismember(final K key, final string member);

    /**
     * Get all the members in a set.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the set cardinality.
     * 
     * Returns all the members of the set value stored at `key`.
     * 
     * This has the same effect as running `SINTER` with one argument `key`.
     * 
     * 
     * @return a multi-bulk reply : all elements of the set.
     * 
     * Examples :
     * 
     *     SADD myset "Hello"
     *     SADD myset "World"
     *     SMEMBERS myset
     * 
     */
    List<byte[]> smembers(final K key);

    /**
     * Move a member from one set to another.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Move `member` from the set at `source` to the set at `destination`. This
     * operation is atomic. In every given moment the element will appear to be a
     * member of `source` **or** `destination` for other clients.
     * 
     * If the source set does not exist or does not contain the specified element, no
     * operation is performed and `0` is returned. Otherwise, the element is removed
     * from the source set and added to the destination set. When the specified
     * element already exists in the destination set, it is only removed from the
     * source set.
     * 
     * An error is returned if `source` or `destination` does not hold a set value.
     * 
     * 
     * @return an integer reply , specifically:
     * 
     * * `1` if the element is moved.
     * * `0` if the element is not a member of `source` and no operation was performed.
     * 
     * Examples :
     * 
     *     SADD myset "one"
     *     SADD myset "two"
     *     SADD myotherset "three"
     *     SMOVE myset myotherset "two"
     *     SMEMBERS myset
     *     SMEMBERS myotherset
     * 
     */
    long smove(final K source, final K destination, final string member);

    /**
     * Remove and return a random member from a set.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Removes and returns a random element from the set value stored at `key`.
     * 
     * This operation is similar to `SRANDMEMBER`, that returns a random
     * element from a set but does not remove it.
     * 
     * 
     * @return a bulk reply : the removed element, or `nil` when `key` does not exist.
     * 
     * Examples :
     * 
     *     SADD myset "one"
     *     SADD myset "two"
     *     SADD myset "three"
     *     SPOP myset
     *     SMEMBERS myset
     * 
     */
    byte[] spop(final K key);

    /**
     * Get a random member from a set.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Return a random element from the set value stored at `key`.
     * 
     * This operation is similar to `SPOP`, however while `SPOP` also removes the
     * randomly selected element from the set, `SRANDMEMBER` will just return a random
     * element without altering the original set in any way.
     * 
     * 
     * @return a bulk reply : the randomly selected element, or `nil` when `key` does not exist.
     * 
     * Examples :
     * 
     *     SADD myset "one"
     *     SADD myset "two"
     *     SADD myset "three"
     *     SRANDMEMBER myset
     * 
     */
    byte[] srandmember(final K key);

    /**
     * Remove a member from a set.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Remove `member` from the set stored at `key`. If `member` is not a member of
     * this set, no operation is performed.
     * 
     * An error is returned when the value stored at `key` is not a set.
     * 
     * 
     * @return an integer reply , specifically:
     * 
     * * `1` if the element was removed.
     * * `0` if the element was not a member of the set.
     * 
     * Examples :
     * 
     *     SADD myset "one"
     *     SADD myset "two"
     *     SADD myset "three"
     *     SREM myset "one"
     *     SREM myset "four"
     *     SMEMBERS myset
     * 
     */
    long srem(final K key, final string member);

    /**
     * Add multiple sets.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the total number of elements in all given sets.
     * 
     * Returns the members of the set resulting from the union of all the
     * given sets.
     * 
     * For example:
     * 
     *     key1 = {a,b,c,d}
     *     key2 = {c}
     *     key3 = {a,c,e}
     *     SUNION key1 key2 key3 = {a,b,c,d,e}
     * 
     * Keys that do not exist are considered to be empty sets.
     * 
     * 
     * @return a multi-bulk reply : list with members of the resulting set.
     * 
     * Examples :
     * 
     *     SADD key1 "a"
     *     SADD key1 "b"
     *     SADD key1 "c"
     *     SADD key2 "c"
     *     SADD key2 "d"
     *     SADD key2 "e"
     *     SUNION key1 key2
     * 
     */
    List<byte[]> sunion(final K key);

    /**
     * Add multiple sets and store the resulting set in a key.<br />
     * (set operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the total number of elements in all given sets.
     * 
     * This command is equal to `SUNION`, but instead of returning the resulting set,
     * it is stored in `destination`.
     * 
     * If `destination` already exists, it is overwritten.
     * 
     * 
     * @return an integer reply : the number of elements in the resulting set.
     */
    long sunionstore(final K destination, final K key);

    /**
     * Add a member to a sorted set, or update its score if it already exists.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(log(N)) where N is the number of elements in the sorted set.
     * 
     * Adds the `member` with the specified `score` to the sorted set stored at `key`.
     * If `member` is already a member of the sorted set, the score is updated and the
     * element reinserted at the right position to ensure the correct ordering.  If
     * `key` does not exist, a new sorted set with the specified `member` as sole
     * member is created.  If the key exists but does not hold a sorted set, an error
     * is returned.
     * 
     * The `score` value should be the string representation of a numeric value, and
     * accepts double precision floating point numbers.
     * 
     * For an introduction to sorted sets, see the data types page on [sorted
     * sets](/topics/data-types#sorted-sets).
     * 
     * 
     * @return an integer reply , specifically:
     * 
     * * `1` if the element was added.
     * * `0` if the element was already a member of the sorted set and the score was
     *   updated.
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZADD myzset 3 "two"
     *     ZRANGE myzset 0 -1 WITHSCORES
     * 
     */
    long zadd(final K key, final double score, final string member);

    /**
     * Get the number of members in a sorted set.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns the sorted set cardinality (number of elements) of the sorted set
     * stored at `key`.
     * 
     * 
     * @return an integer reply : the cardinality (number of elements) of the sorted set, or `0`
     * if `key` does not exist.
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZCARD myzset
     * 
     */
    long zcard(final K key);

    /**
     * Count the members in a sorted set with scores within the given values.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(log(N)+M) with N being the number of elements in the
     * sorted set and M being the number of elements between `min` and `max`.
     * 
     * Returns the number of elements in the sorted set at `key` with
     * a score between `min` and `max`.
     * 
     * The `min` and `max` arguments have the same semantic as described
     * for `ZRANGEBYSCORE`.
     * 
     * 
     * @return an integer reply : the number of elements in the specified score range.
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZADD myzset 3 "three"
     *     ZCOUNT myzset -inf +inf
     *     ZCOUNT myzset (1 3
     * 
     */
    long zcount(final K key, final double min, final double max);

    /**
     * Increment the score of a member in a sorted set.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(log(N)) where N is the number of elements in the sorted set.
     * 
     * Increments the score of `member` in the sorted set stored at `key` by
     * `increment`.  If `member` does not exist in the sorted set, it is added with
     * `increment` as its score (as if its previous score was `0.0`).  If `key` does
     * not exist, a new sorted set with the specified `member` as its sole member is
     * created.
     * 
     * An error is returned when `key` exists but does not hold a sorted set.
     * 
     * The `score` value should be the string representation of a numeric value, and
     * accepts double precision floating point numbers. It is possible to provide a
     * negative value to decrement the score.
     * 
     * 
     * @return a bulk reply : the new score of `member` (a double precision floating point
     * number), represented as string.
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZINCRBY myzset 2 "one"
     *     ZRANGE myzset 0 -1 WITHSCORES
     * 
     */
    byte[] zincrby(final K key, final integer increment, final string member);

    /**
     * Intersect multiple sorted sets and store the resulting sorted set in a new key.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(N\*K)+O(M\*log(M)) worst case with N being the smallest input sorted set, K
     * being the number of input sorted sets and M being the number of elements in the
     * resulting sorted set.
     * 
     * Computes the intersection of `numkeys` sorted sets given by the specified keys,
     * and stores the result in `destination`. It is mandatory to provide the number
     * of input keys (`numkeys`) before passing the input keys and the other
     * (optional) arguments.
     * 
     * By default, the resulting score of an element is the sum of its scores in the
     * sorted sets where it exists. Because intersection requires an element
     * to be a member of every given sorted set, this results in the score of every
     * element in the resulting sorted set to be equal to the number of input sorted sets.
     * 
     * For a description of the `WEIGHTS` and `AGGREGATE` options, see `ZUNIONSTORE`.
     * 
     * If `destination` already exists, it is overwritten.
     * 
     * 
     * @return an integer reply : the number of elements in the resulting sorted set at
     * `destination`.
     * 
     * Examples :
     * 
     *     ZADD zset1 1 "one"
     *     ZADD zset1 2 "two"
     *     ZADD zset2 1 "one"
     *     ZADD zset2 2 "two"
     *     ZADD zset2 3 "three"
     *     ZINTERSTORE out 2 zset1 zset2 WEIGHTS 2 3
     *     ZRANGE out 0 -1 WITHSCORES
     * 
     */
    long zinterstore(final K destination, final integer numkeys, final K key, final integer weight, final enum aggregate);

    /**
     * Return a range of members in a sorted set, by index.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(log(N)+M) with N being the number of elements in the sorted set and M the
     * number of elements returned.
     * 
     * Returns the specified range of elements in the sorted set stored at `key`. The
     * elements are considered to be ordered from the lowest to the highest score.
     * Lexicographical order is used for elements with equal score.
     * 
     * See `ZREVRANGE` when you need the elements ordered from highest to lowest
     * score (and descending lexicographical order for elements with equal score).
     * 
     * Both `start` and `stop` are zero-based indexes, where `0` is the first element,
     * `1` is the next element and so on. They can also be negative numbers indicating
     * offsets from the end of the sorted set, with `-1` being the last element of the
     * sorted set, `-2` the penultimate element and so on.
     * 
     * Out of range indexes will not produce an error. If `start` is larger than the
     * largest index in the sorted set, or `start > stop`, an empty list is returned.
     * If `stop` is larger than the end of the sorted set Redis will treat it like it
     * is the last element of the sorted set.
     * 
     * It is possible to pass the `WITHSCORES` option in order to return the scores of
     * the elements together with the elements.  The returned list will contain
     * `value1,score1,...,valueN,scoreN` instead of `value1,...,valueN`.  Client
     * libraries are free to return a more appropriate data type (suggestion: an array
     * with (value, score) arrays/tuples).
     * 
     * 
     * @return a multi-bulk reply : list of elements in the specified range (optionally with
     * their scores).
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZADD myzset 3 "three"
     *     ZRANGE myzset 0 -1
     *     ZRANGE myzset 2 3
     *     ZRANGE myzset -2 -1
     * 
     */
    List<byte[]> zrange(final K key, final integer start, final integer stop, final enum withscores);

    /**
     * Return a range of members in a sorted set, by score.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(log(N)+M) with N being the number of elements in the sorted set and M the
     * number of elements being returned. If M is constant (e.g. always asking for the
     * first 10 elements with `LIMIT`), you can consider it O(log(N)).
     * 
     * Returns all the elements in the sorted set at `key` with a score between `min`
     * and `max` (including elements with score equal to `min` or `max`). The
     * elements are considered to be ordered from low to high scores.
     * 
     * The elements having the same score are returned in lexicographical order (this
     * follows from a property of the sorted set implementation in Redis and does not
     * involve further computation).
     * 
     * The optional `LIMIT` argument can be used to only get a range of the matching
     * elements (similar to _SELECT LIMIT offset, count_ in SQL). Keep in mind that if
     * `offset` is large, the sorted set needs to be traversed for `offset` elements
     * before getting to the elements to return, which can add up to O(N) time
     * complexity.
     * 
     * The optional `WITHSCORES` argument makes the command return both the element
     * and its score, instead of the element alone. This option is available since
     * Redis 2.0.
     * 
     * ## Exclusive intervals and infinity
     * 
     * `min` and `max` can be `-inf` and `+inf`, so that you are not required to know
     * the highest or lowest score in the sorted set to get all elements from or up to
     * a certain score.
     * 
     * By default, the interval specified by `min` and `max` is closed (inclusive).
     * It is possible to specify an open interval (exclusive) by prefixing the score
     * with the character `(`. For example:
     * 
     *     ZRANGEBYSCORE zset (1 5
     * 
     * Will return all elements with `1 < score <= 5` while:
     * 
     *     ZRANGEBYSCORE zset (5 (10
     * 
     * Will return all the elements with `5 < score < 10` (5 and 10 excluded).
     * 
     * 
     * @return a multi-bulk reply : list of elements in the specified score range (optionally with
     * their scores).
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZADD myzset 3 "three"
     *     ZRANGEBYSCORE myzset -inf +inf
     *     ZRANGEBYSCORE myzset 1 2
     *     ZRANGEBYSCORE myzset (1 2
     *     ZRANGEBYSCORE myzset (1 (2
     * 
     */
    List<byte[]> zrangebyscore(final K key, final double min, final double max, final enum withscores, final [integer, integer] [offset, count]);

    /**
     * Determine the index of a member in a sorted set.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(log(N))
     * 
     * 
     * Returns the rank of `member` in the sorted set stored at `key`, with the scores
     * ordered from low to high. The rank (or index) is 0-based, which means that the
     * member with the lowest score has rank `0`.
     * 
     * Use `ZREVRANK` to get the rank of an element with the scores ordered from high
     * to low.
     * 
     * 
     * * If `member` exists in the sorted set, @integer-reply: the rank of `member`.
     * * If `member` does not exist in the sorted set or `key` does not exist,
     * @return a bulk reply : `nil`.
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZADD myzset 3 "three"
     *     ZRANK myzset "three"
     *     ZRANK myzset "four"
     * 
     */
    byte[] zrank(final K key, final string member);

    /**
     * Remove a member from a sorted set.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(log(N)) with N being the number of elements in the sorted set.
     * 
     * Removes the `member` from the sorted set stored at `key`. If
     * `member` is not a member of the sorted set, no operation is performed.
     * 
     * An error is returned when `key` exists and does not hold a sorted set.
     * 
     * 
     * @return an integer reply , specifically:
     * 
     * * `1` if `member` was removed.
     * * `0` if `member` is not a member of the sorted set.
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZADD myzset 3 "three"
     *     ZREM myzset "two"
     *     ZRANGE myzset 0 -1 WITHSCORES
     * 
     */
    long zrem(final K key, final string member);

    /**
     * Remove all members in a sorted set within the given indexes.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(log(N)+M) with N being the number of elements in the sorted set and M the
     * number of elements removed by the operation.
     * 
     * Removes all elements in the sorted set stored at `key` with rank between
     * `start` and `stop`.  Both `start` and `stop` are `0`-based indexes with `0`
     * being the element with the lowest score. These indexes can be negative numbers,
     * where they indicate offsets starting at the element with the highest score. For
     * example: `-1` is the element with the highest score, `-2` the element with the
     * second highest score and so forth.
     * 
     * 
     * @return an integer reply : the number of elements removed.
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZADD myzset 3 "three"
     *     ZREMRANGEBYRANK myzset 0 1
     *     ZRANGE myzset 0 -1 WITHSCORES
     * 
     */
    long zremrangebyrank(final K key, final integer start, final integer stop);

    /**
     * Remove all members in a sorted set within the given scores.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(log(N)+M) with N being the number of elements in the sorted set and M the
     * number of elements removed by the operation.
     * 
     * Removes all elements in the sorted set stored at `key` with a score between
     * `min` and `max` (inclusive).
     * 
     * Since version 2.1.6, `min` and `max` can be exclusive, following the syntax of
     * `ZRANGEBYSCORE`.
     * 
     * 
     * @return an integer reply : the number of elements removed.
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZADD myzset 3 "three"
     *     ZREMRANGEBYSCORE myzset -inf (2
     *     ZRANGE myzset 0 -1 WITHSCORES
     * 
     */
    long zremrangebyscore(final K key, final double min, final double max);

    /**
     * Return a range of members in a sorted set, by index, with scores ordered from high to low.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(log(N)+M) with N being the number of elements in the
     * sorted set and M the number of elements returned.
     * 
     * Returns the specified range of elements in the sorted set stored at `key`. The
     * elements are considered to be ordered from the highest to the lowest score.
     * Descending lexicographical order is used for elements with equal score.
     * 
     * Apart from the reversed ordering, `ZREVRANGE` is similar to `ZRANGE`.
     * 
     * 
     * @return a multi-bulk reply : list of elements in the specified range (optionally with
     * their scores).
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZADD myzset 3 "three"
     *     ZREVRANGE myzset 0 -1
     *     ZREVRANGE myzset 2 3
     *     ZREVRANGE myzset -2 -1
     * 
     */
    List<byte[]> zrevrange(final K key, final integer start, final integer stop, final enum withscores);

    /**
     * Return a range of members in a sorted set, by score, with scores ordered from high to low.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(log(N)+M) with N being the number of elements in the sorted set and M the
     * number of elements being returned. If M is constant (e.g. always asking for the
     * first 10 elements with `LIMIT`), you can consider it O(log(N)).
     * 
     * Returns all the elements in the sorted set at `key` with a score between `max`
     * and `min` (including elements with score equal to `max` or `min`). In contrary
     * to the default ordering of sorted sets, for this command the elements are
     * considered to be ordered from high to low scores.
     * 
     * The elements having the same score are returned in reverse lexicographical order.
     * 
     * Apart from the reversed ordering, `ZREVRANGEBYSCORE` is similar to
     * `ZRANGEBYSCORE`.
     * 
     * 
     * @return a multi-bulk reply : list of elements in the specified score range (optionally with
     * their scores).
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZADD myzset 3 "three"
     *     ZREVRANGEBYSCORE myzset +inf -inf
     *     ZREVRANGEBYSCORE myzset 2 1
     *     ZREVRANGEBYSCORE myzset 2 (1
     *     ZREVRANGEBYSCORE myzset (2 (1
     * 
     */
    List<byte[]> zrevrangebyscore(final K key, final double max, final double min, final enum withscores, final [integer, integer] [offset, count]);

    /**
     * Determine the index of a member in a sorted set, with scores ordered from high to low.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(log(N))
     * 
     * 
     * Returns the rank of `member` in the sorted set stored at `key`, with the scores
     * ordered from high to low. The rank (or index) is 0-based, which means that the
     * member with the highest score has rank `0`.
     * 
     * Use `ZRANK` to get the rank of an element with the scores ordered from low to
     * high.
     * 
     * 
     * * If `member` exists in the sorted set, @integer-reply: the rank of `member`.
     * * If `member` does not exist in the sorted set or `key` does not exist,
     * @return a bulk reply : `nil`.
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZADD myzset 2 "two"
     *     ZADD myzset 3 "three"
     *     ZREVRANK myzset "one"
     *     ZREVRANK myzset "four"
     * 
     */
    byte[] zrevrank(final K key, final string member);

    /**
     * Get the score associated with the given member in a sorted set.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns the score of `member` in the sorted set at `key`.
     * 
     * If `member` does not exist in the sorted set, or `key` does not exist,
     * `nil` is returned.
     * 
     * 
     * @return a bulk reply : the score of `member` (a double precision floating point number),
     * represented as string.
     * 
     * Examples :
     * 
     *     ZADD myzset 1 "one"
     *     ZSCORE myzset "one"
     * 
     */
    byte[] zscore(final K key, final string member);

    /**
     * Add multiple sorted sets and store the resulting sorted set in a new key.<br />
     * (sorted_set operation)<br /><br />
     * Complexity :
     * 
     * O(N)+O(M log(M)) with N being the sum of the sizes of the input sorted sets,
     * and M being the number of elements in the resulting sorted set.
     * 
     * Computes the union of `numkeys` sorted sets given by the specified keys, and
     * stores the result in `destination`. It is mandatory to provide the number of
     * input keys (`numkeys`) before passing the input keys and the other (optional)
     * arguments.
     * 
     * By default, the resulting score of an element is the sum of its scores in the
     * sorted sets where it exists.
     * 
     * Using the `WEIGHTS` option, it is possible to specify a multiplication factor
     * for each input sorted set. This means that the score of every element in every
     * input sorted set is multiplied by this factor before being passed to the
     * aggregation function.  When `WEIGHTS` is not given, the multiplication factors
     * default to `1`.
     * 
     * With the `AGGREGATE` option, it is possible to specify how the results of the
     * union are aggregated. This option defaults to `SUM`, where the score of an
     * element is summed across the inputs where it exists. When this option is set to
     * either `MIN` or `MAX`, the resulting set will contain the minimum or maximum
     * score of an element across the inputs where it exists.
     * 
     * If `destination` already exists, it is overwritten.
     * 
     * 
     * @return an integer reply : the number of elements in the resulting sorted set at
     * `destination`.
     * 
     * Examples :
     * 
     *     ZADD zset1 1 "one"
     *     ZADD zset1 2 "two"
     *     ZADD zset2 1 "one"
     *     ZADD zset2 2 "two"
     *     ZADD zset2 3 "three"
     *     ZUNIONSTORE out 2 zset1 zset2 WEIGHTS 2 3
     *     ZRANGE out 0 -1 WITHSCORES
     * 
     */
    long zunionstore(final K destination, final integer numkeys, final K key, final integer weight, final enum aggregate);

    /**
     * Append a value to a key.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1). The amortized time complexity is O(1) assuming the appended value is
     * small and the already present value is of any size, since the dynamic string
     * library used by Redis will double the free space available on every
     * reallocation.
     * 
     * If `key` already exists and is a string, this command appends the `value` at
     * the end of the string.  If `key` does not exist it is created and set as an
     * empty string, so `APPEND` will be similar to `SET` in this special case.
     * 
     * 
     * @return an integer reply : the length of the string after the append operation.
     * 
     * Examples :
     * 
     *     EXISTS mykey
     *     APPEND mykey "Hello"
     *     APPEND mykey " World"
     *     GET mykey
     * 
     */
    long append(final K key, final string value);

    /**
     * Decrement the integer value of a key by one.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Decrements the number stored at `key` by one.
     * If the key does not exist, it is set to `0` before performing the operation. An
     * error is returned if the key contains a value of the wrong type or contains a
     * string that is not representable as integer. This operation is limited to 64
     * bit signed integers.
     * 
     * See `INCR` for extra information on increment/decrement operations.
     * 
     * 
     * @return an integer reply : the value of `key` after the decrement
     * 
     * Examples :
     * 
     *     SET mykey "10"
     *     DECR mykey
     * 
     */
    long decr(final K key);

    /**
     * Decrement the integer value of a key by the given number.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Decrements the number stored at `key` by `decrement`.
     * If the key does not exist, it is set to `0` before performing the operation. An
     * error is returned if the key contains a value of the wrong type or contains a
     * string that is not representable as integer. This operation is limited to 64
     * bit signed integers.
     * 
     * See `INCR` for extra information on increment/decrement operations.
     * 
     * 
     * @return an integer reply : the value of `key` after the decrement
     * 
     * Examples :
     * 
     *     SET mykey "10"
     *     DECRBY mykey 5
     * 
     */
    long decrby(final K key, final integer decrement);

    /**
     * Get the value of a key.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Get the value of `key`. If the key does not exist the special value `nil` is returned.
     * An error is returned if the value stored at `key` is not a string, because `GET`
     * only handles string values.
     * 
     * 
     * @return a bulk reply : the value of `key`, or `nil` when `key` does not exist.
     * 
     * Examples :
     * 
     *     GET nonexisting
     *     SET mykey "Hello"
     *     GET mykey
     * 
     */
    byte[] get(final K key);

    /**
     * Returns the bit value at offset in the string value stored at key.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns the bit value at _offset_ in the string value stored at _key_.
     * 
     * When _offset_ is beyond the string length, the string is assumed to be a
     * contiguous space with 0 bits. When _key_ does not exist it is assumed to be an
     * empty string, so _offset_ is always out of range and the value is also assumed
     * to be a contiguous space with 0 bits.
     * 
     * 
     * @return an integer reply : the bit value stored at _offset_.
     * 
     * Examples :
     * 
     *     SETBIT mykey 7 1
     *     GETBIT mykey 0
     *     GETBIT mykey 7
     *     GETBIT mykey 100
     * 
     */
    long getbit(final K key, final integer offset);

    /**
     * Get a substring of the string stored at a key.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the length of the returned string. The complexity is ultimately
     * determined by the returned length, but because creating a substring from an
     * existing string is very cheap, it can be considered O(1) for small strings.
     * 
     * **Warning**: this command was renamed to `GETRANGE`, it is called `SUBSTR` in Redis versions `<= 2.0`.
     * 
     * Returns the substring of the string value stored at `key`, determined by the
     * offsets `start` and `end` (both are inclusive). Negative offsets can be used in
     * order to provide an offset starting from the end of the string. So -1 means the
     * last character, -2 the penultimate and so forth.
     * 
     * The function handles out of range requests by limiting the resulting range to
     * the actual length of the string.
     * 
     * 
     * @return a bulk reply 
     * 
     * Examples :
     * 
     *     SET mykey "This is a string"
     *     GETRANGE mykey 0 3
     *     GETRANGE mykey -3 -1
     *     GETRANGE mykey 0 -1
     *     GETRANGE mykey 10 100
     * 
     */
    byte[] getrange(final K key, final integer start, final integer end);

    /**
     * Set the string value of a key and return its old value.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Atomically sets `key` to `value` and returns the old value stored at `key`.
     * Returns an error when `key` exists but does not hold a string value.
     * 
     * ## Design pattern
     * 
     * `GETSET` can be used together with `INCR` for counting with atomic reset.  For
     * example: a process may call `INCR` against the key `mycounter` every time some
     * event occurs, but from time to time we need to get the value of the counter and
     * reset it to zero atomically.  This can be done using `GETSET mycounter "0"`:
     * 
     *     INCR mycounter
     *     GETSET mycounter "0"
     *     GET mycounter
     * 
     * 
     * @return a bulk reply : the old value stored at `key`, or `nil` when `key` did not exist.
     * 
     * Examples :
     * 
     *     SET mykey "Hello"
     *     GETSET mykey "World"
     *     GET mykey
     * 
     */
    byte[] getset(final K key, final string value);

    /**
     * Increment the integer value of a key by one.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Increments the number stored at `key` by one.
     * If the key does not exist, it is set to `0` before performing the operation. An
     * error is returned if the key contains a value of the wrong type or contains a
     * string that is not representable as integer. This operation is limited to 64
     * bit signed integers.
     * 
     * **Note**: this is a string operation because Redis does not have a dedicated
     * integer type. The the string stored at the key is interpreted as a base-10 64
     * bit signed integer to execute the operation.
     * 
     * Redis stores integers in their integer representation, so for string values
     * that actually hold an integer, there is no overhead for storing the
     * string representation of the integer.
     * 
     * 
     * @return an integer reply : the value of `key` after the increment
     * 
     * Examples :
     * 
     *     SET mykey "10"
     *     INCR mykey
     *     GET mykey
     * 
     */
    long incr(final K key);

    /**
     * Increment the integer value of a key by the given number.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Increments the number stored at `key` by `increment`.
     * If the key does not exist, it is set to `0` before performing the operation. An
     * error is returned if the key contains a value of the wrong type or contains a
     * string that is not representable as integer. This operation is limited to 64
     * bit signed integers.
     * 
     * See `INCR` for extra information on increment/decrement operations.
     * 
     * 
     * @return an integer reply : the value of `key` after the increment
     * 
     * Examples :
     * 
     *     SET mykey "10"
     *     INCRBY mykey 5
     * 
     */
    long incrby(final K key, final integer increment);

    /**
     * Get the values of all the given keys.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of keys to retrieve
     * 
     * 
     * Returns the values of all specified keys. For every key that does not hold a string value
     * or does not exist, the special value `nil` is returned.
     * Because of this, the operation never fails.
     * 
     * 
     * @return a multi-bulk reply : list of values at the specified keys.
     * 
     * Examples :
     * 
     *     SET key1 "Hello"
     *     SET key2 "World"
     *     MGET key1 key2 nonexisting
     * 
     */
    List<byte[]> mget(final K key);

    /**
     * Set multiple keys to multiple values.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of keys to set
     * 
     * 
     * Sets the given keys to their respective values. `MSET` replaces existing values
     * with new values, just as regular `SET`.  See `MSETNX` if you don't want to
     * overwrite existing values.
     * 
     * `MSET` is atomic, so all given keys are set at once. It is not possible for
     * clients to see that some of the keys were updated while others are unchanged.
     * 
     * 
     * @return a status reply : always `OK` since `MSET` can't fail.
     * 
     * Examples :
     * 
     *     MSET key1 "Hello" key2 "World"
     *     GET key1
     *     GET key2
     * 
     */
    Status mset(final [key, string] [key, value]);

    /**
     * Set multiple keys to multiple values, only if none of the keys exist.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(N) where N is the number of keys to set
     * 
     * 
     * Sets the given keys to their respective values. `MSETNX` will not perform any
     * operation at all even if just a single key already exists.
     * 
     * Because of this semantic `MSETNX` can be used in order to set different keys
     * representing different fields of an unique logic object in a way that
     * ensures that either all the fields or none at all are set.
     * 
     * `MSETNX` is atomic, so all given keys are set at once. It is not possible for
     * clients to see that some of the keys were updated while others are unchanged.
     * 
     * 
     * @return an integer reply , specifically:
     * 
     * * `1` if the all the keys were set.
     * * `0` if no key was set (at least one key already existed).
     * 
     * Examples :
     * 
     *     MSETNX key1 "Hello" key2 "there"
     *     MSETNX key2 "there" key3 "world"
     *     MGET key1 key2 key3
     * 
     */
    long msetnx(final [key, string] [key, value]);

    /**
     * Set the string value of a key.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Set `key` to hold the string `value`. If `key` already holds a value, it is
     * overwritten, regardless of its type.
     * 
     * 
     * @return a status reply : always `OK` since `SET` can't fail.
     * 
     * Examples :
     * 
     *     SET mykey "Hello"
     *     GET mykey
     * 
     */
    Status set(final K key, final string value);

    /**
     * Sets or clears the bit at offset in the string value stored at key.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Sets or clears the bit at _offset_ in the string value stored at _key_.
     * 
     * The bit is either set or cleared depending on _value_, which can be either 0 or
     * 1. When _key_ does not exist, a new string value is created. The string is
     * grown to make sure it can hold a bit at _offset_. The _offset_ argument is
     * required to be greater than or equal to 0, and smaller than 2^32 (this
     * limits bitmaps to 512MB). When the string at _key_ is grown, added
     * bits are set to 0.
     * 
     * **Warning**: When setting the last possible bit (_offset_ equal to 2^32 -1) and
     * the string value stored at _key_ does not yet hold a string value, or holds a
     * small string value, Redis needs to allocate all intermediate memory which can
     * block the server for some time.  On a 2010 MacBook Pro, setting bit number
     * 2^32 -1 (512MB allocation) takes ~300ms, setting bit number 2^30 -1 (128MB
     * allocation) takes ~80ms, setting bit number 2^28 -1 (32MB allocation) takes
     * ~30ms and setting bit number 2^26 -1 (8MB allocation) takes ~8ms.  Note that
     * once this first allocation is done, subsequent calls to `SETBIT` for the same
     * _key_ will not have the allocation overhead.
     * 
     * 
     * @return an integer reply : the original bit value stored at _offset_.
     * 
     * Examples :
     * 
     *     SETBIT mykey 7 1
     *     SETBIT mykey 7 0
     *     GET mykey
     * 
     */
    long setbit(final K key, final integer offset, final string value);

    /**
     * Set the value and expiration of a key.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Set `key` to hold the string `value` and set `key` to timeout after a given
     * number of seconds.  This command is equivalent to executing the following
     * commands:
     * 
     *     SET mykey value
     *     EXPIRE mykey seconds
     * 
     * `SETEX` is atomic, and can be reproduced by using the previous two commands
     * inside an `MULTI`/`EXEC` block. It is provided as a faster alternative to the
     * given sequence of operations, because this operation is very common when Redis
     * is used as a cache.
     * 
     * An error is returned when `seconds` is invalid.
     * 
     * 
     * @return a status reply 
     * 
     * Examples :
     * 
     *     SETEX mykey 10 "Hello"
     *     TTL mykey
     *     GET mykey
     * 
     */
    Status setex(final K key, final integer seconds, final string value);

    /**
     * Set the value of a key, only if the key does not exist.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Set `key` to hold string `value` if `key` does not exist.
     * In that case, it is equal to `SET`. When `key` already holds
     * a value, no operation is performed.
     * `SETNX` is short for "**SET** if **N**ot e**X**ists".
     * 
     * 
     * @return an integer reply , specifically:
     * 
     * * `1` if the key was set
     * * `0` if the key was not set
     * 
     * Examples :
     * 
     *     SETNX mykey "Hello"
     *     SETNX mykey "World"
     *     GET mykey
     * 
     * ## Design pattern: Locking with `!SETNX`
     * 
     * `SETNX` can be used as a locking primitive. For example, to acquire
     * the lock of the key `foo`, the client could try the following:
     * 
     *     SETNX lock.foo <current Unix time + lock timeout + 1>
     * 
     * If `SETNX` returns `1` the client acquired the lock, setting the `lock.foo`
     * key to the Unix time at which the lock should no longer be considered valid.
     * The client will later use `DEL lock.foo` in order to release the lock.
     * 
     * If `SETNX` returns `0` the key is already locked by some other client. We can
     * either return to the caller if it's a non blocking lock, or enter a
     * loop retrying to hold the lock until we succeed or some kind of timeout
     * expires.
     * 
     * ### Handling deadlocks
     * 
     * In the above locking algorithm there is a problem: what happens if a client
     * fails, crashes, or is otherwise not able to release the lock?
     * It's possible to detect this condition because the lock key contains a
     * UNIX timestamp. If such a timestamp is equal to the current Unix time the lock
     * is no longer valid.
     * 
     * When this happens we can't just call `DEL` against the key to remove the lock
     * and then try to issue a `SETNX`, as there is a race condition here, when
     * multiple clients detected an expired lock and are trying to release it.
     * 
     * * C1 and C2 read `lock.foo` to check the timestamp, because they both received
     *   `0` after executing `SETNX`, as the lock is still held by C3 that crashed
     *   after holding the lock.
     * * C1 sends `DEL lock.foo`
     * * C1 sends `SETNX lock.foo` and it succeeds
     * * C2 sends `DEL lock.foo`
     * * C2 sends `SETNX lock.foo` and it succeeds
     * * **ERROR**: both C1 and C2 acquired the lock because of the race condition.
     * 
     * Fortunately, it's possible to avoid this issue using the following algorithm.
     * Let's see how C4, our sane client, uses the good algorithm:
     * 
     * * C4 sends `SETNX lock.foo` in order to acquire the lock
     * * The crashed client C3 still holds it, so Redis will reply with `0` to C4.
     * * C4 sends `GET lock.foo` to check if the lock expired. If it is not, it will
     *   sleep for some time and retry from the start.
     * * Instead, if the lock is expired because the Unix time at `lock.foo` is older
     *   than the current Unix time, C4 tries to perform:
     * 
     *       GETSET lock.foo <current Unix timestamp + lock timeout + 1>
     * 
     * * Because of the `GETSET` semantic, C4 can check if the old value stored
     *   at `key` is still an expired timestamp. If it is, the lock was acquired.
     * * If another client, for instance C5, was faster than C4 and acquired
     *   the lock with the `GETSET` operation, the C4 `GETSET` operation will return a non
     *   expired timestamp. C4 will simply restart from the first step. Note that even
     *   if C4 set the key a bit a few seconds in the future this is not a problem.
     * 
     * **Important note**: In order to make this locking algorithm more robust, a client
     * holding a lock should always check the timeout didn't expire before unlocking
     * the key with `DEL` because client failures can be complex, not just crashing
     * but also blocking a lot of time against some operations and trying to issue
     * `DEL` after a lot of time (when the LOCK is already held by another client).
     * 
     */
    long setnx(final K key, final string value);

    /**
     * Overwrite part of a string at key starting at the specified offset.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1), not counting the time taken to copy the new string in place. Usually,
     * this string is very small so the amortized complexity is O(1). Otherwise,
     * complexity is O(M) with M being the length of the _value_ argument.
     * 
     * Overwrites part of the string stored at _key_, starting at the specified
     * offset, for the entire length of _value_. If the offset is larger than the
     * current length of the string at _key_, the string is padded with zero-bytes to
     * make _offset_ fit. Non-existing keys are considered as empty strings, so this
     * command will make sure it holds a string large enough to be able to set _value_
     * at _offset_.
     * 
     * Note that the maximum offset that you can set is 2^29 -1 (536870911), as Redis
     * Strings are limited to 512 megabytes. If you need to grow beyond this size, you
     * can use multiple keys.
     * 
     * **Warning**: When setting the last possible byte and the string value stored at
     * _key_ does not yet hold a string value, or holds a small string value, Redis
     * needs to allocate all intermediate memory which can block the server for some
     * time.  On a 2010 MacBook Pro, setting byte number 536870911 (512MB allocation)
     * takes ~300ms, setting byte number 134217728 (128MB allocation) takes ~80ms,
     * setting bit number 33554432 (32MB allocation) takes ~30ms and setting bit
     * number 8388608 (8MB allocation) takes ~8ms. Note that once this first
     * allocation is done, subsequent calls to `SETRANGE` for the same _key_ will not
     * have the allocation overhead.
     * 
     * ## Patterns
     * 
     * Thanks to `SETRANGE` and the analogous `GETRANGE` commands, you can use Redis strings
     * as a linear array with O(1) random access. This is a very fast and
     * efficient storage in many real world use cases.
     * 
     * 
     * @return an integer reply : the length of the string after it was modified by the command.
     * 
     * Examples :
     * 
     * Basic usage:
     * 
     *     SET key1 "Hello World"
     *     SETRANGE key1 6 "Redis"
     *     GET key1
     * 
     * Example of zero padding:
     * 
     *     SETRANGE key2 6 "Redis"
     *     GET key2
     * 
     */
    long setrange(final K key, final integer offset, final string value);

    /**
     * Get the length of the value stored in a key.<br />
     * (string operation)<br /><br />
     * Complexity :
     * 
     * O(1)
     * 
     * 
     * Returns the length of the string value stored at `key`.
     * An error is returned when `key` holds a non-string value.
     * 
     * 
     * @return an integer reply : the length of the string at `key`, or `0` when `key` does not exist.
     * 
     * Examples :
     * 
     *     SET mykey "Hello world"
     *     STRLEN mykey
     *     STRLEN nonexisting
     * 
     */
    long strlen(final K key);

    /**
     * Discard all commands issued after MULTI.<br />
     * (transactions operation)<br /><br />
     * Flushes all previously queued commands in a
     * [transaction](/topics/transactions) and restores the connection state to
     * normal.
     * 
     * If `WATCH` was used, `DISCARD` unwatches all keys.
     * 
     * 
     * @return a status reply : always `OK`.
     */
    Status discard();

    /**
     * Execute all commands issued after MULTI.<br />
     * (transactions operation)<br /><br />
     * Executes all previously queued commands in a
     * [transaction](/topics/transactions) and restores the connection state to
     * normal.
     * 
     * When using `WATCH`, `EXEC` will execute commands only if the
     * watched keys were not modified, allowing for a [check-and-set
     * mechanism](/topics/transactions#cas).
     * 
     * 
     * @return a multi-bulk reply : each element being the reply to each of the commands
     * in the atomic transaction.
     * 
     * When using `WATCH`, `EXEC` can return a @nil-reply if the execution was
     * aborted.
     */
    List<byte[]> exec();

    /**
     * Mark the start of a transaction block.<br />
     * (transactions operation)<br /><br />
     * Marks the start of a [transaction](/topics/transactions)
     * block. Subsequent commands will be queued for atomic execution using
     * `EXEC`.
     * 
     * 
     * @return a status reply : always `OK`.
     */
    Status multi();

    /**
     * Forget about all watched keys.<br />
     * (transactions operation)<br /><br />
     * Complexity :
     * 
     * O(1).
     * 
     * Flushes all the previously watched keys for a [transaction](/topics/transactions).
     * 
     * If you call `EXEC` or `DISCARD`, there's no need to manually call `UNWATCH`.
     * 
     * 
     * @return a status reply : always `OK`.
     */
    Status unwatch();

    /**
     * Watch the given keys to determine execution of the MULTI/EXEC block.<br />
     * (transactions operation)<br /><br />
     * Complexity :
     * 
     * O(1) for every key.
     * 
     * Marks the given keys to be watched for conditional execution of a [transaction](/topics/transactions).
     * 
     * 
     * @return a status reply : always `OK`.
     */
    Status watch(final K key);

}

