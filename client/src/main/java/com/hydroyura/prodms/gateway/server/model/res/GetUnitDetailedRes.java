package com.hydroyura.prodms.gateway.server.model.res;

import com.hydroyura.prodms.archive.client.model.enums.UnitStatus;
import com.hydroyura.prodms.archive.client.model.enums.UnitType;
import com.hydroyura.prodms.archive.client.model.res.GetUnitRes.UnitHist;
import com.hydroyura.prodms.files.server.api.enums.DrawingType;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class GetUnitDetailedRes {

    private String number;
    private String name;
    private UnitType type;
    private UnitStatus status;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;
    private String additional;
    private Collection<UnitHist> history;
    private Map<DrawingType, String> urls = new HashMap<>();

}
