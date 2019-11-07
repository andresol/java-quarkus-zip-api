package no.tine.web.zipcode.service;

import lombok.NonNull;
import no.tine.web.zipcode.beans.Zip;
import no.tine.web.zipcode.handler.ZipCodeHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class ZipCodeService {
    private final ZipCodeHandler handler;

    @Inject
    public ZipCodeService(ZipCodeHandler handler) {
        this.handler = handler;
    }

    public void getData() {
        this.handler.getData();
    }

    public String getAllData() {
       return this.handler.getAllData();
    }

    public boolean isReady() {
       return  this.handler.isReady();
    }

    public Stream<Zip> getZipFromIds(@NonNull final Collection<Integer> ids) {
        return ids.parallelStream()
                .map(this.handler::getZip)
                .flatMap(Optional::stream);
    }

    public Optional<Zip> getZip(int zip) {
        return this.handler.getZip(zip);
    }
}
