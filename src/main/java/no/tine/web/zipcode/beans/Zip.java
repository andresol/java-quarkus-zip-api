package no.tine.web.zipcode.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.val;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;

@RegisterForReflection
@XmlRootElement
@Data
@Builder
@Value
public class Zip {

	private int zip;

	private String postPlace;

	private int municipalityCode;

	private String municipality;

	private char category;

	private transient String json;


}
