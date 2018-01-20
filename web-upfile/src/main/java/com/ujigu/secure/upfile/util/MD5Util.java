package com.ujigu.secure.upfile.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.util.DigestUtils;

/**
 * 简单MD5加密
 * 
 * @author huaiyu.du
 *
 */
public class MD5Util {

    public static String encode(String password) {
        if (password == null) {
            throw new IllegalArgumentException();
        }
        byte[] enP = null;
        try {
            enP = password.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            //			e.printStackTrace();
        }
        return DigestUtils.md5DigestAsHex(enP);
    }

    /**
     * 计算文件md5sum
     * 
     * zhi.liu
     * @param fileLocation
     * @return
     * @throws FileNotFoundException
     */
    public static String md5sum(String fileLocation) throws FileNotFoundException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            File f = new File(fileLocation);
            InputStream is = new FileInputStream(f);
            byte[] buffer = new byte[8192];
            int read = 0;
            try {
                while ((read = is.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
                byte[] md5sum = digest.digest();
                BigInteger bigInt = new BigInteger(1, md5sum);
                String output = bigInt.toString(16);
                return output;
            } catch (IOException e) {
                //				throw new RuntimeException("Unable to process file for MD5", e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    //					throw new RuntimeException("Unable to close input stream for MD5 calculation", e);
                }
            }
        } catch (NoSuchAlgorithmException e1) {
            //			e1.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        /*try {
        	System.out.println(md5sum("/home/mac/桌面/色情图片/large_pQCp_532c039124.jpg"));
        	//d666e54ae4100eb9d57a5a7068b85118
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        }*/
        String body = "src=\"http://www.mop.com\"";
        int startInt = body.indexOf("\"") + 1;
        int lastInt = body.lastIndexOf("\"");
        String bodyString = body.substring(startInt, lastInt);
        System.out.println(bodyString);
    }
}
