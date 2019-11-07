package no.tine.web.zipcode;


import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;
import no.tine.web.zipcode.handler.ZipCodeHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
@Slf4j
public class AppLifecycleBean {

    private final ZipCodeHandler handler;

    @Inject
    public AppLifecycleBean(ZipCodeHandler handler) {
        this.handler = handler;
    }

    void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting...");
        handler.getData();
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("The application is stopping...");
    }

    @Scheduled(cron="0 22 * * * ?")
    void getData() {
        log.info("The application is updating zip codes");
        handler.getData();
    }
}
