/**
 *
 */
package no.tine.web.zipcode;

import lombok.extern.slf4j.Slf4j;
import no.tine.web.zipcode.beans.Zip;
import no.tine.web.zipcode.service.ZipCodeService;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Andre Sollie (andre.sollie@tine.no)
 */
@Path("/v1/zip")
@Slf4j
public class ZipResource {

    private final ZipCodeService service;

    @Inject
    public ZipResource(ZipCodeService service) {
        this.service = service;
    }

    @GET
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public Response start() {
        service.getData();
        ZipStatus status = new ZipStatus(true,
                String.valueOf(0));
        Jsonb jsonb = JsonbBuilder.create();
        return Response.ok().entity(jsonb.toJson(status)).build();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        ZipStatus status = new ZipStatus(this.service.isReady(),
                "0");
        Jsonb jsonb = JsonbBuilder.create();
        return Response.ok().entity(jsonb.toJson(status)).build();
    }

    @GET
    @Path("/all")
    @Produces("application/json; charset=UTF-8")
    public String getAllContent() {
        return service.getAllData();
    }

    @GET
    @Path("/{i}")
    @Produces("application/json; charset=UTF-8")
    public Response getInvoices(@PathParam("i") int zip,
                                @MatrixParam("ids") List<Integer> ids) {
        if (ids.isEmpty()) {
            return getSingleZip(zip);
        } else {
            return getMultiple(zip, ids);
        }
    }

    private Response getMultiple(int zip, List<Integer> ids) {
        Jsonb jsonb = JsonbBuilder.create();
        ids.add(zip);
        List<Zip> zips = this.service.getZipFromIds(ids)
                .collect(Collectors.toList());
        return Response.ok(jsonb.toJson(zips)).build();
    }

    private Response getSingleZip(int zip) {
        return this.service.getZip(zip)
                .map(z -> Response.ok(z.getJson()).build())
                .orElse(Response.status(Status.NOT_FOUND).build());
    }
}
