/**
 *
 */
package no.tine.web.zipcode.handler;

import lombok.extern.slf4j.Slf4j;
import no.tine.web.zipcode.beans.Zip;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * @author Andre Sollie (andre.sollie@tine.no)
 *
 */
@Singleton
@Slf4j
public class ZipCodeHandler {
	private static final int CAPACITY = 10001;
	private static final int UPDATE_DELAY = 5 * 60 * 1000; //5 min

	private final Map<String, Zip> place = new NonBlockingHashMap<>(CAPACITY);
	private final Map<Integer, Zip> zip = new NonBlockingHashMap<>(CAPACITY);
	private final AtomicReference<String> jsonAllData = new AtomicReference<>("{}");
	private final Lock lock = new ReentrantLock();
	private final AtomicBoolean updating = new AtomicBoolean(false);
	private final String url;

	@Inject
	public ZipCodeHandler(@ConfigProperty(name = "url.zip") String url ) {
		this.url = url;
	}

	public final void getData() {
		final Map<String, Zip> place = new HashMap<>(CAPACITY);
		final Map<Integer, Zip> tempZip = new HashMap<>(CAPACITY);
		Jsonb jsonb = JsonbBuilder.create();

		URL url = null;
		try {
			url = new URL(this.url);
		} catch (MalformedURLException e) {
			log.error("Cannot update post number. Url is wrong.");
			return;
		}
		try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			 InputStream inputStream = Channels.newInputStream(rbc);
			 InputStreamReader iReader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1);
			 BufferedReader reader = new BufferedReader(iReader)) {

			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(UPDATE_DELAY);
			reader.lines()
					.map(line -> line.split("\t"))
					.filter(s -> s.length == 5)
					.map(marshallZip(jsonb))
					.forEach(s -> {
						place.put(s.getMunicipality(), s);
						tempZip.put(s.getZip(), s);
					});
		} catch (IOException e) {
			log.error("Classic IO exception. See error", e);
		}

		if (!tempZip.isEmpty()) {
			if (lock.tryLock()) {
				try {
					updateData(place, tempZip, jsonb);
				} finally {
					lock.unlock();
					updating.set(false);
				}
			} else {
				log.info("Cannot update zipcode cache. Already updating.");
			}
		}
	}

	private Function<String[], Zip> marshallZip(Jsonb jsonb) {
		return split -> {
			Zip.ZipBuilder zipBuilder = Zip.builder().zip(Integer.parseInt(split[0]))
					.postPlace(split[1]).municipalityCode(Integer.parseInt(split[2]))
					.municipality(split[3]).category(split[4].charAt(0));
			String json = jsonb.toJson(zipBuilder.build());
			zipBuilder.json(json);

			return zipBuilder.build();
		};
	}

	private void updateData(Map<String, Zip> place, Map<Integer, Zip> tempZip, Jsonb jsonb) {
		updating.set(true);
		this.zip.clear();
		this.place.clear();
		this.zip.putAll(tempZip);
		this.place.putAll(place);
		String json;
		json = jsonb.toJson(zip);
		jsonAllData.set(json);
	}

	public final Optional<Zip> getZip(String zip) {
		return Optional.ofNullable(this.place.get(zip));
	}

	public final Optional<Zip> getZip(int zip) {
		return Optional.ofNullable(this.zip.get(zip));
	}

	public final String getAllData() {
		return this.jsonAllData.get();
	}

	public final boolean isUpdating() {
		return this.updating.get();
	}

	public final int elementsInCache() {
		return this.zip.size();
	}

	public final boolean isReady() {
		return (!this.isUpdating() && this.elementsInCache() > 0);
	}
}
