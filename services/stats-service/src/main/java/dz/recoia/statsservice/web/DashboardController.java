package dz.recoia.statsservice.web;

import dz.recoia.statsservice.service.DashboardService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/** Requete GraphQL du tableau de bord : POST /graphql avec { dashboard(limit: 10) { ... } }. */
@Controller
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @QueryMapping
    public DashboardService.DashboardView dashboard(@Argument Integer limit) {
        return service.dashboard(limit == null ? 10 : limit);
    }
}
