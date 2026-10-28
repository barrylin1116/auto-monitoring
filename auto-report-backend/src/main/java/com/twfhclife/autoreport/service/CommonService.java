package com.twfhclife.autoreport.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Slf4j
@Service
public class CommonService {

    public String getRandom(Integer size) throws NoSuchAlgorithmException {
        String numStr = "1";
        for (int i = 0; i < size; i++) {
            numStr += "0";
        }
        Long num = Long.valueOf(numStr);
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        int verifiCode = (int) Math.ceil(random.nextFloat() * num);
        String verifiCodeStr = String.valueOf(verifiCode);
        while (verifiCodeStr.length() < size) {
            verifiCode = (int) Math.ceil(random.nextFloat() * num);
            verifiCodeStr = String.valueOf(verifiCode);
        }
        return verifiCodeStr;
    }
}
