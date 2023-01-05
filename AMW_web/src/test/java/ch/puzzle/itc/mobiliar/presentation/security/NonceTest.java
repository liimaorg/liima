package ch.puzzle.itc.mobiliar.presentation.security;

import org.junit.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class NonceTest {

    @Test
    public void shouldGetNextNonce() {
        Nonce nonce = Nonce.next();
        Nonce nonce2 = Nonce.next();
        assertNotEquals(nonce2, nonce);
    }

    @Test
    public void shouldGet32BitNonce() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        Nonce nonce = Nonce.next();
        assertEquals(nonce.toString().length(), Base64.getEncoder().encodeToString(bytes).length());
    }

}
