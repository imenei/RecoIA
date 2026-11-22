package dz.recoia.activityservice.web;

import dz.recoia.activityservice.grpc.ActivityStatsGrpc;
import dz.recoia.activityservice.grpc.CourseStat;
import dz.recoia.activityservice.grpc.SummaryReply;
import dz.recoia.activityservice.grpc.SummaryRequest;
import dz.recoia.activityservice.grpc.Totals;
import dz.recoia.activityservice.service.ActivityService;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

/**
 * Service gRPC : statistiques d'activite pour stats-service.
 * Le contrat est dans src/main/protobuf/activity.proto. Spring gRPC detecte ce bean et le publie
 * sur le port spring.grpc.server.port (9091).
 */
@Service
public class ActivityStatsGrpcService extends ActivityStatsGrpc.ActivityStatsImplBase {

    private final ActivityService service;

    public ActivityStatsGrpcService(ActivityService service) {
        this.service = service;
    }

    @Override
    public void getSummary(SummaryRequest request, StreamObserver<SummaryReply> responseObserver) {
        var summary = service.summary(request.getLimit() <= 0 ? 10 : request.getLimit());

        SummaryReply.Builder reply = SummaryReply.newBuilder()
                .setTotals(Totals.newBuilder()
                        .setViews(summary.totals().views())
                        .setRecommended(summary.totals().recommended())
                        .setAccepted(summary.totals().accepted())
                        .setRejected(summary.totals().rejected()));
        summary.mostViewed().forEach(c -> reply.addMostViewed(stat(c)));
        summary.mostRecommended().forEach(c -> reply.addMostRecommended(stat(c)));
        summary.mostAccepted().forEach(c -> reply.addMostAccepted(stat(c)));
        summary.mostRejected().forEach(c -> reply.addMostRejected(stat(c)));

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    private static CourseStat stat(ActivityService.CourseStat c) {
        return CourseStat.newBuilder().setCourseId(c.courseId()).setCount(c.count()).build();
    }
}
