package com.SportConnect.demo.service;


import com.SportConnect.demo.dto.FieldRequest;
import com.SportConnect.demo.dto.FieldResponse;
import com.SportConnect.demo.model.Field;
import com.SportConnect.demo.model.User;
import com.SportConnect.demo.repository.FieldRepository;
import com.SportConnect.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FieldService {

    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public FieldService(FieldRepository fieldRepository, UserRepository userRepository, CloudinaryService cloudinaryService) {
        this.fieldRepository = fieldRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public FieldResponse addField(FieldRequest request, String ownerEmail, MultipartFile imageFile) throws IOException { // <--- Adaugă MultipartFile
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Proprietarul nu a fost gasit!"));

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(imageFile);
        } else {
            imageUrl = request.imageURL();
        }

        Field field = new Field(
                request.name(),
                request.address(),
                request.category(),
                request.pricePerHour(),
                request.phoneNumber(),
                request.description(),
                imageUrl, // <--- URL-ul generat de Cloudinary
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

    public List<FieldResponse> getFieldsByCategory(String category) {
        List<Field> fields = fieldRepository.findByCategoryIgnoreCase(category);

        return fields.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FieldResponse getFieldById(Long id) {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Terenul cu ID-ul " + id + " nu a fost găsit!"));

        return mapToResponse(field);
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


    public FieldResponse updateField(Long id, FieldRequest request, String currentUserEmail, MultipartFile imageFile) throws IOException {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Terenul nu a fost gasit!"));

        boolean isAdmin = userRepository.findByEmail(currentUserEmail)
                .map(u -> u.getRole().name().equals("ROLE_ADMIN")).orElse(false);

        if (!field.getOwner().getEmail().equals(currentUserEmail) && !isAdmin) {
            throw new RuntimeException("Nu ai permisiunea să modifici acest teren!");
        }

        field.setName(request.name());
        field.setAddress(request.address());
        field.setCategory(request.category());
        field.setPricePerHour(request.pricePerHour());
        field.setPhoneNumber(request.phoneNumber());
        field.setDescription(request.description());
        field.setHasLighting(request.hasLighting());
        field.setIndoor(request.isIndoor());
        field.setOpenTime(request.openTime());
        field.setCloseTime(request.closeTime());

        if (imageFile != null && !imageFile.isEmpty()) {
            String newImageUrl = cloudinaryService.uploadImage(imageFile);
            field.setImageURL(newImageUrl);
        }

        Field updatedField = fieldRepository.save(field);
        return mapToResponse(updatedField);
    }

    public void deleteField(Long id, String currentUserEmail) {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Terenul nu a fost gasit!"));

        boolean isAdmin = userRepository.findByEmail(currentUserEmail)
                .map(u -> u.getRole().name().equals("ROLE_ADMIN")).orElse(false);

        if (!field.getOwner().getEmail().equals(currentUserEmail) && !isAdmin) {
            throw new RuntimeException("Nu ai permisiunea să ștergi acest teren!");
        }

        fieldRepository.delete(field);
    }

    public List<FieldResponse> searchFields(String name, String category, Double maxPrice) {
        return fieldRepository.searchFields(name, category, maxPrice)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}

