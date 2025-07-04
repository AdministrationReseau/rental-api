// PATH: src/test/java/inc/yowyob/rental_api/driver/controller/DriverControllerTest.java
package inc.yowyob.rental_api.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import inc.yowyob.rental_api.driver.dto.CreateDriverDto;
import inc.yowyob.rental_api.driver.dto.DriverDto;
import inc.yowyob.rental_api.driver.service.DriverService;
import inc.yowyob.rental_api.security.jwt.JwtTokenProvider;
import inc.yowyob.rental_api.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest est optimisé pour ne tester que la couche Web.
// Il désactive la plupart des AutoConfigurations (comme Cassandra, etc.)
@WebMvcTest(DriverController.class)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Le contrôleur a besoin du service, on le mock.
    @MockBean
    private DriverService driverService;

    // La sécurité de Spring a besoin de ces beans, on les mock aussi
    // pour éviter qu'il ne tente de créer les vrais.
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    // Simule un utilisateur connecté avec la permission DRIVER_WRITE
    @WithMockUser(authorities = "DRIVER_WRITE")
    void createDriver_withValidDataAndPermission_shouldReturnCreated() throws Exception {
        // GIVEN
        CreateDriverDto createDto = CreateDriverDto.builder()
                .userId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .age(30)
                .licenseNumber("LICENSE123")
                .licenseType("B")
                .build();
        
        // Simuler la réponse du service mocké
        given(driverService.createDriver(any(CreateDriverDto.class), any())).willReturn(DriverDto.builder().build());

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/drivers")
                        .with(csrf()) // Ajoute un token CSRF valide pour les tests
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "SOME_OTHER_PERMISSION")
    void createDriver_withoutPermissions_shouldReturnForbidden() throws Exception {
        // GIVEN un DTO valide
        CreateDriverDto createDto = CreateDriverDto.builder()
                .userId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .age(30)
                .licenseNumber("LICENSE123")
                .licenseType("B")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/drivers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden()); // Vérifie bien le 403
    }
    
    @Test
    @WithMockUser(authorities = "DRIVER_WRITE")
    void createDriver_withInvalidData_shouldReturnBadRequest() throws Exception {
        // GIVEN un DTO INVALIDE (age est manquant)
        CreateDriverDto invalidDto = CreateDriverDto.builder()
                .userId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .licenseNumber("LICENSE123")
                .licenseType("B")
                .build();
        
        // WHEN & THEN
        mockMvc.perform(post("/api/v1/drivers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest()); // Vérifie bien le 400
    }
}