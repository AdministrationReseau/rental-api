// src/main/java/inc/yowyob/rental_api/driver/mapper/DriverMapper.java
package inc.yowyob.rental_api.driver.mapper;

import inc.yowyob.rental_api.driver.dto.DriverDto;
import inc.yowyob.rental_api.driver.entities.Driver;
import inc.yowyob.rental_api.user.entities.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mapping(target = "driverId", source = "driver.driverId")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "organizationId", source = "driver.organizationId")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "profilePicture", source = "user.profileImageUrl")
    @Mapping(target = "age", source = "driver.age")
    @Mapping(target = "licenseNumber", source = "driver.licenseNumber")
    @Mapping(target = "licenseType", source = "driver.licenseType")
    @Mapping(target = "location", source = "driver.location")
    @Mapping(target = "idCardUrl", source = "driver.idCardUrl")
    @Mapping(target = "driverLicenseUrl", source = "driver.driverLicenseUrl")
    @Mapping(target = "assignedVehicleIds", source = "driver.assignedVehicleIds")
    @Mapping(target = "available", source = "driver.available")
    @Mapping(target = "rating", source = "driver.rating")
    @Mapping(target = "insuranceProvider", source = "driver.insuranceProvider")
    @Mapping(target = "insurancePolicy", source = "driver.insurancePolicy")
    @Mapping(target = "status", source = "driver.status")
    @Mapping(target = "createdAt", source = "driver.createdAt")
    @Mapping(target = "updatedAt", source = "driver.updatedAt")
    DriverDto toDto(Driver driver, User user);
}