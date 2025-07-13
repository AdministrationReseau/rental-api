package inc.yowyob.rental_api.driver.mapper;

import inc.yowyob.rental_api.driver.dto.DriverDto;
import inc.yowyob.rental_api.driver.entities.Driver;
import inc.yowyob.rental_api.user.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    // On mappe les champs correspondants.
    // L'âge sera calculé dans le service.
    @Mapping(target = "age", ignore = true) // On ignore le mapping direct de l'âge
    
    // Mappages de l'entité User
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "profileImageUrl", source = "user.profileImageUrl")

    // Mappages de l'entité Driver
    @Mapping(target = "driverId", source = "driver.driverId")
    @Mapping(target = "organizationId", source = "driver.organizationId")
    @Mapping(target = "agencyId", source = "driver.agencyId")
    @Mapping(target = "dateOfBirth", source = "driver.dateOfBirth")
    @Mapping(target = "licenseNumber", source = "driver.licenseNumber")
    @Mapping(target = "licenseType", source = "driver.licenseType")
    @Mapping(target = "licenseExpiry", source = "driver.licenseExpiry")
    @Mapping(target = "experience", source = "driver.experience")
    @Mapping(target = "rating", source = "driver.rating")
    @Mapping(target = "registrationId", source = "driver.registrationId")
    @Mapping(target = "cni", source = "driver.cni")
    @Mapping(target = "position", source = "driver.position")
    @Mapping(target = "department", source = "driver.department")
    @Mapping(target = "staffStatus", source = "driver.staffStatus")
    @Mapping(target = "hourlyRate", source = "driver.hourlyRate")
    @Mapping(target = "workingHours", source = "driver.workingHours")
    @Mapping(target = "hireDate", source = "driver.hireDate")
    @Mapping(target = "status", source = "driver.status")
    @Mapping(target = "createdAt", source = "driver.createdAt")
    @Mapping(target = "updatedAt", source = "driver.updatedAt")
    DriverDto toDto(Driver driver, User user);
}