package com.hydroyura.prodms.gateway.server.mapper;

import com.hydroyura.prodms.archive.client.model.res.GetUnitRes;
import com.hydroyura.prodms.gateway.server.model.res.GetUnitDetailedRes;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-02-12T22:35:04+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.13 (Azul Systems, Inc.)"
)
public class GetUnitResToGetUnitDetailResMapperImpl implements GetUnitResToGetUnitDetailResMapper {

    @Override
    public GetUnitDetailedRes toDestination(GetUnitRes source) {
        if ( source == null ) {
            return null;
        }

        GetUnitDetailedRes getUnitDetailedRes = new GetUnitDetailedRes();

        getUnitDetailedRes.setNumber( source.getNumber() );
        getUnitDetailedRes.setName( source.getName() );
        getUnitDetailedRes.setType( source.getType() );
        getUnitDetailedRes.setStatus( source.getStatus() );
        getUnitDetailedRes.setVersion( source.getVersion() );
        getUnitDetailedRes.setCreatedAt( source.getCreatedAt() );
        getUnitDetailedRes.setUpdatedAt( source.getUpdatedAt() );
        getUnitDetailedRes.setAdditional( source.getAdditional() );
        Collection<GetUnitRes.UnitHist> collection = source.getHistory();
        if ( collection != null ) {
            getUnitDetailedRes.setHistory( new ArrayList<GetUnitRes.UnitHist>( collection ) );
        }

        return getUnitDetailedRes;
    }
}
