package grafo;

import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5 {
    public static int md5(String vertice, String numServidores) throws Exception{
       MessageDigest m=MessageDigest.getInstance("MD5");
       m.update(vertice.getBytes(),0,vertice.length());
       return (new BigInteger(1,m.digest()).mod(new BigInteger(numServidores))).intValue();
    }
} 