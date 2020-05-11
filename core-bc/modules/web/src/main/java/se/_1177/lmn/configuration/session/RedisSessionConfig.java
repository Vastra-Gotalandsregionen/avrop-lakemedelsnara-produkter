package se._1177.lmn.configuration.session;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import se._1177.lmn.service.util.LakemedelsnaraProperties;

@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600) // One hour, which is more than session timeout for fronting web servers.
public class RedisSessionConfig {

	@Bean
	public LettuceConnectionFactory lettuceConnectionFactory() {
		LakemedelsnaraProperties properties = LakemedelsnaraProperties.getInstance();

		return new LettuceConnectionFactory(
				new RedisStandaloneConfiguration(
						properties.get("spring.redis.host"),
						Integer.parseInt(properties.get("spring.redis.port"))
				)
		);
	}
}

