package com.example.suco.mapper;

import org.springframework.stereotype.Component;

import com.example.suco.dto.AddressResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class AddressMapper {


    public AddressResponseDTO toDTO(JsonNode rootNode) {

        AddressResponseDTO dto =
                new AddressResponseDTO();


        // Địa chỉ đầy đủ
        JsonNode displayName =
                rootNode.get("display_name");

        if (displayName != null) {
            dto.setFullAddress(
                    displayName.asText()
            );
        }


        JsonNode address =
                rootNode.get("address");


        if (address != null) {


            dto.setSoNha(
                    getJsonValue(
                            address,
                            "house_number"
                    )
            );


            dto.setTenDuong(
                    getJsonValue(
                            address,
                            "road"
                    )
            );


            String district =
                    getJsonValue(
                            address,
                            "city_district"
                    );


            // Nếu không có quận thì lấy suburb
            if (district.isBlank()) {

                district =
                        getJsonValue(
                                address,
                                "suburb"
                        );
            }


            dto.setQuan(district);



            dto.setHuyen(
                    getJsonValue(
                            address,
                            "county"
                    )
            );


            dto.setThanhPho(
                    getJsonValue(
                            address,
                            "city"
                    )
            );


            dto.setTinh(
                    getJsonValue(
                            address,
                            "state"
                    )
            );

        }


        return dto;
    }



    private String getJsonValue(
            JsonNode node,
            String fieldName
    ) {

        JsonNode field =
                node.get(fieldName);


        return field != null
                ? field.asText()
                : "";
    }

}