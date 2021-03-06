/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.redis.impl;

import java.util.concurrent.TimeUnit;

import org.hibernate.ogm.cfg.spi.Hosts;

import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.cluster.RedisClusterClient;

/**
 * Data store provider that reused the {@link com.lambdaworks.redis.RedisClient} throughout the test run.
 *
 * @author Mark Paluch
 */
public class TestRedisDatastoreProvider extends RedisDatastoreProvider {

	private static volatile AbstractRedisClient staticInstance;

	@Override
	protected RedisClient createClient(Hosts.HostAndPort hostAndPort) {
		if ( staticInstance == null ) {
			final RedisClient redisClient = super.createClient( hostAndPort );

			Runtime.getRuntime().addShutdownHook(
					new Thread( "RedisClient shutdown hook" ) {
						@Override
						public void run() {
							redisClient.shutdown( 0, 0, TimeUnit.MILLISECONDS );
						}
					}
			);
			staticInstance = redisClient;
		}

		return (RedisClient) staticInstance;
	}

	@Override
	protected RedisClusterClient createClusterClient(Hosts hosts) {
		if ( staticInstance == null ) {
			final RedisClusterClient redisClient = super.createClusterClient( hosts );

			Runtime.getRuntime().addShutdownHook(
					new Thread( "RedisClient shutdown hook" ) {
						@Override
						public void run() {
							redisClient.shutdown( 0, 0, TimeUnit.MILLISECONDS );
						}
					}
			);
			staticInstance = redisClient;
		}

		return (RedisClusterClient) staticInstance;
	}

	@Override
	protected void shutdownClient() {
		// noop. Client is closed using a ShutdownHook
	}
}
