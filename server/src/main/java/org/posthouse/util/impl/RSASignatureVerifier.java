package org.posthouse.util.impl;

import io.netty.util.CharsetUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jentiti.annotation.Singleton;
import org.posthouse.util.intf.SignatureVerifier;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Singleton("signatureVerifier")
public class RSASignatureVerifier implements SignatureVerifier {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public boolean verify(String msgBody, String signature, String publicKey) throws NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        Signature verifier = Signature.getInstance("RSA", "BC");
        byte[] decodedKey = Base64.getDecoder().decode(publicKey);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
        PublicKey receivedPublicKey = keyFactory.generatePublic(keySpec);

        verifier.initVerify(receivedPublicKey);
        verifier.update(msgBody.getBytes(CharsetUtil.UTF_8));

        return verifier.verify(Base64.getDecoder().decode(signature));
    }
}
