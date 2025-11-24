package functionapp;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

import datastructures.CloudResponse;

class CloudHelperTest {

    private final CloudHelper testHelper = new CloudHelper();

    private HttpRequestMessage<Optional<String>> mockRequest() {
        HttpRequestMessage<Optional<String>> request = mock(HttpRequestMessage.class);
        doAnswer(invocation -> {
            HttpStatus status = (HttpStatus) invocation.getArguments()[0];
            return new FakeResponseBuilder().status(status);
        }).when(request).createResponseBuilder(any(HttpStatus.class));
        return request;
    }

    @Test
    void handleResponseTest() {
        HttpRequestMessage<Optional<String>> request = mockRequest();
        CloudResponse testCloudResponse = new CloudResponse(200, "success", null);

        HttpResponseMessage response = testHelper.handleResponse(testCloudResponse, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
    }

    // @Test
    // void handleResponseExceptionTest() throws Exception {
    //     HttpRequestMessage<Optional<String>> request = mockRequest();

    //     ObjectMapper mockMapper = mock(ObjectMapper.class);
    //     when(mockMapper.writeValueAsString(any())).thenThrow(new RuntimeException("forced exception"));

    //     Field f = CloudHelper.class.getDeclaredField("objectMapper");
    //     f.setAccessible(true);
    //     f.set(testHelper, mockMapper);

    //     CloudResponse testCloudResponse = new CloudResponse(200, "fail", null);
    //     HttpResponseMessage response = testHelper.handleResponse(testCloudResponse, request);

    //     assertNotNull(response);
    //     assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    // }

    @Test
    void handleErrorTest() {
        HttpRequestMessage<Optional<String>> request = mockRequest();

        HttpResponseMessage response = testHelper.handleError(request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }
}
