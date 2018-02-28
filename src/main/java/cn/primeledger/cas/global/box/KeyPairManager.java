package cn.primeledger.cas.global.box;

import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.exception.KeyPairManagerException;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
/**
 * The class is used to manage public and private keys:
 * Includes generating public and private keys and saving them to local configuration files;
 * The public and private keys are read from the configuration file when restarting.
 *
 * @author Su Jiulong
 * @date 2018/2/23
 */
@Slf4j
public class KeyPairManager {
    /**
     * private key
     */
    private String priKey;

    /**
     * public key
     */
    private String pubKey;

    /**
     * This method is used to generate a public and private keys,
     * and to store it in a local configuration file, and then to the public key.
     *
     * @param filePath : the address of the configuration file
     * @return pubKey
     */
    public String createKeyPair(String filePath) {
        if (StringUtil.isNullOrEmpty(filePath)) {
            return "The filePath is null or empty";
        }
        //Get the public and private key pairs from ECKey class.
        ECKey casKey = new ECKey();
        KeyPair keyPair = casKey.getKeyPair();
        //Extract the public and private keys from the keyPair.
        priKey = keyPair.getPriKey();
        pubKey = keyPair.getPubKey();
        try {
            writeKeyPair2cfg(filePath, priKey, pubKey);
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                LOGGER.error(e.getMessage());
                return "The filePath not found";
            } else {
                LOGGER.error(e.getMessage());
                return "Failed to write the public and private key to the configuration file";
            }
        }
        return pubKey;
    }

    /**
     * This method is to take out the public and private keys that is read from the configuration file,
     * and verify that it is the same pair and return the public key after the verification is successful.
     *
     * @param filePath : the address of the configuration file
     * @return pubKey
     */
    public String validateKeyPair(String filePath) {
        if (StringUtil.isNullOrEmpty(filePath)) {
            return "The filePath is null or empty";
        }
        String keyPairOfJsonString = null;
        try {
            //Read the public and private key from the configuration file.
            keyPairOfJsonString = readeKeyPair(filePath);
            //Converts the json string of the keyPairOfJsonString to a json object.
            JSONObject jsonObject = JSONObject.parseObject(keyPairOfJsonString);
            //Extract the public and private key from the json object.
            priKey = jsonObject.getString("priKey");
            pubKey = jsonObject.getString("pubKey");
            //Determines whether the public or private key is an empty string.
            if ("".equals(priKey) || "".equals(pubKey)) {
                return "The public or private key is empty string";
            }
            //Check that the public and private keys retrieved from the configuration file are
            // a pair of public and private keys.
            if (ECKey.checkPriKeyAndPubKey(priKey, pubKey)) {
                return pubKey;
            }
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                LOGGER.error(e.getMessage());
                return "The filePath not found!";
            } else {
                LOGGER.error(e.getMessage());
                return "Failed to read the configuration file";
            }
        } catch (JSONException e) {
            LOGGER.error(e.getMessage());
            return "Json string format error";
        }
        return "The keyPair has been changed, please reset the public and private key.";
    }

    /**
     * The method that write the public and private keys to the configuration file.
     *
     * @param filePath : the address of the configuration file
     * @param priKey   : private key
     * @param pubKey   : public key
     */
    private void writeKeyPair2cfg(String filePath, String priKey, String pubKey) throws IOException {
        // Splice the public and private key into json format.
        String keyPair2Json = spliceKeyPair2Json(priKey, pubKey);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath);
            //Call the write() method to write the data.
            fileWriter.write(keyPair2Json);
            //Flush the data to disk.
            fileWriter.flush();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("The filePath not found!");
        } catch (IOException e) {
            throw new KeyPairManagerException(e.toString());
        } finally {
            try {
                if (fileWriter != null) {
                    // Close fileWriter
                    fileWriter.close();
                }
            } catch (IOException e) {
                throw new KeyPairManagerException(e.toString());
            }
        }
    }

    /**
     * The method that read the public and private keys from the configuration file.
     *
     * @param filePath : the address of the configuration file
     * @return keyPair of public and private key
     */
    private String readeKeyPair(String filePath) throws IOException {
        FileReader fileReader = null;
        //keyPair of json String
        String keyPairOfJsonString = null;
        try {
            fileReader = new FileReader(filePath);
            //Buffer character array
            char[] buf = new char[200];
            int num = 0;
            //Read the keyPairOfJsonString in the local configuration file to the buffer array via fileReader.
            while ((num = fileReader.read(buf)) != -1) {
                //Converts public and private key characters in the buffer array to strings.
                keyPairOfJsonString = new String(buf, 0, num);
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("The filePath not found!");
        } catch (IOException e) {
            throw new KeyPairManagerException(e.toString());
        } finally {
            try {
                if (fileReader != null) {
                    // Close fileWriter
                    fileReader.close();
                }
            } catch (IOException e) {
                throw new KeyPairManagerException(e.toString());
            }
        }
        return keyPairOfJsonString;
    }

    /**
     * The method of splicing the public and private key into json format output.
     *
     * @return keyPair2Json：keyPair of json
     */
    private String spliceKeyPair2Json(String priKey, String pubKey) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("priKey", priKey);
        jsonObject.put("pubKey", pubKey);
        return jsonObject.toString();
    }
}
