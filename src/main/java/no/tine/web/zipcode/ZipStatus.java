package no.tine.web.zipcode;


import lombok.Data;

@Data
public class ZipStatus  {
	private final boolean ready;
	private final String nextUpdate;
}
