package com.higgsblock.global.chain.crypto;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.higgsblock.global.chain.crypto.exception.AddressFormatException;
import com.higgsblock.global.chain.crypto.exception.ECKeyCrypterException;
import com.higgsblock.global.chain.crypto.exception.ParamsErrorException;
import com.higgsblock.global.chain.crypto.utils.Base58;
import com.higgsblock.global.chain.crypto.utils.CryptoUtils;
import com.higgsblock.global.chain.crypto.utils.Sha256Hash;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.asn1.*;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.*;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;
import org.spongycastle.math.ec.FixedPointUtil;
import org.spongycastle.math.ec.custom.sec.SecP256K1Curve;
import org.spongycastle.util.encoders.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Public and private key and address generation classes,Public and private keys and addresses can be generated randomly,
 * the corresponding public and address can be generated according to the specified private key, or the corresponding
 * address can be generated according to the specified public key
 *
 * @author kongyu
 * @create 2018-02-23 15:38
 */
@Slf4j
public class ECKey {
    private static final int COMPRESS_PUBKEY_LENGTH = 33;
    private static final int UNCOMPRESS_PUBKEY_LENGTH = 65;
    private static final byte UNCOMPRESS_PRE_PUBKEY = 0x04;
    private static final byte COMPRESS_PRE_PUBKEY_EVEN = 0x02;
    private static final byte COMPRESS_PRE_PUBKEY_ODD = 0x03;
    private static final int MAX_SIGNATURE = 21;
    private static final int LOW_HEADER_BORDER = 27;
    private static final int MIDDLE_HEADER_BORDER = 31;
    private static final int HIGH_HEADER_BORDER = 34;
    private static final int LOOP_COUNT = 4;

    private static final Logger log = LoggerFactory.getLogger(ECKey.class);
    /**
     * The parameters of the secp256k1 curve that ECKey uses.
     */
    private static final String COMMON_CURVE = "secp256k1";

    private static final byte ADDRESS_HEADER_TEST = 0x00;

    private static final byte ADDRESS_HEADER_NORMAL = 0x01;

    private static final byte ADDRESS_HEADER_MULTISIG = 0x05;

    /**
     * Elliptic curve parameter set.
     */
    private static final ECDomainParameters CURVE;

    private static final BigInteger HALF_CURVE_ORDER;

    /**
     * Initialize the elliptic curve.
     */
    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName(COMMON_CURVE);

    /**
     * pseudorandom number generator
     */
    private static final SecureRandom SECURE_RANDOM;

