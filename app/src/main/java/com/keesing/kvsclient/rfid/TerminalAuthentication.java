package com.keesing.kvsclient.rfid;

import com.secunet.epassportapi.CVCertificate;
import com.secunet.epassportapi.CertificateChain;
import com.secunet.epassportapi.TerminalAuthenticationBase;

import java.util.Arrays;

public class TerminalAuthentication extends TerminalAuthenticationBase {

    // you can return true here if an external PKI requires a pre-hashed challenge and you don't want to implement the hashing algorithm in your application
    public boolean requiresHashedChallenge() {
        return false;
    }

    // return a certificate chain matching to one of the provided certificate authority references, or throw a RuntimeException if no such chain can be found
    public CertificateChain getCertificateChain(byte[][] certificateAuthorityReferences) {
        CertificateChain r = new CertificateChain();
        //r.setCertificateChain(... (certificate chain from selected CAR to inspection system certificate, not including the referenced CVCA certificate) ...);
        //r.setCertificateAuthorityReference(... (selected CAR) ...);
        return r;
    }

    // sign the challenge from the chip for the terminal authentication procedure
    public byte[] sign(byte[] certificateHolderReference, byte[] challenge) {
        //CVCertificate isCertificate = new CVCertificate();
        //if((Arrays.equals(certificateHolderReference, isCertificate.getCertificateHolderReference())) {
        //byte[] response = isCertificate.sign(challenge, ... (private key) ...);
        return null;
        //}
        // else throw new RuntimeException("unknown inspection system certificate, cannot sign challenge");
    }

}
