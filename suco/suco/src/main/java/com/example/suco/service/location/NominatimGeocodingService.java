package com.example.suco.service.location;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.suco.dto.AddressResponseDTO;


@Service
public class NominatimGeocodingService
        implements GeocodingService {


    @Autowired
    private NominatimGeocodingClient geocodingClient;



    @Override
    public String getAddress(
            Double viDo,
            Double kinhDo
    ) {


        if (viDo == null || kinhDo == null) {

            return "Không xác định vị trí";

        }


        try {


            AddressResponseDTO addressDTO =
                    geocodingClient.getAddressFromCoordinates(
                            viDo,
                            kinhDo
                    );



            String fullAddress =
                    addressDTO.getFullAddress();



            return (fullAddress == null
                    || fullAddress.isBlank())

                    ? "Tọa độ: "
                        + viDo
                        + ", "
                        + kinhDo

                    : fullAddress;



        } catch (Exception e) {


            return "Tọa độ: "
                    + viDo
                    + ", "
                    + kinhDo;

        }

    }
}