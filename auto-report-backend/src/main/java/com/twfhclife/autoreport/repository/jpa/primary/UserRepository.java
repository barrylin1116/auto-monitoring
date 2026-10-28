package com.twfhclife.autoreport.repository.jpa.primary;

import com.twfhclife.autoreport.entity.primary.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface UserRepository extends JpaRepository<User, String> {

    @Query(
            value = "select " +
                    "	top 1 user_id," +
                    "	ESERVICE_JD.dbo.FN_DEBASE64(roc_id) as ROC_ID,"+
                    "	mobile," +
                    "	ESERVICE_JD.DBO.FN_DEBASE64(email) as EMAIL," +
                    "	login_fail_count," +
                    "	last_chang_password_date," +
                    "	sms_flag," +
                    "	mail_flag,"+
                    "	fb_id," +
                    "	moica_id," +
                    "	user_type," +
                    "	create_date," +
                    "	create_user, " +
                    "	ESERVICE_JD.dbo.FN_DEBASE64(user_name) as USER_NAME," +
                    "	online_flag," +
                    "	login_fail_count," +
                    "	status," +
                    "	LOGIN_TIME," +
                    "	CLAUSE_FLAG," +
                    "	JD_LOGGED," +
                    "	SIGN_LOGED," +
                    "	ACTION_LOGGED," +
                    "	IC_ID," +
                    "	EFFECTIVE_DATE," +
                    "	EXPIRATION_DATE," +
                    "	ESERVICE_JD.DBO.FN_DEBASE64(INITPASSWORD) as INITPASSWORD," +
                    "	CLAUSE_FLAG," +
                    "	SERIAL_NUM," +
                    "	LOGIN_SIZE," +
                    "	AES_PASSWORD " +
                    "from {h-schema}vw_USERS_01 " +
                    "where LOWER(?1) = LOWER(USER_ID)",
            nativeQuery = true
    )
    User getUserByAccount(String account);
}
