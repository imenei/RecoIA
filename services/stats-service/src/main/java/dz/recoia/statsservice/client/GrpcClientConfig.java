package dz.recoia.statsservice.client;

import dz.recoia.statsservice.grpc.ActivityStatsGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

/** Client gRPC vers activity-service. L'adresse du canal "activity" est dans stats-service.yml. */
@Configuration
public class GrpcClientConfig {

    @Bean
    ActivityStatsGrpc.ActivityStatsBlockingStub activityStats(GrpcChannelFactory channels) {
        return ActivityStatsGrpc.newBlockingStub(channels.createChannel("activity"));
    }
}
