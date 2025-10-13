package com.ryuqq.crawlinghub.adapter.redis.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

import java.time.Duration;
import java.util.List;

/**
 * Redis Configuration.
 * - Connection Pool: min=10, max=50
 * - Timeouts: connect=5s, command=3s
 * - Lua Scripts: Token Bucket, Distributed Lock
 *
 * @author crawlinghub
 */
@Configuration
public class RedisConfig {

    private final RedisProperties redisProperties;

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * Redis Client Resources
     * - IO Thread Pool
     * - Computation Thread Pool
     */
    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return DefaultClientResources.builder()
                .ioThreadPoolSize(4)
                .computationThreadPoolSize(4)
                .build();
    }

    /**
     * Lettuce Client Configuration
     * - Connection Pool: min=10, max=50
     * - Connect Timeout: 5s
     * - Command Timeout: 3s
     */
    @Bean
    public LettuceClientConfiguration lettuceClientConfiguration(ClientResources clientResources) {
        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(3)))
                .autoReconnect(true)
                .build();

        return LettucePoolingClientConfiguration.builder()
                .poolConfig(new org.apache.commons.pool2.impl.GenericObjectPoolConfig<>() {{
                    setMinIdle(10);
                    setMaxIdle(20);
                    setMaxTotal(50);
                    setTestOnBorrow(true);
                    setTestWhileIdle(true);
                    setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
                }})
                .clientOptions(clientOptions)
                .clientResources(clientResources)
                .commandTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Redis Connection Factory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            LettuceClientConfiguration lettuceClientConfiguration) {

        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName(redisProperties.getHost());
        serverConfig.setPort(redisProperties.getPort());
        if (redisProperties.getPassword() != null) {
            serverConfig.setPassword(redisProperties.getPassword());
        }
        if (redisProperties.getDatabase() != 0) {
            serverConfig.setDatabase(redisProperties.getDatabase());
        }

        return new LettuceConnectionFactory(serverConfig, lettuceClientConfiguration);
    }

    /**
     * Redis Template
     * - Key Serializer: String
     * - Value Serializer: JSON
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Token Bucket Lua Script
     * 반환값: [success (1/0), current_tokens, retry_after_ms]
     */
    @Bean
    public DefaultRedisScript<List> tokenBucketScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/token_bucket.lua")));
        script.setResultType(List.class);
        return script;
    }

    /**
     * Distributed Lock Acquire Script
     * 반환값: 1 (성공) / 0 (실패)
     */
    @Bean
    public DefaultRedisScript<Long> distributedLockScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/distributed_lock.lua")));
        script.setResultType(Long.class);
        return script;
    }

    /**
     * Distributed Lock Release Script
     * 반환값: 1 (성공) / 0 (실패)
     */
    @Bean
    public DefaultRedisScript<Long> distributedUnlockScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/distributed_unlock.lua")));
        script.setResultType(Long.class);
        return script;
    }
}
