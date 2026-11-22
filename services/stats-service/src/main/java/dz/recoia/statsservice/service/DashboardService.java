package dz.recoia.statsservice.service;

import dz.recoia.statsservice.client.CatalogueClient;
import dz.recoia.statsservice.grpc.ActivityStatsGrpc;
import dz.recoia.statsservice.grpc.SummaryReply;
import dz.recoia.statsservice.grpc.SummaryRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Assemble le tableau de bord : chiffres d'activite (gRPC) + titres des formations (REST). */
@Service
public class DashboardService {

    public record TotalsView(int views, int recommended, int accepted, int rejected) { }

    public record StatView(int courseId, String title, int count) { }

    public record DashboardView(TotalsView totals, List<StatView> mostViewed, List<StatView> mostRecommended,
                                List<StatView> mostAccepted, List<StatView> mostRejected) { }

    private final ActivityStatsGrpc.ActivityStatsBlockingStub activity;
    private final CatalogueClient catalogue;

    public DashboardService(ActivityStatsGrpc.ActivityStatsBlockingStub activity, CatalogueClient catalogue) {
        this.activity = activity;
        this.catalogue = catalogue;
    }

    public DashboardView dashboard(int limit) {
        SummaryReply reply = activity.withDeadlineAfter(5, TimeUnit.SECONDS)
                .getSummary(SummaryRequest.newBuilder().setLimit(limit).build());
        Map<Long, String> titles = catalogue.titles();

        var t = reply.getTotals();
        return new DashboardView(
                new TotalsView((int) t.getViews(), (int) t.getRecommended(), (int) t.getAccepted(), (int) t.getRejected()),
                stats(reply.getMostViewedList(), titles),
                stats(reply.getMostRecommendedList(), titles),
                stats(reply.getMostAcceptedList(), titles),
                stats(reply.getMostRejectedList(), titles));
    }

    private static List<StatView> stats(List<dz.recoia.statsservice.grpc.CourseStat> rows, Map<Long, String> titles) {
        return rows.stream()
                .map(r -> new StatView((int) r.getCourseId(), titles.get(r.getCourseId()), (int) r.getCount()))
                .toList();
    }
}
