package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.request.CreateDisputeRequest;
import org.example.order_app_backend.dto.request.ResolveDisputeRequest;
import org.example.order_app_backend.dto.response.DisputeResponse;

import java.util.List;

public interface DisputeService {

    DisputeResponse createDispute(CreateDisputeRequest request);

    DisputeResponse getDisputeById(Long id);

    List<DisputeResponse> getMyDisputes();

    DisputeResponse resolveDispute(Long id, ResolveDisputeRequest request);
}

