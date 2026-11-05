package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.request.ClientCrashLogRequest;

public interface SystemLogService {

    void recordClientCrashLog(ClientCrashLogRequest request);
}

