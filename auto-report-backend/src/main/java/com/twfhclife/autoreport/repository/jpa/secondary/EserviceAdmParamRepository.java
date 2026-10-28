package com.twfhclife.autoreport.repository.jpa.secondary;

import com.twfhclife.autoreport.entity.secondary.EserviceAdmParamPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EserviceAdmParamRepository extends JpaRepository<EserviceAdmParamPo, String> {
    @Query(
            value = "SELECT top 1 P.PARAMETER_ID, P.PARAMETER_NAME, P.PARAMETER_VALUE " +
                    "FROM vw_PARAMETER_CATEGORY_01 PC , vw_PARAMETER_01 P " +
                    "WHERE PC.PARAMETER_CATEGORY_ID = P.PARAMETER_CATEGORY_ID AND PC.STATUS = P.STATUS AND PC.STATUS = 1 AND P.PARAMETER_CODE = ?2 AND PC.SYSTEM_ID = P.SYSTEM_ID AND PC.SYSTEM_ID = ?1 ",
            nativeQuery = true
    )
    public EserviceAdmParamPo getParameterByCode(String systemIdJd, String parameterCode);

}
