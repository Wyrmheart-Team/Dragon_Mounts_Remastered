package dmr.DragonMounts.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a configuration value that should be synced between server and client.
 * For server configs, syncing to client is always enabled.
 * For client configs, this annotation controls syncing to server.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SyncedConfig {}
