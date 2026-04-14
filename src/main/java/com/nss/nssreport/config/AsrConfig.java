package com.nss.nssreport.config;


import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class AsrConfig {

    // Берёт значение из application.properties
    @Value("${asr.excel.path}")
    private String excelPath;

    public String getExcelPath() {
        return excelPath;
    }
}
