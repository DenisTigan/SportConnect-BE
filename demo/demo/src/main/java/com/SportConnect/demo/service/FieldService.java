package com.SportConnect.demo.service;


import com.SportConnect.demo.dto.FieldRequest;
import com.SportConnect.demo.dto.FieldResponse;
import com.SportConnect.demo.model.Field;
import com.SportConnect.demo.model.User;
import com.SportConnect.demo.repository.FieldRepository;
import com.SportConnect.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FieldService {

    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;

    public FieldService(FieldRepository fieldRepository, UserRepository userRepository) {
        this.fieldRepository = fieldRepository;
        this.userRepository = userRepository;
    }

    public FieldResponse addField(FieldRequest request, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Proprietarul nu a fost gasit!"));

        Field field = new Field(
                request.name(),
                request.address(),
                request.category(),
                request.pricePerHour(),
                request.phoneNumber(),
                request.description(),
                request.imageURL(),
                request.hasLighting(),
                request.isIndoor(),
                request.openTime(),
                request.closeTime(),
                owner
        );

        Field savedField = fieldRepository.save(field);

        return mapToResponse(savedField);

    }


    public List<FieldResponse> getAllFields() {
        List<Field> fields = fieldRepository.findAll();

        return fields.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    private FieldResponse mapToResponse(Field field) {
        return new FieldResponse(
                field.getId(),
                field.getName(),
                field.getAddress(),
                field.getCategory(),
                field.getPricePerHour(),
                field.getPhoneNumber(),
                field.getDescription(),
                field.getImageURL(),
                field.isHasLighting(),
                field.isIndoor(),
                field.getOpenTime(),
                field.getCloseTime(),
                field.getOwner().getId(),
                field.getOwner().getFirstName() + " " + field.getOwner().getLastName()
        );
    }

}

