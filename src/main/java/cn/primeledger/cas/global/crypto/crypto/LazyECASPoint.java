package cn.primeledger.cas.global.crypto.crypto;

import org.jetbrains.annotations.Nullable;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECFieldElement;
import org.spongycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author kongyu
 * @create 2018-02-24 10:20
 */
public class LazyECASPoint {
    private final ECCurve curve;
    private final byte[] bits;

    @Nullable
    private ECPoint point;

    public LazyECASPoint(ECCurve curve, byte[] bits) {
        this.curve = curve;
        this.bits = bits;
    }

    public LazyECASPoint(ECPoint point) {
        this.point = checkNotNull(point);
        this.curve = null;
        this.bits = null;
    }

    public ECPoint get() {
        if (point == null) {
            point = curve.decodePoint(bits);
        }
        return point;
    }

    public byte[] getEncoded() {
        if (bits != null) {
            return Arrays.copyOf(bits, bits.length);
        } else {
            return get().getEncoded();
        }
    }

    public boolean isCompressed() {
        if (bits != null) {
            return bits[0] == 2 || bits[0] == 3;
        } else {
            return get().isCompressed();
        }
    }

    public boolean equals(ECPoint other) {
        return get().equals(other);
    }

    public byte[] getEncoded(boolean compressed) {
        if (compressed == isCompressed() && bits != null) {
            return Arrays.copyOf(bits, bits.length);
        } else {
            return get().getEncoded(compressed);
        }
    }

    public ECPoint add(ECPoint b) {
        return get().add(b);
    }

    public ECPoint normalize() {
        return get().normalize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(getCanonicalEncoding(), ((LazyECASPoint) o).getCanonicalEncoding());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getCanonicalEncoding());
    }

    private byte[] getCanonicalEncoding() {
        return getEncoded(true);
    }
}
