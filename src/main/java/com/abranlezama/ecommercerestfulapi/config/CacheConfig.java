package com.abranlezama.ecommercerestfulapi.config;

import com.abranlezama.ecommercerestfulapi.hazelcast.HttpResponseCompactSerializer;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.SerializationConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    Config config() {
        Config config = new Config();
        SerializationConfig serializationConfig = config.getSerializationConfig();

        MapConfig mapConfig = new MapConfig();
        mapConfig.setTimeToLiveSeconds(60 * 60 );
        config.getMapConfigs().put("idempotency", mapConfig);
        serializationConfig.getCompactSerializationConfig()
                .addSerializer(new HttpResponseCompactSerializer());

        return config;
    }
}
