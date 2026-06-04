package com.hanson.network_device_monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.RegisterDeviceRequest;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.SubmitStatusRequest;
import com.hanson.network_device_monitor.model.NetworkDeviceStatus;
import com.hanson.network_device_monitor.model.NetworkDeviceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NetworkDeviceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void fullDeviceLifecycle() throws Exception {
		// 1. Register a device
		RegisterDeviceRequest regReq = new RegisterDeviceRequest(
				"Router-Core-01", NetworkDeviceType.ROUTER, "10.0.0.1", "Data Center A");

		MvcResult regResult = mockMvc.perform(post("/api/devices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(regReq)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Router-Core-01"))
				.andExpect(jsonPath("$.deviceType").value("ROUTER"))
				.andExpect(jsonPath("$.stale").value(true))
				.andReturn();

		String deviceId = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("id").asText();

		// 2. Submit a status report
		SubmitStatusRequest statusReq = new SubmitStatusRequest(NetworkDeviceStatus.ONLINE, "All interfaces up");

		mockMvc.perform(post("/api/devices/" + deviceId + "/status")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(statusReq)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("ONLINE"))
				.andExpect(jsonPath("$.message").value("All interfaces up"));

		// 3. List all devices — should show current status
		mockMvc.perform(get("/api/devices"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].currentStatus").value("ONLINE"))
				.andExpect(jsonPath("$[0].stale").value(false));

		// 4. Get device detail
		mockMvc.perform(get("/api/devices/" + deviceId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.recentReports").isArray())
				.andExpect(jsonPath("$.recentReports[0].status").value("ONLINE"));
	}

	@Test
	void registerDevice_withMissingName_shouldReturn400() throws Exception {
		String badJson = "{\"deviceType\":\"ROUTER\",\"host\":\"192.168.1.1\",\"location\":\"HQ\"}";

		mockMvc.perform(post("/api/devices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(badJson))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation Error"));
	}

	@Test
	void getDevice_withUnknownId_shouldReturn404() throws Exception {
		mockMvc.perform(get("/api/devices/00000000-0000-0000-0000-000000000000"))
				.andExpect(status().isNotFound());
	}

	@Test
	void submitStatus_forUnknownDevice_shouldReturn404() throws Exception {
		String json = "{\"status\":\"ONLINE\"}";

		mockMvc.perform(post("/api/devices/00000000-0000-0000-0000-000000000000/status")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isNotFound());
	}

}
