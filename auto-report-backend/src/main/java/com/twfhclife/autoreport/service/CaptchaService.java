package com.twfhclife.autoreport.service;

import com.twfhclife.autoreport.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final String KEY_PREFIX = "captcha:";

    private final StringRedisTemplate redisTemplate;
    private final CommonService commonService;
    private final AuthProperties authProperties;

    
    public Captcha generate() throws NoSuchAlgorithmException {
        String code = commonService.getRandom(authProperties.getCaptcha().getLength());
        String captchaId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                KEY_PREFIX + captchaId,
                code,
                Duration.ofSeconds(authProperties.getCaptcha().getTtlSeconds()));
        return new Captcha(captchaId, convertCaptchaCodeToImgBase64(code));
    }

    
    public boolean validate(String captchaId, String captchaCode) {
        if (captchaId == null || captchaCode == null) {
            return false;
        }
        String key = KEY_PREFIX + captchaId;
        String expected = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        return expected != null && expected.equalsIgnoreCase(captchaCode.trim());
    }

    
    public String convertCaptchaCodeToImgBase64(String captchaCode) {
        int width = 165; 
        int height = 50; 
        BufferedImage captchaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = captchaImage.createGraphics();
        int fontSize = 24;

        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, fontSize));

        
        char[] codeSnippets = captchaCode.toCharArray();

        
        for (int i = 0; i < codeSnippets.length; i++) {
            double sDeg = (createDynamicNumber() * 30 * Math.PI) / 180;
            int x = 10 + i * (fontSize + 5);
            int y = height / 2 + fontSize / 2;
            g2d.translate(x, y);
            g2d.rotate(sDeg);
            g2d.setColor(createNumberColor());
            FontMetrics fontMetrics = g2d.getFontMetrics();

            int textWidth = fontMetrics.stringWidth(String.valueOf(codeSnippets[i]));
            int textHeight = fontMetrics.getHeight();
            int textX = -textWidth / 2; 
            int textY = textHeight / 2 - fontMetrics.getDescent(); 

            g2d.drawString(String.valueOf(codeSnippets[i]), textX, textY);
            g2d.rotate(-sDeg);
            g2d.translate(-x, -y);
        }

        
        for (int i = 0; i <= 5; i++) {
            g2d.setColor(createRandomColor());
            int x1 = (int) (createDynamicNumber() * width);
            int y1 = (int) (createDynamicNumber() * height);
            int x2 = (int) (createDynamicNumber() * width);
            int y2 = (int) (createDynamicNumber() * height);
            g2d.drawLine(x1, y1, x2, y2);
        }

        for (int i = 0; i < 30; i++) {
            g2d.setColor(createRandomColor());
            int x = (int) (createDynamicNumber() * width);
            int y = (int) (createDynamicNumber() * height);
            g2d.drawLine(x, y, x + 1, y + 1);
        }
        g2d.dispose();

        return imgConvertToBase64Str(captchaImage);
    }

    
    private static Color createRandomColor() {
        int r = (int) (createDynamicNumber() * 256);
        int g = (int) (createDynamicNumber() * 256);
        int b = (int) (createDynamicNumber() * 256);
        return new Color(r, g, b);
    }

    
    private static double createDynamicNumber() {
        String patchNum = new StringBuilder(String.valueOf(System.currentTimeMillis())).reverse().toString();
        String patchNumReverse = new StringBuilder(patchNum).reverse().toString();
        double randomValue = Math.random();
        String numStr = "0." + String.valueOf(randomValue).substring(2) + patchNum + patchNumReverse;
        return Double.parseDouble(numStr);
    }

    private static Color createNumberColor() {
        int r = 64;
        int g = 64;
        int b = 64;
        return new Color(r, g, b);
    }

    
    private String imgConvertToBase64Str(BufferedImage captchaImage) {
        try {
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(captchaImage, "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            baos.close();

            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public record Captcha(String captchaId, String imageBase64) {
    }
}
