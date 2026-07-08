package com.example.suco.service.location;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.suco.dto.AddressResponseDTO;
import com.example.suco.mapper.AddressMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class NominatimGeocodingClient {

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/reverse";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    private AddressMapper addressMapper;

    public AddressResponseDTO getAddressFromCoordinates(
            Double latitude,
            Double longitude) {

        try {

            String url = String.format(
                    "%s?format=json&lat=%f&lon=%f&language=vi&zoom=18&addressdetails=1",
                    NOMINATIM_URL,
                    latitude,
                    longitude);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new java.net.URI(url))
                    .header("User-Agent", "SuCo-Map-App")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {

                JsonNode rootNode =
                        objectMapper.readTree(response.body());

                return addressMapper.toDTO(rootNode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new AddressResponseDTO();
    }
}