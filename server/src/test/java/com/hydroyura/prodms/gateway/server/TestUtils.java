package com.hydroyura.prodms.gateway.server;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtils {

    public static String URI_ARCHIVE_GET_UNIT = "/api/v1/units/%s";

    public static String URI_FILES_GET_URLS = "/api/v1/drawings/%s";


    public static String ERROR_MSG_FILES_UNIT_NOT_FOUND = "Files with id = [%s] not found";
    public static String ERROR_MSG_ARCHIVE_UNIT_NOT_FOUND = "Files with id = [%s] not found";


    public static String UNIT_NUMBER_1 = "test-number-1";
}
