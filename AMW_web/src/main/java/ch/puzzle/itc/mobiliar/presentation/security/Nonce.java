package ch.puzzle.itc.mobiliar.presentation.security;

import org.apache.commons.lang3.ArrayUtils;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public class Nonce {

    protected static final int NONCE_SIZE = 32;
    private static final Random RND = new SecureRandom();

    private final byte[] nonce;


    private Nonce(final byte[] nonce) {
        this.nonce = ArrayUtils.clone(nonce);
    }

    /**
     * Creates a new nonce with a randomly generated 256-bit (32-byte)
     * value, Base64URL-encoded.
     */
    public static Nonce next() {
        byte[] bytes = new byte[NONCE_SIZE];
        RND.nextBytes(bytes);

        return new Nonce(bytes);
    }

    /**
     * Converts nonce to Base64URL-encoded string
     */
    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(nonce);
    }
}
