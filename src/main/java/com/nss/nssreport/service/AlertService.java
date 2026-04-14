package com.nss.nssreport.service;

import com.nss.nssreport.domain.dto.AlertDto;
import java.util.List;

public interface AlertService {

    List<AlertDto> findAsrDropAlerts();
}
