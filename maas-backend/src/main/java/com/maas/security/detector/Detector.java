package com.maas.security.detector;

import com.maas.security.dto.InspectionResult;
import com.maas.security.entity.DetectorType;
import com.maas.security.entity.SecurityRule;

import java.util.List;

public interface Detector {
    DetectorType supportedType();
    InspectionResult detect(String content, SecurityRule rule);
    List<String> detectMatches(String content, SecurityRule rule);
}
