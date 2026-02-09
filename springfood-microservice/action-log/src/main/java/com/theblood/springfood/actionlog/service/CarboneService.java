package com.theblood.springfood.actionlog.service;

import com.google.gson.Gson;
import com.theblood.springfood.actionlog.service.dto.carbone.CarboneBody;
import com.theblood.springfood.actionlog.service.dto.carbone.CarboneOption;
import com.theblood.springfood.actionlog.service.dto.carbone.CarboneResponseDTO;
import com.theblood.springfood.actionlog.service.dto.carbone.CarboneResponseData;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CarboneService {

    @Autowired
    OkHttpClient client;
    @Value("${carbone.base-url}")
    private String carboneBaseUrl;
    @Autowired
    private Gson gson;

    private static boolean isNullOrEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    protected Request createRequest(String url, String method, RequestBody body) {
        return new Request.Builder().url(url).method(method, body).addHeader("Content-Type", "application/json").build();
    }

    protected Request createRequest(String url, String method, RequestBody body, Map<String, String> additionalHeaders) {
        Request.Builder reqBuilder = new Request.Builder();
        reqBuilder.url(url).method(method, body).addHeader("Content-Type", "application/json");
        for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
            reqBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        return reqBuilder.build();
    }

    protected String buildJsonBodyRender(String fileTemplate, CarboneOption carboneOption, Object data) {
        CarboneBody carboneBodyDTO = CarboneBody.builder().fileName(fileTemplate).options(carboneOption).data(data).build();
        return gson.toJson(carboneBodyDTO);
    }

    protected String buildJsonBodyConvert(String fileTemplate, CarboneOption carboneOption) {
        CarboneBody carboneBodyDTO = CarboneBody.builder().fileName(fileTemplate).options(carboneOption).build();
        return gson.toJson(carboneBodyDTO);
    }

    protected String buildJsonBodyMultiRender(CarboneOption carboneOption, Object dataset) {
        CarboneBody carboneBodyDTO = CarboneBody.builder().options(carboneOption).dataset(dataset).build();
        return gson.toJson(carboneBodyDTO);
    }

    protected String sendToReportCore(String requestPath, String jsonBody) {
        RequestBody requestBody = RequestBody.create(jsonBody.getBytes());
        Request request = createRequest(carboneBaseUrl + requestPath, "POST", requestBody);
        Response responses;
        try {
            responses = client.newCall(request).execute();
            if (responses.isSuccessful()) {
                return responses.body().string();
            } else {
                throw new RuntimeException(String.format("Carbone server response with error %s", responses.code()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot connect to Carbone Server", e);
        }
    }

    protected String sendToReportCore(String requestPath, String jsonBody, Map<String, String> additionalHeaders) {
        RequestBody requestBody = RequestBody.create(jsonBody.getBytes());
        Request request = createRequest(carboneBaseUrl + requestPath, "POST", requestBody, additionalHeaders);
        Response responses = null;
        try {
            responses = client.newCall(request).execute();
            if (responses.isSuccessful()) {
                return responses.body().string();
            } else {
                throw new RuntimeException(String.format("Carbone server response with error %s", responses.code()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot connect to Carbone Server", e);
        }
    }

    public CarboneResponseData renderReport(Object inputJson, String templateFileName, String convertTo) {
        CarboneOption carboneOption = CarboneOption.builder()
                .convertTo(convertTo)
                .responseType("url")
                .timezone("Asia/Ho_Chi_Minh")
                .lang("vi")
                .build();
        return renderReport(inputJson, carboneOption, templateFileName);
    }

    public CarboneResponseData renderReport(Object inputJson, String templateFileName, String reportName, String convertTo) {
        CarboneOption carboneOption = CarboneOption.builder()
                .reportName(reportName)
                .convertTo(convertTo)
                .responseType("url")
                .timezone("Asia/Ho_Chi_Minh")
                .lang("vi")
                .build();
        return renderReport(inputJson, carboneOption, templateFileName);
    }

    public CarboneResponseData convert(String bucketName, String templateFileName, String convertTo) {
        CarboneOption carboneOption = CarboneOption.builder()
                .convertTo(convertTo)
                .responseType("url")
                .timezone("Asia/Ho_Chi_Minh")
                .saveTarget(true)
                .lang("vi")
                .build();
        return convert(bucketName, carboneOption, templateFileName);
    }

    public CarboneResponseData renderReport(Object inputJson, CarboneOption carboneOption, String templateFileName) {
        if (inputJson instanceof String && isNullOrEmpty(String.valueOf(inputJson))) inputJson = "{}";
        String body = buildJsonBodyRender(templateFileName, carboneOption, inputJson);
        LOG.info(">>>>>Request Report Core renderReport \n" + body);
        String response = sendToReportCore("/render", body);
        CarboneResponseDTO carboneResponse = gson.fromJson(response, CarboneResponseDTO.class);
        return carboneResponse.getData();
    }

    public CarboneResponseData convert(String bucketName, CarboneOption carboneOption, String templateFileName) {
        String body = buildJsonBodyConvert(templateFileName, carboneOption);
        LOG.info(">>>>>Request Report Core renderReport \n" + body);
        String response = sendToReportCore("/convert", body, Map.of("X-BUCKET-IN", bucketName, "X-BUCKET-OUT", bucketName));
        CarboneResponseDTO carboneResponse = gson.fromJson(response, CarboneResponseDTO.class);
        return carboneResponse.getData();
    }
}
