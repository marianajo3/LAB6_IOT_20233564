package pe.lab6.measurements;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class MeasurementControllerTest {
 MockMvc mvc;
 @BeforeEach void setup(){mvc=MockMvcBuilders.standaloneSetup(new MeasurementController(8081)).build();}
 @Test void savesAndReadsLatest() throws Exception {
  mvc.perform(post("/api/measurements").contentType(MediaType.APPLICATION_JSON).content("{\"deviceId\":\"sensor-01\",\"humidity\":20,\"temperature\":28}")).andExpect(status().isCreated());
  mvc.perform(get("/api/measurements/latest").param("deviceId","sensor-01")).andExpect(status().isOk()).andExpect(jsonPath("$.humidity").value(20));
 }
 @Test void unknownDeviceReturns404() throws Exception {mvc.perform(get("/api/measurements/latest").param("deviceId","missing")).andExpect(status().isNotFound());}
 @Test void invalidReadingReturns400() throws Exception {mvc.perform(post("/api/measurements").contentType(MediaType.APPLICATION_JSON).content("{\"deviceId\":\"\",\"humidity\":120,\"temperature\":28}")).andExpect(status().isBadRequest());}
}