    static {
        //Pseudo-random Numbers generated by different versions of the JVM.
        if (CryptoUtils.isAndroidRuntime()) {
            new LinuxSecureRandom();
        }

        FixedPointUtil.precompute(CURVE_PARAMS.getG(), 12);
        CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
                CURVE_PARAMS.getH());
        HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
        SECURE_RANDOM = new SecureRandom();
    }

    /**
     * The privateKey of key
     */
    protected final BigInteger priv;
    /**
     * The publicKey of key
     */
    protected final LazyECASPoint pub;
    /**
     * Public and private key pairs, including addresses
     */
    protected KeyPair keyPair;
    /**
     * Public-private key creation time
     */
    protected long creationTimeSeconds;

    public ECKey() {
        this(SECURE_RANDOM);
    }

    public ECKey(SecureRandom secureRandom) {
        if (null == secureRandom) {
            throw new ParamsErrorException("secureRandom is null");
        }
        this.keyPair = new KeyPair();


        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, secureRandom);
        generator.init(keygenParams);
        AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
        this.priv = privParams.getD();
        this.pub = new LazyECASPoint(CURVE.getCurve(), pubParams.getQ().getEncoded(true));
        this.creationTimeSeconds = CryptoUtils.currentTimeSeconds();

        this.keyPair.setPriKey(getPrivateKeyAsHex());
        this.keyPair.setPubKey(getPublicKeyAsHex());
    }

    public ECKey(KeyPair keyPair, boolean compressed) {
        if (null == keyPair) {
            throw new ParamsErrorException("params is null");
        }
        this.keyPair = new KeyPair();
        if (keyPair.getPriKey() == null && keyPair.getPubKey() == null) {
            throw new IllegalArgumentException("KeyPair requires at least private or public key");
        }
        this.priv = new BigInteger(1, CryptoUtils.HEX.decode(keyPair.getPriKey()));
        if (keyPair.getPubKey() == null) {
            // Derive public from private.
            ECPoint point = publicPointFromPrivateKey(this.priv);
            point = getPointWithCompression(point, compressed);
            this.pub = new LazyECASPoint(point);
        } else {
            this.pub = new LazyECASPoint(CURVE.getCurve(), CryptoUtils.HEX.decode(keyPair.getPubKey()));
        }
        this.keyPair.setPriKey(getPrivateKeyAsHex());
        this.keyPair.setPubKey(getPublicKeyAsHex());
    }

    private ECKey(BigInteger priv, ECPoint pub) {
        this(priv, new LazyECASPoint(checkNotNull(pub)));
    }

    private ECKey(BigInteger priv, LazyECASPoint pub) {
        if (null == pub) {
            throw new ParamsErrorException("LazyECASPoint is null");
        }
        this.keyPair = new KeyPair();
        this.priv = priv;
        this.pub = checkNotNull(pub);
        if (priv != null) {
            checkArgument(priv.bitLength() <= 32 * 8, "private key exceeds 32 bytes: %s bits", priv.bitLength());
            checkArgument(!priv.equals(BigInteger.ZERO));
            checkArgument(!priv.equals(BigInteger.ONE));
            this.keyPair.setPriKey(getPrivateKeyAsHex());
            this.keyPair.setPubKey(getPublicKeyAsHex());
        }
    }

    /**
     * The private key of the asn.1 encoding may be generated by the openssl method,
     * based on the private key of the asn.1 encoding format.
     *
     * @param asn1privkey
     * @return
     */
    public static ECKey fromASN1Key(byte[] asn1privkey) {
        return extractKeyFromASN1(asn1privkey);
    }

    public static ECPoint publicPointFromPrivateKey(BigInteger privKey) {
        if (null == privKey) {
            return null;
        }
        if (privKey.bitLength() > CURVE.getN().bitLength()) {
            privKey = privKey.mod(CURVE.getN());
        }
        return new FixedPointCombMultiplier().multiply(CURVE.getG(), privKey);
    }

    public static ECKey fromPrivateKey(KeyPair key) {
        if (null == key || null == key.getPriKey()) {
            return null;
        }
        return fromPrivateKey(CryptoUtils.HEX.decode(key.getPriKey()));
    }

    public static ECKey fromPrivateKey(String priKey) {
        if (null == priKey) {
            return null;
        }
        return fromPrivateKey(CryptoUtils.HEX.decode(priKey));
    }

    public static ECKey fromPublicKeyOnly(KeyPair key) {
        if (null == key || null == key.getPubKey()) {
            return null;
        }
        return fromPublicKeyOnly(CryptoUtils.HEX.decode(key.getPubKey()));
    }

    public static ECKey fromPublicKeyOnly(String pubKey) {
        if (null == pubKey) {
            return null;
        }
        return fromPublicKeyOnly(CryptoUtils.HEX.decode(pubKey));
    }

    /**
     * Verify that the public key is legal
     *
     * @param key
     * @return
     */
    public static boolean checkPublicKey(KeyPair key) {
        if (null == key || null == key.getPubKey()) {
            return false;
        }
        try {
            ECKey.fromPublicKeyOnly(CryptoUtils.HEX.decode(key.getPubKey()));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Verify that the address is in base58 encoding format.
     *
     * @param address
     * @return
     */
    public static boolean checkBase58Addr(String address) {
        if (null == address) {
            return false;
        }
        try {
            Base58.decodeChecked(address);
        } catch (AddressFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Verify that the public key and address match.
     *
     * @param pubKey
     * @param addr
     * @return
     * @throws SignatureException
     */
    public static boolean checkPubKeyAndAddr(String pubKey, String addr) {
        if (null == pubKey || null == addr) {
            return false;
        }
        String address = pubKey2Base58Address(pubKey);
        return addr.equals(address);
    }

    /**
     * Verify that the public and private keys match.
     *
     * @param key
     * @return
     * @throws SignatureException
     */
    public static boolean checkPriKeyAndPubKey(KeyPair key) {
        if (null == key) {
            return false;
        }
        if (null == key.getPubKey() || null == key.getPriKey()) {
            return false;
        }

        ECKey newKey = ECKey.fromPrivateKey(key);
        if (null == newKey) {
            return false;
        }
        return key.getPubKey().equals(newKey.getPublicKeyAsHex());
    }

    public static boolean checkPriKeyAndPubKey(String priKey, String pubKey) {
        if (null == pubKey || null == priKey) {
            return false;
        }
        ECKey newKey = ECKey.fromPrivateKey(priKey);
        if (null == newKey) {
            return false;
        }
        return pubKey.equals(newKey.getPublicKeyAsHex());
    }

    /**
     * Generate the address of the base58 encoding format.
     *
     * @param key
     * @return
     */
    public static String pubKey2Base58Address(KeyPair key) {
        if (null == key || null == key.getPubKey()) {
            return null;
        }
        byte[] hash160 = CryptoUtils.sha256hash160(CryptoUtils.HEX.decode(key.getPubKey()));
        if (null == hash160) {
            return null;
        }
        Preconditions.checkArgument(hash160.length == 20, "Addresses are 160-bit hashes, so you must provide 20 bytes");
        byte[] addressBytes = new byte[1 + hash160.length + 4];
        //address version ADDRESS_HEADER_NORMAL
        addressBytes[0] = (byte) ADDRESS_HEADER_TEST;
        System.arraycopy(hash160, 0, addressBytes, 1, hash160.length);
        byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, hash160.length + 1);
        System.arraycopy(checksum, 0, addressBytes, hash160.length + 1, 4);
        return Base58.encode(addressBytes);
    }

    public static String pubKey2Base58Address(String pubKey) {
        if (null == pubKey) {
            return null;
        }
        byte[] hash160 = CryptoUtils.sha256hash160(CryptoUtils.HEX.decode(pubKey));
        if (null == hash160) {
            return null;
        }
        Preconditions.checkArgument(hash160.length == 20, "Addresses are 160-bit hashes, so you must provide 20 bytes");
        byte[] addressBytes = new byte[1 + hash160.length + 4];
        //address version ADDRESS_HEADER_NORMAL
        addressBytes[0] = (byte) ADDRESS_HEADER_TEST;
        System.arraycopy(hash160, 0, addressBytes, 1, hash160.length);
        byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, hash160.length + 1);
        System.arraycopy(checksum, 0, addressBytes, hash160.length + 1, 4);
        return Base58.encode(addressBytes);
    }

    /**
     * Verify that the public key conforms to the format.
     *
     * @param key
     * @return
     */
    public static boolean isPublicKeyCanonical(KeyPair key) {
        if (null == key || null == key.getPubKey()) {
            return false;
        }
        byte[] pubkey = CryptoUtils.HEX.decode(key.getPubKey());
        if (pubkey.length < COMPRESS_PUBKEY_LENGTH) {
            return false;
        }
        // Uncompressed pubkey
        if (pubkey[0] == UNCOMPRESS_PRE_PUBKEY) {
            if (pubkey.length != UNCOMPRESS_PUBKEY_LENGTH) {
                return false;
            }
            return true;
        }
        // Compressed pubkey
        if (pubkey[0] == COMPRESS_PRE_PUBKEY_EVEN || pubkey[0] == COMPRESS_PRE_PUBKEY_ODD) {
            if (pubkey.length != COMPRESS_PUBKEY_LENGTH) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean isPublicKeyCanonical(String pubKey) {
        if (null == pubKey) {
            return false;
        }
        byte[] key = CryptoUtils.HEX.decode(pubKey);
        if (key.length < COMPRESS_PUBKEY_LENGTH) {
            return false;
        }
        // Uncompressed pubkey
        if (key[0] == UNCOMPRESS_PRE_PUBKEY) {
            if (key.length != UNCOMPRESS_PUBKEY_LENGTH) {
                return false;
            }
            return true;
        }
        // Compressed pubkey
        if (key[0] == COMPRESS_PRE_PUBKEY_EVEN || key[0] == COMPRESS_PRE_PUBKEY_ODD) {
            if (key.length != COMPRESS_PUBKEY_LENGTH) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static ECKey recoverFromSignature(int recId, ECASSignature sig, Sha256Hash message, boolean compressed) {
        Preconditions.checkArgument(recId >= 0, "recId must be positive");
        Preconditions.checkArgument(sig.r.signum() >= 0, "r must be positive");
        Preconditions.checkArgument(sig.s.signum() >= 0, "s must be positive");
        Preconditions.checkNotNull(message);
        // Curve order.
        BigInteger n = CURVE.getN();
        BigInteger i = BigInteger.valueOf((long) recId / 2);
        BigInteger x = sig.r.add(i.multiply(n));

        BigInteger prime = SecP256K1Curve.q;
        if (x.compareTo(prime) >= 0) {
            return null;
        }

        ECPoint point = decompressKey(x, (recId & 1) == 1);

        if (!point.multiply(n).isInfinity()) {
            return null;
        }
        BigInteger e = message.toBigInteger();

        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = sig.r.modInverse(n);
        BigInteger srInv = rInv.multiply(sig.s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, point, srInv);
        return ECKey.fromPublicKeyOnly(q.getEncoded(compressed));
    }

    /**
     * @param message
     * @param signature
     * @param pubkey
     * @return
     */
    public static boolean verifySign(String message, String signature, String pubkey) {
        if (null == message || null == signature || null == pubkey) {
            return false;
        }
        ECKey ecKey = ECKey.fromPublicKeyOnly(CryptoUtils.HEX.decode(pubkey));
        if (null == ecKey) {
            return false;
        }
        try {
            ecKey.verifyMessage(message, signature);
            return true;
        } catch (SignatureException e) {
            return false;
        }
    }

    public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
        if (Secp256k1Context.isEnabled()) {
            try {
                return NativeSecp256k1.verify(data, signature, pub);
            } catch (NativeSecp256k1Util.AssertFailException e) {
                log.error("Caught AssertFailException inside secp256k1", e);
                return false;
            }
        }
        return verify(data, ECASSignature.decodeFromDER(signature), pub);
    }

    public static String createMultiSigByBase58(int sigNum, List<String> pubKeyLists) {
        if (CollectionUtils.isEmpty(pubKeyLists) || 0 >= sigNum) {
            throw new ParamsErrorException("params is error");
        }
        if (sigNum > pubKeyLists.size()) {
            throw new ParamsErrorException("sigNum cannot large pubKeyLists.size()");
        }
        if (sigNum > MAX_SIGNATURE) {
            throw new ParamsErrorException("sigNum is too large than 21");
        }
        int length = pubKeyLists.size();
        byte[] array = new byte[1 + (length * 33)];
        array[0] = (byte) sigNum;
        for (int i = 0; i < pubKeyLists.size(); i++) {
            byte[] pubKeyByte = CryptoUtils.HEX.decode(pubKeyLists.get(i));
            System.arraycopy(pubKeyByte, 0, array, (i * 33) + 1, pubKeyByte.length);
        }

        byte[] hash160 = CryptoUtils.sha256hash160(array);
        byte[] bytes = hash160;

        Preconditions.checkArgument(hash160.length == 20, "Addresses are 160-bit hashes, so you must provide 20 bytes");
        byte[] addressBytes = new byte[1 + bytes.length + 4];

        //multisig address header byte
        addressBytes[0] = ADDRESS_HEADER_MULTISIG;
        System.arraycopy(bytes, 0, addressBytes, 1, bytes.length);
        byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, bytes.length + 1);
        System.arraycopy(checksum, 0, addressBytes, bytes.length + 1, 4);
        return Base58.encode(addressBytes);
    }

    public static String signMessage(String message, String priKey) {
        if (null == message || null == priKey) {
            String error = "params is null";
            throw new ParamsErrorException(error);
        }
        ECKey eckey = ECKey.fromPrivateKey(priKey);
        if (null == eckey) {
            return null;
        }
        byte[] data = CryptoUtils.formatMessageForSigning(message, Charset.forName("UTF-8"), null);
        Sha256Hash hash = Sha256Hash.twiceOf(data);
        return eckey.signMessage(hash, null);
    }

    private static boolean verify(byte[] data, ECASSignature signature, byte[] pub) {
        if (Secp256k1Context.isEnabled()) {
            try {
                return NativeSecp256k1.verify(data, signature.encodeToDER(), pub);
            } catch (NativeSecp256k1Util.AssertFailException e) {
                log.error("Caught AssertFailException inside secp256k1", e);
                return false;
            }
        }

        ECDSASigner signer = new ECDSASigner();
        ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pub), CURVE);
        signer.init(false, params);
        try {
            return signer.verifySignature(data, signature.r, signature.s);
        } catch (NullPointerException e) {
            log.error("Caught NPE inside bouncy castle", e);
            return false;
        }
    }

    private static ECKey signedMessageToKey(String message, String signatureBase64) throws SignatureException {
        if (null == message || null == signatureBase64) {
            return null;
        }
        byte[] signatureEncoded;
        try {
            signatureEncoded = Base64.decode(signatureBase64);
        } catch (RuntimeException e) {
            throw new SignatureException("Could not decode base64", e);
        }

        if (signatureEncoded.length < UNCOMPRESS_PUBKEY_LENGTH) {
            throw new SignatureException("Signature truncated, expected 65 bytes and got " + signatureEncoded.length);
        }

        int header = signatureEncoded[0] & 255;

        if (header < LOW_HEADER_BORDER || header > HIGH_HEADER_BORDER) {
            throw new SignatureException("Header byte out of range: " + header);
        }

        BigInteger r = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 1, 33));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 33, 65));
        ECASSignature sig = new ECASSignature(r, s);
        byte[] messageBytes = CryptoUtils.formatMessageForSigning(message, Charset.forName("UTF-8"), null);

        Sha256Hash messageHash = Sha256Hash.twiceOf(messageBytes);
        boolean compressed = false;
        if (header >= MIDDLE_HEADER_BORDER) {
            compressed = true;
            header -= 4;
        }
        int recId = header - 27;
        ECKey key = ECKey.recoverFromSignature(recId, sig, messageHash, compressed);
        if (key == null) {
            throw new SignatureException("Could not recover public key from signature");
        }

        return key;
    }

    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        if (null == xBN) {
            return null;
        }
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
        return CURVE.getCurve().decodePoint(compEnc);
    }

    /**
     * Generate the corresponding key according to the generated private key byte[].
     *
     * @param asn1privkey
     * @return
     */
    private static ECKey extractKeyFromASN1(byte[] asn1privkey) {
        if (null == asn1privkey) {
            return null;
        }
        try {
            ASN1InputStream decoder = new ASN1InputStream(asn1privkey);
            DLSequence seq = (DLSequence) decoder.readObject();
            checkArgument(decoder.readObject() == null, "Input contains extra bytes");
            decoder.close();

            checkArgument(seq.size() == 4, "Input does not appear to be an ASN.1 OpenSSL EC private key");

            checkArgument(((ASN1Integer) seq.getObjectAt(0)).getValue().equals(BigInteger.ONE),
                    "Input is of wrong version");

            byte[] privbits = ((ASN1OctetString) seq.getObjectAt(1)).getOctets();
            BigInteger privkey = new BigInteger(1, privbits);

            ASN1TaggedObject pubkey = (ASN1TaggedObject) seq.getObjectAt(3);
            checkArgument(pubkey.getTagNo() == 1, "Input has 'publicKey' with bad tag number");
            byte[] pubbits = ((DERBitString) pubkey.getObject()).getBytes();
            checkArgument(pubbits.length == 33 || pubbits.length == 65, "Input has 'publicKey' with invalid length");
            int encoding = pubbits[0] & 0xFF;

            checkArgument(encoding >= 2 && encoding <= 4, "Input has 'publicKey' with invalid encoding");


            boolean compressed = (pubbits.length == 33);

            KeyPair pair = new KeyPair();
            pair.setPriKey(privkey.toString());

            ECKey key = new ECKey(pair, compressed);
            if (!Arrays.equals(key.getPublicKey(), pubbits)) {
                throw new IllegalArgumentException("Public key in ASN.1 structure does not match private key.");
            }
            return key;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The corresponding key is generated according to the byte[] private key passed in by the user.
     *
     * @param privKeyBytes
     * @return
     */
    private static ECKey fromPrivateKey(byte[] privKeyBytes) {
        if (null == privKeyBytes) {
            return null;
        }
        return fromPrivateKey(new BigInteger(1, privKeyBytes));
    }

    /**
     * The corresponding key is generated according to the Integer private key passed in by the user,
     * where the public key is compressed.
     *
     * @param privKey
     * @return
     */
    private static ECKey fromPrivateKey(BigInteger privKey) {
        if (null == privKey) {
            return null;
        }
        return fromPrivateKey(privKey, true);
    }

    /**
     * The corresponding key is generated based on the user's incoming Integer private key and the way to generate the public key.
     *
     * @param privKey
     * @param compressed
     * @return
     */
    private static ECKey fromPrivateKey(BigInteger privKey, boolean compressed) {
        if (null == privKey) {
            return null;
        }
        ECPoint point = publicPointFromPrivateKey(privKey);
        return new ECKey(privKey, getPointWithCompression(point, compressed));
    }

    /**
     * The corresponding key is generated based on the user's incoming byte[] private key and the way to generate the public key.
     *
     * @param privKeyBytes
     * @param compressed
     * @return
     */
    private static ECKey fromPrivateKey(byte[] privKeyBytes, boolean compressed) {
        if (null == privKeyBytes) {
            return null;
        }
        return fromPrivateKey(new BigInteger(1, privKeyBytes), compressed);
    }

    /**
     * Based on the byte[] public key passed in by the user, the generation of key objects can only be used for signature verification.
     *
     * @param pub
     * @return
     */
    private static ECKey fromPublicKeyOnly(byte[] pub) {
        if (null == pub) {
            return null;
        }
        return new ECKey(null, CURVE.getCurve().decodePoint(pub));
    }

    private static ECPoint getPointWithCompression(ECPoint point, boolean compressed) {
        if (null == point) {
            return null;
        }
        if (point.isCompressed() == compressed) {
            return point;
        }
        point = point.normalize();
        BigInteger x = point.getAffineXCoord().toBigInteger();
        BigInteger y = point.getAffineYCoord().toBigInteger();
        return CURVE.getCurve().createPoint(x, y, compressed);
    }

    public boolean verify(byte[] hash, byte[] signature) {
        return ECKey.verify(hash, signature, getPublicKey());
    }

    /**
     * Returns the private key of the Integer type to the user.
     *
     * @return
     */
    public BigInteger getPrivateKeyByBigInteger() {
        if (this.priv == null) {
            throw new MissingPrivateKeyException();
        }
        return this.priv;
    }

    public String signMessage(String message) {
        if (message == null) {
            String error = "params is null";
            throw new ParamsErrorException(error);
        }
        byte[] data = CryptoUtils.formatMessageForSigning(message, Charset.forName("UTF-8"), null);
        Sha256Hash hash = Sha256Hash.twiceOf(data);
        return signMessage(hash, null);
    }

    public boolean isCompressed() {
        return this.pub.isCompressed();
    }

    /**
     * Returns the private key to the byte[] format for the user.
     *
     * @return
     */
    private byte[] getPrivateKeyBytes() {
        return CryptoUtils.bigIntegerToBytes(getPrivateKeyByBigInteger(), 32);
    }

    private byte[] getPublicKey() {
        return pub.getEncoded();
    }

    private byte[] getPublicKeyHash() {
        return CryptoUtils.sha256hash160(this.pub.getEncoded());
    }

    private String signMessage(Sha256Hash messageHash, KeyParameter aesKey) {
        if (null == messageHash) {
            String error = "params is null";
            throw new ParamsErrorException(error);
        }
        ECASSignature sig = sign(messageHash, aesKey);
        int recId = -1;
        for (int i = 0; i < LOOP_COUNT; i++) {
            ECKey k = ECKey.recoverFromSignature(i, sig, messageHash, isCompressed());
            if (k != null && k.pub.equals(pub)) {
                recId = i;
                break;
            }
        }
        if (recId == -1) {
            throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
        }
        int headerByte = recId + 27 + (isCompressed() ? 4 : 0);
        // 1 header + 32 bytes for R + 32 bytes
        byte[] sigData = new byte[65];
        // for S
        sigData[0] = (byte) headerByte;
        System.arraycopy(CryptoUtils.bigIntegerToBytes(sig.r, 32), 0, sigData, 1, 32);
        System.arraycopy(CryptoUtils.bigIntegerToBytes(sig.s, 32), 0, sigData, 33, 32);
        return new String(Base64.encode(sigData), Charset.forName("UTF-8"));
    }

    private ECASSignature sign(Sha256Hash input, KeyParameter aesKey) throws ECKeyCrypterException {
        return doSign(input, this.priv);
    }

    private ECASSignature doSign(Sha256Hash input, BigInteger privateKeyForSigning) {
        if (Secp256k1Context.isEnabled()) {
            try {
                byte[] signature = NativeSecp256k1.sign(input.getBytes(),
                        CryptoUtils.bigIntegerToBytes(privateKeyForSigning, 32));
                return ECASSignature.decodeFromDER(signature);
            } catch (NativeSecp256k1Util.AssertFailException e) {
                log.error("Caught AssertFailException inside secp256k1", e);
                throw new RuntimeException(e);
            }
        }
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKeyForSigning, CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(input.getBytes());
        return new ECASSignature(components[0], components[1]).toCanonicalised();
    }

    /**
     * Returns the private key of hexadecimal to the user.
     *
     * @return
     */
    private String getPrivateKeyAsHex() {
        return CryptoUtils.HEX.encode(getPrivateKeyBytes());
    }

    public String toBase58Address() {
        byte[] hash160 = getPublicKeyHash();
        byte[] bytes = hash160;

        Preconditions.checkArgument(hash160.length == 20, "Addresses are 160-bit hashes, so you must provide 20 bytes");
        byte[] addressBytes = new byte[1 + bytes.length + 4];
        //address version ADDRESS_HEADER_NORMAL
        addressBytes[0] = ADDRESS_HEADER_TEST;
        //addressBytes[0] = ADDRESS_HEADER_NORMAL;
        System.arraycopy(bytes, 0, addressBytes, 1, bytes.length);
        byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, bytes.length + 1);
        System.arraycopy(checksum, 0, addressBytes, bytes.length + 1, 4);
        return Base58.encode(addressBytes);
    }

    public boolean verifySign(String message, String signature) {
        if (null == message || null == signature) {
            return false;
        }
        try {
            verifyMessage(message, signature);
            return true;
        } catch (SignatureException e) {
            return false;
        }
    }

    private void verifyMessage(String message, String signatureBase64) throws SignatureException {
        ECKey key = ECKey.signedMessageToKey(message, signatureBase64);
        if (null == key) {
            throw new SignatureException("cannot produce key");
        }
        if (!key.pub.equals(pub)) {
            throw new SignatureException("Signature did not match for message");
        }
    }

    private String getPublicKeyAsHex() {
        return CryptoUtils.HEX.encode(pub.getEncoded());
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public static class MissingPrivateKeyException extends RuntimeException {
    }

    public static class KeyIsEncryptedException extends MissingPrivateKeyException {
    }

    public static class ECASSignature {
        public final BigInteger r, s;

        public ECASSignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        public static ECASSignature decodeFromDER(byte[] bytes) throws IllegalArgumentException {
            if (null == bytes) {
                return null;
            }
            ASN1InputStream decoder = null;
            try {
                decoder = new ASN1InputStream(bytes);
                final ASN1Primitive seqObj = decoder.readObject();
                if (seqObj == null) {
                    throw new IllegalArgumentException("Reached past end of ASN.1 stream.");
                }

                if (!(seqObj instanceof DLSequence)) {
                    throw new IllegalArgumentException("Read unexpected class: " + seqObj.getClass().getName());
                }

                final DLSequence seq = (DLSequence) seqObj;
                ASN1Integer r, s;
                try {
                    r = (ASN1Integer) seq.getObjectAt(0);
                    s = (ASN1Integer) seq.getObjectAt(1);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(e);
                }
                return new ECASSignature(r.getPositiveValue(), s.getPositiveValue());
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            } finally {
                if (decoder != null) {
                    try {
                        decoder.close();
                    } catch (IOException x) {
                    }
                }
            }
        }

        public boolean isCanonical() {
            return s.compareTo(HALF_CURVE_ORDER) <= 0;
        }

        public ECASSignature toCanonicalised() {
            if (!isCanonical()) {
                return new ECASSignature(r, CURVE.getN().subtract(s));
            } else {
                return this;
            }
        }

        public byte[] encodeToDER() {
            try {
                return derByteStream().toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        protected ByteArrayOutputStream derByteStream() throws IOException {
            // Usually 70-72 bytes.
            ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
            DERSequenceGenerator seq = new DERSequenceGenerator(bos);
            seq.addObject(new ASN1Integer(r));
            seq.addObject(new ASN1Integer(s));
            seq.close();
            return bos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ECASSignature other = (ECASSignature) o;
            return r.equals(other.r) && s.equals(other.s);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(r, s);
        }
    }

}
